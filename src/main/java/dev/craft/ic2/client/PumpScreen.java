package dev.craft.ic2.client;

import dev.craft.ic2.machine.PumpBlockEntity;
import dev.craft.ic2.menu.PumpMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** IC2 {@code GuiPump}: energy gauge, charge bar with pointer and the scale on top. */
public class PumpScreen extends MachineScreen<PumpMenu> {
    public PumpScreen(PumpMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, "ic2_pump");
    }

    @Override
    protected void renderGauges(GuiGraphics graphics, int x, int y) {
        int energy = menu.getEnergy();
        if (energy > 0) {
            int height = Math.min(14, energy * 14 / PumpBlockEntity.MAX_ENERGY);
            blit(graphics, x + 62, y + 36 + 14 - height, 176, 14 - height, 14, height);
        }
        int charge = Math.min(41, menu.getCharge() * 41 / PumpBlockEntity.CHARGE_READY);
        blit(graphics, x + 99, y + 61 - charge, 176, 55, 12, 5);
        if (charge > 0) {
            blit(graphics, x + 99, y + 25 + 41 - charge, 176, 14, 12, charge);
        }
        blit(graphics, x + 98, y + 19, 188, 14, 13, 47);
    }
}
