package dev.craft.nuclearcontrol.client;

import dev.craft.Craft;
import dev.craft.nuclearcontrol.NcBlockEntityTypes;
import dev.craft.nuclearcontrol.NcMenus;
import dev.craft.nuclearcontrol.block.AlarmBlockEntity;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

@EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT)
public final class NuclearControlClient {
    private NuclearControlClient() {
    }

    /** GUI textures cut from the pack zip ({@code img/GUI*.png}). */
    static ResourceLocation gui(String name) {
        return ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/gui/nuclearcontrol/" + name + ".png");
    }

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        AlarmBlockEntity.AlarmSoundHooks.setHandler(new AlarmSoundHandler());
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(NcMenus.THERMAL_MONITOR.get(), ThermalMonitorScreen::new);
        event.register(NcMenus.REMOTE_THERMAL_MONITOR.get(), RemoteThermalMonitorScreen::new);
        event.register(NcMenus.ALARM.get(), AlarmScreen::new);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(NcBlockEntityTypes.THERMAL_MONITOR.get(), ThermalMonitorRenderer::new);
        event.registerBlockEntityRenderer(NcBlockEntityTypes.REMOTE_THERMAL_MONITOR.get(),
                RemoteThermalMonitorRenderer::new);
    }
}
