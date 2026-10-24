package dev.craft.ic2.item;

import net.minecraft.world.item.ItemStack;

/**
 * IC2 {@code ICustomElectricItem}: an {@link ElectricItem} that handles charging itself. {@link ElectricItems} hands
 * every call over unchanged, without its own tier, transfer or stack checks.
 */
public interface CustomElectricItem extends ElectricItem {
    /** @return EU actually (or, when simulating, possibly) taken */
    int charge(ItemStack stack, int amount, int tier, boolean ignoreTransferLimit, boolean simulate);

    /** @return EU actually (or, when simulating, possibly) given */
    int discharge(ItemStack stack, int amount, int tier, boolean ignoreTransferLimit, boolean simulate);

    boolean canUse(ItemStack stack, int amount);

    /** Stored energy in EU, for displays. */
    int getCharge(ItemStack stack);
}
