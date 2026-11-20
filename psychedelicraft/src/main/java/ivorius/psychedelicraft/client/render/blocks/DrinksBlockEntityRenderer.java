/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import ivorius.psychedelicraft.block.PlacedDrinksBlock;
import ivorius.psychedelicraft.client.render.PlacedDrinksModelProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DrinksBlockEntityRenderer implements BlockEntityRenderer<PlacedDrinksBlock.Data> {
    private static final VoxelShape FILLED_SLOT_RAY_TRACE_SHAPE = Block.box(-2, 0, -2, 2, 4, 2);
    private static final VoxelShape EMPTY_SLOT_RAY_TRACE_SHAPE = Block.box(-2, 0, -2, 2, 0.01, 2);

    public DrinksBlockEntityRenderer(BlockEntityRendererProvider.Context context) { }

    @Override
    public void render(PlacedDrinksBlock.Data entity, float tickDelta, PoseStack matrices, MultiBufferSource vertices, int light, int overlay) {
        entity.forEachDrink((y, drink) -> {
            PlacedDrinksModelProvider.Entry geometry = PlacedDrinksModelProvider.INSTANCE.get("ground", drink.stack().getItem()).orElse(PlacedDrinksModelProvider.Entry.DEFAULT);
            matrices.pushPose();
            matrices.translate(drink.x(), y, drink.z());
            matrices.translate(0.5F, 0, 0.5F);
            matrices.mulPose(Axis.YP.rotationDegrees(drink.rotation()));
            matrices.translate(-0.5F, 0, -0.5F);
            PlacedDrinksModelProvider.INSTANCE.renderDrink("ground", drink.stack(), matrices, vertices, light, overlay);
            matrices.popPose();

            return geometry.height();
        });

        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        if (player != null && client.hitResult.getType() == HitResult.Type.BLOCK) {
            BlockHitResult hit = (BlockHitResult)client.hitResult;
            if (hit.getBlockPos().equals(entity.getBlockPos())) {
                PlacedDrinksBlock.Data.getHitPos(hit).ifPresent(pos -> {
                    LevelRenderer.renderVoxelShape(matrices, vertices.getBuffer(RenderType.lines()), entity.hasDrink(pos) ? FILLED_SLOT_RAY_TRACE_SHAPE : EMPTY_SLOT_RAY_TRACE_SHAPE, pos.getX() / 16F, 0, pos.getZ() / 16F, 0, 0, 0, 0.4F, false);
                    RenderSystem.setShaderColor(0, 0, 0, 0.4F);
                    Minecraft.getInstance().renderBuffers().bufferSource().endBatch(RenderType.lines());
                    RenderSystem.setShaderColor(1, 1, 1, 1);
                });
            }
        }
    }
}
