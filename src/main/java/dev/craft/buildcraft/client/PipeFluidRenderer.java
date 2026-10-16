package dev.craft.buildcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.craft.buildcraft.transport.FluidPipeBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * BuildCraft {@code RenderPipe.renderLiquids}: per section a box in 40 stages ({@code amount / 250 · 39}). Horizontal
 * arms fill from the bottom, vertical arms are a column growing in width; the center is a column if there is liquid
 * above it and a basin if there is none above or some at a horizontal side.
 */
public class PipeFluidRenderer implements BlockEntityRenderer<FluidPipeBlockEntity> {
    private static final float SIZE = 0.5F;

    public PipeFluidRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(FluidPipeBlockEntity pipe, float partialTick, PoseStack pose, MultiBufferSource buffers,
                       int light, int overlay) {
        FluidStack[] cache = pipe.getRenderCache();
        boolean sides = false;
        boolean above = false;
        for (Direction side : Direction.values()) {
            FluidStack liquid = cache[side.get3DDataValue()];
            if (liquid.isEmpty()) {
                continue;
            }
            float ratio = stage(liquid) / 40.0F;
            switch (side) {
                case UP -> {
                    above = true;
                    verticalArm(pose, buffers, liquid, ratio, 0.74F, 1.0F, light);
                }
                case DOWN -> verticalArm(pose, buffers, liquid, ratio, -0.01F, 0.25F, light);
                default -> {
                    sides = true;
                    horizontalArm(pose, buffers, liquid, ratio, side, light);
                }
            }
        }
        FluidStack liquid = cache[FluidPipeBlockEntity.CENTER];
        if (liquid.isEmpty()) {
            return;
        }
        float ratio = stage(liquid) / 40.0F;
        if (above) {
            float half = (SIZE / 2.0F - 0.02F) * ratio;
            FluidBoxRenderer.render(pose, buffers, liquid, 0.5F - half, 0.26F, 0.5F - half, 0.5F + half, 0.74F,
                    0.5F + half, light);
        }
        if (!above || sides) {
            FluidBoxRenderer.render(pose, buffers, liquid, 0.26F, 0.26F, 0.26F, 0.74F,
                    0.26F + (SIZE - 0.02F) * ratio, 0.74F, light);
        }
    }

    private static int stage(FluidStack liquid) {
        return (int) ((float) liquid.getAmount() / FluidPipeBlockEntity.CAPACITY * 39.0F);
    }

    /** {@code sideVertical}: a square column 0.74–1, half width {@code (0.25 − 0.01)·ratio}. */
    private static void verticalArm(PoseStack pose, MultiBufferSource buffers, FluidStack liquid, float ratio,
                                    float y0, float y1, int light) {
        float half = (SIZE / 2.0F - 0.01F) * ratio;
        FluidBoxRenderer.render(pose, buffers, liquid, 0.5F - half, y0, 0.5F - half, 0.5F + half, y1, 0.5F + half,
                light);
    }

    /** {@code sideHorizontal}: from the block edge 0.26 deep, 0.26–0.74 wide, filled from 0.26 upwards. */
    private static void horizontalArm(PoseStack pose, MultiBufferSource buffers, FluidStack liquid, float ratio,
                                      Direction side, int light) {
        float depth = SIZE / 2.0F + 0.01F;
        float y1 = 0.26F + (SIZE - 0.02F) * ratio;
        float x0 = 0.26F;
        float x1 = 0.74F;
        float z0 = 0.26F;
        float z1 = 0.74F;
        switch (side) {
            case WEST -> { x0 = 0.0F; x1 = depth; }
            case EAST -> { x0 = 1.0F - depth; x1 = 1.0F; }
            case NORTH -> { z0 = 0.0F; z1 = depth; }
            default -> { z0 = 1.0F - depth; z1 = 1.0F; }
        }
        FluidBoxRenderer.render(pose, buffers, liquid, x0, 0.26F, z0, x1, y1, z1, light);
    }
}
