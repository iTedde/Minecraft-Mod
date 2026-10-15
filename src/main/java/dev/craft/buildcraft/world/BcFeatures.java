package dev.craft.buildcraft.world;

import java.util.function.Supplier;

import dev.craft.Craft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class BcFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Craft.MODID);

    public static final Supplier<Feature<NoneFeatureConfiguration>> OIL = FEATURES.register("oil", OilFeature::new);

    private BcFeatures() {
    }
}
