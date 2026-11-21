package dev.craft.nuclearcontrol.client;

import javax.annotation.Nullable;

import dev.craft.nuclearcontrol.block.ThermalMonitorBlockEntity;
import dev.craft.nuclearcontrol.menu.RemoteThermalMonitorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

/** {@code GuiRemoteThermo}: 214 × 166, heat level controls 5 px higher, charge bar 76 px wide. */
public class RemoteThermalMonitorScreen extends AbstractContainerScreen<RemoteThermalMonitorMenu> {
    private static final ResourceLocation TEXTURE = NuclearControlClient.gui("remote_thermal_monitor");

    @Nullable
    private final HeatLevelControls controls;

    public RemoteThermalMonitorScreen(RemoteThermalMonitorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 214;
        imageHeight = 166;
        inventoryLabelX = 8;
        inventoryLabelY = imageHeight - 96 + 2;
        controls = inventory.player.level().getBlockEntity(menu.getPos()) instanceof ThermalMonitorBlockEntity monitor
                ? new HeatLevelControls(monitor) : null;
    }

    @Override
    protected void init() {
        super.init();
        if (controls != null) {
            controls.init(font, leftPos, topPos + 15, 1, 18, this::addRenderableWidget);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (controls != null && keyCode == GLFW.GLFW_KEY_ENTER) {
            controls.updateHeat(0);
            return true;
        }
        if (controls != null && controls.textbox().isFocused() && keyCode != GLFW.GLFW_KEY_ESCAPE) {
            return controls.textbox().keyPressed(keyCode, scanCode, modifiers);
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void removed() {
        if (controls != null) {
            controls.updateHeat(0);
        }
        super.removed();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        int max = menu.getMaxStorage();
        int chargeWidth = max > 0 ? Math.min(76, (int) (76.0F * menu.getEnergy()) / max) : 0;
        if (chargeWidth > 0) {
            graphics.blit(TEXTURE, leftPos + 55 - 14, topPos + 54, 8, 166, chargeWidth, 14);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, (imageWidth - font.width(title)) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, 0x404040, false);
    }
}
