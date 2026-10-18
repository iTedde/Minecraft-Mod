package dev.craft.ic2.machine;

import dev.craft.core.energy.ConsumptionReadout;
import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySink;
import dev.craft.core.energy.EnergyTile;
import dev.craft.core.energy.OutputMeter;
import dev.craft.core.energy.OutputReadout.OutputReading;
import dev.craft.core.explosion.Ic2Explosion;
import dev.craft.ic2.Ic2BlockEntityTypes;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.block.IronFenceBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * IC2 {@code TileEntityMagnetizer}: charges the iron fences in the column above and below to magnetization 15
 * (spec 04, magnetizer). No inventory, no GUI.
 */
public class MagnetizerBlockEntity extends MachineBlockEntity implements EnergySink, ConsumptionReadout {
    public static final int MAX_ENERGY = 100;
    public static final int MAX_INPUT = 32;
    private static final int RANGE = 20;

    private final OutputMeter consumptionMeter = new OutputMeter();
    private int energy;
    private int ticker;

    public MagnetizerBlockEntity(BlockPos pos, BlockState state) {
        super(Ic2BlockEntityTypes.get(Ic2Machine.MAGNETIZER), pos, state, 0);
    }

    public int getEnergy() {
        return energy;
    }

    @Override
    public void serverTick() {
        if (ticker-- > 0) {
            return;
        }
        boolean changed = chargeColumn(-1);
        changed |= chargeColumn(1);
        if (!changed) {
            ticker = 10;
        }
    }

    /**
     * Tops up the unbroken column of iron fences below ({@code -1}) or above ({@code 1}), at most 20 blocks, while
     * energy is left. Original bug kept: if a fence needs more than the stored energy, the energy is raised to the
     * need first, so the charge is free.
     *
     * @return whether a fence changed
     */
    private boolean chargeColumn(int direction) {
        boolean changed = false;
        for (int i = 1; i <= RANGE && energy > 0; i++) {
            BlockPos pos = worldPosition.above(i * direction);
            if (direction < 0 ? pos.getY() <= level.getMinBuildHeight() : pos.getY() >= level.getMaxBuildHeight()) {
                break;
            }
            BlockState state = level.getBlockState(pos);
            if (!state.is(Ic2Blocks.IRON_FENCE.get())) {
                break;
            }
            int need = 15 - state.getValue(IronFenceBlock.MAGNETIZATION);
            if (need > 0) {
                changed = true;
                // IC2 tops the energy up to the need, so only what was stored counts as used
                consumptionMeter.record(level, Math.min(need, energy));
                if (need > energy) {
                    energy = need;
                }
                level.setBlock(pos, state.setValue(IronFenceBlock.MAGNETIZATION, 15), Block.UPDATE_CLIENTS);
                energy -= need;
                setChanged();
            }
        }
        return changed;
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
        int need = amount;
        if (energy + amount >= MAX_ENERGY + MAX_INPUT) {
            need = MAX_ENERGY + MAX_INPUT - energy - 1;
        }
        energy += need;
        setChanged();
        return amount - need;
    }

    @Override
    public int getMaxSafeInput() {
        return MAX_INPUT;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return null;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy = tag.getShort("energy");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putShort("energy", (short) energy);
    }

    /** Test hook. */
    public void setEnergyForTest(int energy) {
        this.energy = energy;
    }

    @Override
    public OutputReading getConsumptionReading() {
        return consumptionMeter.read(level, OutputReading.Unit.EU);
    }
}
