package dev.craft.redpower.table;

import static dev.craft.redpower.table.ProjectTableBlockEntity.GRID_SIZE;
import static dev.craft.redpower.table.ProjectTableBlockEntity.PLAN_SLOT;
import static dev.craft.redpower.table.ProjectTableBlockEntity.STORAGE_SIZE;
import static dev.craft.redpower.table.ProjectTableBlockEntity.STORAGE_START;

import java.util.List;

import dev.craft.redpower.RpMenus;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.SlotItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * RedPower {@code ContainerAdvBench}. Menu slots: 0–8 grid, 9 plan, 10 result, 11–28 storage, 29–55 player inventory,
 * 56–64 hotbar. The result is computed on the server whenever the table's inventory changed.
 */
public class ProjectTableMenu extends AbstractContainerMenu {
    public static final int RESULT = 10;
    private static final int STORAGE = 11;
    private static final int PLAYER = STORAGE + STORAGE_SIZE;
    private static final int END = PLAYER + 36;
    /** Menu button: write the blank plan. */
    public static final int BUTTON_WRITE_PLAN = 0;

    @Nullable
    private final ProjectTableBlockEntity table;
    private final ItemStackHandler inventory;
    private final ResultContainer result = new ResultContainer();
    private final ProjectTableResultSlot resultSlot;
    private final DataSlot satisfyMask = DataSlot.standalone();
    private int seenChanges = -1;

    public ProjectTableMenu(int id, Inventory playerInventory, ProjectTableBlockEntity table) {
        this(id, playerInventory, table, table.getInventory());
    }

    public ProjectTableMenu(int id, Inventory playerInventory) {
        this(id, playerInventory, null, ProjectTableBlockEntity.createInventory(() -> {
        }));
    }

    private ProjectTableMenu(int id, Inventory playerInventory, @Nullable ProjectTableBlockEntity table,
                             ItemStackHandler inventory) {
        super(RpMenus.PROJECT_TABLE.get(), id);
        this.table = table;
        this.inventory = inventory;
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 3; column++) {
                addSlot(new SlotItemHandler(inventory, column + row * 3, 48 + column * 18, 18 + row * 18));
            }
        }
        addSlot(new SlotItemHandler(inventory, PLAN_SLOT, 17, 36));
        resultSlot = new ProjectTableResultSlot(this, playerInventory.player, result, 143, 36);
        addSlot(resultSlot);
        for (int row = 0; row < 2; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new SlotItemHandler(inventory, STORAGE_START + column + row * 9, 8 + column * 18,
                        90 + row * 18));
            }
        }
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                addSlot(new Slot(playerInventory, column + row * 9 + 9, 8 + column * 18, 140 + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            addSlot(new Slot(playerInventory, column, 8 + column * 18, 198));
        }
        addDataSlot(satisfyMask);
        updateResult();
    }

    /** The plan items of the plan slot, or {@code null} without a written plan (client side for the ghost items). */
    @Nullable
    public ItemStack[] getPlanItems() {
        return ProjectTableCrafting.planItems(inventory);
    }

    /** Bit per grid slot that is satisfied (synced; red ghost items for the others). */
    public int getSatisfyMask() {
        return satisfyMask.get();
    }

    List<ItemStack> shadowGrid() {
        return ProjectTableCrafting.shadowGrid(inventory);
    }

    /** {@code onCraftMatrixChanged}: new result from the shadow grid, only when every slot is satisfied. */
    private void updateResult() {
        if (table == null || table.getLevel() == null) {
            return;
        }
        seenChanges = table.getChanges();
        Level level = table.getLevel();
        satisfyMask.set(ProjectTableCrafting.satisfyMask(inventory));
        ItemStack stack = ProjectTableCrafting.findRecipe(inventory, level).map(recipe -> {
            result.setRecipeUsed(recipe);
            return ProjectTableCrafting.assemble(recipe, inventory, level);
        }).orElse(ItemStack.EMPTY);
        result.setItem(0, stack);
    }

    void craftTaken(Player player) {
        ProjectTableCrafting.consumeIngredients(inventory, player);
        updateResult();
    }

    @Override
    public void broadcastChanges() {
        if (table != null && table.getChanges() != seenChanges) {
            updateResult();
        }
        super.broadcastChanges();
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id != BUTTON_WRITE_PLAN || table == null) {
            return false;
        }
        return ProjectTableCrafting.writePlan(inventory, result.getItem(0));
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        if (index == RESULT) {
            if (!player.level().isClientSide()) {
                mergeCrafting(player);
            }
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        boolean moved;
        if (index < GRID_SIZE) {
            moved = moveItemStackTo(stack, STORAGE, PLAYER, false);
        } else if (index < PLAYER) {
            moved = moveItemStackTo(stack, PLAYER, END, true);
        } else {
            moved = moveItemStackTo(stack, STORAGE, PLAYER, false);
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

    /**
     * {@code mergeCrafting}: crafts again and again into the player inventory while the result fits, up to the
     * result's stack size (16 for unstackable results); stops early when the next craft would be the last one.
     */
    private void mergeCrafting(Player player) {
        ItemStack stack = resultSlot.getItem();
        ItemStack craftAs = stack.copy();
        int limit = craftAs.getMaxStackSize() == 1 ? 16 : craftAs.getMaxStackSize();
        int crafted = 0;
        while (canFit(stack)) {
            crafted += stack.getCount();
            ItemStack taken = resultSlot.remove(stack.getCount());
            fitItem(taken.copy());
            resultSlot.onTake(player, taken);
            if (crafted >= limit || ProjectTableCrafting.isLastUse(inventory)) {
                return;
            }
            stack = resultSlot.getItem();
            if (stack.isEmpty() || !ItemStack.isSameItemSameComponents(stack, craftAs)) {
                return;
            }
        }
    }

    /** {@code canFit}: an empty player slot, or enough room in stacks of the same item. */
    private boolean canFit(ItemStack stack) {
        int room = 0;
        for (int i = PLAYER; i < END; i++) {
            ItemStack other = slots.get(i).getItem();
            if (other.isEmpty()) {
                return true;
            }
            if (ItemStack.isSameItemSameComponents(other, stack)) {
                room += other.getMaxStackSize() - other.getCount();
                if (room >= stack.getCount()) {
                    return true;
                }
            }
        }
        return false;
    }

    /** {@code fitItem}: fills stacks of the same item first, the rest goes to the first empty slot. */
    private void fitItem(ItemStack stack) {
        if (stack.isStackable()) {
            for (int i = PLAYER; i < END && !stack.isEmpty(); i++) {
                Slot slot = slots.get(i);
                ItemStack other = slot.getItem();
                if (!other.isEmpty() && ItemStack.isSameItemSameComponents(other, stack)) {
                    int count = Math.min(stack.getCount(), other.getMaxStackSize() - other.getCount());
                    if (count > 0) {
                        stack.shrink(count);
                        other.grow(count);
                        slot.setChanged();
                    }
                }
            }
        }
        if (stack.isEmpty()) {
            return;
        }
        for (int i = PLAYER; i < END; i++) {
            Slot slot = slots.get(i);
            if (!slot.hasItem()) {
                slot.setByPlayer(stack);
                return;
            }
        }
    }

    /** Double-click collecting never takes the result (it would skip the crafting). */
    @Override
    public boolean canTakeItemForPickAll(ItemStack stack, Slot slot) {
        return slot != resultSlot && super.canTakeItemForPickAll(stack, slot);
    }

    @Override
    public boolean stillValid(Player player) {
        return table == null || Container.stillValidBlockEntity(table, player);
    }
}
