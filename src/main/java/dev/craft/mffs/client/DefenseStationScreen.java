package dev.craft.mffs.client;

import dev.craft.mffs.block.DefenseStationBlockEntity;
import dev.craft.mffs.menu.DefenseStationMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * {@code GuiAreaDefenseStation} (256 × 216): FE or "No Link/OOR", station link, perimeters, buttons for contra type
 * (allowed/banned list), action mode with its description and scan mode (sphere/cube).
 */
public class DefenseStationScreen extends MffsMachineScreen<DefenseStationMenu> {
    private static final int GREEN = 2263842;
    private static final int RED = 16711680;
    private static final int BLUE = 139;
    private static final int ORANGE = 15612731;
    private static final int CONTRA_X = 175;
    private static final int CONTRA_Y = 70;
    private static final int ACTION_X = 92;
    private static final int SCAN_X = 148;
    private static final int MODE_Y = 50;
    /** Description lines of the action modes 0–5. */
    private static final int[] DESCRIPTION_LINES = {4, 4, 4, 3, 2, 2};

    public DefenseStationScreen(DefenseStationMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, "defense_station", 256, 216, new int[] {115, 5, 233, 19}, 235, 4);
    }

    @Override
    protected void renderMachineBg(GuiGraphics graphics) {
        icon(graphics, CONTRA_X, CONTRA_Y, 176 + menu.getContraType() * 16, 80);
        icon(graphics, ACTION_X, MODE_Y, 64 + menu.getActionMode() * 16, 96);
        icon(graphics, SCAN_X, MODE_Y, 160 + menu.getScanMode() * 16, 96);
    }

    @Override
    protected boolean machineClicked(double mouseX, double mouseY) {
        if (inside(mouseX, mouseY, CONTRA_X, CONTRA_Y, CONTRA_X + 15, CONTRA_Y + 15)) {
            send(DefenseStationBlockEntity.EVENT_CONTRA_TYPE, "");
            return true;
        }
        if (inside(mouseX, mouseY, ACTION_X, MODE_Y, ACTION_X + 15, MODE_Y + 15)) {
            send(DefenseStationBlockEntity.EVENT_ACTION_MODE, "");
            return true;
        }
        if (inside(mouseX, mouseY, SCAN_X, MODE_Y, SCAN_X + 15, MODE_Y + 15)) {
            send(DefenseStationBlockEntity.EVENT_SCAN_MODE, "");
            return true;
        }
        return false;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        text(graphics, Component.translatable("gui.craft.mffs.defense_station"), 7, 9, TEXT);
        text(graphics, deviceName(), 120, 9, TEXT);
        int mode = Math.max(0, Math.min(5, menu.getActionMode()));
        text(graphics, Component.translatable("gui.craft.mffs.action_mode." + mode), 110, 55, TEXT);
        for (int line = 0; line < DESCRIPTION_LINES[mode]; line++) {
            text(graphics, Component.translatable("gui.craft.mffs.action_mode." + mode + "." + line), 95,
                    85 + line * 10, TEXT);
        }
        text(graphics, Component.translatable("gui.craft.mffs.action_desc"), 95, 73, BLUE);
        text(graphics, Component.translatable("gui.craft.mffs.items"), 205, 68, GREEN);
        if (menu.getContraType() == 0) {
            text(graphics, Component.translatable("gui.craft.mffs.allowed"), 200, 82, GREEN);
        } else {
            text(graphics, Component.translatable("gui.craft.mffs.banned"), 200, 82, RED);
        }
        if (menu.hasPowerSource()) {
            text(graphics, Component.translatable("gui.craft.mffs.fe_percent", menu.getCapacity()), 35, 31, TEXT);
        } else {
            text(graphics, Component.translatable("gui.craft.mffs.no_link_oor"), 35, 31, RED);
        }
        if (menu.hasSecurityCard()) {
            text(graphics, Component.translatable("gui.craft.mffs.linked"), 120, 31, GREEN);
        }
        text(graphics, Component.translatable("gui.craft.mffs.warning"), 35, 55, BLUE);
        text(graphics, Component.translatable("gui.craft.mffs.perimeter", menu.getInfoDistance()), 12, 73, TEXT);
        text(graphics, Component.translatable("gui.craft.mffs.action"), 35, 91, ORANGE);
        text(graphics, Component.translatable("gui.craft.mffs.perimeter", menu.getActionDistance()), 12, 111, TEXT);
        text(graphics, Component.translatable("gui.craft.mffs.inventory"), 180, 195, TEXT);
    }
}
