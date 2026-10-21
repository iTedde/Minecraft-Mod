package dev.craft.thermalexpansion.client;

import dev.craft.Craft;
import dev.craft.thermalexpansion.TeFluids;
import dev.craft.thermalexpansion.TeMenus;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/** Client side of Thermal Expansion (spec 26): machine screens and model, liquid textures. */
@EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT)
public final class ThermalExpansionClient {
    private ThermalExpansionClient() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(TeMenus.MACHINE.get(), TeMachineScreen::new);
    }

    @SubscribeEvent
    public static void registerGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(TeMachineModel.LOADER_ID, TeMachineModel.LOADER);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerFluidType(liquid("molten_redstone"), TeFluids.MOLTEN_REDSTONE_TYPE.get());
        event.registerFluidType(liquid("molten_ender"), TeFluids.MOLTEN_ENDER_TYPE.get());
    }

    /** The 1.4.7 liquid icon is a still 16×16 sprite; it serves as still and flowing texture. */
    private static IClientFluidTypeExtensions liquid(String name) {
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(Craft.MODID, "block/te/fluid/" + name);
        return new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return texture;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return texture;
            }
        };
    }
}
