package dev.craft.ic2.item.armor;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.damagesource.DamageContainer;
import net.neoforged.neoforge.event.entity.living.ArmorHurtEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

/**
 * Forge hooks of the IC2 armor. A player wearing at least one {@link SpecialArmor} piece takes damage through
 * Forge's {@link ArmorProperties#applyArmor} instead of the vanilla armor formula, as in 1.4.7.
 */
public final class ArmorEvents {
    private ArmorEvents() {
    }

    public static void register(IEventBus bus) {
        bus.addListener(ArmorEvents::onIncomingDamage);
        bus.addListener(ArmorEvents::onArmorHurt);
        bus.addListener(ArmorEvents::onFall);
    }

    private static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Player player && ArmorProperties.wearsSpecialArmor(player)) {
            event.getContainer().addModifier(DamageContainer.Reduction.ARMOR, (container, reduction) -> {
                float damage = container.getNewDamage();
                return damage - ArmorProperties.applyArmor(player, container.getSource(), damage);
            });
        }
    }

    /** Armor damage is applied by {@link ArmorProperties#applyArmor}. */
    private static void onArmorHurt(ArmorHurtEvent event) {
        if (event.getEntity() instanceof Player player && ArmorProperties.wearsSpecialArmor(player)) {
            event.setCanceled(true);
        }
    }

    private static void onFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) {
            return;
        }
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        if (boots.getItem() instanceof Ic2ArmorItem armor && armor.onFall(player, boots,
                (int) event.getDistance() - 3)) {
            event.setCanceled(true);
        }
    }
}
