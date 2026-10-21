package dev.craft.thermalexpansion.transport;

import java.util.ArrayList;
import java.util.List;

import dev.craft.buildcraft.power.BcCapabilities;
import dev.craft.buildcraft.power.PowerProvider;
import dev.craft.buildcraft.power.PowerReceptor;
import dev.craft.buildcraft.transport.PipeBlockEntity;
import dev.craft.thermalexpansion.TeBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * {@code TileConduitEnergy}: an MJ receiver whose provider is the network buffer. Nodes are conduits next to MJ
 * receivers (BuildCraft pipes do not count). Output nodes (default) feed their receivers, input nodes ask for up to
 * 500 MJ per tick. Light 7. A node saves its share of the network.
 */
public class EnergyConduitBlockEntity extends ConduitBlockEntity implements PowerReceptor {
    /** {@code Utils.dummyProvider}: what a conduit without network offers. */
    private static final GridPowerProvider NO_GRID = new GridPowerProvider();
    /** Multimeter: average over 20 ticks. */
    private static final int TRACK_TICKS = 20;

    @Nullable
    EnergyGrid grid;
    private float pendingEnergy;
    @Nullable
    private List<Float> energySent;
    private boolean trackEnergy;

    public EnergyConduitBlockEntity(BlockPos pos, BlockState state) {
        super(TeBlockEntityTypes.ENERGY_CONDUIT.get(), pos, state);
    }

    @Nullable
    public EnergyGrid getGrid() {
        return grid;
    }

    float takePendingEnergy() {
        float energy = pendingEnergy;
        pendingEnergy = 0.0F;
        return energy;
    }

    void setPendingEnergy(float energy) {
        pendingEnergy = energy;
    }

    /** The MJ receiver on {@code side}, if it counts for the network (not another conduit, not a pipe). */
    @Nullable
    private PowerReceptor receptor(Direction side) {
        BlockEntity neighbour = neighbour(side);
        if (neighbour == null || neighbour instanceof EnergyConduitBlockEntity || neighbour instanceof PipeBlockEntity) {
            return null;
        }
        PowerReceptor receptor = level.getCapability(BcCapabilities.POWER_RECEPTOR, neighbour.getBlockPos(),
                side.getOpposite());
        return receptor != null && receptor.getPowerProvider() != null ? receptor : null;
    }

    /** {@code checkIfNode}: conduits, receivers, nothing. */
    boolean refreshSides() {
        boolean changed = false;
        for (Direction side : Direction.values()) {
            byte value;
            if (neighbour(side) instanceof EnergyConduitBlockEntity other && !other.isRemoved()) {
                value = CONDUIT;
            } else {
                value = receptor(side) != null ? OTHER : NONE;
            }
            if (sides[side.get3DDataValue()] != value) {
                changed |= value == OTHER || sides[side.get3DDataValue()] == OTHER;
                sides[side.get3DDataValue()] = value;
            }
        }
        return changed;
    }

    @Override
    public void serverTick() {
        if (grid == null) {
            EnergyGrid.build(this);
        }
        if (grid != null && isNode()) {
            grid.tick(this);
        }
    }

    @Override
    public void onNeighborBlockChange() {
        boolean nodesChanged = refreshSides();
        if (nodesChanged && grid != null) {
            grid.dissolve();
        }
        syncSides();
    }

    @Override
    protected void leaveGrid() {
        if (grid != null) {
            grid.dissolve();
        }
    }

    @Override
    public boolean wrenchAction(Direction side) {
        return false;
    }

    @Override
    public void changeMode() {
        if (!isNode() || grid == null) {
            return;
        }
        mode = (byte) ((mode + 1) % 2);
        grid.changeMode(this);
        sync();
    }

    /** {@code transferEnergy(side, available)}: what the receiver asks for, at most {@code available}. */
    float transferEnergy(Direction side, float available) {
        if (sides[side.get3DDataValue()] != OTHER || grid == null) {
            return 0.0F;
        }
        PowerReceptor receptor = receptor(side);
        if (receptor == null) {
            return 0.0F;
        }
        PowerProvider target = receptor.getPowerProvider();
        float powerRequested = receptor.powerRequest();
        if (powerRequested <= 0.0F) {
            return 0.0F;
        }
        float adjusted = Math.min(powerRequested, target.getMaxEnergyStored() - target.getEnergyStored());
        float energy = grid.getProvider().useEnergy(0.0F, Math.min(adjusted, available), true);
        target.receiveEnergy(energy, side.getOpposite());
        return energy;
    }

    // --- multimeter ---

    void trackSent(float energy) {
        if (trackEnergy && energySent != null) {
            energySent.add(energy);
            if (energySent.size() >= TRACK_TICKS) {
                trackEnergy = false;
            }
        }
    }

    /** {@code ItemMultimeter}: start monitoring, or the average (and the number of ticks) when done. */
    @Nullable
    public float[] toggleMonitoring() {
        if (energySent == null) {
            energySent = new ArrayList<>();
            trackEnergy = true;
            return null;
        }
        float total = 0.0F;
        for (float energy : energySent) {
            total += energy;
        }
        float[] result = {energySent.size(), energySent.isEmpty() ? 0.0F : total / energySent.size()};
        trackEnergy = false;
        energySent = null;
        return result;
    }

    /** Output node: the multimeter monitors; otherwise it shows the network saturation. */
    public boolean canTransfer() {
        return mode == MODE_OUTPUT;
    }

    // --- PowerReceptor ---

    @Override
    public PowerProvider getPowerProvider() {
        return grid != null ? grid.getProvider() : NO_GRID;
    }

    @Override
    public void doWork() {
    }

    /** Input nodes ask for the free buffer, at most 500 MJ. */
    @Override
    public int powerRequest() {
        if (grid == null || mode == MODE_OUTPUT) {
            return 0;
        }
        GridPowerProvider provider = grid.getProvider();
        return (int) Math.min(GridPowerProvider.MAX_NODE_STORAGE,
                Math.ceil(provider.getMaxEnergyStored() - provider.getEnergyStored()));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        float energy = tag.getFloat("energy.stored");
        pendingEnergy = Float.isNaN(energy) ? 0.0F : energy;
    }

    /** A node writes its share of the network ({@code lastWriteAverage}). */
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        float energy = grid != null ? (isNode() ? grid.getNodeEnergy() : 0.0F) : pendingEnergy;
        tag.putFloat("energy.stored", energy);
    }
}
