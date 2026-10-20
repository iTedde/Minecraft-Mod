package dev.craft.thermalexpansion.world;

import java.util.List;
import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.thermalexpansion.TeBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Thermal Expansion ore veins (spec 26 §3, pack config {@code world.tweak}): silver veins are 10 % lead, lead veins 20 %
 * silver. Placement (count, height) lives in {@code CraftWorldGen}.
 */
public final class TeWorldGen {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Craft.MODID);

    public static final int SILVER_CLUSTERS = 3;
    public static final int SILVER_MIN_Y = 5;
    public static final int SILVER_MAX_Y = 30;
    public static final int LEAD_CLUSTERS = 4;
    public static final int LEAD_MIN_Y = 10;
    public static final int LEAD_MAX_Y = 35;
    public static final int NICKEL_CLUSTERS = 2;
    public static final int NICKEL_MIN_Y = 5;
    public static final int NICKEL_MAX_Y = 20;

    private static final TeOreFeature.Entry SILVER = new TeOreFeature.Entry(TeBlocks.SILVER_ORE,
            TeBlocks.DEEPSLATE_SILVER_ORE, 90);
    private static final TeOreFeature.Entry LEAD_IN_SILVER = new TeOreFeature.Entry(TeBlocks.LEAD_ORE,
            TeBlocks.DEEPSLATE_LEAD_ORE, 10);
    private static final TeOreFeature.Entry LEAD = new TeOreFeature.Entry(TeBlocks.LEAD_ORE,
            TeBlocks.DEEPSLATE_LEAD_ORE, 80);
    private static final TeOreFeature.Entry SILVER_IN_LEAD = new TeOreFeature.Entry(TeBlocks.SILVER_ORE,
            TeBlocks.DEEPSLATE_SILVER_ORE, 20);

    public static final Supplier<Feature<NoneFeatureConfiguration>> SILVER_ORE = FEATURES.register("te_silver_ore",
            () -> new TeOreFeature(List.of(SILVER, LEAD_IN_SILVER), 8));
    public static final Supplier<Feature<NoneFeatureConfiguration>> LEAD_ORE = FEATURES.register("te_lead_ore",
            () -> new TeOreFeature(List.of(LEAD, SILVER_IN_LEAD), 8));
    public static final Supplier<Feature<NoneFeatureConfiguration>> NICKEL_ORE = FEATURES.register("te_nickel_ore",
            () -> new TeOreFeature(List.of(new TeOreFeature.Entry(TeBlocks.NICKEL_ORE, TeBlocks.DEEPSLATE_NICKEL_ORE,
                    1)), 4));

    private TeWorldGen() {
    }
}
