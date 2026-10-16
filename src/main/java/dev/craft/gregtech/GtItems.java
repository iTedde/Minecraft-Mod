package dev.craft.gregtech;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import dev.craft.Craft;
import dev.craft.gregtech.block.GtCasingBlock;
import dev.craft.gregtech.item.GtMachineItem;
import dev.craft.gregtech.item.GtCoolantCellItem;
import dev.craft.gregtech.item.GtMaterialItem;
import dev.craft.gregtech.item.GtNeutronReflectorItem;
import dev.craft.gregtech.item.GtRadioactiveCellItem;
import dev.craft.gregtech.item.LithiumBatpackItem;
import dev.craft.gregtech.item.LithiumBatteryItem;
import dev.craft.gregtech.world.GtOre;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class GtItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Craft.MODID);

    private static final List<DeferredItem<? extends Item>> SIMPLE = new ArrayList<>();
    private static final Map<GtMachine, DeferredItem<GtMachineItem>> MACHINES = new EnumMap<>(GtMachine.class);
    private static final Map<GtCasingBlock.Type, DeferredItem<BlockItem>> CASINGS =
            new EnumMap<>(GtCasingBlock.Type.class);
    private static final Map<GtOre, DeferredItem<BlockItem>> ORES = new EnumMap<>(GtOre.class);
    private static final Map<GtOre, DeferredItem<BlockItem>> DEEPSLATE_ORES = new EnumMap<>(GtOre.class);

    static {
        for (GtMachine machine : GtMachine.values()) {
            MACHINES.put(machine, ITEMS.registerItem(machine.getName(),
                    properties -> new GtMachineItem(GtBlocks.machine(machine).get(), properties)));
        }
        for (GtCasingBlock.Type type : GtCasingBlock.Type.values()) {
            CASINGS.put(type, ITEMS.registerSimpleBlockItem(GtBlocks.casing(type)));
        }
        for (GtOre ore : GtOre.values()) {
            ORES.put(ore, ITEMS.registerSimpleBlockItem(GtBlocks.ore(ore)));
            if (ore.hasDeepslateVariant()) {
                DEEPSLATE_ORES.put(ore, ITEMS.registerSimpleBlockItem(GtBlocks.deepslateOre(ore)));
            }
        }
    }

    // --- plates (GT_MetaItem_Material 64–84) ---
    public static final DeferredItem<GtMaterialItem> IRON_PLATE = material("iron_plate", "Fe");
    public static final DeferredItem<GtMaterialItem> GOLD_PLATE = material("gold_plate", "Au");
    public static final DeferredItem<GtMaterialItem> REFINED_IRON_PLATE = material("refined_iron_plate", "Fe");
    public static final DeferredItem<GtMaterialItem> TIN_PLATE = material("tin_plate", "Sn");
    public static final DeferredItem<GtMaterialItem> COPPER_PLATE = material("copper_plate", "Cu");
    public static final DeferredItem<GtMaterialItem> BRONZE_PLATE = material("bronze_plate", "SnCu3");
    public static final DeferredItem<GtMaterialItem> NICKEL_PLATE = material("nickel_plate", "Ni");
    public static final DeferredItem<GtMaterialItem> ALUMINIUM_PLATE = material("aluminium_plate", "Al");
    public static final DeferredItem<GtMaterialItem> STEEL_PLATE = material("steel_plate", "Fe");

    // --- ingots and other materials (GT_MetaItem_Material) ---
    public static final DeferredItem<GtMaterialItem> ALUMINIUM_INGOT = material("aluminium_ingot", "Al");
    public static final DeferredItem<GtMaterialItem> STEEL_INGOT = material("steel_ingot", "Fe");
    public static final DeferredItem<GtMaterialItem> NICKEL_INGOT = material("nickel_ingot", "Ni");
    public static final DeferredItem<GtMaterialItem> SILICON_PLATE = material("silicon_plate", "Si2");
    public static final DeferredItem<GtMaterialItem> IRIDIUM_INGOT = material("iridium_ingot", "Ir");
    public static final DeferredItem<GtMaterialItem> ELECTRUM_INGOT = material("electrum_ingot", "AgAu");
    /** Material 4: no formula line. */
    public static final DeferredItem<GtMaterialItem> IRIDIUM_ALLOY_INGOT = material("iridium_alloy_ingot", "");

    // --- cells (GT_MetaItem_Cell) ---
    public static final DeferredItem<GtMaterialItem> HYDROGEN_CELL = material("hydrogen_cell", "H");
    public static final DeferredItem<GtMaterialItem> LITHIUM_CELL = material("lithium_cell", "Li");
    public static final DeferredItem<GtMaterialItem> SILICON_CELL = material("silicon_cell", "Si");
    public static final DeferredItem<GtMaterialItem> CARBON_CELL = material("carbon_cell", "C");
    public static final DeferredItem<GtMaterialItem> CALCIUM_CELL = material("calcium_cell", "Ca");
    public static final DeferredItem<GtMaterialItem> SODIUM_CELL = material("sodium_cell", "Na");
    public static final DeferredItem<GtMaterialItem> DEUTERIUM_CELL = material("deuterium_cell", "H-2");
    public static final DeferredItem<GtMaterialItem> TRITIUM_CELL = material("tritium_cell", "H-3");
    public static final DeferredItem<GtMaterialItem> HELIUM_CELL = material("helium_cell", "He");
    public static final DeferredItem<GtMaterialItem> HELIUM_3_CELL = material("helium_3_cell", "He-3");
    public static final DeferredItem<GtMaterialItem> METHANE_CELL = material("methane_cell", "CH4");
    public static final DeferredItem<GtMaterialItem> MERCURY_CELL = material("mercury_cell", "Hg");
    public static final DeferredItem<GtMaterialItem> OIL_CELL = material("oil_cell", "");

    // --- dusts (GT_MetaItem_Dust) ---
    public static final DeferredItem<GtMaterialItem> SULFUR_DUST = material("sulfur_dust", "S");
    public static final DeferredItem<GtMaterialItem> ALUMINIUM_DUST = material("aluminium_dust", "Al");
    public static final DeferredItem<GtMaterialItem> DARK_ASH = material("dark_ash", "C");
    public static final DeferredItem<GtMaterialItem> STEEL_DUST = material("steel_dust", "Fe");
    public static final DeferredItem<GtMaterialItem> NICKEL_DUST = material("nickel_dust", "Ni");
    public static final DeferredItem<GtMaterialItem> EMERALD_DUST = material("emerald_dust", "Be3Al2Si6O18");
    public static final DeferredItem<GtMaterialItem> LAZURITE_DUST = material("lazurite_dust", "Al6Si6Ca8Na8");
    public static final DeferredItem<GtMaterialItem> PYRITE_DUST = material("pyrite_dust", "FeS2");
    public static final DeferredItem<GtMaterialItem> SALTPETER_DUST = material("saltpeter_dust", "KNO3");
    public static final DeferredItem<GtMaterialItem> RUBY_DUST = material("ruby_dust", "Al2O6Cr");
    public static final DeferredItem<GtMaterialItem> ASH = material("ash", "C");
    public static final DeferredItem<GtMaterialItem> DIAMOND_DUST = material("diamond_dust", "C128");

    // --- small dusts (GT_MetaItem_SmallDust, same index as the dust) ---
    public static final DeferredItem<GtMaterialItem> SMALL_PYRITE_DUST = material("small_pyrite_dust", "FeS2");
    public static final DeferredItem<GtMaterialItem> SMALL_CALCITE_DUST = material("small_calcite_dust", "CaCO3");
    public static final DeferredItem<GtMaterialItem> SMALL_SODALITE_DUST = material("small_sodalite_dust",
            "Al3Si3Na4Cl");
    public static final DeferredItem<GtMaterialItem> SMALL_TUNGSTEN_DUST = material("small_tungsten_dust", "W");
    public static final DeferredItem<GtMaterialItem> SMALL_LEAD_DUST = material("small_lead_dust", "Pb");
    public static final DeferredItem<GtMaterialItem> SMALL_ZINC_DUST = material("small_zinc_dust", "Zn");
    public static final DeferredItem<GtMaterialItem> SMALL_NICKEL_DUST = material("small_nickel_dust", "Ni");
    public static final DeferredItem<GtMaterialItem> SMALL_GOLD_DUST = material("small_gold_dust", "Au");
    public static final DeferredItem<GtMaterialItem> SMALL_COPPER_DUST = material("small_copper_dust", "Cu");
    public static final DeferredItem<GtMaterialItem> SMALL_TIN_DUST = material("small_tin_dust", "Sn");

    // --- components (GT_MetaItem_Component) ---
    public static final DeferredItem<GtMaterialItem> CONVEYOR_MODULE = component("conveyor_module");
    public static final DeferredItem<GtMaterialItem> MACHINE_PARTS = component("machine_parts");
    public static final DeferredItem<GtMaterialItem> KANTHAL_HEATING_COIL = component("kanthal_heating_coil");
    public static final DeferredItem<GtMaterialItem> NICHROME_HEATING_COIL = component("nichrome_heating_coil");
    public static final DeferredItem<GtMaterialItem> CUPRONICKEL_HEATING_COIL = component("cupronickel_heating_coil");
    public static final DeferredItem<GtMaterialItem> PNEUMATIC_GENERATOR_UPGRADE =
            component("pneumatic_generator_upgrade");
    public static final DeferredItem<GtMaterialItem> LITHIUM_BATTERY_UPGRADE = component("lithium_battery_upgrade");
    public static final DeferredItem<GtMaterialItem> HV_TRANSFORMER_UPGRADE = component("hv_transformer_upgrade");
    public static final DeferredItem<GtMaterialItem> ALUMINIUM_MACHINE_HULL = component("aluminium_machine_hull");
    public static final DeferredItem<GtMaterialItem> BRONZE_MACHINE_HULL = component("bronze_machine_hull");
    public static final DeferredItem<GtMaterialItem> STEEL_MACHINE_HULL = component("steel_machine_hull");

    // --- electric items (GT items 56–58) ---
    public static final DeferredItem<LithiumBatteryItem> LITHIUM_BATTERY =
            ITEMS.registerItem("lithium_battery", LithiumBatteryItem::new);
    public static final DeferredItem<LithiumBatpackItem> LITHIUM_BATPACK =
            ITEMS.registerItem("lithium_batpack", LithiumBatpackItem::new);

    /**
     * The water block item the electrolyzer tank produces in its holo slot (1.4.7 {@code new ItemStack(Block.waterStill)});
     * not obtainable otherwise.
     */
    public static final DeferredItem<Item> WATER = ITEMS.registerSimpleItem("water");

    /** The lava block item of the centrifuge's holo slot (1.4.7 {@code new ItemStack(Block.lavaStill)}). */
    public static final DeferredItem<Item> LAVA = ITEMS.registerSimpleItem("lava");

    private GtItems() {
    }

    public static DeferredItem<GtMachineItem> machine(GtMachine machine) {
        return MACHINES.get(machine);
    }

    public static DeferredItem<BlockItem> casing(GtCasingBlock.Type type) {
        return CASINGS.get(type);
    }

    // --- nuclear (phase 7b): GT_MetaItem_Material 38/39, Dust 16/22/80/81, Cell 10, items 34–36, 40, 48–53 ---
    public static final DeferredItem<GtMaterialItem> THORIUM_INGOT = glowing("thorium_ingot", "Th");
    public static final DeferredItem<GtMaterialItem> PLUTONIUM_INGOT = glowing("plutonium_ingot", "Pu");
    public static final DeferredItem<GtMaterialItem> URANIUM_DUST = glowing("uranium_dust", "U");
    public static final DeferredItem<GtMaterialItem> THORIUM_DUST = glowing("thorium_dust", "Th");
    public static final DeferredItem<GtMaterialItem> PLUTONIUM_DUST = glowing("plutonium_dust", "Pu");
    public static final DeferredItem<GtMaterialItem> SMALL_PLUTONIUM_DUST = glowing("small_plutonium_dust", "Pu");
    public static final DeferredItem<GtMaterialItem> TUNGSTEN_DUST = material("tungsten_dust", "W");
    public static final DeferredItem<GtMaterialItem> BERYLLIUM_CELL = material("beryllium_cell", "Be");
    public static final DeferredItem<GtCoolantCellItem> HELIUM_COOLANT_CELL_60K = simple(ITEMS.registerItem(
            "helium_coolant_cell_60k", properties -> new GtCoolantCellItem(properties, 60000)));
    public static final DeferredItem<GtCoolantCellItem> HELIUM_COOLANT_CELL_180K = simple(ITEMS.registerItem(
            "helium_coolant_cell_180k", properties -> new GtCoolantCellItem(properties, 180000)));
    public static final DeferredItem<GtCoolantCellItem> HELIUM_COOLANT_CELL_360K = simple(ITEMS.registerItem(
            "helium_coolant_cell_360k", properties -> new GtCoolantCellItem(properties, 360000)));
    public static final DeferredItem<GtNeutronReflectorItem> IRIDIUM_NEUTRON_REFLECTOR = simple(ITEMS.registerItem(
            "iridium_neutron_reflector", GtNeutronReflectorItem::new));
    public static final DeferredItem<GtRadioactiveCellItem> THORIUM_CELL = radioactive("thorium_cell", 25000, 1, -5,
            false);
    public static final DeferredItem<GtRadioactiveCellItem> DUAL_THORIUM_CELL = radioactive("dual_thorium_cell", 25000,
            2, -5, false);
    public static final DeferredItem<GtRadioactiveCellItem> QUAD_THORIUM_CELL = radioactive("quad_thorium_cell", 25000,
            4, -5, false);
    public static final DeferredItem<GtRadioactiveCellItem> PLUTONIUM_CELL = radioactive("plutonium_cell", 20000, 1, 2,
            true);
    public static final DeferredItem<GtRadioactiveCellItem> DUAL_PLUTONIUM_CELL = radioactive("dual_plutonium_cell",
            20000, 2, 2, true);
    public static final DeferredItem<GtRadioactiveCellItem> QUAD_PLUTONIUM_CELL = radioactive("quad_plutonium_cell",
            20000, 4, 2, true);

    // --- GT follow-ups (phase 8a): Dust 0/5/23, SmallDust 246, Cell 13–15, Material 23/74, items 60–62 ---
    public static final DeferredItem<GtMaterialItem> ENDER_PEARL_DUST = material("ender_pearl_dust", "BeK4N5Cl6");
    public static final DeferredItem<GtMaterialItem> SODALITE_DUST = material("sodalite_dust", "Al3Si3Na4Cl");
    public static final DeferredItem<GtMaterialItem> LEAD_DUST = material("lead_dust", "Pb");
    public static final DeferredItem<GtMaterialItem> SMALL_SILVER_DUST = material("small_silver_dust", "Ag");
    public static final DeferredItem<GtMaterialItem> LEAD_INGOT = material("lead_ingot", "Pb");
    public static final DeferredItem<GtMaterialItem> LEAD_PLATE = material("lead_plate", "Pb");
    public static final DeferredItem<GtMaterialItem> CHLORITE_CELL = material("chlorite_cell", "Cl");
    public static final DeferredItem<GtMaterialItem> POTASSIUM_CELL = material("potassium_cell", "K");
    public static final DeferredItem<GtMaterialItem> NITROGEN_CELL = material("nitrogen_cell", "N");
    public static final DeferredItem<GtCoolantCellItem> NAK_COOLANT_CELL_60K = simple(ITEMS.registerItem(
            "nak_coolant_cell_60k", properties -> new GtCoolantCellItem(properties, 60000)));
    public static final DeferredItem<GtCoolantCellItem> NAK_COOLANT_CELL_180K = simple(ITEMS.registerItem(
            "nak_coolant_cell_180k", properties -> new GtCoolantCellItem(properties, 180000)));
    public static final DeferredItem<GtCoolantCellItem> NAK_COOLANT_CELL_360K = simple(ITEMS.registerItem(
            "nak_coolant_cell_360k", properties -> new GtCoolantCellItem(properties, 360000)));

    // --- GT ores (phase 8c): gems GT_MetaItem_Material 27/32–34/37/54/55, Dust 4/11/14/17/24/27/33/34/37/44/54/55, Cell 33 ---
    public static final DeferredItem<GtMaterialItem> RUBY = material("ruby", "Al2O6Cr");
    public static final DeferredItem<GtMaterialItem> SAPPHIRE = material("sapphire", "Al2O6");
    public static final DeferredItem<GtMaterialItem> GREEN_SAPPHIRE = material("green_sapphire", "Al2O6");
    public static final DeferredItem<GtMaterialItem> OLIVINE = material("olivine", "Mg2Fe2SiO4");
    public static final DeferredItem<GtMaterialItem> RED_GARNET = material("red_garnet",
            "(Al2Mg3Si3O12)3(Al2Fe3Si3O12)5(Al2Mn3Si3O12)8");
    public static final DeferredItem<GtMaterialItem> YELLOW_GARNET = material("yellow_garnet",
            "(Ca3Fe2Si3O12)5(Ca3Al2Si3O12)8(Ca3Cr2Si3O12)3");
    public static final DeferredItem<GtMaterialItem> PLATINUM_INGOT = material("platinum_ingot", "Pt");
    public static final DeferredItem<GtMaterialItem> GALENA_DUST = material("galena_dust", "Pb3Ag3S2");
    public static final DeferredItem<GtMaterialItem> CINNABAR_DUST = material("cinnabar_dust", "HgS");
    public static final DeferredItem<GtMaterialItem> SPHALERITE_DUST = material("sphalerite_dust", "ZnS");
    public static final DeferredItem<GtMaterialItem> BAUXITE_DUST = material("bauxite_dust", "TiAl16H10O12");
    public static final DeferredItem<GtMaterialItem> ZINC_DUST = material("zinc_dust", "Zn");
    public static final DeferredItem<GtMaterialItem> PLATINUM_DUST = material("platinum_dust", "Pt");
    public static final DeferredItem<GtMaterialItem> SAPPHIRE_DUST = material("sapphire_dust", "Al2O6");
    public static final DeferredItem<GtMaterialItem> GREEN_SAPPHIRE_DUST = material("green_sapphire_dust", "Al2O6");
    public static final DeferredItem<GtMaterialItem> OLIVINE_DUST = material("olivine_dust", "Mg2Fe2SiO4");
    public static final DeferredItem<GtMaterialItem> RED_GARNET_DUST = material("red_garnet_dust",
            "(Al2Mg3Si3O12)3(Al2Fe3Si3O12)5(Al2Mn3Si3O12)8");
    public static final DeferredItem<GtMaterialItem> YELLOW_GARNET_DUST = material("yellow_garnet_dust",
            "(Ca3Fe2Si3O12)5(Ca3Al2Si3O12)8(Ca3Cr2Si3O12)3");
    public static final DeferredItem<GtMaterialItem> CALCITE_DUST = material("calcite_dust", "CaCO3");
    public static final DeferredItem<GtMaterialItem> CALCIUM_CARBONATE_CELL = material("calcium_carbonate_cell",
            "CaCO3");
    public static final DeferredItem<GtMaterialItem> SMALL_SULFUR_DUST = material("small_sulfur_dust", "S");

    public static DeferredItem<BlockItem> ore(GtOre ore) {
        return ORES.get(ore);
    }

    /** The deepslate variant's item, {@code null} if the ore has none. */
    public static DeferredItem<BlockItem> deepslateOre(GtOre ore) {
        return DEEPSLATE_ORES.get(ore);
    }

    /** Items with a generated flat item model. */
    public static List<DeferredItem<? extends Item>> simpleItems() {
        return Collections.unmodifiableList(SIMPLE);
    }

    private static DeferredItem<GtMaterialItem> material(String name, String formula) {
        DeferredItem<GtMaterialItem> item = ITEMS.registerItem(name,
                properties -> GtMaterialItem.formula(properties, formula));
        SIMPLE.add(item);
        return item;
    }

    private static DeferredItem<GtMaterialItem> glowing(String name, String formula) {
        DeferredItem<GtMaterialItem> item = ITEMS.registerItem(name,
                properties -> GtMaterialItem.glowing(properties, formula));
        SIMPLE.add(item);
        return item;
    }

    private static <T extends Item> DeferredItem<T> simple(DeferredItem<T> item) {
        SIMPLE.add(item);
        return item;
    }

    private static DeferredItem<GtRadioactiveCellItem> radioactive(String name, int maxDelay, int cells, int pulseRate,
                                                                   boolean leavesDepleted) {
        return simple(ITEMS.registerItem(name,
                properties -> new GtRadioactiveCellItem(properties, maxDelay, cells, pulseRate, leavesDepleted)));
    }

    private static DeferredItem<GtMaterialItem> component(String name) {
        DeferredItem<GtMaterialItem> item = ITEMS.registerItem(name,
                properties -> GtMaterialItem.described(properties, name));
        SIMPLE.add(item);
        return item;
    }
}
