package dev.craft.compat.epl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Spec 39 §3: the extra "mined blocks" column of Enhanced Player List. EPL already sums all {@code minecraft:mined}
 * values of every player's stats file (online and offline) and syncs them; it only lacks a column for them.
 */
public final class EplMinedBlocks {
    /** Stat id in the EPL config list {@code visibleStats}. */
    public static final String STAT = "blocksMined";

    private EplMinedBlocks() {
    }

    /** EPL's default column list with {@link #STAT} inserted after the distance column. */
    public static List<String> withMinedBlocks(List<String> defaults) {
        if (defaults.contains(STAT)) {
            return defaults;
        }
        List<String> list = new ArrayList<>(defaults);
        int distance = list.indexOf("distance");
        list.add(distance < 0 ? list.size() : distance + 1, STAT);
        return List.copyOf(list);
    }

    /** Column value: plain number, in compact mode shortened to thousands/millions. */
    public static String format(long mined, boolean compact) {
        if (!compact || mined < 1000) {
            return String.format(Locale.ROOT, "%,d", mined);
        }
        if (mined < 1_000_000) {
            return String.format(Locale.ROOT, "%.1fk", mined / 1000.0);
        }
        return String.format(Locale.ROOT, "%.1fM", mined / 1_000_000.0);
    }

    /** Column header, matching EPL's English headers ("Playtime", "Deaths" …; compact: three capitals). */
    public static String header(boolean compact) {
        return compact ? "MIN" : "Mined";
    }
}
