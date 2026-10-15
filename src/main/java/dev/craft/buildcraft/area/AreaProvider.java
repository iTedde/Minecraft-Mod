package dev.craft.buildcraft.area;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

/** BuildCraft {@code IAreaProvider}: a box marked in the world, e.g. by landmarks. */
public interface AreaProvider {
    int xMin();

    int yMin();

    int zMin();

    int xMax();

    int yMax();

    int zMax();

    /** Called when a machine takes the area over (landmarks drop as items). */
    void removeFromWorld();

    /** BuildCraft {@code Utils.getNearbyAreaProvider}: checks +X, −X, +Z, −Z, +Y, −Y in this order. */
    @Nullable
    static AreaProvider findNearby(Level level, BlockPos pos) {
        Direction[] order = {Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH, Direction.UP,
                Direction.DOWN};
        for (Direction side : order) {
            if (level.getBlockEntity(pos.relative(side)) instanceof AreaProvider provider) {
                return provider;
            }
        }
        return null;
    }

    /** BuildCraft {@code DefaultAreaProvider}: a fixed box without anything to remove. */
    static AreaProvider of(int xMin, int yMin, int zMin, int xMax, int yMax, int zMax) {
        return new AreaProvider() {
            @Override
            public int xMin() {
                return xMin;
            }

            @Override
            public int yMin() {
                return yMin;
            }

            @Override
            public int zMin() {
                return zMin;
            }

            @Override
            public int xMax() {
                return xMax;
            }

            @Override
            public int yMax() {
                return yMax;
            }

            @Override
            public int zMax() {
                return zMax;
            }

            @Override
            public void removeFromWorld() {
            }
        };
    }
}
