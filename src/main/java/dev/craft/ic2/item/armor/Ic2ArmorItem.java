package dev.craft.ic2.item.armor;

import net.minecraft.core.Holder;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Base of all IC2 armor: Forge {@link SpecialArmor} with IC2 {@code ItemArmorUtility} defaults (absorbs nothing, not
 * enchantable, not repairable) and Forge's {@code onArmorTickUpdate} while worn.
 */
public abstract class Ic2ArmorItem extends ArmorItem implements SpecialArmor {
    protected Ic2ArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties);
    }

    /** Forge armor slot of this piece: 0 = boots … 3 = helmet. */
    public int getForgeSlot() {
        return 3 - type.ordinal();
    }

    @Override
    public ArmorProperties getProperties(LivingEntity entity, ItemStack stack, DamageSource source, double damage,
                                         int slot) {
        return ArmorProperties.NONE;
    }

    @Override
    public int getArmorDisplay(ItemStack stack) {
        return 0;
    }

    @Override
    public void damageArmor(LivingEntity entity, ItemStack stack, DamageSource source, int damage, int slot) {
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return false;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (entity instanceof Player player && player.getItemBySlot(type.getSlot()) == stack) {
            onArmorTick(level, player, stack);
        }
    }

    /**
     * Forge {@code LivingFallEvent} for worn boots, on the server.
     *
     * @param fallDamage IC2 fall damage {@code (int) distance - 3}
     * @return whether the fall damage is cancelled
     */
    public boolean onFall(Player player, ItemStack stack, int fallDamage) {
        return false;
    }

    /** Called each tick on both sides while {@code stack} is worn. */
    protected void onArmorTick(Level level, Player player, ItemStack stack) {
    }
}
