package dev.craft.ironchest.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.craft.Craft;
import dev.craft.ironchest.IronChestBlock;
import dev.craft.ironchest.IronChestBlockEntity;
import dev.craft.ironchest.IronChestType;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.Util;
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
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Iron Chests {@code TileEntityIronChestRenderer} (spec 22 §1–2): the 1.4.7 {@code ModelChest} drawn upside down as in
 * 1.4.7, so the original 64×64 textures fit unchanged; the crystal chest also shows its top stacks spinning inside.
 */
public class IronChestRenderer implements BlockEntityRenderer<IronChestBlockEntity> {
    private static final Map<IronChestType, ResourceLocation> TEXTURES = new EnumMap<>(IronChestType.class);
    private static final float HALF_PI = (float) (Math.PI / 2.0);
    /** Item positions inside the crystal chest; the last one is used for a single item. */
    private static final float[][] SHIFTS = {
            {0.3F, 0.45F, 0.3F}, {0.7F, 0.45F, 0.3F}, {0.3F, 0.45F, 0.7F}, {0.7F, 0.45F, 0.7F},
            {0.3F, 0.1F, 0.3F}, {0.7F, 0.1F, 0.3F}, {0.3F, 0.1F, 0.7F}, {0.7F, 0.1F, 0.7F},
            {0.5F, 0.32F, 0.5F}};

    static {
        for (IronChestType type : IronChestType.values()) {
            TEXTURES.put(type, ResourceLocation.fromNamespaceAndPath(Craft.MODID,
                    "textures/entity/ironchest/" + type.getName() + ".png"));
        }
    }

    private final ModelPart root = createModel();
    private final ModelPart lid = root.getChild("lid");
    private final ModelPart knob = root.getChild("knob");

    public IronChestRenderer(BlockEntityRendererProvider.Context context) {
    }

    /** {@code ModelChest}: lid, knob and bottom with the 1.4.7 boxes and pivots. */
    private static ModelPart createModel() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition parts = mesh.getRoot();
        parts.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 0).addBox(0, -5, -14, 14, 5, 14),
                PartPose.offset(1, 7, 15));
        parts.addOrReplaceChild("knob", CubeListBuilder.create().texOffs(0, 0).addBox(-1, -2, -15, 2, 4, 1),
                PartPose.offset(8, 7, 15));
        parts.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 19).addBox(0, 0, 0, 14, 10, 14),
                PartPose.offset(1, 6, 1));
        return LayerDefinition.create(mesh, 64, 64).bakeRoot();
    }

    @Override
    public void render(IronChestBlockEntity chest, float partialTick, PoseStack pose, MultiBufferSource buffers,
                       int light, int overlay) {
        IronChestType type = chest.getChestType();
        render(type, chest.getBlockState().getValue(IronChestBlock.FACING), chest.getLidAngle(partialTick), pose,
                buffers, light, overlay);
        Level level = chest.getLevel();
        if (type.isTransparent() && level != null) {
            renderTopStacks(chest.getTopStacks(), level, pose, buffers, light, overlay);
        }
    }

    void render(IronChestType type, Direction facing, float lidAngle, PoseStack pose, MultiBufferSource buffers,
                int light, int overlay) {
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
        float open = 1.0F - lidAngle;
        open = 1.0F - open * open * open;
        lid.xRot = -(open * HALF_PI);
        knob.xRot = lid.xRot;
        ResourceLocation texture = TEXTURES.get(type);
        RenderType renderType = type.isTransparent()
                ? RenderType.entityTranslucent(texture) : RenderType.entityCutout(texture);
        root.render(pose, buffers.getBuffer(renderType), light, overlay);
        pose.popPose();
    }

    /** Up to eight items at 0.7 scale, a single item centred at 0.85; one turn every 16.384 s. */
    private static void renderTopStacks(List<ItemStack> stacks, Level level, PoseStack pose,
                                        MultiBufferSource buffers, int light, int overlay) {
        if (stacks.isEmpty()) {
            return;
        }
        int shift = 0;
        float scale = 0.7F;
        if (stacks.size() == 1) {
            shift = 8;
            scale = 0.85F;
        }
        float rotation = (float) (360.0 * (Util.getMillis() & 16383L) / 16383.0);
        for (ItemStack stack : stacks) {
            if (shift >= SHIFTS.length) {
                break;
            }
            float[] position = SHIFTS[shift++];
            pose.pushPose();
            pose.translate(position[0], position[1], position[2]);
            pose.mulPose(Axis.YP.rotationDegrees(rotation));
            pose.scale(scale, scale, scale);
            // The 1.4.7 item entity floated 0.1 above its position.
            pose.translate(0.0F, 0.1F, 0.0F);
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.GROUND, light, overlay,
                    pose, buffers, level, 0);
            pose.popPose();
        }
    }

    /** Only within 128 blocks the crystal chest shows its items; the chest itself as far as that too. */
    @Override
    public int getViewDistance() {
        return 128;
    }

    /** The closed chest facing south ({@code IronChestRenderHelper}). */
    public static class ItemRenderer extends BlockEntityWithoutLevelRenderer {
        private IronChestRenderer renderer;

        public ItemRenderer() {
            super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
        }

        @Override
        public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack pose, MultiBufferSource buffers,
                                 int light, int overlay) {
            if (renderer == null) {
                renderer = new IronChestRenderer(null);
            }
            if (stack.getItem() instanceof BlockItem item && item.getBlock() instanceof IronChestBlock block) {
                renderer.render(block.getType(), Direction.SOUTH, 0.0F, pose, buffers, light, overlay);
            }
        }
    }
}
