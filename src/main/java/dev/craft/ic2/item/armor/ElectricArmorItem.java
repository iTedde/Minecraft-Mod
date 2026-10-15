package dev.craft.ic2.item.armor;

import dev.craft.ic2.item.ElectricItem;
import dev.craft.ic2.item.ElectricItems;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/** IC2 {@code ItemArmorElectric}: armor that absorbs damage for EU. */
public abstract class ElectricArmorItem extends Ic2ArmorItem implements ElectricItem {
    private final int maxCharge;
    private final int transferLimit;
    private final int tier;
    private ItemAttributeModifiers chargedModifiers;

    protected ElectricArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties, int maxCharge,
                                int transferLimit, int tier) {
        super(material, type, properties.stacksTo(1));
        this.maxCharge = maxCharge;
        this.transferLimit = transferLimit;
        this.tier = tier;
    }

    public abstract double getDamageAbsorptionRatio();

    public abstract int getEnergyPerDamage();

    private double getBaseAbsorptionRatio() {
        return switch (type) {
            case HELMET -> 0.15;
            case CHESTPLATE -> 0.4;
            case LEGGINGS -> 0.3;
            case BOOTS -> 0.15;
            default -> 0.0;
        };
    }

    /** {@code 25 · charge / EU per damage}, the most damage (in 1/25) the stored energy can absorb. */
    protected int getDamageLimit(ItemStack stack) {
        int energyPerDamage = getEnergyPerDamage();
        return energyPerDamage > 0
                ? 25 * ElectricItems.discharge(stack, Integer.MAX_VALUE, Integer.MAX_VALUE, true, true) / energyPerDamage
                : 0;
    }

    @Override
    public ArmorProperties getProperties(LivingEntity entity, ItemStack stack, DamageSource source, double damage,
                                         int slot) {
        if (source.is(DamageTypeTags.BYPASSES_ARMOR)) {
            return ArmorProperties.NONE;
        }
        return new ArmorProperties(0, getBaseAbsorptionRatio() * getDamageAbsorptionRatio(), getDamageLimit(stack));
    }

    @Override
    public int getArmorDisplay(ItemStack stack) {
        return ElectricItems.discharge(stack, Integer.MAX_VALUE, Integer.MAX_VALUE, true, true) >= getEnergyPerDamage()
                ? (int) Math.round(20.0 * getBaseAbsorptionRatio() * getDamageAbsorptionRatio())
                : 0;
    }

    @Override
    public void damageArmor(LivingEntity entity, ItemStack stack, DamageSource source, int damage, int slot) {
        ElectricItems.discharge(stack, damage * getEnergyPerDamage(), Integer.MAX_VALUE, true, false);
    }

    /** The armor bar shows {@link #getArmorDisplay}; the damage itself goes through {@link ArmorProperties}. */
    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        int display = getArmorDisplay(stack);
        if (display <= 0) {
            return ItemAttributeModifiers.EMPTY;
        }
        if (chargedModifiers == null) {
            chargedModifiers = ItemAttributeModifiers.builder()
                    .add(Attributes.ARMOR, new AttributeModifier(
                            ResourceLocation.withDefaultNamespace("armor." + type.getName()), display,
                            AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.bySlot(type.getSlot()))
                    .build();
        }
        return chargedModifiers;
    }

    @Override
    public int getMaxCharge() {
        return maxCharge;
    }

    @Override
    public int getTransferLimit() {
        return transferLimit;
    }

    @Override
    public int getTier() {
        return tier;
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
