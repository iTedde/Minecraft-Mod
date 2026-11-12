package dev.craft.nuclearcontrol.client;

import javax.annotation.Nullable;

import dev.craft.nuclearcontrol.block.ThermalMonitorBlockEntity;
import dev.craft.nuclearcontrol.menu.ThermalMonitorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import org.lwjgl.glfw.GLFW;

/** {@code GuiIC2Thermo}: 191 × 64, title centred, heat level controls. */
public class ThermalMonitorScreen extends AbstractContainerScreen<ThermalMonitorMenu> {
    @Nullable
    private final HeatLevelControls controls;

    public ThermalMonitorScreen(ThermalMonitorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 191;
        imageHeight = 64;
        controls = inventory.player.level().getBlockEntity(menu.getPos()) instanceof ThermalMonitorBlockEntity monitor
                ? new HeatLevelControls(monitor) : null;
    }

    @Override
    protected void init() {
        super.init();
        if (controls != null) {
            controls.init(font, leftPos, topPos + 20, 1, 18, this::addRenderableWidget);
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
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(HeatLevelControls.TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, (imageWidth - font.width(title)) / 2, 6, 0x404040, false);
    }
}
