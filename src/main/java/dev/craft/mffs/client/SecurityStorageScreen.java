package dev.craft.mffs.client;

import dev.craft.mffs.menu.SecurityStorageMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** {@code GuiSecStorage} (185 × 238): name input, switch mode button, 6 × 9 storage. */
public class SecurityStorageScreen extends MffsMachineScreen<SecurityStorageMenu> {
    public SecurityStorageScreen(SecurityStorageMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, "security_storage", 185, 238, new int[] {10, 5, 141, 19}, 157, 6);
    }

    @Override
    protected void renderMachineBg(GuiGraphics graphics) {
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        text(graphics, deviceName(), 12, 9, TEXT);
        text(graphics, Component.translatable("gui.craft.mffs.security_storage"), 38, 28, TEXT);
    }
}
