package dev.craft.gregtech.client;

import dev.craft.Craft;
import dev.craft.gregtech.GtItems;
import dev.craft.gregtech.GtMenus;
import dev.craft.ic2.client.Ic2Client;
import net.minecraft.client.renderer.item.ItemProperties;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class GregTechClient {
    /** Default water colour of the plains biome for the grey water texture. */
    private static final int WATER_COLOR = 0xFF3F76E4;

    private GregTechClient() {
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        // Two icons: empty (item 56) and charged (item 57).
        event.enqueueWork(() -> ItemProperties.register(GtItems.LITHIUM_BATTERY.get(), Ic2Client.CHARGE_PROPERTY,
                (stack, level, entity, seed) -> GtItems.LITHIUM_BATTERY.get().isEmptyState(stack) ? 0.0F : 1.0F));
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(GtMenus.BASIC_MACHINE.get(), GtBasicMachineScreen::new);
        event.register(GtMenus.ELECTROLYZER.get(), GtTankMachineScreen::electrolyzer);
        event.register(GtMenus.CENTRIFUGE.get(), GtTankMachineScreen::centrifuge);
        event.register(GtMenus.BLAST_FURNACE.get(), GtBlastFurnaceScreen::new);
        event.register(GtMenus.IMPLOSION_COMPRESSOR.get(), GtImplosionCompressorScreen::new);
        event.register(GtMenus.GRINDER.get(), GtGrinderScreen::new);
    }

    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        event.register((stack, tintIndex) -> tintIndex == 0 ? WATER_COLOR : -1, GtItems.WATER.get());
    }
}
