package dev.craft.gravitygun.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.craft.gravitygun.GrabbedBlock;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

/** Draws the grabbed block like a falling block, centred on the entity. */
public class GrabbedBlockRenderer extends EntityRenderer<GrabbedBlock> {
    private final BlockRenderDispatcher blocks;

    public GrabbedBlockRenderer(EntityRendererProvider.Context context) {
        super(context);
        blocks = context.getBlockRenderDispatcher();
        shadowRadius = 0.5F;
    }

    @Override
    public void render(GrabbedBlock entity, float yaw, float partialTick, PoseStack poseStack,
            MultiBufferSource buffer, int packedLight) {
        BlockState state = entity.getBlockState();
        if (state.getRenderShape() == RenderShape.INVISIBLE) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(-0.5, 0.0, -0.5);
        blocks.renderSingleBlock(state, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    @SuppressWarnings("deprecation")
    public ResourceLocation getTextureLocation(GrabbedBlock entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
