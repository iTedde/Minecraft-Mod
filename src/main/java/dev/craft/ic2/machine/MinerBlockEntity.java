package dev.craft.ic2.machine;

import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySink;
import dev.craft.core.energy.EnergyTile;
import dev.craft.core.explosion.Ic2Explosion;
import dev.craft.ic2.Ic2BlockEntityTypes;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.Ic2Sounds;
import dev.craft.ic2.item.ElectricItem;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.item.tool.DrillItem;
import dev.craft.ic2.item.tool.OreValues;
import dev.craft.ic2.item.tool.ScannerItem;
import dev.craft.ic2.menu.MinerMenu;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * IC2 {@code TileEntityMiner}: lowers a mining pipe, mines the block below the tip (or, with a scanner, the first
 * valuable block of the tip's layer), pumps liquids through an adjacent pump and pulls the pipe back up without a
 * drill (spec 04, miner).
 */
public class MinerBlockEntity extends MachineBlockEntity implements EnergySink {
    public static final int SLOT_BATTERY = 0;
    public static final int SLOT_SCANNER = 1;
    public static final int SLOT_PIPE = 2;
    public static final int SLOT_DRILL = 3;
    public static final int MAX_ENERGY = 1000;
    public static final int MAX_INPUT = 32;
    /** Config {@code enableMinerLapotron=false} in the pack. */
    public static final int TIER = 1;
    /** IC2 uses {@code targetY = -1}; negative heights are valid in 1.21. */
    private static final int NO_TARGET = Integer.MIN_VALUE;
    /** IC2 order for adjacent pumps: up, down, +X, −X, +Z, −Z. */
    private static final Direction[] PUMP_SIDES = {Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST,
            Direction.SOUTH, Direction.NORTH};

    private int energy;
    private int targetX;
    private int targetY = NO_TARGET;
    private int targetZ;
    private short miningTicker;
    private String stuckOn;

    public MinerBlockEntity(BlockPos pos, BlockState state) {
        super(Ic2BlockEntityTypes.get(Ic2Machine.MINER), pos, state, 4);
    }

    public int getEnergy() {
        return energy;
    }

    public int getMiningTicker() {
        return miningTicker;
    }

    public String getStuckOn() {
        return stuckOn;
    }

    @Override
    public void serverTick() {
        boolean wasOperating = isOperating();
        boolean needsInvUpdate = false;
        if (wasOperating) {
            energy--;
            ItemStack scanner = inventory.get(SLOT_SCANNER);
            if (scanner.getItem() instanceof ScannerItem) {
                energy -= ElectricItems.charge(scanner, energy, 2, false, false);
            }
            ItemStack drill = inventory.get(SLOT_DRILL);
            if (drill.getItem() instanceof DrillItem) {
                energy -= ElectricItems.charge(drill, energy, 1, false, false);
            }
        }
        if (energy <= MAX_ENERGY) {
            needsInvUpdate = provideEnergy();
        }
        if (wasOperating) {
            needsInvUpdate = mine();
        } else if (inventory.get(SLOT_DRILL).isEmpty()) {
            if (energy >= 2 && canWithdraw()) {
                targetY = NO_TARGET;
                miningTicker++;
                energy -= 2;
                if (miningTicker >= 20) {
                    miningTicker = 0;
                    needsInvUpdate = withdrawPipe();
                }
            } else if (isStuck()) {
                miningTicker = 0;
            }
        }
        setActive(isOperating());
        if (wasOperating != isOperating()) {
            needsInvUpdate = true;
        }
        if (needsInvUpdate) {
            onInventoryChanged();
        } else {
            setChanged();
        }
    }

    @Override
    public void clientTick() {
        MachineSoundHooks.updateLoop(this, Ic2Sounds.MINER_OP.get(), isActive());
    }

    /** IC2 {@code TileEntityElecMachine.provideEnergy} (redstone adds the full capacity). */
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

    // --- mining ---

    private boolean mine() {
        if (targetY == NO_TARGET) {
            aquireTarget();
            return false;
        }
        if (!canReachTarget(targetX, targetY, targetZ)) {
            int dx = targetX - worldPosition.getX();
            int dz = targetZ - worldPosition.getZ();
            if (Math.abs(dx) > Math.abs(dz)) {
                targetX += dx > 0 ? -1 : 1;
            } else {
                targetZ += dz > 0 ? -1 : 1;
            }
            return false;
        }
        BlockPos target = new BlockPos(targetX, targetY, targetZ);
        if (canMine(target)) {
            stuckOn = null;
            miningTicker++;
            energy--;
            if (inventory.get(SLOT_DRILL).is(Ic2Items.DIAMOND_DRILL.get())) {
                miningTicker += 3;
                energy -= 14;
            }
            if (miningTicker >= 200) {
                miningTicker = 0;
                mineBlock(target);
                return true;
            }
            return false;
        }
        BlockState state = level.getBlockState(target);
        if (isWaterOrLava(state) && isAnyPumpConnected()) {
            return false;
        }
        miningTicker = -1;
        stuckOn = state.getBlock().getDescriptionId();
        return false;
    }

    private void mineBlock(BlockPos target) {
        ItemStack drill = inventory.get(SLOT_DRILL);
        if (drill.is(Ic2Items.MINING_DRILL.get())) {
            ElectricItems.use(drill, 50, null);
        } else if (drill.is(Ic2Items.DIAMOND_DRILL.get())) {
            ElectricItems.use(drill, 80, null);
        }
        BlockState state = level.getBlockState(target);
        boolean liquid = isWaterOrLava(state);
        boolean mined = !state.isAir() && (!liquid || state.getFluidState().isSource());
        if (mined) {
            if (!liquid) {
                List<ItemStack> drops = new ArrayList<>(Block.getDrops(state, (ServerLevel) level, target,
                        level.getBlockEntity(target), null, drill));
                StackUtil.distributeDrop(level, worldPosition, drops);
            } else {
                usePump(state.is(Blocks.WATER));
            }
            level.setBlock(target, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            energy -= 2 * (worldPosition.getY() - targetY);
        }
        if (targetX == worldPosition.getX() && targetZ == worldPosition.getZ()) {
            // Original: the pipe below is placed without taking one from the slot.
            level.setBlock(target, Ic2Blocks.MINING_PIPE.get().defaultBlockState(), Block.UPDATE_ALL);
            energy -= 10;
        }
        updateMineTip(targetY);
        targetY = NO_TARGET;
    }

    private boolean withdrawPipe() {
        int y = getPipeTip();
        BlockPos tip = new BlockPos(worldPosition.getX(), y, worldPosition.getZ());
        BlockState state = level.getBlockState(tip);
        if (!state.isAir()) {
            List<ItemStack> drops = new ArrayList<>(Block.getDrops(state, (ServerLevel) level, tip,
                    level.getBlockEntity(tip)));
            StackUtil.distributeDrop(level, worldPosition, drops);
            level.setBlock(tip, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
        ItemStack filler = inventory.get(SLOT_PIPE);
        if (!filler.isEmpty() && !filler.is(Ic2Items.MINING_PIPE.get()) && filler.getItem() instanceof BlockItem block) {
            level.setBlock(tip, block.getBlock().defaultBlockState(), Block.UPDATE_ALL);
            filler.shrink(1);
            updateMineTip(y + 1);
            return true;
        }
        updateMineTip(y + 1);
        return false;
    }

    /**
     * IC2 {@code updateMineTip}: fills missing pipe between the miner and {@code low} from slot 2 (whatever it holds)
     * and puts the tip at {@code low}.
     */
    private void updateMineTip(int low) {
        if (low == worldPosition.getY()) {
            return;
        }
        int x = worldPosition.getX();
        int z = worldPosition.getZ();
        for (int y = worldPosition.getY() - 1; y > low; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            ItemStack pipes = inventory.get(SLOT_PIPE);
            if (!level.getBlockState(pos).is(Ic2Blocks.MINING_PIPE.get()) && !pipes.isEmpty()) {
                level.setBlock(pos, Ic2Blocks.MINING_PIPE.get().defaultBlockState(), Block.UPDATE_ALL);
                pipes.shrink(1);
            }
        }
        level.setBlock(new BlockPos(x, low, z), Ic2Blocks.MINING_TIP.get().defaultBlockState(), Block.UPDATE_ALL);
    }

    /** IC2 {@code canReachTarget}: walks from the target towards the pipe along the larger difference first. */
    private boolean canReachTarget(int x, int y, int z) {
        boolean first = true;
        while (x != worldPosition.getX() || z != worldPosition.getZ()) {
            if (!first && !canPass(level.getBlockState(new BlockPos(x, y, z)))) {
                return false;
            }
            first = false;
            int dx = x - worldPosition.getX();
            int dz = z - worldPosition.getZ();
            if (Math.abs(dx) > Math.abs(dz)) {
                x += dx > 0 ? -1 : 1;
            } else {
                z += dz > 0 ? -1 : 1;
            }
        }
        return true;
    }

    private void aquireTarget() {
        int y = getPipeTip();
        int x = worldPosition.getX();
        int z = worldPosition.getZ();
        ItemStack scanner = inventory.get(SLOT_SCANNER);
        if (y < worldPosition.getY() && scanner.getItem() instanceof ScannerItem item) {
            int range = item.startLayerScan(scanner);
            if (range > 0) {
                for (int scanX = x - range; scanX <= x + range; scanX++) {
                    for (int scanZ = z - range; scanZ <= z + range; scanZ++) {
                        BlockPos pos = new BlockPos(scanX, y, scanZ);
                        BlockState state = level.getBlockState(pos);
                        if (OreValues.valueOf(state) > 0 && canMine(pos)
                                || isAnyPumpConnected() && state.is(Blocks.LAVA) && state.getFluidState().isSource()) {
                            setTarget(scanX, y, scanZ);
                            return;
                        }
                    }
                }
            }
        }
        setTarget(x, y - 1, z);
    }

    private void setTarget(int x, int y, int z) {
        targetX = x;
        targetY = y;
        targetZ = z;
    }

    /** Height of the lowest pipe or tip below the miner (the miner's own height without pipe). */
    private int getPipeTip() {
        int y = worldPosition.getY();
        while (isPipe(level.getBlockState(new BlockPos(worldPosition.getX(), y - 1, worldPosition.getZ())))) {
            y--;
        }
        return y;
    }

    private static boolean isPipe(BlockState state) {
        return state.is(Ic2Blocks.MINING_PIPE.get()) || state.is(Ic2Blocks.MINING_TIP.get());
    }

    private static boolean isWaterOrLava(BlockState state) {
        return state.is(Blocks.WATER) || state.is(Blocks.LAVA);
    }

    private static boolean canPass(BlockState state) {
        return state.isAir() || isWaterOrLava(state) || isPipe(state)
                || state.getBlock() == Ic2Blocks.machine(Ic2Machine.MINER).get();
    }

    public boolean isOperating() {
        return energy > 100 && canOperate();
    }

    private boolean canOperate() {
        ItemStack drill = inventory.get(SLOT_DRILL);
        return inventory.get(SLOT_PIPE).is(Ic2Items.MINING_PIPE.get())
                && (drill.is(Ic2Items.MINING_DRILL.get()) || drill.is(Ic2Items.DIAMOND_DRILL.get()))
                && !isStuck();
    }

    public boolean isStuck() {
        return miningTicker < 0;
    }

    private boolean canMine(BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return true;
        }
        if (isPipe(state) || state.is(Blocks.CHEST)) {
            return false;
        }
        if (isWaterOrLava(state) && isPumpConnected()) {
            return true;
        }
        if (state.getDestroySpeed(level, pos) < 0.0F) {
            return false;
        }
        if (!state.liquid() && !state.requiresCorrectToolForDrops() || state.is(Blocks.COBWEB)) {
            return true;
        }
        return canDrillHarvest(inventory.get(SLOT_DRILL), state);
    }

    /** Forge harvest levels: the mining drill is an iron pickaxe (2), the diamond drill a diamond one (3). */
    private static boolean canDrillHarvest(ItemStack drill, BlockState state) {
        if (!(drill.getItem() instanceof DrillItem item) || state.liquid() || !item.isCorrectToolForDrops(drill, state)) {
            return false;
        }
        return drill.is(Ic2Items.DIAMOND_DRILL.get()) || !state.is(BlockTags.NEEDS_DIAMOND_TOOL);
    }

    private boolean canWithdraw() {
        return isPipe(level.getBlockState(worldPosition.below()));
    }

    private boolean isPumpConnected() {
        return findPump(true) != null;
    }

    private boolean isAnyPumpConnected() {
        return findPump(false) != null;
    }

    private PumpBlockEntity findPump(boolean mustHarvest) {
        for (Direction direction : PUMP_SIDES) {
            if (level.getBlockEntity(worldPosition.relative(direction)) instanceof PumpBlockEntity pump
                    && (!mustHarvest || pump.canHarvest())) {
                return pump;
            }
        }
        return null;
    }

    private void usePump(boolean water) {
        PumpBlockEntity pump = findPump(true);
        if (pump != null) {
            pump.pumpThis(water);
        }
    }

    // --- inventory ---

    /**
     * IC2 {@code getStartInventorySide}: left and IC2's "front" (the side opposite the facing) → drill, right and
     * the facing side → scanner, bottom → battery, top → pipe.
     */
    @Override
    protected int getSlotForSide(Direction side) {
        Direction facing = getFacing();
        if (side == facing.getCounterClockWise() || side == facing.getOpposite()) {
            return SLOT_DRILL;
        }
        if (side == facing.getClockWise() || side == facing) {
            return SLOT_SCANNER;
        }
        return side == Direction.DOWN ? SLOT_BATTERY : SLOT_PIPE;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new MinerMenu(id, playerInventory, this);
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
        targetX = tag.getInt("targetX");
        targetY = tag.contains("targetY") ? tag.getInt("targetY") : NO_TARGET;
        targetZ = tag.getInt("targetZ");
        miningTicker = tag.getShort("miningTicker");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", energy);
        if (targetY != NO_TARGET) {
            tag.putInt("targetX", targetX);
            tag.putInt("targetY", targetY);
            tag.putInt("targetZ", targetZ);
        }
        tag.putShort("miningTicker", miningTicker);
    }

    /** Test hook. */
    public void setEnergyForTest(int energy) {
        this.energy = energy;
    }
}
