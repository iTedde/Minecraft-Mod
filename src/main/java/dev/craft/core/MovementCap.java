package dev.craft.core;

import net.minecraft.world.phys.Vec3;

/** Spec 37 §1: no Craft item moves the player horizontally faster than vanilla ground sprinting. */
public final class MovementCap {
    /** Top speed of vanilla ground sprinting in blocks per tick (≈ 5.6 m/s). */
    public static final double MAX_HORIZONTAL = 0.28;
    /**
     * Flying acceleration whose top speed with the air friction 0.91 is {@link #MAX_HORIZONTAL}
     * ({@code v = 0.91 · a / 0.09}); vanilla flies with 0.05, twice that sprinting.
     */
    public static final float FLY_ACCELERATION = (float) (MAX_HORIZONTAL * 0.09 / 0.91);

    private MovementCap() {
    }

    /** Scales the horizontal part of {@code motion} down to {@link #MAX_HORIZONTAL}; vertical motion is kept. */
    public static Vec3 clampHorizontal(Vec3 motion) {
        double horizontal = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        if (horizontal <= MAX_HORIZONTAL) {
            return motion;
        }
        double scale = MAX_HORIZONTAL / horizontal;
        return new Vec3(motion.x * scale, motion.y, motion.z * scale);
    }
}
