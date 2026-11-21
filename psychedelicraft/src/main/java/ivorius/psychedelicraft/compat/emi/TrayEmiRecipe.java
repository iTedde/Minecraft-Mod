package ivorius.psychedelicraft.compat.emi;

import java.util.List;

import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.recipe.HardeningRecipe;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

/** Bunsen burner + tray: a fluid boiled down and hardened (upstream: {@code compat/tia/TrayEmiRecipe}). */
final class TrayEmiRecipe extends PSEmiRecipe {
    private static final EmiTexture PLUS = new EmiTexture(PSEmiPlugin.WIDGETS, 82, 0, 13, 13);

    private final RecipeHolder<HardeningRecipe> recipe;

    private final EmiIngredient base;
    private final List<EmiIngredient> impurities;
    private final List<EmiStack> output;

    TrayEmiRecipe(RecipeHolder<HardeningRecipe> recipe) {
        this.recipe = recipe;
        this.base = EmiUtil.toIngredient(recipe.value().coreFluid(), 1);
        this.impurities = recipe.value().solutions().stream().map(i -> EmiUtil.toIngredient(i, 1)).toList();
        this.output = List.of(EmiStack.of(recipe.value().getResultItem(EmiUtil.registries())));
    }

    @Override
    public PSEmiCategory getCategory() {
        return PSEmiCategory.TRAY;
    }

    @Override
    public ResourceLocation getId() {
        return recipe.id();
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return List.of(base);
    }

    @Override
    public List<EmiStack> getOutputs() {
        return output;
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return impurities;
    }

    @Override
    public RecipeHolder<?> getBackingRecipe() {
        return recipe;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int y = 4;
        widgets.addSlot(EmiStack.of(PSItems.BOTTLE), 10, y + 13).drawBack(false);
        widgets.addSlot(EmiStack.of(PSItems.BUNSEN_BURNER), 12, y + 23).drawBack(false).catalyst(true);
        addArrow(widgets, 30, y + 23);
        addAnimatedFlame(widgets, 30, y + 23, 900);

        int ingredientsX = 70;

        if (impurities.isEmpty()) {
            widgets.addSlot(base, ingredientsX, y + 22);
        } else {
            widgets.addSlot(base, ingredientsX, y + 4);
            widgets.addTexture(PLUS, ingredientsX + 3, y + 24);

            int gridWidth = impurities.size() * 20;
            int gridLeft = ingredientsX + 10 - gridWidth / 2;

            for (int i = 0; i < impurities.size(); i++) {
                widgets.addSlot(impurities.get(i), i % 3 * 18 + gridLeft, (i / 3 * 18) + y + 40);
            }
        }
        addArrow(widgets, 100, y + 23);
        widgets.addSlot(output.get(0), 170, y + 20).large(true).recipeContext(this);
        widgets.addFillingArrow(130, y + 23, Math.max(1, recipe.value().hardeningTime()) * 900);
        widgets.addSlot(EmiStack.of(PSItems.TRAY), 130, y + 23).drawBack(false).catalyst(true);
    }
}
