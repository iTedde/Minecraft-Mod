package dev.craft.data;

import java.util.concurrent.CompletableFuture;

import dev.craft.Craft;
import dev.craft.gregtech.GtItems;
import dev.craft.gregtech.world.GtOre;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.cable.CableType;
import dev.craft.ic2.item.ToolboxItem;
import dev.craft.ic2.item.reactor.GradualItem;
import dev.craft.powerplants.PpItems;
import dev.craft.ic2.block.IronFenceBlock;
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
import net.neoforged.neoforge.registries.DeferredHolder;

public final class CraftItemTagsProvider extends ItemTagsProvider {
    public static final TagKey<Item> INGOTS_TIN = commonTag("ingots/tin");
    public static final TagKey<Item> INGOTS_URANIUM = commonTag("ingots/uranium");
    public static final TagKey<Item> INGOTS_BRONZE = commonTag("ingots/bronze");
    public static final TagKey<Item> INGOTS_REFINED_IRON = commonTag("ingots/refined_iron");
    public static final TagKey<Item> INGOTS_NICKEL = commonTag("ingots/nickel");
    public static final TagKey<Item> INGOTS_ALUMINIUM = commonTag("ingots/aluminium");
    public static final TagKey<Item> INGOTS_STEEL = commonTag("ingots/steel");
    public static final TagKey<Item> INGOTS_LEAD = commonTag("ingots/lead");
    public static final TagKey<Item> DUSTS_BRONZE = commonTag("dusts/bronze");
    public static final TagKey<Item> DUSTS_CLAY = commonTag("dusts/clay");
    public static final TagKey<Item> DUSTS_COAL = commonTag("dusts/coal");
    public static final TagKey<Item> COAL_COKE = commonTag("coal_coke");
    public static final TagKey<Item> DUSTS_COPPER = commonTag("dusts/copper");
    public static final TagKey<Item> DUSTS_GOLD = commonTag("dusts/gold");
    public static final TagKey<Item> DUSTS_IRON = commonTag("dusts/iron");
    public static final TagKey<Item> DUSTS_TIN = commonTag("dusts/tin");
    public static final TagKey<Item> DUSTS_SILVER = commonTag("dusts/silver");
    public static final TagKey<Item> PLATES_IRON = commonTag("plates/iron");
    public static final TagKey<Item> PLATES_GOLD = commonTag("plates/gold");
    public static final TagKey<Item> PLATES_REFINED_IRON = commonTag("plates/refined_iron");
    public static final TagKey<Item> PLATES_TIN = commonTag("plates/tin");
    public static final TagKey<Item> PLATES_COPPER = commonTag("plates/copper");
    public static final TagKey<Item> PLATES_BRONZE = commonTag("plates/bronze");
    public static final TagKey<Item> PLATES_NICKEL = commonTag("plates/nickel");
    public static final TagKey<Item> PLATES_ALUMINIUM = commonTag("plates/aluminium");
    public static final TagKey<Item> PLATES_STEEL = commonTag("plates/steel");
    public static final TagKey<Item> PLATES_SILICON = commonTag("plates/silicon");
    public static final TagKey<Item> PLATES_LEAD = commonTag("plates/lead");
    public static final TagKey<Item> DUSTS_NICKEL = commonTag("dusts/nickel");
    public static final TagKey<Item> DUSTS_ALUMINIUM = commonTag("dusts/aluminium");
    public static final TagKey<Item> DUSTS_STEEL = commonTag("dusts/steel");
    public static final TagKey<Item> DUSTS_LEAD = commonTag("dusts/lead");
    public static final TagKey<Item> INGOTS_PLATINUM = commonTag("ingots/platinum");
    public static final TagKey<Item> DUSTS_PLATINUM = commonTag("dusts/platinum");
    public static final TagKey<Item> DUSTS_ZINC = commonTag("dusts/zinc");
    public static final TagKey<Item> DUSTS_CALCITE = commonTag("dusts/calcite");
    public static final TagKey<Item> INGOTS_SILVER = commonTag("ingots/silver");
    public static final TagKey<Item> INGOTS_ZINC = commonTag("ingots/zinc");
    public static final TagKey<Item> INGOTS_TITANIUM = commonTag("ingots/titanium");
    public static final TagKey<Item> INGOTS_CHROME = commonTag("ingots/chrome");
    public static final TagKey<Item> PLATES_SILVER = commonTag("plates/silver");
    public static final TagKey<Item> PLATES_ZINC = commonTag("plates/zinc");
    public static final TagKey<Item> PLATES_TITANIUM = commonTag("plates/titanium");
    public static final TagKey<Item> PLATES_CHROME = commonTag("plates/chrome");
    public static final TagKey<Item> PLATES_PLATINUM = commonTag("plates/platinum");
    public static final TagKey<Item> PLATES_MAGNALIUM = commonTag("plates/magnalium");
    public static final TagKey<Item> INGOTS_TUNGSTEN = commonTag("ingots/tungsten");
    public static final TagKey<Item> INGOTS_TUNGSTENSTEEL = commonTag("ingots/tungstensteel");
    public static final TagKey<Item> PLATES_TUNGSTEN = commonTag("plates/tungsten");
    public static final TagKey<Item> PLATES_TUNGSTENSTEEL = commonTag("plates/tungstensteel");
    public static final TagKey<Item> PLATES_ELECTRUM = commonTag("plates/electrum");
    public static final TagKey<Item> INGOTS_ELECTRUM = commonTag("ingots/electrum");
    public static final TagKey<Item> PLATES_BRASS = commonTag("plates/brass");
    public static final TagKey<Item> INGOTS_BRASS = commonTag("ingots/brass");
    public static final TagKey<Item> DUSTS_BRASS = commonTag("dusts/brass");
    public static final TagKey<Item> DUSTS_TUNGSTEN = commonTag("dusts/tungsten");
    public static final TagKey<Item> STORAGE_BLOCKS_STEEL = commonTag("storage_blocks/steel");
    public static final TagKey<Item> DUSTS_MAGNESIUM = commonTag("dusts/magnesium");
    public static final TagKey<Item> DUSTS_MANGANESE = commonTag("dusts/manganese");
    public static final TagKey<Item> DUSTS_TITANIUM = commonTag("dusts/titanium");
    public static final TagKey<Item> DUSTS_CHROME = commonTag("dusts/chrome");
    public static final TagKey<Item> GEMS_RUBY = commonTag("gems/ruby");
    public static final TagKey<Item> GEMS_SAPPHIRE = commonTag("gems/sapphire");
    public static final TagKey<Item> GEMS_GREEN_SAPPHIRE = commonTag("gems/green_sapphire");
    public static final TagKey<Item> GEMS_OLIVINE = commonTag("gems/olivine");
    public static final TagKey<Item> GEMS_RED_GARNET = commonTag("gems/red_garnet");
    public static final TagKey<Item> GEMS_YELLOW_GARNET = commonTag("gems/yellow_garnet");
    /** GT {@code craftingRawMachineTier01}: IC2 machine casing, aluminium and bronze machine hull. */
    public static final TagKey<Item> RAW_MACHINE_TIER_01 = craftTag("raw_machine_tier_01");
    /** GT {@code craftingRawMachineTier02}: IC2 advanced machine casing, steel machine hull. */
    public static final TagKey<Item> RAW_MACHINE_TIER_02 = craftTag("raw_machine_tier_02");
    /** GT {@code crafting100kEUStore}: energy crystal, lithium battery. */
    public static final TagKey<Item> CRAFTING_100K_EU_STORE = craftTag("crafting_100k_eu_store");
    /** GT {@code itemLazurite}: lapis lazuli ({@code dustLapis}) and lazurite dust. */
    public static final TagKey<Item> ITEM_LAZURITE = craftTag("item_lazurite");
    /** GT {@code chunkLazurite}: lapis block and chunk of lazurite. */
    public static final TagKey<Item> CHUNK_LAZURITE = craftTag("chunk_lazurite");
    /** GT {@code craftingGrinder}: diamond and wolframium grinding heads. */
    public static final TagKey<Item> CRAFTING_GRINDER = craftTag("crafting_grinder");

    public CraftItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
                                 CompletableFuture<TagLookup<Block>> blockTags, ExistingFileHelper files) {
        super(output, lookup, blockTags, Craft.MODID, files);
    }

    private static TagKey<Item> craftTag(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath(Craft.MODID, path));
    }

    private static TagKey<Item> commonTag(String path) {
        return TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("c", path));
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        copy(BlockTags.LOGS_THAT_BURN, ItemTags.LOGS_THAT_BURN);
        copy(BlockTags.LEAVES, ItemTags.LEAVES);
        copy(BlockTags.DOORS, ItemTags.DOORS);
        // IC2 IBoxable: cables and the SU battery go into the toolbox despite their stack size; ItemGradual items
        // (except the hydration cell), the fuel can and the toolbox itself never do.
        for (CableType type : CableType.values()) {
            tag(ToolboxItem.BOXABLE).add(Ic2Items.cable(type).get());
        }
        tag(ToolboxItem.BOXABLE).add(Ic2Items.SU_BATTERY.get(), Ic2Items.RE_BATTERY.get(), Ic2Items.MINING_PIPE.get(),
                Ic2Items.WOODEN_SCAFFOLD.get(), Ic2Items.FERTILIZER.get(), Ic2Items.DYNAMITE.get(),
                Ic2Items.STICKY_DYNAMITE.get());
        tag(ToolboxItem.NOT_BOXABLE).add(Ic2Items.TOOLBOX.get(), Ic2Items.FILLED_FUEL_CAN.get());
        Ic2Items.ITEMS.getEntries().stream().map(DeferredHolder::get).filter(GradualItem.class::isInstance)
                .filter(item -> item != Ic2Items.HYDRATING_CELL.get())
                .forEach(item -> tag(ToolboxItem.NOT_BOXABLE).add(item));
        // The rubber sapling stays out of the item tag minecraft:saplings: IC2 did not register it as "treeSapling",
        // and the tag would give it the vanilla burn time of 100 instead of 80.
        copy(CraftBlockTagsProvider.ORES_TIN, commonTag("ores/tin"));
        copy(Tags.Blocks.ORES, Tags.Items.ORES);
        copy(Tags.Blocks.ORES_IN_GROUND_STONE, Tags.Items.ORES_IN_GROUND_STONE);
        copy(Tags.Blocks.ORES_IN_GROUND_DEEPSLATE, Tags.Items.ORES_IN_GROUND_DEEPSLATE);
        copy(CraftBlockTagsProvider.STORAGE_BLOCKS_TIN, commonTag("storage_blocks/tin"));
        copy(CraftBlockTagsProvider.ORES_URANIUM, commonTag("ores/uranium"));
        copy(Tags.Blocks.ORES_IN_GROUND_NETHERRACK, Tags.Items.ORES_IN_GROUND_NETHERRACK);
        for (GtOre ore : GtOre.values()) {
            copy(TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "ores/" + ore.getMaterial())),
                    commonTag("ores/" + ore.getMaterial()));
        }
        copy(CraftBlockTagsProvider.STORAGE_BLOCKS_URANIUM, commonTag("storage_blocks/uranium"));
        copy(CraftBlockTagsProvider.STORAGE_BLOCKS_BRONZE, commonTag("storage_blocks/bronze"));
        copy(CraftBlockTagsProvider.STORAGE_BLOCKS_STEEL, STORAGE_BLOCKS_STEEL);
        copy(Tags.Blocks.STORAGE_BLOCKS, Tags.Items.STORAGE_BLOCKS);

        tag(INGOTS_TIN).add(Ic2Items.TIN_INGOT.get());
        tag(INGOTS_URANIUM).add(Ic2Items.URANIUM_INGOT.get());
        tag(INGOTS_BRONZE).add(Ic2Items.BRONZE_INGOT.get());
        tag(INGOTS_REFINED_IRON).add(Ic2Items.REFINED_IRON_INGOT.get());
        tag(PLATES_IRON).add(GtItems.IRON_PLATE.get());
        tag(PLATES_GOLD).add(GtItems.GOLD_PLATE.get());
        tag(PLATES_REFINED_IRON).add(GtItems.REFINED_IRON_PLATE.get());
        tag(PLATES_TIN).add(GtItems.TIN_PLATE.get());
        tag(PLATES_COPPER).add(GtItems.COPPER_PLATE.get());
        tag(PLATES_BRONZE).add(GtItems.BRONZE_PLATE.get());
        tag(PLATES_NICKEL).add(GtItems.NICKEL_PLATE.get());
        tag(PLATES_ALUMINIUM).add(GtItems.ALUMINIUM_PLATE.get());
        tag(PLATES_STEEL).add(GtItems.STEEL_PLATE.get());
        tag(PLATES_SILICON).add(GtItems.SILICON_PLATE.get());
        tag(PLATES_LEAD).add(GtItems.LEAD_PLATE.get());
        tag(INGOTS_NICKEL).add(GtItems.NICKEL_INGOT.get());
        tag(INGOTS_ALUMINIUM).add(GtItems.ALUMINIUM_INGOT.get());
        tag(INGOTS_STEEL).add(GtItems.STEEL_INGOT.get());
        tag(INGOTS_LEAD).add(GtItems.LEAD_INGOT.get());
        tag(DUSTS_NICKEL).add(GtItems.NICKEL_DUST.get());
        tag(DUSTS_ALUMINIUM).add(GtItems.ALUMINIUM_DUST.get());
        tag(DUSTS_STEEL).add(GtItems.STEEL_DUST.get());
        tag(DUSTS_LEAD).add(GtItems.LEAD_DUST.get());
        tag(INGOTS_PLATINUM).add(GtItems.PLATINUM_INGOT.get());
        tag(DUSTS_PLATINUM).add(GtItems.PLATINUM_DUST.get());
        tag(DUSTS_ZINC).add(GtItems.ZINC_DUST.get());
        tag(DUSTS_CALCITE).add(GtItems.CALCITE_DUST.get());
        tag(INGOTS_SILVER).add(GtItems.SILVER_INGOT.get());
        tag(INGOTS_ZINC).add(GtItems.ZINC_INGOT.get());
        tag(INGOTS_TITANIUM).add(GtItems.TITANIUM_INGOT.get());
        tag(INGOTS_CHROME).add(GtItems.CHROME_INGOT.get());
        tag(PLATES_SILVER).add(GtItems.SILVER_PLATE.get());
        tag(PLATES_ZINC).add(GtItems.ZINC_PLATE.get());
        tag(PLATES_TITANIUM).add(GtItems.TITANIUM_PLATE.get());
        tag(PLATES_CHROME).add(GtItems.CHROME_PLATE.get());
        tag(PLATES_PLATINUM).add(GtItems.PLATINUM_PLATE.get());
        tag(PLATES_MAGNALIUM).add(GtItems.MAGNALIUM_PLATE.get());
        tag(PLATES_ELECTRUM).add(GtItems.ELECTRUM_PLATE.get());
        tag(INGOTS_ELECTRUM).add(GtItems.ELECTRUM_INGOT.get());
        tag(PLATES_BRASS).add(GtItems.BRASS_PLATE.get());
        tag(INGOTS_BRASS).add(GtItems.BRASS_INGOT.get());
        tag(DUSTS_BRASS).add(GtItems.BRASS_DUST.get());
        tag(INGOTS_TUNGSTEN).add(GtItems.TUNGSTEN_INGOT.get());
        tag(INGOTS_TUNGSTENSTEEL).add(GtItems.TUNGSTENSTEEL_INGOT.get());
        tag(PLATES_TUNGSTEN).add(GtItems.TUNGSTEN_PLATE.get());
        tag(PLATES_TUNGSTENSTEEL).add(GtItems.TUNGSTENSTEEL_PLATE.get());
        tag(DUSTS_TUNGSTEN).add(GtItems.TUNGSTEN_DUST.get());
        tag(DUSTS_MAGNESIUM).add(GtItems.MAGNESIUM_DUST.get());
        tag(DUSTS_MANGANESE).add(GtItems.MANGANESE_DUST.get());
        tag(DUSTS_TITANIUM).add(GtItems.TITANIUM_DUST.get());
        tag(DUSTS_CHROME).add(GtItems.CHROME_DUST.get());
        tag(GEMS_RUBY).add(GtItems.RUBY.get());
        tag(GEMS_SAPPHIRE).add(GtItems.SAPPHIRE.get());
        tag(GEMS_GREEN_SAPPHIRE).add(GtItems.GREEN_SAPPHIRE.get());
        tag(GEMS_OLIVINE).add(GtItems.OLIVINE.get());
        tag(GEMS_RED_GARNET).add(GtItems.RED_GARNET.get());
        tag(GEMS_YELLOW_GARNET).add(GtItems.YELLOW_GARNET.get());
        tag(Tags.Items.GEMS).addTag(GEMS_RUBY).addTag(GEMS_SAPPHIRE).addTag(GEMS_GREEN_SAPPHIRE).addTag(GEMS_OLIVINE)
                .addTag(GEMS_RED_GARNET).addTag(GEMS_YELLOW_GARNET);
        tag(RAW_MACHINE_TIER_01).add(Ic2Items.MACHINE_CASING.get(), GtItems.ALUMINIUM_MACHINE_HULL.get(),
                GtItems.BRONZE_MACHINE_HULL.get());
        tag(RAW_MACHINE_TIER_02).add(Ic2Items.ADVANCED_MACHINE_CASING.get(), GtItems.STEEL_MACHINE_HULL.get());
        tag(CRAFTING_100K_EU_STORE).add(Ic2Items.ENERGY_CRYSTAL.get(), GtItems.LITHIUM_BATTERY.get());
        tag(ITEM_LAZURITE).add(Items.LAPIS_LAZULI, GtItems.LAZURITE_DUST.get());
        tag(CHUNK_LAZURITE).add(Items.LAPIS_BLOCK, GtItems.LAZURITE_CHUNK.get());
        tag(CRAFTING_GRINDER).add(GtItems.DIAMOND_GRINDER.get(), GtItems.WOLFRAMIUM_GRINDER.get());
        tag(Tags.Items.INGOTS).addTag(INGOTS_TIN).addTag(INGOTS_URANIUM).addTag(INGOTS_BRONZE).addTag(INGOTS_REFINED_IRON)
                .addTag(INGOTS_NICKEL).addTag(INGOTS_ALUMINIUM).addTag(INGOTS_STEEL).addTag(INGOTS_LEAD)
                .addTag(INGOTS_PLATINUM).addTag(INGOTS_SILVER).addTag(INGOTS_ZINC).addTag(INGOTS_TITANIUM)
                .addTag(INGOTS_CHROME).addTag(INGOTS_TUNGSTEN).addTag(INGOTS_TUNGSTENSTEEL).addTag(INGOTS_BRASS)
                .addTag(INGOTS_ELECTRUM);
        tag(DUSTS_BRONZE).add(Ic2Items.BRONZE_DUST.get());
        tag(DUSTS_CLAY).add(Ic2Items.CLAY_DUST.get());
        tag(DUSTS_COAL).add(Ic2Items.COAL_DUST.get());
        tag(COAL_COKE).add(PpItems.COKE.get());
        tag(DUSTS_COPPER).add(Ic2Items.COPPER_DUST.get());
        tag(DUSTS_GOLD).add(Ic2Items.GOLD_DUST.get());
        tag(DUSTS_IRON).add(Ic2Items.IRON_DUST.get());
        tag(DUSTS_TIN).add(Ic2Items.TIN_DUST.get());
        tag(DUSTS_SILVER).add(Ic2Items.SILVER_DUST.get());
        tag(Tags.Items.DUSTS).addTag(DUSTS_BRONZE).addTag(DUSTS_CLAY).addTag(DUSTS_COAL).addTag(DUSTS_COPPER)
                .addTag(DUSTS_GOLD).addTag(DUSTS_IRON).addTag(DUSTS_TIN).addTag(DUSTS_SILVER)
                .addTag(DUSTS_NICKEL).addTag(DUSTS_ALUMINIUM).addTag(DUSTS_STEEL).addTag(DUSTS_LEAD)
                .addTag(DUSTS_PLATINUM).addTag(DUSTS_ZINC).addTag(DUSTS_CALCITE).addTag(DUSTS_MAGNESIUM)
                .addTag(DUSTS_MANGANESE).addTag(DUSTS_TITANIUM).addTag(DUSTS_CHROME).addTag(DUSTS_TUNGSTEN)
                .addTag(DUSTS_BRASS);

        // GT wood plate is registered as plankWood.
        tag(ItemTags.PLANKS).add(GtItems.WOOD_PLATE.get());

        tag(IronFenceBlock.METAL_BOOTS).add(Items.IRON_BOOTS, Items.GOLDEN_BOOTS, Items.CHAINMAIL_BOOTS,
                Ic2Items.NANO_BOOTS.get(), Ic2Items.QUANTUM_BOOTS.get(), Ic2Items.STATIC_BOOTS.get());
        // IC2 default blacklist (glass pane, stick, snowball, wooden scaffold) + pack config "102, 280, 332, 4081",
        // then GregTech's unconditional entries (snowball, snow layer, ice, snow block).
        tag(RecyclerBlockEntity.BLACKLIST).add(Items.GLASS_PANE, Items.STICK, Items.SNOWBALL,
                Ic2Items.WOODEN_SCAFFOLD.get(), Items.SNOW, Items.ICE, Items.SNOW_BLOCK);
    }
}
