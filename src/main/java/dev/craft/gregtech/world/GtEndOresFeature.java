package dev.craft.gregtech.world;

import java.util.function.Predicate;

import dev.craft.gregtech.GtBlocks;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * {@code GT_Worldgenerator.generateEnd} with {@code EnderAsteroids = true} (spec 07 §6.3): first the asteroids
 * (1 in 100 chunks, of those 1 in 25 with an extra large one), then the ore veins in the end stone of every chunk.
 */
public final class GtEndOresFeature extends Feature<NoneFeatureConfiguration> {
    private static final Predicate<BlockState> END_STONE = state -> state.is(Blocks.END_STONE);
    private static final Predicate<BlockState> END_STONE_OR_VOID = state -> state.is(Blocks.END_STONE) || state.isAir();

    public GtEndOresFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        int x = context.origin().getX();
        int z = context.origin().getZ();
        if (random.nextInt(100) == 0) {
            asteroids(level, random, x, z);
        }
        veins(level, random, x, z, GtOre.TUNGSTATE, 4, 16);
        veins(level, random, x, z, GtOre.SHELDONITE, 1, 4);
        veins(level, random, x, z, GtOre.OLIVINE, 5, 8);
        veins(level, random, x, z, GtOre.SODALITE, 8, 16);
        return true;
    }

    private static void asteroids(WorldGenLevel level, RandomSource random, int x, int z) {
        BlockState endStone = Blocks.END_STONE.defaultBlockState();
        int y = 10 + random.nextInt(237);
        if (random.nextInt(25) == 0) {
            int size = 100 + random.nextInt(101);
            GtVein.place(level, random, x + random.nextInt(16), y, z + random.nextInt(16), size, endStone,
                    END_STONE_OR_VOID);
            asteroidVeins(level, random, x, y, z, GtOre.TUNGSTATE, 5, 16);
            asteroidVeins(level, random, x, y, z, GtOre.SHELDONITE, 2, 4);
            asteroidVeins(level, random, x, y, z, GtOre.OLIVINE, 7, 8);
            asteroidVeins(level, random, x, y, z, GtOre.SODALITE, 12, 16);
        }
        for (int i = 0; i < 5; i++) {
            int size = 30 + random.nextInt(31);
            GtVein.place(level, random, x + random.nextInt(16), y + random.nextInt(51) - 25, z + random.nextInt(16),
                    size, endStone, END_STONE_OR_VOID);
        }
        asteroidVeins(level, random, x, y - 20, z, GtOre.TUNGSTATE, 5, 12);
        asteroidVeins(level, random, x, y - 20, z, GtOre.SHELDONITE, 1, 4);
        asteroidVeins(level, random, x, y - 20, z, GtOre.OLIVINE, 3, 8);
    }

    /** Ore veins in and around an asteroid: {@code (x - 8 + rand(24), baseY + rand(41), z - 8 + rand(24))}, may replace air. */
    private static void asteroidVeins(WorldGenLevel level, RandomSource random, int x, int baseY, int z, GtOre ore,
                                      int count, int size) {
        BlockState state = GtBlocks.ore(ore).get().defaultBlockState();
        for (int i = 0; i < count; i++) {
            int veinX = x - 8 + random.nextInt(24);
            int veinY = baseY + random.nextInt(41);
            int veinZ = z - 8 + random.nextInt(24);
            GtVein.place(level, random, veinX, veinY, veinZ, size, state, END_STONE_OR_VOID);
        }
    }

    /** Base generation: veins in end stone at {@code rand(128)}. */
    private static void veins(WorldGenLevel level, RandomSource random, int x, int z, GtOre ore, int count, int size) {
        BlockState state = GtBlocks.ore(ore).get().defaultBlockState();
        for (int i = 0; i < count; i++) {
            int veinX = x + random.nextInt(16);
            int veinY = random.nextInt(128);
            int veinZ = z + random.nextInt(16);
            GtVein.place(level, random, veinX, veinY, veinZ, size, state, END_STONE);
        }
    }
}
