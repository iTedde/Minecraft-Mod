package dev.craft.powerconverters.power;

import dev.craft.powerconverters.PowerSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** {@code TileEntityEnergyConsumer}: feeds converted energy into the adjacent bridges. */
public abstract class EnergyConsumerBlockEntity<A> extends BridgeComponentBlockEntity<A> {
    protected EnergyConsumerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                        PowerSystem powerSystem, int voltageIndex) {
        super(type, pos, state, powerSystem, voltageIndex);
    }

    /** Stores {@code energy} PCU in the adjacent bridges one after another; returns what did not fit. */
    protected int storeEnergy(int energy) {
        for (EnergyBridgeBlockEntity bridge : getBridges().values()) {
            if (bridge.isRemoved()) {
                continue;
            }
            energy = bridge.storeEnergy(energy);
            if (energy <= 0) {
                return 0;
            }
        }
        return energy;
    }

    protected int getTotalEnergyDemand() {
        int demand = 0;
        for (EnergyBridgeBlockEntity bridge : getBridges().values()) {
            if (!bridge.isRemoved()) {
                demand += bridge.getEnergyStoredMax() - bridge.getEnergyStored();
            }
        }
        return demand;
    }

    /** Input per tick shown in the bridge GUI (in the system's unit). */
    public abstract int getInputRate();
}
