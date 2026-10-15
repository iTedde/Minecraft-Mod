package dev.craft.buildcraft.world;

import dev.craft.buildcraft.BcBlocks;
import dev.craft.buildcraft.fluid.BcFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Oil deposits, 1:1 with BuildCraft 3.4.3 {@code OilPopulate.doPopulate}. Placed once per chunk (origin = chunk
 * corner) after the vanilla decoration, like the {@code PopulateChunkEvent.Post} hook.
 * <p>
 * Deviations (see {@code docs/abweichungen.md}): the scans that started at y = 128 start at the terrain surface and
 * the oil column may rise above 128; blocks outside the 3×3 chunks a feature may access are skipped.
 */
public final class OilFeature extends Feature<NoneFeatureConfiguration> {
    public OilFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        new Populator(context.level(), context.random(), context.origin().getX() & ~15,
                context.origin().getZ() & ~15).populate();
        return true;
    }

    private static final class Populator {
        private final WorldGenLevel level;
        private final RandomSource rand;
        private final int x;
        private final int z;
        private final BlockState oil = BcBlocks.OIL.get().defaultBlockState();
        private final BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        Populator(WorldGenLevel level, RandomSource rand, int x, int z) {
            this.level = level;
            this.rand = rand;
            this.x = x;
            this.z = z;
        }

        void populate() {
            if (level.getBiome(new BlockPos(x + 16, surface(x + 16, z + 16), z + 16)).is(Biomes.DESERT)
                    && rand.nextFloat() > 0.97) {
                int i = x + rand.nextInt(10) + 2;
                int k = z + rand.nextInt(10) + 2;
                for (int j = surface(i, k); j > 65; j--) {
                    BlockState state = get(i, j, k);
                    if (!state.isAir()) {
                        if (state.is(Blocks.SAND)) {
                            generateSurfaceDeposit(i, j, k, 3);
                        }
                        break;
                    }
                }
            }

            boolean mediumDeposit = rand.nextDouble() <= 0.0015;
            boolean largeDeposit = rand.nextDouble() <= 5.0E-5;
            if (!mediumDeposit && !largeDeposit) {
                return;
            }
            int cy = 20 + rand.nextInt(10);
            int r = largeDeposit ? 8 + rand.nextInt(9) : 4 + rand.nextInt(4);
            int r2 = r * r;
            for (int bx = -r; bx <= r; bx++) {
                for (int by = -r; by <= r; by++) {
                    for (int bz = -r; bz <= r; bz++) {
                        if (bx * bx + by * by + bz * bz <= r2) {
                            setOil(bx + x, by + cy, bz + z);
                        }
                    }
                }
            }

            boolean started = false;
            for (int y = surface(x, z); y >= cy; y--) {
                if (!started && isDepositStart(get(x, y, z))) {
                    started = true;
                    generateSurfaceDeposit(x, y, z, largeDeposit ? 20 + rand.nextInt(20) : 5 + rand.nextInt(5));
                    int ymax = Math.min(largeDeposit ? y + 30 : y + 4, level.getMaxBuildHeight() - 1);
                    for (int h = y + 1; h <= ymax; h++) {
                        setOil(x, h, z);
                    }
                } else if (started) {
                    setOil(x, y, z);
                }
            }
        }

        /** The column starts at the first block that is not air, leaves, log or grass block. */
        private static boolean isDepositStart(BlockState state) {
            return !state.isAir() && !state.is(BlockTags.LEAVES) && !state.is(BlockTags.LOGS)
                    && !state.is(Blocks.GRASS_BLOCK);
        }

        private void generateSurfaceDeposit(int x, int y, int z, int radius) {
            setOilWithProba(1.0F, x, y, z, true);
            for (int w = 1; w <= radius; w++) {
                float proba = (float) (radius - w + 4) / (radius + 4);
                for (int d = -w; d <= w; d++) {
                    setOilWithProba(proba, x + d, y, z + w, false);
                    setOilWithProba(proba, x + d, y, z - w, false);
                    setOilWithProba(proba, x + w, y, z + d, false);
                    setOilWithProba(proba, x - w, y, z + d, false);
                }
            }
            for (int dx = x - radius; dx <= x + radius; dx++) {
                for (int dz = z - radius; dz <= z + radius; dz++) {
                    if (!get(dx, y - 1, dz).is(BcBlocks.OIL.get()) && isOil(dx + 1, y - 1, dz)
                            && isOil(dx - 1, y - 1, dz) && isOil(dx, y - 1, dz + 1) && isOil(dx, y - 1, dz - 1)) {
                        setOilWithProba(1.0F, dx, y, dz, true);
                    }
                }
            }
        }

        private void setOilWithProba(float proba, int x, int y, int z, boolean force) {
            if (rand.nextFloat() <= proba && !get(x, y - 2, z).isAir() || force) {
                boolean adjacentOil = false;
                for (int d = -1; d <= 1; d++) {
                    if (isOil(x + d, y - 1, z) || isOil(x - d, y - 1, z) || isOil(x, y - 1, z + d)
                            || isOil(x, y - 1, z - d)) {
                        adjacentOil = true;
                    }
                }
                if (adjacentOil || force) {
                    BlockState state = get(x, y, z);
                    if (!state.is(Blocks.WATER) && !isOil(x, y, z)) {
                        set(x, y, z, Blocks.AIR.defaultBlockState());
                    } else {
                        setOil(x, y, z);
                    }
                    setOil(x, y - 1, z);
                }
            }
        }

        private boolean isOil(int x, int y, int z) {
            return get(x, y, z).is(BcBlocks.OIL.get());
        }

        /** Height of the topmost non-air block (replaces the fixed scan start y = 128). */
        private int surface(int x, int z) {
            return accessible(x, z) ? level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z) - 1 : 0;
        }

        private boolean accessible(int x, int z) {
            return Math.abs((x >> 4) - (this.x >> 4)) <= 1 && Math.abs((z >> 4) - (this.z >> 4)) <= 1;
        }

        private BlockState get(int x, int y, int z) {
            pos.set(x, y, z);
            if (!accessible(x, z) || level.isOutsideBuildHeight(y)) {
                return Blocks.AIR.defaultBlockState();
            }
            return level.getBlockState(pos);
        }

        private void setOil(int x, int y, int z) {
            if (set(x, y, z, oil)) {
                level.scheduleTick(pos.immutable(), BcFluids.OIL.get(), 0);
            }
        }

        private boolean set(int x, int y, int z, BlockState state) {
            pos.set(x, y, z);
            if (!accessible(x, z) || level.isOutsideBuildHeight(y)) {
                return false;
            }
            return level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        }
    }
}
