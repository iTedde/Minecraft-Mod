package ivorius.psychedelicraft.world.gen;

import net.minecraft.core.Holder;
import net.minecraft.world.level.biome.Biome;

/**
 * The biome a feature may be added to (upstream: Fabric {@code BiomeSelectionContext}).
 */
public record BiomeSelectionContext(Holder<Biome> holder) {
    public Biome getBiome() {
        return holder.value();
    }

    public Holder<Biome> getBiomeRegistryEntry() {
        return holder;
    }
}
