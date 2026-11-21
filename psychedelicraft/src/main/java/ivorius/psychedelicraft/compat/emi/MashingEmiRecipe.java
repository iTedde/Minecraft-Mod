package ivorius.psychedelicraft.compat.emi;

import java.util.List;

import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.recipe.MashingRecipe;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

/** Mash tub: solids soaked in a base fluid (upstream: {@code compat/tia/MashingEmiRecipe}). */
final class MashingEmiRecipe extends PSEmiRecipe {
    private static final ResourceLocation TEXTURE = Psychedelicraft.id("textures/gui/wooden_vat.png");
    private static final EmiTexture IN_VAT = new EmiTexture(TEXTURE, 57, 15, 60, 44);
    private static final EmiTexture OUT_VAT = new EmiTexture(TEXTURE, 28, 10, 30, 20, 30, 20, 128, 128);

    private final RecipeHolder<MashingRecipe> recipe;
    private final List<EmiIngredient> input;

    private final ItemFluids outputFluid;
    private final EmiStack output;

    private final List<ItemFluids> baseFluids;
    private final EmiIngredient fluidIngredient;

    MashingEmiRecipe(RecipeHolder<MashingRecipe> recipe) {
        this.recipe = recipe;
        this.input = EmiUtil.grouped(recipe.value().getIngredients().stream());
        this.outputFluid = recipe.value().result().ofAmount(FluidVolumes.VAT);
        this.output = EmiUtil.toEmiStack(outputFluid);
        this.baseFluids = EmiUtil.getMatchingFluids(recipe.value().baseFluid(), FluidVolumes.VAT);
        this.fluidIngredient = EmiIngredient.of(baseFluids.stream().map(EmiUtil::toEmiStack).filter(s -> !s.isEmpty()).toList());
    }

    @Override
    public PSEmiCategory getCategory() {
        return PSEmiCategory.VAT;
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
        return List.of(output);
    }

    @Override
    public List<EmiIngredient> getCatalysts() {
        return List.of(fluidIngredient);
    }

    @Override
    public RecipeHolder<?> getBackingRecipe() {
        return recipe;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        int x = 7;
        int y = 0;

        widgets.addFillingArrow(60 + x, 21 + y, 5000).tooltipText(List.of(
                Component.translatable("gui.psychedelicraft.recipe.stewing_time", recipe.value().stewTime())));
        var inBox = FluidBoxWidget.create(baseFluids, FluidVolumes.VAT, x, 17 + y, 60, 31, widgets);
        widgets.addTexture(IN_VAT, x, 5 + y);

        widgets.addSlot(fluidIngredient, 2, getDisplayHeight() - 16 - 4).catalyst(true);

        x += 85;
        y += 15;

        var outBox = FluidBoxWidget.create(outputFluid, FluidVolumes.VAT, x, 8 + y, 30, 16, widgets);
        outBox.addExclusion(widgets.addSlot(output, 6 + x, 8 + y).drawBack(false).recipeContext(this));
        widgets.addTexture(OUT_VAT, x, 5 + y);

        int ingredientsStackCount = input.size();

        int maxIngredientsWidth = 3 * 16;
        int maxIngredientsHeight = 2 * 16;

        int ingredientsLeft = 9 + (maxIngredientsWidth - (Math.min(3, ingredientsStackCount) * 16)) / 2;
        int ingredientsTop = 2 + (maxIngredientsHeight - (ingredientsStackCount / 4) * 8) / 2;

        for (int i = 0; i < ingredientsStackCount; i++) {
            int row = i / 3;
            int col = i % 3;
            inBox.addExclusion(widgets.addSlot(input.get(i), col * 16 + ingredientsLeft, row * 16 + ingredientsTop + (col % 2) * 8)
                    .drawBack(false));
        }
    }
}
