package dev.craft.forestry.client;

import dev.craft.Craft;
import dev.craft.forestry.FrBlockEntityTypes;
import dev.craft.forestry.FrFluids;
import dev.craft.forestry.FrItems;
import dev.craft.forestry.FrMenus;
import dev.craft.forestry.circuit.CircuitBoardType;
import dev.craft.forestry.circuit.ElectronTube;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class ForestryClient {
    private ForestryClient() {
    }

    @SubscribeEvent
    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ForestryMachineRenderer.CARPENTER_LAYER,
                ForestryMachineRenderer::createCarpenterLayer);
        event.registerLayerDefinition(ForestryMachineRenderer.FABRICATOR_LAYER,
                ForestryMachineRenderer::createFabricatorLayer);
        event.registerLayerDefinition(ElectricalEngineRenderer.LAYER, ElectricalEngineRenderer::createLayer);
    }

    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(FrBlockEntityTypes.CARPENTER.get(),
                context -> new ForestryMachineRenderer<>(context, ForestryMachineRenderer.Kind.CARPENTER));
        event.registerBlockEntityRenderer(FrBlockEntityTypes.FABRICATOR.get(),
                context -> new ForestryMachineRenderer<>(context, ForestryMachineRenderer.Kind.FABRICATOR));
        event.registerBlockEntityRenderer(FrBlockEntityTypes.ELECTRICAL_ENGINE.get(), ElectricalEngineRenderer::new);
    }

    @SubscribeEvent
    public static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(FrMenus.FABRICATOR.get(), FabricatorScreen::new);
        event.register(FrMenus.CARPENTER.get(), CarpenterScreen::new);
        event.register(FrMenus.ELECTRICAL_ENGINE.get(), ElectricalEngineScreen::new);
        event.register(FrMenus.SOLDERING_IRON.get(), SolderingIronScreen::new);
    }

    /** {@code getColorFromItemStack}: render pass 0 and 1 of the tubes ({@code ItemOverlay}) and the boards. */
    @SubscribeEvent
    public static void registerItemColors(RegisterColorHandlersEvent.Item event) {
        for (ElectronTube tube : ElectronTube.values()) {
            event.register((stack, tintIndex) -> 0xFF000000
                    | (tintIndex == 0 ? ElectronTube.PRIMARY_COLOR : tube.getSecondaryColor()), FrItems.tube(tube));
        }
        for (CircuitBoardType type : CircuitBoardType.values()) {
            event.register((stack, tintIndex) -> 0xFF000000
                    | (tintIndex == 0 ? type.getPrimaryColor() : type.getSecondaryColor()), FrItems.circuitBoard(type));
        }
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new ForestryMachineItemRenderer();
                }
                return renderer;
            }
        }, FrItems.CARPENTER.get(), FrItems.THERMIONIC_FABRICATOR.get(), FrItems.ELECTRICAL_ENGINE.get());
        ResourceLocation glass = ResourceLocation.fromNamespaceAndPath(Craft.MODID, "block/liquid_glass_still");
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return glass;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return glass;
            }
        }, FrFluids.LIQUID_GLASS_TYPE.get());
    }
}
