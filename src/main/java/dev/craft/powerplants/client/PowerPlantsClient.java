package dev.craft.powerplants.client;

import dev.craft.Craft;
import dev.craft.powerplants.PpMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class PowerPlantsClient {
    private PowerPlantsClient() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(PpMenus.COKE_OVEN.get(), CokeOvenScreen::new);
    }
}
