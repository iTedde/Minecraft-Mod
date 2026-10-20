package dev.craft.ironchest;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Iron Chests {@code ContainerIronChestBase} (spec 22 §1): chest grid from (12, 8), player inventory centred. */
public class IronChestMenu extends AbstractContainerMenu {
    private final Container chest;
    private final IronChestType type;

    /** Client side. */
    public IronChestMenu(int id, Inventory inventory, IronChestType type) {
        this(id, inventory, new SimpleContainer(type.getSize()), type);
    }

    public IronChestMenu(int id, Inventory inventory, Container chest, IronChestType type) {
        super(IronChestMenus.IRON_CHEST.get(), id);
        this.chest = chest;
        this.type = type;
        checkContainerSize(chest, type.getSize());
        chest.startOpen(inventory.player);
        for (int row = 0; row < type.getRowCount(); row++) {
            for (int col = 0; col < type.getRowLength(); col++) {
                addSlot(new Slot(chest, col + row * type.getRowLength(), 12 + col * 18, 8 + row * 18));
            }
        }
        int left = (type.getGuiWidth() - 162) / 2 + 1;
        int height = type.getGuiHeight();
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                addSlot(new Slot(inventory, col + row * 9 + 9, left + col * 18, height - (4 - row) * 18 - 10));
            }
        }
        for (int col = 0; col < 9; col++) {
            addSlot(new Slot(inventory, col, left + col * 18, height - 24));
        }
    }

    public Container getContainer() {
        return chest;
    }

    public IronChestType getChestType() {
        return type;
    }

    /** Chest to inventory (filled from the back), inventory to chest. */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        int size = type.getSize();
        if (index < size) {
            if (!moveItemStackTo(stack, size, slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, size, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    @Override
    public boolean stillValid(Player player) {
        return chest.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        chest.stopOpen(player);
    }
}
