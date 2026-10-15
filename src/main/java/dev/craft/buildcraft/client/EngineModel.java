package dev.craft.buildcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.craft.Craft;
import dev.craft.buildcraft.engine.EnergyStage;
import dev.craft.buildcraft.engine.EngineType;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

/**
 * Engine model of BuildCraft {@code RenderEngine}: base plate, moving plate, chamber rings and the coloured trunk,
 * all modelled for an output pointing up and rotated about the block centre.
 */
public final class EngineModel {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "engine"), "main");

    private static final ResourceLocation CHAMBER = texture("chamber");
    private static final ResourceLocation[] TRUNKS = {texture("trunk_blue"), texture("trunk_green"),
            texture("trunk_yellow"), texture("trunk_red")};

    private final ModelPart box;
    private final ModelPart trunk;
    private final ModelPart movingBox;
    private final ModelPart chamber;

    public EngineModel(ModelPart root) {
        this.box = root.getChild("box");
        this.trunk = root.getChild("trunk");
        this.movingBox = root.getChild("moving_box");
        this.chamber = root.getChild("chamber");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartPose centre = PartPose.offset(8.0F, 8.0F, 8.0F);
        root.addOrReplaceChild("box", CubeListBuilder.create().texOffs(0, 1)
                .addBox(-8.0F, -8.0F, -8.0F, 16, 4, 16), centre);
        root.addOrReplaceChild("trunk", CubeListBuilder.create().texOffs(1, 1)
                .addBox(-4.0F, -4.0F, -4.0F, 8, 12, 8), centre);
        root.addOrReplaceChild("moving_box", CubeListBuilder.create().texOffs(0, 1)
                .addBox(-8.0F, -4.0F, -8.0F, 16, 4, 16), centre);
        root.addOrReplaceChild("chamber", CubeListBuilder.create().texOffs(1, 1)
                .addBox(-5.0F, -4.0F, -5.0F, 10, 2, 10), centre);
        return LayerDefinition.create(mesh, 64, 32);
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/entity/engine/" + name + ".png");
    }

    public static ResourceLocation baseTexture(EngineType type) {
        return texture("base_" + type.getMaterial());
    }

    public void render(EngineType type, EnergyStage stage, float progress, Direction orientation, PoseStack pose,
                       MultiBufferSource buffers, int light, int overlay) {
        float step = progress > 0.5F ? 7.99F - (progress - 0.5F) * 2.0F * 7.99F : progress * 2.0F * 7.99F;
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
        for (ModelPart part : new ModelPart[]{box, trunk, movingBox, chamber}) {
            part.xRot = angleX;
            part.zRot = angleZ;
        }
        float dx = orientation.getStepX();
        float dy = orientation.getStepY();
        float dz = orientation.getStepZ();

        var base = buffers.getBuffer(RenderType.entityCutout(baseTexture(type)));
        box.render(pose, base, light, overlay);
        float shift = step / 16.0F;
        pose.pushPose();
        pose.translate(dx * shift, dy * shift, dz * shift);
        movingBox.render(pose, base, light, overlay);
        pose.popPose();

        var chamberBuffer = buffers.getBuffer(RenderType.entityCutout(CHAMBER));
        pose.pushPose();
        for (int i = 0; i <= step + 2.0F; i += 2) {
            chamber.render(pose, chamberBuffer, light, overlay);
            pose.translate(dx * 0.125F, dy * 0.125F, dz * 0.125F);
        }
        pose.popPose();

        int trunkIndex = Math.min(stage.ordinal(), TRUNKS.length - 1);
        trunk.render(pose, buffers.getBuffer(RenderType.entityCutout(TRUNKS[trunkIndex])), light, overlay);
    }
}
