package dev.craft.ic2.item.armor;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

/** Forge {@code ISpecialArmor}: armor that decides itself how much damage it absorbs. */
public interface SpecialArmor {
    /**
     * @param damage incoming damage (not scaled by 25)
     * @param slot   Forge armor slot, 0 = boots … 3 = helmet
     */
    ArmorProperties getProperties(LivingEntity entity, ItemStack stack, DamageSource source, double damage, int slot);

    /** Armor points shown in the HUD. */
    int getArmorDisplay(ItemStack stack);

    void damageArmor(LivingEntity entity, ItemStack stack, DamageSource source, int damage, int slot);
}
