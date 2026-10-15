package dev.craft.buildcraft.factory;

import java.util.ArrayList;
import java.util.List;

import dev.craft.buildcraft.fluid.BcFluids;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

/**
 * BuildCraft {@code RefineryRecipe}: up to two liquid ingredients, one result, MJ per operation and the delay in
 * ticks between operations. Matching ignores the amounts.
 */
public record RefineryRecipe(FluidStack ingredient1, FluidStack ingredient2, FluidStack result, int energy,
                             int delay) {
    private static final List<RefineryRecipe> RECIPES = new ArrayList<>();

    public static void register(RefineryRecipe recipe) {
        if (!RECIPES.contains(recipe)) {
            RECIPES.add(recipe);
        }
    }

    /** The recipes of the pack: BuildCraft oil → fuel (Forestry's biomass → biofuel is out of scope). */
    public static void registerDefaults() {
        register(new RefineryRecipe(new FluidStack(BcFluids.OIL.get(), 1), FluidStack.EMPTY,
                new FluidStack(BcFluids.FUEL.get(), 1), 12, 1));
    }

    @Nullable
    public static RefineryRecipe find(FluidStack liquid1, FluidStack liquid2) {
        for (RefineryRecipe recipe : RECIPES) {
            if (recipe.matches(liquid1, liquid2)) {
                return recipe;
            }
        }
        return null;
    }

    /** {@code matches}: a single ingredient may be in either tank, two must be in both (either order). */
    public boolean matches(FluidStack liquid1, FluidStack liquid2) {
        if (liquid1.isEmpty() && liquid2.isEmpty()) {
            return false;
        }
        if (!ingredient1.isEmpty() && !ingredient2.isEmpty() && (liquid1.isEmpty() || liquid2.isEmpty())) {
            return false;
        }
        if (ingredient2.isEmpty()) {
            return equal(ingredient1, liquid1) || equal(ingredient1, liquid2);
        }
        return equal(ingredient1, liquid1) && equal(ingredient2, liquid2)
                || equal(ingredient2, liquid1) && equal(ingredient1, liquid2);
    }

    /** {@code LiquidStack.isLiquidEqual}: same liquid, any amount. */
    private static boolean equal(FluidStack a, FluidStack b) {
        return !a.isEmpty() && !b.isEmpty() && FluidStack.isSameFluidSameComponents(a, b);
    }
}
