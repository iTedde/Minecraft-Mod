package dev.craft.powerconverters.client;

import dev.craft.Craft;
import dev.craft.powerconverters.PcMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class PowerConvertersClient {
    private PowerConvertersClient() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(PcMenus.ENERGY_BRIDGE.get(), EnergyBridgeScreen::new);
    }
}
