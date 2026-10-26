package dev.craft.powerconverters.power;

import dev.craft.core.energy.OutputMeter;
import dev.craft.core.energy.OutputReadout;
import dev.craft.powerconverters.PcBlockEntityTypes;
import dev.craft.powerconverters.PowerSystem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.Nullable;

/**
 * FE Producer (new content, spec 33): pushes the bridge energy as FE into the adjacent FE receivers every tick and
 * lets FE cables of other mods pull it through {@link #getEnergyStorage()}. Fractional FE stay in the bridge, and
 * several receivers share the energy (unlike the BC producer).
 */
public class FeProducerBlockEntity extends EnergyProducerBlockEntity<IEnergyStorage> implements OutputReadout {
    private final OutputMeter outputMeter = new OutputMeter();
    private final IEnergyStorage energyStorage = new PullStorage();

    public FeProducerBlockEntity(BlockPos pos, BlockState state) {
        super(PcBlockEntityTypes.FE_PRODUCER.get(), pos, state, PowerSystem.FE, 0);
    }

    @Override
    protected @Nullable IEnergyStorage findAdjacent(Direction side, BlockPos neighbor) {
        return receiverAt(side);
    }

    /** Asked again every tick, as other mods may swap their capability object without a neighbour update. */
    private @Nullable IEnergyStorage receiverAt(Direction side) {
        IEnergyStorage storage = level.getCapability(Capabilities.EnergyStorage.BLOCK, worldPosition.relative(side),
                side.getOpposite());
        return storage != null && storage.canReceive() ? storage : null;
    }

    @Override
    public int produceEnergy(int energy) {
        int perOutput = powerSystem.getEnergyPerOutput();
        int fe = energy / perOutput;
        if (fe <= 0) {
            return energy;
        }
        List<IEnergyStorage> receivers = new ArrayList<>();
        for (Direction side : getTiles().keySet()) {
            IEnergyStorage receiver = receiverAt(side);
            if (receiver != null) {
                receivers.add(receiver);
            }
        }
        int produced = push(fe, receivers);
        outputMeter.record(level, produced);
        return energy - produced * perOutput;
    }

    /** Offers {@code fe} to the receivers in order, each getting what the ones before left; returns the FE used. */
    public static int push(int fe, List<IEnergyStorage> receivers) {
        int produced = 0;
        for (IEnergyStorage receiver : receivers) {
            produced += Math.max(0, Math.min(fe - produced, receiver.receiveEnergy(fe - produced, false)));
            if (produced >= fe) {
                break;
            }
        }
        return produced;
    }

    public IEnergyStorage getEnergyStorage() {
        return energyStorage;
    }

    @Override
    public OutputReading getOutputReading() {
        return outputMeter.read(level, OutputReading.Unit.FE);
    }

    /** Extract-only view of the adjacent bridges (spec 33 §2.2). */
    private final class PullStorage implements IEnergyStorage {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            return 0;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            int perOutput = powerSystem.getEnergyPerOutput();
            int extracted = 0;
            for (EnergyBridgeBlockEntity bridge : getBridges().values()) {
                if (extracted >= maxExtract) {
                    break;
                }
                if (bridge.isRemoved()) {
                    continue;
                }
                int fe = Math.min(maxExtract - extracted, bridge.getEnergyStored() / perOutput);
                if (fe > 0 && !simulate) {
                    bridge.setEnergyStored(bridge.getEnergyStored() - fe * perOutput);
                }
                extracted += fe;
            }
            if (!simulate && extracted > 0) {
                outputMeter.record(level, extracted);
            }
            return extracted;
        }

        @Override
        public int getEnergyStored() {
            int stored = 0;
            for (EnergyBridgeBlockEntity bridge : getBridges().values()) {
                if (!bridge.isRemoved()) {
                    stored += bridge.getEnergyStored() / powerSystem.getEnergyPerOutput();
                }
            }
            return stored;
        }

        @Override
        public int getMaxEnergyStored() {
            int max = 0;
            for (EnergyBridgeBlockEntity bridge : getBridges().values()) {
                if (!bridge.isRemoved()) {
                    max += bridge.getEnergyStoredMax() / powerSystem.getEnergyPerOutput();
                }
            }
            return max;
        }

        @Override
        public boolean canExtract() {
            return true;
        }

        @Override
        public boolean canReceive() {
            return false;
        }
    }
}
