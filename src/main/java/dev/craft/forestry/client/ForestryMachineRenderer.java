package dev.craft.forestry.client;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.craft.Craft;
import dev.craft.forestry.machine.CarpenterBlockEntity;
import dev.craft.forestry.machine.ForestryMachineBlock;
import dev.craft.forestry.machine.ForestryMachineBlockEntity;
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
 * Forestry {@code RenderMachine} (carpenter: two base plates and two tanks, the resource tank texture shows the fill
 * level) and {@code RenderBlock} (fabricator: one textured cube), rotated by the machine orientation.
 */
public class ForestryMachineRenderer<T extends ForestryMachineBlockEntity> implements BlockEntityRenderer<T> {
    public static final ModelLayerLocation CARPENTER_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "carpenter"), "main");
    public static final ModelLayerLocation FABRICATOR_LAYER =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "thermionic_fabricator"), "main");

    private static final String[] TANK_LEVELS = {"empty", "low", "medium", "high", "maximum"};

    private final Kind kind;
    private final ModelPart root;

    public ForestryMachineRenderer(BlockEntityRendererProvider.Context context, Kind kind) {
        this(context.bakeLayer(kind == Kind.CARPENTER ? CARPENTER_LAYER : FABRICATOR_LAYER), kind);
    }

    ForestryMachineRenderer(ModelPart root, Kind kind) {
        this.root = root;
        this.kind = kind;
    }

    public static LayerDefinition createCarpenterLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartPose centre = PartPose.offset(8.0F, 8.0F, 8.0F);
        root.addOrReplaceChild("base_front", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-8.0F, -8.0F, -8.0F, 16, 4, 16), centre);
        root.addOrReplaceChild("base_back", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-8.0F, 4.0F, -8.0F, 16, 4, 16), centre);
        root.addOrReplaceChild("resource_tank", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-6.0F, -8.0F, -6.0F, 12, 16, 6), centre);
        root.addOrReplaceChild("product_tank", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-6.0F, -8.0F, 0.0F, 12, 16, 6), centre);
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static LayerDefinition createFabricatorLayer() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("block", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-8.0F, -8.0F, -8.0F, 16, 16, 16), PartPose.offset(8.0F, 8.0F, 8.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/entity/forestry/" + name + ".png");
    }

    @Override
    public void render(T machine, float partialTick, PoseStack pose, MultiBufferSource buffers, int light,
                       int overlay) {
        Direction orientation = machine.getBlockState().getValue(ForestryMachineBlock.FACING);
        int tankLevel = machine instanceof CarpenterBlockEntity carpenter ? carpenter.getTankLevel() : 0;
        render(orientation, tankLevel, pose, buffers, light, overlay);
    }

    /** {@code render(orientation)}: the same rotation angles for every model part. */
    public void render(Direction orientation, int tankLevel, PoseStack pose, MultiBufferSource buffers, int light,
                       int overlay) {
        float angleX = 0.0F;
        float angleY = 0.0F;
        float angleZ = 0.0F;
        switch (orientation) {
            case EAST -> {
                angleY = (float) Math.PI;
                angleZ = (float) (-Math.PI / 2);
            }
            case WEST -> angleZ = (float) (Math.PI / 2);
            case UP -> {
            }
            case DOWN -> angleZ = (float) Math.PI;
            case SOUTH -> {
                angleX = (float) (Math.PI / 2);
                angleZ = (float) (Math.PI / 2);
            }
            default -> {
                angleX = (float) (-Math.PI / 2);
                angleZ = (float) (Math.PI / 2);
            }
        }
        for (ModelPart part : root.getAllParts().toList()) {
            if (part != root) {
                part.xRot = angleX;
                part.yRot = angleY;
                part.zRot = angleZ;
            }
        }
        if (kind == Kind.FABRICATOR) {
            root.getChild("block").render(pose, buffers.getBuffer(RenderType.entityCutout(
                    texture("fabricator_block"))), light, overlay);
            return;
        }
        var base = buffers.getBuffer(RenderType.entityCutout(texture("carpenter_base")));
        root.getChild("base_front").render(pose, base, light, overlay);
        root.getChild("base_back").render(pose, base, light, overlay);
        root.getChild("resource_tank").render(pose, buffers.getBuffer(RenderType.entityCutout(
                texture("carpenter_tank_resource_" + TANK_LEVELS[tankLevel]))), light, overlay);
        root.getChild("product_tank").render(pose, buffers.getBuffer(RenderType.entityCutout(
                texture("carpenter_tank_product_empty"))), light, overlay);
    }

    public enum Kind {
        CARPENTER,
        FABRICATOR
    }
}
