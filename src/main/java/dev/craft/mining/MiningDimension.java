package dev.craft.mining;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalLong;

import dev.craft.Craft;
import dev.craft.data.CraftWorldGen;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.data.worldgen.placement.OrePlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.AmbientMoodSettings;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeGenerationSettings;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.DensityFunctions;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.placement.BiomeFilter;
import net.minecraft.world.level.levelgen.placement.CountPlacement;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.InSquarePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;
import net.minecraft.world.level.levelgen.placement.RarityFilter;

/**
 * Mining dimension (spec 18 §2–3): flat stone world for quarries – solid stone/deepslate from the bedrock floor up to a
 * flat surface at y 64, open sky above, no caves or fluids, fully lit, no mob spawns, all overworld ores twice.
 */
public final class MiningDimension {
    public static final ResourceKey<DimensionType> TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, id());
    public static final ResourceKey<NoiseGeneratorSettings> NOISE_SETTINGS =
            ResourceKey.create(Registries.NOISE_SETTINGS, id());
    public static final ResourceKey<Biome> BIOME = ResourceKey.create(Registries.BIOME, id());
    public static final ResourceKey<LevelStem> STEM = ResourceKey.create(Registries.LEVEL_STEM, id());
    public static final ResourceKey<Level> LEVEL = ResourceKey.create(Registries.DIMENSION, id());

    private static final int MIN_Y = -64;
    private static final int HEIGHT = 384;
    /** Top block of the flat stone surface. */
    public static final int SURFACE_Y = 64;
    /** Overworld sky and fog colours at temperature 0.5 ({@code OverworldBiomes.calculateSkyColor}). */
    private static final int SKY = 0x7BA4FF;
    private static final int FOG = 0xC0D8FF;

    private MiningDimension() {
    }

    private static ResourceLocation id() {
        return ResourceLocation.fromNamespaceAndPath(Craft.MODID, "mining");
    }

    public static void bootstrapType(BootstrapContext<DimensionType> context) {
        context.register(TYPE, new DimensionType(OptionalLong.of(6000L), true, false, false, false, 1.0, true, false,
                MIN_Y, HEIGHT, HEIGHT, BlockTags.INFINIBURN_OVERWORLD, BuiltinDimensionTypes.OVERWORLD_EFFECTS, 1.0F,
                new DimensionType.MonsterSettings(false, false, ConstantInt.of(0), 0)));
    }

    public static void bootstrapNoiseSettings(BootstrapContext<NoiseGeneratorSettings> context) {
        DensityFunction zero = DensityFunctions.zero();
        DensityFunction solid = DensityFunctions.yClampedGradient(SURFACE_Y, SURFACE_Y + 1, 1.0, -1.0);
        NoiseRouter router = new NoiseRouter(zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, solid, solid,
                zero, zero, zero);
        SurfaceRules.RuleSource bedrock = SurfaceRules.state(Blocks.BEDROCK.defaultBlockState());
        SurfaceRules.RuleSource surface = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.verticalGradient("bedrock_floor", VerticalAnchor.bottom(),
                        VerticalAnchor.aboveBottom(5)), bedrock),
                SurfaceRules.ifTrue(SurfaceRules.verticalGradient("deepslate", VerticalAnchor.absolute(0),
                        VerticalAnchor.absolute(8)), SurfaceRules.state(Blocks.DEEPSLATE.defaultBlockState())));
        context.register(NOISE_SETTINGS, new NoiseGeneratorSettings(NoiseSettings.create(MIN_Y, HEIGHT, 1, 2),
                Blocks.STONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), router, surface, List.of(), MIN_Y,
                true, false, false, false));
    }


    /** Key of the doubled mining-dimension copy of an overworld ore ({@code craft:mining/<name>}). */
    public static ResourceKey<PlacedFeature> doubled(ResourceKey<PlacedFeature> ore) {
        return ResourceKey.create(Registries.PLACED_FEATURE,
                ResourceLocation.fromNamespaceAndPath(Craft.MODID, "mining/" + ore.location().getPath()));
    }

    /**
     * Twice the overworld amount (user, 2026-10-17): the same vein size and height range, with a count of 2 in front
     * of the overworld placement, so every vein roll (count or rarity) runs twice per chunk.
     */
    public static void registerDoubled(BootstrapContext<PlacedFeature> context, ResourceKey<PlacedFeature> ore,
                                       Holder<ConfiguredFeature<?, ?>> feature, List<PlacementModifier> placement) {
        List<PlacementModifier> modifiers = new ArrayList<>();
        modifiers.add(CountPlacement.of(2));
        modifiers.addAll(placement);
        context.register(doubled(ore), new PlacedFeature(feature, modifiers));
    }

    /** Doubled copies of the vanilla ores; placements as in vanilla {@code OrePlacements}. */
    public static void bootstrapVanillaOres(BootstrapContext<PlacedFeature> context) {
        HolderGetter<ConfiguredFeature<?, ?>> configured = context.lookup(Registries.CONFIGURED_FEATURE);
        VerticalAnchor top = VerticalAnchor.top();
        VerticalAnchor bottom = VerticalAnchor.bottom();
        vanillaOre(context, configured, OrePlacements.ORE_COAL_UPPER, OreFeatures.ORE_COAL, 30, uniform(abs(136), top));
        vanillaOre(context, configured, OrePlacements.ORE_COAL_LOWER, OreFeatures.ORE_COAL_BURIED, 20,
                triangle(abs(0), abs(192)));
        vanillaOre(context, configured, OrePlacements.ORE_IRON_UPPER, OreFeatures.ORE_IRON, 90,
                triangle(abs(80), abs(384)));
        vanillaOre(context, configured, OrePlacements.ORE_IRON_MIDDLE, OreFeatures.ORE_IRON, 10,
                triangle(abs(-24), abs(56)));
        vanillaOre(context, configured, OrePlacements.ORE_IRON_SMALL, OreFeatures.ORE_IRON_SMALL, 10,
                uniform(bottom, abs(72)));
        vanillaOre(context, configured, OrePlacements.ORE_GOLD, OreFeatures.ORE_GOLD_BURIED, 4,
                triangle(abs(-64), abs(32)));
        registerDoubled(context, OrePlacements.ORE_GOLD_LOWER, configured.getOrThrow(OreFeatures.ORE_GOLD_BURIED),
                List.of(CountPlacement.of(UniformInt.of(0, 1)), InSquarePlacement.spread(),
                        uniform(abs(-64), abs(-48)), BiomeFilter.biome()));
        vanillaOre(context, configured, OrePlacements.ORE_REDSTONE, OreFeatures.ORE_REDSTONE, 4,
                uniform(bottom, abs(15)));
        vanillaOre(context, configured, OrePlacements.ORE_REDSTONE_LOWER, OreFeatures.ORE_REDSTONE, 8,
                triangle(VerticalAnchor.aboveBottom(-32), VerticalAnchor.aboveBottom(32)));
        vanillaOre(context, configured, OrePlacements.ORE_DIAMOND, OreFeatures.ORE_DIAMOND_SMALL, 7,
                triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(80)));
        vanillaOre(context, configured, OrePlacements.ORE_DIAMOND_MEDIUM, OreFeatures.ORE_DIAMOND_MEDIUM, 2,
                uniform(abs(-64), abs(-4)));
        registerDoubled(context, OrePlacements.ORE_DIAMOND_LARGE, configured.getOrThrow(OreFeatures.ORE_DIAMOND_LARGE),
                List.of(RarityFilter.onAverageOnceEvery(9), InSquarePlacement.spread(),
                        triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(80)),
                        BiomeFilter.biome()));
        vanillaOre(context, configured, OrePlacements.ORE_DIAMOND_BURIED, OreFeatures.ORE_DIAMOND_BURIED, 4,
                triangle(VerticalAnchor.aboveBottom(-80), VerticalAnchor.aboveBottom(80)));
        vanillaOre(context, configured, OrePlacements.ORE_LAPIS, OreFeatures.ORE_LAPIS, 2, triangle(abs(-32), abs(32)));
        vanillaOre(context, configured, OrePlacements.ORE_LAPIS_BURIED, OreFeatures.ORE_LAPIS_BURIED, 4,
                uniform(bottom, abs(64)));
        vanillaOre(context, configured, OrePlacements.ORE_EMERALD, OreFeatures.ORE_EMERALD, 100,
                triangle(abs(-16), abs(480)));
    }

    /** Vanilla {@code commonOrePlacement}. */
    private static void vanillaOre(BootstrapContext<PlacedFeature> context,
                                   HolderGetter<ConfiguredFeature<?, ?>> configured, ResourceKey<PlacedFeature> ore,
                                   ResourceKey<ConfiguredFeature<?, ?>> feature, int count, PlacementModifier height) {
        registerDoubled(context, ore, configured.getOrThrow(feature),
                List.of(CountPlacement.of(count), InSquarePlacement.spread(), height, BiomeFilter.biome()));
    }

    private static VerticalAnchor abs(int y) {
        return VerticalAnchor.absolute(y);
    }

    private static PlacementModifier uniform(VerticalAnchor min, VerticalAnchor max) {
        return HeightRangePlacement.uniform(min, max);
    }

    private static PlacementModifier triangle(VerticalAnchor min, VerticalAnchor max) {
        return HeightRangePlacement.triangle(min, max);
    }
    public static void bootstrapBiome(BootstrapContext<Biome> context) {
        HolderGetter<PlacedFeature> placed = context.lookup(Registries.PLACED_FEATURE);
        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(placed,
                context.lookup(Registries.CONFIGURED_CARVER));
        List<ResourceKey<PlacedFeature>> ores = List.of(OrePlacements.ORE_COAL_UPPER, OrePlacements.ORE_COAL_LOWER,
                OrePlacements.ORE_IRON_UPPER, OrePlacements.ORE_IRON_MIDDLE, OrePlacements.ORE_IRON_SMALL,
                OrePlacements.ORE_GOLD, OrePlacements.ORE_GOLD_LOWER, OrePlacements.ORE_REDSTONE,
                OrePlacements.ORE_REDSTONE_LOWER, OrePlacements.ORE_DIAMOND, OrePlacements.ORE_DIAMOND_MEDIUM,
                OrePlacements.ORE_DIAMOND_LARGE, OrePlacements.ORE_DIAMOND_BURIED, OrePlacements.ORE_LAPIS,
                OrePlacements.ORE_LAPIS_BURIED, OrePlacements.ORE_EMERALD, CraftWorldGen.ORE_COPPER,
                CraftWorldGen.ORE_TIN, CraftWorldGen.ORE_URANIUM, CraftWorldGen.GT_GALENA, CraftWorldGen.GT_IRIDIUM,
                CraftWorldGen.GT_RUBY, CraftWorldGen.GT_SAPPHIRE, CraftWorldGen.GT_BAUXITE);
        for (ResourceKey<PlacedFeature> ore : ores) {
            generation.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, placed.getOrThrow(doubled(ore)));
        }
        BiomeSpecialEffects effects = new BiomeSpecialEffects.Builder().fogColor(FOG).skyColor(SKY)
                .waterColor(4159204).waterFogColor(329011).ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                .build();
        context.register(BIOME, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(0.5F).downfall(0.0F)
                .specialEffects(effects).mobSpawnSettings(MobSpawnSettings.EMPTY).generationSettings(generation.build())
                .build());
    }

    public static void bootstrapStem(BootstrapContext<LevelStem> context) {
        context.register(STEM, new LevelStem(context.lookup(Registries.DIMENSION_TYPE).getOrThrow(TYPE),
                new NoiseBasedChunkGenerator(new FixedBiomeSource(context.lookup(Registries.BIOME).getOrThrow(BIOME)),
                        context.lookup(Registries.NOISE_SETTINGS).getOrThrow(NOISE_SETTINGS))));
    }
}
