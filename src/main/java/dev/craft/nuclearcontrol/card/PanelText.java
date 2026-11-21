package dev.craft.nuclearcontrol.card;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

import net.minecraft.network.chat.Component;

/** {@code StringUtils}: panel texts, numbers grouped with spaces ({@code #,###.###}). */
public final class PanelText {
    public static final String PREFIX = "panel.craft.nuclearcontrol.";
    private static final ThreadLocal<DecimalFormat> FORMAT = ThreadLocal.withInitial(() -> {
        DecimalFormat format = new DecimalFormat("#,###.###");
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setGroupingSeparator(' ');
        format.setDecimalFormatSymbols(symbols);
        return format;
    });

    private PanelText() {
    }

    public static String number(long value) {
        return FORMAT.get().format(value);
    }

    /** {@code getFormatted}: with labels the translated template, otherwise only the value. */
    public static String labeled(String key, String value, boolean showLabels) {
        return showLabels ? Component.translatable(PREFIX + key, value).getString() : value;
    }

    public static String labeled(String key, long value, boolean showLabels) {
        return labeled(key, number(value), showLabels);
    }

    public static String text(String key, Object... args) {
        return Component.translatable(PREFIX + key, args).getString();
    }
}
