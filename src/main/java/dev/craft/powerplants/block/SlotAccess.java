package dev.craft.powerplants.block;

import java.util.function.Predicate;

import dev.craft.ic2.machine.MachineInventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Automation view of one inventory slot: inserts only what {@code accepts} allows, extracts only if
 * {@code extractable}.
 */
public record SlotAccess(MachineInventory inventory, int slot, Predicate<ItemStack> accepts, boolean extractable)
        implements IItemHandler {
    public static SlotAccess input(MachineInventory inventory, int slot, Predicate<ItemStack> accepts) {
        return new SlotAccess(inventory, slot, accepts, false);
    }

    public static SlotAccess output(MachineInventory inventory, int slot) {
        return new SlotAccess(inventory, slot, stack -> false, true);
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int index) {
        return inventory.getStackInSlot(slot);
    }

    @Override
    public ItemStack insertItem(int index, ItemStack stack, boolean simulate) {
        return accepts.test(stack) ? inventory.insertItem(slot, stack, simulate) : stack;
    }

    @Override
    public ItemStack extractItem(int index, int amount, boolean simulate) {
        return extractable ? inventory.extractItem(slot, amount, simulate) : ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int index) {
        return inventory.getSlotLimit(slot);
    }

    @Override
    public boolean isItemValid(int index, ItemStack stack) {
        return accepts.test(stack);
    }
}
