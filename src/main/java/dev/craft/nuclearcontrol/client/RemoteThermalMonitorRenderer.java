package dev.craft.nuclearcontrol.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.craft.Craft;
import dev.craft.nuclearcontrol.block.RemoteThermalMonitorBlock;
import dev.craft.nuclearcontrol.block.RemoteThermalMonitorBlockEntity;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

/**
 * {@code TileEntityRemoteThermoRenderer}: on the display a 14 × 4 px bar below the centre – grey without reactor,
 * otherwise filled with the colour gradient up to {@code heat / heat level} – and the heat level in black above the
 * centre; both turned by the display rotation. Without energy only the number shows.
 */
public class RemoteThermalMonitorRenderer implements BlockEntityRenderer<RemoteThermalMonitorBlockEntity> {
    private static final ResourceLocation BAR = ResourceLocation.fromNamespaceAndPath(Craft.MODID,
            "textures/block/nuclearcontrol/thermo_bar.png");
    private static final float SCALE = 1.0F / 60.0F;
    private static final float LEFT = -0.4375F;
    private static final float TOP = -0.3125F;
    private static final float WIDTH = 0.875F;
    private static final float HEIGHT = 0.25F;
    /** u of the bar strip inside the 16 × 16 tile (px 161 of the 256 sheet). */
    private static final float U = 1.0F / 16.0F;

    private final Font font;

    public RemoteThermalMonitorRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.getFont();
    }

    @Override
    public void render(RemoteThermalMonitorBlockEntity monitor, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        poseStack.pushPose();
        FaceTransform.apply(poseStack, monitor.getBlockState().getValue(RemoteThermalMonitorBlock.FACING));
        poseStack.translate(0.0, 0.502, 0.0);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        switch (monitor.getRotation()) {
            case 1 -> poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0F));
            case 2 -> poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
            case 3 -> poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
            default -> {
            }
        }
        int fire = monitor.getOnFire();
        if (fire > -2) {
            VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(BAR));
            PoseStack.Pose pose = poseStack.last();
            float middle;
            float v;
            if (fire == -1) {
                v = 0.0F;
                middle = WIDTH;
            } else {
                float level = monitor.getHeatLevel() > 0 ? Math.min(1.0F, (float) fire / monitor.getHeatLevel()) : 1.0F;
                middle = level * WIDTH;
                v = 4.0F / 16.0F;
            }
            quad(consumer, pose, LEFT, LEFT + middle, U, U + middle, v, packedLight);
            if (middle != WIDTH) {
                quad(consumer, pose, LEFT + middle, LEFT + WIDTH, U, U + WIDTH, 8.0F / 16.0F, packedLight);
            }
        }
        String text = Integer.toString(monitor.getHeatLevel());
        poseStack.scale(SCALE, -SCALE, SCALE);
        font.drawInBatch(text, -font.width(text) / 2.0F, -font.lineHeight, 0x000000, false,
                poseStack.last().pose(), buffers, Font.DisplayMode.POLYGON_OFFSET, 0, packedLight);
        poseStack.popPose();
    }

    private static void quad(VertexConsumer consumer, PoseStack.Pose pose, float x0, float x1, float u0, float u1,
                             float v, int light) {
        float v1 = v + HEIGHT;
        vertex(consumer, pose, x0, TOP, u0, v, light);
        vertex(consumer, pose, x1, TOP, u1, v, light);
        vertex(consumer, pose, x1, TOP + HEIGHT, u1, v1, light);
        vertex(consumer, pose, x0, TOP + HEIGHT, u0, v1, light);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float u,
                               float v, int light) {
        consumer.addVertex(pose, x, y, -0.001F).setColor(255, 255, 255, 255).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
    }
}
