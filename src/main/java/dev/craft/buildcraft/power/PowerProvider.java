package dev.craft.buildcraft.power;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;

/**
 * MJ buffer of a receiver, 1:1 with BuildCraft {@code PowerProvider} as created by the pack's
 * {@code PneumaticPowerFramework}: the latency is always 0, so {@link PowerReceptor#doWork()} runs every tick while
 * the stored energy reaches the activation energy. Every 100 ticks 1 MJ is lost.
 */
public final class PowerProvider {
    private int minEnergyReceived;
    private int maxEnergyReceived;
    private int maxEnergyStored;
    private int minActivationEnergy;
    private float energyStored;
    private int powerLoss = 1;
    private int powerLossRegularity = 100;
    private final SafeTimeTracker energyLossTracker = new SafeTimeTracker();
    private final int[] powerSources = new int[6];

    /** BuildCraft {@code configure}; the latency argument of the original is always 0 in the pack. */
    public void configure(int minEnergyReceived, int maxEnergyReceived, int minActivationEnergy,
                          int maxStoredEnergy) {
        this.minEnergyReceived = minEnergyReceived;
        this.maxEnergyReceived = maxEnergyReceived;
        this.maxEnergyStored = maxStoredEnergy;
        this.minActivationEnergy = minActivationEnergy;
    }

    public void configurePowerPerdition(int powerLoss, int powerLossRegularity) {
        this.powerLoss = powerLoss;
        this.powerLossRegularity = powerLossRegularity;
    }

    public int getMinEnergyReceived() {
        return minEnergyReceived;
    }

    public int getMaxEnergyReceived() {
        return maxEnergyReceived;
    }

    public int getMaxEnergyStored() {
        return maxEnergyStored;
    }

    public int getActivationEnergy() {
        return minActivationEnergy;
    }

    public float getEnergyStored() {
        return energyStored;
    }

    public void setEnergyStored(float energyStored) {
        this.energyStored = energyStored;
    }

    /** {@code TileMachine.powerRequest} when active: {@code ceil(min(maxReceived, maxStored − stored))}. */
    public int missingEnergyRequest() {
        return (int) Math.ceil(Math.min(maxEnergyReceived, maxEnergyStored - energyStored));
    }

    /** Runs once per tick of the receiver: work, power loss and the power source markers. */
    public boolean update(PowerReceptor receptor, Level level) {
        boolean result = false;
        if (energyStored >= minActivationEnergy) {
            receptor.doWork();
            result = true;
        }
        if (powerLoss > 0 && energyLossTracker.markTimeIfDelay(level, powerLossRegularity)) {
            energyStored -= powerLoss;
            if (energyStored < 0.0F) {
                energyStored = 0.0F;
            }
        }
        for (int i = 0; i < 6; i++) {
            if (powerSources[i] > 0) {
                powerSources[i]--;
            }
        }
        return result;
    }

    /** Takes {@code min(stored, max)} if at least {@code min} is stored, otherwise nothing. */
    public float useEnergy(float min, float max, boolean doUse) {
        float result = 0.0F;
        if (energyStored >= min) {
            if (energyStored <= max) {
                result = energyStored;
                if (doUse) {
                    energyStored = 0.0F;
                }
            } else {
                result = max;
                if (doUse) {
                    energyStored -= max;
                }
            }
        }
        return result;
    }

    public void receiveEnergy(float quantity, Direction from) {
        powerSources[from.get3DDataValue()] = 2;
        energyStored += quantity;
        if (energyStored > maxEnergyStored) {
            energyStored = maxEnergyStored;
        }
    }

    public boolean isPowerSource(Direction from) {
        return powerSources[from.get3DDataValue()] != 0;
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("minEnergyReceived", minEnergyReceived);
        tag.putInt("maxEnergyReceived", maxEnergyReceived);
        tag.putInt("maxStoreEnergy", maxEnergyStored);
        tag.putInt("minActivationEnergy", minActivationEnergy);
        tag.putFloat("storedEnergy", energyStored);
        return tag;
    }

    public void load(CompoundTag tag) {
        minEnergyReceived = tag.getInt("minEnergyReceived");
        maxEnergyReceived = tag.getInt("maxEnergyReceived");
        maxEnergyStored = tag.getInt("maxStoreEnergy");
        minActivationEnergy = tag.getInt("minActivationEnergy");
        energyStored = tag.getFloat("storedEnergy");
    }
}
