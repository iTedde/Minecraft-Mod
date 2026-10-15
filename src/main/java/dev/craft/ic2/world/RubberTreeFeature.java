package dev.craft.ic2.world;

import dev.craft.Craft;
import dev.craft.ic2.block.RubberTree;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.common.Tags;

/**
 * Rubber tree clusters, 1:1 with IC2 {@code IC2.generate} + {@code WorldGenRubTree.generate}. Placed once per chunk
 * (origin = chunk corner). IC2 matched biome names; here the biome tags taiga, forest, jungle and swamp are used.
 */
public class RubberTreeFeature extends Feature<NoneFeatureConfiguration> {
    /** Biomes the feature is added to (union of the four tags below). */
    public static final TagKey<Biome> HAS_RUBBER_TREES =
            TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Craft.MODID, "has_rubber_trees"));
    /** Trees must stay inside the 3×3 chunks a feature may write to; 2 blocks margin for the leaves. */
    private static final int MARGIN = 14;

    public RubberTreeFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        int chunkX = context.origin().getX() & ~15;
        int chunkZ = context.origin().getZ() & ~15;
        Holder<Biome> biome = level.getBiome(new BlockPos(chunkX + 16,
                level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, chunkX + 16, chunkZ + 16), chunkZ + 16));
        int rubberTrees = 0;
        if (biome.is(BiomeTags.IS_TAIGA)) {
            rubberTrees += random.nextInt(3);
        }
        if (biome.is(BiomeTags.IS_FOREST) || biome.is(BiomeTags.IS_JUNGLE)) {
            rubberTrees += random.nextInt(5) + 1;
        }
        if (biome.is(Tags.Biomes.IS_SWAMP)) {
            rubberTrees += random.nextInt(10) + 5;
        }
        if (rubberTrees <= 0 || random.nextInt(100) + 1 > rubberTrees * 2) {
            return false;
        }
        generate(level, random, chunkX, chunkZ, chunkX + random.nextInt(16), rubberTrees, chunkZ + random.nextInt(16));
        return true;
    }

    /** IC2 {@code WorldGenRubTree.generate}: a random walk of trees around the start position. */
    private static void generate(WorldGenLevel level, RandomSource random, int chunkX, int chunkZ, int x, int count,
                                 int z) {
        while (count > 0) {
            if (x >= chunkX - MARGIN && x < chunkX + 16 + MARGIN && z >= chunkZ - MARGIN && z < chunkZ + 16 + MARGIN) {
                int y = level.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
                if (!RubberTree.grow(level, new BlockPos(x, y, z), random)) {
                    count -= 3;
                }
            }
            x += random.nextInt(15) - 7;
            z += random.nextInt(15) - 7;
            count--;
        }
    }
}
