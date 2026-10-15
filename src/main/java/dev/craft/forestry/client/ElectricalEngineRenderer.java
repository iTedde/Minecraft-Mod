package dev.craft.forestry.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.craft.Craft;
import dev.craft.forestry.engine.ElectricalEngineBlockEntity;
import dev.craft.forestry.engine.TemperatureState;
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
 * Forestry {@code RenderEngine} with the {@code engine_tin_} textures: boiler, piston, extension rings and the trunk
 * coloured by temperature, modelled for an output pointing up and rotated about the block centre.
 */
public class ElectricalEngineRenderer implements BlockEntityRenderer<ElectricalEngineBlockEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "electrical_engine"), "main");

    private static final ResourceLocation BASE = texture("engine_tin_base");
    private static final ResourceLocation PISTON = texture("engine_tin_piston");
    private static final ResourceLocation EXTENSION = texture("engine_tin_extension");

    private final ModelPart boiler;
    private final ModelPart trunk;
    private final ModelPart piston;
    private final ModelPart extension;

    public ElectricalEngineRenderer(BlockEntityRendererProvider.Context context) {
        this(context.bakeLayer(LAYER));
    }

    ElectricalEngineRenderer(ModelPart root) {
        this.boiler = root.getChild("boiler");
        this.trunk = root.getChild("trunk");
        this.piston = root.getChild("piston");
        this.extension = root.getChild("extension");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartPose centre = PartPose.offset(8.0F, 8.0F, 8.0F);
        root.addOrReplaceChild("boiler", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-8.0F, -8.0F, -8.0F, 16, 6, 16), centre);
        root.addOrReplaceChild("trunk", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4.0F, -4.0F, -4.0F, 8, 12, 8), centre);
        root.addOrReplaceChild("piston", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-6.0F, -2.0F, -6.0F, 12, 4, 12), centre);
        root.addOrReplaceChild("extension", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-5.0F, -3.0F, -5.0F, 10, 2, 10), centre);
        return LayerDefinition.create(mesh, 64, 32);
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/entity/engine/" + name + ".png");
    }

    /** Trunk texture by temperature; {@code MELTING} falls into the default branch (low). */
    private static ResourceLocation trunkTexture(TemperatureState state) {
        return texture(switch (state) {
            case OVERHEATING -> "engine_trunk_highest";
            case RUNNING_HOT -> "engine_trunk_higher";
            case OPERATING_TEMPERATURE -> "engine_trunk_high";
            case WARMED_UP -> "engine_trunk_medium";
            default -> "engine_trunk_low";
        });
    }

    @Override
    public void render(ElectricalEngineBlockEntity engine, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int light, int overlay) {
        render(engine.getTemperatureState(), engine.getProgress(), engine.getOrientation(), pose, buffers, light,
                overlay);
    }

    public void render(TemperatureState state, float progress, Direction orientation, PoseStack pose,
                       MultiBufferSource buffers, int light, int overlay) {
        float step = progress > 0.5F ? 5.99F - (progress - 0.5F) * 2.0F * 5.99F : progress * 2.0F * 5.99F;
        float angleX = 0.0F;
        float angleZ = 0.0F;
        switch (orientation) {
            case EAST -> angleZ = (float) (-Math.PI / 2);
            case WEST -> angleZ = (float) (Math.PI / 2);
            case DOWN -> angleZ = (float) Math.PI;
            case SOUTH -> angleX = (float) (Math.PI / 2);
            case NORTH -> angleX = (float) (-Math.PI / 2);
            default -> {
            }
        }
        for (ModelPart part : new ModelPart[]{boiler, trunk, piston, extension}) {
            part.xRot = angleX;
            part.zRot = angleZ;
        }
        float dx = orientation.getStepX();
        float dy = orientation.getStepY();
        float dz = orientation.getStepZ();

        boiler.render(pose, buffers.getBuffer(RenderType.entityCutout(BASE)), light, overlay);
        float shift = step / 16.0F;
        pose.pushPose();
        pose.translate(dx * shift, dy * shift, dz * shift);
        piston.render(pose, buffers.getBuffer(RenderType.entityCutout(PISTON)), light, overlay);
        pose.popPose();

        var extensionBuffer = buffers.getBuffer(RenderType.entityCutout(EXTENSION));
        pose.pushPose();
        for (int i = 0; i <= step + 2.0F; i += 2) {
            extension.render(pose, extensionBuffer, light, overlay);
            pose.translate(dx * 0.125F, dy * 0.125F, dz * 0.125F);
        }
        pose.popPose();

        trunk.render(pose, buffers.getBuffer(RenderType.entityCutout(trunkTexture(state))), light, overlay);
    }
}
