package dev.craft.nuclearcontrol.client;

import dev.craft.nuclearcontrol.NcPayloads;
import dev.craft.nuclearcontrol.block.InfoPanelBlockEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * {@code GuiScreenColor}: 226 × 94, 15 colours each for the screen (y 32–41) and the text (y 63–72), 14 px apart
 * from x 7; the frame marks the current choice. Escape returns to the panel GUI.
 */
public class ScreenColorScreen extends Screen {
    private static final ResourceLocation TEXTURE = NuclearControlClient.gui("screen_colors");
    private static final int WIDTH = 226;
    private static final int HEIGHT = 94;

    private final InfoPanelScreen parent;
    private final InfoPanelBlockEntity panel;
    private int colorBack;
    private int colorText;
    private int left;
    private int top;

    public ScreenColorScreen(InfoPanelScreen parent, InfoPanelBlockEntity panel) {
        super(Component.translatable("gui.craft.nuclearcontrol.screen_color"));
        this.parent = parent;
        this.panel = panel;
        this.colorBack = panel.getColorBackground();
        this.colorText = panel.getColorText();
    }

    @Override
    protected void init() {
        left = (width - WIDTH) / 2;
        top = (height - HEIGHT) / 2;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = (int) mouseX - left;
        int y = (int) mouseY - top;
        if (x >= 7 && x <= 212) {
            int index = (x - 7) / 14;
            int shift = (x - 7) % 14;
            if (y >= 32 && y <= 41 && shift <= 9) {
                colorBack = index;
                apply();
                return true;
            }
            if (y >= 63 && y <= 72 && shift <= 9) {
                colorText = index;
                apply();
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void apply() {
        panel.setColors(colorBack, colorText);
        NcPayloads.send(panel.getBlockPos(), NcPayloads.COLORS, colorBack | colorText << 4);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(TEXTURE, left, top, 0, 0, WIDTH, HEIGHT);
        graphics.blit(TEXTURE, left + 5 + colorBack * 14, top + 30, 226, 0, 14, 14);
        graphics.blit(TEXTURE, left + 5 + colorText * 14, top + 61, 226, 0, 14, 14);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawString(font, Component.translatable("gui.craft.nuclearcontrol.screen_color"), left + 8, top + 20,
                0x404040, false);
        graphics.drawString(font, Component.translatable("gui.craft.nuclearcontrol.text_color"), left + 8, top + 52,
                0x404040, false);
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }
}
