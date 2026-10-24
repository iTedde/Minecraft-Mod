package dev.craft.lightshield;

import java.util.ArrayDeque;

import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

/**
 * Flashlight cone of the light shield (spec 30 §3a): pure light values plus the table of lit blocks for a given eye
 * position and view direction. Only blocks in sight of the eye are lit; the table is filled once per change and read
 * by the dynamic light behaviour.
 */
public final class LightCone {
    public static final double RANGE = 16.0;
    /** Up to this distance the cone has full brightness. */
    public static final double FULL_RANGE = 4.0;
    public static final double HALF_ANGLE = 15.0;
    /** Inside this half angle the cone has full brightness; towards {@link #HALF_ANGLE} it fades to 0. */
    public static final double FULL_HALF_ANGLE = 10.0;
    public static final float CONE_LIGHT = 14.0F;
    public static final float GLOW_LIGHT = 7.0F;
    /** Largest gap between two rays at the base of the cone. */
    private static final double RAY_SPACING = 0.5;
    private static final double STEP = 0.25;

    private LightCone() {
    }

    /** Cone brightness at {@code distance} blocks from the eye and {@code angle} degrees off the axis. */
    public static float coneLight(double distance, double angle) {
        if (distance >= RANGE || angle >= HALF_ANGLE) {
            return 0.0F;
        }
        double range = distance <= FULL_RANGE ? 1.0 : (RANGE - distance) / (RANGE - FULL_RANGE);
        double edge = angle <= FULL_HALF_ANGLE ? 1.0 : (HALF_ANGLE - angle) / (HALF_ANGLE - FULL_HALF_ANGLE);
        return (float) (CONE_LIGHT * range * edge);
    }

    /** Glow around the holder: 7 at the eye, −1 per block. */
    public static float glowLight(double distance) {
        return (float) Math.max(0.0, GLOW_LIGHT - distance);
    }

    /** Brightness of the block at {@code pos} if it is in sight, from cone and glow. */
    public static float lightAt(Vec3 eye, Vec3 axis, BlockPos pos) {
        Vec3 toBlock = Vec3.atCenterOf(pos).subtract(eye);
        double distance = toBlock.length();
        double angle = distance < 1.0E-6 ? 0.0
                : Math.toDegrees(Math.acos(Math.min(1.0, toBlock.dot(axis) / distance)));
        return Math.max(coneLight(distance, angle), glowLight(distance));
    }

    /**
     * Lit blocks (as {@link BlockPos#asLong}) with their brightness. The cone is traced with a fan of rays that stop at
     * the first colliding block (which is lit itself); the glow spreads from the eye through non-colliding blocks.
     * Fluids let light through.
     */
    public static Long2FloatMap compute(BlockGetter level, Vec3 eye, Vec3 axis) {
        Long2FloatOpenHashMap lights = new Long2FloatOpenHashMap();
        Vec3 dir = axis.normalize();
        traceCone(level, eye, dir, lights);
        spreadGlow(level, eye, lights);
        return lights;
    }

    private static void traceCone(BlockGetter level, Vec3 eye, Vec3 dir, Long2FloatOpenHashMap lights) {
        Vec3 side = Math.abs(dir.y) > 0.99 ? new Vec3(1.0, 0.0, 0.0) : dir.cross(new Vec3(0.0, 1.0, 0.0)).normalize();
        Vec3 up = side.cross(dir).normalize();
        double radius = RANGE * Math.tan(Math.toRadians(HALF_ANGLE));
        int steps = (int) Math.ceil(radius / RAY_SPACING);
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int u = -steps; u <= steps; u++) {
            for (int v = -steps; v <= steps; v++) {
                double a = u * RAY_SPACING;
                double b = v * RAY_SPACING;
                if (a * a + b * b > radius * radius) {
                    continue;
                }
                Vec3 end = eye.add(dir.scale(RANGE)).add(side.scale(a)).add(up.scale(b));
                BlockHitResult hit = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE, CollisionContext.empty()));
                Vec3 ray = end.subtract(eye);
                double length = hit.getType() == HitResult.Type.MISS ? ray.length()
                        : hit.getLocation().distanceTo(eye);
                Vec3 step = ray.normalize();
                for (double t = 0.0; t < length; t += STEP) {
                    cursor.set(eye.x + step.x * t, eye.y + step.y * t, eye.z + step.z * t);
                    light(lights, eye, dir, cursor);
                }
                if (hit.getType() != HitResult.Type.MISS) {
                    light(lights, eye, dir, hit.getBlockPos());
                }
            }
        }
    }

    /** Glow only, by flood fill from the eye block; colliding blocks are lit but stop the spread. */
    private static void spreadGlow(BlockGetter level, Vec3 eye, Long2FloatOpenHashMap lights) {
        BlockPos start = BlockPos.containing(eye);
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        LongOpenHashSet seen = new LongOpenHashSet();
        queue.add(start);
        seen.add(start.asLong());
        while (!queue.isEmpty()) {
            BlockPos pos = queue.poll();
            put(lights, pos, glowLight(Vec3.atCenterOf(pos).distanceTo(eye)));
            if (!pos.equals(start) && !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
                continue;
            }
            for (Direction direction : Direction.values()) {
                BlockPos next = pos.relative(direction);
                if (glowLight(Vec3.atCenterOf(next).distanceTo(eye)) > 0.0F && seen.add(next.asLong())) {
                    queue.add(next);
                }
            }
        }
    }

    private static void light(Long2FloatOpenHashMap lights, Vec3 eye, Vec3 dir, BlockPos pos) {
        put(lights, pos, lightAt(eye, dir, pos));
    }

    private static void put(Long2FloatOpenHashMap lights, BlockPos pos, float light) {
        if (light > 0.0F) {
            long key = pos.asLong();
            if (light > lights.get(key)) {
                lights.put(key, light);
            }
        }
    }
}
