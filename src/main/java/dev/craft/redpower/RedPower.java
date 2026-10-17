package dev.craft.redpower;

import net.neoforged.bus.api.IEventBus;

/** Entry point of the RedPower World gem tools (spec 19). */
public final class RedPower {
    private RedPower() {
    }

    public static void init(IEventBus modBus) {
        RpItems.ITEMS.register(modBus);
    }
}
