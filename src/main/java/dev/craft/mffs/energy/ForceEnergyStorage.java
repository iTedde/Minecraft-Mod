package dev.craft.mffs.energy;

/** {@code IForceEnergyStorageBlock}: the capacitor as seen through a power link card (spec 48 §6.1). */
public interface ForceEnergyStorage {
    int getStorageAvailablePower();

    int getStorageMaxPower();

    int getPowerStorageId();

    /** Cached fill level in %, refreshed every 10 ticks. */
    int getPercentageStorageCapacity();

    boolean consumePowerFromStorage(int amount, boolean simulate);

    boolean insertPowerToStorage(int amount, boolean simulate);

    int getFreeStorageAmount();

    int getTransmitRange();
}
