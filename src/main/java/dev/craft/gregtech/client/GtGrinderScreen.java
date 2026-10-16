package dev.craft.gregtech.client;

import dev.craft.Craft;
import dev.craft.gregtech.menu.GtGrinderMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** {@code GT_GUIContainer_Grinder}: casing warning, progress arrow, water level, error text. */
public class GtGrinderScreen extends AbstractContainerScreen<GtGrinderMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/gui/gt/grinder.png");

    public GtGrinderScreen(GtGrinderMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, 8, 4, 0x404040, false);
        if (!menu.isMachine()) {
            graphics.drawString(font, Component.translatable("container.craft.gt.incomplete_casing"), 8, 62, 0x404040,
                    false);
        }
        Component bottom = menu.isMissingEnergy() ? Component.translatable("container.craft.gt.insufficient_energy")
                : playerInventoryTitle;
        graphics.drawString(font, bottom, 8, 72, 0x404040, false);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int scale = menu.getProgressScale();
        if (scale > 0) {
            graphics.blit(TEXTURE, leftPos + 58, topPos + 28, 176, 0, scale, 11);
        }
        int water = menu.getWaterScale();
        if (water > 0) {
            graphics.blit(TEXTURE, leftPos + 33, topPos + 51 - water, 176, 51 - water, 18, water);
        }
    }
}
