package dev.craft.forestry;

import dev.craft.buildcraft.power.BcCapabilities;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/**
 * Entry point of the Forestry part: Thermionic Fabricator, Carpenter, electron tubes, circuit boards and the
 * soldering iron (the Electrical Engine follows).
 */
public final class Forestry {
    private Forestry() {
    }

    public static void init(IEventBus modBus) {
        FrBlocks.BLOCKS.register(modBus);
        FrItems.ITEMS.register(modBus);
        FrFluids.FLUID_TYPES.register(modBus);
        FrFluids.FLUIDS.register(modBus);
        FrBlockEntityTypes.BLOCK_ENTITY_TYPES.register(modBus);
        FrDataComponents.COMPONENTS.register(modBus);
        FrRecipes.TYPES.register(modBus);
        FrRecipes.SERIALIZERS.register(modBus);
        FrMenus.MENUS.register(modBus);
        modBus.addListener(Forestry::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(BcCapabilities.POWER_RECEPTOR, FrBlockEntityTypes.FABRICATOR.get(), (be, side) -> be);
        event.registerBlockEntity(BcCapabilities.POWER_RECEPTOR, FrBlockEntityTypes.CARPENTER.get(), (be, side) -> be);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FrBlockEntityTypes.FABRICATOR.get(),
                (be, side) -> be.getItemHandler(side));
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, FrBlockEntityTypes.CARPENTER.get(),
                (be, side) -> be.getItemHandler());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, FrBlockEntityTypes.CARPENTER.get(),
                (be, side) -> be.getFluidHandler());
    }
}
