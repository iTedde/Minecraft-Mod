package dev.craft.ironchest.client;

import dev.craft.Craft;
import dev.craft.ironchest.IronChestMenu;
import dev.craft.ironchest.IronChestType;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/** Iron Chests {@code GUIChest}: the original background at its size, without any titles. */
public class IronChestScreen extends AbstractContainerScreen<IronChestMenu> {
    private final ResourceLocation background;

    public IronChestScreen(IronChestMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        IronChestType type = menu.getChestType();
        imageWidth = type.getGuiWidth();
        imageHeight = type.getGuiHeight();
        background = ResourceLocation.fromNamespaceAndPath(Craft.MODID,
                "textures/gui/ironchest/" + type.getGuiTexture() + ".png");
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(background, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
    }
}
