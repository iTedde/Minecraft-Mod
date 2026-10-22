package dev.craft.compat.emi;

import java.util.HashMap;
import java.util.Map;

import dev.craft.core.client.ExtraAreas;
import dev.craft.core.client.RecipeArea;
import dev.craft.core.client.RecipeCategories;
import dev.craft.gregtech.GtItems;
import dev.craft.redpower.RpItems;
import dev.emi.emi.api.EmiApi;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;
import net.minecraft.resources.ResourceLocation;

/**
 * EMI integration (spec 16 §2): recipe categories for the machines, hidden helper items and the GUI recipe areas.
 * EMI is optional; no EMI class is used outside this package.
 */
@EmiEntrypoint
public class CraftEmiPlugin implements EmiPlugin {
    private static final Map<ResourceLocation, EmiRecipeCategory> CATEGORIES = new HashMap<>();

    @Override
    public void register(EmiRegistry registry) {
        CATEGORIES.clear();
        CATEGORIES.put(RecipeCategories.SMELTING, VanillaEmiRecipeCategories.SMELTING);
        Ic2EmiRecipes.register(registry, CATEGORIES);
        AdvancedMachinesEmiRecipes.register(registry, CATEGORIES);
        GtEmiRecipes.register(registry, CATEGORIES);
        BcEmiRecipes.register(registry, CATEGORIES);
        ForestryEmiRecipes.register(registry, CATEGORIES);
        PowerPlantEmiRecipes.register(registry, CATEGORIES);
        TeEmiRecipes.register(registry, CATEGORIES);
        registry.addWorkstation(VanillaEmiRecipeCategories.CRAFTING, EmiStack.of(RpItems.PROJECT_TABLE.get()));
        CraftingTableEmiHandlers.register(registry);
        MultiblockEmiInfo.register(registry);
        MultiblockEmiInfo.registerTerraformer(registry);
        MultiblockEmiInfo.registerBrewing(registry);
        SourceEmiInfo.register(registry);

        // block items only shown in holo slots of the GT electrolyzer and centrifuge
        registry.removeEmiStacks(EmiStack.of(GtItems.WATER.get()));
        registry.removeEmiStacks(EmiStack.of(GtItems.LAVA.get()));

        // Forestry ledgers to the right of the GUI
        registry.addGenericExclusionArea((screen, consumer) -> {
            if (screen instanceof ExtraAreas areas) {
                areas.addExtraAreas(rect -> consumer.accept(
                        new Bounds(rect.getX(), rect.getY(), rect.getWidth(), rect.getHeight())));
            }
        });

        RecipeArea.setViewer(id -> {
            EmiRecipeCategory category = CATEGORIES.get(id);
            if (category == null) {
                return false;
            }
            EmiApi.displayRecipeCategory(category);
            return true;
        });
    }
}
