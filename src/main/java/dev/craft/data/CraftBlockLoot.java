package dev.craft.data;

import java.util.Set;

import dev.craft.ic2.Ic2Blocks;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;

public final class CraftBlockLoot extends BlockLootSubProvider {
    public CraftBlockLoot(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.REGISTRY.allFlags(), registries);
    }

    @Override
    protected void generate() {
        Ic2Blocks.BLOCKS.getEntries().forEach(block -> dropSelf(block.get()));
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return Ic2Blocks.BLOCKS.getEntries().stream().map(holder -> (Block) holder.get()).toList();
    }
}
