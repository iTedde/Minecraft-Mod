package dev.craft.powerplants.boiler;

import java.util.function.Supplier;

import com.google.common.base.Suppliers;
import dev.craft.core.multiblock.BlockMatcher;
import dev.craft.core.multiblock.MultiblockPattern;
import dev.craft.powerplants.PpBlocks;
import dev.craft.powerplants.block.PlantShapes;
import net.minecraft.world.level.block.Block;

/**
 * Size of a boiler: a hollow cube of casings with the firebox in the middle of one side. The small boiler is the
 * coal power plant boiler (spec 14 §3), the high-pressure boiler its large stage (spec 23 §1).
 */
public final class BoilerTier {
    public static final BoilerTier SMALL = new BoilerTier(3, BoilerBlockEntity.TANK_CAPACITY,
            BoilerBlockEntity.MAX_TEMPERATURE, BoilerBlockEntity.MAX_STEAM_RATE, BoilerBlockEntity.COAL_BURN_TIME,
            BoilerBlockEntity.COKE_BURN_TIME, BoilerBlockEntity.EXPLOSION_POWER,
            PpBlocks.BOILER_CASING, PpBlocks.BOILER_FIREBOX);
    public static final BoilerTier HIGH_PRESSURE = new BoilerTier(5, 64000, 1500, 160, 150, 300, 10.0F,
            PpBlocks.HIGH_PRESSURE_BOILER_CASING, PpBlocks.HIGH_PRESSURE_BOILER_FIREBOX);

    private final int size;
    private final int tankCapacity;
    private final int maxTemperature;
    private final int maxSteamRate;
    private final int coalBurnTime;
    private final int cokeBurnTime;
    private final float explosionPower;
    private final Supplier<? extends Block> casing;
    private final Supplier<? extends Block> firebox;
    private final Supplier<MultiblockPattern> pattern;

    private BoilerTier(int size, int tankCapacity, int maxTemperature, int maxSteamRate, int coalBurnTime,
                       int cokeBurnTime, float explosionPower, Supplier<? extends Block> casing,
                       Supplier<? extends Block> firebox) {
        this.size = size;
        this.tankCapacity = tankCapacity;
        this.maxTemperature = maxTemperature;
        this.maxSteamRate = maxSteamRate;
        this.coalBurnTime = coalBurnTime;
        this.cokeBurnTime = cokeBurnTime;
        this.explosionPower = explosionPower;
        this.casing = casing;
        this.firebox = firebox;
        this.pattern = Suppliers.memoize(() -> PlantShapes.box(size, size, size,
                        (x, y, z) -> PlantShapes.onShell(size, size, size, x, y, z) ? 'B' : 'A')
                .where('B', BlockMatcher.blocks(casing.get()))
                .where('A', BlockMatcher.AIR)
                .controller(PlantShapes.CONTROLLER, BlockMatcher.blocks(firebox.get()))
                .build());
    }

    /** Edge length of the cube. */
    public int size() {
        return size;
    }

    public int tankCapacity() {
        return tankCapacity;
    }

    public int maxTemperature() {
        return maxTemperature;
    }

    /** Steam per tick at {@link #maxTemperature()}; scales linearly from the boiling point. */
    public int maxSteamRate() {
        return maxSteamRate;
    }

    public int coalBurnTime() {
        return coalBurnTime;
    }

    public int cokeBurnTime() {
        return cokeBurnTime;
    }

    public float explosionPower() {
        return explosionPower;
    }

    public MultiblockPattern pattern() {
        return pattern.get();
    }

    /** Whether the block belongs to a boiler of this size. */
    public boolean isPart(Block block) {
        return block == casing.get() || block == firebox.get();
    }
}
