package dev.craft.thermalexpansion.client;

import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.thermalexpansion.TeBlockEntityTypes;
import dev.craft.thermalexpansion.TeFluids;
import dev.craft.thermalexpansion.TeItems;
import dev.craft.thermalexpansion.TeMenus;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

/** Client side of Thermal Expansion (spec 26): screens, machine model, renderers, liquid textures. */
@EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT)
public final class ThermalExpansionClient {
    private ThermalExpansionClient() {
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(TeMenus.MACHINE.get(), TeMachineScreen::new);
        event.register(TeMenus.ENGINE.get(), TeEngineScreen::new);
        event.register(TeMenus.ENERGY_CELL.get(), EnergyCellScreen::new);
    }

    @SubscribeEvent
    public static void registerGeometryLoaders(ModelEvent.RegisterGeometryLoaders event) {
        event.register(TeMachineModel.LOADER_ID, TeMachineModel.LOADER);
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(PortableTankRenderer.LAYER, PortableTankRenderer::createLayer);
        event.registerLayerDefinition(TeEngineRenderer.LAYER, TeEngineRenderer::createLayer);
        event.registerLayerDefinition(EnergyCellRenderer.LAYER, EnergyCellRenderer::createLayer);
        event.registerLayerDefinition(ConduitRenderer.LAYER, ConduitRenderer::createLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(TeBlockEntityTypes.PORTABLE_TANK.get(), PortableTankRenderer::new);
        event.registerBlockEntityRenderer(TeBlockEntityTypes.STEAM_ENGINE.get(), TeEngineRenderer::new);
        event.registerBlockEntityRenderer(TeBlockEntityTypes.MAGMATIC_ENGINE.get(), TeEngineRenderer::new);
        event.registerBlockEntityRenderer(TeBlockEntityTypes.ENERGY_CELL.get(), EnergyCellRenderer::new);
        event.registerBlockEntityRenderer(TeBlockEntityTypes.ENERGY_CONDUIT.get(), ConduitRenderer::new);
        event.registerBlockEntityRenderer(TeBlockEntityTypes.LIQUIDUCT.get(), ConduitRenderer::new);
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(itemRenderer(PortableTankItemRenderer::new), TeItems.PORTABLE_TANK.get());
        event.registerItem(itemRenderer(TeEngineItemRenderer::new), TeItems.STEAM_ENGINE.get(),
                TeItems.MAGMATIC_ENGINE.get());
        event.registerItem(itemRenderer(EnergyCellItemRenderer::new), TeItems.ENERGY_CELL.get());
        event.registerItem(itemRenderer(ConduitItemRenderer::new), TeItems.ENERGY_CONDUIT.get(),
                TeItems.LIQUIDUCT.get());
        event.registerFluidType(liquid("molten_redstone"), TeFluids.MOLTEN_REDSTONE_TYPE.get());
        event.registerFluidType(liquid("molten_ender"), TeFluids.MOLTEN_ENDER_TYPE.get());
    }

    /** An item drawn by a block-entity-style renderer, created on first use. */
    private static IClientItemExtensions itemRenderer(Supplier<BlockEntityWithoutLevelRenderer> factory) {
        return new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = factory.get();
                }
                return renderer;
            }
        };
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
