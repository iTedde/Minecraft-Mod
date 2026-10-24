package dev.craft.mps.client;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** MPS {@code MuseStringUtils} number formatting: three significant digits with SI suffix, percent values ×100. */
public final class MpsFormat {
    private static final String[] BIG = {"k", "M", "G", "T", "P", "E", "Z", "Y"};
    private static final String[] SMALL = {"m", "µ", "n", "p", "f", "a", "z", "y"};

    private MpsFormat() {
    }

    /** {@code formatNumberShort}: engineering notation, e.g. 2.5k, 100k, 5M. */
    public static String formatShort(double number) {
        DecimalFormat format = new DecimalFormat("##0.##E0", DecimalFormatSymbols.getInstance(Locale.ROOT));
        String[] exploded = format.format(number).split("E");
        String result = exploded[0];
        if (result.length() > 3) {
            result = result.charAt(3) == '.' ? result.substring(0, 3) : result.substring(0, 4);
        }
        if (exploded.length > 1) {
            int exponent = Integer.parseInt(exploded[1]);
            if (exponent > 0) {
                result += BIG[Math.min(exponent / 3, BIG.length) - 1];
            } else if (exponent < 0) {
                result += SMALL[Math.min(exponent / -3, SMALL.length) - 1];
            }
        }
        return result;
    }

    /** {@code formatNumberFromUnits}: "%" values as percent, others short with the unit appended. */
    public static String formatWithUnit(double number, String unit) {
        if (unit.equals("%")) {
            return new DecimalFormat("##0.##", DecimalFormatSymbols.getInstance(Locale.ROOT)).format(number * 100.0)
                    + "%";
        }
        return formatShort(number) + unit;
    }
}
