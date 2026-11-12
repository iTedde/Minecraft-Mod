package dev.craft.nuclearcontrol.client;

import dev.craft.nuclearcontrol.NcPayloads;
import dev.craft.nuclearcontrol.block.AverageCounterBlockEntity;
import dev.craft.nuclearcontrol.card.PanelText;
import dev.craft.nuclearcontrol.menu.CounterMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

/**
 * {@code GuiEnergyCounter}/{@code GuiAverageCounter} (both on {@code GUIEnergyCounter.png}, 176 × 166): energy
 * counter with its reading centred at y 22 and a "Reset" button (35, 42, 127 × 20); average counter with the lines
 * "Output: …EU/t" (y 22) and "Period: …sec" (y 32) and the buttons 1/3/5/10 (30 wide each).
 */
public class CounterScreen extends AbstractContainerScreen<CounterMenu> {
    private static final ResourceLocation TEXTURE = NuclearControlClient.gui("energy_counter");
    private static final int[] PERIODS = {1, 3, 5, 10};

    /** From the block entity: the synced data slots only arrive after the screen is opened. */
    private final boolean average;

    public CounterScreen(CounterMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        inventoryLabelY = imageHeight - 96 + 2;
        average = inventory.player.level().getBlockEntity(menu.getPos()) instanceof AverageCounterBlockEntity;
    }

    @Override
    protected void init() {
        super.init();
        if (average) {
            for (int i = 0; i < PERIODS.length; i++) {
                int period = PERIODS[i];
                addRenderableWidget(Button.builder(Component.literal(Integer.toString(period)),
                                button -> NcPayloads.send(menu.getPos(), NcPayloads.AVERAGE_PERIOD, period))
                        .bounds(leftPos + 35 + i * 30, topPos + 42, 30, 20).build());
            }
        } else {
            addRenderableWidget(Button.builder(Component.translatable("gui.craft.nuclearcontrol.reset"),
                            button -> NcPayloads.send(menu.getPos(), NcPayloads.COUNTER_RESET, 0))
                    .bounds(leftPos + 35, topPos + 42, 127, 20).build());
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
        if (average) {
            centered(graphics, PanelText.labeled("output", menu.getAverage(), true), 22);
            centered(graphics, Component.translatable("gui.craft.nuclearcontrol.average_period",
                    PanelText.number(menu.getPeriod())).getString(), 32);
        } else {
            centered(graphics, PanelText.number(menu.getCounter()), 22);
        }
    }

    private void centered(GuiGraphics graphics, String text, int y) {
        graphics.drawString(font, text, (imageWidth - font.width(text)) / 2, y, 0x404040, false);
    }
}
