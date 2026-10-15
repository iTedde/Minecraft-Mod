package dev.craft.data;

import java.util.concurrent.CompletableFuture;

import dev.craft.Craft;
import dev.craft.buildcraft.BcBlocks;
import dev.craft.buildcraft.engine.EngineType;
import dev.craft.core.explosion.Ic2Explosion;
import dev.craft.forestry.FrBlocks;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.item.tool.ChainsawItem;
import dev.craft.ic2.item.tool.DrillItem;
import dev.craft.ic2.item.tool.ElectricHoeItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class CraftBlockTagsProvider extends BlockTagsProvider {
    public static final TagKey<Block> ORES_TIN = commonTag("ores/tin");
    public static final TagKey<Block> STORAGE_BLOCKS_TIN = commonTag("storage_blocks/tin");
    public static final TagKey<Block> STORAGE_BLOCKS_BRONZE = commonTag("storage_blocks/bronze");

    public CraftBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
                                  ExistingFileHelper files) {
        super(output, lookup, Craft.MODID, files);
    }

    private static TagKey<Block> commonTag(String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", path));
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(Ic2Explosion.WHITELIST).add(Blocks.BEDROCK);
        for (Ic2Machine machine : Ic2Machine.values()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(Ic2Blocks.machine(machine).get());
        }
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(Ic2Blocks.MACHINE_CASING.get(), Ic2Blocks.TIN_ORE.get(),
                Ic2Blocks.DEEPSLATE_TIN_ORE.get(), Ic2Blocks.TIN_BLOCK.get(), Ic2Blocks.BRONZE_BLOCK.get());
        tag(BlockTags.NEEDS_STONE_TOOL).add(Ic2Blocks.TIN_ORE.get(), Ic2Blocks.DEEPSLATE_TIN_ORE.get());
        for (EngineType type : EngineType.values()) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(BcBlocks.engine(type).get());
        }
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(BcBlocks.QUARRY.get(), BcBlocks.PUMP.get(), BcBlocks.REFINERY.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(FrBlocks.THERMIONIC_FABRICATOR.get(), FrBlocks.CARPENTER.get(),
                FrBlocks.ELECTRICAL_ENGINE.get());
        // Material.ground: the shovel is the effective tool.
        tag(BlockTags.MINEABLE_WITH_SHOVEL).add(BcBlocks.MINING_WELL.get());
        tag(BlockTags.MINEABLE_WITH_AXE).add(Ic2Blocks.RUBBER_WOOD.get());
        tag(BlockTags.MINEABLE_WITH_HOE).add(Ic2Blocks.RUBBER_LEAVES.get());
        tag(BlockTags.LOGS_THAT_BURN).add(Ic2Blocks.RUBBER_WOOD.get());
        tag(BlockTags.LEAVES).add(Ic2Blocks.RUBBER_LEAVES.get());
        tag(BlockTags.SAPLINGS).add(Ic2Blocks.RUBBER_SAPLING.get());
        tag(ORES_TIN).add(Ic2Blocks.TIN_ORE.get(), Ic2Blocks.DEEPSLATE_TIN_ORE.get());
        tag(Tags.Blocks.ORES).addTag(ORES_TIN);
        tag(Tags.Blocks.ORES_IN_GROUND_STONE).add(Ic2Blocks.TIN_ORE.get());
        tag(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE).add(Ic2Blocks.DEEPSLATE_TIN_ORE.get());
        tag(STORAGE_BLOCKS_TIN).add(Ic2Blocks.TIN_BLOCK.get());
        tag(STORAGE_BLOCKS_BRONZE).add(Ic2Blocks.BRONZE_BLOCK.get());
        tag(Tags.Blocks.STORAGE_BLOCKS).addTag(STORAGE_BLOCKS_TIN).addTag(STORAGE_BLOCKS_BRONZE);
        addElectricToolTags();
    }

    /**
     * IC2 electric tools: Forge tool class, material rules and block lists as one tag per tool. Blocks split up
     * since 1.4.7 (coarse dirt, podzol, red sand, soul soil …) count like their base block.
     */
    private void addElectricToolTags() {
        tag(DrillItem.MINEABLE).addTag(BlockTags.MINEABLE_WITH_PICKAXE).addTag(BlockTags.SAND).add(
                Blocks.GRASS_BLOCK, Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.PODZOL, Blocks.ROOTED_DIRT,
                Blocks.MYCELIUM, Blocks.FARMLAND, Blocks.GRAVEL, Blocks.SNOW, Blocks.SNOW_BLOCK, Blocks.CLAY,
                Blocks.SOUL_SAND, Blocks.SOUL_SOIL);
        tag(ChainsawItem.MINEABLE).addTag(BlockTags.MINEABLE_WITH_AXE).addTag(BlockTags.LEAVES).add(Blocks.COBWEB);
        tag(ChainsawItem.SHEARABLE).addTag(BlockTags.LEAVES).add(Blocks.SHORT_GRASS, Blocks.FERN, Blocks.VINE,
                Blocks.DEAD_BUSH);
        tag(ElectricHoeItem.MINEABLE).add(Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.PODZOL, Blocks.ROOTED_DIRT,
                Blocks.GRASS_BLOCK, Blocks.MYCELIUM);
    }
}
