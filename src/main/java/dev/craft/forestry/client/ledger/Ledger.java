package dev.craft.forestry.client.ledger;

import dev.craft.Craft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Forestry {@code Ledger}: a tab to the right of the GUI that slides open (4 px per frame) on a click. Coordinates
 * passed to {@link #draw} are screen coordinates.
 */
public abstract class Ledger {
    protected static final ResourceLocation ERRORS = texture("forestry_errors");
    protected static final ResourceLocation MISC = texture("forestry_misc");
    private static final ResourceLocation BACKGROUND = texture("ledger");
    protected static final int HEADER_COLOR = 0xE1C92F;
    protected static final int SUBHEADER_COLOR = 0xAAAFB8;
    protected static final int TEXT_COLOR = 0x000000;
    protected static final int MIN_SIZE = 24;
    protected static final int MAX_WIDTH = 124;

    private final int overlayColor;
    private final int maxHeight;
    private boolean open;
    private int width = MIN_SIZE;
    private int height = MIN_SIZE;

    protected Ledger(int overlayColor, int maxHeight) {
        this.overlayColor = overlayColor;
        this.maxHeight = maxHeight;
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/gui/" + name + ".png");
    }

    void update() {
        if (open && width < MAX_WIDTH) {
            width += 4;
        } else if (!open && width > MIN_SIZE) {
            width -= 4;
        }
        if (open && height < maxHeight) {
            height += 4;
        } else if (!open && height > MIN_SIZE) {
            height -= 4;
        }
    }

    public abstract void draw(GuiGraphics graphics, Font font, int x, int y);

    public abstract Component getTooltip();

    public boolean isVisible() {
        return true;
    }

    int getWidth() {
        return width;
    }

    int getHeight() {
        return height;
    }

    boolean isOpen() {
        return open;
    }

    void setOpen(boolean open) {
        this.open = open;
    }

    void setFullyOpen() {
        open = true;
        width = MAX_WIDTH;
        height = maxHeight;
    }

    protected boolean isFullyOpened() {
        return width >= MAX_WIDTH;
    }

    /** The four corners of {@code ledger.png}, tinted with the overlay colour. */
    protected void drawBackground(GuiGraphics graphics, int x, int y) {
        graphics.setColor((overlayColor >> 16 & 0xFF) / 255.0F, (overlayColor >> 8 & 0xFF) / 255.0F,
                (overlayColor & 0xFF) / 255.0F, 1.0F);
        graphics.blit(BACKGROUND, x, y, 0, 256 - height, 4, height);
        graphics.blit(BACKGROUND, x + 4, y, 256 - width + 4, 0, width - 4, 4);
        graphics.blit(BACKGROUND, x, y, 0, 0, 4, 4);
        graphics.blit(BACKGROUND, x + 4, y + 4, 256 - width + 4, 256 - height + 4, width - 4, height - 4);
        graphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);
    }

    /** Icon {@code index} of a 256 × 256 sheet with 16 icons per row. */
    protected void drawIcon(GuiGraphics graphics, ResourceLocation sheet, int index, int x, int y) {
        graphics.blit(sheet, x, y, 16 * (index & 15), 16 * (index >> 4), 16, 16);
    }

    /** Header in the original's shadowed style. */
    protected void drawHeader(GuiGraphics graphics, Font font, Component text, int x, int y, int color) {
        graphics.drawString(font, text, x, y, color, true);
    }

    protected void drawValue(GuiGraphics graphics, Font font, String text, int x, int y) {
        graphics.drawString(font, text, x, y, TEXT_COLOR, false);
    }

    /** {@code drawSplitString} over the open width minus icon and border. */
    protected void drawWrapped(GuiGraphics graphics, Font font, Component text, int x, int y) {
        graphics.drawWordWrap(font, text, x, y, MAX_WIDTH - 28, TEXT_COLOR);
    }
}
