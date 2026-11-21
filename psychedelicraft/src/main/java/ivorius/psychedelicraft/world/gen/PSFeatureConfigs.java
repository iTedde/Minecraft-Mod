package ivorius.psychedelicraft.world.gen;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public interface PSFeatureConfigs {
    ResourceKey<ConfiguredFeature<?, ?>> JUNIPER_TREE = of("juniper_tree");
    ResourceKey<ConfiguredFeature<?, ?>> CANNABIS_TILLED_PATCH = of("cannabis_tilled_patch");
    ResourceKey<ConfiguredFeature<?, ?>> HOP_TILLED_PATCH = of("hop_tilled_patch");
    ResourceKey<ConfiguredFeature<?, ?>> TOBACCO_TILLED_PATCH = of("tobacco_tilled_patch");
    ResourceKey<ConfiguredFeature<?, ?>> COFFEA_TILLED_PATCH = of("coffea_tilled_patch");
    ResourceKey<ConfiguredFeature<?, ?>> COCA_TILLED_PATCH = of("coca_tilled_patch");

    ResourceKey<ConfiguredFeature<?, ?>> MORNING_GLORY_PATCH = of("morning_glory_patch");
    ResourceKey<ConfiguredFeature<?, ?>> BELLADONNA_PATCH = of("belladonna_patch");
    ResourceKey<ConfiguredFeature<?, ?>> JIMSONWEED_PATCH = of("jimsonweed_patch");
    ResourceKey<ConfiguredFeature<?, ?>> TOMATO_PATCH = of("tomato_patch");
    ResourceKey<ConfiguredFeature<?, ?>> PEYOTE_PATCH = of("peyote_patch");
    ResourceKey<ConfiguredFeature<?, ?>> AGAVE_PATCH = of("agave_patch");

    static ResourceKey<ConfiguredFeature<?, ?>> of(String name) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, Psychedelicraft.id(name));
    }

}
