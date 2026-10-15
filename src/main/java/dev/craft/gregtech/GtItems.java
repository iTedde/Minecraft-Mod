package dev.craft.gregtech;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import dev.craft.Craft;
import dev.craft.gregtech.item.GtMachineItem;
import dev.craft.gregtech.item.GtMaterialItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class GtItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Craft.MODID);

    private static final List<DeferredItem<? extends Item>> SIMPLE = new ArrayList<>();
    private static final Map<GtMachine, DeferredItem<GtMachineItem>> MACHINES = new EnumMap<>(GtMachine.class);

    static {
        for (GtMachine machine : GtMachine.values()) {
            MACHINES.put(machine, ITEMS.registerItem(machine.getName(),
                    properties -> new GtMachineItem(GtBlocks.machine(machine).get(), properties)));
        }
    }

    // --- plates (GT_MetaItem_Material 64–84) ---
    public static final DeferredItem<GtMaterialItem> IRON_PLATE = material("iron_plate", "Fe");
    public static final DeferredItem<GtMaterialItem> GOLD_PLATE = material("gold_plate", "Au");
    public static final DeferredItem<GtMaterialItem> REFINED_IRON_PLATE = material("refined_iron_plate", "Fe");
    public static final DeferredItem<GtMaterialItem> TIN_PLATE = material("tin_plate", "Sn");
    public static final DeferredItem<GtMaterialItem> COPPER_PLATE = material("copper_plate", "Cu");
    public static final DeferredItem<GtMaterialItem> BRONZE_PLATE = material("bronze_plate", "SnCu3");

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

    // --- components (GT_MetaItem_Component) ---
    public static final DeferredItem<GtMaterialItem> CONVEYOR_MODULE = component("conveyor_module");
    public static final DeferredItem<GtMaterialItem> MACHINE_PARTS = component("machine_parts");

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
