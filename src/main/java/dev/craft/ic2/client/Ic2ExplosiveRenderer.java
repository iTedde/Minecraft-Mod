package dev.craft.ic2.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.craft.ic2.block.Ic2ExplosiveEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.TntMinecartRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

/** IC2 {@code RenderExplosiveBlock}: the block swells during the last 10 ticks and flashes white every 5 ticks. */
public class Ic2ExplosiveRenderer extends EntityRenderer<Ic2ExplosiveEntity> {
    private final BlockRenderDispatcher blockRenderer;

    public Ic2ExplosiveRenderer(EntityRendererProvider.Context context) {
        super(context);
        shadowRadius = 0.0F;
        blockRenderer = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(Ic2ExplosiveEntity entity, float entityYaw, float partialTick, PoseStack poseStack,
                       MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.translate(0.0F, 0.5F, 0.0F);
        int fuse = entity.getFuse();
        if (fuse - partialTick + 1.0F < 10.0F) {
            float swell = Mth.clamp(1.0F - (fuse - partialTick + 1.0F) / 10.0F, 0.0F, 1.0F);
            swell *= swell;
            swell *= swell;
            float scale = 1.0F + swell * 0.3F;
            poseStack.scale(scale, scale, scale);
        }
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0F));
        poseStack.translate(-0.5F, -0.5F, 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F));
        TntMinecartRenderer.renderWhiteSolidBlock(blockRenderer, entity.getRenderState(), poseStack, buffer,
                packedLight, fuse / 5 % 2 == 0);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(Ic2ExplosiveEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
