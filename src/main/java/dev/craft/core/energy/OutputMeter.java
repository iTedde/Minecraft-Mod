package dev.craft.core.energy;

import dev.craft.core.energy.OutputReadout.OutputReading;
import net.minecraft.world.level.Level;

/**
 * Counts the energy a producer generates and averages it over windows of one second, so values that only come in
 * bursts (wind, redstone engine, reactor cycles) still read as a steady rate. Display only, not saved.
 */
public final class OutputMeter {
    private static final int WINDOW = 20;

    private long windowStart = -1;
    private double sum;
    private double rate;

    /** Adds energy generated in the current tick. */
    public void record(Level level, double amount) {
        roll(level.getGameTime());
        sum += amount;
    }

    public OutputReading read(Level level, OutputReading.Unit unit) {
        roll(level.getGameTime());
        return new OutputReading(rate, unit);
    }

    private void roll(long now) {
        if (windowStart < 0) {
            windowStart = now;
            return;
        }
        long elapsed = now - windowStart;
        if (elapsed < WINDOW) {
            return;
        }
        // a window without any record call in between means the producer was idle (or not ticking)
        boolean consecutive = elapsed < 2 * WINDOW;
        rate = consecutive ? sum / WINDOW : 0.0;
        windowStart = consecutive ? windowStart + WINDOW : now;
        sum = 0.0;
    }
}
