package dev.craft.redpower.client;

import dev.craft.Craft;
import dev.craft.redpower.RpMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class RedPowerClient {
    private RedPowerClient() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(RpMenus.PROJECT_TABLE.get(), ProjectTableScreen::new);
        event.register(RpMenus.LARGE_PROJECT_TABLE.get(), ProjectTableScreen::new);
    }
}
