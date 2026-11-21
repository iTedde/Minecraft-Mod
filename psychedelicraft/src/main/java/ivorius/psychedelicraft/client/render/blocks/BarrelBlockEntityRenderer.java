/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import ivorius.psychedelicraft.block.BarrelBlock;
import ivorius.psychedelicraft.block.entity.BarrelBlockEntity;
import ivorius.psychedelicraft.client.render.RenderUtil;
import ivorius.psychedelicraft.fluid.container.Resovoir;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import org.joml.Matrix4f;

public class BarrelBlockEntityRenderer implements BlockEntityRenderer<BarrelBlockEntity> {
    private final BarrelModel model;

    public BarrelBlockEntityRenderer(BlockEntityRendererProvider.Context context) {
        model = new BarrelModel(BarrelModel.getTexturedModelData().bakeRoot());
    }

    @Override
    public void render(BarrelBlockEntity entity, float tickDelta, PoseStack matrices, MultiBufferSource vertices, int light, int overlay) {
        matrices.pushPose();
        matrices.translate(0.5F, 0, 0.5F);
        matrices.mulPose(Axis.YP.rotationDegrees(180 - entity.getBlockState().getValue(BarrelBlock.FACING).toYRot()));

        model.setRotationAngles(entity, tickDelta);
        model.renderToBuffer(matrices, vertices.getBuffer(model.renderType(getBarrelTexture(entity))), light, overlay, CommonColors.WHITE);

        Resovoir tank = entity.getPrimaryTank();

        ItemFluids stack = tank.getContents();
        if (!stack.isEmpty()) {
            ResourceLocation symbol = stack.fluid().getSymbol(stack);

            if (Minecraft.getInstance().getResourceManager().getResource(symbol).isPresent()) {
                matrices.translate(0, 0.5, 0);
                if (entity.getBlockState().getValue(BarrelBlock.FACING).getAxis() == net.minecraft.core.Direction.Axis.Y) {
                    Matrix4f mat = new Matrix4f();
                    Axis.XP.rotationDegrees(90).get(mat);

                    matrices.mulPose(mat);
                    matrices.translate(0, -0.1, 0);
                }
                float barrelZ = -0.4376F + 0.06F;
                float iconSize = 0.5F;
                VertexConsumer buffer = vertices.getBuffer(model.renderType(symbol));


                matrices.mulPose(Axis.YP.rotationDegrees(90));

                for (int i = 0; i < 2; i++) {
                    RenderUtil.vertex(buffer, matrices, -iconSize, -iconSize, barrelZ, 1, 1, overlay, light);
                    RenderUtil.vertex(buffer, matrices, -iconSize,  iconSize, barrelZ, 1, 0, overlay, light);
                    RenderUtil.vertex(buffer, matrices,  iconSize,  iconSize, barrelZ, 0, 0, overlay, light);
                    RenderUtil.vertex(buffer, matrices,  iconSize, -iconSize, barrelZ, 0, 1, overlay, light);
                    matrices.mulPose(Axis.YP.rotationDegrees(180));
                }
            }
        }

        matrices.popPose();
    }

    public static ResourceLocation getBarrelTexture(BarrelBlockEntity barrel) {
        return BuiltInRegistries.BLOCK.getKey(barrel.getBlockState().getBlock()).withPath(p -> "textures/entity/barrel/" + p + ".png");
    }
}
