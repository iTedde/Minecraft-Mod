package dev.craft.ic2.client;

import dev.craft.ic2.menu.IronFurnaceMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** IC2 {@code GuiIronFurnace}: the vanilla furnace GUI with IC2's gauges. */
public class IronFurnaceScreen extends AbstractContainerScreen<IronFurnaceMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/furnace.png");
    private static final ResourceLocation LIT_PROGRESS = ResourceLocation.withDefaultNamespace("container/furnace/lit_progress");
    private static final ResourceLocation BURN_PROGRESS =
            ResourceLocation.withDefaultNamespace("container/furnace/burn_progress");

    public IronFurnaceScreen(IronFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
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
        int x = leftPos;
        int y = topPos;
        graphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);
        if (menu.getFuel() > 0) {
            int fuel = Math.min(menu.gaugeFuelScaled(12), 12);
            graphics.blitSprite(LIT_PROGRESS, 14, 14, 0, 12 - fuel, x + 56, y + 36 + 12 - fuel, 14, fuel + 2);
        }
        int progress = menu.gaugeProgressScaled(24);
        graphics.blitSprite(BURN_PROGRESS, 24, 16, 0, 0, x + 79, y + 34, progress + 1, 16);
    }
}
