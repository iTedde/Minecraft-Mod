package dev.craft.buildcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.craft.buildcraft.factory.TankBlockEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.neoforged.neoforge.fluids.FluidStack;

/** BuildCraft {@code RenderTank}: the content of this tank as a box 0.135–0.865 wide, in 100 height steps. */
public class TankRenderer implements BlockEntityRenderer<TankBlockEntity> {
    public TankRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(TankBlockEntity tank, float partialTick, PoseStack pose, MultiBufferSource buffers,
                       int light, int overlay) {
        FluidStack liquid = tank.getTank().getFluid();
        if (liquid.isEmpty()) {
            return;
        }
        float height = FluidBoxRenderer.level(liquid.getAmount(), tank.getTank().getCapacity());
        FluidBoxRenderer.render(pose, buffers, liquid, 0.135F, 0.0F, 0.135F, 0.865F, height, 0.865F, light);
    }
}
