package dev.craft.ic2.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.craft.Craft;
import dev.craft.ic2.personal.PersonalSafeBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/**
 * IC2 {@code TileEntityPersonalChestRenderer} with {@code ModelPersonalChest}: five walls and a door that swings
 * open while the safe is in use; texture {@code newsafe.png}. IC2 sets {@code mirror} only after adding the boxes, so
 * nothing is mirrored.
 */
public class PersonalSafeRenderer implements BlockEntityRenderer<PersonalSafeBlockEntity> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/block/personal/personal_safe.png");
    private static final float HALF_PI = (float) (Math.PI / 2.0);

    private final ModelPart root = createModel();
    private final ModelPart door = root.getChild("door");

    public PersonalSafeRenderer(BlockEntityRendererProvider.Context context) {
    }

    private static ModelPart createModel() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();
        parts.addOrReplaceChild("wall_r", CubeListBuilder.create().texOffs(0, 0).addBox(0, 0, 0, 14, 16, 1),
                PartPose.offsetAndRotation(1, 0, 15, 0, 1.570796F, 0));
        parts.addOrReplaceChild("wall_l", CubeListBuilder.create().texOffs(0, 0).addBox(0, 0, 0, 14, 16, 1),
                PartPose.offsetAndRotation(15, 0, 1, 0, -1.570796F, 0));
        parts.addOrReplaceChild("wall_b", CubeListBuilder.create().texOffs(1, 1).addBox(1, 1, 0, 12, 14, 1),
                PartPose.offsetAndRotation(15, 0, 15, 0, 3.141593F, 0));
        parts.addOrReplaceChild("wall_u", CubeListBuilder.create().texOffs(1, 17).addBox(1, 0, 0, 12, 14, 1),
                PartPose.offsetAndRotation(1, 0, 15, -1.570796F, 0, 0));
        parts.addOrReplaceChild("wall_d", CubeListBuilder.create().texOffs(1, 17).addBox(1, 0, 0, 12, 14, 1),
                PartPose.offsetAndRotation(15, 15, 1, -1.570796F, 3.141593F, 0));
        parts.addOrReplaceChild("door", CubeListBuilder.create().texOffs(30, 0).addBox(0, 0, 0, 12, 14, 1),
                PartPose.offset(2, 1, 2));
        return LayerDefinition.create(mesh, 64, 64).bakeRoot();
    }

    @Override
    public void render(PersonalSafeBlockEntity safe, float partialTick, PoseStack pose, MultiBufferSource buffers,
                       int light, int overlay) {
        render(safe.getFacing(), safe.getLidAngle(partialTick), pose, buffers, light, overlay);
    }

    void render(Direction facing, float lid, PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        pose.pushPose();
        pose.translate(0.0F, 1.0F, 1.0F);
        pose.scale(1.0F, -1.0F, -1.0F);
        pose.translate(0.5F, 0.5F, 0.5F);
        int angle = switch (facing) {
            case NORTH -> 180;
            case WEST -> 90;
            case EAST -> -90;
            default -> 0;
        };
        pose.mulPose(Axis.YP.rotationDegrees(angle));
        pose.translate(-0.5F, -0.5F, -0.5F);
        float open = 1.0F - lid;
        open = 1.0F - open * open * open;
        door.yRot = open * HALF_PI;
        root.render(pose, buffers.getBuffer(RenderType.entityCutout(TEXTURE)), light, overlay);
        pose.popPose();
    }

    /** IC2 {@code RenderBlockPersonal.renderInventoryBlock}: the closed safe. */
    public static class ItemRenderer extends BlockEntityWithoutLevelRenderer {
        private PersonalSafeRenderer renderer;

        public ItemRenderer() {
            super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        }

        @Override
        public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers,
                                 int light, int overlay) {
            if (renderer == null) {
                renderer = new PersonalSafeRenderer(null);
            }
            renderer.render(Direction.SOUTH, 0.0F, pose, buffers, light, overlay);
        }
    }
}
