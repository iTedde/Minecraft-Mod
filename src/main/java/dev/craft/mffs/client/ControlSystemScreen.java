package dev.craft.mffs.client;

import dev.craft.mffs.block.ControlSystemBlockEntity;
import dev.craft.mffs.block.MffsMachineBlockEntity;
import dev.craft.mffs.menu.ControlSystemMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * {@code GuiControlSystem} (256 × 216): remote name and kind, with a data link card in the reader the target's
 * station link, power and range; buttons for the target's active state (display), switch mode, switch value and
 * "Open Remote Gui".
 */
public class ControlSystemScreen extends MffsMachineScreen<ControlSystemMenu> {
    private static final int GREEN = 2263842;
    private static final int RED = 9116186;
    private static final int BUTTON_X = 13;
    private static final int ACTIVE_Y = 63;
    private static final int MODE_Y = 83;
    private static final int VALUE_Y = 103;

    public ControlSystemScreen(ControlSystemMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, "control_system", 256, 216, new int[] {115, 5, 234, 19}, -1, 0);
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(Button.builder(Component.translatable("gui.craft.mffs.open_remote_gui"),
                button -> send(ControlSystemBlockEntity.EVENT_REMOTE_GUI, "")).bounds(leftPos + 63, topPos + 100,
                100, 20).build());
    }

    private ControlSystemBlockEntity control() {
        MffsMachineBlockEntity machine = machine();
        return machine instanceof ControlSystemBlockEntity control ? control : null;
    }

    @Override
    protected void renderMachineBg(GuiGraphics graphics) {
        ControlSystemBlockEntity control = control();
        if (control == null || !menu.hasReaderCard()) {
            return;
        }
        icon(graphics, BUTTON_X, ACTIVE_Y, control.isRemoteActive() ? 176 : 192, 80);
        if (control.getRemoteSwitchMode() > 0) {
            icon(graphics, BUTTON_X, MODE_Y, 80 + control.getRemoteSwitchMode() * 16, 112);
        }
        if (control.getRemoteSwitchMode() == 3) {
            icon(graphics, BUTTON_X, VALUE_Y, control.getRemoteSwitchValue() ? 208 : 224, 80);
        }
    }

    @Override
    protected boolean machineClicked(double mouseX, double mouseY) {
        if (inside(mouseX, mouseY, BUTTON_X, MODE_Y, BUTTON_X + 15, MODE_Y + 15)) {
            send(ControlSystemBlockEntity.EVENT_REMOTE_MODE, "");
            return true;
        }
        if (inside(mouseX, mouseY, BUTTON_X, VALUE_Y, BUTTON_X + 15, VALUE_Y + 15)) {
            send(ControlSystemBlockEntity.EVENT_REMOTE_VALUE, "");
            return true;
        }
        return false;
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        ControlSystemBlockEntity control = control();
        text(graphics, Component.translatable("gui.craft.mffs.control_system"), 8, 9, TEXT);
        text(graphics, deviceName(), 123, 9, TEXT);
        text(graphics, Component.translatable("gui.craft.mffs.data_link"), 190, 54, TEXT);
        text(graphics, Component.translatable("gui.craft.mffs.reader"), 190, 65, TEXT);
        if (control == null) {
            return;
        }
        text(graphics, Component.translatable("gui.craft.mffs.remote_name", control.getRemoteName()), 15, 30, TEXT);
        String kind = control.getRemoteKind();
        text(graphics, Component.translatable("gui.craft.mffs.remote_type",
                kind.equals("-") ? Component.literal("-") : Component.translatable(kind)), 15, 45, TEXT);
        if (!menu.hasReaderCard()) {
            return;
        }
        Component linked = Component.translatable("gui.craft.mffs.linked");
        text(graphics, linked, 60, 64, control.hasRemoteStationLink() ? GREEN : RED);
        if (control.remoteHasPowerSource()) {
            text(graphics, linked, 120, 64, GREEN);
            text(graphics, Component.translatable("gui.craft.mffs.power_left", control.getRemotePowerLeft()), 40,
                    80, TEXT);
        } else {
            text(graphics, linked, 120, 64, RED);
        }
        if (control.isRemoteGuiInRange()) {
            text(graphics, "OK", 40, 107, GREEN);
        } else {
            text(graphics, "OOR", 40, 107, RED);
        }
    }
}
