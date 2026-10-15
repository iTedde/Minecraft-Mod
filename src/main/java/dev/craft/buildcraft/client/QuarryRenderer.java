package dev.craft.buildcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.craft.Craft;
import dev.craft.buildcraft.area.Box;
import dev.craft.buildcraft.factory.QuarryBlockEntity;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

/**
 * Quarry visuals: stripe lasers while the frame is built, the construction robot with its energy laser
 * ({@code RenderRobot}, {@code RenderEnergyLaser}) and the drill arm ({@code EntityMechanicalArm}).
 */
public class QuarryRenderer implements BlockEntityRenderer<QuarryBlockEntity> {
    private static final ResourceLocation ROBOT = texture("robot");
    private static final ResourceLocation[] ENERGY_LASER = {texture("energy_laser_1"), texture("energy_laser_2"),
            texture("energy_laser_3"), texture("energy_laser_4")};
    private final ModelPart robot;
    private final ModelPart[] laserBoxes = new ModelPart[40];

    public QuarryRenderer(BlockEntityRendererProvider.Context context) {
        robot = single(CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -4.0F, -4.0F, 8, 8, 8));
        for (int i = 0; i < laserBoxes.length; i++) {
            laserBoxes[i] = single(CubeListBuilder.create().texOffs(laserBoxes.length - i, 0)
                    .addBox(0.0F, -0.5F, -0.5F, 16, 1, 1));
        }
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/entity/quarry/" + name + ".png");
    }

    private static ModelPart single(CubeListBuilder cubes) {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("box", cubes, PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32).bakeRoot().getChild("box");
    }

    @Override
    public void render(QuarryBlockEntity quarry, float partialTick, PoseStack pose, MultiBufferSource buffers,
                       int packedLight, int overlay) {
        Level level = quarry.getLevel();
        BlockPos origin = quarry.getBlockPos();
        Box box = quarry.getBox();
        if (quarry.showsStripes()) {
            BlockBoxRenderer.laserBox(pose, buffers, level, origin, BlockBoxRenderer.LASER_STRIPES,
                    box.xMin, box.yMin, box.zMin, box.xMax, box.yMax, box.zMax);
        }
        if (quarry.hasArm()) {
            renderArm(quarry, pose, buffers, level, origin, box);
        }
        if (quarry.hasRobot()) {
            renderRobot(quarry, partialTick, pose, buffers, level, origin);
        }
    }

    private void renderArm(QuarryBlockEntity quarry, PoseStack pose, MultiBufferSource buffers, Level level,
                           BlockPos origin, Box box) {
        double xRoot = box.xMin + 0.75F;
        double yRoot = origin.getY() + box.sizeY() - 1 + 0.25F;
        double zRoot = box.zMin + 0.75F;
        double armSizeX = box.sizeX() - 2 + 0.5F;
        double armSizeZ = box.sizeZ() - 2 + 0.5F;
        double hx = quarry.armHead(0);
        double hy = quarry.armHead(1);
        double hz = quarry.armHead(2);
        BlockBoxRenderer.box(pose, buffers, level, origin, BlockBoxRenderer.DRILL, xRoot, yRoot, hz + 0.25,
                armSizeX, 0.5, 0.5);
        BlockBoxRenderer.box(pose, buffers, level, origin, BlockBoxRenderer.DRILL, hx + 0.25, hy + 1.0, hz + 0.25,
                0.5, yRoot - hy - 1.0, 0.5);
        BlockBoxRenderer.box(pose, buffers, level, origin, BlockBoxRenderer.DRILL, hx + 0.25, yRoot, zRoot,
                0.5, 0.5, armSizeZ);
        BlockBoxRenderer.box(pose, buffers, level, origin, BlockBoxRenderer.DRILL_HEAD, hx + 0.4, hy, hz + 0.4,
                0.2, 1.0, 0.2);
    }

    private void renderRobot(QuarryBlockEntity quarry, float partialTick, PoseStack pose, MultiBufferSource buffers,
                             Level level, BlockPos origin) {
        QuarryBlockEntity.RobotView view = quarry.getRobotView();
        double rx = view.x(partialTick);
        double ry = view.y(partialTick);
        double rz = view.z(partialTick);
        int light = LevelRenderer.getLightColor(level, BlockPos.containing(rx, ry, rz));
        pose.pushPose();
        pose.translate(rx - origin.getX(), ry - origin.getY(), rz - origin.getZ());
        robot.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(ROBOT)), light,
                OverlayTexture.NO_OVERLAY);

        BlockPos target = quarry.getRobotTarget();
        if (target != null) {
            // EntityLaser: head = robot, tail = centre of the target block
            double dx = rx - (target.getX() + 0.5);
            double dy = ry - (target.getY() + 0.5);
            double dz = rz - (target.getZ() + 0.5);
            double size = Math.sqrt(dx * dx + dy * dy + dz * dz);
            double angleZ = 360.0 - (Math.atan2(dz, dx) * 180.0 / Math.PI + 180.0);
            double horizontal = Math.sqrt(size * size - dy * dy);
            double angleY = -Math.atan2(dy, horizontal) * 180.0 / Math.PI;
            float average = view.getPowerAverage();
            int textureIndex = average <= 1.0F ? 0 : average <= 2.0F ? 1 : average <= 3.0F ? 2 : 3;
            ModelPart part = laserBoxes[view.nextDisplayStage() / 10];
            var consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(ENERGY_LASER[textureIndex]));
            pose.mulPose(Axis.YP.rotationDegrees((float) angleZ));
            pose.mulPose(Axis.ZP.rotationDegrees((float) angleY));
            int whole = 0;
            for (float i = 0.0F; i <= size - 1.0; i++) {
                part.render(pose, consumer, light, OverlayTexture.NO_OVERLAY);
                pose.translate(1.0, 0.0, 0.0);
                whole++;
            }
            pose.scale((float) size - whole, 1.0F, 1.0F);
            part.render(pose, consumer, light, OverlayTexture.NO_OVERLAY);
        }
        pose.popPose();
    }

    @Override
    public boolean shouldRenderOffScreen(QuarryBlockEntity quarry) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public AABB getRenderBoundingBox(QuarryBlockEntity quarry) {
        return AABB.INFINITE;
    }
}
