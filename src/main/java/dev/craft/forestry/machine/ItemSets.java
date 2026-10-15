package dev.craft.forestry.machine;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

/** Forestry {@code StackUtils.condenseStacks/containsSets} and the "take one set from the stock" loops. */
final class ItemSets {
    private ItemSets() {
    }

    /** {@code condenseStacks}: equal items merged, counts summed. */
    static List<ItemStack> condense(List<ItemStack> stacks) {
        List<ItemStack> condensed = new ArrayList<>();
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            boolean matched = false;
            for (ItemStack cached : condensed) {
                if (ItemStack.isSameItemSameComponents(cached, stack)) {
                    cached.grow(stack.getCount());
                    matched = true;
                }
            }
            if (!matched) {
                condensed.add(stack.copy());
            }
        }
        return condensed;
    }

    /** {@code containsSets}: how many complete sets the stock holds (0 for an empty set). */
    static int containsSets(List<ItemStack> set, List<ItemStack> stock) {
        int count = 0;
        List<ItemStack> offered = condense(stock);
        for (ItemStack required : condense(set)) {
            boolean matched = false;
            for (ItemStack offer : offered) {
                if (ItemStack.isSameItemSameComponents(required, offer)) {
                    matched = true;
                    int stackCount = offer.getCount() / required.getCount();
                    if (stackCount <= 0) {
                        return 0;
                    }
                    if (count == 0 || count > stackCount) {
                        count = stackCount;
                    }
                }
            }
            if (!matched) {
                return 0;
            }
        }
        return count;
    }

    /**
     * Takes one set from the stock slots {@code [from, to)}; returns whether the stock held all of it. Without
     * {@code doRemove} nothing changes.
     */
    static boolean removeSet(IItemHandlerModifiable inventory, int from, int to, List<ItemStack> set,
                             boolean doRemove) {
        List<ItemStack> condensed = condense(set);
        for (ItemStack required : condensed) {
            for (int slot = from; slot < to; slot++) {
                ItemStack stock = inventory.getStackInSlot(slot);
                if (stock.isEmpty() || !ItemStack.isSameItemSameComponents(stock, required)) {
                    continue;
                }
                int removed = Math.min(stock.getCount(), required.getCount());
                if (doRemove && removed > 0) {
                    ItemStack rest = stock.copy();
                    rest.shrink(removed);
                    inventory.setStackInSlot(slot, rest);
                }
                required.shrink(removed);
            }
        }
        for (ItemStack required : condensed) {
            if (!required.isEmpty()) {
                return false;
            }
        }
        return true;
    }
}
