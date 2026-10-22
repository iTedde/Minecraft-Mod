package dev.craft.thermalexpansion.transport;

import dev.craft.buildcraft.power.PowerProvider;
import net.minecraft.core.Direction;

/**
 * {@code PowerProviderGrid} ({@code IPowerProviderLoss}): the shared buffer of an energy conduit network, 500 MJ per
 * node; everything received loses 5 % ({@code transport.conduit.energy.loss}). Engines recognise it and push at least
 * {@code 25 · minPower} per stroke into it.
 */
public class GridPowerProvider extends PowerProvider implements LossPowerProvider {
    public static final float CONDUIT_ENERGY_LOSS = 0.95F;
    public static final int MAX_NODE_STORAGE = 500;

    public GridPowerProvider() {
        configure(2, MAX_NODE_STORAGE, 0, 0);
        configurePowerPerdition(0, 72000);
    }

    /** {@code setMaxEnergyStored(nodeCount)}. */
    public void setNodeCount(int nodeCount) {
        configure(2, MAX_NODE_STORAGE, 0, nodeCount * MAX_NODE_STORAGE);
        if (getEnergyStored() > getMaxEnergyStored()) {
            setEnergyStored(getMaxEnergyStored());
        }
    }

    /** {@code addEnergy}: with the conduit loss, capped. */
    @Override
    public void receiveEnergy(float quantity, Direction from) {
        setEnergyStored(Math.min(getMaxEnergyStored(), getEnergyStored() + quantity * CONDUIT_ENERGY_LOSS));
    }

    /** {@code addEnergyWithoutLoss}: energy kept in the conduits themselves (saved shares, merged networks). */
    public void addEnergyWithoutLoss(float quantity) {
        setEnergyStored(getEnergyStored() + quantity);
    }
}
