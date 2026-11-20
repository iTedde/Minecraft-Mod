/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.entity.RealityRiftEntity;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import net.minecraft.util.Mth;
import org.joml.*;

import java.util.Random;
import java.lang.Math;

/**
 * Created by lukas on 03.03.14.
 */
public class RealityRiftEntityRenderer extends EntityRenderer<RealityRiftEntity> {
    public static final ResourceLocation CENTER_TEXTURE = Psychedelicraft.id("textures/entity/reality_rift/zero_center.png");
    private static final Random RANDOM = new Random(432L);

    public RealityRiftEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
    }

    @Override
    public ResourceLocation getTextureLocation(RealityRiftEntity entity) {
        return CENTER_TEXTURE;
    }

    @Override
    public void render(RealityRiftEntity entity, float yaw, float tickDelta, PoseStack matrices, MultiBufferSource vertices, int light) {

        matrices.pushPose();
        matrices.translate(0, entity.getBbHeight() * 0.5, 0);

        float size = entity.getRiftSize(tickDelta);
        float instability = entity.getInstability();

        float visualRiftSize = size < 0.01F ? (size * 10)
                : (0.1F + 0.1F * (size - 0.01F));

        matrices.scale(visualRiftSize, visualRiftSize, visualRiftSize);

        renderRift(matrices, vertices, tickDelta, entity.tickCount + tickDelta + (instability * instability * 3000));

        VertexConsumer consumer = vertices.getBuffer(RenderType.breezeEyes(CENTER_TEXTURE));
        Vector4f vector = new Vector4f(0, 0, 0, 1);

        matrices.pushPose();
        matrices.scale(5F, 5F, 5F);
        Matrix4f positionMatrix = matrices.last().pose();

        size = 1;

        light = 0;

        Quaternionf cameraRotation = Minecraft.getInstance().gameRenderer.getMainCamera().rotation();
        matrices.mulPose(cameraRotation);
        matrices.translate(-size * 0.5F, -size * 0.5F, 0);

        vector.set(0, 0, 0, 1);
        Vector4f pos = positionMatrix.transform(vector);
        consumer.addVertex(pos.x, pos.y, pos.z, CommonColors.WHITE, 0, 0, light, 0, 1, 1, 1);

        vector.set(size, 0, 0, 1);
        pos = positionMatrix.transform(vector);
        consumer.addVertex(pos.x, pos.y, pos.z, CommonColors.WHITE, 1, 0, light, 0, 1, 1, 1);

        vector.set(size, size, 0, 1);
        pos = positionMatrix.transform(vector);
        consumer.addVertex(pos.x, pos.y, pos.z, CommonColors.WHITE, 1, 1, light, 0, 1, 1, 1);

        vector.set(0, size, 0, 1);
        pos = positionMatrix.transform(vector);
        consumer.addVertex(pos.x, pos.y, pos.z, CommonColors.WHITE, 0, 1, light, 0, 1, 1, 1);

        matrices.popPose();
        matrices.popPose();
    }

    public void renderRift(PoseStack matrices, MultiBufferSource vertices, float partialTicks, float ticks) {
        ZeroScreen.render(ticks, (layer, u, v) -> {
            renderLightsScreen(matrices, vertices.getBuffer(layer), u, v, ticks, 1, 0xffffffff, 20);
        });
    }

    public static void renderLightsScreen(PoseStack matrices, VertexConsumer vertices, float u, float v, float ticks, float alpha, int color, int number) {
        RANDOM.setSeed(432L);
        matrices.pushPose();

        float width = 2.5F;
        float rotation = ticks / 200F;

        Matrix4f positionMatrix = matrices.last().pose();
        int light = 0x0;

        Vector4f vector = new Vector4f(0, 0, 0, 1);

        for (int i = 0; i < number; ++i) {
            float xLogFunc = (((float) i / number * 28493.0f + ticks) / 10F) % 20F;
            if (xLogFunc > 10) {
                xLogFunc = 20 - xLogFunc;
            }

            float lightAlpha = 1F / (1 + (float) Math.pow(2.71828f, -0.8F * xLogFunc) * ((1F / 0.01F) - 1));

            if (lightAlpha > 0.01F) {
                matrices.mulPose(new Quaternionf().rotateXYZ(
                        RANDOM.nextFloat() * Mth.TWO_PI,
                        RANDOM.nextFloat() * Mth.TWO_PI,
                        RANDOM.nextFloat() * Mth.TWO_PI
                ));
                matrices.mulPose(new Quaternionf().rotateXYZ(
                        RANDOM.nextFloat() * Mth.TWO_PI,
                        RANDOM.nextFloat() * Mth.TWO_PI,
                        RANDOM.nextFloat() * Mth.TWO_PI + rotation * Mth.HALF_PI * 0.5F
                ));

                float var8 = RANDOM.nextFloat() * 20 + 5;
                float var9 = RANDOM.nextFloat() * 2 + 1;

                vector.set(0, 0, 0, 1);
                Vector4f pos = positionMatrix.transform(vector);

                int opaque = MathUtils.withAlpha(CommonColors.WHITE, alpha * lightAlpha);
                int transparent = MathUtils.withAlpha(CommonColors.WHITE, 0);

                vertices.addVertex(pos.x, pos.y, pos.z, opaque, 0, 0, light, 0, 1, 1, 1);

                vector.set(-width * var9, var8, -0.5F * var9, 1);
                pos = positionMatrix.transform(vector);
                vertices.addVertex(pos.x, pos.y, pos.z, transparent, 1, 0, light, 0, 1, 1, 1);

                vector.set(width * var9, var8, -0.5F * var9, 1);
                pos = positionMatrix.transform(vector);
                vertices.addVertex(pos.x, pos.y, pos.z, transparent, 0, 1, light, 0, 1, 1, 1);

                vector.set(0, var8, var9, 1);
                pos = positionMatrix.transform(vector);
                vertices.addVertex(pos.x, pos.y, pos.z, transparent, 1, 1, light, 0, 1, 1, 1);

                vector.set(-width * var9, var8, -0.5F * var9, 1);
                pos = positionMatrix.transform(vector);
                vertices.addVertex(pos.x, pos.y, pos.z, transparent, 1, 1, light, 0, 1, 1, 1);
            }
        }

        matrices.popPose();

    }
}
