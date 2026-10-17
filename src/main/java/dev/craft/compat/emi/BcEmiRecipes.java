package dev.craft.compat.emi;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dev.craft.buildcraft.BcItems;
import dev.craft.buildcraft.engine.CombustionEngineBlockEntity;
import dev.craft.buildcraft.engine.CombustionFuels;
import dev.craft.buildcraft.engine.EngineType;
import dev.craft.buildcraft.factory.RefineryRecipe;
import dev.craft.core.client.RecipeCategories;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;

/** EMI categories of BuildCraft (spec 16 §2.2): refinery and combustion engine; the stirling engine burns furnace fuel. */
final class BcEmiRecipes {
    /** Refinery amounts are per mB; the display scales them to one bucket. */
    private static final int BUCKET = 1000;

    static final EmiRecipeCategory REFINERY =
            new EmiRecipeCategory(RecipeCategories.REFINERY, EmiStack.of(BcItems.REFINERY.get()));
    static final EmiRecipeCategory COMBUSTION_ENGINE = new EmiRecipeCategory(RecipeCategories.COMBUSTION_ENGINE,
            EmiStack.of(BcItems.engine(EngineType.COMBUSTION).get()));

    private BcEmiRecipes() {
    }

    static void register(EmiRegistry registry, Map<ResourceLocation, EmiRecipeCategory> categories) {
        for (EmiRecipeCategory category : List.of(REFINERY, COMBUSTION_ENGINE)) {
            registry.addCategory(category);
            categories.put(category.getId(), category);
        }
        categories.put(RecipeCategories.FUEL, VanillaEmiRecipeCategories.FUEL);
        registry.addWorkstation(REFINERY, EmiStack.of(BcItems.REFINERY.get()));
        registry.addWorkstation(COMBUSTION_ENGINE, EmiStack.of(BcItems.engine(EngineType.COMBUSTION).get()));
        registry.addWorkstation(VanillaEmiRecipeCategories.FUEL, EmiStack.of(BcItems.engine(EngineType.STIRLING).get()));
        registry.addWorkstation(VanillaEmiRecipeCategories.CRAFTING, EmiStack.of(BcItems.AUTO_WORKBENCH.get()));

        refinery(registry);
        combustionEngine(registry);
    }

    private static String path(Fluid fluid) {
        ResourceLocation key = BuiltInRegistries.FLUID.getKey(fluid);
        return key.getNamespace() + "/" + key.getPath();
    }

    private static EmiStack bucket(FluidStack stack) {
        return EmiStack.of(stack.getFluid(), (long) stack.getAmount() * BUCKET);
    }

    private static void refinery(EmiRegistry registry) {
        for (RefineryRecipe recipe : RefineryRecipe.all()) {
            List<EmiIngredient> inputs = new ArrayList<>();
            inputs.add(bucket(recipe.ingredient1()));
            if (!recipe.ingredient2().isEmpty()) {
                inputs.add(bucket(recipe.ingredient2()));
            }
            String name = path(recipe.ingredient1().getFluid())
                    + (recipe.ingredient2().isEmpty() ? "" : "/" + path(recipe.ingredient2().getFluid()));
            registry.addRecipe(new CraftEmiRecipe(REFINERY, Ic2EmiRecipes.id("refinery/" + name), inputs,
                    List.of(bucket(recipe.result())), 0, List.of(
                    Component.translatable("emi.craft.refinery.energy", EmiText.number((long) recipe.energy() * BUCKET),
                            recipe.energy()),
                    Component.translatable("emi.craft.refinery.rate", recipe.result().getAmount(), recipe.delay()))));
        }
    }

    private static void combustionEngine(EmiRegistry registry) {
        CombustionFuels.fuels().forEach((fluid, fuel) -> {
            int ticks = fuel.totalBurningTime();
            registry.addRecipe(new CraftEmiRecipe(COMBUSTION_ENGINE,
                    Ic2EmiRecipes.id("combustion_engine/fuel/" + path(fluid)),
                    List.of(EmiStack.of(fluid, BUCKET)), List.of(), ticks, List.of(
                    Component.translatable("emi.craft.combustion_engine.fuel", EmiText.decimal(fuel.powerPerCycle()),
                            EmiText.seconds(ticks), EmiText.number((long) (fuel.powerPerCycle() * ticks))))));
        });
        CombustionFuels.coolants().forEach((fluid, cooling) -> registry.addRecipe(new CraftEmiRecipe(
                COMBUSTION_ENGINE, Ic2EmiRecipes.id("combustion_engine/coolant/" + path(fluid)),
                List.of(EmiStack.of(fluid, BUCKET)), List.of(), 0, List.of(
                Component.translatable("emi.craft.combustion_engine.coolant", EmiText.decimal(cooling),
                        EmiText.number(CombustionEngineBlockEntity.COOLANT_THRESHOLD)),
                Component.translatable("emi.craft.combustion_engine.ice")))));
    }
}
