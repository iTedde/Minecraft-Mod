package dev.craft.secretrooms.client;

import java.util.ArrayList;
import java.util.List;

import dev.craft.secretrooms.CamoBlockEntity;
import dev.craft.secretrooms.OneWayGlassBlock;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;

/**
 * Camouflage model (spec 50 §4; the original drew through a "FakeWorld"): draws the model of the copied block. For
 * one-way glass only the facing side is camouflaged, the other sides keep the glass model.
 */
public class CamoBakedModel extends BakedModelWrapper<BakedModel> {
    public CamoBakedModel(BakedModel original) {
        super(original);
    }

    private static BlockState camo(ModelData data) {
        BlockState camo = data.get(CamoBlockEntity.CAMO);
        return camo == null || camo.getBlock() instanceof dev.craft.secretrooms.CamoBlock ? null : camo;
    }

    private static BakedModel modelOf(BlockState state) {
        return Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData data,
            RenderType renderType) {
        BlockState camo = camo(data);
        if (camo == null) {
            return super.getQuads(state, side, rand, data, renderType);
        }
        BakedModel camoModel = modelOf(camo);
        boolean camoType = renderType == null || camoModel.getRenderTypes(camo, rand, ModelData.EMPTY)
                .contains(renderType);
        if (state != null && (state.getBlock() instanceof DoorBlock || state.getBlock() instanceof TrapDoorBlock)) {
            if (!camoType) {
                return List.of();
            }
            List<BakedQuad> quads = new ArrayList<>();
            for (BakedQuad quad : originalModel.getQuads(state, side, rand, data, null)) {
                quads.add(retexture(quad, camo, camoModel, rand));
            }
            return quads;
        }
        if (state != null && state.getBlock() instanceof OneWayGlassBlock) {
            Direction facing = state.getValue(OneWayGlassBlock.FACING);
            List<BakedQuad> quads = new ArrayList<>();
            if (side == facing) {
                if (camoType) {
                    quads.addAll(camoModel.getQuads(camo, side, rand, ModelData.EMPTY, renderType));
                }
            } else if (renderType == null || originalModel.getRenderTypes(state, rand, data).contains(renderType)) {
                quads.addAll(super.getQuads(state, side, rand, data, renderType));
            }
            return quads;
        }
        return camoType ? camoModel.getQuads(camo, side, rand, ModelData.EMPTY, renderType) : List.of();
    }

    /**
     * Door and trapdoor keep their shape: each quad gets the camouflage sprite of its direction, at the same relative
     * position in the sprite, and that face's tint.
     */
    private static BakedQuad retexture(BakedQuad quad, BlockState camo, BakedModel camoModel, RandomSource rand) {
        List<BakedQuad> faces = camoModel.getQuads(camo, quad.getDirection(), rand, ModelData.EMPTY, null);
        if (faces.isEmpty()) {
            faces = camoModel.getQuads(camo, null, rand, ModelData.EMPTY, null);
        }
        TextureAtlasSprite target = faces.isEmpty() ? camoModel.getParticleIcon(ModelData.EMPTY)
                : faces.get(0).getSprite();
        int tint = faces.isEmpty() ? -1 : faces.get(0).getTintIndex();
        TextureAtlasSprite source = quad.getSprite();
        int[] vertices = quad.getVertices().clone();
        int stride = vertices.length / 4;
        for (int v = 0; v < 4; v++) {
            int u = v * stride + 4;
            float relU = (Float.intBitsToFloat(vertices[u]) - source.getU0()) / (source.getU1() - source.getU0());
            float relV = (Float.intBitsToFloat(vertices[u + 1]) - source.getV0()) / (source.getV1() - source.getV0());
            vertices[u] = Float.floatToRawIntBits(target.getU0() + relU * (target.getU1() - target.getU0()));
            vertices[u + 1] = Float.floatToRawIntBits(target.getV0() + relV * (target.getV1() - target.getV0()));
        }
        return new BakedQuad(vertices, tint, quad.getDirection(), target, quad.isShade());
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        BlockState camo = camo(data);
        if (camo == null) {
            return super.getRenderTypes(state, rand, data);
        }
        ChunkRenderTypeSet camoTypes = modelOf(camo).getRenderTypes(camo, rand, ModelData.EMPTY);
        if (state.getBlock() instanceof OneWayGlassBlock) {
            return ChunkRenderTypeSet.union(camoTypes, super.getRenderTypes(state, rand, data));
        }
        return camoTypes;
    }

    @Override
    public TextureAtlasSprite getParticleIcon(ModelData data) {
        BlockState camo = camo(data);
        return camo == null ? super.getParticleIcon(data) : modelOf(camo).getParticleIcon(ModelData.EMPTY);
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, BlockPos pos, BlockState state, ModelData data) {
        return level.getBlockEntity(pos) instanceof CamoBlockEntity camo ? camo.getModelData() : data;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return true;
    }
}
