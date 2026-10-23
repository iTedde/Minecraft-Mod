package dev.craft.enderstorage.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.craft.Craft;
import dev.craft.enderstorage.EnderChestBlock;
import dev.craft.enderstorage.EnderChestBlockEntity;
import dev.craft.enderstorage.EsDataComponents;
import dev.craft.enderstorage.Frequency;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Matrix4f;

/**
 * EnderStorage {@code EnderChestRenderer} (spec 27 §3): {@code ModelEnderChest} (vanilla chest boxes, latch or diamond
 * latch), the three colour buttons on the lid, the end portal surface inside and the floating pearl. The pearl is the
 * ender pearl item instead of CodeChickenCore's textured icosahedron.
 */
public class EnderChestRenderer implements BlockEntityRenderer<EnderChestBlockEntity> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/entity/enderstorage/ender_chest.png");
    private static final ResourceLocation BUTTONS =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/entity/enderstorage/buttons.png");

    private final ModelPart root = createModel();
    private final ModelPart lid = root.getChild("lid");
    private final ModelPart knob = root.getChild("knob");
    private final ModelPart diamondKnob = root.getChild("diamond_knob");
    private final ModelPart bottom = root.getChild("bottom");

    public EnderChestRenderer(BlockEntityRendererProvider.Context context) {
    }

    /** {@code ModelEnderChest}: 1.4.7 chest boxes and pivots, the diamond latch from texture row 5. */
    private static ModelPart createModel() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();
        parts.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 0).addBox(0, -5, -14, 14, 5, 14),
                PartPose.offset(1, 7, 15));
        parts.addOrReplaceChild("knob", CubeListBuilder.create().texOffs(0, 0).addBox(-1, -2, -15, 2, 4, 1),
                PartPose.offset(8, 7, 15));
        parts.addOrReplaceChild("diamond_knob", CubeListBuilder.create().texOffs(0, 5).addBox(-1, -2, -15, 2, 4, 1),
                PartPose.offset(8, 7, 15));
        parts.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 19).addBox(0, 0, 0, 14, 10, 14),
                PartPose.offset(1, 6, 1));
        return LayerDefinition.create(mesh, 64, 64).bakeRoot();
    }

    @Override
    public void render(EnderChestBlockEntity chest, float partialTick, PoseStack pose, MultiBufferSource buffers,
                       int light, int overlay) {
        BlockPos pos = chest.getBlockPos();
        // getTimeOffset: chests do not bob in step.
        double time = (chest.getLevel() != null ? chest.getLevel().getGameTime() : 0) + partialTick
                + pos.getX() * 3 + pos.getY() * 5 + pos.getZ() * 9;
        render(chest.getFrequency(), chest.getBlockState().getValue(EnderChestBlock.FACING),
                chest.getLidAngle(partialTick), time, pose, buffers, light, overlay);
    }

    void render(Frequency frequency, Direction facing, float lidAngle, double time, PoseStack pose,
                MultiBufferSource buffers, int light, int overlay) {
        renderPortal(pose, buffers);
        renderChest(frequency.isPersonal(), facing, lidAngle, pose, buffers, light, overlay);
        renderButtons(frequency, facing, lidAngle, pose, buffers, light, overlay);
        renderPearl(lidAngle, time, pose, buffers, light, overlay);
    }

    /** {@code RenderCustomEndPortal(0.626, 0.188, 0.812, 0.188, 0.812)}: the floor of the chest's inside. */
    private static void renderPortal(PoseStack pose, MultiBufferSource buffers) {
        Matrix4f matrix = pose.last().pose();
        VertexConsumer buffer = buffers.getBuffer(RenderType.endPortal());
        float y = 0.626F;
        buffer.addVertex(matrix, 0.188F, y, 0.812F);
        buffer.addVertex(matrix, 0.812F, y, 0.812F);
        buffer.addVertex(matrix, 0.812F, y, 0.188F);
        buffer.addVertex(matrix, 0.188F, y, 0.188F);
    }

    private void renderChest(boolean personal, Direction facing, float lidAngle, PoseStack pose,
                             MultiBufferSource buffers, int light, int overlay) {
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
        lid.xRot = lidAngle;
        knob.xRot = lidAngle;
        diamondKnob.xRot = lidAngle;
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutout(TEXTURE));
        lid.render(pose, buffer, light, overlay);
        bottom.render(pose, buffer, light, overlay);
        (personal ? diamondKnob : knob).render(pose, buffer, light, overlay);
        pose.popPose();
    }

    /**
     * {@code drawButton}: 2 × 1 × 4 pixel buttons on the lid, turned with it around the hinge (0, 9, 1 px) and with
     * the chest; colour {@code c} uses cell {@code c} of the 4 × 4 sheet.
     */
    private static void renderButtons(Frequency frequency, Direction facing, float lidAngle, PoseStack pose,
                                      MultiBufferSource buffers, int light, int overlay) {
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutout(BUTTONS));
        pose.pushPose();
        pose.translate(0.5F, 0.0F, 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(-90.0F * facing.get2DDataValue()));
        pose.translate(-0.5F, 0.0F, -0.5F);
        pose.translate(0.0F, 0.5625F, 0.0625F);
        pose.mulPose(Axis.XP.rotation(lidAngle));
        pose.translate(0.0F, -0.5625F, -0.0625F);
        PoseStack.Pose last = pose.last();
        for (int button = 0; button < 3; button++) {
            int colour = frequency.colour(button);
            float u = 0.25F * (colour % 4);
            float v = 0.25F * (colour / 4);
            float x = 0.25F + 0.1875F * button;
            box(last, buffer, x, 0.875F, 0.375F, x + 0.125F, 0.9375F, 0.625F,
                    u + 0.0938F, v + 0.0625F, u + 0.1562F, v + 0.1875F, light, overlay);
        }
        pose.popPose();
    }

    /** A box with one texture area on every face. */
    private static void box(PoseStack.Pose pose, VertexConsumer buffer, float x0, float y0, float z0, float x1,
                            float y1, float z1, float u0, float v0, float u1, float v1, int light, int overlay) {
        quad(pose, buffer, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0, u0, v0, u1, v1, 0, 1, 0, light, overlay);
        quad(pose, buffer, x0, y0, z1, x0, y0, z0, x1, y0, z0, x1, y0, z1, u0, v0, u1, v1, 0, -1, 0, light, overlay);
        quad(pose, buffer, x0, y1, z1, x0, y0, z1, x1, y0, z1, x1, y1, z1, u0, v0, u1, v1, 0, 0, 1, light, overlay);
        quad(pose, buffer, x1, y1, z0, x1, y0, z0, x0, y0, z0, x0, y1, z0, u0, v0, u1, v1, 0, 0, -1, light, overlay);
        quad(pose, buffer, x1, y1, z1, x1, y0, z1, x1, y0, z0, x1, y1, z0, u0, v0, u1, v1, 1, 0, 0, light, overlay);
        quad(pose, buffer, x0, y1, z0, x0, y0, z0, x0, y0, z1, x0, y1, z1, u0, v0, u1, v1, -1, 0, 0, light, overlay);
    }

    private static void quad(PoseStack.Pose pose, VertexConsumer buffer, float ax, float ay, float az, float bx,
                             float by, float bz, float cx, float cy, float cz, float dx, float dy, float dz, float u0,
                             float v0, float u1, float v1, float nx, float ny, float nz, int light, int overlay) {
        vertex(pose, buffer, ax, ay, az, u0, v0, nx, ny, nz, light, overlay);
        vertex(pose, buffer, bx, by, bz, u0, v1, nx, ny, nz, light, overlay);
        vertex(pose, buffer, cx, cy, cz, u1, v1, nx, ny, nz, light, overlay);
        vertex(pose, buffer, dx, dy, dz, u1, v0, nx, ny, nz, light, overlay);
    }

    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, float x, float y, float z, float u,
                               float v, float nx, float ny, float nz, int light, int overlay) {
        buffer.addVertex(pose, x, y, z).setColor(-1).setUv(u, v).setOverlay(overlay).setLight(light)
                .setNormal(pose, nx, ny, nz);
    }

    /** The pearl floats 0.2 above the floor, rises as the lid opens, bobs ±0.1 and turns. */
    private static void renderPearl(float lidAngle, double time, PoseStack pose, MultiBufferSource buffers, int light,
                                    int overlay) {
        float bob = (float) Math.sin(time / 25.0 * Math.PI) * 0.1F;
        pose.pushPose();
        pose.translate(0.5, 0.2 + lidAngle * -0.5 + bob, 0.5);
        pose.mulPose(Axis.YP.rotation((float) (time / 3.0)));
        pose.scale(0.35F, 0.35F, 0.35F);
        Minecraft.getInstance().getItemRenderer().renderStatic(new ItemStack(Items.ENDER_PEARL),
                ItemDisplayContext.FIXED, light, overlay, pose, buffers, null, 0);
        pose.popPose();
    }

    /** The closed chest facing south with the colours of the item. */
    public static class ItemRenderer extends BlockEntityWithoutLevelRenderer {
        private EnderChestRenderer renderer;

        public ItemRenderer() {
            super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        }

        @Override
        public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers,
                                 int light, int overlay) {
            if (renderer == null) {
                renderer = new EnderChestRenderer(null);
            }
            Frequency frequency = stack.getOrDefault(EsDataComponents.FREQUENCY.get(), Frequency.GLOBAL);
            renderer.renderChest(frequency.isPersonal(), Direction.SOUTH, 0.0F, pose, buffers, light, overlay);
            renderButtons(frequency, Direction.SOUTH, 0.0F, pose, buffers, light, overlay);
        }
    }
}
