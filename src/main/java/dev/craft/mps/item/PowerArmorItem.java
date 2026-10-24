package dev.craft.mps.item;

import dev.craft.ic2.item.armor.ArmorProperties;
import dev.craft.ic2.item.armor.Ic2ArmorItem;
import dev.craft.mps.ModularItems;
import dev.craft.mps.MpsEnergy;
import dev.craft.mps.module.ModuleTarget;
import dev.craft.mps.module.MpsModules;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;

/**
 * MPS {@code ItemPowerArmor} (spec 29 §5): protection from the plating and energy shield modules, damage costs energy
 * from the player's pool instead of durability.
 */
public class PowerArmorItem extends Ic2ArmorItem implements ModularItem {
    private final ModuleTarget target;

    public PowerArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties) {
        super(material, type, properties.stacksTo(1));
        this.target = switch (type) {
            case HELMET -> ModuleTarget.HEAD;
            case CHESTPLATE -> ModuleTarget.TORSO;
            case LEGGINGS -> ModuleTarget.LEGS;
            default -> ModuleTarget.FEET;
        };
    }

    @Override
    public ModuleTarget getTarget() {
        return target;
    }

    /** {@code getArmorDouble}: plating plus, while the pool holds more than a hit costs, the energy shield; max 6. */
    public static double getArmorValue(double playerEnergy, ItemStack stack) {
        double armor = ModularItems.computeProperty(stack, MpsModules.ARMOR_PHYSICAL);
        if (playerEnergy > ModularItems.computeProperty(stack, MpsModules.ENERGY_PER_DAMAGE)) {
            armor += ModularItems.computeProperty(stack, MpsModules.ARMOR_ENERGY);
        }
        return Math.min(6.0, armor);
    }

    /** No check for armor-bypassing damage: Forge asked every {@code ISpecialArmor} piece (spec 29 §5). */
    @Override
    public ArmorProperties getProperties(LivingEntity entity, ItemStack stack, DamageSource source, double damage,
                                         int slot) {
        double armor = entity instanceof Player player ? getArmorValue(MpsEnergy.getPlayerEnergy(player), stack) : 2.0;
        return new ArmorProperties(1, 0.04 * armor, (int) armor * 75);
    }

    /** Without the player only this piece's own energy counts (spec 29 §14). */
    @Override
    public int getArmorDisplay(ItemStack stack) {
        return (int) getArmorValue(MpsEnergy.getJoules(stack), stack);
    }

    @Override
    public void damageArmor(LivingEntity entity, ItemStack stack, DamageSource source, int damage, int slot) {
        double drain = ModularItems.computeProperty(stack, MpsModules.ENERGY_PER_DAMAGE) * damage;
        if (entity instanceof Player player) {
            MpsEnergy.drainPlayerEnergy(player, drain);
        } else {
            MpsEnergy.discharge(stack, drain);
        }
    }

    /** The armor bar shows {@link #getArmorDisplay}; the damage itself goes through {@link ArmorProperties}. */
    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        int display = getArmorDisplay(stack);
        if (display <= 0) {
            return ItemAttributeModifiers.EMPTY;
        }
        return ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR, new AttributeModifier(
                        ResourceLocation.withDefaultNamespace("armor." + type.getName()), display,
                        AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.bySlot(type.getSlot()))
                .build();
    }
}
