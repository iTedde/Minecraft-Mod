package dev.craft.ic2;

import dev.craft.ic2.generator.GeothermalGeneratorBlockEntity;
import dev.craft.ic2.machine.MachineBlockEntity;
import dev.craft.ic2.world.Ic2Features;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/** Entry point of the IndustrialCraft 2 part. */
public final class Ic2 {
    private Ic2() {
    }

    public static void init(IEventBus modBus) {
        Ic2Blocks.BLOCKS.register(modBus);
        Ic2Items.ITEMS.register(modBus);
        Ic2BlockEntityTypes.BLOCK_ENTITY_TYPES.register(modBus);
        Ic2DataComponents.COMPONENTS.register(modBus);
        Ic2Recipes.TYPES.register(modBus);
        Ic2Recipes.SERIALIZERS.register(modBus);
        Ic2Menus.MENUS.register(modBus);
        Ic2Sounds.SOUNDS.register(modBus);
        Ic2Features.FEATURES.register(modBus);
        modBus.addListener(Ic2::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (Ic2Machine machine : Ic2Machine.values()) {
            event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, Ic2BlockEntityTypes.get(machine),
                    MachineBlockEntity::getItemHandler);
        }
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                Ic2BlockEntityTypes.get(Ic2Machine.GEOTHERMAL_GENERATOR),
                (be, side) -> ((GeothermalGeneratorBlockEntity) be).getFluidHandler());
    }
}
