package dev.craft.nuclearcontrol.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.craft.nuclearcontrol.block.AttachedBlock;
import dev.craft.nuclearcontrol.block.ThermalMonitorBlockEntity;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;

/** {@code TileEntityIC2ThermoRenderer}: the heat level in black on the display, centred, its foot at z 11/16. */
public class ThermalMonitorRenderer implements BlockEntityRenderer<ThermalMonitorBlockEntity> {
    private static final float SCALE = 0.014F;

    private final Font font;

    public ThermalMonitorRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.getFont();
    }

    @Override
    public void render(ThermalMonitorBlockEntity monitor, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffers, int packedLight, int packedOverlay) {
        String text = Integer.toString(monitor.getHeatLevel());
        poseStack.pushPose();
        FaceTransform.apply(poseStack, monitor.getBlockState().getValue(AttachedBlock.FACING));
        poseStack.translate(0.0, AttachedBlock.HEIGHT / 16.0 - 0.5 + 0.002, 0.1875);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
        poseStack.scale(SCALE, -SCALE, SCALE);
        font.drawInBatch(text, -font.width(text) / 2.0F, -font.lineHeight, 0x000000, false,
                poseStack.last().pose(), buffers, Font.DisplayMode.POLYGON_OFFSET, 0, packedLight);
        poseStack.popPose();
    }
}
