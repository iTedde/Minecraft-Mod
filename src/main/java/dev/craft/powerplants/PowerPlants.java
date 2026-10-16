package dev.craft.powerplants;

import dev.craft.powerplants.block.PlantPartBlockEntity;
import dev.craft.powerplants.boiler.BoilerBlockEntity;
import dev.craft.powerplants.cokeoven.CokeOvenBlockEntity;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/** Entry point of the new power plants (spec 14): coke oven, coal power plant, water turbine. */
public final class PowerPlants {
    private PowerPlants() {
    }

    public static void init(IEventBus modBus) {
        PpFluids.FLUID_TYPES.register(modBus);
        PpFluids.FLUIDS.register(modBus);
        PpBlocks.BLOCKS.register(modBus);
        PpItems.ITEMS.register(modBus);
        PpBlockEntityTypes.BLOCK_ENTITY_TYPES.register(modBus);
        PpMenus.MENUS.register(modBus);
        modBus.addListener(PowerPlants::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, PpBlockEntityTypes.PLANT_PART.get(),
                PlantPartBlockEntity::getItemHandler);
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, PpBlockEntityTypes.PLANT_PART.get(),
                PlantPartBlockEntity::getFluidHandler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, PpBlockEntityTypes.COKE_OVEN.get(),
                CokeOvenBlockEntity::getItemHandler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, PpBlockEntityTypes.BOILER.get(),
                BoilerBlockEntity::getItemHandler);
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, PpBlockEntityTypes.BOILER.get(),
                BoilerBlockEntity::getPartFluidHandler);
    }
}
