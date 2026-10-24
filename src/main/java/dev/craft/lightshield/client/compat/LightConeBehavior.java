package dev.craft.lightshield.client.compat;

import dev.craft.lightshield.LightCone;
import dev.lambdaurora.lambdynlights.api.behavior.DynamicLightBehavior;
import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import it.unimi.dsi.fastutil.longs.Long2FloatMaps;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/**
 * Flashlight cone of one player's light shield (spec 30 §3a). The client tick moves it along and recomputes the table
 * of lit blocks when needed; LambDynamicLights reads the table, also from chunk builder threads.
 */
final class LightConeBehavior implements DynamicLightBehavior {
    /** Recompute after the eye moved this far (squared) or the view turned this much. */
    private static final double MOVE_SQR = 0.25 * 0.25;
    private static final double TURN_COS = Math.cos(Math.toRadians(2.0));
    /** Recompute at least this often, for block changes in the cone. */
    private static final int REFRESH_TICKS = 10;

    private final Player player;
    private volatile Long2FloatMap lights = Long2FloatMaps.EMPTY_MAP;
    private volatile BoundingBox box;
    private volatile boolean changed;
    private volatile boolean removed;
    private Vec3 eye;
    private Vec3 axis;
    private int age;

    LightConeBehavior(Player player) {
        this.player = player;
        update();
    }

    Player player() {
        return player;
    }

    /** Client tick: recompute when the holder moved or turned, or the last result is old. */
    void tick() {
        age++;
        Vec3 newEye = player.getEyePosition();
        Vec3 newAxis = player.getViewVector(1.0F);
        if (age >= REFRESH_TICKS || newEye.distanceToSqr(eye) >= MOVE_SQR || newAxis.dot(axis) <= TURN_COS) {
            update();
        }
    }

    private void update() {
        eye = player.getEyePosition();
        axis = player.getViewVector(1.0F);
        age = 0;
        Long2FloatMap table = LightCone.compute(player.level(), eye, axis);
        BlockPos center = BlockPos.containing(eye);
        int minX = center.getX(), minY = center.getY(), minZ = center.getZ();
        int maxX = minX, maxY = minY, maxZ = minZ;
        for (long key : table.keySet()) {
            int x = BlockPos.getX(key), y = BlockPos.getY(key), z = BlockPos.getZ(key);
            minX = Math.min(minX, x);
            minY = Math.min(minY, y);
            minZ = Math.min(minZ, z);
            maxX = Math.max(maxX, x);
            maxY = Math.max(maxY, y);
            maxZ = Math.max(maxZ, z);
        }
        lights = table;
        box = new BoundingBox(minX, minY, minZ, maxX, maxY, maxZ);
        changed = true;
    }

    void remove() {
        removed = true;
    }

    @Override
    public double lightAtPos(BlockPos pos, double falloffRatio) {
        return lights.get(pos.asLong());
    }

    @Override
    public BoundingBox getBoundingBox() {
        return box;
    }

    @Override
    public boolean hasChanged() {
        boolean result = changed;
        changed = false;
        return result;
    }

    @Override
    public boolean isRemoved() {
        return removed;
    }
}
