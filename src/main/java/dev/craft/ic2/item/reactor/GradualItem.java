package dev.craft.ic2.item.reactor;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * IC2 {@code ItemGradual}: stack size 1, 10 000 damage steps shown as durability bar, no repair by combining.
 * Wear is read and written through {@link #getDamageOfStack}/{@link #setDamageForStack} so that
 * {@link GradualIntItem} can keep a finer value.
 */
public class GradualItem extends Item {
    public static final int MAX_DAMAGE = 10000;

    public GradualItem(Properties properties) {
        this(properties, MAX_DAMAGE);
    }

    /** GregTech coolant cells use only 100 damage steps for the bar. */
    public GradualItem(Properties properties, int maxDamage) {
        super(properties.durability(maxDamage).setNoRepair());
    }

    public void setDamageForStack(ItemStack stack, int damage) {
        stack.setDamageValue(damage);
    }

    public int getDamageOfStack(ItemStack stack) {
        return stack.getDamageValue();
    }

    public int getMaxDamageEx() {
        return MAX_DAMAGE;
    }
}
