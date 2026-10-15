package dev.craft.compactsolars;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/** Entry point of the Compact Solars part: LV/MV/HV solar arrays as flat multiblocks (spec 11 §4). */
public final class CompactSolars {
    private CompactSolars() {
    }

    public static void init(IEventBus modBus) {
        CsBlocks.BLOCKS.register(modBus);
        CsItems.ITEMS.register(modBus);
        CsBlockEntityTypes.BLOCK_ENTITY_TYPES.register(modBus);
        CsMenus.MENUS.register(modBus);
        modBus.addListener(CompactSolars::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, CsBlockEntityTypes.SOLAR_ARRAY.get(),
                SolarArrayBlockEntity::getItemHandler);
    }
}
