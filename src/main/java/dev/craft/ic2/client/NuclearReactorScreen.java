package dev.craft.ic2.client;

import dev.craft.ic2.menu.NuclearReactorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** IC2 {@code GuiNuclearReactor}: background {@code GUI6by<width>}, 176 × 222, no gauges. */
public class NuclearReactorScreen extends MachineScreen<NuclearReactorMenu> {
    public NuclearReactorScreen(NuclearReactorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, "nuclear_reactor_" + menu.getSize());
        imageHeight = 222;
        inventoryLabelY = imageHeight - 96 + 2;
    }

    @Override
    protected void renderGauges(GuiGraphics graphics, int x, int y) {
    }
}
