package dev.craft.chargepads.item;

import net.minecraft.world.item.Item;

/**
 * Efficiency upgrade (spec 47 §7, original item {@code r}): cancels the overclocker's extra idle drain and lowers the
 * charge cost to {@code 0.995^n}.
 */
public class EfficiencyUpgradeItem extends Item {
    public EfficiencyUpgradeItem(Properties properties) {
        super(properties);
    }
}
