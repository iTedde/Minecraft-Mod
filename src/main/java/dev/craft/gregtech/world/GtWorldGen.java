package dev.craft.gregtech.world;

import java.util.function.Supplier;

import dev.craft.Craft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Registries of the GregTech ore generation ({@code GT_Worldgenerator}) and ore drops ({@code GT_BlockMetaID_Ore}). */
public final class GtWorldGen {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Craft.MODID);
    public static final DeferredRegister<PlacementModifierType<?>> PLACEMENT_MODIFIERS =
            DeferredRegister.create(Registries.PLACEMENT_MODIFIER_TYPE, Craft.MODID);
    public static final DeferredRegister<LootItemFunctionType<?>> LOOT_FUNCTIONS =
            DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, Craft.MODID);
    public static final DeferredRegister<LootItemConditionType> LOOT_CONDITIONS =
            DeferredRegister.create(Registries.LOOT_CONDITION_TYPE, Craft.MODID);

    public static final Supplier<Feature<NoneFeatureConfiguration>> NETHER_ORES =
            FEATURES.register("gt_nether_ores", GtNetherOresFeature::new);
    public static final Supplier<Feature<NoneFeatureConfiguration>> END_ORES =
            FEATURES.register("gt_end_ores", GtEndOresFeature::new);
    public static final Supplier<PlacementModifierType<ChunkBiomeFilter>> CHUNK_BIOME =
            PLACEMENT_MODIFIERS.register("chunk_biome", () -> () -> ChunkBiomeFilter.CODEC);
    public static final Supplier<LootItemFunctionType<GtOreCountFunction>> ORE_COUNT =
            LOOT_FUNCTIONS.register("gt_ore_count", () -> new LootItemFunctionType<>(GtOreCountFunction.CODEC));
    public static final Supplier<LootItemConditionType> ORE_CHANCE =
            LOOT_CONDITIONS.register("gt_ore_chance", () -> new LootItemConditionType(GtOreChanceCondition.CODEC));

    /** GT biome lists ({@code mRubyList}, {@code mSapphireList}, {@code mBauxiteList}) as biome tags. */
    public static final TagKey<Biome> RUBY_BIOMES = biomeTag("has_gt_ruby_ore");
    public static final TagKey<Biome> SAPPHIRE_BIOMES = biomeTag("has_gt_sapphire_ore");
    public static final TagKey<Biome> BAUXITE_BIOMES = biomeTag("has_gt_bauxite_ore");

    private GtWorldGen() {
    }

    public static void register(IEventBus modBus) {
        FEATURES.register(modBus);
        PLACEMENT_MODIFIERS.register(modBus);
        LOOT_FUNCTIONS.register(modBus);
        LOOT_CONDITIONS.register(modBus);
    }

    private static TagKey<Biome> biomeTag(String name) {
        return TagKey.create(Registries.BIOME, ResourceLocation.fromNamespaceAndPath(Craft.MODID, name));
    }
}
