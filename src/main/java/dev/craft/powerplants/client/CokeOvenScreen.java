package dev.craft.powerplants.client;

import java.util.List;

import dev.craft.Craft;
import dev.craft.core.client.RecipeArea;
import dev.craft.core.client.RecipeCategories;
import dev.craft.powerplants.cokeoven.CokeOvenMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Coke oven GUI (texture after Railcraft {@code gui_coke_oven.png}, tank removed). */
public class CokeOvenScreen extends AbstractContainerScreen<CokeOvenMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/gui/coke_oven.png");
    private static final List<RecipeArea> RECIPE_AREAS = List.of(
            new RecipeArea(16, 27, 14, 14, RecipeCategories.COKE_OVEN),
            new RecipeArea(34, 47, 20, 15, RecipeCategories.COKE_OVEN));

    public CokeOvenScreen(CokeOvenMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
        if (hoveredSlot == null && menu.getCarried().isEmpty()) {
            RecipeArea.renderTooltip(RECIPE_AREAS, graphics, font, leftPos, topPos, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && RecipeArea.click(RECIPE_AREAS, leftPos, topPos, mouseX, mouseY)) {
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        if (!menu.isFormed()) {
            graphics.drawString(font, Component.translatable("gui.craft.structure_incomplete"), 92, 30, 0xA00000,
                    false);
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        if (menu.isBurning()) {
            graphics.blit(TEXTURE, x + 16, y + 27, 176, 47, 14, 14);
        }
        int progress = menu.gaugeProgressScaled(20);
        graphics.blit(TEXTURE, x + 34, y + 47, 176, 61, progress, 15);
    }
}
