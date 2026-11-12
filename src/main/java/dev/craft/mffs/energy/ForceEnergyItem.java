package dev.craft.mffs.energy;

import dev.craft.mffs.MffsDataComponents;
import net.minecraft.world.item.ItemStack;

/**
 * {@code IForceEnergyItems} (spec 48 §6.1): an item that a capacitor charges or discharges in its slot 2 – the force
 * energy crystal, the multitool and the infinite power card. Default: the energy sits in {@code craft:force_energy}.
 */
public interface ForceEnergyItem {
    int getMaximumPower(ItemStack stack);

    /** Largest amount moved per capacitor step (every 10 ticks). */
    int getPowerTransferRate();

    default int getAvailablePower(ItemStack stack) {
        return stack.getOrDefault(MffsDataComponents.FORCE_ENERGY.get(), 0);
    }

    default void setAvailablePower(ItemStack stack, int amount) {
        stack.set(MffsDataComponents.FORCE_ENERGY.get(), amount);
    }

    /** {@code ForceEnergyItems.consumePower}. */
    default boolean consumePower(ItemStack stack, int amount, boolean simulate) {
        if (getAvailablePower(stack) < amount) {
            return false;
        }
        if (!simulate) {
            setAvailablePower(stack, getAvailablePower(stack) - amount);
        }
        return true;
    }

    /** {@code ForceEnergyItems.chargeItem}. */
    default boolean chargeItem(ItemStack stack, int amount, boolean simulate) {
        if (getAvailablePower(stack) + amount > getMaximumPower(stack)) {
            return false;
        }
        if (!simulate) {
            setAvailablePower(stack, getAvailablePower(stack) + amount);
        }
        return true;
    }

    /** Original item damage {@code 101 − energy · 100 / max} as a bar width (13 = full). */
    static int barWidth(int energy, int max) {
        return Math.round(13.0F * energy / max);
    }
}
