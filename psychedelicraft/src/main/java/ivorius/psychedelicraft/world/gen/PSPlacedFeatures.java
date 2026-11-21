package ivorius.psychedelicraft.world.gen;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public interface PSPlacedFeatures {
    ResourceKey<PlacedFeature> SPARCE_JUNIPER_TREE = of("sparce_juniper_tree_checked");
    ResourceKey<PlacedFeature> DENSE_JUNIPER_TREE = of("dense_juniper_tree_checked");
    ResourceKey<PlacedFeature> CANNABIS_TILLED_PATCH = of("cannabis_tilled_patch_checked");
    ResourceKey<PlacedFeature> HOP_TILLED_PATCH = of("hop_tilled_patch_checked");
    ResourceKey<PlacedFeature> TOBACCO_TILLED_PATCH = of("tobacco_tilled_patch_checked");
    ResourceKey<PlacedFeature> COFFEA_TILLED_PATCH = of("coffea_tilled_patch_checked");
    ResourceKey<PlacedFeature> COCA_TILLED_PATCH = of("coca_tilled_patch_checked");

    ResourceKey<PlacedFeature> MORNING_GLORY_PATCH_CHECKED = of("morning_glory_patch_checked");
    ResourceKey<PlacedFeature> BELLADONNA_PATCH_CHECKED = of("belladonna_patch_checked");
    ResourceKey<PlacedFeature> JIMSONWEED_PATCH_CHECKED = of("jimsonweed_patch_checked");
    ResourceKey<PlacedFeature> TOMATO_PATCH_CHECKED = of("tomato_patch_checked");
    ResourceKey<PlacedFeature> PEYOTE_PATCH_CHECKED = of("peyote_patch_checked");
    ResourceKey<PlacedFeature> AGAVE_PATCH_CHECKED = of("agave_patch_checked");

    ResourceKey<PlacedFeature> MORNING_GLORY_PATCH_UNCHECKED = of("morning_glory_patch_unchecked");
    ResourceKey<PlacedFeature> BELLADONNA_PATCH_UNCHECKED = of("belladonna_patch_unchecked");
    ResourceKey<PlacedFeature> JIMSONWEED_PATCH_UNCHECKED = of("jimsonweed_patch_unchecked");
    ResourceKey<PlacedFeature> TOMATO_PATCH_UNCHECKED = of("tomato_patch_unchecked");
    ResourceKey<PlacedFeature> PEYOTE_PATCH_UNCHECKED = of("peyote_patch_unchecked");
    ResourceKey<PlacedFeature> AGAVE_PATCH_UNCHECKED = of("agave_patch_unchecked");

    static ResourceKey<PlacedFeature> of(String id) {
        return ResourceKey.create(Registries.PLACED_FEATURE, Psychedelicraft.id(id));
    }

}
