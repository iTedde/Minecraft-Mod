package dev.craft.forestry.menu;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/** Forestry {@code SlotForestry} with {@code isPhantom}: holds a pattern copy, changed only by phantom clicks. */
public class PhantomSlot extends SlotItemHandler {
    private final Runnable onChanged;

    public PhantomSlot(IItemHandler handler, int index, int x, int y, Runnable onChanged) {
        super(handler, index, x, y);
        this.onChanged = onChanged;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }

    @Override
    public boolean mayPickup(Player player) {
        return false;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        onChanged.run();
    }
}
