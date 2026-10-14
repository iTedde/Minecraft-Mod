package dev.craft.ic2.generator;

import java.util.Random;

import dev.craft.Craft;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Global wind strength (IC2 {@code windStrength}): starts at 10–19, every 128 level ticks (counted over all levels)
 * the overworld drifts it by ±1.
 */
@EventBusSubscriber(modid = Craft.MODID)
public final class Wind {
    private static final Random RANDOM = new Random();
    private static int strength = 10 + RANDOM.nextInt(10);
    private static int ticker;

    private Wind() {
    }

    public static int getStrength() {
        return strength;
    }

    /** For tests. */
    public static void setStrength(int value) {
        strength = value;
    }

    @SubscribeEvent
    public static void onServerStarting(ServerStartingEvent event) {
        strength = 10 + RANDOM.nextInt(10);
        ticker = 0;
    }

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Pre event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (ticker % 128 == 0) {
            update(level);
        }
        ticker++;
    }

    private static void update(ServerLevel level) {
        if (level.dimension() != Level.OVERWORLD) {
            return;
        }
        int upChance = 10;
        int downChance = 10;
        if (strength > 20) {
            upChance -= strength - 20;
        }
        if (strength < 10) {
            downChance -= 10 - strength;
        }
        if (RANDOM.nextInt(100) <= upChance) {
            strength++;
        } else if (RANDOM.nextInt(100) <= downChance) {
            strength--;
        }
    }
}
