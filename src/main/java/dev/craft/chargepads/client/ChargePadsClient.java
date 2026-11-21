package dev.craft.chargepads.client;

import dev.craft.Craft;
import dev.craft.chargepads.CpMenus;
import dev.craft.chargepads.particle.CpParticles;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT)
public final class ChargePadsClient {
    private ChargePadsClient() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(CpMenus.CHARGE_PAD.get(), ChargePadScreen::new);
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(CpParticles.CHARGE_SPARK.get(), ChargeSparkParticle.Provider::new);
    }
}
