package dev.craft.enderstorage;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * EnderStorage {@code EnderStorageRecipe} (spec 27 §6.2): dyes in the row above an ender chest or tank set the colour
 * of their column (left, middle, right of the chest), empty columns keep theirs. Deviation: the grid comes trimmed,
 * so the columns count from the chest, and other items in the grid prevent the recipe instead of being used up.
 */
public class EnderStorageRecolourRecipe extends CustomRecipe {
    public EnderStorageRecolourRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return !assemble(input).isEmpty();
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return assemble(input);
    }

    private static ItemStack assemble(CraftingInput input) {
        int chestX = -1;
        int chestY = -1;
        for (int y = 0; y < input.height(); y++) {
            for (int x = 0; x < input.width(); x++) {
                if (input.getItem(x, y).getItem() instanceof EnderStorageItem) {
                    if (chestX >= 0) {
                        return ItemStack.EMPTY;
                    }
                    chestX = x;
                    chestY = y;
                }
            }
        }
        if (chestX < 0 || chestY < 1) {
            return ItemStack.EMPTY;
        }
        ItemStack chest = input.getItem(chestX, chestY);
        Frequency frequency = chest.getOrDefault(EsDataComponents.FREQUENCY.get(), Frequency.GLOBAL);
        boolean dyed = false;
        for (int y = 0; y < input.height(); y++) {
            for (int x = 0; x < input.width(); x++) {
                ItemStack stack = input.getItem(x, y);
                if (stack.isEmpty() || x == chestX && y == chestY) {
                    continue;
                }
                DyeColor dye = DyeColor.getColor(stack);
                int button = x - chestX + 1;
                if (dye == null || y != chestY - 1 || button < 0 || button > 2) {
                    return ItemStack.EMPTY;
                }
                frequency = frequency.withColour(button, dye.getId());
                dyed = true;
            }
        }
        if (!dyed) {
            return ItemStack.EMPTY;
        }
        ItemStack result = chest.copyWithCount(1);
        result.set(EsDataComponents.FREQUENCY.get(), frequency);
        return result;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return EsRecipes.RECOLOUR.get();
    }
}
