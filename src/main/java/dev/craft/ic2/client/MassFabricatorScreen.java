package dev.craft.ic2.client;

import dev.craft.ic2.menu.MassFabricatorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** IC2 {@code GuiMatter}: text only, no gauges. */
public class MassFabricatorScreen extends MachineScreen<MassFabricatorMenu> {
    private static final int TEXT_COLOR = 0x404040;

    public MassFabricatorScreen(MassFabricatorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, "mass_fabricator");
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        super.renderLabels(graphics, mouseX, mouseY);
        graphics.drawString(font, Component.translatable("container.craft.mass_fabricator.progress"), 16, 20,
                TEXT_COLOR, false);
        graphics.drawString(font, menu.getProgress() + "%", 16, 28, TEXT_COLOR, false);
        if (menu.getScrap() > 0) {
            graphics.drawString(font, Component.translatable("container.craft.mass_fabricator.amplifier"), 16, 44,
                    TEXT_COLOR, false);
            graphics.drawString(font, String.valueOf(menu.getScrap()), 16, 56, TEXT_COLOR, false);
        }
    }

    @Override
    protected void renderGauges(GuiGraphics graphics, int x, int y) {
    }
}
