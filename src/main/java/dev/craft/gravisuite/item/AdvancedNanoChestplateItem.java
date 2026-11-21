package dev.craft.gravisuite.item;

import dev.craft.ic2.item.ElectricItems;
import net.minecraft.core.Holder;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;

/**
 * GraviSuite {@code ItemAdvancedNanoChestPlate} (spec 28 §2, §4): the advanced jetpack that also protects like a
 * NanoSuit chestplate (0.4 · 0.9, 800 EU per damage, armor display 7 while charge ≥ 800).
 */
public class AdvancedNanoChestplateItem extends AdvancedJetpackItem {
    public AdvancedNanoChestplateItem(Holder<ArmorMaterial> material, Properties properties) {
        super(material, properties);
    }

    /** Original error: {@code charge / 800} without IC2's ×25, so it absorbs at most 1/25 of what it could. */
    @Override
    protected int getDamageLimit(ItemStack stack) {
        return ElectricItems.discharge(stack, Integer.MAX_VALUE, Integer.MAX_VALUE, true, true) / getEnergyPerDamage();
    }

    @Override
    public double getDamageAbsorptionRatio() {
        return 0.9;
    }

    @Override
    public int getEnergyPerDamage() {
        return 800;
    }
}
