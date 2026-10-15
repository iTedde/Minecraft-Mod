package dev.craft.ic2.item.armor;

import dev.craft.ic2.item.ElectricItems;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/** IC2 {@code ItemArmorNanoSuit}: 100 000 EU, tier 2, 90 % absorption, 800 EU per damage. */
public class NanoArmorItem extends ElectricArmorItem {
    public NanoArmorItem(Type type, Properties properties) {
        super(Ic2ArmorMaterials.NANO, type, properties, 100000, 160, 2);
    }

    @Override
    public ArmorProperties getProperties(LivingEntity entity, ItemStack stack, DamageSource source, double damage,
                                         int slot) {
        if (source.is(DamageTypes.FALL) && type == Type.BOOTS) {
            return new ArmorProperties(10, damage < 8.0 ? 1.0 : 0.875, getDamageLimit(stack));
        }
        return super.getProperties(entity, stack, source, damage, slot);
    }

    @Override
    public boolean onFall(Player player, ItemStack stack, int fallDamage) {
        if (type != Type.BOOTS || fallDamage >= 8) {
            return false;
        }
        int energyCost = getEnergyPerDamage() * fallDamage;
        if (energyCost <= ElectricItems.discharge(stack, Integer.MAX_VALUE, Integer.MAX_VALUE, true, true)) {
            ElectricItems.discharge(stack, energyCost, Integer.MAX_VALUE, true, false);
            return true;
        }
        return false;
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
