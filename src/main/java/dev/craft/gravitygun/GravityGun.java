package dev.craft.gravitygun;

import java.util.function.Supplier;

import dev.craft.Craft;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityStruckByLightningEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/** GraviGun 1.4.7v2 (spec 50 §3): gravity gun, charged gravity gun and the grabbed-block entity. */
public final class GravityGun {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Craft.MODID);
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, Craft.MODID);

    public static final DeferredItem<GravityGunItem> GRAVITY_GUN =
            ITEMS.register("gravity_gun", () -> new GravityGunItem(new Item.Properties(), false));
    public static final DeferredItem<GravityGunItem> CHARGED_GRAVITY_GUN =
            ITEMS.register("charged_gravity_gun", () -> new GravityGunItem(new Item.Properties(), true));

    public static final Supplier<EntityType<GrabbedBlock>> GRABBED_BLOCK = ENTITY_TYPES.register("grabbed_block",
            () -> EntityType.Builder.<GrabbedBlock>of(GrabbedBlock::new, MobCategory.MISC).sized(0.98F, 0.98F)
                    .clientTrackingRange(10).updateInterval(1).build("grabbed_block"));

    private GravityGun() {
    }

    public static void init(IEventBus modBus) {
        ITEMS.register(modBus);
        ENTITY_TYPES.register(modBus);
        modBus.addListener(GravityGunPayload::register);
        NeoForge.EVENT_BUS.addListener((LevelTickEvent.Post event) -> {
            if (event.getLevel() instanceof ServerLevel level) {
                GravityGunHandler.tick(level);
            }
        });
        NeoForge.EVENT_BUS.addListener((EntityTickEvent.Post event) -> {
            if (event.getEntity() instanceof Zombie zombie && !zombie.level().isClientSide()) {
                GravityGunHandler.zombieTick(zombie);
            }
        });
        NeoForge.EVENT_BUS.addListener((EntityStruckByLightningEvent event) -> {
            if (event.getEntity() instanceof Player player && !player.level().isClientSide()) {
                GravityGunHandler.charge(player);
            }
        });
        // EventHandler.onInteract: no entity interaction while holding the gun.
        NeoForge.EVENT_BUS.addListener((PlayerInteractEvent.EntityInteract event) -> {
            if (!GravityGunHandler.gun(event.getEntity()).isEmpty()) {
                event.setCanceled(true);
            }
        });
        NeoForge.EVENT_BUS.addListener((PlayerEvent.PlayerLoggedOutEvent event) ->
                GravityGunHandler.forget(event.getEntity()));
        NeoForge.EVENT_BUS.addListener((ServerStoppingEvent event) -> GravityGunHandler.clear());
    }
}
