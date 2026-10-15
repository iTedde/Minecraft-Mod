package dev.craft.buildcraft.client;

import dev.craft.Craft;
import dev.craft.buildcraft.BcBlockEntityTypes;
import dev.craft.buildcraft.BcItems;
import dev.craft.buildcraft.BcMenus;
import dev.craft.buildcraft.engine.EngineType;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class BuildCraftClient {
    private BuildCraftClient() {
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(EngineModel.LAYER, EngineModel::createLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        for (EngineType type : EngineType.values()) {
            event.registerBlockEntityRenderer(BcBlockEntityTypes.engine(type), EngineRenderer::new);
        }
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        for (EngineType type : EngineType.values()) {
            if (type.hasMenu()) {
                event.register(BcMenus.get(type), EngineScreen::new);
            }
        }
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        IClientItemExtensions engineItem = new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new EngineItemRenderer();
                }
                return renderer;
            }
        };
        for (EngineType type : EngineType.values()) {
            event.registerItem(engineItem, BcItems.engine(type).get());
        }
    }
}
