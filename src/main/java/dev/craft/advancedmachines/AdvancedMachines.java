package dev.craft.advancedmachines;

import net.neoforged.bus.api.IEventBus;

/**
 * Entry point of Advanced Machines 4.7b (spec 25). The three machines are {@code Ic2Machine} entries (blocks, block
 * entity types, menus and item capabilities come with the IC2 machines); this package holds their logic.
 */
public final class AdvancedMachines {
    private AdvancedMachines() {
    }

    public static void init(IEventBus modBus) {
        AdvancedMachinesItems.ITEMS.register(modBus);
    }
}
