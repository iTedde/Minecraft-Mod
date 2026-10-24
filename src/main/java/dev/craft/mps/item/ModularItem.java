package dev.craft.mps.item;

import dev.craft.ic2.item.CustomElectricItem;
import dev.craft.mps.MpsEnergy;
import dev.craft.mps.module.ModuleTarget;
import net.minecraft.world.item.ItemStack;

/**
 * MPS {@code IModularItem}: power armor and power tool. Charged like IC2's {@code ICustomElectricItem} with tier 1,
 * {@code getMaxCharge} 1 and transfer limit 0 as in the pack; they can also power other items (spec 29 §2).
 */
public interface ModularItem extends CustomElectricItem {
    ModuleTarget getTarget();

    /** Armor caps each IC2 transfer at a 200th of its capacity, the tool does not. */
    default boolean hasTransferCap() {
        return getTarget() != ModuleTarget.TOOL;
    }

    @Override
    default int charge(ItemStack stack, int amount, int tier, boolean ignoreTransferLimit, boolean simulate) {
        return MpsEnergy.chargeEu(stack, amount, hasTransferCap(), ignoreTransferLimit, simulate);
    }

    @Override
    default int discharge(ItemStack stack, int amount, int tier, boolean ignoreTransferLimit, boolean simulate) {
        return MpsEnergy.dischargeEu(stack, amount, hasTransferCap(), ignoreTransferLimit, simulate);
    }

    @Override
    default boolean canUse(ItemStack stack, int amount) {
        return MpsEnergy.canUseEu(stack, amount);
    }

    @Override
    default int getCharge(ItemStack stack) {
        return (int) (MpsEnergy.getJoules(stack) / MpsEnergy.JOULES_PER_EU);
    }

    @Override
    default int getMaxCharge(ItemStack stack) {
        return (int) (MpsEnergy.getMaxJoules(stack) / MpsEnergy.JOULES_PER_EU);
    }

    @Override
    default int getMaxCharge() {
        return 1;
    }

    @Override
    default int getTransferLimit() {
        return 0;
    }

    @Override
    default int getTier() {
        return 1;
    }

    @Override
    default boolean canProvideEnergy() {
        return true;
    }
}
