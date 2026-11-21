package ivorius.psychedelicraft.compat.emi;

import java.util.Collections;
import java.util.List;

import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import ivorius.psychedelicraft.block.entity.DryingTableBlockEntity;
import ivorius.psychedelicraft.client.screen.DryingTableScreen;
import ivorius.psychedelicraft.recipe.DryingRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import net.minecraft.world.item.crafting.RecipeHolder;

/** Drying table: nine times the input in full sunlight (upstream: {@code compat/tia/DryingEmiRecipe}). */
final class DryingEmiRecipe extends PSEmiRecipe {
    private static final EmiTexture SUN = new EmiTexture(DryingTableScreen.TEXTURE, 177, 20, 25, 25);
    private static final EmiTexture ARROW_FILL = new EmiTexture(DryingTableScreen.TEXTURE, 177, 42, 25, 20);

    private final RecipeHolder<DryingRecipe> recipe;
    private final List<EmiIngredient> input;
    private final List<EmiStack> output;

    DryingEmiRecipe(RecipeHolder<DryingRecipe> recipe) {
        this.recipe = recipe;
        this.input = Collections.nCopies(9, EmiIngredient.of(recipe.value().input()));
        this.output = List.of(EmiStack.of(recipe.value().getResultItem(EmiUtil.registries())));
    }

    @Override
    public PSEmiCategory getCategory() {
        return PSEmiCategory.DRYING_TABLE;
    }

    @Override
    public ResourceLocation getId() {
        return recipe.id();
    }

    @Override
    public List<EmiIngredient> getInputs() {
        return input;
    }

    @Override
    public List<EmiStack> getOutputs() {
        return output;
    }

    @Override
    public RecipeHolder<?> getBackingRecipe() {
        return recipe;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int y = 12;
        addArrow(widgets, 60, 18 + y);
        for (int i = 0; i < 9; i++) {
            widgets.addSlot(input.get(i), i % 3 * 18, (i / 3 * 18) + y);
        }
        widgets.addSlot(output.get(0), 92, 14 + y).large(true).recipeContext(this);
        widgets.addText(Component.translatable("psychedelicraft.recipe.experience", String.valueOf(recipe.value().experience())),
                58, 55, CommonColors.WHITE, true);

        widgets.addTexture(SUN, 95, 0);

        long cookingTime = DryingTableBlockEntity.getCookingTime(recipe.value().cookTime(), false);
        widgets.addAnimatedTexture(ARROW_FILL, 60, 18 + y, arrowMillis(cookingTime), true, false, false).tooltipText(List.of(
                Component.translatable("psychedelicraft.recipe.drying_time", EmiUtil.formatTicks((int) cookingTime))
        ));
    }
}
