package dev.craft.compat.emi;

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

/**
 * Generic machine recipe display: input slots, an arrow, output slots and text lines below (energy, duration, notes).
 * The arrow fills over {@code ticks} game ticks; 0 shows a static arrow.
 */
public class CraftEmiRecipe extends BasicEmiRecipe {
    protected static final int SLOT = 18;
    protected static final int ARROW_WIDTH = 24;
    protected static final int LINE_HEIGHT = 10;
    protected static final int TEXT_COLOR = 0x404040;

    private final int ticks;
    private final List<Component> lines;

    public CraftEmiRecipe(EmiRecipeCategory category, ResourceLocation id, List<EmiIngredient> inputs,
                          List<EmiStack> outputs, int ticks, List<Component> lines) {
        super(category, id, 0, 0);
        this.inputs = inputs;
        this.outputs = outputs;
        this.ticks = ticks;
        this.lines = lines;
        int textWidth = 0;
        for (Component line : lines) {
            textWidth = Math.max(textWidth, Minecraft.getInstance().font.width(line));
        }
        this.width = Math.max(arrowX() + ARROW_WIDTH + 4 + Math.max(1, outputs.size()) * SLOT, textWidth);
        this.height = SLOT + (lines.isEmpty() ? 0 : 2 + lines.size() * LINE_HEIGHT);
    }

    protected int arrowX() {
        return Math.max(1, inputs.size()) * SLOT + 4;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        for (int i = 0; i < inputs.size(); i++) {
            widgets.addSlot(inputs.get(i), i * SLOT, 0);
        }
        addArrow(widgets, arrowX(), 1);
        int outputX = arrowX() + ARROW_WIDTH + 4;
        for (int i = 0; i < outputs.size(); i++) {
            widgets.addSlot(outputs.get(i), outputX + i * SLOT, 0).recipeContext(this);
        }
        addLines(widgets);
    }

    protected void addArrow(WidgetHolder widgets, int x, int y) {
        if (ticks > 0) {
            widgets.addFillingArrow(x, y, ticks * 50);
        } else {
            widgets.addTexture(EmiTexture.EMPTY_ARROW, x, y);
        }
    }

    protected void addLines(WidgetHolder widgets) {
        for (int i = 0; i < lines.size(); i++) {
            widgets.addText(lines.get(i), 0, SLOT + 2 + i * LINE_HEIGHT, TEXT_COLOR, false);
        }
    }
}
