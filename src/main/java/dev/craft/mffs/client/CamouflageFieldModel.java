package dev.craft.mffs.client;

import dev.craft.mffs.block.ForceFieldBlockEntity;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Vector3f;

/**
 * Camouflage force field (spec 48 §9.3/§14): draws the model of the block the field block entity names, a full cube
 * with the still texture for water/lava, and the default camouflage model without a block.
 */
public final class CamouflageFieldModel extends BakedModelWrapper<BakedModel> {
    private static final FaceBakery FACE_BAKERY = new FaceBakery();
    private static final Map<TextureAtlasSprite, Map<Direction, List<BakedQuad>>> FLUID_QUADS =
            new ConcurrentHashMap<>();

    public CamouflageFieldModel(BakedModel original) {
        super(original);
    }

    private static BlockState camouflage(ModelData data) {
        return data.has(ForceFieldBlockEntity.CAMOUFLAGE) ? data.get(ForceFieldBlockEntity.CAMOUFLAGE) : null;
    }

    private static boolean isFluid(BlockState camouflage) {
        return camouflage.getBlock() instanceof LiquidBlock;
    }

    private static BakedModel modelOf(BlockState camouflage) {
        return Minecraft.getInstance().getBlockRenderer().getBlockModel(camouflage);
    }

    private static TextureAtlasSprite stillSprite(BlockState fluid) {
        return Minecraft.getInstance().getBlockRenderer().getBlockModelShaper().getParticleIcon(fluid);
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData data,
                                    RenderType renderType) {
        BlockState camouflage = camouflage(data);
        if (camouflage == null) {
            return super.getQuads(state, side, rand, data, renderType);
        }
        if (isFluid(camouflage)) {
            if (side == null || renderType != null
                    && renderType != ItemBlockRenderTypes.getRenderLayer(camouflage.getFluidState())) {
                return List.of();
            }
            return FLUID_QUADS.computeIfAbsent(stillSprite(camouflage), key -> new ConcurrentHashMap<>())
                    .computeIfAbsent(side, face -> List.of(cubeFace(stillSprite(camouflage), face)));
        }
        return modelOf(camouflage).getQuads(camouflage, side, rand, ModelData.EMPTY, renderType);
    }

    private static BakedQuad cubeFace(TextureAtlasSprite sprite, Direction face) {
        BlockElementFace element = new BlockElementFace(face, 0, "", new BlockFaceUV(new float[] {0, 0, 16, 16}, 0));
        return FACE_BAKERY.bakeQuad(new Vector3f(0, 0, 0), new Vector3f(16, 16, 16), element, sprite, face,
                BlockModelRotation.X0_Y0, null, true);
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
        BlockState camouflage = camouflage(data);
        if (camouflage == null) {
            return super.getRenderTypes(state, rand, data);
        }
        if (isFluid(camouflage)) {
            return ChunkRenderTypeSet.of(ItemBlockRenderTypes.getRenderLayer(camouflage.getFluidState()));
        }
        return modelOf(camouflage).getRenderTypes(camouflage, rand, ModelData.EMPTY);
    }

    @Override
    public TextureAtlasSprite getParticleIcon(ModelData data) {
        BlockState camouflage = camouflage(data);
        return camouflage == null ? super.getParticleIcon(data) : modelOf(camouflage).getParticleIcon(ModelData.EMPTY);
    }

    @Override
    public TriState useAmbientOcclusion(BlockState state, ModelData data, RenderType renderType) {
        BlockState camouflage = camouflage(data);
        return camouflage == null || isFluid(camouflage) ? super.useAmbientOcclusion(state, data, renderType)
                : modelOf(camouflage).useAmbientOcclusion(camouflage, ModelData.EMPTY, renderType);
    }
}
