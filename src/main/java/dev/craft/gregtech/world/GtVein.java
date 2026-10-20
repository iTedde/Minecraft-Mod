package dev.craft.gregtech.world;

import java.util.function.BiFunction;
import java.util.function.Predicate;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * {@code GT_MinableOreGenerator}: the 1.4.7 {@code WorldGenMinable} vein (a chain of {@code size + 1} ellipsoids along
 * a random horizontal line of length {@code size / 4} centred at {@code (x + 8, y, z + 8)}) with GT's replacement rule.
 * Unlike the vanilla 1.21 ore feature it needs no ground below, so asteroid veins work in the void. Blocks outside the
 * writable area of the decorated chunk are skipped.
 */
public final class GtVein {
    private GtVein() {
    }

    public static void place(WorldGenLevel level, RandomSource random, int x, int y, int z, int size, BlockState ore,
                             Predicate<BlockState> replaceable) {
        place(level, random, x, y, z, size, (rand, existing) -> replaceable.test(existing) ? ore : null);
    }

    /**
     * The same vein with a per-block choice: {@code ore} returns the block for a replaceable {@code existing} state
     * (CoFH {@code WorldGenMulti} picks a weighted ore per block), or {@code null} to keep it.
     */
    public static void place(WorldGenLevel level, RandomSource random, int x, int y, int z, int size,
                             BiFunction<RandomSource, BlockState, BlockState> ore) {
        float angle = random.nextFloat() * Mth.PI;
        double startX = x + 8 + Mth.sin(angle) * size / 8.0F;
        double endX = x + 8 - Mth.sin(angle) * size / 8.0F;
        double startZ = z + 8 + Mth.cos(angle) * size / 8.0F;
        double endZ = z + 8 - Mth.cos(angle) * size / 8.0F;
        double startY = y + random.nextInt(3) - 2;
        double endY = y + random.nextInt(3) - 2;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int step = 0; step <= size; step++) {
            double centerX = startX + (endX - startX) * step / size;
            double centerY = startY + (endY - startY) * step / size;
            double centerZ = startZ + (endZ - startZ) * step / size;
            double scale = random.nextDouble() * size / 16.0;
            double diameter = (Mth.sin(step * Mth.PI / size) + 1.0F) * scale + 1.0;
            double radius = diameter / 2.0;
            int minX = Mth.floor(centerX - radius);
            int minY = Mth.floor(centerY - radius);
            int minZ = Mth.floor(centerZ - radius);
            int maxX = Mth.floor(centerX + radius);
            int maxY = Mth.floor(centerY + radius);
            int maxZ = Mth.floor(centerZ + radius);
            for (int bx = minX; bx <= maxX; bx++) {
                double dx = (bx + 0.5 - centerX) / radius;
                if (dx * dx >= 1.0) {
                    continue;
                }
                for (int by = minY; by <= maxY; by++) {
                    double dy = (by + 0.5 - centerY) / radius;
                    if (dx * dx + dy * dy >= 1.0) {
                        continue;
                    }
                    for (int bz = minZ; bz <= maxZ; bz++) {
                        double dz = (bz + 0.5 - centerZ) / radius;
                        if (dx * dx + dy * dy + dz * dz >= 1.0) {
                            continue;
                        }
                        pos.set(bx, by, bz);
                        if (!level.isOutsideBuildHeight(pos) && level.ensureCanWrite(pos)) {
                            BlockState chosen = ore.apply(random, level.getBlockState(pos));
                            if (chosen != null) {
                                level.setBlock(pos, chosen, 2);
                            }
                        }
                    }
                }
            }
        }
    }
}
