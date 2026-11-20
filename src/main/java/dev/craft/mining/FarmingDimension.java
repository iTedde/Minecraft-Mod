package dev.craft.mining;

import java.util.List;
import java.util.OptionalLong;

import dev.craft.Craft;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.Level;
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
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.NoiseRouter;
import net.minecraft.world.level.levelgen.NoiseSettings;
import net.minecraft.world.level.levelgen.SurfaceRules;
import net.minecraft.world.level.levelgen.VerticalAnchor;

/**
 * Farming dimension (spec 53 §2): flat grass world at y 64 over dirt and stone, always noon, no rain, no mobs, one
 * plains-like biome that is in no biome tag so other mods' biome modifiers stay out. The gardens follow in 53.2.
 */
public final class FarmingDimension {
    public static final ResourceKey<DimensionType> TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, id());
    public static final ResourceKey<NoiseGeneratorSettings> NOISE_SETTINGS =
            ResourceKey.create(Registries.NOISE_SETTINGS, id());
    public static final ResourceKey<Biome> BIOME = ResourceKey.create(Registries.BIOME, id());
    public static final ResourceKey<LevelStem> STEM = ResourceKey.create(Registries.LEVEL_STEM, id());
    public static final ResourceKey<Level> LEVEL = ResourceKey.create(Registries.DIMENSION, id());

    private static final int MIN_Y = 0;
    private static final int HEIGHT = 256;
    /** Grass block layer of the flat surface. */
    public static final int SURFACE_Y = 64;
    /** Top stone layer; dirt fills the blocks between it and the grass. */
    public static final int STONE_TOP_Y = 60;
    /** Plains sky and fog colours ({@code OverworldBiomes.calculateSkyColor(0.8)}). */
    private static final int SKY = 0x78A7FF;
    private static final int FOG = 0xC0D8FF;

    private FarmingDimension() {
    }

    private static ResourceLocation id() {
        return ResourceLocation.fromNamespaceAndPath(Craft.MODID, "farming");
    }

    /** Like the mining dimension, but y 0–256 and normal ambient light (light comes from the fixed noon). */
    public static void bootstrapType(BootstrapContext<DimensionType> context) {
        context.register(TYPE, new DimensionType(OptionalLong.of(6000L), true, false, false, false, 1.0, true, false,
                MIN_Y, HEIGHT, HEIGHT, BlockTags.INFINIBURN_OVERWORLD, BuiltinDimensionTypes.OVERWORLD_EFFECTS, 0.0F,
                new DimensionType.MonsterSettings(false, false, ConstantInt.of(0), 0)));
    }

    public static void bootstrapNoiseSettings(BootstrapContext<NoiseGeneratorSettings> context) {
        DensityFunction zero = DensityFunctions.zero();
        DensityFunction solid = DensityFunctions.yClampedGradient(SURFACE_Y, SURFACE_Y + 1, 1.0, -1.0);
        NoiseRouter router = new NoiseRouter(zero, zero, zero, zero, zero, zero, zero, zero, zero, zero, solid, solid,
                zero, zero, zero);
        SurfaceRules.RuleSource surface = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.not(SurfaceRules.yBlockCheck(VerticalAnchor.absolute(MIN_Y + 1), 0)),
                        SurfaceRules.state(Blocks.BEDROCK.defaultBlockState())),
                SurfaceRules.ifTrue(SurfaceRules.yBlockCheck(VerticalAnchor.absolute(SURFACE_Y), 0),
                        SurfaceRules.state(Blocks.GRASS_BLOCK.defaultBlockState())),
                SurfaceRules.ifTrue(SurfaceRules.yBlockCheck(VerticalAnchor.absolute(STONE_TOP_Y + 1), 0),
                        SurfaceRules.state(Blocks.DIRT.defaultBlockState())));
        context.register(NOISE_SETTINGS, new NoiseGeneratorSettings(NoiseSettings.create(MIN_Y, HEIGHT, 1, 2),
                Blocks.STONE.defaultBlockState(), Blocks.AIR.defaultBlockState(), router, surface, List.of(), MIN_Y,
                true, false, false, false));
    }

    /** Plains climate without rain, empty spawns and no features yet (gardens: 53.2). */
    public static void bootstrapBiome(BootstrapContext<Biome> context) {
        BiomeGenerationSettings.Builder generation = new BiomeGenerationSettings.Builder(
                context.lookup(Registries.PLACED_FEATURE), context.lookup(Registries.CONFIGURED_CARVER));
        BiomeSpecialEffects effects = new BiomeSpecialEffects.Builder().fogColor(FOG).skyColor(SKY)
                .waterColor(4159204).waterFogColor(329011).build();
        context.register(BIOME, new Biome.BiomeBuilder().hasPrecipitation(false).temperature(0.8F).downfall(0.4F)
                .specialEffects(effects).mobSpawnSettings(MobSpawnSettings.EMPTY).generationSettings(generation.build())
                .build());
    }

    public static void bootstrapStem(BootstrapContext<LevelStem> context) {
        context.register(STEM, new LevelStem(context.lookup(Registries.DIMENSION_TYPE).getOrThrow(TYPE),
                new NoiseBasedChunkGenerator(new FixedBiomeSource(context.lookup(Registries.BIOME).getOrThrow(BIOME)),
                        context.lookup(Registries.NOISE_SETTINGS).getOrThrow(NOISE_SETTINGS))));
    }
}
