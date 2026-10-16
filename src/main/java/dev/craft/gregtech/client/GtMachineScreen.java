package dev.craft.gregtech.client;

import java.util.List;

import dev.craft.core.client.RecipeArea;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/** Common base of the GT machine screens: item tooltips and the progress areas that open the recipe viewer. */
public abstract class GtMachineScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    protected GtMachineScreen(T menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (hoveredSlot == null && menu.getCarried().isEmpty()) {
            RecipeArea.renderTooltip(getRecipeAreas(), graphics, font, leftPos, topPos, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && RecipeArea.click(getRecipeAreas(), leftPos, topPos, mouseX, mouseY)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    /** Areas that open the recipe viewer (progress arrow). */
    protected abstract List<RecipeArea> getRecipeAreas();
}
