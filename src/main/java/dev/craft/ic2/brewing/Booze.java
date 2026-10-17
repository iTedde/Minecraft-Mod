package dev.craft.ic2.brewing;

import dev.craft.ic2.Ic2DataComponents;
import net.minecraft.world.item.ItemStack;

/**
 * The packed booze value of IC2 (barrel item and booze mug metadata, {@code ItemBooze.get*OfValue}): bits 0–1 type
 * (0 none, 1 beer, 2 rum), bits 2–6 amount − 1, then for beer three bits each solid, hops and time ratio, for rum
 * seven bits progress in percent.
 */
public final class Booze {
    public static final int TYPE_NONE = 0;
    public static final int TYPE_BEER = 1;
    public static final int TYPE_RUM = 2;
    /** Time ratio of spoiled beer ("Black Stuff"). */
    public static final int BLACK_STUFF = 5;

    private Booze() {
    }

    public static int getValue(ItemStack stack) {
        return stack.getOrDefault(Ic2DataComponents.BOOZE.get(), 0);
    }

    public static int getType(int value) {
        return bits(value, 0, 2);
    }

    public static int getAmount(int value) {
        return getType(value) == TYPE_NONE ? 0 : bits(value, 2, 5) + 1;
    }

    public static int getSolidRatio(int value) {
        return bits(value, 7, 3);
    }

    public static int getHopsRatio(int value) {
        return bits(value, 10, 3);
    }

    public static int getTimeRatio(int value) {
        return bits(value, 13, 3);
    }

    public static int getRumProgress(int value) {
        return bits(value, 7, 7);
    }

    public static int beer(int amount, int solidRatio, int hopsRatio, int timeRatio) {
        return ((((timeRatio << 3 | hopsRatio) << 3 | solidRatio) << 5 | amount - 1) << 2) | TYPE_BEER;
    }

    public static int rum(int amount, int progress) {
        return ((progress << 5 | amount - 1) << 2) | TYPE_RUM;
    }

    private static int bits(int value, int shift, int count) {
        return value >> shift & (1 << count) - 1;
    }
}
