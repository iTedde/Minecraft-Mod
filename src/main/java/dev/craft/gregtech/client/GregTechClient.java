package dev.craft.gregtech.client;

import dev.craft.Craft;
import dev.craft.gregtech.GtItems;
import dev.craft.gregtech.GtMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class GregTechClient {
    /** Default water colour of the plains biome for the grey water texture. */
    private static final int WATER_COLOR = 0xFF3F76E4;

    private GregTechClient() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(GtMenus.BASIC_MACHINE.get(), GtBasicMachineScreen::new);
        event.register(GtMenus.ELECTROLYZER.get(), GtElectrolyzerScreen::new);
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> tintIndex == 0 ? WATER_COLOR : -1, GtItems.WATER.get());
    }
}
