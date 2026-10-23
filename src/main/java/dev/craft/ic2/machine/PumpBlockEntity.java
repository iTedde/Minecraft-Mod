package dev.craft.ic2.machine;

import dev.craft.core.energy.ConsumptionReadout;
import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySink;
import dev.craft.core.energy.EnergyTile;
import dev.craft.core.energy.OutputMeter;
import dev.craft.core.energy.OutputReadout.OutputReading;
import dev.craft.core.explosion.Ic2Explosion;
import dev.craft.ic2.Ic2BlockEntityTypes;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2Sounds;
import dev.craft.ic2.generator.GeothermalGeneratorBlockEntity;
import dev.craft.ic2.item.ElectricItem;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.menu.PumpMenu;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/**
 * IC2 {@code TileEntityPump}: charges for 200 ticks (1 EU each), then takes the water or lava source block below
 * into a cell, a bucket or an adjacent geothermal generator (spec 04, pump). Addition (user, 2026-10-23): when none of
 * these takes it and a liquid handler is adjacent, the source goes into a one-bucket buffer that is pushed into
 * adjacent tanks, pipes and machines and can be drained (wooden pipes).
 */
public class PumpBlockEntity extends MachineBlockEntity implements EnergySink, ConsumptionReadout {
    public static final int SLOT_CONTAINER = 0;
    public static final int SLOT_BATTERY = 1;
    public static final int MAX_ENERGY = 200;
    public static final int MAX_INPUT = 32;
    public static final int TIER = 1;
    public static final int CHARGE_READY = 200;
    /** IC2 order for chests and geothermal generators: up, down, +X, −X, +Z, −Z. */
    private static final Direction[] NEIGHBOURS = {Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST,
            Direction.SOUTH, Direction.NORTH};

    public static final int BUFFER = FluidType.BUCKET_VOLUME;

    private final OutputMeter consumptionMeter = new OutputMeter();
    private final FluidTank buffer = new FluidTank(BUFFER) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };
    private final IFluidHandler fluidHandler = new BufferHandler();
    private int energy;
    /** IC2 keeps the charge in a {@code short}. */
    private short pumpCharge;

    public PumpBlockEntity(BlockPos pos, BlockState state) {
        super(Ic2BlockEntityTypes.get(Ic2Machine.PUMP), pos, state, 2);
    }

    public int getEnergy() {
        return energy;
    }

    public int getPumpCharge() {
        return pumpCharge;
    }

    public boolean isPumpReady() {
        return pumpCharge >= CHARGE_READY;
    }

    /** Used by the compressor (snowballs from water below a ready pump). */
    public void resetCharge() {
        pumpCharge = 0;
        setChanged();
    }

    @Override
    public void serverTick() {
        boolean needsInvUpdate = false;
        if (energy > 0 && !isPumpReady()) {
            energy--;
            consumptionMeter.record(level, 1);
            pumpCharge++;
        }
        if (energy <= MAX_ENERGY) {
            needsInvUpdate = provideEnergy();
        }
        if (isPumpReady()) {
            needsInvUpdate = pump();
        }
        pushBuffer();
        if (isActive() == isPumpReady() && energy > 0) {
            setActive(!isActive());
        }
        if (needsInvUpdate) {
            onInventoryChanged();
        } else {
            setChanged();
        }
    }

    @Override
    public void clientTick() {
        MachineSoundHooks.updateLoop(this, Ic2Sounds.PUMP_OP.get(), isActive());
    }

    /** IC2 {@code TileEntityElecMachine.provideEnergy} (redstone adds the full capacity, no cap). */
    private boolean provideEnergy() {
        ItemStack stack = inventory.get(SLOT_BATTERY);
        if (stack.isEmpty()) {
            return false;
        }
        if (stack.getItem() instanceof ElectricItem item) {
            if (!item.canProvideEnergy()) {
                return false;
            }
            int transfer = ElectricItems.discharge(stack, MAX_ENERGY - energy, TIER, false, false);
            energy += transfer;
            return transfer > 0;
        }
        if (stack.is(Items.REDSTONE)) {
            energy += MAX_ENERGY;
            stack.shrink(1);
            return true;
        }
        if (stack.is(Ic2Items.SU_BATTERY.get())) {
            energy += 1000;
            stack.shrink(1);
            return true;
        }
        return false;
    }

    // --- pumping ---

    private boolean pump() {
        if (!canHarvest()) {
            return false;
        }
        BlockPos below = worldPosition.below();
        boolean water = isWaterBelow();
        if (!water && !isLavaBelow()) {
            return pumpOtherFluid(below);
        }
        level.setBlock(below, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        return pumpThis(water);
    }

    /**
     * IC2 posts a Forge {@code FillBucketEvent} with an empty bucket from slot 0 (e.g. BuildCraft oil). Here: any
     * other fluid source block that can be picked up into a bucket.
     */
    private boolean pumpOtherFluid(BlockPos below) {
        ItemStack container = inventory.get(SLOT_CONTAINER);
        BlockState state = level.getBlockState(below);
        if (!(state.getBlock() instanceof LiquidBlock liquid) || !state.getFluidState().isSource()) {
            return false;
        }
        if (!container.is(Items.BUCKET)) {
            if (!canFillBuffer()) {
                return false;
            }
            buffer.fill(new FluidStack(state.getFluidState().getType(), BUFFER), IFluidHandler.FluidAction.EXECUTE);
            level.setBlock(below, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            pumpCharge = 0;
            return true;
        }
        ItemStack filled = liquid.pickupBlock(null, level, below, state);
        if (filled.isEmpty() || filled.is(Items.BUCKET)) {
            return false;
        }
        List<ItemStack> drops = new ArrayList<>();
        drops.add(filled);
        StackUtil.distributeDrop(level, worldPosition, drops);
        container.shrink(1);
        inventory.set(SLOT_CONTAINER, container.isEmpty() ? ItemStack.EMPTY : container);
        pumpCharge = 0;
        return true;
    }

    /** Also used by the miner, which removes the source block itself. */
    public boolean pumpThis(boolean water) {
        if (!water && deliverLavaToGeo()) {
            pumpCharge = 0;
            return true;
        }
        ItemStack container = inventory.get(SLOT_CONTAINER);
        if (container.is(Items.BUCKET)) {
            // Original quirk: the whole stack of empty buckets is filled by one source block.
            Item filled = water ? Items.WATER_BUCKET : Items.LAVA_BUCKET;
            List<ItemStack> drops = new ArrayList<>();
            drops.add(new ItemStack(filled, container.getCount()));
            inventory.set(SLOT_CONTAINER, ItemStack.EMPTY);
            StackUtil.distributeDrop(level, worldPosition, drops);
            pumpCharge = 0;
            return true;
        }
        if (container.is(Ic2Items.EMPTY_CELL.get())) {
            container.shrink(1);
            inventory.set(SLOT_CONTAINER, container.isEmpty() ? ItemStack.EMPTY : container);
            List<ItemStack> drops = new ArrayList<>();
            drops.add(new ItemStack(water ? Ic2Items.WATER_CELL.get() : Ic2Items.LAVA_CELL.get()));
            StackUtil.distributeDrop(level, worldPosition, drops);
            pumpCharge = 0;
            return true;
        }
        pumpCharge = 0;
        if (putInChestBucket(water)) {
            return true;
        }
        if (canFillBuffer()) {
            buffer.fill(new FluidStack(water ? Fluids.WATER : Fluids.LAVA, BUFFER), IFluidHandler.FluidAction.EXECUTE);
            return true;
        }
        return false;
    }

    // --- liquid buffer (addition) ---

    /** The buffer is empty and something next to the pump takes liquids. */
    private boolean canFillBuffer() {
        return buffer.isEmpty() && level != null && hasLiquidNeighbour();
    }

    private boolean hasLiquidNeighbour() {
        for (Direction direction : NEIGHBOURS) {
            if (neighbourHandler(direction) != null) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    private IFluidHandler neighbourHandler(Direction direction) {
        return level.getCapability(Capabilities.FluidHandler.BLOCK, worldPosition.relative(direction),
                direction.getOpposite());
    }

    /** Pushes the buffer into the neighbours in IC2's side order, like the BuildCraft pump. */
    private void pushBuffer() {
        for (Direction direction : NEIGHBOURS) {
            if (buffer.isEmpty()) {
                return;
            }
            IFluidHandler handler = neighbourHandler(direction);
            if (handler != null) {
                int moved = handler.fill(buffer.getFluid().copy(), IFluidHandler.FluidAction.EXECUTE);
                buffer.drain(moved, IFluidHandler.FluidAction.EXECUTE);
            }
        }
    }

    /** Capability: drain only. */
    public IFluidHandler getFluidHandler() {
        return fluidHandler;
    }

    public FluidStack getBufferedFluid() {
        return buffer.getFluid();
    }

    /**
     * IC2 {@code putInChestBucket}: the first empty bucket stack in the first adjacent chest becomes filled buckets
     * (the whole stack). Filled buckets stack to 1 in 1.21, so the slot keeps one and the rest is distributed.
     */
    private boolean putInChestBucket(boolean water) {
        for (Direction direction : NEIGHBOURS) {
            if (!(level.getBlockEntity(worldPosition.relative(direction)) instanceof ChestBlockEntity chest)) {
                continue;
            }
            for (int i = 0; i < chest.getContainerSize(); i++) {
                ItemStack stack = chest.getItem(i);
                if (stack.is(Items.BUCKET)) {
                    Item filled = water ? Items.WATER_BUCKET : Items.LAVA_BUCKET;
                    int count = stack.getCount();
                    chest.setItem(i, new ItemStack(filled));
                    if (count > 1) {
                        List<ItemStack> drops = new ArrayList<>();
                        drops.add(new ItemStack(filled, count - 1));
                        StackUtil.distributeDrop(level, worldPosition, drops);
                    }
                    return true;
                }
            }
            return false;
        }
        return false;
    }

    /** IC2 {@code deliverLavaToGeo}: offers 3000 mB in turn; success when more than 20 mB were taken. */
    private boolean deliverLavaToGeo() {
        int lava = 3000;
        for (Direction direction : NEIGHBOURS) {
            if (lava > 0 && level.getBlockEntity(worldPosition.relative(direction))
                    instanceof GeothermalGeneratorBlockEntity geo) {
                lava = geo.distributeLava(lava);
            }
        }
        return lava < 2980;
    }

    public boolean canHarvest() {
        if (!isPumpReady()) {
            return false;
        }
        ItemStack container = inventory.get(SLOT_CONTAINER);
        return container.is(Ic2Items.EMPTY_CELL.get()) || container.is(Items.BUCKET) || isBucketInChestAvailable()
                || canFillBuffer();
    }

    /** Note: IC2 stops at the first adjacent chest only in {@code putInChestBucket}, not here. */
    private boolean isBucketInChestAvailable() {
        for (Direction direction : NEIGHBOURS) {
            if (level.getBlockEntity(worldPosition.relative(direction)) instanceof ChestBlockEntity chest) {
                for (int i = 0; i < chest.getContainerSize(); i++) {
                    if (chest.getItem(i).is(Items.BUCKET)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isWaterBelow() {
        return isSourceBelow(Blocks.WATER);
    }

    public boolean isLavaBelow() {
        return isSourceBelow(Blocks.LAVA);
    }

    /** IC2 compares the block ID (still or flowing) and metadata 0, i.e. a source block. */
    private boolean isSourceBelow(Block fluidBlock) {
        if (level == null) {
            return false;
        }
        BlockState state = level.getBlockState(worldPosition.below());
        FluidState fluid = state.getFluidState();
        return state.is(fluidBlock) && fluid.isSource();
    }

    // --- inventory ---

    /** IC2 {@code getStartInventorySide}: bottom → battery, every other side → container slot. */
    @Override
    protected int getSlotForSide(Direction side) {
        return side == Direction.DOWN ? SLOT_BATTERY : SLOT_CONTAINER;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new PumpMenu(id, playerInventory, this);
    }

    // --- energy net ---

    @Override
    public void onLoad() {
        super.onLoad();
        EnergyNets.add(level, this);
    }

    @Override
    public void setRemoved() {
        EnergyNets.remove(level, this);
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        EnergyNets.remove(level, this);
        super.onChunkUnloaded();
    }

    @Override
    public boolean acceptsEnergyFrom(EnergyTile emitter, Direction direction) {
        return true;
    }

    @Override
    public int demandsEnergy() {
        return isRemoved() ? 0 : MAX_ENERGY - energy;
    }

    @Override
    public int injectEnergy(Direction direction, int amount) {
        if (isRemoved()) {
            return amount;
        }
        if (amount > MAX_INPUT) {
            if (level instanceof ServerLevel serverLevel) {
                Ic2Explosion.explodeMachineAt(serverLevel, worldPosition);
            }
            return 0;
        }
        energy += amount;
        int rest = 0;
        if (energy > MAX_ENERGY) {
            rest = energy - MAX_ENERGY;
            energy = MAX_ENERGY;
        }
        setChanged();
        return rest;
    }

    @Override
    public int getMaxSafeInput() {
        return MAX_INPUT;
    }

    // --- persistence ---

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy = tag.getInt("energy");
        pumpCharge = tag.getShort("pumpCharge");
        buffer.readFromNBT(registries, tag.getCompound("buffer"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", energy);
        tag.putShort("pumpCharge", pumpCharge);
        tag.put("buffer", buffer.writeToNBT(registries, new CompoundTag()));
    }

    /** Test hooks. */
    public void setEnergyForTest(int energy) {
        this.energy = energy;
    }

    public void setPumpChargeForTest(int charge) {
        this.pumpCharge = (short) charge;
    }

    @Override
    public OutputReading getConsumptionReading() {
        return consumptionMeter.read(level, OutputReading.Unit.EU);
    }

    private final class BufferHandler implements IFluidHandler {
        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return buffer.getFluid();
        }

        @Override
        public int getTankCapacity(int tank) {
            return BUFFER;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return true;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return buffer.drain(resource, action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return buffer.drain(maxDrain, action);
        }
    }
}
