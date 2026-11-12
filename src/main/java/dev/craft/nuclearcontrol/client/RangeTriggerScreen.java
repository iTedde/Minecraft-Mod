package dev.craft.nuclearcontrol.client;

import javax.annotation.Nullable;

import dev.craft.nuclearcontrol.NcPayloads;
import dev.craft.nuclearcontrol.block.RangeTriggerBlockEntity;
import dev.craft.nuclearcontrol.menu.RangeTriggerMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * {@code GuiRangeTrigger} (176 × 190): two limits of ten digits, each digit with a "−" button above and a "+" button
 * below ({@code CompactButton}, grouped in threes), values at y 33 (start) and y 70 (end); redstone inversion switch
 * at 8, 62. A digit only changes within 0…9 (no carry); the new value is set on the client at once and sent.
 */
public class RangeTriggerScreen extends AbstractContainerScreen<RangeTriggerMenu> {
    private static final ResourceLocation TEXTURE = NuclearControlClient.gui("range_trigger");
    private static final int DIGITS = 10;

    @Nullable
    private final RangeTriggerBlockEntity trigger;

    public RangeTriggerScreen(RangeTriggerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageHeight = 190;
        inventoryLabelY = imageHeight - 96 + 2;
        trigger = inventory.player.level().getBlockEntity(menu.getPos()) instanceof RangeTriggerBlockEntity entity
                ? entity : null;
    }

    @Override
    protected void init() {
        super.init();
        if (trigger == null) {
            return;
        }
        for (int end = 0; end < 2; end++) {
            for (int i = 0; i < DIGITS; i++) {
                int x = leftPos + 30 + i * 12 + (i + 2) / 3 * 6;
                int power = DIGITS - 1 - i;
                boolean isEnd = end == 1;
                addRenderableWidget(new HeatLevelControls.CompactButton(x, topPos + (isEnd ? 57 : 20), 12, 12, "-",
                        () -> change(isEnd, power, false)));
                addRenderableWidget(new HeatLevelControls.CompactButton(x, topPos + (isEnd ? 79 : 42), 12, 12, "+",
                        () -> change(isEnd, power, true)));
            }
        }
        addRenderableWidget(new InvertSwitch(leftPos + 8, topPos + 62));
    }

    private void change(boolean isEnd, int power, boolean plus) {
        long value = isEnd ? trigger.getLevelEnd() : trigger.getLevelStart();
        long delta = (long) Math.pow(10.0, power);
        long digit = value / delta % 10L;
        long changed = value;
        if (plus && digit < 9L) {
            changed += delta;
        } else if (!plus && digit > 0L) {
            changed -= delta;
        }
        if (changed != value) {
            NcPayloads.sendText(menu.getPos(), NcPayloads.TRIGGER_LIMIT, isEnd ? 1 : 0, Long.toString(changed));
            trigger.setLimit(isEnd, changed);
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        graphics.blit(TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, (imageWidth - font.width(title)) / 2, 6, 0x404040, false);
        graphics.drawString(font, playerInventoryTitle, 8, inventoryLabelY, 0x404040, false);
        if (trigger != null) {
            renderValue(graphics, trigger.getLevelStart(), 30, 33);
            renderValue(graphics, trigger.getLevelEnd(), 30, 70);
        }
    }

    /** {@code renderValue}: digit by digit from the right, each centred over its buttons. */
    private void renderValue(GuiGraphics graphics, long value, int x, int y) {
        x += 114;
        for (int i = 0; i < DIGITS; i++) {
            String digit = Long.toString(value % 10L);
            graphics.drawString(font, digit, x - 12 * i - font.width(digit) / 2 + (DIGITS - 1 - i + 2) / 3 * 6, y,
                    0x404040, false);
            value /= 10L;
        }
    }

    /** {@code GuiRangeTriggerInvertRedstone}: 18 × 15, image at u 176, v 0 (off) or 15 (on). */
    private final class InvertSwitch extends AbstractWidget {
        InvertSwitch(int x, int y) {
            super(x, y, 18, 15, Component.translatable("gui.craft.nuclearcontrol.invert_redstone"));
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            graphics.blit(TEXTURE, getX(), getY() + 1, 176, trigger.isInvertRedstone() ? 15 : 0, 18, 15);
        }

        @Override
        public void onClick(double mouseX, double mouseY) {
            NcPayloads.send(menu.getPos(), NcPayloads.INVERT, trigger.isInvertRedstone() ? 0 : 1);
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput output) {
            defaultButtonNarrationText(output);
        }
    }
}
