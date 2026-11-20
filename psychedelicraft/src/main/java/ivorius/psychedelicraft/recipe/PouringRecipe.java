package ivorius.psychedelicraft.recipe;

import java.util.List;

import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Recipe for pouring fluid from one container to another.
 *
 * The left container in the grid must contain fluid to pour
 * The right container in the grid must either be empty or have the same fluid with room to pour into
 *
 * cup of water + empty bucket -> empty cup + bucket (partially filled) of water
 */
public class PouringRecipe extends CustomRecipe {
    public PouringRecipe(CraftingBookCategory category) {
        super(category);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.CRAFTING_POURING;
    }

    @Override
    public boolean matches(CraftingInput inventory, Level world) {
        List<ItemStack> recepticals = RecipeUtils
                .recepticals(inventory.items().stream())
                .toList();

        if (inventory.items().size() != 2 || recepticals.size() != 2) {
            return false;
        }

        ItemFluids to = ItemFluids.of(recepticals.get(1));
        ItemFluids from = ItemFluids.of(recepticals.get(0));

        return to.canCombine(from)
            && recepticals.get(0).getCount() == 1
            && Math.min(from.amount(), FluidCapacity.get(recepticals.get(1)) - to.amount()) > 0
            && FluidCapacity.getPercentage(recepticals.get(0)) > 0
            && FluidCapacity.getPercentage(recepticals.get(1)) < 1;
    }

    @Override
    public ItemStack assemble(CraftingInput inventory, Provider registries) {
        var recepticals = RecipeUtils.recepticals(inventory.items().stream()).toList();

        ItemFluids.Transaction to = ItemFluids.Transaction.begin(recepticals.get(1).copyWithCount(1));
        ItemFluids.Transaction from = ItemFluids.Transaction.begin(recepticals.get(0).copy());

        int maxMoved = Math.min(from.fluids().amount(), to.capacity() - to.fluids().amount());
        if (maxMoved <= 0) {
            return ItemStack.EMPTY;
        }
        to.deposit(from.withdraw(maxMoved));

        return to.toItemStack();
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inventory) {
        var recepticals = RecipeUtils.recepticalSlots(inventory).toList();
        if (recepticals.size() < 2) {
            return NonNullList.withSize(inventory.size(), ItemStack.EMPTY);
        }

        ItemFluids.Transaction to = ItemFluids.Transaction.begin(recepticals.get(1).content());
        ItemFluids.Transaction from = ItemFluids.Transaction.begin(recepticals.get(0).content());

        from.withdraw(Math.min(from.fluids().amount(), to.capacity() - to.fluids().amount()));

        NonNullList<ItemStack> remainder = NonNullList.withSize(inventory.size(), ItemStack.EMPTY);
        remainder.set(recepticals.get(0).position(), from.toItemStack());
        return remainder;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return (width * height) > 2;
    }
}
