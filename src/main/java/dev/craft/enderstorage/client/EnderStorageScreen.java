package dev.craft.enderstorage.client;

import dev.craft.enderstorage.EnderStorageMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;

/** EnderStorage {@code GuiEnderItemStorage}: the vanilla 3 × 9 chest; personal storages show the owner on the right. */
public class EnderStorageScreen extends ContainerScreen {
    private final String ownerName;

    public EnderStorageScreen(ChestMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.ownerName = menu instanceof EnderStorageMenu storage ? storage.getOwnerName() : "";
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        if (!ownerName.isEmpty()) {
            graphics.drawString(font, ownerName, 170 - font.width(ownerName), 6, 4210752, false);
        }
    }
}
