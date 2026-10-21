package dev.craft.thermalexpansion.client;

import dev.craft.Craft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * CoFH {@code Tab}: a sliding panel beside a GUI that grows by 8 px per frame towards its open size. Right tabs are
 * drawn from the GUI's right edge, left tabs towards the left; positions are relative to the GUI origin.
 */
public abstract class TeTab {
    public static final ResourceLocation ICONS = texture("icons");
    private static final ResourceLocation TAB_LEFT = texture("tab_left");
    private static final ResourceLocation TAB_RIGHT = texture("tab_right");
    protected static final int HEADER_COLOR = 0xE1C92F;
    protected static final int SUBHEADER_COLOR = 0xAAAFB8;
    protected static final int TEXT_COLOR = 0x000000;

    protected final boolean leftSide;
    protected final int backgroundColor;
    protected final int minWidth = 22;
    protected final int minHeight = 22;
    protected int maxWidth = 124;
    protected int maxHeight = 22;
    protected boolean open;
    protected int currentWidth = minWidth;
    protected int currentHeight = minHeight;
    /** Position of the tab's anchor edge, set while drawing. */
    int shiftX;
    int shiftY;

    protected TeTab(boolean leftSide, int backgroundColor) {
        this.leftSide = leftSide;
        this.backgroundColor = backgroundColor;
    }

    static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/gui/te/" + name + ".png");
    }

    public abstract void draw(GuiGraphics graphics, Font font, int x, int y);

    /** Tooltip while closed, {@code null} otherwise. */
    @Nullable
    public abstract Component getTooltip();

    /** True if the click was used inside the open tab; otherwise the tab toggles. */
    public boolean mouseClicked(int x, int y, int button) {
        return false;
    }

    void update() {
        if (open && currentWidth < maxWidth) {
            currentWidth += 8;
        } else if (!open && currentWidth > minWidth) {
            currentWidth -= 8;
        }
        currentWidth = Math.max(minWidth, Math.min(maxWidth, currentWidth));
        if (open && currentHeight < maxHeight) {
            currentHeight += 8;
        } else if (!open && currentHeight > minHeight) {
            currentHeight -= 8;
        }
        currentHeight = Math.max(minHeight, Math.min(maxHeight, currentHeight));
    }

    boolean intersects(int mouseX, int mouseY) {
        if (leftSide) {
            return mouseX <= shiftX && mouseX >= shiftX - currentWidth && mouseY >= shiftY
                    && mouseY <= shiftY + currentHeight;
        }
        return mouseX >= shiftX && mouseX <= shiftX + currentWidth && mouseY >= shiftY
                && mouseY <= shiftY + currentHeight;
    }

    protected boolean isFullyOpened() {
        return currentWidth >= maxWidth;
    }

    void setFullyOpen() {
        open = true;
        currentWidth = maxWidth;
        currentHeight = maxHeight;
    }

    protected void drawBackground(GuiGraphics graphics, int x, int y) {
        setColor(graphics, backgroundColor, 1.0F);
        int w = currentWidth;
        int h = currentHeight;
        if (leftSide) {
            graphics.blit(TAB_LEFT, x - w, y + 4, 0, 256 - h + 4, 4, h - 4);
            graphics.blit(TAB_LEFT, x - w + 4, y, 256 - w + 4, 0, w - 4, 4);
            graphics.blit(TAB_LEFT, x - w, y, 0, 0, 4, 4);
            graphics.blit(TAB_LEFT, x - w + 4, y + 4, 256 - w + 4, 256 - h + 4, w - 4, h - 4);
        } else {
            graphics.blit(TAB_RIGHT, x, y, 0, 256 - h, 4, h);
            graphics.blit(TAB_RIGHT, x + 4, y, 256 - w + 4, 0, w - 4, 4);
            graphics.blit(TAB_RIGHT, x, y, 0, 0, 4, 4);
            graphics.blit(TAB_RIGHT, x + 4, y + 4, 256 - w + 4, 256 - h + 4, w - 4, h - 4);
        }
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /** The darker inner panel of the redstone and configuration tabs (colour × 0.6). */
    protected void drawPanel(GuiGraphics graphics, int x, int y, int width, int height) {
        setColor(graphics, backgroundColor, 0.6F);
        graphics.blit(leftSide ? TAB_LEFT : TAB_RIGHT, x, y, 16, 20, width, height);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    protected static void drawIcon(GuiGraphics graphics, int index, int x, int y) {
        graphics.blit(ICONS, x, y, index % 16 * 16, index / 16 * 16, 16, 16);
    }

    private static void setColor(GuiGraphics graphics, int color, float factor) {
        graphics.setColor((color >> 16 & 0xFF) / 255.0F * factor, (color >> 8 & 0xFF) / 255.0F * factor,
                (color & 0xFF) / 255.0F * factor, 1.0F);
    }
}
