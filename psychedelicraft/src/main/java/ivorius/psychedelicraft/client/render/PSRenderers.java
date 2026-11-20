package ivorius.psychedelicraft.client.render;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;

import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.block.PSWoodTypes;
import ivorius.psychedelicraft.block.entity.*;
import ivorius.psychedelicraft.client.render.blocks.*;
import ivorius.psychedelicraft.entity.*;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.fluid.container.FluidVariant;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Client renderer registration (upstream: Fabric renderer registries).
 */
public interface PSRenderers {
    ResourceLocation WATER_STILL = ResourceLocation.withDefaultNamespace("block/water_still");
    ResourceLocation WATER_FLOWING = ResourceLocation.withDefaultNamespace("block/water_flow");
    ResourceLocation WATER_OVERLAY = ResourceLocation.withDefaultNamespace("block/water_overlay");

    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(PSEntities.MOLOTOV_COCKTAIL, context -> new ThrownItemRenderer<>(context, 1, true));
        event.registerEntityRenderer(PSEntities.REALITY_RIFT, RealityRiftEntityRenderer::new);

        event.registerBlockEntityRenderer(PSBlockEntities.DISTILLERY, FlaskBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(PSBlockEntities.FLASK, FlaskBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(PSBlockEntities.MASH_TUB, MashTubBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(PSBlockEntities.BARREL, BarrelBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(PSBlockEntities.DRYING_TABLE, DryingTableBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(PSBlockEntities.RIFT_JAR, RiftJarBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(PSBlockEntities.BOTTLE_RACK, BottleRackBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(PSBlockEntities.PEYOTE, PeyoteBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(PSBlockEntities.PLACED_DRINK, DrinksBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(PSBlockEntities.BUNSEN_BURNER, BurnerBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(PSBlockEntities.TRAY, TrayBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(PSBlockEntities.GLASS_TUBE, GlassTubeBlockEntityRenderer::new);
        event.registerBlockEntityRenderer(PSBlockEntities.CAULDRON, CauldronBlockEntityRenderer::new);
    }

    /**
     * Render layers and sign textures; run during client setup.
     */
    @SuppressWarnings("deprecation")
    static void setup() {
        for (Block block : new Block[] { PSBlocks.DISTILLERY, PSBlocks.FLASK, PSBlocks.GLASS_TUBE, PSBlocks.GLASS_VALVE }) {
            ItemBlockRenderTypes.setRenderLayer(block, RenderType.translucent());
        }
        for (Block block : new Block[] {
                PSBlocks.JUNIPER_SAPLING, PSBlocks.JUNIPER_LEAVES, PSBlocks.FRUITING_JUNIPER_LEAVES, PSBlocks.LATTICE, PSBlocks.WINE_GRAPE_LATTICE, PSBlocks.MORNING_GLORY_LATTICE,
                PSBlocks.CANNABIS, PSBlocks.HOP, PSBlocks.TOBACCO, PSBlocks.COCA, PSBlocks.COFFEA, PSBlocks.MORNING_GLORY,
                PSBlocks.AGAVE_PLANT,
                PSBlocks.JIMSONWEED, PSBlocks.BELLADONNA, PSBlocks.TOMATOES,
                PSBlocks.MASH_TUB, PSBlocks.JUNIPER_DOOR, PSBlocks.JUNIPER_TRAPDOOR,
                PSBlocks.POTTED_CANNABIS, PSBlocks.POTTED_JUNIPER_SAPLING, PSBlocks.POTTED_MORNING_GLORY,
                PSBlocks.POTTED_HOP, PSBlocks.POTTED_TOBACCO, PSBlocks.POTTED_COCA, PSBlocks.POTTED_COFFEA }) {
            ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutoutMipped());
        }
        SimpleFluid.REGISTRY.forEach(fluid -> {
            if (fluid.isEmpty() || !fluid.isCustomFluid()) {
                return;
            }
            ItemBlockRenderTypes.setRenderLayer(fluid.getPhysical().getStandingFluid(), RenderType.translucent());
            ItemBlockRenderTypes.setRenderLayer(fluid.getPhysical().getFlowingFluid(), RenderType.translucent());
        });
        Sheets.addWoodType(PSWoodTypes.JUNIPER);
    }

    static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(itemRenderer(RiftJarBlockEntityRenderer::renderStack), PSItems.RIFT_JAR);
        event.registerItem(itemRenderer(MashTubBlockEntityRenderer::renderStack), PSItems.MASH_TUB);

        SimpleFluid.REGISTRY.forEach(fluid -> {
            if (fluid.isEmpty() || !fluid.isCustomFluid() || fluid.getPhysical().getFluidType() == null) {
                return;
            }
            event.registerFluidType(new IClientFluidTypeExtensions() {
                private ItemFluids stackOf(FluidState state) {
                    return fluid.getStack(state, 1);
                }

                private static int argb(int color) {
                    return (color & 0xFF000000) == 0 ? color | 0xFF000000 : color;
                }

                @Override
                public ResourceLocation getStillTexture() {
                    return WATER_STILL;
                }

                @Override
                public ResourceLocation getFlowingTexture() {
                    return WATER_FLOWING;
                }

                @Override
                public ResourceLocation getOverlayTexture() {
                    return WATER_OVERLAY;
                }

                @Override
                public ResourceLocation getStillTexture(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
                    return fluid.getStandingTexture(stackOf(state)).orElse(WATER_STILL);
                }

                @Override
                public ResourceLocation getFlowingTexture(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
                    return fluid.getFlowTexture(stackOf(state)).orElse(WATER_FLOWING);
                }

                @Override
                public ResourceLocation getStillTexture(FluidStack stack) {
                    return fluid.getStandingTexture(ItemFluids.of(FluidVariant.of(stack), 1)).orElse(WATER_STILL);
                }

                @Override
                public ResourceLocation getFlowingTexture(FluidStack stack) {
                    return fluid.getFlowTexture(ItemFluids.of(FluidVariant.of(stack), 1)).orElse(WATER_FLOWING);
                }

                @Override
                public int getTintColor() {
                    return argb(fluid.getColor(fluid.getDefaultStack(1)));
                }

                @Override
                public int getTintColor(FluidState state, BlockAndTintGetter getter, BlockPos pos) {
                    return argb(fluid.getColor(stackOf(state)));
                }

                @Override
                public int getTintColor(FluidStack stack) {
                    return argb(fluid.getColor(ItemFluids.of(FluidVariant.of(stack), 1)));
                }
            }, fluid.getPhysical().getFluidType());
        });
    }

    interface StackRenderer {
        void render(ItemStack stack, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vertices, int light, int overlay);
    }

    private static IClientItemExtensions itemRenderer(StackRenderer renderer) {
        return new IClientItemExtensions() {
            @Nullable
            private BlockEntityWithoutLevelRenderer bewlr;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (bewlr == null) {
                    Minecraft client = Minecraft.getInstance();
                    bewlr = new BlockEntityWithoutLevelRenderer(client.getBlockEntityRenderDispatcher(), client.getEntityModels()) {
                        @Override
                        public void renderByItem(ItemStack stack, ItemDisplayContext mode, PoseStack matrices, MultiBufferSource vertices, int light, int overlay) {
                            renderer.render(stack, mode, matrices, vertices, light, overlay);
                        }
                    };
                }
                return bewlr;
            }
        };
    }
}
