/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import ivorius.psychedelicraft.block.FluidCauldronBlock;
import ivorius.psychedelicraft.client.render.FluidBoxRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.LayeredCauldronBlock;

/**
 * Renders fluid in the cauldron
 */
public class CauldronBlockEntityRenderer implements BlockEntityRenderer<FluidCauldronBlock.Data> {
    public CauldronBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(FluidCauldronBlock.Data entity, float tickDelta, PoseStack matrices, MultiBufferSource vertices, int light, int overlay) {
        int level = entity.getBlockState().getValue(LayeredCauldronBlock.LEVEL);
        if (level > 0) {
            float unit = 0.0625F;
            float inset = unit * 2;
            // Cauldron model doesn't follow a linear progression, yaaaaaaay
            float fillPercentage = level == 1 ? unit * 6.16F : level == 2 ? unit * 9.85F : unit * 13.54F;
            FluidBoxRenderer.getInstance().scale(1).light(light).overlay(overlay).position(matrices)
                .texture(vertices, entity.getFluid())
                .draw(inset, unit * 4, inset, 1 - inset * 2, fillPercentage * (1 - inset * 1.5F), 1 - inset * 2, Direction.UP);
        }
    }
}
