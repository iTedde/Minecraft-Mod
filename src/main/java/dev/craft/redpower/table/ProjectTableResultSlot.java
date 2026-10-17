package dev.craft.redpower.table;

import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.EventHooks;

/**
 * RedPower {@code SlotCraftRefill}: like the vanilla result slot (achievements, crafting event, recipe book), but the
 * ingredients are taken by {@link ProjectTableCrafting#consumeIngredients} on the server.
 */
public class ProjectTableResultSlot extends Slot {
    private final ProjectTableMenu menu;
    private final Player player;
    private int removeCount;

    public ProjectTableResultSlot(ProjectTableMenu menu, Player player, ResultContainer result, int x, int y) {
        super(result, 0, x, y);
        this.menu = menu;
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
        SimpleContainer grid = new SimpleContainer(menu.shadowGrid().toArray(ItemStack[]::new));
        if (removeCount > 0) {
            stack.onCraftedBy(player.level(), player, removeCount);
            EventHooks.firePlayerCraftingEvent(player, stack, grid);
        }
        ((ResultContainer) container).awardUsedRecipes(player, grid.getItems());
        removeCount = 0;
    }

    @Override
    public void onTake(Player player, ItemStack stack) {
        checkTakeAchievements(stack);
        if (!player.level().isClientSide()) {
            menu.craftTaken(player);
        }
    }

    @Override
    public boolean isFake() {
        return true;
    }
}
