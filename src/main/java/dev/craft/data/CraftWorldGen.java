package dev.craft.data;

import java.util.List;
import java.util.Set;

import dev.craft.Craft;
import dev.craft.buildcraft.world.BcFeatures;
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
import net.minecraft.world.level.levelgen.heightproviders.TrapezoidHeight;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.BiomeModifiers;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * World generation as data: IC2 ({@code IC2.generate}) tin ore, copper ore with IC2 frequency instead of the vanilla
 * copper veins, rubber trees; BuildCraft oil.
 */
public final class CraftWorldGen {
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_TIN_CONFIGURED = configuredKey("ore_tin");
    public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_COPPER_CONFIGURED = configuredKey("ore_copper");
    public static final ResourceKey<ConfiguredFeature<?, ?>> RUBBER_TREE_CONFIGURED = configuredKey("rubber_tree");
    public static final ResourceKey<ConfiguredFeature<?, ?>> OIL_CONFIGURED = configuredKey("oil");
    public static final ResourceKey<PlacedFeature> ORE_TIN = placedKey("ore_tin");
    public static final ResourceKey<PlacedFeature> ORE_COPPER = placedKey("ore_copper");
    public static final ResourceKey<PlacedFeature> RUBBER_TREE = placedKey("rubber_tree");
    public static final ResourceKey<PlacedFeature> OIL = placedKey("oil");
    public static final ResourceKey<BiomeModifier> ADD_ORES = modifierKey("add_ores");
    public static final ResourceKey<BiomeModifier> REMOVE_VANILLA_COPPER = modifierKey("remove_vanilla_copper");
    public static final ResourceKey<BiomeModifier> ADD_RUBBER_TREES = modifierKey("add_rubber_trees");
    public static final ResourceKey<BiomeModifier> ADD_OIL = modifierKey("add_oil");

    /** IC2 vein counts per chunk: {@code X * baseScale / 64} with baseScale 65 (sea level 63 + 1, density 1.0). */
    private static final int COPPER_BASE_COUNT = 15 * 65 / 64;
    private static final int TIN_BASE_COUNT = 25 * 65 / 64;

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
        register(context, RUBBER_TREE_CONFIGURED, Ic2Features.RUBBER_TREE.get(), NoneFeatureConfiguration.INSTANCE);
        register(context, OIL_CONFIGURED, BcFeatures.OIL.get(), NoneFeatureConfiguration.INSTANCE);
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
        context.register(RUBBER_TREE, new PlacedFeature(configured.getOrThrow(RUBBER_TREE_CONFIGURED), List.of()));
        context.register(OIL, new PlacedFeature(configured.getOrThrow(OIL_CONFIGURED), List.of()));
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
                HolderSet.direct(placed.getOrThrow(ORE_COPPER), placed.getOrThrow(ORE_TIN)),
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
    }
}
