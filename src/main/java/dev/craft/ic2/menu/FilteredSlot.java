package dev.craft.ic2.menu;

import java.util.function.Predicate;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/** Machine slot that only accepts stacks matching a predicate in the GUI (IC2 slot classes). */
public class FilteredSlot extends SlotItemHandler {
    private final Predicate<ItemStack> filter;

    public FilteredSlot(IItemHandler handler, int index, int x, int y, Predicate<ItemStack> filter) {
        super(handler, index, x, y);
        this.filter = filter;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return filter.test(stack) && super.mayPlace(stack);
    }
}
