package dev.craft.ic2.menu;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

/** IC2 {@code SlotDisplay}: shows a stack (the O-Mat patterns) but can be neither filled nor emptied. */
public class DisplaySlot extends MachineSlot {
    public DisplaySlot(IItemHandler handler, int index, int x, int y) {
        super(handler, index, x, y);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }
}
