package dev.craft.mffs;

import dev.craft.buildcraft.power.BcCapabilities;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;

/** Entry point of Modular Force Field System V2 2.2.8.3.6 (spec 48); own creative tab (§11). */
public final class Mffs {
    private Mffs() {
    }

    public static void init(IEventBus modBus) {
        MffsBlocks.BLOCKS.register(modBus);
        MffsItems.ITEMS.register(modBus);
        MffsBlockEntityTypes.BLOCK_ENTITY_TYPES.register(modBus);
        MffsMenus.MENUS.register(modBus);
        MffsDataComponents.COMPONENTS.register(modBus);
        modBus.addListener(Mffs::registerCapabilities);
        modBus.addListener(MffsPayloads::register);
        modBus.addListener(MffsChunkLoading::register);
    }

    /**
     * {@code ISidedInventory}: the extractor takes forcicium in slot 0 from every side, the converter exposes its
     * hidden slot 1, the security storage slots 1–54 while inactive, the defence station its storage 15–34, the
     * capacitor nothing. The extractor also takes MJ.
     */
    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, MffsBlockEntityTypes.EXTRACTOR.get(),
                (extractor, side) -> new RangedWrapper(extractor.getInventory(), 0, 1));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, MffsBlockEntityTypes.CONVERTER.get(),
                (converter, side) -> new RangedWrapper(converter.getInventory(), 1, 2));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, MffsBlockEntityTypes.SECURITY_STORAGE.get(),
                (storage, side) -> storage.getAutomationHandler());
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, MffsBlockEntityTypes.DEFENSE_STATION.get(),
                (station, side) -> station.getAutomationHandler());
        event.registerBlockEntity(BcCapabilities.POWER_RECEPTOR, MffsBlockEntityTypes.EXTRACTOR.get(),
                (extractor, side) -> extractor);
    }
}
