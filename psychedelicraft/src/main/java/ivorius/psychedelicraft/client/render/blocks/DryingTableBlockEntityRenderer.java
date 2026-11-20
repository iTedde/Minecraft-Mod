/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import ivorius.psychedelicraft.block.entity.DryingTableBlockEntity;

import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * Renders items on top of the drying table
 *
 * Updated by Sollace on 5 Jan 2023
 */
public class DryingTableBlockEntityRenderer implements BlockEntityRenderer<DryingTableBlockEntity> {
    public DryingTableBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(DryingTableBlockEntity entity, float tickDelta, PoseStack matrices, MultiBufferSource vertices, int light, int overlay) {
        matrices.pushPose();
        matrices.translate(0, 0.75f, 0);

        long seed = entity.getBlockPos().asLong() + 1;
        Random random = new Random(seed);

        for (int i = 0; i < entity.getContainerSize(); i++) {
            boolean result = i == 0;
            ItemStack stack = entity.getItem(i);

            if (stack.isEmpty()) {
                continue;
            }

            float positionX = result ? 0.5F : (0.35F + random.nextFloat() * 0.3F);
            float positionZ = result ? 0.5F : (0.35F + random.nextFloat() * 0.3F);
            float rotation = random.nextFloat() * 360.0f;

            matrices.pushPose();
            matrices.translate(positionX, i / 500F, positionZ);

            matrices.mulPose(Axis.YP.rotationDegrees(rotation));
            matrices.scale(0.5f, 0.5f, 0.5f);
            if (result) {
                matrices.scale(1.5F, 1.5F, 1.5F);
            }
            matrices.translate(0, 0, -0.2F);
            matrices.mulPose(Axis.XP.rotationDegrees(90));
            matrices.mulPose(Axis.ZN.rotationDegrees(-50));
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, light, overlay, matrices, vertices, entity.getLevel(), (int)seed);

            matrices.popPose();
        }

        matrices.popPose();
    }
}
