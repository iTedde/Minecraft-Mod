package dev.craft.core.energy;

public interface EnergySource extends EnergyEmitter {
    /** Largest packet this source emits; also the loss limit for path discovery. */
    int getMaxEnergyOutput();
}
