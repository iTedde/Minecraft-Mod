package ivorius.psychedelicraft.world.gen;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.levelgen.feature.Feature;

public interface PSFeatures {
    ResourceKey<Feature<?>> TILLED_PATCH = ResourceKey.create(Registries.FEATURE, Psychedelicraft.id("tilled_patch"));
}
