package dev.craft.ic2.client;

import dev.craft.ic2.menu.InductionFurnaceMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** IC2 {@code GuiInduction}: energy gauge, progress arrow and the heat in percent. */
public class InductionFurnaceScreen extends MachineScreen<InductionFurnaceMenu> {
    public static final String HEAT_KEY = "gui.craft.induction_furnace.heat";

    public InductionFurnaceScreen(InductionFurnaceMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, "induction_furnace");
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        graphics.drawString(font, Component.translatable(HEAT_KEY), 10, 36, 0x404040, false);
        graphics.drawString(font, menu.getHeatPercent() + "%", 10, 44, 0x404040, false);
    }

    @Override
    protected void renderGauges(GuiGraphics graphics, int x, int y) {
        if (menu.hasEnergy()) {
            int height = menu.getEnergyScaled(14);
            blit(graphics, x + 56, y + 36 + 14 - height, 176, 14 - height, 14, height);
        }
        blit(graphics, x + 79, y + 34, 176, 14, menu.getProgressScaled(24) + 1, 16);
    }
}
