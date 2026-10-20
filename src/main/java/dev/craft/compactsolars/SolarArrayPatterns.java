package dev.craft.compactsolars;

import java.util.EnumMap;
import java.util.Map;

import dev.craft.core.multiblock.MultiblockPattern;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Machine;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Flat 3×3 layout of the solar arrays: the array in the centre, the ring made of IC2 solar panels and arrays of at
 * most the centre's tier, mixed freely.
 */
public final class SolarArrayPatterns {
    public static final char RING = 'R';
    public static final int RADIUS = 1;

    private static final Map<SolarArrayType, MultiblockPattern> PATTERNS = new EnumMap<>(SolarArrayType.class);

    private SolarArrayPatterns() {
    }

    /** Rows from north to south; the centre array sits in the middle. */
    public static String[] layout(SolarArrayType type) {
        return new String[] {"RRR", "R" + type.getSymbol() + "R", "RRR"};
    }

    public static synchronized MultiblockPattern pattern(SolarArrayType type) {
        return PATTERNS.computeIfAbsent(type, SolarArrayPatterns::build);
    }

    private static MultiblockPattern build(SolarArrayType type) {
        return MultiblockPattern.builder().layer(layout(type))
                .controller(type.getSymbol(), (level, pos, state) -> state.is(CsBlocks.array(type).get()))
                .where(RING, (level, pos, state) -> isRingPart(type, state))
                .build();
    }

    private static boolean isRingPart(SolarArrayType centre, BlockState state) {
        if (state.is(Ic2Blocks.machine(Ic2Machine.SOLAR_PANEL).get())) {
            return true;
        }
        for (SolarArrayType type : SolarArrayType.values()) {
            if (type.ordinal() <= centre.ordinal() && state.is(CsBlocks.array(type).get())) {
                return true;
            }
        }
        return false;
    }
}
