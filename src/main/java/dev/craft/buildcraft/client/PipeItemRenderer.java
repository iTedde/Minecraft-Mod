package dev.craft.buildcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import dev.craft.buildcraft.transport.ItemPipeBlockEntity;
import dev.craft.buildcraft.transport.TravelingItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemDisplayContext;

/**
 * {@code RenderPipe.renderSolids}: the first 10 items of a pipe as floating items at scale 0.7, lifted by 0.25 so the
 * item's middle sits on the pipe's axis. Items turn towards the camera around the vertical axis.
 */
public class PipeItemRenderer implements BlockEntityRenderer<ItemPipeBlockEntity> {
    private static final int MAX_ITEMS = 10;
    private static final float SCALE = 0.7F;
    /** The ground transform draws the item 2/16 above its origin at half size. */
    private static final double GROUND_OFFSET = 2.0 / 16.0 * SCALE;

    private final ItemRenderer itemRenderer;

    public PipeItemRenderer(BlockEntityRendererProvider.Context context) {
        itemRenderer = context.getItemRenderer();
    }

    @Override
    public void render(ItemPipeBlockEntity pipe, float partialTick, PoseStack poseStack, MultiBufferSource buffers,
                       int packedLight, int packedOverlay) {
        BlockPos pos = pipe.getBlockPos();
        int count = 0;
        for (TravelingItem item : pipe.getItems()) {
            if (count++ >= MAX_ITEMS) {
                break;
            }
            poseStack.pushPose();
            poseStack.translate(item.getX(partialTick) - pos.getX(),
                    item.getY(partialTick) - pos.getY() + 0.25 - GROUND_OFFSET, item.getZ(partialTick) - pos.getZ());
            poseStack.mulPose(Axis.YP.rotationDegrees(
                    180.0F - Minecraft.getInstance().gameRenderer.getMainCamera().getYRot()));
            poseStack.scale(SCALE, SCALE, SCALE);
            itemRenderer.renderStatic(item.getStack(), ItemDisplayContext.GROUND, packedLight, packedOverlay,
                    poseStack, buffers, pipe.getLevel(), item.hashCode());
            poseStack.popPose();
        }
    }
}
