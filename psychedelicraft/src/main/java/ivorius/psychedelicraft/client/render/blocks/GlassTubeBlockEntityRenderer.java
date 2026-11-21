/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.blocks;

import java.util.function.Function;
import net.minecraft.Util;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;
import com.mojang.blaze3d.vertex.PoseStack;
import ivorius.psychedelicraft.block.GlassTubeBlock;
import ivorius.psychedelicraft.client.render.FluidBoxRenderer;
import ivorius.psychedelicraft.recipe.FluidMound;

public class GlassTubeBlockEntityRenderer implements BlockEntityRenderer<GlassTubeBlock.Data> {
    private static final Function<Integer, Function<Direction, VoxelShape>> SHAPE_PART_CACHE = Util.memoize(step -> {
        return GlassTubeBlock.createShapePartCache(GlassTubeBlock.RADIUS * 0.5, step / 10D, 0.1);
    });

    public GlassTubeBlockEntityRenderer(BlockEntityRendererProvider.Context context) {

    }

    @Override
    public void render(GlassTubeBlock.Data entity, float tickDelta, PoseStack matrices, MultiBufferSource vertices, int light, int overlay) {
        var contents = entity.getContents();

        BlockState state = entity.getBlockState();
        FluidBoxRenderer.getInstance().light(light).overlay(overlay).position(matrices);

        var in = state.getValue(GlassTubeBlock.IN).getDirection();
        if (in.isPresent()) {
            for (int i = 0; i < 5 && i < contents.size(); i++) {
                FluidMound mound = contents.get(i).fluids();
                if (!mound.isEmpty()) {
                    FluidBoxRenderer.getInstance().texture(vertices, mound.get(0)).draw(SHAPE_PART_CACHE.apply(-i + 5).apply(in.get()));
                }
            }
        }

        var out = state.getValue(GlassTubeBlock.OUT).getDirection();
        if (out.isPresent()) {
            for (int i = 5; i < contents.size(); i++) {
                FluidMound mound = contents.get(i).fluids();
                if (!mound.isEmpty()) {
                    FluidBoxRenderer.getInstance().texture(vertices, mound.get(0)).draw(SHAPE_PART_CACHE.apply(i - 5).apply(out.get()));
                }
            }
        }
    }
}
