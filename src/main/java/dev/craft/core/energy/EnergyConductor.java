package dev.craft.core.energy;

public interface EnergyConductor extends EnergyAcceptor, EnergyEmitter {
    /** EU lost per packet and block. */
    double getConductionLoss();

    /** Packets with more injected EU than this shock nearby living entities. */
    int getInsulationEnergyAbsorption();

    int getInsulationBreakdownEnergy();

    /** Packets with at least this many injected EU destroy the conductor. */
    int getConductorBreakdownEnergy();

    void removeInsulation();

    void removeConductor();
}
