package dev.craft.powerplants.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.craft.Craft;
import dev.craft.powerplants.windturbine.LargeWindTurbineBlockEntity;
import dev.craft.powerplants.windturbine.WindRotorItem;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.AABB;

/**
 * Rotor of the large wind turbine (spec 23 §4): a hub and four blades (diameter ≈ 5 blocks) in front of the nacelle,
 * textured after the rotor material and turning faster with more output. Purely visual, no collision.
 */
public class WindRotorRenderer implements BlockEntityRenderer<LargeWindTurbineBlockEntity> {
    private static final float MIN_SPEED = 1.0F;
    private static final float MAX_SPEED = 15.0F;

    private final ModelPart rotor = createModel();

    public WindRotorRenderer(BlockEntityRendererProvider.Context context) {
    }

    private static ModelPart createModel() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();
        parts.addOrReplaceChild("hub", CubeListBuilder.create().texOffs(32, 0).addBox(-3, -3, -2, 6, 6, 4),
                PartPose.ZERO);
        for (int blade = 0; blade < 4; blade++) {
            parts.addOrReplaceChild("blade" + blade, CubeListBuilder.create().texOffs(0, 0)
                    .addBox(-3, 3, -0.5F, 6, 33, 1), PartPose.rotation(0, 0, blade * (float) (Math.PI / 2.0)));
        }
        return LayerDefinition.create(mesh, 64, 64).bakeRoot();
    }

    @Override
    public void render(LargeWindTurbineBlockEntity turbine, float partialTick, PoseStack pose,
                       MultiBufferSource buffers, int light, int overlay) {
        if (!(turbine.getRotor().getItem() instanceof WindRotorItem item) || turbine.getLevel() == null) {
            return;
        }
        Direction facing = turbine.getFacing();
        float speed = MIN_SPEED + (float) (turbine.getPower() / LargeWindTurbineBlockEntity.MAX_OUTPUT)
                * (MAX_SPEED - MIN_SPEED);
        float angle = ((turbine.getLevel().getGameTime() % 3600) + partialTick) * speed % 360.0F;
        ResourceLocation texture = ResourceLocation.fromNamespaceAndPath(Craft.MODID,
                "textures/entity/wind_rotor/" + item.getMaterial() + ".png");
        // the nacelle is opaque: take the light in front of it
        int frontLight = LevelRenderer.getLightColor(turbine.getLevel(), turbine.getBlockPos().relative(facing));
        pose.pushPose();
        pose.translate(0.5, 0.5, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));
        pose.translate(0.0, 0.0, 0.65);
        pose.mulPose(Axis.ZP.rotationDegrees(angle));
        rotor.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(texture)), frontLight, overlay);
        pose.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(LargeWindTurbineBlockEntity turbine) {
        return true;
    }

    @Override
    public AABB getRenderBoundingBox(LargeWindTurbineBlockEntity turbine) {
        return new AABB(turbine.getBlockPos()).inflate(3.0);
    }
}
