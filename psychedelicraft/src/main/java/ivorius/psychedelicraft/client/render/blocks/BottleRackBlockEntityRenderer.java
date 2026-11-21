package ivorius.psychedelicraft.client.render.blocks;

import java.util.Random;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.item.ItemStack;
import com.mojang.blaze3d.vertex.PoseStack;
import ivorius.psychedelicraft.block.BottleRackBlock;
import ivorius.psychedelicraft.block.entity.BottleRackBlockEntity;
import ivorius.psychedelicraft.client.render.PlacedDrinksModelProvider;
import ivorius.psychedelicraft.client.render.RenderUtil;

/**
 * Created by lukas on 16.11.14.
 * Updated by Sollace on 6 Jan 2023
 */
public class BottleRackBlockEntityRenderer implements BlockEntityRenderer<BottleRackBlockEntity> {
    public BottleRackBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(BottleRackBlockEntity entity, float tickDelta, PoseStack matrices, MultiBufferSource vertices, int light, int overlay) {
        matrices.pushPose();
        matrices.translate(0.5F, 0.5F, 0.5F);
        Direction direction = entity.getBlockState().getValue(BottleRackBlock.FACING);
        if (direction.getAxis() == Axis.X) {
            direction = direction.getOpposite();
        }
        float facing = direction.toYRot() + 90;

        matrices.mulPose(com.mojang.math.Axis.YP.rotationDegrees(facing));

        double offset = ((BottleRackBlock)entity.getBlockState().getBlock()).getZOffset() / 16D;

        matrices.translate(0.14F - offset, -0.55F, -0.8F);
        matrices.mulPose(com.mojang.math.Axis.ZN.rotationDegrees(-90));

        Random rng = RenderUtil.random(entity.getBlockPos().asLong());

        final float spacing = 0.3F;

        for (int i = 0; i < entity.getContainerSize(); i++) {
            ItemStack bottle = entity.getItem(i);
            float rot = rng.nextFloat() - 0.5F;
            if (!bottle.isEmpty()) {
                matrices.pushPose();
                matrices.translate((1 - (i / 3)) * spacing, 0, (i % 3) * spacing);
                float rotPoint = 1F;
                matrices.translate(0, rotPoint, rotPoint * -1.2F);
                matrices.mulPose(com.mojang.math.Axis.XP.rotationDegrees(rot * 4));
                matrices.translate(0, -rotPoint, -rotPoint * -1.2F);

                PlacedDrinksModelProvider.INSTANCE.renderDrink("ground", bottle, matrices, vertices, light, overlay);

                matrices.popPose();
            }
        }
        matrices.popPose();
    }
}
