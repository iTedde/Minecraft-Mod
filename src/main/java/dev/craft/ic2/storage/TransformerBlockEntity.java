package dev.craft.ic2.storage;

import dev.craft.core.energy.EnergyNets;
import dev.craft.core.energy.EnergySink;
import dev.craft.core.energy.EnergySource;
import dev.craft.core.energy.EnergyTile;
import dev.craft.core.explosion.Ic2Explosion;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.machine.MachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * LV, MV and HV transformer, 1:1 with IC2 {@code TileEntityTransformer}. Without redstone it steps down: input on the
 * front, up to four low-voltage packets per tick on the other sides. With redstone it steps up: input on the other
 * sides, one high-voltage packet per tick on the front.
 */
public class TransformerBlockEntity extends MachineBlockEntity implements EnergySink, EnergySource {
    private final int lowOutput;
    private final int highOutput;
    private final int maxStorage;
    private int energy;
    /** Not saved: like IC2, the first tick after loading re-reads the redstone state. */
    private boolean redstone;

    public TransformerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Spec spec) {
        super(type, pos, state, 0);
        this.lowOutput = spec.lowOutput();
        this.highOutput = spec.highOutput();
        this.maxStorage = spec.maxStorage();
    }

    public int getEnergy() {
        return energy;
    }

    public boolean isStepUp() {
        return redstone;
    }

    @Override
    public void serverTick() {
        updateRedstone();
        if (redstone) {
            if (energy >= highOutput) {
                int rest = EnergyNets.emit(level, this, highOutput);
                energy -= highOutput - rest;
            }
        } else {
            for (int i = 0; i < 4 && energy >= lowOutput; i++) {
                int rest = EnergyNets.emit(level, this, lowOutput);
                energy -= lowOutput - rest;
            }
        }
    }

    /** Switching modes swaps input and output sides, so the transformer re-registers in the energy net. */
    private void updateRedstone() {
        boolean powered = level.hasNeighborSignal(worldPosition);
        if (powered != redstone) {
            EnergyNets.remove(level, this);
            redstone = powered;
            EnergyNets.add(level, this);
            setActive(redstone);
        }
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
        return redstone ? direction != getFacing() : direction == getFacing();
    }

    @Override
    public boolean emitsEnergyTo(EnergyTile receiver, Direction direction) {
        return redstone ? direction == getFacing() : direction != getFacing();
    }

    @Override
    public int getMaxEnergyOutput() {
        return redstone ? highOutput : lowOutput;
    }

    @Override
    public int demandsEnergy() {
        return isRemoved() ? 0 : maxStorage - energy;
    }

    @Override
    public int injectEnergy(Direction direction, int amount) {
        if (isRemoved()) {
            return amount;
        }
        if (amount > getMaxSafeInput()) {
            if (level instanceof ServerLevel serverLevel) {
                Ic2Explosion.explodeMachineAt(serverLevel, worldPosition);
            }
            return 0;
        }
        int need = amount;
        if (energy + amount >= maxStorage + highOutput) {
            need = maxStorage + highOutput - energy - 1;
        }
        energy += need;
        return amount - need;
    }

    /** Step-down input is limited to the high voltage, except for the HV transformer (2048 → unlimited). */
    @Override
    public int getMaxSafeInput() {
        if (redstone) {
            return lowOutput;
        }
        return highOutput != 2048 ? highOutput : Integer.MAX_VALUE;
    }

    // --- wrench ---

    @Override
    public boolean wrenchCanSetFacing(Player player, Direction side) {
        return getFacing() != side;
    }

    @Override
    public void setFacing(Direction facing) {
        EnergyNets.remove(level, this);
        super.setFacing(facing);
        EnergyNets.add(level, this);
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return null;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        energy = tag.getInt("energy");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("energy", energy);
    }

    /** Values of {@code TileEntityTransformer{LV,MV,HV}}. */
    public record Spec(int lowOutput, int highOutput, int maxStorage) {
        public static final Spec LV = new Spec(32, 128, 256);
        public static final Spec MV = new Spec(128, 512, 1024);
        public static final Spec HV = new Spec(512, 2048, 4096);

        public static Spec of(Ic2Machine machine) {
            return switch (machine) {
                case LV_TRANSFORMER -> LV;
                case MV_TRANSFORMER -> MV;
                case HV_TRANSFORMER -> HV;
                default -> throw new IllegalArgumentException(machine + " is no transformer");
            };
        }
    }
}
