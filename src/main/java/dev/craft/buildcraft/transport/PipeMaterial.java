package dev.craft.buildcraft.transport;

/** The {@code PipeLogic*} of a pipe: connection rules between pipes and the per-material item speed. */
public enum PipeMaterial {
    WOOD, EMERALD, COBBLESTONE, STONE, SANDSTONE, IRON, GOLD, DIAMOND, OBSIDIAN, VOID;

    private static final float NORMAL_SPEED = 0.01F;

    /** {@code PipeLogic*.isPipeConnected} towards another pipe. Emerald counts as wood. */
    public boolean acceptsPipe(PipeMaterial other) {
        return switch (this) {
            case WOOD, EMERALD -> !other.isWood();
            case COBBLESTONE -> other != STONE;
            case STONE -> other != COBBLESTONE;
            case OBSIDIAN -> other != OBSIDIAN;
            default -> true;
        };
    }

    /** {@code PipeLogicSandstone}: only pipes, no inventories, tanks or machines. */
    public boolean connectsToNonPipes() {
        return this != SANDSTONE;
    }

    public boolean isWood() {
        return this == WOOD || this == EMERALD;
    }

    /** {@code readjustSpeed}: stone slows down less, gold speeds up, everything else uses the default. */
    public float readjustSpeed(float speed) {
        return switch (this) {
            case STONE -> Math.max(speed > NORMAL_SPEED ? speed - NORMAL_SPEED / 2.0F : speed, NORMAL_SPEED);
            case GOLD -> goldSpeed(speed);
            default -> Math.max(speed > NORMAL_SPEED ? speed - NORMAL_SPEED : speed, NORMAL_SPEED);
        };
    }

    /** {@code PipeItemsGold}: used by {@code readjustSpeed} and again by {@code entityEntered}. */
    public static float goldSpeed(float speed) {
        return Math.min(Math.max(NORMAL_SPEED, speed) * 2.0F, 0.3F);
    }
}
