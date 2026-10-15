package dev.craft.ic2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import dev.craft.Craft;
import dev.craft.ic2.cable.CableType;
import dev.craft.ic2.item.BatteryItem;
import dev.craft.ic2.item.EmptyCellItem;
import dev.craft.ic2.item.SingleUseBatteryItem;
import dev.craft.ic2.item.TreetapItem;
import dev.craft.ic2.item.UpgradeModuleItem;
import dev.craft.ic2.item.WrenchItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class Ic2Items {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Craft.MODID);

    private static final Map<CableType, DeferredItem<BlockItem>> CABLES = new EnumMap<>(CableType.class);
    private static final Map<Ic2Machine, DeferredItem<BlockItem>> MACHINES = new EnumMap<>(Ic2Machine.class);
    /** Items with a plain generated item model (one texture named like the item). */
    private static final List<DeferredItem<? extends Item>> SIMPLE = new ArrayList<>();

    static {
        for (CableType type : CableType.values()) {
            CABLES.put(type, ITEMS.registerSimpleBlockItem(Ic2Blocks.cable(type)));
        }
        for (Ic2Machine machine : Ic2Machine.values()) {
            MACHINES.put(machine, ITEMS.registerSimpleBlockItem(Ic2Blocks.machine(machine)));
        }
    }

    // --- block items ---
    public static final DeferredItem<BlockItem> MACHINE_CASING = ITEMS.registerSimpleBlockItem(Ic2Blocks.MACHINE_CASING);
    public static final DeferredItem<BlockItem> TIN_ORE = ITEMS.registerSimpleBlockItem(Ic2Blocks.TIN_ORE);
    public static final DeferredItem<BlockItem> DEEPSLATE_TIN_ORE =
            ITEMS.registerSimpleBlockItem(Ic2Blocks.DEEPSLATE_TIN_ORE);
    public static final DeferredItem<BlockItem> TIN_BLOCK = ITEMS.registerSimpleBlockItem(Ic2Blocks.TIN_BLOCK);
    public static final DeferredItem<BlockItem> BRONZE_BLOCK = ITEMS.registerSimpleBlockItem(Ic2Blocks.BRONZE_BLOCK);
    public static final DeferredItem<BlockItem> RUBBER_WOOD = ITEMS.registerSimpleBlockItem(Ic2Blocks.RUBBER_WOOD);
    public static final DeferredItem<BlockItem> RUBBER_LEAVES = ITEMS.registerSimpleBlockItem(Ic2Blocks.RUBBER_LEAVES);
    public static final DeferredItem<BlockItem> RUBBER_SAPLING =
            ITEMS.registerSimpleBlockItem(Ic2Blocks.RUBBER_SAPLING);

    // --- materials ---
    public static final DeferredItem<Item> RESIN = simple("resin");
    public static final DeferredItem<Item> RUBBER = simple("rubber");
    public static final DeferredItem<Item> BRONZE_DUST = simple("bronze_dust");
    public static final DeferredItem<Item> CLAY_DUST = simple("clay_dust");
    public static final DeferredItem<Item> COAL_DUST = simple("coal_dust");
    public static final DeferredItem<Item> COPPER_DUST = simple("copper_dust");
    public static final DeferredItem<Item> GOLD_DUST = simple("gold_dust");
    public static final DeferredItem<Item> IRON_DUST = simple("iron_dust");
    public static final DeferredItem<Item> SMALL_IRON_DUST = simple("small_iron_dust");
    public static final DeferredItem<Item> TIN_DUST = simple("tin_dust");
    public static final DeferredItem<Item> HYDRATED_COAL_DUST = simple("hydrated_coal_dust");
    public static final DeferredItem<Item> REFINED_IRON_INGOT = simple("refined_iron_ingot");
    public static final DeferredItem<Item> TIN_INGOT = simple("tin_ingot");
    public static final DeferredItem<Item> BRONZE_INGOT = simple("bronze_ingot");
    public static final DeferredItem<Item> ELECTRONIC_CIRCUIT = simple("electronic_circuit");
    public static final DeferredItem<Item> ADVANCED_CIRCUIT =
            simple("advanced_circuit", new Item.Properties().rarity(Rarity.UNCOMMON));
    public static final DeferredItem<Item> COAL_BALL = simple("coal_ball");
    public static final DeferredItem<Item> COMPRESSED_COAL_BALL = simple("compressed_coal_ball");
    public static final DeferredItem<Item> COAL_CHUNK = simple("coal_chunk");
    public static final DeferredItem<Item> INDUSTRIAL_DIAMOND = simple("industrial_diamond");
    public static final DeferredItem<Item> SCRAP = simple("scrap");
    public static final DeferredItem<Item> SCRAP_BOX = simple("scrap_box");
    public static final DeferredItem<Item> HYDRATED_COAL_CLUMP = simple("hydrated_coal_clump");
    public static final DeferredItem<Item> PLANT_BALL = simple("plant_ball");
    public static final DeferredItem<Item> COMPRESSED_PLANT_BALL = simple("compressed_plant_ball");

    // --- cells ---
    public static final DeferredItem<EmptyCellItem> EMPTY_CELL =
            simple(ITEMS.registerItem("empty_cell", EmptyCellItem::new));
    public static final DeferredItem<Item> LAVA_CELL = simple("lava_cell");
    public static final DeferredItem<Item> WATER_CELL = simple("water_cell");
    public static final DeferredItem<Item> AIR_CELL = simple("air_cell");
    /** 10k coolant cell; only an ingredient until the reactor (phase 7) gives it its heat logic. */
    public static final DeferredItem<Item> COOLANT_CELL_10K =
            simple("coolant_cell_10k", new Item.Properties().stacksTo(1));

    // --- upgrades ---
    public static final DeferredItem<UpgradeModuleItem> OVERCLOCKER_UPGRADE =
            upgrade("overclocker_upgrade", UpgradeModuleItem.Type.OVERCLOCKER);
    public static final DeferredItem<UpgradeModuleItem> TRANSFORMER_UPGRADE =
            upgrade("transformer_upgrade", UpgradeModuleItem.Type.TRANSFORMER);
    public static final DeferredItem<UpgradeModuleItem> ENERGY_STORAGE_UPGRADE =
            upgrade("energy_storage_upgrade", UpgradeModuleItem.Type.ENERGY_STORAGE);

    // --- batteries ---
    public static final DeferredItem<BatteryItem> RE_BATTERY = ITEMS.registerItem("re_battery",
            properties -> new BatteryItem(properties, 10000, 100, 1, true));
    public static final DeferredItem<BatteryItem> ENERGY_CRYSTAL = ITEMS.registerItem("energy_crystal",
            properties -> new BatteryItem(properties, 100000, 250, 2, false));
    public static final DeferredItem<BatteryItem> LAPOTRON_CRYSTAL = ITEMS.registerItem("lapotron_crystal",
            properties -> new BatteryItem(properties.rarity(Rarity.UNCOMMON), 1000000, 600, 3, false));
    public static final DeferredItem<SingleUseBatteryItem> SU_BATTERY =
            simple(ITEMS.registerItem("su_battery", SingleUseBatteryItem::new));

    // --- tools ---
    public static final DeferredItem<TreetapItem> TREETAP = simple(ITEMS.registerItem("treetap", TreetapItem::new));
    public static final DeferredItem<WrenchItem> WRENCH = simple(ITEMS.registerItem("wrench", WrenchItem::new));

    private Ic2Items() {
    }

    private static DeferredItem<Item> simple(String name) {
        return simple(name, new Item.Properties());
    }

    private static DeferredItem<Item> simple(String name, Item.Properties properties) {
        return simple(ITEMS.registerSimpleItem(name, properties));
    }

    private static <T extends Item> DeferredItem<T> simple(DeferredItem<T> item) {
        SIMPLE.add(item);
        return item;
    }

    private static DeferredItem<UpgradeModuleItem> upgrade(String name, UpgradeModuleItem.Type type) {
        return simple(ITEMS.registerItem(name, properties -> new UpgradeModuleItem(properties, type)));
    }

    public static DeferredItem<BlockItem> cable(CableType type) {
        return CABLES.get(type);
    }

    public static DeferredItem<BlockItem> machine(Ic2Machine machine) {
        return MACHINES.get(machine);
    }

    /** Items whose model is a plain generated model with the texture {@code item/<name>}. */
    public static List<DeferredItem<? extends Item>> simpleItems() {
        return Collections.unmodifiableList(SIMPLE);
    }
}
