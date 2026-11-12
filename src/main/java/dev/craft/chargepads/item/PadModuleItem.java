package dev.craft.chargepads.item;

import net.minecraft.world.item.Item;

/** An emitter module item (spec 47 §7); registered with phase 47.2. */
public class PadModuleItem extends Item {
    private final PadModule module;

    public PadModuleItem(Properties properties, PadModule module) {
        super(properties);
        this.module = module;
    }

    public PadModule getModule() {
        return module;
    }
}
