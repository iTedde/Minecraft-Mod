package dev.craft.ic2.item;

import net.minecraft.world.item.Item;

/** Machine upgrade module (IC2 {@code ItemUpgradeModule}); one item per former metadata value. */
public class UpgradeModuleItem extends Item {
    /** GregTech {@code UpgradeStacksize}, pack value 4 (IC2 alone: 64). */
    public static final int MAX_STACK_SIZE = 4;

    private final Type type;

    public UpgradeModuleItem(Properties properties, Type type) {
        super(properties);
        this.type = type;
    }

    public Type getType() {
        return type;
    }

    public enum Type {
        OVERCLOCKER,
        TRANSFORMER,
        ENERGY_STORAGE
    }
}
