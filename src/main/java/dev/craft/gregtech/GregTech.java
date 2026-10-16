package dev.craft.gregtech;

import dev.craft.buildcraft.power.BcCapabilities;
import dev.craft.gregtech.machine.GtMachineBlockEntity;
import dev.craft.gregtech.machine.GtTankMachineBlockEntity;
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
                    GtMachineBlockEntity::getItemHandler);
            // MJ receiver only with an installed MJ converter (spec 13 §6.3).
            event.registerBlockEntity(BcCapabilities.POWER_RECEPTOR, GtBlockEntityTypes.get(machine),
                    (be, side) -> be.getPowerProvider() != null ? be : null);
        }
        for (GtMachine machine : new GtMachine[]{GtMachine.INDUSTRIAL_ELECTROLYZER, GtMachine.INDUSTRIAL_CENTRIFUGE}) {
            event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, GtBlockEntityTypes.get(machine),
                    (be, side) -> ((GtTankMachineBlockEntity) be).getFluidHandler(side));
        }
    }
}
