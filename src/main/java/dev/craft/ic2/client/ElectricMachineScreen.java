package dev.craft.ic2.client;

import dev.craft.ic2.menu.ElectricMachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** IC2 {@code GuiMacerator}, {@code GuiExtractor}, {@code GuiCompressor}, {@code GuiElecFurnace}, {@code GuiRecycler}. */
public class ElectricMachineScreen extends MachineScreen<ElectricMachineMenu> {
    public ElectricMachineScreen(ElectricMachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, menu.getKind().getName());
    }

    @Override
    protected void renderGauges(GuiGraphics graphics, int x, int y) {
        int charge = (int) (14.0F * menu.getChargeLevel());
        int progress = (int) (24.0F * menu.getProgress());
        if (charge > 0) {
            blit(graphics, x + 56, y + 36 + 14 - charge, 176, 14 - charge, 14, charge);
        }
        if (progress > 0) {
            blit(graphics, x + 79, y + 34, 176, 14, progress + 1, 16);
        }
    }
}
