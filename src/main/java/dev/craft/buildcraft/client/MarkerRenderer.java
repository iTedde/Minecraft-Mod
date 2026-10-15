package dev.craft.buildcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.craft.buildcraft.builders.MarkerBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

/** Blue signal lasers of a landmark and the red laser box of a landmark group (drawn by its origin). */
public class MarkerRenderer implements BlockEntityRenderer<MarkerBlockEntity> {
    private static final int REACH = MarkerBlockEntity.MAX_SIZE - 1;

    public MarkerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(MarkerBlockEntity marker, float partialTick, PoseStack pose, MultiBufferSource buffers,
                       int light, int overlay) {
        BlockPos pos = marker.getBlockPos();
        int x = pos.getX();
        int y = pos.getY();
        int z = pos.getZ();
        if (marker.showsSignals()) {
            for (int axis = 0; axis < 3; axis++) {
                if (!marker.isAxisFree(axis)) {
                    continue;
                }
                int dx = axis == 0 ? REACH : 0;
                int dy = axis == 1 ? REACH : 0;
                int dz = axis == 2 ? REACH : 0;
                BlockBoxRenderer.laser(pose, buffers, marker.getLevel(), pos, BlockBoxRenderer.LASER_BLUE,
                        x, y, z, x + dx, y + dy, z + dz);
                BlockBoxRenderer.laser(pose, buffers, marker.getLevel(), pos, BlockBoxRenderer.LASER_BLUE,
                        x - dx, y - dy, z - dz, x, y, z);
            }
        }
        int[] b = marker.getClientBounds();
        if (b != null) {
            BlockBoxRenderer.laserBox(pose, buffers, marker.getLevel(), pos, BlockBoxRenderer.LASER_RED,
                    b[0], b[1], b[2], b[3], b[4], b[5]);
        }
    }

    @Override
    public boolean shouldRenderOffScreen(MarkerBlockEntity marker) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public AABB getRenderBoundingBox(MarkerBlockEntity marker) {
        return AABB.INFINITE;
    }
}
