package dev.craft.ic2.machine;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.ItemStackHandler;

/** Item storage of a machine; reports changes to the owner (IC2 {@code onInventoryChanged}). */
public class MachineInventory extends ItemStackHandler {
    private final Runnable onChanged;

    public MachineInventory(int size, Runnable onChanged) {
        super(size);
        this.onChanged = onChanged;
    }

    @Override
    protected void onContentsChanged(int slot) {
        onChanged.run();
    }

    /** Direct slot write without validity checks, as IC2's inventory arrays allow. */
    public void set(int slot, ItemStack stack) {
        setStackInSlot(slot, stack);
    }

    public ItemStack get(int slot) {
        return getStackInSlot(slot);
    }

    public boolean isEmpty(int slot) {
        return getStackInSlot(slot).isEmpty();
    }
}
