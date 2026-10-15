package dev.craft.data;

import java.util.concurrent.CompletableFuture;

import dev.craft.Craft;
import dev.craft.core.CraftDamageTypes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class CraftDamageTypeTagsProvider extends TagsProvider<DamageType> {
    public CraftDamageTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
                                       ExistingFileHelper files) {
        super(output, Registries.DAMAGE_TYPE, lookup, Craft.MODID, files);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(DamageTypeTags.BYPASSES_ARMOR).add(CraftDamageTypes.RADIATION);
    }
}
