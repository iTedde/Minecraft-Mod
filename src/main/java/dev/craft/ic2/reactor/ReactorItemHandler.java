package dev.craft.ic2.reactor;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

/**
 * Automation access to the reactor matrix (IC2 plain {@code IInventory}, all 54 slots from every side): slots right
 * of the usable width are redirected to the last usable column of their row, and only items that may stay in the
 * reactor are accepted (like the GUI slots).
 */
public class ReactorItemHandler implements IItemHandlerModifiable {
    private final NuclearReactorBlockEntity reactor;

    public ReactorItemHandler(NuclearReactorBlockEntity reactor) {
        this.reactor = reactor;
    }

    @Override
    public int getSlots() {
        return NuclearReactorBlockEntity.COLUMNS * NuclearReactorBlockEntity.ROWS;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return reactor.getInventory().getStackInSlot(reactor.redirectSlot(slot));
    }

    @Override
    public void setStackInSlot(int slot, ItemStack stack) {
        reactor.getInventory().setStackInSlot(reactor.redirectSlot(slot), stack);
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (!isItemValid(slot, stack)) {
            return stack;
        }
        return reactor.getInventory().insertItem(reactor.redirectSlot(slot), stack, simulate);
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return reactor.getInventory().extractItem(reactor.redirectSlot(slot), amount, simulate);
    }

    @Override
    public int getSlotLimit(int slot) {
        return reactor.getInventory().getSlotLimit(reactor.redirectSlot(slot));
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return NuclearReactorBlockEntity.isUsefulItem(stack);
    }
}
