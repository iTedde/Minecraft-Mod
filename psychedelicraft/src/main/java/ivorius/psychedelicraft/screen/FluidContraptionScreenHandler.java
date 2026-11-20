/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.screen;

import org.jetbrains.annotations.Nullable;

import ivorius.psychedelicraft.block.BlockWithFluid;
import ivorius.psychedelicraft.block.entity.FlaskBlockEntity;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import net.minecraft.core.Direction;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/**
 * Created by lukas on 26.10.14.
 * Updated by Sollace on 3 Jan 2023
 */
public class FluidContraptionScreenHandler<T extends FlaskBlockEntity> extends AbstractContainerMenu {

    static final int INVENTORY_START = 2;
    static final int INVENTORY_END = 28;
    static final int HOTBAR_START = INVENTORY_END + 1;
    static final int HOTBAR_END = HOTBAR_START + 9;

    private final Resovoir tank;

    private final T blockEntity;

    @SuppressWarnings("unchecked")
    public FluidContraptionScreenHandler(MenuType<? extends FluidContraptionScreenHandler<T>> type, int syncId, Inventory inventory, BlockWithFluid.InteractionData data) {
        this(type, syncId, inventory, (T)inventory.player.level().getBlockEntity(data.pos()), data.side());
    }

    public FluidContraptionScreenHandler(MenuType<? extends FluidContraptionScreenHandler<T>> type, int syncId, Inventory inventory, T blockEntity, Direction direction) {
        super(type, syncId);
        this.tank = blockEntity.getTankOnSide(direction);
        this.blockEntity = blockEntity;
        addSlot(new InputSlot(blockEntity.ioInventory, 0, 21, 20));
        addSlot(new InputSlot(blockEntity.ioInventory, 1, 123, 61));

        for (int y = 0; y < 3; ++y) {
            for (int x = 0; x < 9; ++x) {
                addSlot(new Slot(inventory, x + y * 9 + 9, 8 + x * 18, 84 + y * 18));
            }
        }

        for (int x = 0; x < 9; ++x) {
            addSlot(new Slot(inventory, x, 8 + x * 18, 142));
        }

        addDataSlots(blockEntity.propertyDelegate);
    }

    public Resovoir getTank() {
        return tank;
    }

    public T getBlockEntity() {
        return blockEntity;
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        @Nullable
        Slot slot = slots.get(index);

        if (slot == null || !slot.hasItem()) {
            return ItemStack.EMPTY;
        }

        ItemStack stack = slot.getItem();
        ItemStack originalStack = stack.copy();

        if (index < INVENTORY_START) {
            if (!moveItemStackTo(stack, HOTBAR_START, HOTBAR_END, true)
                    && !moveItemStackTo(stack, INVENTORY_START, INVENTORY_END, false)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, originalStack);
        } if (index < HOTBAR_START) {
            if (!insertStack(stack, 0) && !insertStack(stack, 1)
                    && !moveItemStackTo(stack, HOTBAR_START, HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, originalStack);
        } else {
            if (!insertStack(stack, 0)
                    && !insertStack(stack, 1)
                    && !moveItemStackTo(stack, INVENTORY_START, INVENTORY_END, false)
                    && !moveItemStackTo(stack, HOTBAR_START, Math.min(index - 1, HOTBAR_END), false)
                    && !moveItemStackTo(stack, Math.max(index + 1, HOTBAR_START), HOTBAR_END, false)) {
                return ItemStack.EMPTY;
            }
            slot.onQuickCraft(stack, originalStack);
        }

        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }

        if (stack.getCount() == originalStack.getCount()) {
            return ItemStack.EMPTY;
        }

        slot.onTake(player, stack);

        return originalStack;
    }

    private boolean insertStack(ItemStack stack, int slotIndex) {
        @Nullable
        Slot slot = slots.get(slotIndex);
        if (slot == null) {
            return false;
        }
        ItemStack currentStack = slot.getItem();
        if (!currentStack.isEmpty() || !slot.mayPlace(stack)) {
            return false;
        }

        slot.setByPlayer(stack.split(Math.min(stack.getCount(), slot.getMaxStackSize(stack))));
        slot.setChanged();
        return true;
    }

    final class InputSlot extends Slot {
        public InputSlot(Container inventory, int slot, int x, int y) {
            super(inventory, slot, x, y);
        }

        @Override
        public ItemStack remove(int amount) {
            ItemStack stack = super.remove(amount);
            blockEntity.onContentsExternallyChanged(getContainerSlot());
            return stack;
        }

        @Override
        public ItemStack safeInsert(ItemStack stack, int count) {
            ItemStack removed = super.safeInsert(stack, count);
            blockEntity.onContentsExternallyChanged(getContainerSlot());
            return removed;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return container.canPlaceItem(getContainerSlot(), stack);
        }
    }

}
