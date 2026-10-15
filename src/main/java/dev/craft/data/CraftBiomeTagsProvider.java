package dev.craft.data;

import java.util.concurrent.CompletableFuture;

import dev.craft.Craft;
import dev.craft.ic2.world.RubberTreeFeature;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
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
    }
}
