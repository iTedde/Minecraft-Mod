package ivorius.psychedelicraft.client.render.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import ivorius.psychedelicraft.block.entity.MashTubBlockEntity;
import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.fluid.Processable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.BlockHitResult;

public abstract class LabelledBlockEntityRenderer<T extends BlockEntity> implements BlockEntityRenderer<T> {

    protected final Font textRenderer;

    static boolean shouldRenderLabel(BlockEntity entity) {
        Minecraft client = Minecraft.getInstance();
        return entity.getBlockPos() != null
                && entity.getLevel() != null
                && client.getEntityRenderDispatcher().camera.getBlockPosition().distSqr(entity.getBlockPos()) < 4096
                && client.hitResult instanceof BlockHitResult hit
                && (hit.getBlockPos().equals(entity.getBlockPos()) || (
                        entity instanceof MashTubBlockEntity
                        && hit.getBlockPos().getY() == entity.getBlockPos().getY()
                        && Math.abs(hit.getBlockPos().getX() - entity.getBlockPos().getX()) < 2
                        && Math.abs(hit.getBlockPos().getZ() - entity.getBlockPos().getZ()) < 2
                ));
    }

    public LabelledBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        textRenderer = context.getFont();
    }

    static Component getFillPercentage(Processable.Context entity, int volume) {
        int totalFluids = entity.getTotalFluidVolume();
        int percentage = (int)((totalFluids / (float)volume) * 100);
        if (percentage == 0 && totalFluids > 0) {
            return Component.literal(FluidVolumes.format(totalFluids));
        }
        return switch (percentage) {
            case 100 -> Component.literal("Full");
            case 0 -> Component.literal("Empty");
            default -> Component.literal(percentage + "%");
        };
    }

    @Override
    public void render(T entity, float tickDelta, PoseStack matrices, MultiBufferSource vertices, int light, int overlay) {
        if (shouldRenderLabel(entity)) {
            matrices.pushPose();
            matrices.translate(0.5, 0, 0.5);
            matrices.mulPose(Minecraft.getInstance().getEntityRenderDispatcher().cameraOrientation());
            matrices.translate(0, 0, getLabelDistanceFromCenter(entity));
            float scale = getLabelScale(entity, tickDelta);
            matrices.scale(scale, -scale, scale);
            renderLabels(entity, tickDelta, matrices, vertices, light, overlay);
            matrices.popPose();
        }
    }

    protected double getLabelDistanceFromCenter(T entity) {
        return 0.5;
    }

    protected float getLabelScale(T entity, float tickDelta) {
        return 0.005F;
    }

    protected abstract void renderLabels(T entity, float tickDelta, PoseStack matrices, MultiBufferSource vertices, int light, int overlay);
}
