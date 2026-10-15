package dev.craft.ic2.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.craft.Craft;
import dev.craft.ic2.item.tool.MiningLaserEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** IC2 {@code RenderCrossed}: the arrow geometry with the laser texture. */
public class MiningLaserRenderer extends EntityRenderer<MiningLaserEntity> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/entity/mining_laser.png");

    public MiningLaserRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(MiningLaserEntity entity, float entityYaw, float partialTicks, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTicks, entity.yRotO, entity.getYRot()) - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(Mth.lerp(partialTicks, entity.xRotO, entity.getXRot())));
        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        poseStack.scale(0.05625F, 0.05625F, 0.05625F);
        poseStack.translate(-4.0F, 0.0F, 0.0F);
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutout(TEXTURE));
        PoseStack.Pose pose = poseStack.last();
        // tail
        vertex(pose, consumer, -7, -2, -2, 0.0F, 0.15625F, -1, 0, 0, packedLight);
        vertex(pose, consumer, -7, -2, 2, 0.15625F, 0.15625F, -1, 0, 0, packedLight);
        vertex(pose, consumer, -7, 2, 2, 0.15625F, 0.3125F, -1, 0, 0, packedLight);
        vertex(pose, consumer, -7, 2, -2, 0.0F, 0.3125F, -1, 0, 0, packedLight);
        vertex(pose, consumer, -7, 2, -2, 0.0F, 0.15625F, 1, 0, 0, packedLight);
        vertex(pose, consumer, -7, 2, 2, 0.15625F, 0.15625F, 1, 0, 0, packedLight);
        vertex(pose, consumer, -7, -2, 2, 0.15625F, 0.3125F, 1, 0, 0, packedLight);
        vertex(pose, consumer, -7, -2, -2, 0.0F, 0.3125F, 1, 0, 0, packedLight);
        // four crossed shaft planes
        for (int i = 0; i < 4; i++) {
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            vertex(pose, consumer, -8, -2, 0, 0.0F, 0.0F, 0, 1, 0, packedLight);
            vertex(pose, consumer, 8, -2, 0, 0.5F, 0.0F, 0, 1, 0, packedLight);
            vertex(pose, consumer, 8, 2, 0, 0.5F, 0.15625F, 0, 1, 0, packedLight);
            vertex(pose, consumer, -8, 2, 0, 0.0F, 0.15625F, 0, 1, 0, packedLight);
        }
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer consumer, int x, int y, int z, float u, float v,
                               int normalX, int normalY, int normalZ, int packedLight) {
        consumer.addVertex(pose, x, y, z)
                .setColor(-1)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(packedLight)
                .setNormal(pose, normalX, normalZ, normalY);
    }

    @Override
    public ResourceLocation getTextureLocation(MiningLaserEntity entity) {
        return TEXTURE;
    }
}
