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
 * {@code ItemCardPower} (spec 48 §6.2, creative only): always 10 000 000 FE, consuming is free, nothing can be put in;
 * in the capacitor it discharges any amount (rate 1 000 000).
 */
public class InfinitePowerCardItem extends Item implements PowerLinkItem, ForceEnergyItem {
    private static final int POWER = 10000000;

    public InfinitePowerCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getMaximumPower(ItemStack stack) {
        return POWER;
    }

    @Override
    public int getPowerTransferRate() {
        return 1000000;
    }

    @Override
    public int getAvailablePower(ItemStack stack) {
        return POWER;
    }

    @Override
    public void setAvailablePower(ItemStack stack, int amount) {
    }

    @Override
    public boolean consumePower(ItemStack stack, int amount, boolean simulate) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.craft.mffs.infinite_power_card.1"));
        tooltip.add(Component.translatable("tooltip.craft.mffs.infinite_power_card.2"));
    }

    @Override
    public int getAvailablePower(ItemStack stack, MffsMachineBlockEntity machine) {
        return POWER;
    }

    @Override
    public int getMaximumPower(ItemStack stack, MffsMachineBlockEntity machine) {
        return POWER;
    }

    @Override
    public int getPowerSourceId(ItemStack stack, MffsMachineBlockEntity machine) {
        return 0;
    }

    @Override
    public int getPercentageCapacity(ItemStack stack, MffsMachineBlockEntity machine) {
        return 100;
    }

    @Override
    public boolean consumePower(ItemStack stack, int amount, boolean simulate, MffsMachineBlockEntity machine) {
        return true;
    }

    @Override
    public boolean insertPower(ItemStack stack, int amount, boolean simulate, MffsMachineBlockEntity machine) {
        return false;
    }

    @Override
    public int getFreeStorageAmount(ItemStack stack, MffsMachineBlockEntity machine) {
        return 0;
    }

    @Override
    public boolean isPowerSourceItem() {
        return true;
    }
}
