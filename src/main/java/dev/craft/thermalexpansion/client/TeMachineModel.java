package dev.craft.thermalexpansion.client;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import dev.craft.Craft;
import dev.craft.thermalexpansion.machine.TeMachineBlock;
import dev.craft.thermalexpansion.machine.TeMachineBlockEntity;
import dev.craft.thermalexpansion.machine.TeMachineType;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.BlockElementFace;
import net.minecraft.client.renderer.block.model.BlockFaceUV;
import net.minecraft.client.renderer.block.model.FaceBakery;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BlockModelRotation;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import net.neoforged.neoforge.client.model.geometry.IGeometryLoader;
import net.neoforged.neoforge.client.model.geometry.IUnbakedGeometry;
import org.joml.Vector3f;

/**
 * Thermal Expansion {@code BlockMachine.getBlockTexture}: the front shows the machine (active or not), every other face
 * the texture column of its side group ({@code sideTex}) in the bottom, top or side row. The groups come from
 * {@link TeMachineBlockEntity#SIDES}.
 */
public final class TeMachineModel implements IUnbakedGeometry<TeMachineModel> {
    public static final ResourceLocation LOADER_ID = ResourceLocation.fromNamespaceAndPath(Craft.MODID, "te_machine");
    public static final IGeometryLoader<TeMachineModel> LOADER = TeMachineModel::read;

    private static final FaceBakery FACE_BAKERY = new FaceBakery();

    private final TeMachineType type;

    private TeMachineModel(TeMachineType type) {
        this.type = type;
    }

    private static TeMachineModel read(JsonObject json, JsonDeserializationContext context) {
        String name = GsonHelper.getAsString(json, "machine");
        for (TeMachineType type : TeMachineType.values()) {
            if (type.getName().equals(name)) {
                return new TeMachineModel(type);
            }
        }
        throw new IllegalArgumentException("unknown machine " + name);
    }

    public static ResourceLocation frontTexture(TeMachineType type, boolean active) {
        return ResourceLocation.fromNamespaceAndPath(Craft.MODID,
                "block/te/machine/" + type.getName() + "_front" + (active ? "_active" : ""));
    }

    public static ResourceLocation sideTexture(Direction face, int column) {
        String row = face == Direction.DOWN ? "bottom" : face == Direction.UP ? "top" : "side";
        return ResourceLocation.fromNamespaceAndPath(Craft.MODID, "block/te/machine/" + row + "_" + column);
    }

    /** The texture of {@code face} for side group {@code group}. */
    public static ResourceLocation texture(TeMachineType type, Direction face, int group, Direction facing,
                                           boolean active) {
        if (face == facing) {
            return frontTexture(type, active);
        }
        return sideTexture(face, type.getSideTexture(Math.floorMod(group, type.getNumGroups())));
    }

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
                           ItemOverrides overrides) {
        Map<ResourceLocation, TextureAtlasSprite> sprites = new HashMap<>();
        for (boolean active : new boolean[]{false, true}) {
            ResourceLocation front = frontTexture(type, active);
            sprites.put(front, spriteGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, front)));
        }
        for (Direction face : Direction.values()) {
            for (int group = 0; group < type.getNumGroups(); group++) {
                ResourceLocation side = sideTexture(face, type.getSideTexture(group));
                sprites.computeIfAbsent(side, location ->
                        spriteGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, location)));
            }
        }
        return new Baked(type, sprites);
    }

    private static final class Baked implements IDynamicBakedModel {
        private static final ChunkRenderTypeSet RENDER_TYPES = ChunkRenderTypeSet.of(RenderType.solid());

        private final TeMachineType type;
        private final Map<ResourceLocation, TextureAtlasSprite> sprites;
        private final Map<String, List<BakedQuad>> quads = new ConcurrentHashMap<>();

        Baked(TeMachineType type, Map<ResourceLocation, TextureAtlasSprite> sprites) {
            this.type = type;
            this.sprites = sprites;
        }

        @Override
        public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData data,
                                        RenderType renderType) {
            if (side == null) {
                return List.of();
            }
            Direction facing = state == null ? Direction.SOUTH : state.getValue(TeMachineBlock.FACING);
            boolean active = state != null && state.getValue(TeMachineBlock.ACTIVE);
            Long packed = data.get(TeMachineBlockEntity.SIDES);
            int group = packed == null ? 0 : (int) (packed >>> (8 * side.get3DDataValue()) & 0xFF);
            ResourceLocation texture = texture(type, side, group, facing, active);
            return quads.computeIfAbsent(side.getName() + "|" + texture,
                    key -> List.of(quad(side, sprites.get(texture))));
        }

        private static BakedQuad quad(Direction face, TextureAtlasSprite sprite) {
            Vector3f from = new Vector3f(0, 0, 0);
            Vector3f to = new Vector3f(16, 16, 16);
            BlockElementFace element = new BlockElementFace(face, BlockElementFace.NO_TINT, "",
                    new BlockFaceUV(new float[]{0, 0, 16, 16}, 0));
            return FACE_BAKERY.bakeQuad(from, to, element, sprite, face, BlockModelRotation.X0_Y0, null, true);
        }

        @Override
        public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource rand, ModelData data) {
            return RENDER_TYPES;
        }

        @Override
        public boolean useAmbientOcclusion() {
            return true;
        }

        @Override
        public boolean isGui3d() {
            return true;
        }

        @Override
        public boolean usesBlockLight() {
            return true;
        }

        @Override
        public boolean isCustomRenderer() {
            return false;
        }

        @Override
        public TextureAtlasSprite getParticleIcon() {
            return sprites.get(sideTexture(Direction.NORTH, 0));
        }

        @Override
        public ItemTransforms getTransforms() {
            return ItemTransforms.NO_TRANSFORMS;
        }

        @Override
        public ItemOverrides getOverrides() {
            return ItemOverrides.EMPTY;
        }
    }
}
