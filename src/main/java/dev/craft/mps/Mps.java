package dev.craft.mps;

import dev.craft.mps.entity.MpsEntityTypes;
import net.neoforged.bus.api.IEventBus;

/** Entry point of the Modular Powersuits content (spec 29). */
public final class Mps {
    private Mps() {
    }

    public static void init(IEventBus modBus) {
        MpsDataComponents.COMPONENTS.register(modBus);
        MpsBlocks.BLOCKS.register(modBus);
        MpsBlocks.BLOCK_ENTITY_TYPES.register(modBus);
        MpsItems.MATERIALS.register(modBus);
        MpsItems.ITEMS.register(modBus);
        MpsEntityTypes.ENTITY_TYPES.register(modBus);
        modBus.addListener(MpsPayloads::register);
    }
}
