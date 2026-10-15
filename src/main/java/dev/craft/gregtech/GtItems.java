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
    public static final DeferredItem<GtMaterialItem> IRON_PLATE = plate("iron_plate", "Fe");
    public static final DeferredItem<GtMaterialItem> GOLD_PLATE = plate("gold_plate", "Au");
    public static final DeferredItem<GtMaterialItem> REFINED_IRON_PLATE = plate("refined_iron_plate", "Fe");
    public static final DeferredItem<GtMaterialItem> TIN_PLATE = plate("tin_plate", "Sn");
    public static final DeferredItem<GtMaterialItem> COPPER_PLATE = plate("copper_plate", "Cu");
    public static final DeferredItem<GtMaterialItem> BRONZE_PLATE = plate("bronze_plate", "SnCu3");

    // --- components (GT_MetaItem_Component) ---
    public static final DeferredItem<GtMaterialItem> CONVEYOR_MODULE = component("conveyor_module");
    public static final DeferredItem<GtMaterialItem> MACHINE_PARTS = component("machine_parts");

    private GtItems() {
    }

    public static DeferredItem<GtMachineItem> machine(GtMachine machine) {
        return MACHINES.get(machine);
    }

    /** Items with a generated flat item model. */
    public static List<DeferredItem<? extends Item>> simpleItems() {
        return Collections.unmodifiableList(SIMPLE);
    }

    private static DeferredItem<GtMaterialItem> plate(String name, String formula) {
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
