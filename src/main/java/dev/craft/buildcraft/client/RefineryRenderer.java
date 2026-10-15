package dev.craft.buildcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.craft.Craft;
import dev.craft.buildcraft.factory.RefineryBlock;
import dev.craft.buildcraft.factory.RefineryBlockEntity;
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
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

/**
 * BuildCraft {@code RenderRefinery}: three tanks, two magnets moving up and down with the animation stage (the
 * magnet texture shows the speed) and the three liquids.
 */
public class RefineryRenderer implements BlockEntityRenderer<RefineryBlockEntity> {
    public static final ModelLayerLocation LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "refinery"), "main");
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/entity/refinery.png");

    private final ModelPart tank;
    private final ModelPart[] magnet = new ModelPart[4];

    public RefineryRenderer(BlockEntityRendererProvider.Context context) {
        this(context.bakeLayer(LAYER));
    }

    RefineryRenderer(ModelPart root) {
        this.tank = root.getChild("tank");
        for (int i = 0; i < 4; i++) {
            magnet[i] = root.getChild("magnet_" + i);
        }
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartPose centre = PartPose.offset(8.0F, 8.0F, 8.0F);
        root.addOrReplaceChild("tank", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-4.0F, -8.0F, -4.0F, 8, 16, 8), centre);
        for (int i = 0; i < 4; i++) {
            root.addOrReplaceChild("magnet_" + i, CubeListBuilder.create().texOffs(32, i * 8)
                    .addBox(0.0F, -8.0F, -8.0F, 8, 4, 4), centre);
        }
        return LayerDefinition.create(mesh, 64, 32);
    }

    @Override
    public void render(RefineryBlockEntity tile, float partialTick, PoseStack pose, MultiBufferSource buffers,
                       int light, int overlay) {
        render(tile, pose, buffers, light, overlay);
    }

    /** {@code render}; {@code tile == null} is the inventory view (no liquids, stage 0, slowest magnet). */
    void render(@Nullable RefineryBlockEntity tile, PoseStack pose, MultiBufferSource buffers, int light,
                int overlay) {
        float anim = 0.0F;
        int angle = 0;
        ModelPart theMagnet = magnet[0];
        if (tile != null) {
            anim = tile.getAnimationStage();
            BlockState state = tile.getBlockState();
            if (state.hasProperty(RefineryBlock.FACING)) {
                angle = switch (state.getValue(RefineryBlock.FACING)) {
                    case NORTH -> 90;
                    case SOUTH -> 270;
                    case WEST -> 180;
                    default -> 0;
                };
            }
            float speed = tile.getAnimationSpeed();
            theMagnet = speed <= 1.0F ? magnet[0] : speed <= 2.5 ? magnet[1] : speed <= 4.5 ? magnet[2] : magnet[3];
        }
        pose.pushPose();
        pose.translate(0.5F, 0.5F, 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(angle));
        pose.translate(-0.5F, -0.5F, -0.5F);
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutout(TEXTURE));
        renderAt(pose, tank, consumer, light, overlay, -0.25F, 0.0F, -0.25F);
        renderAt(pose, tank, consumer, light, overlay, -0.25F, 0.0F, 0.25F);
        renderAt(pose, tank, consumer, light, overlay, 0.25F, 0.0F, 0.0F);
        float trans1;
        float trans2;
        if (anim <= 100.0F) {
            trans1 = 0.75F * anim / 100.0F;
            trans2 = 0.0F;
        } else if (anim <= 200.0F) {
            trans1 = 0.75F - 0.75F * (anim - 100.0F) / 100.0F;
            trans2 = 0.75F * (anim - 100.0F) / 100.0F;
        } else {
            trans1 = 0.75F * (anim - 200.0F) / 100.0F;
            trans2 = 0.75F - 0.75F * (anim - 200.0F) / 100.0F;
        }
        renderAt(pose, theMagnet, consumer, light, overlay, 0.0F, trans1, 0.0F);
        renderAt(pose, theMagnet, consumer, light, overlay, 0.0F, trans2, 0.75F);
        if (tile != null) {
            liquid(pose, buffers, tile.getIngredient1().getFluid(), 0.01F, 0.01F, 0.49F, 0.49F, light);
            liquid(pose, buffers, tile.getIngredient2().getFluid(), 0.01F, 0.51F, 0.49F, 0.99F, light);
            liquid(pose, buffers, tile.getResult().getFluid(), 0.51F, 0.26F, 0.99F, 0.74F, light);
        }
        pose.popPose();
    }

    private static void renderAt(PoseStack pose, ModelPart part, VertexConsumer consumer, int light, int overlay,
                                 float x, float y, float z) {
        pose.pushPose();
        pose.translate(x, y, z);
        part.render(pose, consumer, light, overlay);
        pose.popPose();
    }

    private static void liquid(PoseStack pose, MultiBufferSource buffers, FluidStack liquid, float x0, float z0,
                               float x1, float z1, int light) {
        if (liquid.isEmpty()) {
            return;
        }
        float height = FluidBoxRenderer.level(liquid.getAmount(), RefineryBlockEntity.LIQUID_PER_SLOT);
        FluidBoxRenderer.render(pose, buffers, liquid, x0, 0.0F, z0, x1, height, z1, light);
    }
}
