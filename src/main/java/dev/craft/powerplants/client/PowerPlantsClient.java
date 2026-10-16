package dev.craft.powerplants.client;

import dev.craft.Craft;
import dev.craft.powerplants.PpFluids;
import dev.craft.powerplants.PpMenus;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class PowerPlantsClient {
    private PowerPlantsClient() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(PpMenus.COKE_OVEN.get(), CokeOvenScreen::new);
        event.register(PpMenus.BOILER.get(), BoilerScreen::new);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        ResourceLocation steam = ResourceLocation.fromNamespaceAndPath(Craft.MODID, "block/powerplants/steam");
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return steam;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return steam;
            }
        }, PpFluids.STEAM_TYPE.get());
    }
}
