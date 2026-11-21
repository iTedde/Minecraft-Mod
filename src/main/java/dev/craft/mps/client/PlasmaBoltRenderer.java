package dev.craft.mps.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.craft.mps.entity.PlasmaBoltEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import org.joml.Matrix4f;

/**
 * MPS {@code RenderPlasmaBolt} (spec 29 §11): a camera-facing glow of four blue circles (the inner ones pulsing every
 * 500 ms, the last one white in the centre) and three random white sparks, scaled with the bolt's size.
 */
public class PlasmaBoltRenderer extends EntityRenderer<PlasmaBoltEntity> {
    private static final int SEGMENTS = 24;
    private static final int BLUE_30 = 0x4D4D4DFF;
    private static final int BLUE_60 = 0x994D4DFF;
    private static final int BLUE = 0xFF4D4DFF;
    private static final int WHITE = 0xFFFFFFFF;

    private final RandomSource random = RandomSource.create();

    public PlasmaBoltRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(PlasmaBoltEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        double size = entity.getSize() / 10.0;
        poseStack.pushPose();
        poseStack.scale(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(entityRenderDispatcher.cameraOrientation());
        float scale = (float) (size / 16.0);
        poseStack.scale(scale, scale, scale);
        double timeScale = Math.cos(System.currentTimeMillis() % 500 * 2.0 / 500 - 1.0);
        VertexConsumer consumer = buffer.getBuffer(RenderType.lightning());
        Matrix4f pose = poseStack.last().pose();
        circle(consumer, pose, 4.0F, 0.0F, BLUE_30, BLUE_30);
        circle(consumer, pose, (float) (3.0 + timeScale / 2.0), 0.001F, BLUE_60, BLUE_60);
        circle(consumer, pose, (float) (2.0 + timeScale), 0.002F, BLUE, BLUE);
        circle(consumer, pose, (float) (1.0 + timeScale), 0.003F, WHITE, BLUE);
        for (int i = 0; i < 3; i++) {
            double angle1 = random.nextDouble() * 2.0 * Math.PI;
            double angle2 = random.nextDouble() * 2.0 * Math.PI;
            spark(consumer, pose, (float) (Math.cos(angle1) * 5.0), (float) (Math.sin(angle1) * 5.0),
                    (float) (Math.cos(angle2) * 5.0), (float) (Math.sin(angle2) * 5.0));
        }
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    /** {@code DrawableMuseCircle}: a fan from the centre colour to the edge colour, drawn as quads. */
    private static void circle(VertexConsumer consumer, Matrix4f pose, float radius, float z, int centre, int edge) {
        for (int i = 0; i < SEGMENTS; i++) {
            double a1 = 2.0 * Math.PI * i / SEGMENTS;
            double a2 = 2.0 * Math.PI * (i + 1) / SEGMENTS;
            consumer.addVertex(pose, 0.0F, 0.0F, z).setColor(centre);
            consumer.addVertex(pose, 0.0F, 0.0F, z).setColor(centre);
            consumer.addVertex(pose, (float) (Math.cos(a2) * radius), (float) (Math.sin(a2) * radius), z)
                    .setColor(edge);
            consumer.addVertex(pose, (float) (Math.cos(a1) * radius), (float) (Math.sin(a1) * radius), z)
                    .setColor(edge);
        }
    }

    /** {@code drawLightning}: a thin jagged line in front of the circles. */
    private void spark(VertexConsumer consumer, Matrix4f pose, float x1, float y1, float x2, float y2) {
        int steps = 4;
        float px = x1;
        float py = y1;
        for (int i = 1; i <= steps; i++) {
            float t = (float) i / steps;
            float nx = x1 + (x2 - x1) * t + (i < steps ? (random.nextFloat() - 0.5F) : 0.0F);
            float ny = y1 + (y2 - y1) * t + (i < steps ? (random.nextFloat() - 0.5F) : 0.0F);
            float dx = nx - px;
            float dy = ny - py;
            float length = (float) Math.sqrt(dx * dx + dy * dy);
            float ox = length == 0.0F ? 0.0F : -dy / length * 0.1F;
            float oy = length == 0.0F ? 0.0F : dx / length * 0.1F;
            consumer.addVertex(pose, px - ox, py - oy, 0.01F).setColor(WHITE);
            consumer.addVertex(pose, nx - ox, ny - oy, 0.01F).setColor(WHITE);
            consumer.addVertex(pose, nx + ox, ny + oy, 0.01F).setColor(WHITE);
            consumer.addVertex(pose, px + ox, py + oy, 0.01F).setColor(WHITE);
            px = nx;
            py = ny;
        }
    }

    @Override
    public ResourceLocation getTextureLocation(PlasmaBoltEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
