package ivorius.psychedelicraft.compat.emi;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import ivorius.psychedelicraft.PSTags;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.recipe.PSRecipes;
import net.minecraft.world.level.ItemLike;

/**
 * Recipe categories with their workstations and recipe generators (upstream: {@code compat/tia/RecipeCategory}).
 */
final class PSEmiCategory extends EmiRecipeCategory {
    private static final List<PSEmiCategory> ALL = new ArrayList<>();

    static final PSEmiCategory DRYING_TABLE = register("drying_table", PSBlocks.DRYING_TABLE, 118, 74,
            (category, registry) -> registry.getRecipeManager().getAllRecipesFor(PSRecipes.DRYING_TYPE)
                    .forEach(recipe -> registry.addRecipe(new DryingEmiRecipe(recipe))));
    static final PSEmiCategory VAT = register("wooden_vat", PSBlocks.MASH_TUB, 130, 84,
            (category, registry) -> registry.getRecipeManager().getAllRecipesFor(PSRecipes.MASHING_TYPE)
                    .forEach(recipe -> registry.addRecipe(new MashingEmiRecipe(recipe))));
    static final PSEmiCategory BARREL = register("barrel", PSBlocks.OAK_BARREL, EmiIngredient.of(PSTags.Items.BARRELS), 130, 70,
            DrawingFluidEmiRecipe.generate(FluidVolumes.BARREL));
    static final PSEmiCategory DISTILLERY = register("distillery", PSBlocks.DISTILLERY, 130, 70,
            DrawingFluidEmiRecipe.generate(FluidVolumes.FLASK));
    static final PSEmiCategory FLASK = register("flask", PSBlocks.FLASK, 130, 70,
            DrawingFluidEmiRecipe.generate(FluidVolumes.FLASK));
    static final PSEmiCategory TRAY = register("tray", PSBlocks.TRAY, 200, 70,
            (category, registry) -> registry.getRecipeManager().getAllRecipesFor(PSRecipes.TRAY)
                    .forEach(recipe -> registry.addRecipe(new TrayEmiRecipe(recipe))));
    static final PSEmiCategory PREPARATION = register("fluid_preparation", PSItems.BOTTLE, EmiIngredient.of(List.of(
            EmiIngredient.of(PSTags.Items.BARRELS),
            EmiStack.of(PSItems.DISTILLERY),
            EmiStack.of(PSItems.MASH_TUB),
            EmiStack.of(PSItems.BUNSEN_BURNER)
    )), 260, 20, FluidStagesEmiRecipe.generate());

    final EmiIngredient stations;
    final int width;
    final int height;
    private final BiConsumer<PSEmiCategory, EmiRegistry> recipes;

    private PSEmiCategory(String name, ItemLike icon, EmiIngredient stations, int width, int height,
            BiConsumer<PSEmiCategory, EmiRegistry> recipes) {
        super(Psychedelicraft.id(name), EmiStack.of(icon));
        this.stations = stations;
        this.width = width;
        this.height = height;
        this.recipes = recipes;
    }

    private static PSEmiCategory register(String name, ItemLike station, int width, int height,
            BiConsumer<PSEmiCategory, EmiRegistry> recipes) {
        return register(name, station, EmiStack.of(station), width, height, recipes);
    }

    private static PSEmiCategory register(String name, ItemLike icon, EmiIngredient stations, int width, int height,
            BiConsumer<PSEmiCategory, EmiRegistry> recipes) {
        PSEmiCategory category = new PSEmiCategory(name, icon, stations, width, height, recipes);
        ALL.add(category);
        return category;
    }

    static void bootstrap(EmiRegistry registry) {
        for (PSEmiCategory category : ALL) {
            registry.addCategory(category);
            registry.addWorkstation(category, category.stations);
            try {
                category.recipes.accept(category, registry);
            } catch (Throwable t) {
                Psychedelicraft.LOGGER.error("Error registering EMI recipes for category {}", category.getId(), t);
            }
        }
    }
}
