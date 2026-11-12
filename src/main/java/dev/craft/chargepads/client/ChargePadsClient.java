package dev.craft.chargepads.client;

import dev.craft.Craft;
import dev.craft.chargepads.CpMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT)
public final class ChargePadsClient {
    private ChargePadsClient() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(CpMenus.CHARGE_PAD.get(), ChargePadScreen::new);
    }
}
