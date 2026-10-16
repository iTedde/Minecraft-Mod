package dev.craft.compat.emi;

import java.util.ArrayList;
import java.util.List;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.ShapedRecipePattern;

/**
 * Display of the Forestry grid machines: a shaped 3×3 pattern, a column of side slots (plan, liquid, box), an arrow,
 * the result and text lines below.
 */
class GridEmiRecipe extends BasicEmiRecipe {
    private static final int SLOT = CraftEmiRecipe.SLOT;
    private static final int GRID = 3 * SLOT;
    private static final int SIDE_X = GRID + 4;
    private static final int ARROW_X = SIDE_X + SLOT + 4;
    private static final int OUTPUT_X = ARROW_X + CraftEmiRecipe.ARROW_WIDTH + 4;

    private final List<EmiIngredient> grid = new ArrayList<>();
    private final List<EmiIngredient> side;
    private final List<Component> lines;

    GridEmiRecipe(EmiRecipeCategory category, ResourceLocation id, ShapedRecipePattern pattern,
                  List<EmiIngredient> side, EmiStack result, List<Component> lines) {
        super(category, id, 0, 0);
        for (int i = 0; i < 9; i++) {
            int x = i % 3;
            int y = i / 3;
            grid.add(x < pattern.width() && y < pattern.height()
                    ? EmiIngredient.of(pattern.ingredients().get(y * pattern.width() + x)) : EmiStack.EMPTY);
        }
        this.side = side;
        this.lines = lines;
        this.inputs = new ArrayList<>();
        grid.stream().filter(ingredient -> !ingredient.isEmpty()).forEach(inputs::add);
        inputs.addAll(side);
        this.outputs = List.of(result);
        int textWidth = 0;
        for (Component line : lines) {
            textWidth = Math.max(textWidth, Minecraft.getInstance().font.width(line));
        }
        this.width = Math.max(OUTPUT_X + SLOT, textWidth);
        this.height = GRID + (lines.isEmpty() ? 0 : 2 + lines.size() * CraftEmiRecipe.LINE_HEIGHT);
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        for (int i = 0; i < grid.size(); i++) {
            widgets.addSlot(grid.get(i), i % 3 * SLOT, i / 3 * SLOT);
        }
        for (int i = 0; i < side.size(); i++) {
            widgets.addSlot(side.get(i), SIDE_X, i * SLOT);
        }
        widgets.addTexture(EmiTexture.EMPTY_ARROW, ARROW_X, SLOT + 1);
        widgets.addSlot(outputs.get(0), OUTPUT_X, SLOT).recipeContext(this);
        for (int i = 0; i < lines.size(); i++) {
            widgets.addText(lines.get(i), 0, GRID + 2 + i * CraftEmiRecipe.LINE_HEIGHT, CraftEmiRecipe.TEXT_COLOR,
                    false);
        }
    }
}
