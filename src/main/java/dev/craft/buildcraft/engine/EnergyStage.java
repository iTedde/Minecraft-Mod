package dev.craft.buildcraft.engine;

/** BuildCraft {@code Engine.EnergyStage}: colour of the trunk and piston speed. */
public enum EnergyStage {
    BLUE, GREEN, YELLOW, RED, EXPLOSION;

    private static final EnergyStage[] VALUES = values();

    public static EnergyStage byId(int id) {
        return VALUES[Math.floorMod(id, VALUES.length)];
    }

    /** Stage for a fill ratio in percent (BuildCraft {@code Engine.computeEnergyStage}). */
    public static EnergyStage forPercent(double percent) {
        if (percent <= 25.0) {
            return BLUE;
        }
        if (percent <= 50.0) {
            return GREEN;
        }
        if (percent <= 75.0) {
            return YELLOW;
        }
        if (percent <= 100.0) {
            return RED;
        }
        return EXPLOSION;
    }
}
