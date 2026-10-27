package dev.craft.redpower;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/** Entry point of the RedPower content: World gem tools (spec 19), project table (spec 20). */
public final class RedPower {
    private RedPower() {
    }

    public static void init(IEventBus modBus) {
        RpBlocks.BLOCKS.register(modBus);
        RpItems.ITEMS.register(modBus);
        RpBlockEntityTypes.BLOCK_ENTITY_TYPES.register(modBus);
        RpMenus.MENUS.register(modBus);
        RpDataComponents.COMPONENTS.register(modBus);
        modBus.addListener(RedPower::registerCapabilities);
    }

    /** Every side of the project tables reaches the storage slots only (18, large table 27). */
    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, RpBlockEntityTypes.PROJECT_TABLE.get(),
                (table, side) -> table.getStorage());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, RpBlockEntityTypes.LARGE_PROJECT_TABLE.get(),
                (table, side) -> table.getStorage());
    }
}
