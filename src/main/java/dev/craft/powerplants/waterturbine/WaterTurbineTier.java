package dev.craft.powerplants.waterturbine;

import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import dev.craft.core.multiblock.BlockMatcher;
import dev.craft.core.multiblock.MultiblockPattern;
import dev.craft.powerplants.PpBlocks;
import dev.craft.powerplants.block.PlantShapes;
import net.minecraft.world.level.block.Block;

/**
 * Size of a water turbine: a solid cube of casings with the controller in the middle of one side. The small turbine
 * is spec 14 §5, the large one spec 23 §3; both count the flowing water around and above the cube.
 */
public final class WaterTurbineTier {
    public static final WaterTurbineTier SMALL = new WaterTurbineTier(3, WaterTurbineBlockEntity.AREA_RADIUS,
            WaterTurbineBlockEntity.AREA_HEIGHT, WaterTurbineBlockEntity.MIN_FLOWING,
            WaterTurbineBlockEntity.YIELD_PER_BLOCK, WaterTurbineBlockEntity.MAX_OUTPUT,
            WaterTurbineBlockEntity.MAX_OUTPUT, PpBlocks.WATER_TURBINE_CASING, PpBlocks.WATER_TURBINE);
    public static final WaterTurbineTier LARGE = new WaterTurbineTier(5, 7, 24, 64, 1.0, 256, 384,
            PpBlocks.LARGE_WATER_TURBINE_CASING, PpBlocks.LARGE_WATER_TURBINE);

    private final int size;
    private final int areaRadius;
    private final int areaHeight;
    private final int minFlowing;
    private final double yieldPerBlock;
    private final int maxOutput;
    private final int packetSize;
    private final Supplier<MultiblockPattern> pattern;

    private WaterTurbineTier(int size, int areaRadius, int areaHeight, int minFlowing, double yieldPerBlock,
                             int maxOutput, int packetSize, Supplier<? extends Block> casing,
                             Supplier<? extends Block> controller) {
        this.size = size;
        this.areaRadius = areaRadius;
        this.areaHeight = areaHeight;
        this.minFlowing = minFlowing;
        this.yieldPerBlock = yieldPerBlock;
        this.maxOutput = maxOutput;
        this.packetSize = packetSize;
        this.pattern = Suppliers.memoize(() -> PlantShapes.box(size, size, size, (x, y, z) -> 'H')
                .where('H', BlockMatcher.blocks(casing.get()))
                .controller(PlantShapes.CONTROLLER, BlockMatcher.blocks(controller.get()))
                .build());
    }

    /** Edge length of the cube. */
    public int size() {
        return size;
    }

    /** Horizontal reach of the counted area beyond the cube's side faces. */
    public int areaRadius() {
        return areaRadius;
    }

    /** Height of the counted area above the cube's top layer. */
    public int areaHeight() {
        return areaHeight;
    }

    public int minFlowing() {
        return minFlowing;
    }

    /** Largest output per tick outside rivers. */
    public int maxOutput() {
        return maxOutput;
    }

    /** Largest output per tick: the river factor also raises the cap (spec 32 §3). */
    public int maxOutput(boolean river) {
        return river ? (int) Math.round(maxOutput * WaterTurbineBlockEntity.RIVER_FACTOR) : maxOutput;
    }

    /** Largest packet: the small turbine stays LV and sends more than 32 EU/t as two packets. */
    public int packetSize() {
        return packetSize;
    }

    /** Accumulated EU are capped here so fractions carry over without piling up. */
    public int bufferCapacity() {
        return 2 * maxOutput(true);
    }

    /** EU/t for the given number of counted flowing water blocks. */
    public double output(int flowing, boolean river) {
        if (flowing < minFlowing) {
            return 0.0;
        }
        return Math.min(yieldPerBlock * flowing * (river ? WaterTurbineBlockEntity.RIVER_FACTOR : 1.0),
                maxOutput(river));
    }

    public MultiblockPattern pattern() {
        return pattern.get();
    }
}
