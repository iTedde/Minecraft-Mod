package dev.craft.thermalexpansion.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.craft.Craft;
import dev.craft.buildcraft.client.FluidBoxRenderer;
import dev.craft.thermalexpansion.block.PortableTankBlockEntity;
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
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * {@code RenderBlockTank}: lid, four walls and bottom of {@code liquid_tank.png} (walls and bottom in a second variant
 * for the output mode), the liquid as a box 0.1975–0.8025 wide from 0.0725 up to 0.99 in 100 steps.
 */
public class PortableTankRenderer implements BlockEntityRenderer<PortableTankBlockEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "portable_tank"), "main");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/entity/te/liquid_tank.png");
    /** {@code RenderUtils.angleBaseXPos[2..5][1]}: the wall turned to each horizontal side. */
    private static final float[] WALL_ANGLES = {(float) Math.PI, 0.0F, (float) -Math.PI / 2.0F, (float) Math.PI / 2.0F};
    private static final float LIQUID_MIN = 0.0725F;
    private static final float LIQUID_MAX = 0.99F;

    private final ModelPart top;
    private final ModelPart[] sides = new ModelPart[2];
    private final ModelPart[] bottom = new ModelPart[2];

    public PortableTankRenderer(BlockEntityRendererProvider.Context context) {
        this(context.bakeLayer(LAYER));
    }

    PortableTankRenderer(ModelPart root) {
        top = root.getChild("top");
        for (int i = 0; i < 2; i++) {
            sides[i] = root.getChild("sides_" + i);
            bottom[i] = root.getChild("bottom_" + i);
        }
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartPose centre = PartPose.offset(8.0F, 8.0F, 8.0F);
        root.addOrReplaceChild("top", CubeListBuilder.create().texOffs(36, 0)
                .addBox(-6.0F, 7.0F, -6.0F, 12, 1, 12), centre);
        for (int i = 0; i < 2; i++) {
            root.addOrReplaceChild("sides_" + i, CubeListBuilder.create().texOffs(0, 31 + 16 * i)
                    .addBox(-6.0F, -8.0F, -6.0F, 12, 16, 1), centre);
            root.addOrReplaceChild("bottom_" + i, CubeListBuilder.create().texOffs(0, 13 * i)
                    .addBox(-6.0F, -8.0F, -6.0F, 12, 1, 12), centre);
        }
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void render(PortableTankBlockEntity tank, float partialTick, PoseStack pose, MultiBufferSource buffers,
                       int light, int overlay) {
        render(tank.getMode(), tank.getTank().getFluid(), pose, buffers, light, overlay);
    }

    /** {@code render} + {@code renderLiquid}; the item shows the normal mode. */
    void render(boolean mode, FluidStack liquid, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(TEXTURE));
        int variant = mode ? 1 : 0;
        top.render(pose, consumer, light, overlay);
        for (float angle : WALL_ANGLES) {
            sides[variant].yRot = angle;
            sides[variant].render(pose, consumer, light, overlay);
        }
        bottom[variant].render(pose, consumer, light, overlay);
        if (!liquid.isEmpty()) {
            float level = FluidBoxRenderer.level(liquid.getAmount(), PortableTankBlockEntity.MAX_LIQUID);
            FluidBoxRenderer.render(pose, buffers, liquid, 0.1975F, LIQUID_MIN, 0.1975F, 0.8025F,
                    LIQUID_MIN + level * (LIQUID_MAX - LIQUID_MIN), 0.8025F, light);
        }
    }
}
