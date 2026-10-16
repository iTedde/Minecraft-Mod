package dev.craft.compat.emi;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Number formatting for recipe texts (thousands separator, seconds with up to two decimals). */
final class EmiText {
    private EmiText() {
    }

    static String number(long value) {
        return String.format(Locale.ROOT, "%,d", value).replace(',', ' ');
    }

    static String seconds(int ticks) {
        return new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT)).format(ticks / 20.0);
    }
}
