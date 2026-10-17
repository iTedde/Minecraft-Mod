package dev.craft.buildcraft.factory;

import net.minecraft.world.item.ItemStack;

/**
 * BuildCraft {@code ISpecialInventory} for extraction: wooden and emerald pipes ask the block entity for its output
 * instead of taking from the item capability's slots. The result may be larger than the pipe's MJ (one craft); the pipe
 * pays MJ for the extracted count afterwards.
 */
public interface SpecialInventory {
    /** {@code extractItem(doRemove, from, maxItemCount)}; empty when there is nothing to take. */
    ItemStack extractItem(boolean doRemove);
}
