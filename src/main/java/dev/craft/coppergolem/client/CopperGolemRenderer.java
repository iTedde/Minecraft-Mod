package dev.craft.coppergolem.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.craft.Craft;
import dev.craft.coppergolem.CopperGolem;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;

/** One texture per oxidation stage (spec 35 §2.5); the carried item is drawn between the hands. */
public class CopperGolemRenderer extends MobRenderer<CopperGolem, CopperGolemModel> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "copper_golem"), "main");

    private static final Map<WeatherState, ResourceLocation> TEXTURES = new EnumMap<>(WeatherState.class);

    static {
        for (WeatherState stage : WeatherState.values()) {
            TEXTURES.put(stage, ResourceLocation.fromNamespaceAndPath(Craft.MODID,
                    "textures/entity/copper_golem/" + stage.getSerializedName() + ".png"));
        }
    }

    public CopperGolemRenderer(EntityRendererProvider.Context context) {
        super(context, new CopperGolemModel(context.bakeLayer(LAYER)), 0.25F);
        addLayer(new CarriedItemLayer(this, context.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(CopperGolem golem) {
        return TEXTURES.get(golem.getWeatherState());
    }

    private static final class CarriedItemLayer extends RenderLayer<CopperGolem, CopperGolemModel> {
        private final ItemInHandRenderer itemRenderer;

        private CarriedItemLayer(RenderLayerParent<CopperGolem, CopperGolemModel> parent,
                ItemInHandRenderer itemRenderer) {
            super(parent);
            this.itemRenderer = itemRenderer;
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int light, CopperGolem golem,
                float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw,
                float headPitch) {
            ItemStack stack = golem.getMainHandItem();
            if (stack.isEmpty()) {
                return;
            }
            poseStack.pushPose();
            getParentModel().translateToHands(poseStack);
            poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
            poseStack.scale(0.6F, 0.6F, 0.6F);
            itemRenderer.renderItem(golem, stack, ItemDisplayContext.GROUND, false, poseStack, buffer, light);
            poseStack.popPose();
        }
    }
}
