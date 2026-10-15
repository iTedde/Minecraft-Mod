package dev.craft.ic2.menu;

import java.util.Optional;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Output slot (IC2 {@code SlotOutput}); with {@code furnaceExperience} it behaves like the 1.4.7 {@code SlotFurnace}:
 * taking smelted items grants the furnace experience of their recipe.
 */
public class OutputSlot extends MachineSlot {
    private final Player player;
    private final boolean furnaceExperience;
    private int removeCount;

    public OutputSlot(IItemHandler handler, int index, int x, int y, Player player, boolean furnaceExperience) {
        super(handler, index, x, y);
        this.player = player;
        this.furnaceExperience = furnaceExperience;
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
    public void onTake(Player player, ItemStack stack) {
        checkTakeAchievements(stack);
        super.onTake(player, stack);
    }

    @Override
    public void onQuickCraft(ItemStack oldStack, ItemStack newStack) {
        int amount = newStack.getCount() - oldStack.getCount();
        if (amount > 0) {
            onQuickCraft(newStack, amount);
        }
    }

    @Override
    protected void onQuickCraft(ItemStack stack, int amount) {
        removeCount += amount;
        checkTakeAchievements(stack);
    }

    @Override
    protected void checkTakeAchievements(ItemStack stack) {
        stack.onCraftedBy(player.level(), player, removeCount);
        if (furnaceExperience && !player.level().isClientSide() && removeCount > 0) {
            float experience = getSmeltingExperience(player.level(), stack);
            int count = removeCount;
            int xp;
            if (experience == 0.0F) {
                xp = 0;
            } else if (experience < 1.0F) {
                xp = Mth.floor(count * experience);
                if (xp < Mth.ceil(count * experience) && Math.random() < count * experience - xp) {
                    xp++;
                }
            } else {
                xp = Mth.floor(count * experience);
            }
            ExperienceOrb.award((ServerLevel) player.level(), player.position(), xp);
        }
        removeCount = 0;
    }

    /** 1.4.7 {@code FurnaceRecipes.getExperience}: experience of the first smelting recipe with this result. */
    private static float getSmeltingExperience(Level level, ItemStack result) {
        Optional<Float> experience = level.getRecipeManager().getAllRecipesFor(RecipeType.SMELTING).stream()
                .map(RecipeHolder::value)
                .filter(recipe -> ItemStack.isSameItem(recipe.getResultItem(level.registryAccess()), result))
                .map(SmeltingRecipe::getExperience)
                .findFirst();
        return experience.orElse(0.0F);
    }
}
