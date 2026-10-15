package dev.craft.ic2.menu;

import dev.craft.ic2.machine.MachineInventory;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/** Slot on a machine inventory that reports in-place stack changes (merges) to the machine. */
public class MachineSlot extends SlotItemHandler {
    public MachineSlot(IItemHandler handler, int index, int x, int y) {
        super(handler, index, x, y);
    }

    @Override
    public void setChanged() {
        if (getItemHandler() instanceof MachineInventory inventory) {
            inventory.notifyChanged(getSlotIndex());
        }
    }
}
