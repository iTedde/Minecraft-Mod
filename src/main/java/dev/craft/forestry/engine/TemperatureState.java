package dev.craft.forestry.engine;

/** Forestry {@code TemperatureState} of an engine: heat level, piston speed and trunk texture. */
public enum TemperatureState {
    COOL(0.03F),
    WARMED_UP(0.04F),
    OPERATING_TEMPERATURE(0.05F),
    RUNNING_HOT(0.06F),
    OVERHEATING(0.07F),
    MELTING(0.08F);

    private final float pistonSpeed;

    TemperatureState(float pistonSpeed) {
        this.pistonSpeed = pistonSpeed;
    }

    public float getPistonSpeed() {
        return pistonSpeed;
    }

    /** {@code Engine.getTemperatureState}. */
    public static TemperatureState of(int heat, int maxHeat) {
        double scaled = (double) heat / maxHeat;
        if (scaled < 0.2) {
            return COOL;
        }
        if (scaled < 0.45) {
            return WARMED_UP;
        }
        if (scaled < 0.65) {
            return OPERATING_TEMPERATURE;
        }
        if (scaled < 0.85) {
            return RUNNING_HOT;
        }
        return scaled < 1.0 ? OVERHEATING : MELTING;
    }
}
