package dev.craft.compat.emi;

import java.util.List;

import dev.craft.buildcraft.BcMenus;
import dev.craft.buildcraft.menu.AutoWorkbenchMenu;
import dev.craft.redpower.RpMenus;
import dev.craft.redpower.table.ProjectTableMenu;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipe;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.recipe.handler.StandardRecipeHandler;
import net.minecraft.world.inventory.Slot;

/**
 * EMI recipe fill ("+" button) for the 3×3 crafting grids of the Project Table and the Automatic Workbench.
 * Ingredients come from the table storage (Project Table only) and the player inventory.
 */
final class CraftingTableEmiHandlers {
    private CraftingTableEmiHandlers() {
    }

    static void register(EmiRegistry registry) {
        registry.addRecipeHandler(RpMenus.PROJECT_TABLE.get(), new StandardRecipeHandler<ProjectTableMenu>() {
            @Override
            public List<Slot> getInputSources(ProjectTableMenu menu) {
                return menu.slots.subList(ProjectTableMenu.STORAGE, ProjectTableMenu.END);
            }

            @Override
            public List<Slot> getCraftingSlots(ProjectTableMenu menu) {
                return menu.slots.subList(0, 9);
            }

            @Override
            public Slot getOutputSlot(ProjectTableMenu menu) {
                return menu.slots.get(ProjectTableMenu.RESULT);
            }

            @Override
            public boolean supportsRecipe(EmiRecipe recipe) {
                return isCrafting(recipe);
            }
        });
        registry.addRecipeHandler(BcMenus.AUTO_WORKBENCH.get(), new StandardRecipeHandler<AutoWorkbenchMenu>() {
            @Override
            public List<Slot> getInputSources(AutoWorkbenchMenu menu) {
                return menu.slots.subList(AutoWorkbenchMenu.PLAYER, AutoWorkbenchMenu.END);
            }

            @Override
            public List<Slot> getCraftingSlots(AutoWorkbenchMenu menu) {
                return menu.slots.subList(AutoWorkbenchMenu.RESULT + 1, AutoWorkbenchMenu.PLAYER);
            }

            @Override
            public Slot getOutputSlot(AutoWorkbenchMenu menu) {
                return menu.slots.get(AutoWorkbenchMenu.RESULT);
            }

            @Override
            public boolean supportsRecipe(EmiRecipe recipe) {
                return isCrafting(recipe);
            }
        });
    }

    private static boolean isCrafting(EmiRecipe recipe) {
        return recipe.getCategory() == VanillaEmiRecipeCategories.CRAFTING && recipe.supportsRecipeTree();
    }
}
