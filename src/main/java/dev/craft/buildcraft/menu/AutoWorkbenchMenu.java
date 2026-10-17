package dev.craft.buildcraft.menu;

import dev.craft.buildcraft.BcMenus;
import dev.craft.buildcraft.factory.AutoWorkbenchBlockEntity;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * BuildCraft {@code ContainerAutoWorkbench}. Menu slots: 0 result, 1–9 grid, 10–36 player inventory, 37–45 hotbar.
 * Taking the result uses up one item of every grid slot ({@code extractItem(true, true)}).
 */
public class AutoWorkbenchMenu extends AbstractContainerMenu {
    public static final int RESULT = 0;
    private static final int PLAYER = 10;
    private static final int HOTBAR = 37;
    private static final int END = 46;

    @Nullable
    private final AutoWorkbenchBlockEntity workbench;
    private final ItemStackHandler grid;
    private final SimpleContainer result = new SimpleContainer(1);
    private int seenChanges = -1;

    public AutoWorkbenchMenu(int id, Inventory inventory, AutoWorkbenchBlockEntity workbench) {
        this(id, inventory, workbench, workbench.getGrid());
    }

    public AutoWorkbenchMenu(int id, Inventory inventory) {
        this(id, inventory, null, AutoWorkbenchBlockEntity.createGrid(() -> {
        }));
    }

    private AutoWorkbenchMenu(int id, Inventory inventory, @Nullable AutoWorkbenchBlockEntity workbench,
                              ItemStackHandler grid) {
        super(BcMenus.AUTO_WORKBENCH.get(), id);
        this.workbench = workbench;
        this.grid = grid;
        addSlot(new ResultSlot(inventory.player, 124, 35));
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(new SlotItemHandler(grid, column + row * 3, 30 + column * 18, 17 + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
        updateResult();
    }

    private void updateResult() {
        if (workbench != null) {
            seenChanges = workbench.getChanges();
            result.setItem(0, workbench.findRecipe());
        }
    }

    @Override
    public void broadcastChanges() {
        if (workbench != null && workbench.getChanges() != seenChanges) {
            updateResult();
        }
        super.broadcastChanges();
    }

    /** Shift-click: result → inventory (from the back), inventory ↔ hotbar, grid → inventory. */
    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        boolean moved;
        if (index == RESULT) {
            // only whole results: a partly moved result would be replaced by the next one
            moved = room(stack) >= stack.getCount() && moveItemStackTo(stack, PLAYER, END, true);
        } else if (index >= PLAYER && index < HOTBAR) {
            moved = moveItemStackTo(stack, HOTBAR, END, false);
        } else if (index >= HOTBAR) {
            moved = moveItemStackTo(stack, PLAYER, HOTBAR, false);
        } else {
            moved = moveItemStackTo(stack, PLAYER, END, false);
        }
        if (!moved) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return original;
    }

    private int room(ItemStack stack) {
        int room = 0;
        for (int i = PLAYER; i < END; i++) {
            ItemStack other = slots.get(i).getItem();
            if (other.isEmpty()) {
                room += stack.getMaxStackSize();
            } else if (ItemStack.isSameItemSameComponents(other, stack)) {
                room += other.getMaxStackSize() - other.getCount();
            }
        }
        return room;
    }

    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot.index != RESULT && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public boolean stillValid(Player player) {
        return workbench == null || Container.stillValidBlockEntity(workbench, player);
    }

    /** {@code SlotAutoCrafting}: crafting event and achievements like the workbench, then the grid is used up. */
    private final class ResultSlot extends Slot {
        private final Player player;
        private int removeCount;

        private ResultSlot(Player player, int x, int y) {
            super(result, 0, x, y);
            this.player = player;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public ItemStack remove(int amount) {
            if (hasItem()) {
                removeCount += Math.min(amount, getItem().getCount());
            }
            return super.remove(amount);
        }

        @Override
        protected void onQuickCraft(ItemStack stack, int amount) {
            removeCount += amount;
            checkTakeAchievements(stack);
        }

        @Override
        protected void onSwapCraft(int numItemsCrafted) {
            removeCount += numItemsCrafted;
        }

        @Override
        protected void checkTakeAchievements(ItemStack stack) {
            if (removeCount > 0) {
                stack.onCraftedBy(player.level(), player, removeCount);
                SimpleContainer matrix = new SimpleContainer(AutoWorkbenchBlockEntity.SIZE);
                for (int i = 0; i < AutoWorkbenchBlockEntity.SIZE; i++) {
                    matrix.setItem(i, grid.getStackInSlot(i).copy());
                }
                EventHooks.firePlayerCraftingEvent(player, stack, matrix);
            }
            removeCount = 0;
        }

        @Override
        public void onTake(Player player, ItemStack stack) {
            checkTakeAchievements(stack);
            if (workbench != null) {
                workbench.extractItem(true, true);
                updateResult();
            }
        }

        @Override
        public boolean isFake() {
            return true;
        }
    }
}
