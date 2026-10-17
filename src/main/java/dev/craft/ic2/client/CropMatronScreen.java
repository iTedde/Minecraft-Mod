package dev.craft.ic2.client;

import dev.craft.ic2.crop.CropMatronBlockEntity;
import dev.craft.ic2.menu.CropMatronMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** IC2 {@code GuiCropmatron}: only the energy gauge. */
public class CropMatronScreen extends MachineScreen<CropMatronMenu> {
    public CropMatronScreen(CropMatronMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, "crop_matron");
    }

    @Override
    protected void renderGauges(GuiGraphics graphics, int x, int y) {
        int energy = menu.getEnergy();
        if (energy > 0) {
            int height = Math.min(14, energy * 14 / CropMatronBlockEntity.MAX_ENERGY);
            blit(graphics, x + 26, y + 38 + 14 - height, 176, 14 - height, 14, height);
        }
    }
}
