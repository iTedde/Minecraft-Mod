package dev.craft.ic2.item.reactor;

import dev.craft.ic2.Ic2DataComponents;
import net.minecraft.world.item.ItemStack;

/**
 * IC2 {@code ItemGradualInt}: the wear lives in its own value ({@code advDmg}) with an arbitrary maximum; the
 * durability bar shows it scaled to the 10 000 damage steps.
 */
public class GradualIntItem extends GradualItem {
    protected final int maxDmg;

    public GradualIntItem(Properties properties, int maxDmg) {
        super(properties);
        this.maxDmg = maxDmg;
    }

    @Override
    public void setDamageForStack(ItemStack stack, int advDmg) {
        stack.set(Ic2DataComponents.ADV_DAMAGE.get(), advDmg);
        if (maxDmg > 0) {
            double p = (double) advDmg / maxDmg;
            int newDmg = (int) (stack.getMaxDamage() * p);
            if (newDmg >= stack.getMaxDamage()) {
                newDmg = stack.getMaxDamage() - 1;
            }
            stack.setDamageValue(newDmg);
        }
    }

    @Override
    public int getDamageOfStack(ItemStack stack) {
        return stack.getOrDefault(Ic2DataComponents.ADV_DAMAGE.get(), 0);
    }

    @Override
    public int getMaxDamageEx() {
        return maxDmg;
    }
}
