package dev.craft.gregtech;

import dev.craft.gregtech.machine.GtBasicMachineBlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/** Entry point of the GregTech part: the GT machines and materials the pack recipes need (spec 12/13). */
public final class GregTech {
    private GregTech() {
    }

    public static void init(IEventBus modBus) {
        GtBlocks.BLOCKS.register(modBus);
        GtItems.ITEMS.register(modBus);
        GtBlockEntityTypes.BLOCK_ENTITY_TYPES.register(modBus);
        GtMenus.MENUS.register(modBus);
        GtRecipes.TYPES.register(modBus);
        GtRecipes.SERIALIZERS.register(modBus);
        modBus.addListener(GregTech::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (GtMachine machine : GtMachine.values()) {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, GtBlockEntityTypes.get(machine),
                    GtBasicMachineBlockEntity::getItemHandler);
        }
    }
}
