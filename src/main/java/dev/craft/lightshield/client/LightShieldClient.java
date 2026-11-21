package dev.craft.lightshield.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.craft.Craft;
import dev.craft.lightshield.ElectricLightShieldItem;
import dev.craft.lightshield.LsItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ShieldModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = Craft.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public final class LightShieldClient {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/entity/electric_light_shield.png");

    private LightShieldClient() {
    }

    /** Vanilla registers "blocking" only for the shield itself. */
    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> ItemProperties.register(LsItems.ELECTRIC_LIGHT_SHIELD.get(),
                ResourceLocation.withDefaultNamespace("blocking"), (stack, level, entity, seed) ->
                        entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1.0F : 0.0F));
    }

    @SubscribeEvent
    public static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            private BlockEntityWithoutLevelRenderer renderer;

            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                if (renderer == null) {
                    renderer = new ShieldRenderer();
                }
                return renderer;
            }
        }, LsItems.ELECTRIC_LIGHT_SHIELD.get());
    }

    /** Vanilla shield model with its own texture; the plate glows at full brightness while the light is on. */
    private static final class ShieldRenderer extends BlockEntityWithoutLevelRenderer {
        private ShieldModel model;

        ShieldRenderer() {
            super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        }

        @Override
        public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                MultiBufferSource buffer, int packedLight, int packedOverlay) {
            if (model == null) {
                model = new ShieldModel(Minecraft.getInstance().getEntityModels().bakeLayer(ModelLayers.SHIELD));
            }
            poseStack.pushPose();
            poseStack.scale(1.0F, -1.0F, -1.0F);
            VertexConsumer consumer = ItemRenderer.getFoilBufferDirect(buffer, model.renderType(TEXTURE), true,
                    stack.hasFoil());
            model.handle().render(poseStack, consumer, packedLight, packedOverlay);
            model.plate().render(poseStack, consumer,
                    ElectricLightShieldItem.isLit(stack) ? LightTexture.FULL_BRIGHT : packedLight, packedOverlay);
            poseStack.popPose();
        }
    }
}
