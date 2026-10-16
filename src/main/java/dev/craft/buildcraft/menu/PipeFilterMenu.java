package dev.craft.buildcraft.menu;

import javax.annotation.Nullable;

import dev.craft.buildcraft.BcMenus;
import dev.craft.buildcraft.transport.ItemPipeBlockEntity;
import dev.craft.forestry.menu.PhantomSlot;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;

/**
 * BuildCraft {@code ContainerDiamondPipe} (6 rows of filters) and {@code ContainerEmeraldPipe} (one row). The filters
 * are phantom fields: a click sets a copy of the cursor stack with size 1, an empty cursor clears the field;
 * shift-click and dragging do nothing.
 */
public class PipeFilterMenu extends AbstractContainerMenu {
    @Nullable
    private final ItemPipeBlockEntity pipe;
    private final int filterCount;

    public PipeFilterMenu(int id, Inventory inventory, ItemPipeBlockEntity pipe) {
        this(id, inventory, pipe, pipe.getFilters());
    }

    public PipeFilterMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, null, new ItemStackHandler(buf.readVarInt()));
    }

    private PipeFilterMenu(int id, Inventory inventory, @Nullable ItemPipeBlockEntity pipe,
                           IItemHandlerModifiable filters) {
        super(BcMenus.PIPE_FILTERS.get(), id);
        this.pipe = pipe;
        filterCount = filters.getSlots();
        int rows = getRows();
        Runnable changed = pipe == null ? () -> { } : pipe::setChanged;
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new PhantomSlot(filters, column + row * 9, 8 + column * 18, 18 + row * 18, changed));
            }
        }
        int playerY = 32 + rows * 18;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, playerY + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, playerY + 58));
        }
    }

    /** 6 for the diamond pipe, 1 for the emerald pipe. */
    public int getRows() {
        return filterCount / 9;
    }

    @Override
    public void clicked(int slotId, int button, ClickType clickType, Player player) {
        if (slotId >= 0 && slotId < filterCount) {
            if (clickType == ClickType.PICKUP) {
                ItemStack carried = getCarried();
                slots.get(slotId).set(carried.isEmpty() ? ItemStack.EMPTY : carried.copyWithCount(1));
            }
            return;
        }
        super.clicked(slotId, button, clickType, player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    /** {@code isUseableByPlayer}: as long as the pipe exists. */
    @Override
    public boolean stillValid(Player player) {
        return pipe == null || pipe.getLevel() != null
                && pipe.getLevel().getBlockEntity(pipe.getBlockPos()) == pipe;
    }
}
