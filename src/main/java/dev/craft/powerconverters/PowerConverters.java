package dev.craft.powerconverters;

import dev.craft.buildcraft.power.BcCapabilities;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Entry point of the Power Converters part: Energy Bridge, Universal Charger and the BuildCraft and IC2 consumers
 * and producers (MJ and EU through internal energy units).
 */
public final class PowerConverters {
    private PowerConverters() {
    }

    public static void init(IEventBus modBus) {
        PcBlocks.BLOCKS.register(modBus);
        PcItems.ITEMS.register(modBus);
        PcBlockEntityTypes.BLOCK_ENTITY_TYPES.register(modBus);
        PcMenus.MENUS.register(modBus);
        modBus.addListener(PowerConverters::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(BcCapabilities.POWER_RECEPTOR, PcBlockEntityTypes.BC_CONSUMER.get(),
                (be, side) -> be);
        event.registerBlockEntity(BcCapabilities.POWER_RECEPTOR, PcBlockEntityTypes.BC_PRODUCER.get(),
                (be, side) -> be);
    }
}
