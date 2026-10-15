package dev.craft.gregtech.client;

import dev.craft.Craft;
import dev.craft.gregtech.GtMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class GregTechClient {
    private GregTechClient() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(GtMenus.BASIC_MACHINE.get(), GtBasicMachineScreen::new);
    }
}
