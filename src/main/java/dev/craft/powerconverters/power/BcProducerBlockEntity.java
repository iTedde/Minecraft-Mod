package dev.craft.powerconverters.power;

import dev.craft.buildcraft.power.BcCapabilities;
import dev.craft.buildcraft.power.PowerProvider;
import dev.craft.buildcraft.power.PowerReceptor;
import dev.craft.powerconverters.PcBlockEntityTypes;
import dev.craft.powerconverters.PowerSystem;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * {@code TileEntityBuildCraftProducer}: hands MJ directly to the adjacent receptors every tick. It is a receptor
 * itself with an empty provider, so engines facing it transfer nothing.
 */
public class BcProducerBlockEntity extends EnergyProducerBlockEntity<PowerReceptor> implements PowerReceptor {
    private final PowerProvider powerProvider = new PowerProvider();

    public BcProducerBlockEntity(BlockPos pos, BlockState state) {
        super(PcBlockEntityTypes.BC_PRODUCER.get(), pos, state, PowerSystem.BUILDCRAFT, 0);
        powerProvider.configure(0, 0, 0, 0);
    }

    @Override
    protected @Nullable PowerReceptor findAdjacent(Direction side, BlockPos neighbor) {
        return level.getCapability(BcCapabilities.POWER_RECEPTOR, neighbor, side.getOpposite());
    }

    /**
     * {@code mj} is computed once and not reduced, so several receptors together can get more MJ than there were
     * PCU. The receptor is told the direction from the producer, not the opposite (original bug).
     */
    @Override
    public int produceEnergy(int energy) {
        int perOutput = powerSystem.getEnergyPerOutput();
        int mj = energy / perOutput;
        for (Map.Entry<Direction, PowerReceptor> output : getTiles().entrySet()) {
            PowerProvider provider = output.getValue().getPowerProvider();
            if (provider != null && provider.getMinEnergyReceived() <= mj) {
                int energyUsed = Math.min(Math.min(provider.getMaxEnergyReceived(), mj),
                        provider.getMaxEnergyStored() - (int) Math.floor(provider.getEnergyStored()));
                provider.receiveEnergy(energyUsed, output.getKey());
                energy -= energyUsed * perOutput;
                if (energy <= 0) {
                    return 0;
                }
            }
        }
        return energy;
    }

    @Override
    public PowerProvider getPowerProvider() {
        return powerProvider;
    }

    @Override
    public void doWork() {
    }
}
