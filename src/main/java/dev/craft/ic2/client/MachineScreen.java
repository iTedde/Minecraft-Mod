package dev.craft.ic2.client;

import dev.craft.Craft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

/** IC2 GUI base: 176×166 background from a 256×256 sheet, title centred at y 6, inventory label at y 72. */
public abstract class MachineScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {
    protected final ResourceLocation texture;

    protected MachineScreen(T menu, Inventory inventory, Component title, String texture) {
        super(menu, inventory, title);
        this.texture = ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/gui/" + texture + ".png");
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
        graphics.blit(texture, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        renderGauges(graphics, leftPos, topPos);
    }

    /** Draws the dynamic parts; {@code x}/{@code y} are the GUI origin. */
    protected abstract void renderGauges(GuiGraphics graphics, int x, int y);

    /** IC2 {@code drawTexturedModalRect} on the GUI texture. */
    protected void blit(GuiGraphics graphics, int x, int y, int u, int v, int width, int height) {
        if (width > 0 && height > 0) {
            graphics.blit(texture, x, y, u, v, width, height);
        }
    }
}
