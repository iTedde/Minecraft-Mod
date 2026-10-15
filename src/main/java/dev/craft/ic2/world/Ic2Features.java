package dev.craft.ic2.world;

import java.util.function.Supplier;

import dev.craft.Craft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class Ic2Features {
    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(Registries.FEATURE, Craft.MODID);

    public static final Supplier<Feature<NoneFeatureConfiguration>> RUBBER_TREE =
            FEATURES.register("rubber_tree", RubberTreeFeature::new);

    private Ic2Features() {
    }
}
