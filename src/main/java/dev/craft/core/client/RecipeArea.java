package dev.craft.core.client;

import java.util.List;

import javax.annotation.Nullable;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Clickable GUI area (usually the progress arrow) that opens a recipe viewer category. Coordinates are relative to the
 * GUI origin. The viewer is installed by an optional integration (EMI); without one the areas do nothing.
 */
public record RecipeArea(int x, int y, int width, int height, ResourceLocation category) {
    /** Opens a recipe viewer category by id; returns false if the id is unknown. */
    @FunctionalInterface
    public interface Viewer {
        boolean showCategory(ResourceLocation category);
    }

    private static final Component TOOLTIP = Component.translatable("gui.craft.show_recipes");
    @Nullable
    private static Viewer viewer;

    public static void setViewer(Viewer viewer) {
        RecipeArea.viewer = viewer;
    }

    public boolean contains(int leftPos, int topPos, double mouseX, double mouseY) {
        double relX = mouseX - leftPos;
        double relY = mouseY - topPos;
        return relX >= x && relX < x + width && relY >= y && relY < y + height;
    }

    /** {@code mouseClicked} helper: opens the first area under the mouse. */
    public static boolean click(List<RecipeArea> areas, int leftPos, int topPos, double mouseX, double mouseY) {
        if (viewer == null) {
            return false;
        }
        for (RecipeArea area : areas) {
            if (area.contains(leftPos, topPos, mouseX, mouseY) && viewer.showCategory(area.category)) {
                return true;
            }
        }
        return false;
    }

    /** {@code render} helper: "show recipes" tooltip over an area. */
    public static boolean renderTooltip(List<RecipeArea> areas, GuiGraphics graphics, Font font, int leftPos,
                                        int topPos, int mouseX, int mouseY) {
        if (viewer == null) {
            return false;
        }
        for (RecipeArea area : areas) {
            if (area.contains(leftPos, topPos, mouseX, mouseY)) {
                graphics.renderTooltip(font, TOOLTIP, mouseX, mouseY);
                return true;
            }
        }
        return false;
    }
}
