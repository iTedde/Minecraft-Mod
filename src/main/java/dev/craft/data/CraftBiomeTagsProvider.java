package dev.craft.data;

import java.util.concurrent.CompletableFuture;

import dev.craft.Craft;
import dev.craft.gregtech.world.GtWorldGen;
import dev.craft.ic2.world.RubberTreeFeature;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class CraftBiomeTagsProvider extends BiomeTagsProvider {
    public CraftBiomeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
                                  ExistingFileHelper files) {
        super(output, lookup, Craft.MODID, files);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(RubberTreeFeature.HAS_RUBBER_TREES).addTag(BiomeTags.IS_TAIGA).addTag(BiomeTags.IS_FOREST)
                .addTag(BiomeTags.IS_JUNGLE).addTag(Tags.Biomes.IS_SWAMP);
        // GT biome lists with their 1.21 counterparts (spec 07 §6.5, decided 2026-10-16).
        tag(GtWorldGen.RUBY_BIOMES).add(Biomes.DESERT).addTag(BiomeTags.IS_BADLANDS).addTag(BiomeTags.IS_SAVANNA);
        tag(GtWorldGen.SAPPHIRE_BIOMES).addTag(BiomeTags.IS_OCEAN).addTag(BiomeTags.IS_BEACH)
                .add(Biomes.MANGROVE_SWAMP);
        tag(GtWorldGen.BAUXITE_BIOMES).add(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS, Biomes.MEADOW)
                .addTag(BiomeTags.IS_FOREST).add(Biomes.CHERRY_GROVE);
    }
}
