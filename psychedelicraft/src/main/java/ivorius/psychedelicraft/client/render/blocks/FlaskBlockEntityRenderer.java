/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import ivorius.psychedelicraft.block.entity.DistilleryBlockEntity;
import ivorius.psychedelicraft.block.entity.FlaskBlockEntity;
import ivorius.psychedelicraft.client.render.FluidBoxRenderer;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;

/**
 * Created by lukas on 25.10.14.
 * Updated by Sollace on 5 Jan 2023
 *
 * Renders fluid inside the flask
 */
public class FlaskBlockEntityRenderer<T extends FlaskBlockEntity> implements BlockEntityRenderer<T> {

    public FlaskBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(T entity, float tickDelta, PoseStack matrices, MultiBufferSource vertices, int light, int overlay) {
        matrices.pushPose();
        matrices.translate(0.5F, 0, 0.5F);

        float scale = 1/8F - 0.001F;
        matrices.scale(scale, scale, scale);


        Resovoir tank = entity.getPrimaryTank();
        ItemFluids stack = tank.getContents();

        if (!stack.isEmpty()) {
            float fillPercentage = Mth.clamp((float)stack.amount() / tank.getCapacity(), 0, 1);

            FluidBoxRenderer fluidRenderer = FluidBoxRenderer.getInstance()
                    .texture(vertices, stack)
                    .light(light).overlay(overlay)
                    .position(matrices);

            float firstLevelHeight = Math.min(fillPercentage * 2, 2);

            fluidRenderer.draw(-1, 0, -1, 2, firstLevelHeight, 2, Direction.UP);

            // lower
            fluidRenderer.draw(-1, 0, -2, 2, firstLevelHeight, 1, Direction.NORTH, Direction.UP);
            fluidRenderer.draw(-1, 0,  1, 2, firstLevelHeight, 1, Direction.SOUTH, Direction.UP);

            fluidRenderer.draw( 1, 0, -1, 1, firstLevelHeight, 2, Direction.EAST, Direction.UP);
            fluidRenderer.draw(-2, 0, -1, 1, firstLevelHeight, 2, Direction.WEST, Direction.UP);

            fillPercentage = Math.max(fillPercentage - 0.5F, 0);
            float secondLevelHeight = Math.min(fillPercentage * 2, 2);
            if (secondLevelHeight > 0) {
                if (!(entity instanceof DistilleryBlockEntity)) {
                    matrices.translate(0, -1, 0);
                }

                fluidRenderer.draw(-1, 4.5F, -1, 2, secondLevelHeight, 2, Direction.UP);

                fluidRenderer.draw(-1, 4.5F, -1.5F, 2, secondLevelHeight, 0.5F, Direction.NORTH, Direction.UP);
                fluidRenderer.draw(-1, 4.5F,  1, 2, secondLevelHeight, 0.5F, Direction.SOUTH, Direction.UP);

                fluidRenderer.draw( 1, 4.5F, -1, 0.5F, secondLevelHeight, 2, Direction.EAST, Direction.UP);
                fluidRenderer.draw(-1.5F, 4.5F, -1, 0.5F, secondLevelHeight, 2, Direction.WEST, Direction.UP);
            }
        }

        matrices.popPose();
    }
}
