package dev.craft.compactsolars.client;

import dev.craft.Craft;
import dev.craft.compactsolars.CsMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class CompactSolarsClient {
    private CompactSolarsClient() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(CsMenus.SOLAR_ARRAY.get(), SolarArrayScreen::new);
    }
}
