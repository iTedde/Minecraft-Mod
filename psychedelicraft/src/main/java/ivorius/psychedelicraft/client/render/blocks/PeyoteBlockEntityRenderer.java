/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.PeyoteBlock;
import ivorius.psychedelicraft.block.entity.*;
import ivorius.psychedelicraft.client.render.RenderUtil;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import net.minecraft.world.phys.Vec3;
import java.util.Random;
import java.util.stream.IntStream;

public class PeyoteBlockEntityRenderer implements BlockEntityRenderer<PeyoteBlockEntity> {
    private static final ResourceLocation[] TEXTURES = IntStream.range(0, 4)
            .mapToObj(i -> Psychedelicraft.id("textures/entity/peyote/peyote_stage" + i + ".png"))
            .toArray(ResourceLocation[]::new);

    private final ModelPart[] models = {
            PeyoteModel.stage0().bakeRoot(),
            PeyoteModel.stage1().bakeRoot(),
            PeyoteModel.stage2().bakeRoot(),
            PeyoteModel.stage3().bakeRoot()
    };

    public PeyoteBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(PeyoteBlockEntity entity, float tickDelta, PoseStack matrices, MultiBufferSource vertices, int light, int overlay) {
        matrices.pushPose();
        matrices.translate(0.5F, 0.5f, 0.5F);
        matrices.translate(0, 1, 0);


        Random rng = RenderUtil.random(entity.getBlockState().getSeed(entity.getBlockPos()));

        Vec3 offset = entity.getBlockState().getOffset(entity.getLevel(), entity.getBlockPos());
        matrices.translate(offset.x, offset.y, offset.z);
        matrices.mulPose(Axis.YP.rotationDegrees(rng.nextInt(4) * 180));

        matrices.mulPose(Axis.XP.rotationDegrees(180));

        int age = entity.getBlockState().getValue(PeyoteBlock.AGE) % 4;
        models[age].render(matrices, vertices.getBuffer(RenderType.entityCutout(TEXTURES[age])), light, overlay, CommonColors.WHITE);

        matrices.popPose();
    }
}
