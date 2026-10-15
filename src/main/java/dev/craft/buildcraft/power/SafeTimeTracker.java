package dev.craft.buildcraft.power;

import net.minecraft.world.level.Level;

/** BuildCraft {@code SafeTimeTracker}: fires once every {@code delay} ticks of world time. */
public final class SafeTimeTracker {
    private long lastMark;

    public boolean markTimeIfDelay(Level level, long delay) {
        if (level == null) {
            return false;
        }
        long currentTime = level.getGameTime();
        if (currentTime < lastMark) {
            lastMark = currentTime;
            return false;
        }
        if (lastMark + delay <= currentTime) {
            lastMark = currentTime;
            return true;
        }
        return false;
    }

    public void markTime(Level level) {
        lastMark = level.getGameTime();
    }
}
