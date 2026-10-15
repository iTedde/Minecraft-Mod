package dev.craft.buildcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.craft.buildcraft.factory.PumpBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

/** The tube of {@code TilePump} (an {@code EntityBlock} with texture 102): 0.5 × 0.5 from the tube end up to the pump. */
public class PumpRenderer implements BlockEntityRenderer<PumpBlockEntity> {
    public PumpRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(PumpBlockEntity pump, float partialTick, PoseStack pose, MultiBufferSource buffers, int light,
                       int overlay) {
        if (!pump.hasTube()) {
            return;
        }
        BlockPos pos = pump.getBlockPos();
        double tubeY = pump.getTubeY();
        BlockBoxRenderer.box(pose, buffers, pump.getLevel(), pos, BlockBoxRenderer.PUMP_TUBE, pos.getX() + 0.25,
                tubeY, pos.getZ() + 0.25, 0.5, pos.getY() - tubeY, 0.5);
    }

    @Override
    public boolean shouldRenderOffScreen(PumpBlockEntity pump) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(PumpBlockEntity pump) {
        return AABB.INFINITE;
    }
}
