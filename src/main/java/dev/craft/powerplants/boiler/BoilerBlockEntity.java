package dev.craft.powerplants.boiler;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import dev.craft.buildcraft.fluid.LiquidContainers;
import dev.craft.core.explosion.Ic2Explosion;
import dev.craft.core.multiblock.BlockMatcher;
import dev.craft.core.multiblock.MultiblockPattern;
import dev.craft.gregtech.GtItems;
import dev.craft.powerplants.PpBlockEntityTypes;
import dev.craft.powerplants.PpBlocks;
import dev.craft.powerplants.PpFluids;
import dev.craft.powerplants.PpItems;
import dev.craft.powerplants.block.PlantControllerBlockEntity;
import dev.craft.powerplants.block.SlotAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Solid fuel boiler of the coal power plant (spec 14 §3): 3×3×3 of boiler casings with an air core, the firebox in the
 * middle of one side. Burns coal or coke at full rate, heats up by 1 °C per tick up to 1000 °C and turns water into
 * steam above 100 °C (20 mB/t at 1000 °C). Pushes steam into adjacent fluid receivers and explodes when water reaches
 * a dry, hot boiler.
 */
public class BoilerBlockEntity extends PlantControllerBlockEntity {
    public static final int SLOT_FUEL = 0;
    public static final int SLOT_ASH = 1;
    public static final int SLOT_CONTAINER_IN = 2;
    public static final int SLOT_CONTAINER_OUT = 3;
    public static final int TANK_CAPACITY = 16000;
    public static final int COAL_BURN_TIME = 800;
    public static final int COKE_BURN_TIME = 1600;
    public static final int MIN_TEMPERATURE = 20;
    public static final int MAX_TEMPERATURE = 1000;
    public static final int BOILING_POINT = 100;
    /** Steam per tick at {@link #MAX_TEMPERATURE}; scales linearly from the boiling point. */
    public static final int MAX_STEAM_RATE = 20;
    public static final int MAX_PUSH = 1000;
    public static final int ASH_PER_ITEM = 4;
    public static final float EXPLOSION_POWER = 6.0F;
    private static final int STEAM_DIVISOR = MAX_TEMPERATURE - BOILING_POINT;

    private static final MultiblockPattern PATTERN = MultiblockPattern.builder()
            .layer("BBB", "BBB", "BBB")
            .layer("BCB", "BAB", "BBB")
            .layer("BBB", "BBB", "BBB")
            .where('B', BlockMatcher.blocks(PpBlocks.BOILER_CASING.get()))
            .where('A', BlockMatcher.AIR)
            .controller('C', BlockMatcher.blocks(PpBlocks.BOILER_FIREBOX.get()))
            .build();

    private final IFluidHandler tanks = new Tanks();
    /** Outer faces of the structure that steam is pushed through, in round-robin order. */
    private final List<Face> outerFaces = new ArrayList<>();
    private int pushIndex;

    private int water;
    private int steam;
    private int temperature = MIN_TEMPERATURE;
    private int burnTime;
    private int maxBurnTime;
    private int ashCounter;
    /** Steam production numerator carried over between ticks (denominator {@link #STEAM_DIVISOR}). */
    private int steamRemainder;
    private boolean dry;

    private record Face(BlockPos pos, Direction side) {
    }

    public BoilerBlockEntity(BlockPos pos, BlockState state) {
        super(PpBlockEntityTypes.BOILER.get(), pos, state, 4);
    }

    public static boolean isFuel(ItemStack stack) {
        return stack.is(Items.COAL) || stack.is(PpItems.COKE.get());
    }

    public int getWater() {
        return water;
    }

    public int getSteam() {
        return steam;
    }

    public int getTemperature() {
        return temperature;
    }

    public int getBurnTime() {
        return burnTime;
    }

    public int getMaxBurnTime() {
        return maxBurnTime;
    }

    public boolean isDry() {
        return dry;
    }

    void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    void setWater(int water) {
        this.water = water;
    }

    void setAshCounter(int ashCounter) {
        this.ashCounter = ashCounter;
    }

    public IFluidHandler getTanks() {
        return tanks;
    }

    @Override
    protected MultiblockPattern getPattern() {
        return PATTERN;
    }

    /** Centre of the cube: the air block behind the firebox. */
    public BlockPos getCore() {
        return worldPosition.relative(getFacing().getOpposite());
    }

    @Override
    protected void onStructureChecked(MultiblockPattern.Match match) {
        BlockPos core = getCore();
        Set<BlockPos> cube = new HashSet<>();
        for (BlockPos pos : BlockPos.betweenClosed(core.offset(-1, -1, -1), core.offset(1, 1, 1))) {
            cube.add(pos.immutable());
        }
        outerFaces.clear();
        for (BlockPos pos : cube) {
            for (Direction direction : Direction.values()) {
                BlockPos neighbour = pos.relative(direction);
                if (!cube.contains(neighbour)) {
                    outerFaces.add(new Face(neighbour, direction.getOpposite()));
                }
            }
        }
    }

    @Override
    protected void tickFormed() {
        if (dry && water > 0) {
            explode();
            return;
        }
        fillFromContainer();
        ItemStack fuel = inventory.get(SLOT_FUEL);
        if (burnTime <= 0 && steam < TANK_CAPACITY && water > 0
                && inventory.get(SLOT_ASH).getCount() < inventory.get(SLOT_ASH).getMaxStackSize() && isFuel(fuel)) {
            boolean coke = fuel.is(PpItems.COKE.get());
            burnTime = maxBurnTime = coke ? COKE_BURN_TIME : COAL_BURN_TIME;
            ashCounter += coke ? 2 : 1;
            fuel.shrink(1);
            inventory.notifyChanged(SLOT_FUEL);
            if (ashCounter >= ASH_PER_ITEM) {
                ashCounter -= ASH_PER_ITEM;
                addAsh();
            }
        }
        if (burnTime > 0) {
            burnTime--;
            temperature = Math.min(temperature + 1, MAX_TEMPERATURE);
        } else {
            temperature = Math.max(temperature - 1, MIN_TEMPERATURE);
        }
        produceSteam();
        if (water == 0 && temperature > BOILING_POINT) {
            dry = true;
        } else if (temperature <= BOILING_POINT) {
            dry = false;
        }
        if (dry && temperature >= MAX_TEMPERATURE) {
            explode();
            return;
        }
        pushSteam();
        setActive(burnTime > 0);
        setChanged();
    }

    private void produceSteam() {
        if (temperature <= BOILING_POINT) {
            steamRemainder = 0;
            return;
        }
        steamRemainder += MAX_STEAM_RATE * (temperature - BOILING_POINT);
        int produced = steamRemainder / STEAM_DIVISOR;
        steamRemainder %= STEAM_DIVISOR;
        produced = Math.min(produced, Math.min(water, TANK_CAPACITY - steam));
        water -= produced;
        steam += produced;
    }

    private void addAsh() {
        ItemStack ash = inventory.get(SLOT_ASH);
        if (ash.isEmpty()) {
            inventory.set(SLOT_ASH, new ItemStack(GtItems.ASH.get()));
        } else if (ash.is(GtItems.ASH.get()) && ash.getCount() < ash.getMaxStackSize()) {
            ash.grow(1);
            inventory.notifyChanged(SLOT_ASH);
        }
    }

    /** Water bucket/cell from the container slot into the tank, the empty container into the output slot. */
    private void fillFromContainer() {
        ItemStack input = inventory.get(SLOT_CONTAINER_IN);
        FluidStack liquid = LiquidContainers.getLiquidForFilledItem(input);
        if (!liquid.is(Fluids.WATER) || TANK_CAPACITY - water < liquid.getAmount()) {
            return;
        }
        ItemStack empty = LiquidContainers.emptyContainer(input);
        ItemStack output = inventory.get(SLOT_CONTAINER_OUT);
        if (!empty.isEmpty() && !output.isEmpty() && (!ItemStack.isSameItemSameComponents(output, empty)
                || output.getCount() >= output.getMaxStackSize())) {
            return;
        }
        water += liquid.getAmount();
        input.shrink(1);
        inventory.notifyChanged(SLOT_CONTAINER_IN);
        if (!empty.isEmpty()) {
            if (output.isEmpty()) {
                inventory.set(SLOT_CONTAINER_OUT, empty);
            } else {
                output.grow(1);
                inventory.notifyChanged(SLOT_CONTAINER_OUT);
            }
        }
    }

    /** Up to {@link #MAX_PUSH} mB per tick into the neighbours, starting one face further every tick. */
    private void pushSteam() {
        if (steam <= 0 || outerFaces.isEmpty()) {
            return;
        }
        int budget = Math.min(steam, MAX_PUSH);
        int size = outerFaces.size();
        for (int i = 0; i < size && budget > 0; i++) {
            Face face = outerFaces.get((pushIndex + i) % size);
            if (!level.isLoaded(face.pos())) {
                continue;
            }
            IFluidHandler target = level.getCapability(Capabilities.FluidHandler.BLOCK, face.pos(), face.side());
            if (target != null) {
                int filled = target.fill(new FluidStack(PpFluids.STEAM.get(), budget),
                        IFluidHandler.FluidAction.EXECUTE);
                steam -= filled;
                budget -= filled;
            }
        }
        pushIndex = (pushIndex + 1) % size;
    }

    /** Destroys every boiler block (and the contents) and explodes with power 6 at the core. */
    private void explode() {
        ServerLevel serverLevel = (ServerLevel) level;
        BlockPos core = getCore();
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            inventory.set(slot, ItemStack.EMPTY);
        }
        for (BlockPos pos : BlockPos.betweenClosed(core.offset(-1, -1, -1), core.offset(1, 1, 1))) {
            BlockState state = serverLevel.getBlockState(pos);
            if (state.is(PpBlocks.BOILER_CASING.get()) || state.is(PpBlocks.BOILER_FIREBOX.get())) {
                serverLevel.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            }
        }
        new Ic2Explosion(serverLevel, core.getX() + 0.5, core.getY() + 0.5, core.getZ() + 0.5, EXPLOSION_POWER,
                Ic2Explosion.MACHINE_DROP_RATE, Ic2Explosion.MACHINE_ENTITY_DAMAGE).explode();
    }

    @Nullable
    @Override
    public IItemHandler getPartItemHandler(@Nullable Direction side) {
        if (side == Direction.DOWN) {
            return SlotAccess.output(inventory, SLOT_ASH);
        }
        return SlotAccess.input(inventory, SLOT_FUEL, BoilerBlockEntity::isFuel);
    }

    @Override
    public IItemHandler getItemHandler(@Nullable Direction side) {
        return side == null ? inventory : getPartItemHandler(side);
    }

    @Override
    public IFluidHandler getPartFluidHandler(@Nullable Direction side) {
        return tanks;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new BoilerMenu(id, playerInventory, this);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        water = tag.getInt("water");
        steam = tag.getInt("steam");
        temperature = tag.contains("temperature") ? tag.getInt("temperature") : MIN_TEMPERATURE;
        burnTime = tag.getInt("burnTime");
        maxBurnTime = tag.getInt("maxBurnTime");
        ashCounter = tag.getInt("ashCounter");
        steamRemainder = tag.getInt("steamRemainder");
        dry = tag.getBoolean("dry");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("water", water);
        tag.putInt("steam", steam);
        tag.putInt("temperature", temperature);
        tag.putInt("burnTime", burnTime);
        tag.putInt("maxBurnTime", maxBurnTime);
        tag.putInt("ashCounter", ashCounter);
        tag.putInt("steamRemainder", steamRemainder);
        tag.putBoolean("dry", dry);
    }

    /** Tank 0 water (fill only), tank 1 steam (drain only). */
    private final class Tanks implements IFluidHandler {
        @Override
        public int getTanks() {
            return 2;
        }

        @Override
        public FluidStack getFluidInTank(int index) {
            if (index == 0) {
                return water > 0 ? new FluidStack(Fluids.WATER, water) : FluidStack.EMPTY;
            }
            return steam > 0 ? new FluidStack(PpFluids.STEAM.get(), steam) : FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int index) {
            return TANK_CAPACITY;
        }

        @Override
        public boolean isFluidValid(int index, FluidStack stack) {
            return index == 0 ? stack.is(Fluids.WATER) : stack.is(PpFluids.STEAM.get());
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || !resource.is(Fluids.WATER)) {
                return 0;
            }
            int filled = Math.min(resource.getAmount(), TANK_CAPACITY - water);
            if (action.execute() && filled > 0) {
                water += filled;
                setChanged();
            }
            return filled;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return resource.is(PpFluids.STEAM.get()) ? drain(resource.getAmount(), action) : FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            int drained = Math.min(maxDrain, steam);
            if (drained <= 0) {
                return FluidStack.EMPTY;
            }
            if (action.execute()) {
                steam -= drained;
                setChanged();
            }
            return new FluidStack(PpFluids.STEAM.get(), drained);
        }
    }
}
