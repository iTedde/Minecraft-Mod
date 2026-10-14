package dev.craft.ic2;

import net.neoforged.bus.api.IEventBus;

/** Entry point of the IndustrialCraft 2 part. */
public final class Ic2 {
    private Ic2() {
    }

    public static void init(IEventBus modBus) {
        Ic2Blocks.BLOCKS.register(modBus);
        Ic2Items.ITEMS.register(modBus);
        Ic2BlockEntityTypes.BLOCK_ENTITY_TYPES.register(modBus);
    }
}
