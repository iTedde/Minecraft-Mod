package dev.craft.mffs.item;

import dev.craft.mffs.block.MffsMachineBlockEntity;
import dev.craft.mffs.energy.ForceEnergyItem;
import dev.craft.mffs.energy.PowerLinkItem;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * {@code ItemForcePowerCrystal} (spec 48 §6.2): portable source with 5 000 000 FE, charged/discharged in the capacitor
 * at 100 000 per step, usable directly as power source of a machine. Icon by fill level (5 steps).
 */
public class ForceEnergyCrystalItem extends Item implements PowerLinkItem, ForceEnergyItem {
    public static final int MAX_POWER = 5000000;

    public ForceEnergyCrystalItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getMaximumPower(ItemStack stack) {
        return MAX_POWER;
    }

    @Override
    public int getPowerTransferRate() {
        return 100000;
    }

    /** Icon step 0–4 ({@code 112 + (100 − damage) / 20}, damage {@code 101 − energy · 100 / max}). */
    public static int fillStep(ItemStack stack) {
        if (!(stack.getItem() instanceof ForceEnergyCrystalItem crystal)) {
            return 0;
        }
        int damage = 101 - crystal.getAvailablePower(stack) * 100 / MAX_POWER;
        return Math.max(0, Math.min(4, (100 - damage) / 20));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return ForceEnergyItem.barWidth(getAvailablePower(stack), MAX_POWER);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x55FF55;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.craft.mffs.force_energy", getAvailablePower(stack), MAX_POWER));
    }

    // --- PowerLinkItem ---

    @Override
    public int getAvailablePower(ItemStack stack, MffsMachineBlockEntity machine) {
        return getAvailablePower(stack);
    }

    @Override
    public int getMaximumPower(ItemStack stack, MffsMachineBlockEntity machine) {
        return MAX_POWER;
    }

    @Override
    public int getPowerSourceId(ItemStack stack, MffsMachineBlockEntity machine) {
        return -1;
    }

    @Override
    public int getPercentageCapacity(ItemStack stack, MffsMachineBlockEntity machine) {
        return getAvailablePower(stack) / 1000 * 100 / (MAX_POWER / 1000);
    }

    @Override
    public boolean consumePower(ItemStack stack, int amount, boolean simulate, MffsMachineBlockEntity machine) {
        return consumePower(stack, amount, simulate);
    }

    @Override
    public boolean insertPower(ItemStack stack, int amount, boolean simulate, MffsMachineBlockEntity machine) {
        return chargeItem(stack, amount, simulate);
    }

    @Override
    public int getFreeStorageAmount(ItemStack stack, MffsMachineBlockEntity machine) {
        return MAX_POWER - getAvailablePower(stack);
    }

    @Override
    public boolean isPowerSourceItem() {
        return true;
    }
}
