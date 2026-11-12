package dev.craft.mffs.client;

import dev.craft.mffs.MffsConfig;
import dev.craft.mffs.menu.ExtractorMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** {@code GuiExtractor} (176 × 186): work energy bar, work cycle, FE buffer, name, switch mode. */
public class ExtractorScreen extends MffsMachineScreen<ExtractorMenu> {
    public ExtractorScreen(ExtractorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, "extractor", 176, 186, new int[] {10, 5, 141, 19}, 148, 5);
    }

    @Override
    protected void renderMachineBg(GuiGraphics graphics) {
        int work = 79 * menu.getWorkDone() / 100;
        graphics.blit(background, leftPos + 49, topPos + 89, 176, 0, work, 6, 256, 256);
        int cycle = 32 * menu.getWorkCycle() / MffsConfig.FORCICIUM_WORK_CYCLE;
        graphics.blit(background, leftPos + 73, topPos + 50, 179, 81, cycle, 32, 256, 256);
        int energy = (int) (24L * menu.getForceEnergyBuffer() / menu.getMaxForceEnergyBuffer());
        graphics.blit(background, leftPos + 137, topPos + 60, 219, 80, 32, energy, 256, 256);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        text(graphics, Component.translatable("gui.craft.mffs.force_energy"), 5, 25, TEXT);
        text(graphics, Component.translatable("gui.craft.mffs.upgrades"), 10, 50, TEXT);
        text(graphics, deviceName(), 8, 9, TEXT);
        text(graphics, Component.translatable("gui.craft.mffs.extractor"), 5, 35, TEXT);
        text(graphics, menu.getForceEnergyBuffer() / 1000 + "k", 140, 89, TEXT);
        text(graphics, menu.getWorkDone() + "%", 23, 89, TEXT);
    }
}
