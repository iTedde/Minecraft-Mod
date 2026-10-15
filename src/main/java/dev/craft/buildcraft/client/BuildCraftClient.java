package dev.craft.buildcraft.client;

import dev.craft.Craft;
import dev.craft.buildcraft.BcBlockEntityTypes;
import dev.craft.buildcraft.BcItems;
import dev.craft.buildcraft.BcMenus;
import dev.craft.buildcraft.engine.EngineType;
import dev.craft.buildcraft.fluid.BcFluids;
import com.mojang.blaze3d.shaders.FogShape;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import org.joml.Vector3f;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
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
        event.registerBlockEntityRenderer(BcBlockEntityTypes.MARKER.get(), MarkerRenderer::new);
        event.registerBlockEntityRenderer(BcBlockEntityTypes.QUARRY.get(), QuarryRenderer::new);
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
        event.registerFluidType(new LiquidExtensions("oil_still", "oil_flow") {
            // Oil is Material.water in 1.4.7: under it the water fog applies (colour 0.02, 0.02, 0.2).
            @Override
            public Vector3f modifyFogColor(Camera camera, float partialTick, ClientLevel level, int renderDistance,
                                           float darkenWorldAmount, Vector3f fluidFogColor) {
                return new Vector3f(0.02F, 0.02F, 0.2F);
            }

            @Override
            public void modifyFogRender(Camera camera, FogRenderer.FogMode mode, float renderDistance,
                                        float partialTick, float nearDistance, float farDistance, FogShape shape) {
                RenderSystem.setShaderFogStart(-8.0F);
                RenderSystem.setShaderFogEnd(24.0F);
            }
        }, BcFluids.OIL_TYPE.get());
        event.registerFluidType(new LiquidExtensions("fuel_still", "fuel_still"), BcFluids.FUEL_TYPE.get());
    }

    /** Textures rendered from the BuildCraft liquid FX (see {@code tools/scripts/BcLiquidTextures.java}). */
    private static class LiquidExtensions implements IClientFluidTypeExtensions {
        private final ResourceLocation still;
        private final ResourceLocation flowing;

        LiquidExtensions(String still, String flowing) {
            this.still = ResourceLocation.fromNamespaceAndPath(Craft.MODID, "block/" + still);
            this.flowing = ResourceLocation.fromNamespaceAndPath(Craft.MODID, "block/" + flowing);
        }

        @Override
        public ResourceLocation getStillTexture() {
            return still;
        }

        @Override
        public ResourceLocation getFlowingTexture() {
            return flowing;
        }
    }
}
