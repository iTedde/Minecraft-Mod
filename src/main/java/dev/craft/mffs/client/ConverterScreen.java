package dev.craft.mffs.client;

import dev.craft.mffs.block.ConverterBlockEntity;
import dev.craft.mffs.block.MffsMachineBlockEntity;
import dev.craft.mffs.menu.ConverterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * {@code GuiConverter} (256 × 216): IC2 output on/off, packet size and amount (+/−), FE of the link, online state.
 * The UE half stays covered as in the pack without UE.
 */
public class ConverterScreen extends MffsMachineScreen<ConverterMenu> {
    private static final int OUTPUT_X = 103;
    private static final int OUTPUT_Y = 28;

    public ConverterScreen(ConverterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, "converter", 256, 216, new int[] {97, 4, 227, 18}, 235, 4);
    }

    @Override
    protected void renderMachineBg(GuiGraphics graphics) {
        int fill = 76 * menu.getCapacity() / 100;
        graphics.blit(background, leftPos + 14, topPos + 65, 0, 233, fill + 1, 23, 256, 256);
        graphics.blit(background, leftPos + 174, topPos + 45, 0, 217, 70, 13, 256, 256);
        icon(graphics, OUTPUT_X, OUTPUT_Y, 80 + (menu.isIcOutput() ? 16 : 0), 128);
    }

    @Override
    protected boolean machineClicked(double mouseX, double mouseY) {
        if (inside(mouseX, mouseY, OUTPUT_X, OUTPUT_Y, OUTPUT_X + 15, OUTPUT_Y + 15)) {
            send(ConverterBlockEntity.EVENT_IC_OUTPUT, "");
            return true;
        }
        if (inside(mouseX, mouseY, 100, 46, 114, 57)) {
            send(ConverterBlockEntity.EVENT_PACKET_SIZE, "+");
        } else if (inside(mouseX, mouseY, 115, 46, 128, 57)) {
            send(ConverterBlockEntity.EVENT_PACKET_SIZE, "-");
        } else if (inside(mouseX, mouseY, 140, 46, 154, 57)) {
            send(ConverterBlockEntity.EVENT_PACKET_AMOUNT, "+");
        } else if (inside(mouseX, mouseY, 155, 46, 168, 57)) {
            send(ConverterBlockEntity.EVENT_PACKET_AMOUNT, "-");
        } else {
            return false;
        }
        return true;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        text(graphics, deviceName(), 100, 8, TEXT);
        text(graphics, Component.translatable("gui.craft.mffs.converter"), 8, 8, TEXT);
        text(graphics, "IC 2", 125, 33, 0);
        text(graphics, "EU", 110, 62, 0);
        text(graphics, Component.translatable("gui.craft.mffs.packets"), 130, 62, 0);
        text(graphics, String.valueOf(menu.getPacketSize()), 103, 48, 0xFFFFFF);
        text(graphics, "x", 132, 48, 0xFFFFFF);
        text(graphics, String.valueOf(menu.getPacketAmount()), 150, 48, 0xFFFFFF);
        if (menu.getPowerSourceId() != 0) {
            text(graphics, Component.translatable("gui.craft.mffs.fe", menu.getLinkPower()), 17, 54, TEXT);
        } else {
            text(graphics, Component.translatable("gui.craft.mffs.no_link"), 17, 54, TEXT);
        }
        MffsMachineBlockEntity machine = machine();
        if (machine == null || !machine.isActive()) {
            text(graphics, Component.translatable("gui.craft.mffs.offline"), 32, 99, 0xFF0000);
        } else {
            text(graphics, Component.translatable("gui.craft.mffs.online"), 34, 99, 0x008B00);
        }
    }
}
