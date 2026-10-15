package dev.craft.forestry.menu;

import dev.craft.ic2.menu.SyncedInts;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Forestry {@code ContainerForestry}: machine slots first, then the player inventory. Phantom slots take a copy of
 * the held item on a left click and are cleared by a right click; shift-click moves machine slots to the player
 * (from the back) and player slots into the first {@code shiftSlots} machine slots.
 */
public abstract class ForestryMenu extends AbstractContainerMenu {
    protected final BlockPos pos;
    protected final SyncedInts data;
    private int shiftSlots;

    protected ForestryMenu(@Nullable MenuType<?> type, int id, BlockPos pos, SyncedInts data) {
        super(type, id);
        this.pos = pos;
        this.data = data;
        addDataSlots(data);
    }

    public BlockPos getPos() {
        return pos;
    }

    /** Number of machine slots shift-clicked items may go to ({@code slotCount}); set before the player slots. */
    protected void setShiftSlots(int count) {
        this.shiftSlots = count;
    }

    protected void addPlayerInventory(Inventory inventory, int y, int hotbarY) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, hotbarY));
        }
    }

    protected int playerSlotsStart() {
        return slots.size() - 36;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < slots.size() && slots.get(slotId) instanceof PhantomSlot phantom) {
            if (clickType == ClickType.PICKUP || clickType == ClickType.QUICK_MOVE) {
                if (button == 1) {
                    phantom.set(ItemStack.EMPTY);
                } else if (!getCarried().isEmpty()) {
                    phantom.set(getCarried().copyWithCount(1));
                }
            }
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        if (index < playerSlotsStart()) {
            if (!moveItemStackTo(stack, playerSlotsStart(), slots.size(), true)) {
                return ItemStack.EMPTY;
            }
        } else if (!moveItemStackTo(stack, 0, shiftSlots, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return original;
    }

    /** {@code isUseableByPlayer}: within 8 blocks of the machine. */
    @Override
    public boolean stillValid(Player player) {
        return player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= 64.0;
    }
}
