package dev.craft.enderstorage;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;

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
        modBus.addListener(TankSynchroniser::registerPayloads);
        NeoForge.EVENT_BUS.register(TankSynchroniser.class);
    }

    /** The chest is a plain {@code IInventory}: every slot from every side; the tank fills and drains anywhere. */
    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, EsBlockEntityTypes.ENDER_CHEST.get(),
                (chest, side) -> chest.getItemHandler());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, EsBlockEntityTypes.ENDER_TANK.get(),
                (tank, side) -> tank.getFluidHandler());
    }
}
