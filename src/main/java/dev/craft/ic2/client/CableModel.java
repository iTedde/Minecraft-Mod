package dev.craft.ic2.client;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import dev.craft.Craft;
import dev.craft.ic2.cable.CableBlock;
import dev.craft.ic2.cable.CableBlockEntity;
import dev.craft.ic2.cable.CableType;
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
 * Cable rendering after IC2 {@code RenderBlockCable}: a core cube with arms towards connected sides, textured by
 * block bounds; foamed cables render as a full block with the foam texture. The texture column (paint color or
 * detector/splitter state) and the foam color come from {@link CableBlockEntity#getModelData()}.
 */
public final class CableModel implements IUnbakedGeometry<CableModel> {
    public static final ResourceLocation LOADER_ID = ResourceLocation.fromNamespaceAndPath(Craft.MODID, "cable");
    public static final IGeometryLoader<CableModel> LOADER = CableModel::read;

    private static final FaceBakery FACE_BAKERY = new FaceBakery();

    private final CableType type;

    private CableModel(CableType type) {
        this.type = type;
    }

    private static CableModel read(JsonObject json, JsonDeserializationContext context) {
        String name = GsonHelper.getAsString(json, "cable");
        for (CableType type : CableType.values()) {
            if (type.getName().equals(name)) {
                return new CableModel(type);
            }
        }
        throw new IllegalArgumentException("unknown cable type " + name);
    }

    public static ResourceLocation cableTexture(CableType type, int column) {
        return ResourceLocation.fromNamespaceAndPath(Craft.MODID, "block/cable/" + type.getName() + "_" + column);
    }

    public static ResourceLocation foamTexture(int color) {
        return ResourceLocation.fromNamespaceAndPath(Craft.MODID, "block/cable/foam_" + color);
    }

    public static final ResourceLocation WET_FOAM_TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "block/cable/foam_wet");

    @Override
    public BakedModel bake(IGeometryBakingContext context, ModelBaker baker,
                           Function<Material, TextureAtlasSprite> spriteGetter, ModelState modelState,
                           ItemOverrides overrides) {
        Function<ResourceLocation, TextureAtlasSprite> sprites =
                location -> spriteGetter.apply(new Material(TextureAtlas.LOCATION_BLOCKS, location));
        TextureAtlasSprite[] cable = new TextureAtlasSprite[16];
        TextureAtlasSprite[] foam = new TextureAtlasSprite[16];
        for (int i = 0; i < 16; i++) {
            cable[i] = sprites.apply(cableTexture(type, i));
            foam[i] = sprites.apply(foamTexture(i));
        }
        return new Baked(type.getThickness(), cable, foam, sprites.apply(WET_FOAM_TEXTURE));
    }

    private static final class Baked implements IDynamicBakedModel {
        private static final ChunkRenderTypeSet RENDER_TYPES = ChunkRenderTypeSet.of(RenderType.cutout());

        private final float thickness;
        private final TextureAtlasSprite[] cableSprites;
        private final TextureAtlasSprite[] foamSprites;
        private final TextureAtlasSprite wetFoamSprite;
        private final Map<Integer, List<BakedQuad>> cableQuads = new ConcurrentHashMap<>();
        private final Map<Integer, Map<Direction, List<BakedQuad>>> foamQuads = new ConcurrentHashMap<>();

        Baked(float thickness, TextureAtlasSprite[] cableSprites, TextureAtlasSprite[] foamSprites,
              TextureAtlasSprite wetFoamSprite) {
            this.thickness = thickness;
            this.cableSprites = cableSprites;
            this.foamSprites = foamSprites;
            this.wetFoamSprite = wetFoamSprite;
        }

        @Override
        public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource rand, ModelData data,
                                        RenderType renderType) {
            if (state == null) {
                return List.of();
            }
            CableBlock.Foam foam = state.getValue(CableBlock.FOAM);
            if (foam != CableBlock.Foam.NONE) {
                if (side == null) {
                    return List.of();
                }
                int key = foam == CableBlock.Foam.WET ? -1 : clampColumn(data.get(CableBlockEntity.FOAM_COLOR));
                return foamQuads.computeIfAbsent(key, k -> bakeFullBlock(k < 0 ? wetFoamSprite : foamSprites[k]))
                        .get(side);
            }
            if (side != null) {
                return List.of();
            }
            int mask = 0;
            for (Direction direction : Direction.values()) {
                if (state.getValue(CableBlock.CONNECTIONS.get(direction))) {
                    mask |= 1 << direction.ordinal();
                }
            }
            int column = clampColumn(data.get(CableBlockEntity.TEXTURE_INDEX));
            int key = column << 6 | mask;
            int connections = mask;
            return cableQuads.computeIfAbsent(key, k -> bakeCable(connections, cableSprites[column]));
        }

        private static int clampColumn(Integer value) {
            return value == null ? 0 : Math.floorMod(value, 16);
        }

        private List<BakedQuad> bakeCable(int mask, TextureAtlasSprite sprite) {
            float lo = (1.0F - thickness) / 2.0F * 16.0F;
            float hi = lo + thickness * 16.0F;
            List<BakedQuad> quads = new ArrayList<>();
            for (Direction face : Direction.values()) {
                if ((mask & 1 << face.ordinal()) == 0) {
                    quads.add(quad(new Vector3f(lo, lo, lo), new Vector3f(hi, hi, hi), face, sprite, null));
                }
            }
            for (Direction arm : Direction.values()) {
                if ((mask & 1 << arm.ordinal()) == 0) {
                    continue;
                }
                Vector3f from = new Vector3f(lo, lo, lo);
                Vector3f to = new Vector3f(hi, hi, hi);
                switch (arm) {
                    case WEST -> { from.x = 0; to.x = lo; }
                    case EAST -> { from.x = hi; to.x = 16; }
                    case DOWN -> { from.y = 0; to.y = lo; }
                    case UP -> { from.y = hi; to.y = 16; }
                    case NORTH -> { from.z = 0; to.z = lo; }
                    case SOUTH -> { from.z = hi; to.z = 16; }
                }
                for (Direction face : Direction.values()) {
                    if (face != arm.getOpposite()) {
                        quads.add(quad(from, to, face, sprite, face == arm ? arm : null));
                    }
                }
            }
            return List.copyOf(quads);
        }

        private static Map<Direction, List<BakedQuad>> bakeFullBlock(TextureAtlasSprite sprite) {
            Map<Direction, List<BakedQuad>> quads = new EnumMap<>(Direction.class);
            for (Direction face : Direction.values()) {
                quads.put(face, List.of(quad(new Vector3f(0, 0, 0), new Vector3f(16, 16, 16), face, sprite, face)));
            }
            return quads;
        }

        private static BakedQuad quad(Vector3f from, Vector3f to, Direction face, TextureAtlasSprite sprite,
                                      Direction cull) {
            float[] uv = switch (face) {
                case DOWN -> new float[]{from.x, 16 - to.z, to.x, 16 - from.z};
                case UP -> new float[]{from.x, from.z, to.x, to.z};
                case NORTH -> new float[]{16 - to.x, 16 - to.y, 16 - from.x, 16 - from.y};
                case SOUTH -> new float[]{from.x, 16 - to.y, to.x, 16 - from.y};
                case WEST -> new float[]{from.z, 16 - to.y, to.z, 16 - from.y};
                case EAST -> new float[]{16 - to.z, 16 - to.y, 16 - from.z, 16 - from.y};
            };
            BlockElementFace element = new BlockElementFace(cull, BlockElementFace.NO_TINT, "",
                    new BlockFaceUV(uv, 0));
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
            return false;
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
            return cableSprites[0];
        }

        @Override
        public TextureAtlasSprite getParticleIcon(ModelData data) {
            return cableSprites[clampColumn(data.get(CableBlockEntity.TEXTURE_INDEX))];
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
