package dev.craft.enderstorage;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/** Entry point of the EnderStorage content (spec 27). */
public final class EnderStorage {
    private EnderStorage() {
    }

    public static void init(IEventBus modBus) {
        EsDataComponents.COMPONENTS.register(modBus);
        EsBlocks.BLOCKS.register(modBus);
        EsItems.ITEMS.register(modBus);
        EsBlockEntityTypes.BLOCK_ENTITY_TYPES.register(modBus);
        EsMenus.MENUS.register(modBus);
        EsRecipes.SERIALIZERS.register(modBus);
        modBus.addListener(EnderStorage::registerCapabilities);
    }

    /** The chest is a plain {@code IInventory}: every slot from every side. */
    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, EsBlockEntityTypes.ENDER_CHEST.get(),
                (chest, side) -> chest.getItemHandler());
    }
}
