package dev.craft.ic2.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Automation view of a machine side that combines input and output slots (deviation, see {@code abweichungen.md}):
 * input slots only take what their filter accepts and give nothing back, output slots can only be emptied. Pipes and
 * hoppers therefore never fill an output slot.
 */
public final class SideAccess implements IItemHandler {
    private final IItemHandler inventory;
    private final List<Integer> slots = new ArrayList<>();
    /** Filter per entry; {@code null} marks an output slot. */
    private final List<Predicate<ItemStack>> filters = new ArrayList<>();

    public SideAccess(IItemHandler inventory) {
        this.inventory = inventory;
    }

    public SideAccess input(int slot, Predicate<ItemStack> accepts) {
        slots.add(slot);
        filters.add(accepts);
        return this;
    }

    /** Input slots {@code first..last} (inclusive) with the same filter. */
    public SideAccess inputs(int first, int last, Predicate<ItemStack> accepts) {
        for (int slot = first; slot <= last; slot++) {
            input(slot, accepts);
        }
        return this;
    }

    /** Output slots {@code first..last} (inclusive). */
    public SideAccess outputs(int first, int last) {
        for (int slot = first; slot <= last; slot++) {
            slots.add(slot);
            filters.add(null);
        }
        return this;
    }

    @Override
    public int getSlots() {
        return slots.size();
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return inventory.getStackInSlot(slots.get(index));
    }

    @Override
    public ItemStack insertItem(int index, ItemStack stack, boolean simulate) {
        return isItemValid(index, stack) ? inventory.insertItem(slots.get(index), stack, simulate) : stack;
    }

    @Override
    public ItemStack extractItem(int index, int amount, boolean simulate) {
        return filters.get(index) == null ? inventory.extractItem(slots.get(index), amount, simulate) : ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int index) {
        return inventory.getSlotLimit(slots.get(index));
    }

    @Override
    public boolean isItemValid(int index, ItemStack stack) {
        Predicate<ItemStack> filter = filters.get(index);
        return filter != null && filter.test(stack);
    }
}
