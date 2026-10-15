package dev.craft.buildcraft.item;

import net.minecraft.world.item.Item;

/**
 * BuildCraft wrench ({@code ItemWrench}, {@code IToolWrench}). The blocks check for it themselves: engines turn their
 * output, the quarry re-initialises.
 */
public class BcWrenchItem extends Item {
    public BcWrenchItem(Properties properties) {
        super(properties);
    }
}
