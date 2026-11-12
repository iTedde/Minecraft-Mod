package dev.craft.nuclearcontrol.menu;

import dev.craft.nuclearcontrol.NcMenus;
import dev.craft.nuclearcontrol.block.InfoPanelBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/**
 * {@code ContainerInfoPanel}: card, range and colour upgrade slots at x 8, y 42/60/78; player inventory at y 108.
 * The screen reads the settings from the synced block entity.
 */
public class InfoPanelMenu extends NcMenu {
    private static final int OWN_SLOTS = 3;

    /** Server. */
    public InfoPanelMenu(int id, Inventory inventory, InfoPanelBlockEntity panel) {
        this(id, inventory, panel.getBlockPos(), panel.getInventory());
    }

    /** Client. */
    public InfoPanelMenu(int id, Inventory inventory, BlockPos pos) {
        this(id, inventory, pos, new ItemStackHandler(OWN_SLOTS) {
            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return InfoPanelBlockEntity.isItemValid(slot, stack);
            }
        });
    }

    private InfoPanelMenu(int id, Inventory inventory, BlockPos pos, IItemHandler items) {
        super(NcMenus.INFORMATION_PANEL.get(), id, pos);
        for (int slot = 0; slot < OWN_SLOTS; slot++) {
            addSlot(new SlotItemHandler(items, slot, 8, 42 + slot * 18));
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 108 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 166));
        }
    }

    @Override
    protected boolean isOwnBlockEntity(BlockEntity blockEntity) {
        return blockEntity instanceof InfoPanelBlockEntity;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot source = slots.get(index);
        if (!source.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = source.getItem();
        ItemStack original = stack.copy();
        if (index < OWN_SLOTS) {
            moveItemStackTo(stack, OWN_SLOTS, slots.size(), true);
        } else {
            moveItemStackTo(stack, 0, OWN_SLOTS, false);
        }
        source.setChanged();
        return stack.getCount() == original.getCount() ? ItemStack.EMPTY : original;
    }
}
