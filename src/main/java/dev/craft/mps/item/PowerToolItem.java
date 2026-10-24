package dev.craft.mps.item;

import dev.craft.mps.module.ModuleTarget;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * MPS {@code ItemPowerTool} (spec 29 §1, §8): does nothing without modules; attack damage 1, not enchantable, not
 * repairable. Mining, weapons and modes follow in 28.4.
 */
public class PowerToolItem extends Item implements ModularItem {
    public PowerToolItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public ModuleTarget getTarget() {
        return ModuleTarget.TOOL;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }
}
