package dev.craft.compat.emi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dev.craft.core.client.RecipeCategories;
import dev.craft.forestry.FrItems;
import dev.craft.forestry.FrRecipes;
import dev.craft.forestry.machine.CarpenterBlockEntity;
import dev.craft.forestry.machine.FabricatorBlockEntity;
import dev.craft.forestry.recipe.CarpenterRecipe;
import dev.craft.forestry.recipe.FabricatorRecipe;
import dev.craft.forestry.recipe.FabricatorSmelting;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;

/** EMI categories of the Forestry machines (spec 16 §2.1): thermionic fabricator (melting + casting) and carpenter. */
final class ForestryEmiRecipes {
    static final EmiRecipeCategory FABRICATOR =
            new EmiRecipeCategory(RecipeCategories.FABRICATOR, EmiStack.of(FrItems.THERMIONIC_FABRICATOR.get()));
    static final EmiRecipeCategory CARPENTER =
            new EmiRecipeCategory(RecipeCategories.CARPENTER, EmiStack.of(FrItems.CARPENTER.get()));

    private ForestryEmiRecipes() {
    }

    static void register(EmiRegistry registry, Map<ResourceLocation, EmiRecipeCategory> categories) {
        for (EmiRecipeCategory category : List.of(FABRICATOR, CARPENTER)) {
            registry.addCategory(category);
            categories.put(category.getId(), category);
        }
        registry.addWorkstation(FABRICATOR, EmiStack.of(FrItems.THERMIONIC_FABRICATOR.get()));
        registry.addWorkstation(CARPENTER, EmiStack.of(FrItems.CARPENTER.get()));

        fabricator(registry);
        carpenter(registry);
    }

    private static EmiStack fluid(FluidStack stack) {
        return EmiStack.of(stack.getFluid(), stack.getAmount());
    }

    private static void fabricator(EmiRegistry registry) {
        Component heating = Component.translatable("emi.craft.fabricator.heating",
                FabricatorBlockEntity.ACTIVATION_ENERGY, EmiText.number(FabricatorBlockEntity.MAX_HEAT));
        for (FabricatorSmelting smelting : FabricatorSmelting.all()) {
            registry.addRecipe(new CraftEmiRecipe(FABRICATOR,
                    Ic2EmiRecipes.id("fabricator/smelting/" + Ic2EmiRecipes.path(smelting.resource())),
                    List.of(EmiStack.of(smelting.resource())), List.of(fluid(smelting.product())), 0, List.of(
                    Component.translatable("emi.craft.fabricator.melting_point",
                            EmiText.number(smelting.meltingPoint())), heating)));
        }
        for (RecipeHolder<FabricatorRecipe> holder : registry.getRecipeManager().getAllRecipesFor(FrRecipes.FABRICATOR.get())) {
            FabricatorRecipe recipe = holder.value();
            List<EmiIngredient> side = new ArrayList<>();
            side.add(fluid(recipe.getMolten()));
            recipe.getPlan().ifPresent(plan -> side.add(EmiIngredient.of(plan)));
            registry.addRecipe(new GridEmiRecipe(FABRICATOR, holder.id(), recipe.getPattern(), side,
                    EmiStack.of(recipe.getResult()), recipe.getPlan().isPresent()
                    ? List.of(Component.translatable("emi.craft.fabricator.plan")) : List.of()));
        }
    }

    private static void carpenter(EmiRegistry registry) {
        for (RecipeHolder<CarpenterRecipe> holder : registry.getRecipeManager().getAllRecipesFor(FrRecipes.CARPENTER.get())) {
            CarpenterRecipe recipe = holder.value();
            List<EmiIngredient> side = new ArrayList<>();
            if (!recipe.getLiquid().isEmpty()) {
                side.add(fluid(recipe.getLiquid()));
            }
            recipe.getBox().ifPresent(box -> side.add(EmiIngredient.of(box)));
            registry.addRecipe(new GridEmiRecipe(CARPENTER, holder.id(), recipe.getPattern(), side,
                    EmiStack.of(recipe.getResult()), List.of(Component.translatable("emi.craft.carpenter.steps",
                    recipe.getPackagingTime(), EmiText.number(
                            (long) recipe.getPackagingTime() * CarpenterBlockEntity.ACTIVATION_ENERGY)))));
        }
    }
}
