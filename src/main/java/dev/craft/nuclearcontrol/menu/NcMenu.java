package dev.craft.nuclearcontrol.menu;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

/** Base of the Nuclear Control menus: bound to a block position, valid within 8 blocks ({@code isUseableByPlayer}). */
public abstract class NcMenu extends AbstractContainerMenu {
    protected final BlockPos pos;

    protected NcMenu(MenuType<?> type, int id, BlockPos pos) {
        super(type, id);
        this.pos = pos;
    }

    public BlockPos getPos() {
        return pos;
    }

    protected abstract boolean isOwnBlockEntity(BlockEntity blockEntity);

    @Override
    public boolean stillValid(Player player) {
        BlockEntity blockEntity = player.level().getBlockEntity(pos);
        return blockEntity != null && !blockEntity.isRemoved() && isOwnBlockEntity(blockEntity)
                && player.distanceToSqr(pos.getCenter()) <= 64.0;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    /**
     * {@code transferStackInSlot} of the containers with own slots first: from an own slot into the player inventory;
     * from the player the whole stack into the first empty own slot that accepts it.
     */
    protected ItemStack quickMoveOwn(int ownSlots, int index) {
        Slot source = slots.get(index);
        if (!source.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = source.getItem();
        if (index < ownSlots) {
            ItemStack original = stack.copy();
            moveItemStackTo(stack, ownSlots, slots.size(), false);
            source.setChanged();
            return stack.getCount() != original.getCount() && !stack.isEmpty() ? stack.copy() : ItemStack.EMPTY;
        }
        for (int target = 0; target < ownSlots; target++) {
            Slot slot = slots.get(target);
            if (!slot.hasItem() && slot.mayPlace(stack)) {
                slot.setByPlayer(stack.copy());
                source.setByPlayer(ItemStack.EMPTY);
                break;
            }
        }
        return ItemStack.EMPTY;
    }

    /** Player inventory at {@code x 8}, main rows from {@code top}, hotbar 58 below. */
    protected void addPlayerInventory(net.minecraft.world.entity.player.Inventory inventory, int top) {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, top + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, top + 58));
        }
    }
}
