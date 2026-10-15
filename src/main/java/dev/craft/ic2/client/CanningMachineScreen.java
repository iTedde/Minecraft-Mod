package dev.craft.ic2.client;

import dev.craft.ic2.menu.CanningMachineMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** IC2 {@code GuiCanner}: energy flame and progress arrow. */
public class CanningMachineScreen extends MachineScreen<CanningMachineMenu> {
    public CanningMachineScreen(CanningMachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, "canning_machine");
    }

    @Override
    protected void renderGauges(GuiGraphics graphics, int x, int y) {
        if (menu.hasEnergy()) {
            int height = menu.getEnergyScaled(14);
            blit(graphics, x + 31, y + 27 + 14 - height, 176, 14 - height, 14, height);
        }
        blit(graphics, x + 74, y + 36, 176, 15, menu.getProgressScaled(34), 13);
    }
}
