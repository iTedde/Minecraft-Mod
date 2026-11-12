package dev.craft.mffs.client;

import dev.craft.mffs.block.CapacitorBlockEntity;
import dev.craft.mffs.menu.CapacitorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** {@code GuiCapacitor} (176 × 207): fill bar, FE, transmit range, linked devices, power link mode button. */
public class CapacitorScreen extends MffsMachineScreen<CapacitorMenu> {
    private static final int MODE_X = 108;
    private static final int MODE_Y = 75;

    public CapacitorScreen(CapacitorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, "capacitor", 176, 207, new int[] {5, 4, 135, 18}, 153, 3);
    }

    @Override
    protected void renderMachineBg(GuiGraphics graphics) {
        int fill = 79 * menu.getCapacity() / 100;
        graphics.blit(background, leftPos + 8, topPos + 112, 176, 0, fill + 1, 79, 256, 256);
        icon(graphics, MODE_X, MODE_Y, 96 + menu.getPowerLinkMode() * 16, 80);
    }

    @Override
    protected boolean machineClicked(double mouseX, double mouseY) {
        if (inside(mouseX, mouseY, MODE_X, MODE_Y, MODE_X + 15, MODE_Y + 15)) {
            send(CapacitorBlockEntity.EVENT_LINK_MODE, "");
            return true;
        }
        return false;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        text(graphics, Component.translatable("gui.craft.mffs.capacitor"), 8, 25, TEXT);
        text(graphics, deviceName(), 8, 8, TEXT);
        text(graphics, Component.translatable("gui.craft.mffs.fe", menu.getForcePower()), 8, 100, TEXT);
        text(graphics, Component.translatable("gui.craft.mffs.power_uplink"), 8, 80, TEXT);
        text(graphics, Component.translatable("gui.craft.mffs.transmit_range"), 8, 60, TEXT);
        text(graphics, " " + menu.getTransmitRange(), 90, 60, TEXT);
        text(graphics, Component.translatable("gui.craft.mffs.linked_devices"), 8, 43, TEXT);
        text(graphics, " " + menu.getLinkedDevices(), 90, 45, TEXT);
    }
}
