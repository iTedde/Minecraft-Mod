package dev.craft.thermalexpansion.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.craft.Craft;
import dev.craft.buildcraft.client.FluidBoxRenderer;
import dev.craft.thermalexpansion.transport.TesseractBlockEntity;
import dev.craft.thermalexpansion.transport.TesseractType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

/**
 * {@code RenderBlockTeleport}: the frame of the energy cell (a 3 px plate of {@code tesseract_<type>.png} on each
 * side) around a core 0.15–0.85 of molten ender ({@code TextureEnderFX}).
 */
public class TesseractRenderer implements BlockEntityRenderer<TesseractBlockEntity> {
    private static final ResourceLocation CORE = ResourceLocation.fromNamespaceAndPath(Craft.MODID,
            "block/te/fluid/molten_ender");

    private final ModelPart[] plates = new ModelPart[6];

    public TesseractRenderer(BlockEntityRendererProvider.Context context) {
        this(context.bakeLayer(EnergyCellRenderer.LAYER));
    }

    TesseractRenderer(ModelPart root) {
        for (int i = 0; i < 6; i++) {
            plates[i] = root.getChild("plate_" + i);
        }
    }

    private static ResourceLocation texture(TesseractType type) {
        return ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/entity/te/tesseract_" + type.getName()
                + ".png");
    }

    @Override
    public void render(TesseractBlockEntity tesseract, float partialTick, PoseStack pose, MultiBufferSource buffers,
                       int light, int overlay) {
        render(tesseract.getTesseractType(), pose, buffers, light, overlay);
    }

    void render(TesseractType type, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(texture(type)));
        for (ModelPart plate : plates) {
            plate.render(pose, consumer, light, overlay);
        }
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(CORE);
        FluidBoxRenderer.render(pose, buffers, sprite, 0xFFFFFFFF, 0.15F, 0.15F, 0.15F, 0.85F, 0.85F, 0.85F, light);
    }
}
