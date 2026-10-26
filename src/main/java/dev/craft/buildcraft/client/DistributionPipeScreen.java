package dev.craft.buildcraft.client;

import java.util.Locale;

import dev.craft.buildcraft.menu.DistributionPipeMenu;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/**
 * GUI of the distribution transport pipe (spec 31 §4): one row per side with its diamond-pipe colour, −10/−1/+1/+10
 * buttons (shift: 0 or 100), the weight and the actual share among the connected sides.
 */
public class DistributionPipeScreen extends AbstractContainerScreen<DistributionPipeMenu> {
    /** Arm colours of the diamond pipe textures: down, up, north, south, west, east. */
    private static final int[] SIDE_COLORS = {0xFF000000, 0xFFE5E5E5, 0xFFFF0000, 0xFF0000FF, 0xFF00FF00, 0xFFFFFF00};
    private static final String[] BUTTON_LABELS = {"-10", "-1", "+1", "+10"};
    private static final int[] BUTTON_X = {88, 110, 154, 172};
    private static final int[] BUTTON_WIDTH = {20, 16, 16, 20};
    private static final int ROW_Y = 20;
    private static final int ROW_HEIGHT = 18;
    private static final int TEXT_COLOR = 0x404040;
    private static final int GREY = 0x8B8B8B;

    public DistributionPipeScreen(DistributionPipeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        imageWidth = 240;
        imageHeight = ROW_Y + 6 * ROW_HEIGHT + 30;
    }

    @Override
    protected void init() {
        super.init();
        titleLabelX = (imageWidth - font.width(title)) / 2;
        for (Direction side : Direction.values()) {
            int y = topPos + ROW_Y + side.get3DDataValue() * ROW_HEIGHT;
            for (int k = 0; k < 4; k++) {
                int id = side.get3DDataValue() * 4 + k;
                addRenderableWidget(Button.builder(Component.literal(BUTTON_LABELS[k]),
                                button -> minecraft.gameMode.handleInventoryButtonClick(menu.containerId,
                                        hasShiftDown() ? id + DistributionPipeMenu.SHIFT : id))
                        .bounds(leftPos + BUTTON_X[k], y - 2, BUTTON_WIDTH[k], 14).build());
            }
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        int x = leftPos;
        int y = topPos;
        graphics.fill(x, y, x + imageWidth, y + imageHeight, 0xFF000000);
        graphics.fill(x + 1, y + 1, x + imageWidth - 1, y + imageHeight - 1, 0xFFFFFFFF);
        graphics.fill(x + 2, y + 2, x + imageWidth - 1, y + imageHeight - 1, 0xFF555555);
        graphics.fill(x + 2, y + 2, x + imageWidth - 2, y + imageHeight - 2, 0xFFC6C6C6);
        for (Direction side : Direction.values()) {
            int rowY = y + ROW_Y + side.get3DDataValue() * ROW_HEIGHT;
            graphics.fill(x + 7, rowY - 1, x + 15, rowY + 9, 0xFF373737);
            graphics.fill(x + 8, rowY, x + 14, rowY + 8, SIDE_COLORS[side.get3DDataValue()]);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawString(font, title, titleLabelX, titleLabelY, TEXT_COLOR, false);
        int total = 0;
        int sum = 0;
        for (Direction side : Direction.values()) {
            sum += menu.getWeight(side);
            if (menu.isConnected(side)) {
                total += menu.getWeight(side);
            }
        }
        for (Direction side : Direction.values()) {
            int y = ROW_Y + side.get3DDataValue() * ROW_HEIGHT + 1;
            boolean connected = menu.isConnected(side);
            int color = connected ? TEXT_COLOR : GREY;
            graphics.drawString(font, Component.translatable("gui.craft.distribution_pipe.side." + side.getName()),
                    18, y, color, false);
            String weight = menu.getWeight(side) + " %";
            graphics.drawString(font, weight, 141 - font.width(weight) / 2, y, color, false);
            String share = !connected || total == 0 ? "–"
                    : "(" + String.format(Locale.ROOT, "%.1f", 100.0 * menu.getWeight(side) / total) + " %)";
            graphics.drawString(font, share, 197, y, color, false);
        }
        int footer = ROW_Y + 6 * ROW_HEIGHT + 4;
        graphics.drawString(font, Component.translatable("gui.craft.distribution_pipe.sum", sum), 8, footer,
                TEXT_COLOR, false);
        graphics.drawString(font, Component.translatable("gui.craft.distribution_pipe.hint"), 8, footer + 11,
                GREY, false);
    }
}
