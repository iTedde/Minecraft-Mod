package dev.craft.data;

import java.util.concurrent.CompletableFuture;

import dev.craft.Craft;
import dev.craft.core.explosion.Ic2Explosion;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class CraftBlockTagsProvider extends BlockTagsProvider {
    public CraftBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
                                  ExistingFileHelper files) {
        super(output, lookup, Craft.MODID, files);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(Ic2Explosion.WHITELIST).add(Blocks.BEDROCK);
    }
}
