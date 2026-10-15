package dev.craft.buildcraft;

import dev.craft.buildcraft.engine.CombustionEngineBlockEntity;
import dev.craft.buildcraft.engine.EngineType;
import dev.craft.buildcraft.power.BcCapabilities;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

/** Entry point of the BuildCraft part (MJ system and engines). */
public final class BuildCraft {
    private BuildCraft() {
    }

    public static void init(IEventBus modBus) {
        BcBlocks.BLOCKS.register(modBus);
        BcItems.ITEMS.register(modBus);
        BcBlockEntityTypes.BLOCK_ENTITY_TYPES.register(modBus);
        BcMenus.MENUS.register(modBus);
        modBus.addListener(BuildCraft::registerCapabilities);
    }

    private static void registerCapabilities(RegisterCapabilitiesEvent event) {
        for (EngineType type : EngineType.values()) {
            event.registerBlockEntity(BcCapabilities.POWER_RECEPTOR, BcBlockEntityTypes.engine(type),
                    (be, side) -> be);
            if (type.hasMenu()) {
                event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, BcBlockEntityTypes.engine(type),
                        (be, side) -> be.getItemHandler());
            }
        }
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, BcBlockEntityTypes.engine(EngineType.COMBUSTION),
                (be, side) -> ((CombustionEngineBlockEntity) be).getFluidHandler());
    }
}
