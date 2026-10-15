package dev.craft.compactsolars;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import dev.craft.core.multiblock.BlockMatcher;
import dev.craft.core.multiblock.MultiblockPattern;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Machine;

/**
 * Flat layouts of the solar arrays: LV = 8 solar panels around the LV controller; every higher tier is a 3×3 grid of
 * the tier below whose centre field has its controller replaced by the higher one.
 */
public final class SolarArrayPatterns {
    public static final char PANEL = 'P';

    private static final Map<SolarArrayType, MultiblockPattern> PATTERNS = new EnumMap<>(SolarArrayType.class);

    private SolarArrayPatterns() {
    }

    /** Rows from north to south; the controller sits in the centre. */
    public static String[] layout(SolarArrayType type) {
        char[][] cells = cells(type, type.getSymbol());
        String[] rows = new String[cells.length];
        for (int z = 0; z < cells.length; z++) {
            rows[z] = new String(cells[z]);
        }
        return rows;
    }

    private static char[][] cells(SolarArrayType type, char centre) {
        if (type.ordinal() == 0) {
            char[][] cells = new char[3][3];
            for (char[] row : cells) {
                Arrays.fill(row, PANEL);
            }
            cells[1][1] = centre;
            return cells;
        }
        SolarArrayType lower = SolarArrayType.values()[type.ordinal() - 1];
        int field = lower.getSize();
        char[][] cells = new char[field * 3][field * 3];
        for (int fz = 0; fz < 3; fz++) {
            for (int fx = 0; fx < 3; fx++) {
                char[][] sub = cells(lower, fx == 1 && fz == 1 ? centre : lower.getSymbol());
                for (int z = 0; z < field; z++) {
                    System.arraycopy(sub[z], 0, cells[fz * field + z], fx * field, field);
                }
            }
        }
        return cells;
    }

    public static synchronized MultiblockPattern pattern(SolarArrayType type) {
        return PATTERNS.computeIfAbsent(type, SolarArrayPatterns::build);
    }

    private static MultiblockPattern build(SolarArrayType type) {
        MultiblockPattern.Builder builder = MultiblockPattern.builder().layer(layout(type))
                .where(PANEL, (level, pos, state) -> state.is(Ic2Blocks.machine(Ic2Machine.SOLAR_PANEL).get()));
        for (SolarArrayType other : SolarArrayType.values()) {
            BlockMatcher matcher = (level, pos, state) -> state.is(CsBlocks.array(other).get());
            if (other == type) {
                builder.controller(other.getSymbol(), matcher);
            } else if (other.ordinal() < type.ordinal()) {
                builder.where(other.getSymbol(), matcher);
            }
        }
        return builder.build();
    }
}
