package dev.craft.data;

import java.util.concurrent.CompletableFuture;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.machine.RecyclerBlockEntity;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

public final class CraftItemTagsProvider extends ItemTagsProvider {
    public static final TagKey<Item> INGOTS_TIN = commonTag("ingots/tin");
    public static final TagKey<Item> INGOTS_BRONZE = commonTag("ingots/bronze");
    public static final TagKey<Item> INGOTS_REFINED_IRON = commonTag("ingots/refined_iron");
    public static final TagKey<Item> DUSTS_BRONZE = commonTag("dusts/bronze");
    public static final TagKey<Item> DUSTS_CLAY = commonTag("dusts/clay");
    public static final TagKey<Item> DUSTS_COAL = commonTag("dusts/coal");
    public static final TagKey<Item> DUSTS_COPPER = commonTag("dusts/copper");
    public static final TagKey<Item> DUSTS_GOLD = commonTag("dusts/gold");
    public static final TagKey<Item> DUSTS_IRON = commonTag("dusts/iron");
    public static final TagKey<Item> DUSTS_TIN = commonTag("dusts/tin");

    public CraftItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
                                 CompletableFuture<TagLookup<Block>> blockTags, ExistingFileHelper files) {
        super(output, lookup, blockTags, Craft.MODID, files);
    }

    private static TagKey<Item> commonTag(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        copy(BlockTags.LOGS_THAT_BURN, ItemTags.LOGS_THAT_BURN);
        copy(BlockTags.LEAVES, ItemTags.LEAVES);
        // The rubber sapling stays out of the item tag minecraft:saplings: IC2 did not register it as "treeSapling",
        // and the tag would give it the vanilla burn time of 100 instead of 80.
        copy(CraftBlockTagsProvider.ORES_TIN, commonTag("ores/tin"));
        copy(Tags.Blocks.ORES, Tags.Items.ORES);
        copy(Tags.Blocks.ORES_IN_GROUND_STONE, Tags.Items.ORES_IN_GROUND_STONE);
        copy(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE, Tags.Items.ORES_IN_GROUND_DEEPSLATE);
        copy(CraftBlockTagsProvider.STORAGE_BLOCKS_TIN, commonTag("storage_blocks/tin"));
        copy(CraftBlockTagsProvider.STORAGE_BLOCKS_BRONZE, commonTag("storage_blocks/bronze"));
        copy(Tags.Blocks.STORAGE_BLOCKS, Tags.Items.STORAGE_BLOCKS);

        tag(INGOTS_TIN).add(Ic2Items.TIN_INGOT.get());
        tag(INGOTS_BRONZE).add(Ic2Items.BRONZE_INGOT.get());
        tag(INGOTS_REFINED_IRON).add(Ic2Items.REFINED_IRON_INGOT.get());
        tag(Tags.Items.INGOTS).addTag(INGOTS_TIN).addTag(INGOTS_BRONZE).addTag(INGOTS_REFINED_IRON);
        tag(DUSTS_BRONZE).add(Ic2Items.BRONZE_DUST.get());
        tag(DUSTS_CLAY).add(Ic2Items.CLAY_DUST.get());
        tag(DUSTS_COAL).add(Ic2Items.COAL_DUST.get());
        tag(DUSTS_COPPER).add(Ic2Items.COPPER_DUST.get());
        tag(DUSTS_GOLD).add(Ic2Items.GOLD_DUST.get());
        tag(DUSTS_IRON).add(Ic2Items.IRON_DUST.get());
        tag(DUSTS_TIN).add(Ic2Items.TIN_DUST.get());
        tag(Tags.Items.DUSTS).addTag(DUSTS_BRONZE).addTag(DUSTS_CLAY).addTag(DUSTS_COAL).addTag(DUSTS_COPPER)
                .addTag(DUSTS_GOLD).addTag(DUSTS_IRON).addTag(DUSTS_TIN);

        // IC2 default blacklist (glass pane, stick, snowball) + pack config "102, 280, 332, 4081"; scaffold later.
        tag(RecyclerBlockEntity.BLACKLIST).add(Items.GLASS_PANE, Items.STICK, Items.SNOWBALL);
    }
}
