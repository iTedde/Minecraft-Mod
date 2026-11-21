package ivorius.psychedelicraft.client.render;

import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.neoforge.client.event.ModelEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.render.FluidBoxRenderer.FluidAppearance;
import ivorius.psychedelicraft.item.component.FluidCapacity;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.CommonColors;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.StainedGlassPaneBlock;
import net.minecraft.world.level.block.TransparentBlock;

/**
 * Extra item models for drinks placed on blocks. Upstream used a Fabric model loading plugin; here the config is read
 * and the models are requested in {@link ModelEvent.RegisterAdditional}.
 */
public class PlacedDrinksModelProvider {
    private static final ResourceLocation CONFIG_LOCATION = Psychedelicraft.id("placeable_drinks.json");
    private static final Gson GSON = new Gson();
    private static final RandomSource RNG = RandomSource.create();
    private static final long SEED = 42L;

    public static final PlacedDrinksModelProvider INSTANCE = new PlacedDrinksModelProvider();

    private static final Codec<Map<String, Map<ResourceLocation, Entry>>> CODEC = Codec.unboundedMap(Codec.STRING, Entry.MAP_CODEC);

    private Map<String, Map<ResourceLocation, Entry>> entries = Map.of();

    private Map<String, Map<ResourceLocation, Entry>> load(ResourceManager resourceManager) {
        return resourceManager.getResource(CONFIG_LOCATION).map(resource -> {
            try (BufferedReader reader = resource.openAsReader()) {
                return CODEC.decode(JsonOps.INSTANCE, GsonHelper.fromJson(GSON, reader, JsonElement.class)).getOrThrow().getFirst();
            } catch (IOException e) {
                Psychedelicraft.LOGGER.error("Could not load client drinks file", e);
            }
            return null;
        }).orElseGet(Map::of);
    }

    public void registerModels(ModelEvent.RegisterAdditional event) {
        Map<String, Map<ResourceLocation, Entry>> data = load(Minecraft.getInstance().getResourceManager());
        entries = data;
        data.forEach((type, entries) -> {
            entries.forEach((id, entry) -> {
                event.register(ModelResourceLocation.standalone(getGroundModelId(type, id)));
                if (entry.showFluid()) {
                    event.register(ModelResourceLocation.standalone(getGroundModelFluidId(type, id)));
                }
            });
        });
    }

    public Optional<Entry> get(String type, Item item) {
        return Optional.ofNullable(entries.getOrDefault(type, Map.of()).get(BuiltInRegistries.ITEM.getKey(item)));
    }

    public void renderDrink(String type, ItemStack stack, PoseStack matrices, MultiBufferSource vertices, int light, int overlay) {
        int dyeColor = DyedItemColor.getOrDefault(stack, CommonColors.WHITE);

        renderDrinkModel(stack, matrices, vertices, light, overlay, dyeColor, getGroundModelId(type, stack.getItem()));

        float fillPercentage = FluidCapacity.getPercentage(stack);
        Entry entry = get(type, stack.getItem()).orElse(Entry.DEFAULT);
        if (fillPercentage > 0.01 && entry.showFluid()) {
            float origin = entry.fluidOrigin() / 16F;
            matrices.translate(0, origin, 0);
            matrices.scale(1, fillPercentage, 1);
            matrices.translate(0, -origin, 0);
            int color = FluidAppearance.getItemColor(ItemFluids.of(stack));
            renderDrinkModel(stack, matrices, vertices, light, overlay, color, getGroundModelFluidId(type, stack.getItem()));
        }
    }

    public void renderDrinkModel(ItemStack stack, PoseStack matrices, MultiBufferSource vertices, int light, int overlay, int color, ResourceLocation modelId) {
        ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();

        BakedModel model = renderer.getItemModelShaper().getModelManager().getModel(ModelResourceLocation.standalone(modelId));

        if (model == null) {
            Psychedelicraft.LOGGER.error("Model does not exist: " + modelId);
            return;
        }

        boolean solid = !(stack.getItem() instanceof BlockItem bi) || !(bi.getBlock() instanceof TransparentBlock) && !(bi.getBlock() instanceof StainedGlassPaneBlock);
        RenderType renderLayer = ItemBlockRenderTypes.getRenderType(stack, solid);

        renderBakedItemModel(model, matrices, vertices.getBuffer(renderLayer), light, overlay, color);
    }

    private void renderBakedItemModel(BakedModel model, PoseStack matrices, VertexConsumer vertices, int light, int overlay, int color) {
        for (Direction direction : Direction.values()) {
            RNG.setSeed(SEED);
            renderBakedItemQuads(matrices, vertices, model.getQuads(null, direction, RNG), light, overlay, color);
        }
        RNG.setSeed(SEED);
        renderBakedItemQuads(matrices, vertices, model.getQuads(null, null, RNG), light, overlay, color);
    }

    private void renderBakedItemQuads(PoseStack matrices, VertexConsumer vertices, List<BakedQuad> quads, int light, int overlay, int color) {
        PoseStack.Pose entry = matrices.last();
        for (BakedQuad bakedQuad : quads) {
            vertices.putBulkData(entry, bakedQuad, MathUtils.r(color), MathUtils.g(color), MathUtils.b(color), MathUtils.a(color), light, overlay);
        }
    }

    public static ResourceLocation getGroundModelId(String type, Item item) {
        return getGroundModelId(type, BuiltInRegistries.ITEM.getKey(item));
    }

    public static ResourceLocation getGroundModelFluidId(String type, Item item) {
        return getGroundModelFluidId(type, BuiltInRegistries.ITEM.getKey(item));
    }

    public static ResourceLocation getGroundModelId(String type, ResourceLocation item) {
        return item.withPath(p -> "item/" + p + "_on_" + type);
    }

    public static ResourceLocation getGroundModelFluidId(String type, ResourceLocation item) {
        return getGroundModelId(type, item).withSuffix("_fluid");
    }

    public record Entry(float height, float fluidOrigin, boolean showFluid) {
        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.FLOAT.fieldOf("height").forGetter(Entry::height),
                Codec.FLOAT.fieldOf("fluid_origin").forGetter(Entry::fluidOrigin),
                Codec.BOOL.optionalFieldOf("show_fluid", false).forGetter(Entry::showFluid)
        ).apply(instance, Entry::new));
        public static final Codec<Map<ResourceLocation, Entry>> MAP_CODEC = Codec.unboundedMap(ResourceLocation.CODEC, CODEC);
        public static final Entry DEFAULT = new Entry(0.5F, 0F, false);
    }
}
