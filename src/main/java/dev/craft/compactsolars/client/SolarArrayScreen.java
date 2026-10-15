package dev.craft.compactsolars.client;

import dev.craft.compactsolars.SolarArrayMenu;
import dev.craft.ic2.client.MachineScreen;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Solar array GUI: the IC2 solar panel GUI ({@code GUISolarGenerator}), sun symbol while the array delivers. */
public class SolarArrayScreen extends MachineScreen<SolarArrayMenu> {
    public SolarArrayScreen(SolarArrayMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, "solar_panel");
    }

    @Override
    protected void renderGauges(GuiGraphics graphics, int x, int y) {
        if (menu.isSunVisible()) {
            blit(graphics, x + 80, y + 45, 176, 0, 14, 14);
        }
    }
}
