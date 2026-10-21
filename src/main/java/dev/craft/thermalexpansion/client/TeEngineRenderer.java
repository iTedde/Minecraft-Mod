package dev.craft.thermalexpansion.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.craft.Craft;
import dev.craft.thermalexpansion.energy.TeEngineBlock;
import dev.craft.thermalexpansion.energy.TeEngineBlockEntity;
import dev.craft.thermalexpansion.energy.TeEngineType;
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
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

/**
 * {@code RenderBlockEngine}: base plate, piston plate moving 0–8 px with the stroke, one extension ring per 2 px of
 * stroke and the trunk (second variant while active), all turned by {@code RenderUtils.angleBaseYNeg} to the facing.
 */
public class TeEngineRenderer implements BlockEntityRenderer<TeEngineBlockEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "te_engine"), "main");
    private static final ResourceLocation[] TEXTURES = {texture("engine_steam"), texture("engine_magmatic")};

    private final ModelPart base;
    private final ModelPart piston;
    private final ModelPart extension;
    private final ModelPart[] trunk = new ModelPart[2];

    public TeEngineRenderer(BlockEntityRendererProvider.Context context) {
        this(context.bakeLayer(LAYER));
    }

    TeEngineRenderer(ModelPart root) {
        base = root.getChild("base");
        piston = root.getChild("piston");
        extension = root.getChild("extension");
        for (int i = 0; i < 2; i++) {
            trunk[i] = root.getChild("trunk_" + i);
        }
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/entity/te/" + name + ".png");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartPose centre = PartPose.offset(8.0F, 8.0F, 8.0F);
        root.addOrReplaceChild("base", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-8.0F, -8.0F, -8.0F, 16, 4, 16), centre);
        root.addOrReplaceChild("piston", CubeListBuilder.create().texOffs(64, 0)
                .addBox(-7.0F, -4.0F, -7.0F, 14, 4, 14), centre);
        root.addOrReplaceChild("extension", CubeListBuilder.create().texOffs(0, 20)
                .addBox(-5.0F, -4.5F, -5.0F, 10, 2, 10), centre);
        for (int i = 0; i < 2; i++) {
            root.addOrReplaceChild("trunk_" + i, CubeListBuilder.create().texOffs(32 * i, 44)
                    .addBox(-4.0F, -4.0F, -4.0F, 8, 12, 8), centre);
        }
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void render(TeEngineBlockEntity engine, float partialTick, PoseStack pose, MultiBufferSource buffers,
                       int light, int overlay) {
        render(engine.getEngineType(), engine.isActive(), engine.getCycleProgress(),
                engine.getBlockState().getValue(TeEngineBlock.FACING), pose, buffers, light, overlay);
    }

    /** {@code render(metadata, active, progress, facing, …)}; the item is drawn facing up at progress 0.25. */
    void render(TeEngineType type, boolean active, float progress, Direction facing, PoseStack pose,
                MultiBufferSource buffers, int light, int overlay) {
        progress = Math.min(progress, 1.0F);
        float step = progress > 0.5F ? 8.0F - (progress - 0.5F) * 2.0F * 8.0F : progress * 16.0F;
        float translateFactor = step / 16.0F;
        ModelPart activeTrunk = trunk[active ? 1 : 0];
        for (ModelPart part : new ModelPart[]{base, piston, extension, activeTrunk}) {
            rotate(part, facing);
        }
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(TEXTURES[type.ordinal()]));
        pose.pushPose();
        base.render(pose, consumer, light, overlay);
        pose.pushPose();
        pose.translate(facing.getStepX() * translateFactor, facing.getStepY() * translateFactor,
                facing.getStepZ() * translateFactor);
        piston.render(pose, consumer, light, overlay);
        pose.popPose();
        activeTrunk.render(pose, consumer, light, overlay);
        float extensionFactor = 0.125F;
        for (int i = 0; i < step + 2.0F; i += 2) {
            extension.render(pose, consumer, light, overlay);
            pose.translate(facing.getStepX() * extensionFactor, facing.getStepY() * extensionFactor,
                    facing.getStepZ() * extensionFactor);
        }
        pose.popPose();
    }

    /** {@code RenderUtils.angleBaseYNeg}. */
    private static void rotate(ModelPart part, Direction facing) {
        float pi = (float) Math.PI;
        part.xRot = switch (facing) {
            case NORTH -> -pi / 2.0F;
            case SOUTH -> pi / 2.0F;
            default -> 0.0F;
        };
        part.yRot = 0.0F;
        part.zRot = switch (facing) {
            case DOWN -> pi;
            case WEST -> pi / 2.0F;
            case EAST -> -pi / 2.0F;
            default -> 0.0F;
        };
    }
}
