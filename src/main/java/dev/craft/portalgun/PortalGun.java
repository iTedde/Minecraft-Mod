package dev.craft.portalgun;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;

/** Entry point of the Portal Gun content: long fall boots only (spec 21). */
public final class PortalGun {
    private PortalGun() {
    }

    public static void init(IEventBus modBus) {
        PgItems.MATERIALS.register(modBus);
        PgItems.ITEMS.register(modBus);
        modBus.addListener(PortalGun::addToCreativeTab);
        NeoForge.EVENT_BUS.addListener(PortalGun::onFall);
    }

    /** Like the original, both items sit in the combat tab. */
    private static void addToCreativeTab(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.COMBAT) {
            event.accept(PgItems.LONG_FALL_BOOT);
            event.accept(PgItems.LONG_FALL_BOOTS);
        }
    }

    /** {@code EventHandler.onLivingFall}: players and zombies wearing the boots fall from any height unharmed. */
    private static void onFall(LivingFallEvent event) {
        LivingEntity entity = event.getEntity();
        if (!entity.level().isClientSide() && (entity instanceof Player || entity instanceof Zombie)
                && entity.getItemBySlot(EquipmentSlot.FEET).is(PgItems.LONG_FALL_BOOTS)) {
            event.setDistance(0.0F);
        }
    }
}
