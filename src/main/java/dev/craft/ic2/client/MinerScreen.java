package dev.craft.ic2.client;

import dev.craft.ic2.machine.MinerBlockEntity;
import dev.craft.ic2.menu.MinerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** IC2 {@code GuiMiner}: only the energy gauge. */
public class MinerScreen extends MachineScreen<MinerMenu> {
    public MinerScreen(MinerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, "miner");
    }

    @Override
    protected void renderGauges(GuiGraphics graphics, int x, int y) {
        int energy = menu.getEnergy();
        if (energy > 0) {
            int height = Math.min(14, energy * 14 / MinerBlockEntity.MAX_ENERGY);
            blit(graphics, x + 81, y + 41 + 14 - height, 176, 14 - height, 14, height);
        }
    }
}
