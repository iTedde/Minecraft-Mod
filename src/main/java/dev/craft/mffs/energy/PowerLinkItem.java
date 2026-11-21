package dev.craft.mffs.energy;

import dev.craft.mffs.block.MffsMachineBlockEntity;
import net.minecraft.world.item.ItemStack;

/**
 * {@code IPowerLinkItem} (spec 48 §6.2): an item in a machine's power slot that supplies force energy – the power
 * link card (a capacitor in range), the force energy crystal and the infinite power card.
 */
public interface PowerLinkItem {
    int getAvailablePower(ItemStack stack, MffsMachineBlockEntity machine);

    int getMaximumPower(ItemStack stack, MffsMachineBlockEntity machine);

    /** ID of the capacitor behind the item; 0 = none (infinite card, no link), −1 = crystal. */
    int getPowerSourceId(ItemStack stack, MffsMachineBlockEntity machine);

    int getPercentageCapacity(ItemStack stack, MffsMachineBlockEntity machine);

    boolean consumePower(ItemStack stack, int amount, boolean simulate, MffsMachineBlockEntity machine);

    boolean insertPower(ItemStack stack, int amount, boolean simulate, MffsMachineBlockEntity machine);

    int getFreeStorageAmount(ItemStack stack, MffsMachineBlockEntity machine);

    /** True for items that carry the energy themselves (crystal, infinite card; spec 48 §7.5). */
    boolean isPowerSourceItem();
}
