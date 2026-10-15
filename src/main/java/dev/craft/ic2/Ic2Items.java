package dev.craft.ic2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

import dev.craft.Craft;
import dev.craft.ic2.cable.CableType;
import dev.craft.ic2.item.BatteryItem;
import dev.craft.ic2.item.ElectricWrenchItem;
import dev.craft.ic2.item.EmptyCellItem;
import dev.craft.ic2.item.ResinItem;
import dev.craft.ic2.item.ScrapBoxItem;
import dev.craft.ic2.item.SingleUseBatteryItem;
import dev.craft.ic2.item.TreetapItem;
import dev.craft.ic2.item.UpgradeModuleItem;
import dev.craft.ic2.item.WrenchItem;
import dev.craft.ic2.item.armor.ElectricJetpackItem;
import dev.craft.ic2.item.armor.EnergyPackItem;
import dev.craft.ic2.item.armor.HazmatArmorItem;
import dev.craft.ic2.item.armor.Ic2ArmorMaterials;
import dev.craft.ic2.item.armor.JetpackItem;
import dev.craft.ic2.item.armor.NanoArmorItem;
import dev.craft.ic2.item.armor.NightvisionGogglesItem;
import dev.craft.ic2.item.armor.QuantumArmorItem;
import dev.craft.ic2.item.armor.SolarHelmetItem;
import dev.craft.ic2.item.armor.StaticBootsItem;
import dev.craft.ic2.item.tool.ChainsawItem;
import dev.craft.ic2.item.tool.DrillItem;
import dev.craft.ic2.item.tool.ElectricHoeItem;
import dev.craft.ic2.item.tool.ElectricTreetapItem;
import dev.craft.ic2.item.tool.MiningLaserItem;
import dev.craft.ic2.item.tool.NanoSaberItem;
import dev.craft.ic2.item.tool.ScannerItem;
import net.minecraft.world.item.ArmorItem;
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
    private static final List<DeferredItem<? extends Item>> ARMOR = new ArrayList<>();

    static {
        for (CableType type : CableType.values()) {
            CABLES.put(type, ITEMS.registerSimpleBlockItem(Ic2Blocks.cable(type)));
        }
        for (Ic2Machine machine : Ic2Machine.values()) {
            MACHINES.put(machine, ITEMS.registerSimpleBlockItem(Ic2Blocks.machine(machine),
                    new Item.Properties().rarity(machine.getRarity())));
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
    public static final DeferredItem<ResinItem> RESIN = simple(ITEMS.registerItem("resin", ResinItem::new));
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
    public static final DeferredItem<ScrapBoxItem> SCRAP_BOX = simple(ITEMS.registerItem("scrap_box", ScrapBoxItem::new));
    public static final DeferredItem<Item> HYDRATED_COAL_CLUMP = simple("hydrated_coal_clump");
    public static final DeferredItem<Item> PLANT_BALL = simple("plant_ball");
    public static final DeferredItem<Item> COMPRESSED_PLANT_BALL = simple("compressed_plant_ball");
    public static final DeferredItem<Item> CARBON_FIBER = simple("carbon_fiber");
    public static final DeferredItem<Item> CARBON_MESH = simple("carbon_mesh");
    public static final DeferredItem<Item> CARBON_PLATE = simple("carbon_plate");
    /** Empty fuel can; filling (and the filled can) follows with the canning machine. */
    public static final DeferredItem<Item> EMPTY_FUEL_CAN = simple("empty_fuel_can");

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

    // --- electric tools (shown in the creative tab charged and empty) ---
    public static final DeferredItem<DrillItem> MINING_DRILL = ITEMS.registerItem("mining_drill",
            properties -> new DrillItem(properties, 50, 8.0F));
    public static final DeferredItem<DrillItem> DIAMOND_DRILL = ITEMS.registerItem("diamond_drill",
            properties -> new DrillItem(properties, 80, 16.0F));
    public static final DeferredItem<ChainsawItem> CHAINSAW = ITEMS.registerItem("chainsaw", ChainsawItem::new);
    public static final DeferredItem<ElectricHoeItem> ELECTRIC_HOE =
            ITEMS.registerItem("electric_hoe", ElectricHoeItem::new);
    public static final DeferredItem<ElectricTreetapItem> ELECTRIC_TREETAP =
            ITEMS.registerItem("electric_treetap", ElectricTreetapItem::new);
    public static final DeferredItem<ElectricWrenchItem> ELECTRIC_WRENCH =
            ITEMS.registerItem("electric_wrench", ElectricWrenchItem::new);
    public static final DeferredItem<MiningLaserItem> MINING_LASER = ITEMS.registerItem("mining_laser",
            properties -> new MiningLaserItem(properties.rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<NanoSaberItem> NANO_SABER = ITEMS.registerItem("nano_saber",
            properties -> new NanoSaberItem(properties.rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<ScannerItem> OD_SCANNER = ITEMS.registerItem("od_scanner",
            properties -> new ScannerItem(properties, false));
    public static final DeferredItem<ScannerItem> OV_SCANNER = ITEMS.registerItem("ov_scanner",
            properties -> new ScannerItem(properties, true));

    // --- armor (plain generated model each) ---
    public static final DeferredItem<HazmatArmorItem> HAZMAT_HELMET =
            armor("hazmat_helmet", properties -> new HazmatArmorItem(ArmorItem.Type.HELMET, properties));
    public static final DeferredItem<HazmatArmorItem> HAZMAT_CHESTPLATE =
            armor("hazmat_chestplate", properties -> new HazmatArmorItem(ArmorItem.Type.CHESTPLATE, properties));
    public static final DeferredItem<HazmatArmorItem> HAZMAT_LEGGINGS =
            armor("hazmat_leggings", properties -> new HazmatArmorItem(ArmorItem.Type.LEGGINGS, properties));
    public static final DeferredItem<HazmatArmorItem> RUBBER_BOOTS =
            armor("rubber_boots", properties -> new HazmatArmorItem(ArmorItem.Type.BOOTS, properties));
    public static final DeferredItem<NanoArmorItem> NANO_HELMET = nano("nano_helmet", ArmorItem.Type.HELMET);
    public static final DeferredItem<NanoArmorItem> NANO_CHESTPLATE =
            nano("nano_chestplate", ArmorItem.Type.CHESTPLATE);
    public static final DeferredItem<NanoArmorItem> NANO_LEGGINGS = nano("nano_leggings", ArmorItem.Type.LEGGINGS);
    public static final DeferredItem<NanoArmorItem> NANO_BOOTS = nano("nano_boots", ArmorItem.Type.BOOTS);
    public static final DeferredItem<QuantumArmorItem> QUANTUM_HELMET =
            quantum("quantum_helmet", ArmorItem.Type.HELMET);
    public static final DeferredItem<QuantumArmorItem> QUANTUM_CHESTPLATE =
            quantum("quantum_chestplate", ArmorItem.Type.CHESTPLATE);
    public static final DeferredItem<QuantumArmorItem> QUANTUM_LEGGINGS =
            quantum("quantum_leggings", ArmorItem.Type.LEGGINGS);
    public static final DeferredItem<QuantumArmorItem> QUANTUM_BOOTS = quantum("quantum_boots", ArmorItem.Type.BOOTS);
    public static final DeferredItem<JetpackItem> JETPACK = armor("jetpack", JetpackItem::new);
    public static final DeferredItem<ElectricJetpackItem> ELECTRIC_JETPACK =
            armor("electric_jetpack", ElectricJetpackItem::new);
    public static final DeferredItem<EnergyPackItem> BATPACK = armor("batpack",
            properties -> new EnergyPackItem(Ic2ArmorMaterials.BATPACK, properties, 60000, 100, 1));
    public static final DeferredItem<EnergyPackItem> LAPPACK = armor("lappack",
            properties -> new EnergyPackItem(Ic2ArmorMaterials.LAPPACK, properties.rarity(Rarity.UNCOMMON), 300000,
                    250, 2));
    public static final DeferredItem<SolarHelmetItem> SOLAR_HELMET = armor("solar_helmet", SolarHelmetItem::new);
    public static final DeferredItem<StaticBootsItem> STATIC_BOOTS = armor("static_boots", StaticBootsItem::new);
    public static final DeferredItem<NightvisionGogglesItem> NIGHTVISION_GOGGLES =
            armor("nightvision_goggles", NightvisionGogglesItem::new);

    private Ic2Items() {
    }

    private static <T extends Item> DeferredItem<T> armor(String name,
                                                          Function<Item.Properties, ? extends T> factory) {
        DeferredItem<T> item = ITEMS.registerItem(name, factory);
        ARMOR.add(item);
        return item;
    }

    private static DeferredItem<NanoArmorItem> nano(String name, ArmorItem.Type type) {
        return armor(name, properties -> new NanoArmorItem(type, properties.rarity(Rarity.UNCOMMON)));
    }

    private static DeferredItem<QuantumArmorItem> quantum(String name, ArmorItem.Type type) {
        return armor(name, properties -> new QuantumArmorItem(type, properties.rarity(Rarity.RARE)));
    }

    /** IC2 armor items, each with a plain generated model {@code item/<name>}. */
    public static List<DeferredItem<? extends Item>> armorItems() {
        return Collections.unmodifiableList(ARMOR);
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
