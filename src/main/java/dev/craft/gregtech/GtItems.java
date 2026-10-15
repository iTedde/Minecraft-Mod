package dev.craft.gregtech;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import dev.craft.Craft;
import dev.craft.gregtech.block.GtCasingBlock;
import dev.craft.gregtech.item.GtMachineItem;
import dev.craft.gregtech.item.GtMaterialItem;
import dev.craft.gregtech.item.LithiumBatpackItem;
import dev.craft.gregtech.item.LithiumBatteryItem;
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

    static {
        for (GtMachine machine : GtMachine.values()) {
            MACHINES.put(machine, ITEMS.registerItem(machine.getName(),
                    properties -> new GtMachineItem(GtBlocks.machine(machine).get(), properties)));
        }
        for (GtCasingBlock.Type type : GtCasingBlock.Type.values()) {
            CASINGS.put(type, ITEMS.registerSimpleBlockItem(GtBlocks.casing(type)));
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
    /** Material 4: no formula line. */
    public static final DeferredItem<GtMaterialItem> IRIDIUM_ALLOY_INGOT = material("iridium_alloy_ingot", "");

    // --- cells (GT_MetaItem_Cell) ---
    public static final DeferredItem<GtMaterialItem> HYDROGEN_CELL = material("hydrogen_cell", "H");
    public static final DeferredItem<GtMaterialItem> LITHIUM_CELL = material("lithium_cell", "Li");
    public static final DeferredItem<GtMaterialItem> SILICON_CELL = material("silicon_cell", "Si");
    public static final DeferredItem<GtMaterialItem> CARBON_CELL = material("carbon_cell", "C");
    public static final DeferredItem<GtMaterialItem> CALCIUM_CELL = material("calcium_cell", "Ca");
    public static final DeferredItem<GtMaterialItem> SODIUM_CELL = material("sodium_cell", "Na");

    // --- dusts (GT_MetaItem_Dust) ---
    public static final DeferredItem<GtMaterialItem> SULFUR_DUST = material("sulfur_dust", "S");
    public static final DeferredItem<GtMaterialItem> ALUMINIUM_DUST = material("aluminium_dust", "Al");
    public static final DeferredItem<GtMaterialItem> DARK_ASH = material("dark_ash", "C");
    public static final DeferredItem<GtMaterialItem> STEEL_DUST = material("steel_dust", "Fe");
    public static final DeferredItem<GtMaterialItem> NICKEL_DUST = material("nickel_dust", "Ni");
    public static final DeferredItem<GtMaterialItem> EMERALD_DUST = material("emerald_dust", "Be3Al2Si6O18");
    public static final DeferredItem<GtMaterialItem> DIAMOND_DUST = material("diamond_dust", "C128");

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

    private GtItems() {
    }

    public static DeferredItem<GtMachineItem> machine(GtMachine machine) {
        return MACHINES.get(machine);
    }

    public static DeferredItem<BlockItem> casing(GtCasingBlock.Type type) {
        return CASINGS.get(type);
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

    private static DeferredItem<GtMaterialItem> component(String name) {
        DeferredItem<GtMaterialItem> item = ITEMS.registerItem(name,
                properties -> GtMaterialItem.described(properties, name));
        SIMPLE.add(item);
        return item;
    }
}
