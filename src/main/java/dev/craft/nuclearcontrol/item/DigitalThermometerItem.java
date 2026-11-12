package dev.craft.nuclearcontrol.item;

import dev.craft.ic2.item.ElectricItem;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.reactor.NuclearReactorBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;

/**
 * Digital thermometer ({@code ItemToolDigitalThermometer}): 12 000 EU, tier 1, transfer 250. A reading needs 100 EU
 * available ({@code canTakeDamage(2)} × 50) and uses 50; it also reports 50 % and 85 % of the maximum heat.
 */
public class DigitalThermometerItem extends ThermometerItem implements ElectricItem {
    public static final int ENERGY_PER_USE = 50;

    public DigitalThermometerItem(Properties properties) {
        super(properties.setNoRepair(), false);
    }

    @Override
    public int getMaxCharge() {
        return 12000;
    }

    @Override
    public int getTransferLimit() {
        return 250;
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
    protected boolean canTakeDamage(ItemStack stack) {
        int needed = 2 * ENERGY_PER_USE;
        return ElectricItems.discharge(stack, needed, Integer.MAX_VALUE, true, true) == needed;
    }

    @Override
    protected Component message(NuclearReactorBlockEntity reactor) {
        int maxHeat = reactor.getMaxHeat();
        return Component.translatable("message.craft.nuclearcontrol.thermo_digital", reactor.getHeat(),
                maxHeat * 50 / 100, maxHeat * 85 / 100);
    }

    @Override
    protected void damage(ItemStack stack, Player player, UseOnContext context) {
        ElectricItems.use(stack, ENERGY_PER_USE, player);
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
