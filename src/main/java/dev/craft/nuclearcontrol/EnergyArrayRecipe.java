package dev.craft.nuclearcontrol;

import java.util.ArrayList;
import java.util.List;

import dev.craft.ic2.Ic2Items;
import dev.craft.nuclearcontrol.card.CardData;
import dev.craft.nuclearcontrol.item.EnergyArrayCardItem;
import dev.craft.nuclearcontrol.item.EnergySensorCardItem;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * {@code StorageArrayRecipe} (spec 46 §5.3), shapeless: 2–6 energy sensor cards → energy array card; array card plus
 * energy sensor cards → array card with more targets (at most 6); an array card alone → {@code 2 × targets}
 * electronic circuits. Any other item in the grid prevents the recipe.
 */
public class EnergyArrayRecipe extends CustomRecipe {
    public EnergyArrayRecipe(CraftingBookCategory category) {
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

    public static ItemStack assemble(CraftingInput input) {
        List<ItemStack> cards = new ArrayList<>();
        ItemStack array = ItemStack.EMPTY;
        int arrayCount = 0;
        for (int slot = 0; slot < input.size(); slot++) {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (stack.getItem() instanceof EnergySensorCardItem) {
                cards.add(stack);
            } else if (stack.getItem() instanceof EnergyArrayCardItem) {
                array = stack;
                arrayCount++;
            } else {
                return ItemStack.EMPTY;
            }
        }
        int cardCount = cards.size();
        if (cardCount >= 2 && cardCount <= EnergyArrayCardItem.MAX_CARDS && arrayCount == 0) {
            ItemStack result = new ItemStack(NcItems.ENERGY_ARRAY_LOCATION_CARD.get());
            CardData data = CardData.of(result);
            EnergyArrayCardItem.addCards(data, cards);
            data.save(result);
            return result;
        }
        if (arrayCount != 1) {
            return ItemStack.EMPTY;
        }
        int existing = EnergyArrayCardItem.getCardCount(CardData.of(array));
        if (cardCount == 0) {
            return existing > 0 ? new ItemStack(Ic2Items.ELECTRONIC_CIRCUIT.get(), 2 * existing) : ItemStack.EMPTY;
        }
        if (existing + cardCount <= EnergyArrayCardItem.MAX_CARDS) {
            ItemStack result = array.copyWithCount(1);
            CardData data = CardData.of(result);
            EnergyArrayCardItem.addCards(data, cards);
            data.save(result);
            return result;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= 2;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return NcRecipes.ENERGY_ARRAY.get();
    }
}
