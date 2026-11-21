package dev.craft.mps;

import dev.craft.mps.module.MpsModules;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**
 * MPS {@code ElectricItemUtils} (spec 29 §2): energy in joules per item, the equipped items form one pool; IC2
 * charging at 50 J per EU ({@code Joules per IC2 EU} in the pack config).
 */
public final class MpsEnergy {
    public static final double JOULES_PER_EU = 50.0;

    private MpsEnergy() {
    }

    /** {@code getMaxJoules}: the battery modules' {@code Maximum Energy}. */
    public static double getMaxJoules(ItemStack stack) {
        return ModularItems.computeProperty(stack, MpsModules.MAXIMUM_ENERGY);
    }

    /** {@code getJoules}: capped at the capacity, the cap is written back (removing a battery loses the surplus). */
    public static double getJoules(ItemStack stack) {
        double joules = stack.getOrDefault(MpsDataComponents.ENERGY.get(), 0.0);
        double max = getMaxJoules(stack);
        if (joules > max) {
            joules = max;
            setJoules(stack, joules);
        }
        return joules;
    }

    public static void setJoules(ItemStack stack, double joules) {
        if (joules == 0.0) {
            stack.remove(MpsDataComponents.ENERGY.get());
        } else {
            stack.set(MpsDataComponents.ENERGY.get(), joules);
        }
    }

    /** {@code ElectricItemUtils.charge}: adds up to the capacity, returns the surplus. */
    public static double charge(ItemStack stack, double amount) {
        double stored = getJoules(stack);
        double taken = Math.min(amount, getMaxJoules(stack) - stored);
        setJoules(stack, stored + taken);
        return amount - taken;
    }

    /** {@code ElectricItemUtils.discharge}: takes what is there, returns the joules provided. */
    public static double discharge(ItemStack stack, double needed) {
        double available = getJoules(stack);
        double provided = Math.min(available, needed);
        setJoules(stack, available - provided);
        return provided;
    }

    /** {@code getPlayerEnergy}: all equipped modular items. */
    public static double getPlayerEnergy(Player player) {
        double energy = 0.0;
        for (ItemStack stack : ModularItems.equipped(player)) {
            if (ModularItems.isModular(stack)) {
                energy += getJoules(stack);
            }
        }
        return energy;
    }

    public static double getPlayerMaxEnergy(Player player) {
        double max = 0.0;
        for (ItemStack stack : ModularItems.equipped(player)) {
            if (ModularItems.isModular(stack)) {
                max += getMaxJoules(stack);
            }
        }
        return max;
    }

    /** {@code drainPlayerEnergy}: boots first; an item that holds enough pays alone, otherwise it is emptied. */
    public static void drainPlayerEnergy(Player player, double amount) {
        for (ItemStack stack : ModularItems.equipped(player)) {
            if (!ModularItems.isModular(stack)) {
                continue;
            }
            double joules = getJoules(stack);
            if (joules > amount) {
                discharge(stack, amount);
                return;
            }
            amount -= joules;
            discharge(stack, joules);
        }
    }

    /** {@code givePlayerEnergy}: fills the items in the same order, the rest is lost. */
    public static void givePlayerEnergy(Player player, double amount) {
        for (ItemStack stack : ModularItems.equipped(player)) {
            if (!ModularItems.isModular(stack)) {
                continue;
            }
            double missing = getMaxJoules(stack) - getJoules(stack);
            if (missing > amount) {
                charge(stack, amount);
                return;
            }
            amount -= missing;
            charge(stack, missing);
        }
    }

    /**
     * {@code ICustomElectricItem.charge}: armor limits each call to a 200th of its capacity unless the transfer limit
     * is ignored; the tool has no limit. Returns whole EU.
     */
    public static int chargeEu(ItemStack stack, int amount, boolean limited, boolean ignoreTransferLimit,
                               boolean simulate) {
        double provided = amount * JOULES_PER_EU;
        double max = getMaxJoules(stack);
        if (limited && !ignoreTransferLimit && provided > max / 200.0) {
            provided = max / 200.0;
        }
        double current = getJoules(stack);
        double surplus = charge(stack, provided);
        if (simulate) {
            setJoules(stack, current);
        }
        return (int) ((provided - surplus) / JOULES_PER_EU);
    }

    /** {@code ICustomElectricItem.discharge}, same limits as {@link #chargeEu}. */
    public static int dischargeEu(ItemStack stack, int amount, boolean limited, boolean ignoreTransferLimit,
                                  boolean simulate) {
        double requested = amount * JOULES_PER_EU;
        double max = getMaxJoules(stack);
        if (limited && !ignoreTransferLimit && requested > max / 200.0) {
            requested = max / 200.0;
        }
        double current = getJoules(stack);
        double given = discharge(stack, requested);
        if (simulate) {
            setJoules(stack, current);
        }
        return (int) (given / JOULES_PER_EU);
    }

    /** {@code canUse}: more joules stored than the EU amount needs. */
    public static boolean canUseEu(ItemStack stack, int amount) {
        return getJoules(stack) > amount * JOULES_PER_EU;
    }
}
