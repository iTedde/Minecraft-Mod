package dev.craft.nuclearcontrol.card;

import javax.annotation.Nullable;

/**
 * {@code PanelString}: one line on the information panel with optional left, centred and right text. A colour of 0
 * means "the panel's text colour".
 */
public record PanelLine(@Nullable String left, int colorLeft, @Nullable String center, int colorCenter,
                        @Nullable String right, int colorRight) {
    public static PanelLine left(String text) {
        return new PanelLine(text, 0, null, 0, null, 0);
    }

    public static PanelLine left(String text, int color) {
        return new PanelLine(text, color, null, 0, null, 0);
    }

    public static PanelLine center(String text) {
        return new PanelLine(null, 0, text, 0, null, 0);
    }

    public PanelLine withRight(String text, int color) {
        return new PanelLine(left, colorLeft, center, colorCenter, text, color);
    }
}
