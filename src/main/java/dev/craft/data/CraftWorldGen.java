package dev.craft.data;

import java.util.List;
import java.util.Set;

import dev.craft.Craft;
import dev.craft.buildcraft.world.BcFeatures;
import dev.craft.gregtech.GtBlocks;
import dev.craft.gregtech.world.ChunkBiomeFilter;
import dev.craft.gregtech.world.GtOre;
import dev.craft.gregtech.world.GtWorldGen;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.world.Ic2Features;
import dev.craft.ic2.world.RubberTreeFeature;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.OrePlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ClampedNormalInt;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.FeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.ReplaceBlockConfiguration;
import net.minecraft.world.level.levelgen.heightproviders.TrapezoidHeight;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * World generation as data: IC2 ({@code IC2.generate}) tin and uranium ore, copper ore with IC2 frequency instead of the vanilla
 * copper veins, rubber trees; BuildCraft oil; GregTech ores ({@code GT_Worldgenerator}, spec 07 §6.3).
 */
public final class CraftWorldGen {
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_TIN_CONFIGURED = configuredKey("ore_tin");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_COPPER_CONFIGURED = configuredKey("ore_copper");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_URANIUM_CONFIGURED = configuredKey("ore_uranium");
    public static final ResourceKey<ConfiguredFeature<?, ?>> RUBBER_TREE_CONFIGURED = configuredKey("rubber_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> OIL_CONFIGURED = configuredKey("oil");
    public static final ResourceKey<PlacedFeature> ORE_TIN = placedKey("ore_tin");
    public static final ResourceKey<PlacedFeature> ORE_COPPER = placedKey("ore_copper");
    public static final ResourceKey<PlacedFeature> ORE_URANIUM = placedKey("ore_uranium");
    public static final ResourceKey<PlacedFeature> RUBBER_TREE = placedKey("rubber_tree");
    public static final ResourceKey<PlacedFeature> OIL = placedKey("oil");
    public static final ResourceKey<BiomeModifier> ADD_ORES = modifierKey("add_ores");
    public static final ResourceKey<BiomeModifier> REMOVE_VANILLA_COPPER = modifierKey("remove_vanilla_copper");
    public static final ResourceKey<BiomeModifier> ADD_RUBBER_TREES = modifierKey("add_rubber_trees");
    public static final ResourceKey<BiomeModifier> ADD_OIL = modifierKey("add_oil");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GT_GALENA_CONFIGURED = configuredKey("gt_galena_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GT_IRIDIUM_CONFIGURED = configuredKey("gt_iridium_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GT_RUBY_CONFIGURED = configuredKey("gt_ruby_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GT_SAPPHIRE_CONFIGURED = configuredKey("gt_sapphire_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GT_BAUXITE_CONFIGURED = configuredKey("gt_bauxite_ore");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GT_NETHER_ORES_CONFIGURED = configuredKey("gt_nether_ores");
    public static final ResourceKey<ConfiguredFeature<?, ?>> GT_END_ORES_CONFIGURED = configuredKey("gt_end_ores");
    public static final ResourceKey<PlacedFeature> GT_GALENA = placedKey("gt_galena_ore");
    public static final ResourceKey<PlacedFeature> GT_IRIDIUM = placedKey("gt_iridium_ore");
    public static final ResourceKey<PlacedFeature> GT_RUBY = placedKey("gt_ruby_ore");
    public static final ResourceKey<PlacedFeature> GT_SAPPHIRE = placedKey("gt_sapphire_ore");
    public static final ResourceKey<PlacedFeature> GT_BAUXITE = placedKey("gt_bauxite_ore");
    public static final ResourceKey<PlacedFeature> GT_NETHER_ORES = placedKey("gt_nether_ores");
    public static final ResourceKey<PlacedFeature> GT_END_ORES = placedKey("gt_end_ores");
    public static final ResourceKey<BiomeModifier> ADD_GT_ORES = modifierKey("add_gt_ores");
    public static final ResourceKey<BiomeModifier> ADD_GT_NETHER_ORES = modifierKey("add_gt_nether_ores");
    public static final ResourceKey<BiomeModifier> ADD_GT_END_ORES = modifierKey("add_gt_end_ores");

    /** IC2 vein counts per chunk: {@code X * baseScale / 64} with baseScale 65 (sea level 63 + 1, density 1.0). */
    private static final int COPPER_BASE_COUNT = 15 * 65 / 64;
    private static final int TIN_BASE_COUNT = 25 * 65 / 64;
    private static final int URANIUM_BASE_COUNT = 20 * 65 / 64;

    private CraftWorldGen() {
    }

    private static ResourceKey<ConfiguredFeature<?, ?>> configuredKey(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, ResourceLocation.fromNamespaceAndPath(Craft.MODID, name));
    }

    private static ResourceKey<PlacedFeature> placedKey(String name) {
        return ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(Craft.MODID, name));
    }

    private static ResourceKey<BiomeModifier> modifierKey(String name) {
        return ResourceKey.create(NeoForgeRegistries.Keys.BIOME_MODIFIERS,
                ResourceLocation.fromNamespaceAndPath(Craft.MODID, name));
    }

    public static void bootstrapConfigured(BootstrapContext<ConfiguredFeature<?, ?>> context) {
        TagMatchTest stone = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        TagMatchTest deepslate = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
        register(context, ORE_TIN_CONFIGURED, Feature.ORE, new OreConfiguration(List.of(
                OreConfiguration.target(stone, Ic2Blocks.TIN_ORE.get().defaultBlockState()),
                OreConfiguration.target(deepslate, Ic2Blocks.DEEPSLATE_TIN_ORE.get().defaultBlockState())), 6));
        register(context, ORE_COPPER_CONFIGURED, Feature.ORE, new OreConfiguration(List.of(
                OreConfiguration.target(stone, Blocks.COPPER_ORE.defaultBlockState()),
                OreConfiguration.target(deepslate, Blocks.DEEPSLATE_COPPER_ORE.defaultBlockState())), 10));
        register(context, ORE_URANIUM_CONFIGURED, Feature.ORE, new OreConfiguration(List.of(
                OreConfiguration.target(stone, Ic2Blocks.URANIUM_ORE.get().defaultBlockState()),
                OreConfiguration.target(deepslate, Ic2Blocks.DEEPSLATE_URANIUM_ORE.get().defaultBlockState())), 3));
        register(context, RUBBER_TREE_CONFIGURED, Ic2Features.RUBBER_TREE.get(), NoneFeatureConfiguration.INSTANCE);
        register(context, OIL_CONFIGURED, BcFeatures.OIL.get(), NoneFeatureConfiguration.INSTANCE);
        register(context, GT_GALENA_CONFIGURED, Feature.ORE, new OreConfiguration(gtTargets(GtOre.GALENA), 16));
        register(context, GT_IRIDIUM_CONFIGURED, Feature.REPLACE_SINGLE_BLOCK,
                new ReplaceBlockConfiguration(gtTargets(GtOre.IRIDIUM)));
        register(context, GT_RUBY_CONFIGURED, Feature.REPLACE_SINGLE_BLOCK,
                new ReplaceBlockConfiguration(gtTargets(GtOre.RUBY)));
        register(context, GT_SAPPHIRE_CONFIGURED, Feature.REPLACE_SINGLE_BLOCK,
                new ReplaceBlockConfiguration(gtTargets(GtOre.SAPPHIRE)));
        register(context, GT_BAUXITE_CONFIGURED, Feature.ORE, new OreConfiguration(gtTargets(GtOre.BAUXITE), 16));
        register(context, GT_NETHER_ORES_CONFIGURED, GtWorldGen.NETHER_ORES.get(), NoneFeatureConfiguration.INSTANCE);
        register(context, GT_END_ORES_CONFIGURED, GtWorldGen.END_ORES.get(), NoneFeatureConfiguration.INSTANCE);
    }

    /** Overworld GT ore in stone, its deepslate variant (if any) in deepslate. */
    private static List<OreConfiguration.TargetBlockState> gtTargets(GtOre ore) {
        TagMatchTest stone = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
        if (!ore.hasDeepslateVariant()) {
            return List.of(OreConfiguration.target(stone, GtBlocks.ore(ore).get().defaultBlockState()));
        }
        return List.of(OreConfiguration.target(stone, GtBlocks.ore(ore).get().defaultBlockState()),
                OreConfiguration.target(new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES),
                        GtBlocks.deepslateOre(ore).get().defaultBlockState()));
    }

    private static <C extends FeatureConfiguration, F extends Feature<C>> void register(
            BootstrapContext<ConfiguredFeature<?, ?>> context, ResourceKey<ConfiguredFeature<?, ?>> key, F feature,
            C configuration) {
        context.register(key, new ConfiguredFeature<>(feature, configuration));
    }

    public static void bootstrapPlaced(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configured = context.lookup(Registries.CONFIGURED_FEATURE);
        // y = rand(40): uniform 0..39
        context.register(ORE_TIN, new PlacedFeature(configured.getOrThrow(ORE_TIN_CONFIGURED),
                orePlacement(TIN_BASE_COUNT, HeightRangePlacement.uniform(VerticalAnchor.absolute(0),
                        VerticalAnchor.absolute(39)))));
        // y = rand(40) + rand(20) + 10: exactly a trapezoid 10..68 with plateau 20
        context.register(ORE_COPPER, new PlacedFeature(configured.getOrThrow(ORE_COPPER_CONFIGURED),
                orePlacement(COPPER_BASE_COUNT, HeightRangePlacement.of(TrapezoidHeight.of(VerticalAnchor.absolute(10),
                        VerticalAnchor.absolute(68), 20)))));
        // y = rand(64 * 65 / 64) = rand(65): uniform 0..64
        context.register(ORE_URANIUM, new PlacedFeature(configured.getOrThrow(ORE_URANIUM_CONFIGURED),
                orePlacement(URANIUM_BASE_COUNT, HeightRangePlacement.uniform(VerticalAnchor.absolute(0),
                        VerticalAnchor.absolute(64)))));
        context.register(RUBBER_TREE, new PlacedFeature(configured.getOrThrow(RUBBER_TREE_CONFIGURED), List.of()));
        context.register(OIL, new PlacedFeature(configured.getOrThrow(OIL_CONFIGURED), List.of()));
        // GT_Worldgenerator.generateSurface: galena 1 vein y = rand(32); iridium rand(100) < 20, y = rand(99);
        // ruby/sapphire 8 blocks y = rand(32), bauxite 2 veins y = 32 + rand(48), by the biome at chunk + 8.
        context.register(GT_GALENA, new PlacedFeature(configured.getOrThrow(GT_GALENA_CONFIGURED), List.of(
                CountPlacement.of(1), InSquarePlacement.spread(), uniformHeight(0, 31), BiomeFilter.biome())));
        context.register(GT_IRIDIUM, new PlacedFeature(configured.getOrThrow(GT_IRIDIUM_CONFIGURED), List.of(
                RarityFilter.onAverageOnceEvery(5), InSquarePlacement.spread(), uniformHeight(0, 98),
                BiomeFilter.biome())));
        context.register(GT_RUBY, new PlacedFeature(configured.getOrThrow(GT_RUBY_CONFIGURED), List.of(
                ChunkBiomeFilter.of(GtWorldGen.RUBY_BIOMES), CountPlacement.of(8), InSquarePlacement.spread(),
                uniformHeight(0, 31))));
        context.register(GT_SAPPHIRE, new PlacedFeature(configured.getOrThrow(GT_SAPPHIRE_CONFIGURED), List.of(
                ChunkBiomeFilter.of(GtWorldGen.SAPPHIRE_BIOMES), CountPlacement.of(8), InSquarePlacement.spread(),
                uniformHeight(0, 31))));
        context.register(GT_BAUXITE, new PlacedFeature(configured.getOrThrow(GT_BAUXITE_CONFIGURED), List.of(
                ChunkBiomeFilter.of(GtWorldGen.BAUXITE_BIOMES), CountPlacement.of(2), InSquarePlacement.spread(),
                uniformHeight(32, 79))));
        context.register(GT_NETHER_ORES, new PlacedFeature(configured.getOrThrow(GT_NETHER_ORES_CONFIGURED),
                List.of()));
        context.register(GT_END_ORES, new PlacedFeature(configured.getOrThrow(GT_END_ORES_CONFIGURED), List.of()));
    }

    private static PlacementModifier uniformHeight(int min, int max) {
        return HeightRangePlacement.uniform(VerticalAnchor.absolute(min), VerticalAnchor.absolute(max));
    }

    /**
     * {@code round(nextGaussian * sqrt(base) + base)} veins per chunk; the clamped normal int truncates, so the mean
     * is shifted by 0.5.
     */
    private static List<PlacementModifier> orePlacement(int baseCount, PlacementModifier height) {
        return List.of(CountPlacement.of(ClampedNormalInt.of(baseCount + 0.5F, (float) Math.sqrt(baseCount), 0,
                        baseCount * 4)),
                InSquarePlacement.spread(), height, BiomeFilter.biome());
    }

    public static void bootstrapBiomeModifiers(BootstrapContext<BiomeModifier> context) {
        HolderGetter<PlacedFeature> placed = context.lookup(Registries.PLACED_FEATURE);
        HolderGetter<Biome> biomes = context.lookup(Registries.BIOME);
        HolderSet<Biome> overworld = biomes.getOrThrow(BiomeTags.IS_OVERWORLD);
        context.register(ADD_ORES, new BiomeModifiers.AddFeaturesBiomeModifier(overworld,
                HolderSet.direct(placed.getOrThrow(ORE_COPPER), placed.getOrThrow(ORE_TIN),
                        placed.getOrThrow(ORE_URANIUM)),
                GenerationStep.Decoration.UNDERGROUND_ORES));
        context.register(REMOVE_VANILLA_COPPER, new BiomeModifiers.RemoveFeaturesBiomeModifier(overworld,
                HolderSet.direct(placed.getOrThrow(OrePlacements.ORE_COPPER),
                        placed.getOrThrow(OrePlacements.ORE_COPPER_LARGE)),
                Set.of(GenerationStep.Decoration.UNDERGROUND_ORES)));
        context.register(ADD_RUBBER_TREES, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(RubberTreeFeature.HAS_RUBBER_TREES), HolderSet.direct(placed.getOrThrow(RUBBER_TREE)),
                GenerationStep.Decoration.VEGETAL_DECORATION));
        // BuildCraft populates after the whole chunk decoration (PopulateChunkEvent.Post); not in the Nether/End.
        context.register(ADD_OIL, new BiomeModifiers.AddFeaturesBiomeModifier(overworld,
                HolderSet.direct(placed.getOrThrow(OIL)), GenerationStep.Decoration.TOP_LAYER_MODIFICATION));
        context.register(ADD_GT_ORES, new BiomeModifiers.AddFeaturesBiomeModifier(overworld,
                HolderSet.direct(placed.getOrThrow(GT_GALENA), placed.getOrThrow(GT_IRIDIUM), placed.getOrThrow(GT_RUBY),
                        placed.getOrThrow(GT_SAPPHIRE), placed.getOrThrow(GT_BAUXITE)),
                GenerationStep.Decoration.UNDERGROUND_ORES));
        context.register(ADD_GT_NETHER_ORES, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_NETHER), HolderSet.direct(placed.getOrThrow(GT_NETHER_ORES)),
                GenerationStep.Decoration.UNDERGROUND_ORES));
        context.register(ADD_GT_END_ORES, new BiomeModifiers.AddFeaturesBiomeModifier(
                biomes.getOrThrow(BiomeTags.IS_END), HolderSet.direct(placed.getOrThrow(GT_END_ORES)),
                GenerationStep.Decoration.UNDERGROUND_ORES));
    }
}
