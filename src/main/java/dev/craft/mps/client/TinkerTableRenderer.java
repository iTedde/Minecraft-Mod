package dev.craft.mps.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.craft.Craft;
import dev.craft.mps.block.TinkerTableBlockEntity;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;

/**
 * MPS {@code TinkerTableModel}/{@code TinkerTableRenderer} (spec 29 §11): the 1.4.7 boxes and texture offsets on the
 * 112×70 texture; screens, particles and the turning cube glow, the cube bobs and spins once every 10 s.
 */
public class TinkerTableRenderer implements BlockEntityRenderer<TinkerTableBlockEntity> {
    public static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/entity/mps/tinker_table.png");
    private static final float HALF_PI = (float) (Math.PI / 2.0);
    private static final String[] SOLID = {"middletable", "uppertable", "footbase", "foot1", "fatfoot2", "fatfoot1",
            "backsupport", "tank3", "tank2", "tank1", "wireshort4", "wireshort3", "wireshort2", "wireshort1",
            "wirelong1"};
    private static final String[] GLOWING = {"screen3", "screen2", "screen1", "particles"};

    private final ModelPart root = createModel();

    public TinkerTableRenderer(BlockEntityRendererProvider.Context context) {
    }

    private static ModelPart createModel() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();
        box(parts, "cube", 96, 20, -2, -2, -2, 4, 4, 4, 0, 0, 0, 0);
        box(parts, "screen3", 1, 1, 0, 0, 0, 11, 0, 14, -9.666667F, 3.466667F, -7, 0);
        box(parts, "screen2", 0, 32, 0, 0, 0, 8, 0, 11, 2, 4.966667F, -6, 0);
        box(parts, "screen1", 3, 20, 0, 0, 0, 14, 0, 7, -6, 2.466667F, 3, 0);
        box(parts, "middletable", 40, 49, -5, 1, -5, 17, 3, 18, -4, 10, -4, 0);
        box(parts, "uppertable", 56, 28, 0, 0, 0, 12, 5, 16, -8, 10, -8, 0);
        box(parts, "particles", 90, 0, 0, 0, 0, 6, 7, 5, -3, 15, -3, 0);
        box(parts, "footbase", 0, 54, -5, 8, -5, 12, 2, 11, -1, 14, -1, 0);
        box(parts, "foot1", 82, 13, -5, 8, -5, 4, 3, 3, -2, 13, 3, 0);
        box(parts, "fatfoot2", 96, 13, -5, 8, -5, 4, 3, 4, 3, 13, 1, HALF_PI);
        box(parts, "fatfoot1", 96, 13, -5, 8, -5, 4, 3, 4, 3, 13, -8, HALF_PI);
        box(parts, "backsupport", 38, 34, 0, 0, -2, 2, 8, 7, 3, 14, -2, 0);
        box(parts, "tank3", 51, 18, 0, 0, 0, 3, 5, 3, 6, 10, 3, 0);
        box(parts, "tank2", 51, 18, 0, 0, 0, 3, 5, 3, 6, 10, -2, 0);
        box(parts, "tank1", 51, 18, 0, 0, 0, 3, 5, 3, 6, 10, -7, 0);
        box(parts, "wireshort4", 71, 15, 0, 0, 5, 1, 2, 1, 7, 15, -1, 0);
        box(parts, "wireshort3", 71, 15, 0, 0, 0, 1, 2, 1, 7, 15, -6, 0);
        box(parts, "wireshort2", 69, 13, 0, 0, 0, 2, 1, 1, 5, 17, -1, 0);
        box(parts, "wireshort1", 71, 15, 0, 0, 0, 1, 2, 1, 7, 15, -1, 0);
        box(parts, "wirelong1", 77, 1, 0, 0, 0, 1, 1, 11, 7, 17, -6, 0);
        return LayerDefinition.create(mesh, 112, 70).bakeRoot();
    }

    private static void box(PartDefinition parts, String name, int u, int v, float x, float y, float z, int width,
                            int height, int depth, float pivotX, float pivotY, float pivotZ, float yRot) {
        parts.addOrReplaceChild(name, CubeListBuilder.create().texOffs(u, v).addBox(x, y, z, width, height, depth),
                PartPose.offsetAndRotation(pivotX, pivotY, pivotZ, 0, yRot, 0));
    }

    @Override
    public void render(TinkerTableBlockEntity table, float partialTick, PoseStack pose, MultiBufferSource buffers,
                       int light, int overlay) {
        render(pose, buffers, light, overlay);
    }

    void render(PoseStack pose, MultiBufferSource buffers, int light, int overlay) {
        VertexConsumer solid = buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURE));
        pose.pushPose();
        pose.mulPose(Axis.XP.rotationDegrees(180.0F));
        pose.translate(0.5F, -1.5F, -0.5F);
        for (String name : SOLID) {
            root.getChild(name).render(pose, solid, light, overlay);
        }
        pose.popPose();

        VertexConsumer glow = buffers.getBuffer(RenderType.entityTranslucentEmissive(TEXTURE));
        double angle = (Util.getMillis() % 10000L) * Math.PI / 5000.0;
        pose.pushPose();
        pose.translate(0.5, 1.05F, 0.5);
        pose.translate(0.0, 0.02F * Math.sin(angle * 3.0), 0.0);
        pose.mulPose(Axis.YP.rotation((float) angle));
        pose.mulPose(Axis.XP.rotationDegrees(45.0F));
        // glRotatef(35.26°, 0, 1, 1) about the normalised axis (0, 1, 1)
        pose.mulPose(new Quaternionf().rotationAxis((float) Math.toRadians(35.26439),
                0.0F, (float) Math.sqrt(0.5), (float) Math.sqrt(0.5)));
        pose.scale(0.5F, 0.5F, 0.5F);
        root.getChild("cube").render(pose, glow, LightTexture.FULL_BRIGHT, overlay, 0xCCFFFFFF);
        pose.popPose();

        pose.pushPose();
        pose.mulPose(Axis.XP.rotationDegrees(180.0F));
        pose.translate(0.5F, -1.5F, -0.5F);
        for (String name : GLOWING) {
            root.getChild(name).render(pose, glow, LightTexture.FULL_BRIGHT, overlay);
        }
        pose.popPose();
    }

    /** Item rendering ({@code renderInventoryBlock}). */
    public static class ItemRenderer extends BlockEntityWithoutLevelRenderer {
        private TinkerTableRenderer renderer;

        public ItemRenderer() {
            super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        }

        @Override
        public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers,
                                 int light, int overlay) {
            if (renderer == null) {
                renderer = new TinkerTableRenderer(null);
            }
            renderer.render(pose, buffers, light, overlay);
        }
    }
}
