package dev.craft.ic2.item;

import net.minecraft.world.item.ItemStack;

/** An item that stores EU in {@link dev.craft.ic2.Ic2DataComponents#CHARGE} (IC2 {@code IElectricItem}). */
public interface ElectricItem {
    int getMaxCharge();

    /** Capacity of {@code stack} in EU, for displays; differs from {@link #getMaxCharge()} for modular items. */
    default int getMaxCharge(ItemStack stack) {
        return getMaxCharge();
    }

    /** EU per call when charging/discharging with transfer limit. */
    int getTransferLimit();

    int getTier();

    /** Whether the item can power machines and storage blocks from their discharge slot. */
    boolean canProvideEnergy();

    /** Called after the charge of {@code stack} changed, e.g. to switch between empty and charged state. */
    default void onChargeChanged(ItemStack stack, int charge) {
    }

    /**
     * IC2 shows the charge through the item damage: {@code 1 + (max - charge) * 25 / max} for the usual max damage 27.
     * Returns that value, used for icons and the charge bar.
     */
    default int getChargeDamage(ItemStack stack) {
        return 1 + (getMaxCharge() - ElectricItems.getCharge(stack)) * (ElectricItems.DISPLAY_MAX_DAMAGE - 2)
                / getMaxCharge();
    }
}
