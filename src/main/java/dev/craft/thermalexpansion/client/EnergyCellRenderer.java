package dev.craft.thermalexpansion.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.craft.Craft;
import dev.craft.buildcraft.client.FluidBoxRenderer;
import dev.craft.thermalexpansion.energy.EnergyCellBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

/**
 * {@code RenderBlockEnergyCell}: the frame (a 3 px plate of {@code energy_cell.png} on each of the six sides,
 * {@code generateSidedParts}) and the charge core 0.15–0.85 with one of 16 molten-redstone textures
 * ({@code TextureRedstoneFX(level)}, level = charge · 16 / 600 000, at most 15).
 */
public class EnergyCellRenderer implements BlockEntityRenderer<EnergyCellBlockEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "energy_cell"), "main");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/entity/te/energy_cell.png");
    /** Plate rotations: the bottom plate turned to down, up, north, south, west, east. */
    private static final float[][] SIDES = {{0, 0}, {(float) Math.PI, 0}, {(float) Math.PI / 2, 0},
            {(float) -Math.PI / 2, 0}, {0, (float) -Math.PI / 2}, {0, (float) Math.PI / 2}};

    private final ModelPart[] plates = new ModelPart[6];

    public EnergyCellRenderer(BlockEntityRendererProvider.Context context) {
        this(context.bakeLayer(LAYER));
    }

    EnergyCellRenderer(ModelPart root) {
        for (int i = 0; i < 6; i++) {
            plates[i] = root.getChild("plate_" + i);
        }
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        for (int i = 0; i < 6; i++) {
            root.addOrReplaceChild("plate_" + i, CubeListBuilder.create().texOffs(0, 0)
                            .addBox(-8.0F, -8.0F, -8.0F, 16, 3, 16),
                    PartPose.offsetAndRotation(8.0F, 8.0F, 8.0F, SIDES[i][0], 0.0F, SIDES[i][1]));
        }
        return LayerDefinition.create(mesh, 128, 64);
    }

    /** {@code getScaledEnergyStored(16)}, capped at 15. */
    static int chargeLevel(float energy) {
        return Math.min(15, (int) (energy * 16 / EnergyCellBlockEntity.STORAGE));
    }

    @Override
    public void render(EnergyCellBlockEntity cell, float partialTick, PoseStack pose, MultiBufferSource buffers,
                       int light, int overlay) {
        render(chargeLevel(cell.getEnergy()), pose, buffers, light, overlay);
    }

    void render(int chargeLevel, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(TEXTURE));
        for (ModelPart plate : plates) {
            plate.render(pose, consumer, light, overlay);
        }
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(
                ResourceLocation.fromNamespaceAndPath(Craft.MODID, "block/te/energy_cell/energy_cell_charge_"
                        + chargeLevel));
        FluidBoxRenderer.render(pose, buffers, sprite, 0xFFFFFFFF, 0.15F, 0.15F, 0.15F, 0.85F, 0.85F, 0.85F, light);
    }
}
