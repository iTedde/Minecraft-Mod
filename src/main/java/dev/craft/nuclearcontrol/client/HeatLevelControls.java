package dev.craft.nuclearcontrol.client;

import java.util.function.Consumer;

import dev.craft.nuclearcontrol.NcPayloads;
import dev.craft.nuclearcontrol.block.ThermalMonitorBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * The heat level controls shared by the thermal monitor and the remote thermal monitor GUI ({@code GuiIC2Thermo},
 * {@code GuiRemoteThermo}): text field for digits, ±1…±10000 buttons ({@code CompactButton}) and the redstone
 * inversion switch ({@code GuiThermoInvertRedstone}); all drawn from {@code GUIThermalMonitor.png}.
 */
final class HeatLevelControls {
    static final ResourceLocation TEXTURE = NuclearControlClient.gui("thermal_monitor");
    private static final int[][] BUTTONS = {
            {47, 0, 22, -1}, {47, 11, 22, -10}, {12, 0, 36, -100}, {12, 11, 36, -1000}, {12, 22, 57, -10000},
            {122, 0, 22, 1}, {122, 11, 22, 10}, {143, 0, 36, 100}, {143, 11, 36, 1000}, {122, 22, 57, 10000}};

    private final ThermalMonitorBlockEntity monitor;
    private EditBox textbox;

    HeatLevelControls(ThermalMonitorBlockEntity monitor) {
        this.monitor = monitor;
    }

    /** Adds the widgets; {@code top} is the y of the first button row, {@code textY}/{@code invertY} relative. */
    void init(Font font, int left, int top, int textY, int invertY, Consumer<AbstractWidget> add) {
        for (int[] button : BUTTONS) {
            int delta = button[3];
            add.accept(new CompactButton(left + button[0], top + button[1], button[2], 12,
                    (delta > 0 ? "+" : "") + delta, () -> updateHeat(delta)));
        }
        add.accept(new InvertSwitch(left + 70, top + invertY));
        textbox = new EditBox(font, left + 70, top + textY, 51, 12, Component.empty());
        textbox.setFilter(text -> text.chars().allMatch(Character::isDigit));
        textbox.setMaxLength(7);
        textbox.setValue(Integer.toString(monitor.getHeatLevel()));
        add.accept(textbox);
    }

    EditBox textbox() {
        return textbox;
    }

    /** {@code updateHeat}: value of the text field plus {@code delta}, clamped to 0…1 000 000, sent when changed. */
    void updateHeat(int delta) {
        if (textbox == null) {
            return;
        }
        long heat = 0;
        try {
            if (!textbox.getValue().isEmpty()) {
                heat = Long.parseLong(textbox.getValue());
            }
        } catch (NumberFormatException ignored) {
        }
        heat = Math.max(0, Math.min(ThermalMonitorBlockEntity.MAX_HEAT_LEVEL, heat + delta));
        if (monitor.getHeatLevel() != heat) {
            NcPayloads.send(monitor.getBlockPos(), NcPayloads.HEAT_LEVEL, (int) heat);
        }
        textbox.setValue(Long.toString(heat));
    }

    /** {@code CompactButton}: two halves of the button strip at v 64 + state × 12, dark text. */
    static final class CompactButton extends AbstractWidget {
        private final Runnable action;

        CompactButton(int x, int y, int width, int height, String label, Runnable action) {
            super(x, y, width, height, Component.literal(label));
            this.action = action;
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int v = 64 + (isHovered() ? 2 : 1) * 12;
            int leftHalf = width / 2 + width % 2;
            graphics.blit(TEXTURE, getX(), getY(), 0, v, leftHalf, height);
            graphics.blit(TEXTURE, getX() + leftHalf, getY(), 200 - width / 2, v, width / 2, height);
            Font font = Minecraft.getInstance().font;
            graphics.drawString(font, getMessage(), getX() + (width - font.width(getMessage())) / 2, getY() + 2,
                    0x303030, false);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            action.run();
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }

    /** {@code GuiThermoInvertRedstone}: a 51 × 15 switch, image at u 199, v 0 (off) or 15 (on). */
    final class InvertSwitch extends AbstractWidget {
        InvertSwitch(int x, int y) {
            super(x, y, 51, 15, Component.translatable("gui.craft.nuclearcontrol.invert_redstone"));
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.blit(TEXTURE, getX(), getY() + 1, 199, monitor.isInvertRedstone() ? 15 : 0, 51, 15);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            NcPayloads.send(monitor.getBlockPos(), NcPayloads.INVERT, monitor.isInvertRedstone() ? 0 : 1);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }
}
