package dev.craft.thermalexpansion.energy;

/**
 * {@code Engines.Types} with {@code Engines.defaultConfigs} (spec 26 §6.1): the engine runs at full power while its
 * buffer is below 10 %, at minimum power above 90 %, linear in between.
 */
public enum TeEngineType {
    STEAM("steam_engine", 0.2F, 2, 2000, 200),
    MAGMATIC("magmatic_engine", 0.4F, 4, 4000, 400);

    private final String name;
    private final float minPower;
    private final int maxPower;
    private final int maxEnergy;
    private final int maxCycleEnergy;
    private final float minPowerLevel;
    private final float maxPowerLevel;
    private final float energyRamp;

    TeEngineType(String name, float minPower, int maxPower, int maxEnergy, int maxCycleEnergy) {
        this.name = name;
        this.minPower = minPower;
        this.maxPower = maxPower;
        this.maxEnergy = maxEnergy;
        this.maxCycleEnergy = maxCycleEnergy;
        this.minPowerLevel = 9.0F * maxEnergy / 10.0F;
        this.maxPowerLevel = 1.0F * maxEnergy / 10.0F;
        this.energyRamp = minPowerLevel / maxPower;
    }

    public String getName() {
        return name;
    }

    public float getMinPower() {
        return minPower;
    }

    public int getMaxPower() {
        return maxPower;
    }

    public int getMaxEnergy() {
        return maxEnergy;
    }

    /** At most this much MJ per piston stroke. */
    public int getMaxCycleEnergy() {
        return maxCycleEnergy;
    }

    /** Above this the engine runs at minimum power. */
    public float getMinPowerLevel() {
        return minPowerLevel;
    }

    /** Below this the engine runs at full power. */
    public float getMaxPowerLevel() {
        return maxPowerLevel;
    }

    public float getEnergyRamp() {
        return energyRamp;
    }
}
