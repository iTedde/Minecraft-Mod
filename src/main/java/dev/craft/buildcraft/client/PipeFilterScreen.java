package dev.craft.buildcraft.client;

import dev.craft.Craft;
import dev.craft.buildcraft.menu.PipeFilterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * BuildCraft {@code GuiDiamondPipe} ({@code filter.png}, 175×225) and {@code GuiEmeraldPipe} ({@code filter_2.png},
 * 175×132).
 */
public class PipeFilterScreen extends AbstractContainerScreen<PipeFilterMenu> {
    private static final ResourceLocation DIAMOND =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/gui/diamond_pipe.png");
    private static final ResourceLocation EMERALD =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/gui/emerald_pipe.png");

    private final boolean diamond;

    public PipeFilterScreen(PipeFilterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        diamond = menu.getRows() > 1;
        imageWidth = 175;
        imageHeight = diamond ? 225 : 132;
        inventoryLabelY = imageHeight - (diamond ? 97 : 93);
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(diamond ? DIAMOND : EMERALD, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }
}
