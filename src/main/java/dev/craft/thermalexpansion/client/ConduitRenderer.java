package dev.craft.thermalexpansion.client;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.craft.Craft;
import dev.craft.buildcraft.client.FluidBoxRenderer;
import dev.craft.thermalexpansion.transport.ConduitBlockEntity;
import dev.craft.thermalexpansion.transport.EnergyConduitBlockEntity;
import dev.craft.thermalexpansion.transport.LiquiductBlockEntity;
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
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * {@code RenderBlockConduit}: the 6 px core; per side an arm to the next conduit, or a stub plus a plate (output or
 * input) to a tank or receiver; the liquid inside in six levels (the energy conduit is always full of molten
 * redstone, {@code TextureRedstoneFX(8)}).
 */
public class ConduitRenderer implements BlockEntityRenderer<ConduitBlockEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "te_conduit"), "main");
    private static final ResourceLocation[] TEXTURES = {texture("conduit_energy"), texture("conduit_liquid")};
    private static final ResourceLocation REDSTONE = ResourceLocation.fromNamespaceAndPath(Craft.MODID,
            "block/te/energy_cell/energy_cell_charge_8");
    /** {@code side_models}: 0 item arm, 1 conduit arm, 2 stub, 3 output plate, 4 input plate. */
    private static final int ARM_ITEM = 0;
    private static final int ARM = 1;
    private static final int STUB = 2;
    private static final int PLATE = 3;
    private static final byte[] INV_SIDES = {1, 1, 0, 0, 0, 0};
    private static final float C1 = 0.32F;
    private static final float C2 = 0.68F;

    private final ModelPart base;
    private final ModelPart[] parts = new ModelPart[5];

    public ConduitRenderer(BlockEntityRendererProvider.Context context) {
        this(context.bakeLayer(LAYER));
    }

    ConduitRenderer(ModelPart root) {
        base = root.getChild("base");
        for (int i = 0; i < parts.length; i++) {
            parts[i] = root.getChild("part_" + i);
        }
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/entity/te/" + name + ".png");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartPose centre = PartPose.offset(8.0F, 8.0F, 8.0F);
        root.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 16).addBox(-3, -3, -3, 6, 6, 6), centre);
        root.addOrReplaceChild("part_0", CubeListBuilder.create().texOffs(0, 0).addBox(-3, 3, -3, 6, 5, 6), centre);
        root.addOrReplaceChild("part_1", CubeListBuilder.create().texOffs(32, 0).addBox(-3, 3, -3, 6, 5, 6), centre);
        root.addOrReplaceChild("part_2", CubeListBuilder.create().texOffs(0, 0).addBox(-3, 3, -3, 6, 1, 6), centre);
        root.addOrReplaceChild("part_3", CubeListBuilder.create().texOffs(64, 0).addBox(-4, 4, -4, 8, 4, 8), centre);
        root.addOrReplaceChild("part_4", CubeListBuilder.create().texOffs(96, 0).addBox(-4, 4, -4, 8, 4, 8), centre);
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void render(ConduitBlockEntity conduit, float partialTick, PoseStack pose, MultiBufferSource buffers,
                       int light, int overlay) {
        byte[] sides = new byte[6];
        for (Direction direction : Direction.values()) {
            sides[direction.get3DDataValue()] = conduit.getSide(direction);
        }
        boolean energy = conduit instanceof EnergyConduitBlockEntity;
        render(energy, sides, conduit.getMode(), ARM, pose, buffers, light, overlay);
        if (energy) {
            renderRedstone(sides, pose, buffers, light);
        } else if (conduit instanceof LiquiductBlockEntity liquiduct) {
            renderLiquid(liquiduct.getRenderFluid(), sides, liquiduct.getRenderLevel(), pose, buffers, light);
        }
    }

    /** {@code renderInventoryBlock}: arms up and down, the energy conduit filled. */
    void renderItem(boolean energy, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        render(energy, INV_SIDES, 0, ARM_ITEM, pose, buffers, light, overlay);
        if (energy) {
            renderRedstone(INV_SIDES, pose, buffers, light);
        }
    }

    private void render(boolean energy, byte[] sides, int mode, int armVariant, PoseStack pose,
                        MultiBufferSource buffers, int light, int overlay) {
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(TEXTURES[energy ? 0 : 1]));
        for (Direction direction : Direction.values()) {
            byte connection = sides[direction.get3DDataValue()];
            if (connection == ConduitBlockEntity.CONDUIT) {
                renderPart(parts[armVariant], direction, pose, consumer, light, overlay);
            } else if (connection == ConduitBlockEntity.OTHER) {
                renderPart(parts[STUB], direction, pose, consumer, light, overlay);
                renderPart(parts[PLATE + mode], direction, pose, consumer, light, overlay);
            }
        }
        base.render(pose, consumer, light, overlay);
    }

    /** {@code generateSidedModels}: the part built for "up" turned to {@code side}. */
    private static void renderPart(ModelPart part, Direction side, PoseStack pose, VertexConsumer consumer, int light,
                                   int overlay) {
        float pi = (float) Math.PI;
        part.xRot = switch (side) {
            case DOWN -> pi;
            case NORTH -> -pi / 2.0F;
            case SOUTH -> pi / 2.0F;
            default -> 0.0F;
        };
        part.zRot = switch (side) {
            case WEST -> pi / 2.0F;
            case EAST -> -pi / 2.0F;
            default -> 0.0F;
        };
        part.render(pose, consumer, light, overlay);
    }

    private static void renderRedstone(byte[] sides, PoseStack pose, MultiBufferSource buffers, int light) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(REDSTONE);
        for (float[] box : boxes(sides, 6)) {
            FluidBoxRenderer.render(pose, buffers, sprite, 0xFFFFFFFF, box[0], box[1], box[2], box[3], box[4], box[5],
                    light);
        }
    }

    private static void renderLiquid(Fluid fluid, byte[] sides, int level, PoseStack pose, MultiBufferSource buffers,
                                     int light) {
        if (fluid == Fluids.EMPTY || level <= 0) {
            return;
        }
        FluidStack stack = new FluidStack(fluid, 1000);
        for (float[] box : boxes(sides, level)) {
            FluidBoxRenderer.render(pose, buffers, stack, box[0], box[1], box[2], box[3], box[4], box[5], light);
        }
    }

    /** {@code generateLiquidModels}: the liquid boxes of level 1–6 for the connected sides and the core. */
    private static List<float[]> boxes(byte[] sides, int level) {
        float d1 = 0.47F - 0.025F * level;
        float d2 = 0.53F + 0.025F * level;
        float d3 = 0.32F + 0.06F * level;
        float[][] all = {
                {d1, 0.0F, d1, d2, C1, d2},
                {d1, d3, d1, d2, 1.0F, d2},
                {C1, C1, 0.0F, C2, d3, C1},
                {C1, C1, C2, C2, d3, 1.0F},
                {0.0F, C1, C1, C1, d3, C2},
                {C2, C1, C1, 1.0F, d3, C2}};
        List<float[]> boxes = new ArrayList<>();
        for (int s = 0; s < 6; s++) {
            if (sides[s] > 0) {
                boxes.add(all[s]);
            }
        }
        boxes.add(new float[]{C1, C1, C1, C2, d3, C2});
        return boxes;
    }
}
