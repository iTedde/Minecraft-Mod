package dev.craft.ic2.item.armor;

import dev.craft.ic2.item.ElectricItem;
import dev.craft.ic2.item.ElectricItems;
import net.minecraft.world.item.ItemStack;

/** IC2 {@code ItemArmorJetpackElectric}: 30 000 EU, transfer 60, tier 1. */
public class ElectricJetpackItem extends JetpackItem implements ElectricItem {
    public ElectricJetpackItem(Properties properties) {
        super(properties, true);
    }

    @Override
    public int getCharge(ItemStack stack) {
        return ElectricItems.discharge(stack, Integer.MAX_VALUE, Integer.MAX_VALUE, true, true);
    }

    /**
     * Not overridden in IC2: the fuel formula {@code maxDamage - 2} with the electric max damage 27, so the power
     * drop only starts below 1.25 EU.
     */
    @Override
    public int getMaxCharge(ItemStack stack) {
        return ElectricItems.DISPLAY_MAX_DAMAGE - 2;
    }

    @Override
    public void use(ItemStack stack, int amount) {
        ElectricItems.discharge(stack, amount, Integer.MAX_VALUE, true, false);
    }

    @Override
    public int getMaxCharge() {
        return 30000;
    }

    @Override
    public int getTransferLimit() {
        return 60;
    }

    @Override
    public int getTier() {
        return 1;
    }

    @Override
    public boolean canProvideEnergy() {
        return false;
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return ElectricItems.barWidth(this, stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return ElectricItems.barColor(this, stack);
    }
}
