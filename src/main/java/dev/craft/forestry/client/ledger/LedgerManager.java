package dev.craft.forestry.client.ledger;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/**
 * Forestry {@code LedgerManager}: the visible ledgers stacked from 8 px below the top right corner of the GUI. Only one
 * ledger is open at a time; its class is remembered for the session ({@code SessionVars.openedLedger}) and opens
 * fully in the next GUI.
 */
public class LedgerManager {
    @Nullable
    private static Class<? extends Ledger> openedLedger;

    private final List<Ledger> ledgers = new ArrayList<>();

    public void add(Ledger ledger) {
        ledgers.add(ledger);
        if (ledger.getClass() == openedLedger) {
            ledger.setFullyOpen();
        }
    }

    /** Draws at the GUI's right edge; {@code left}/{@code top} is the GUI origin on screen. */
    public void draw(GuiGraphics graphics, Font font, int left, int top, int imageWidth) {
        int y = top + 8;
        for (Ledger ledger : ledgers) {
            ledger.update();
            if (ledger.isVisible()) {
                ledger.draw(graphics, font, left + imageWidth, y);
                y += ledger.getHeight();
            }
        }
    }

    @Nullable
    private Ledger getAt(double mouseX, double mouseY, int left, int top, int imageWidth) {
        int x = left + imageWidth;
        int y = top + 8;
        for (Ledger ledger : ledgers) {
            if (ledger.isVisible()) {
                if (mouseX >= x && mouseX <= x + ledger.getWidth() && mouseY >= y && mouseY <= y + ledger.getHeight()) {
                    return ledger;
                }
                y += ledger.getHeight();
            }
        }
        return null;
    }

    public boolean contains(double mouseX, double mouseY, int left, int top, int imageWidth) {
        return getAt(mouseX, mouseY, left, top, imageWidth) != null;
    }

    /** The tooltip in a dark box above and right of the mouse, like the original. */
    public void drawTooltip(GuiGraphics graphics, Font font, int mouseX, int mouseY, int left, int top,
                            int imageWidth) {
        Ledger ledger = getAt(mouseX, mouseY, left, top, imageWidth);
        if (ledger == null) {
            return;
        }
        Component tooltip = ledger.getTooltip();
        int x = mouseX + 12;
        int y = mouseY - 12;
        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 400.0F);
        graphics.fill(x - 3, y - 3, x + font.width(tooltip) + 3, y + 8 + 3, 0xC0000000);
        graphics.drawString(font, tooltip, x, y, 0xFFFFFFFF, true);
        graphics.pose().popPose();
    }

    /** {@code handleMouseClicked}: toggles the clicked ledger and closes the others. */
    public boolean click(double mouseX, double mouseY, int left, int top, int imageWidth) {
        Ledger clicked = getAt(mouseX, mouseY, left, top, imageWidth);
        if (clicked == null) {
            return false;
        }
        for (Ledger ledger : ledgers) {
            if (ledger != clicked) {
                ledger.setOpen(false);
            }
        }
        clicked.setOpen(!clicked.isOpen());
        openedLedger = clicked.isOpen() ? clicked.getClass() : null;
        return true;
    }

    /** Screen areas of the visible ledgers, for recipe viewers to keep clear. */
    public void addAreas(Consumer<Rect2i> consumer, int left, int top, int imageWidth) {
        int y = top + 8;
        for (Ledger ledger : ledgers) {
            if (ledger.isVisible()) {
                consumer.accept(new Rect2i(left + imageWidth, y, ledger.getWidth(), ledger.getHeight()));
                y += ledger.getHeight();
            }
        }
    }
}
