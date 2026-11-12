package dev.craft.nuclearcontrol.client;

import javax.annotation.Nullable;

import dev.craft.nuclearcontrol.NcPayloads;
import dev.craft.nuclearcontrol.NcSounds;
import dev.craft.nuclearcontrol.block.AlarmBlockEntity;
import dev.craft.nuclearcontrol.menu.AlarmMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * {@code GuiHowlerAlarm} (131 × 136, range slider and sound list) and {@code GuiIndustrialAlarm} (131 × 64, slider
 * only).
 */
public class AlarmScreen extends AbstractContainerScreen<AlarmMenu> {
    private static final ResourceLocation HOWLER = NuclearControlClient.gui("howler_alarm");
    private static final ResourceLocation INDUSTRIAL = NuclearControlClient.gui("industrial_alarm");
    private static final int STEP = 8;
    private static final int LINE_HEIGHT = 10;

    @Nullable
    private final AlarmBlockEntity alarm;
    private final boolean industrial;

    public AlarmScreen(AlarmMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        alarm = inventory.player.level().getBlockEntity(menu.getPos()) instanceof AlarmBlockEntity entity
                ? entity : null;
        industrial = alarm != null && alarm.isIndustrial();
        imageWidth = 131;
        imageHeight = industrial ? 64 : 136;
    }

    @Override
    protected void init() {
        super.init();
        if (alarm == null) {
            return;
        }
        addRenderableWidget(new RangeSlider(leftPos + 12, topPos + 33));
        if (!industrial) {
            addRenderableWidget(new SoundList(leftPos + 13, topPos + 63, 105, 65));
        }
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(industrial ? INDUSTRIAL : HOWLER, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, (imageWidth - font.width(title)) / 2, 6, 0x404040, false);
        if (!industrial) {
            graphics.drawString(font, Component.translatable("gui.craft.nuclearcontrol.alarm_sound"), 12, 53,
                    0x404040, false);
        }
    }

    /** {@code GuiHowlerAlarmSlider}: 0…128 in steps of 8, 8 px knob at u 131, label 12 px above. */
    private final class RangeSlider extends AbstractWidget {
        private float value;

        RangeSlider(int x, int y) {
            super(x, y, 107, 16, Component.empty());
            value = Math.min(alarm.getRange(), AlarmBlockEntity.MAX_RANGE) / (float) AlarmBlockEntity.MAX_RANGE;
        }

        private int range() {
            return (int) Math.floor(AlarmBlockEntity.MAX_RANGE * value) / STEP * STEP;
        }

        private void setFromMouse(double mouseX) {
            value = Math.max(0.0F, Math.min(1.0F, (float) (mouseX - (getX() + 4)) / (width - 8)));
            int range = range();
            if (alarm.getRange() != range) {
                NcPayloads.send(alarm.getBlockPos(), NcPayloads.RANGE, range);
            }
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.blit(HOWLER, getX() + (int) (value * (width - 8)), getY(), 131, 0, 8, 16);
            graphics.drawString(font, Component.translatable("gui.craft.nuclearcontrol.alarm_range", range()),
                    getX(), getY() - 12, 0x404040, false);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            setFromMouse(mouseX);
        }

        @Override
        protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
            setFromMouse(mouseX);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
        }
    }

    /** {@code GuiHowlerAlarmListBox}: the selected sound highlighted (dark background, light text). */
    private final class SoundList extends AbstractWidget {
        SoundList(int x, int y, int width, int height) {
            super(x, y, width, height, Component.empty());
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int top = getY() + 2;
            for (String name : NcSounds.ALARM_NAMES) {
                if (name.equals(alarm.getSoundName())) {
                    graphics.fill(getX(), top - 1, getX() + width - 10, top + LINE_HEIGHT - 1, 0xFF404040);
                    graphics.drawString(font, name, getX() + 2, top, 0xA0A0A0, false);
                } else {
                    graphics.drawString(font, name, getX() + 2, top, 0x404040, false);
                }
                top += LINE_HEIGHT;
            }
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            int index = (int) (mouseY - 2 - getY()) / LINE_HEIGHT;
            index = Math.max(0, Math.min(NcSounds.ALARM_NAMES.size() - 1, index));
            String sound = NcSounds.ALARM_NAMES.get(index);
            if (!sound.equals(alarm.getSoundName())) {
                NcPayloads.sendSound(alarm.getBlockPos(), sound);
            }
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
        }
    }
}
