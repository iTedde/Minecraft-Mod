package dev.craft.nuclearcontrol.client;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.craft.Craft;
import dev.craft.nuclearcontrol.block.InfoPanelBlock;
import dev.craft.nuclearcontrol.block.InfoPanelBlockEntity;
import dev.craft.nuclearcontrol.card.CardData;
import dev.craft.nuclearcontrol.card.CardState;
import dev.craft.nuclearcontrol.card.PanelCard;
import dev.craft.nuclearcontrol.card.PanelLine;
import dev.craft.nuclearcontrol.card.PanelText;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

/**
 * Screen of the information panel ({@code MainBlockRenderer} + {@code TileEntityInfoPanelRenderer}). The panel draws
 * the fronts of all blocks of its screen from {@code InfoPanelColorsOn/Off.png} (row = background colour, 2 without
 * colour upgrade; column = border mask 1 left, 2 right, 4 top, 8 bottom) and, when powered, the card lines scaled to
 * fit the whole screen ({@code 0.875 + extent} wide and high), turned by the panel rotation. Text without shadow and
 * at full brightness; line colour 0 means the panel's text colour.
 */
public class InfoPanelRenderer implements BlockEntityRenderer<InfoPanelBlockEntity> {
    private static final ResourceLocation SHEET_ON = ResourceLocation.fromNamespaceAndPath(Craft.MODID,
            "textures/block/nuclearcontrol/information_panel_colors_on.png");
    private static final ResourceLocation SHEET_OFF = ResourceLocation.fromNamespaceAndPath(Craft.MODID,
            "textures/block/nuclearcontrol/information_panel_colors_off.png");
    private static final float TILE = 1.0F / 16.0F;
    private static final float FACE = 0.5F + 0.001F;

    private final Font font;

    public InfoPanelRenderer(BlockEntityRendererProvider.Context context) {
        this.font = context.getFont();
    }

    @Override
    public void render(InfoPanelBlockEntity panel, float partialTick, PoseStack poseStack, MultiBufferSource buffers,
                       int packedLight, int packedOverlay) {
        Direction facing = panel.getBlockState().getValue(InfoPanelBlock.FACING);
        Direction reading = FaceTransform.reading(facing);
        Direction up = FaceTransform.up(facing);
        BlockPos origin = panel.getBlockPos();
        BlockPos min = panel.getScreenMin().subtract(origin);
        BlockPos max = panel.getScreenMax().subtract(origin);
        int aMin = Math.min(dot(min, reading), dot(max, reading));
        int aMax = Math.max(dot(min, reading), dot(max, reading));
        int bMin = Math.min(dot(min, up), dot(max, up));
        int bMax = Math.max(dot(min, up), dot(max, up));

        poseStack.pushPose();
        FaceTransform.apply(poseStack, facing);
        poseStack.translate(0.0, FACE, 0.0);
        poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));

        int row = panel.isColored() ? panel.getColorBackground() : InfoPanelBlockEntity.DEFAULT_BACKGROUND;
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(
                panel.isPowered() ? SHEET_ON : SHEET_OFF));
        for (BlockPos pos : BlockPos.betweenClosed(panel.getScreenMin(), panel.getScreenMax())) {
            BlockPos offset = pos.subtract(origin);
            int a = dot(offset, reading);
            int b = dot(offset, up);
            int mask = (a == aMin ? 1 : 0) | (a == aMax ? 2 : 0) | (b == bMax ? 4 : 0) | (b == bMin ? 8 : 0);
            int light = panel.getLevel() == null ? packedLight
                    : LevelRenderer.getLightColor(panel.getLevel(), pos.relative(facing));
            tile(consumer, poseStack.last(), a, b, row * 16 + mask, light);
        }

        List<PanelLine> lines = panel.isPowered() ? lines(panel) : null;
        if (lines != null && !lines.isEmpty()) {
            poseStack.translate((aMin + aMax) / 2.0F, (bMin + bMax) / 2.0F, 0.001F);
            float width = 0.875F + (aMax - aMin);
            float height = 0.875F + (bMax - bMin);
            switch (panel.getRotation()) {
                case 1 -> poseStack.mulPose(Axis.ZP.rotationDegrees(-90.0F));
                case 2 -> poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
                case 3 -> poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
                default -> {
                }
            }
            if (panel.getRotation() == 1 || panel.getRotation() == 2) {
                float swap = width;
                width = height;
                height = swap;
            }
            drawLines(panel, lines, width, height, poseStack, buffers);
        }
        poseStack.popPose();
    }

    private static int dot(BlockPos pos, Direction direction) {
        return pos.getX() * direction.getStepX() + pos.getY() * direction.getStepY()
                + pos.getZ() * direction.getStepZ();
    }

    private static void tile(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, int index, int light) {
        float u0 = index % 16 * TILE;
        float v0 = index / 16 * TILE;
        vertex(consumer, pose, x - 0.5F, y - 0.5F, u0, v0 + TILE, light);
        vertex(consumer, pose, x + 0.5F, y - 0.5F, u0 + TILE, v0 + TILE, light);
        vertex(consumer, pose, x + 0.5F, y + 0.5F, u0 + TILE, v0, light);
        vertex(consumer, pose, x - 0.5F, y + 0.5F, u0, v0, light);
    }

    private static void vertex(VertexConsumer consumer, PoseStack.Pose pose, float x, float y, float u, float v,
                               int light) {
        consumer.addVertex(pose, x, y, 0.0F).setColor(255, 255, 255, 255).setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    /** {@code getCardData}: state message or the card lines, the title centred on top. */
    private static List<PanelLine> lines(InfoPanelBlockEntity panel) {
        ItemStack card = panel.getCard();
        int settings = panel.getDisplaySettings();
        if (!(card.getItem() instanceof PanelCard source) || settings == 0 || panel.getLevel() == null) {
            return null;
        }
        CardData data = CardData.of(card);
        CardState state = data.getState();
        if (state == null) {
            return null;
        }
        if (state != CardState.OK) {
            return List.of(PanelLine.center(PanelText.text(state.getMessage())));
        }
        List<PanelLine> lines = new ArrayList<>(source.lines(settings, data, panel.getShowLabels(),
                panel.getLevel()));
        if (!data.getTitle().isEmpty()) {
            lines.addFirst(PanelLine.center(data.getTitle()));
        }
        return lines;
    }

    private void drawLines(InfoPanelBlockEntity panel, List<PanelLine> lines, float width, float height,
                           PoseStack poseStack, MultiBufferSource buffers) {
        int maxWidth = 1;
        for (PanelLine line : lines) {
            maxWidth = Math.max(maxWidth, font.width(join(line)));
        }
        maxWidth += 4;
        int lineHeight = font.lineHeight + 2;
        int requiredHeight = lineHeight * lines.size();
        float scaleX = width / maxWidth;
        float scaleY = height / requiredHeight;
        float scale = Math.min(scaleX, scaleY);
        poseStack.scale(scale, -scale, scale);
        int realHeight = (int) Math.floor(height / scale);
        int realWidth = (int) Math.floor(width / scale);
        int offsetX;
        int offsetY;
        if (scaleX < scaleY) {
            offsetX = 2;
            offsetY = (realHeight - requiredHeight) / 2;
        } else {
            offsetX = (realWidth - maxWidth) / 2 + 2;
            offsetY = 0;
        }
        int textColor = panel.getColorTextHex();
        int row = 0;
        for (PanelLine line : lines) {
            int y = offsetY - realHeight / 2 + row * lineHeight;
            if (line.left() != null) {
                draw(line.left(), offsetX - realWidth / 2, 1 + y, color(line.colorLeft(), textColor), poseStack,
                        buffers);
            }
            if (line.center() != null) {
                draw(line.center(), -font.width(line.center()) / 2, y, color(line.colorCenter(), textColor),
                        poseStack, buffers);
            }
            if (line.right() != null) {
                draw(line.right(), realWidth / 2 - font.width(line.right()), y, color(line.colorRight(), textColor),
                        poseStack, buffers);
            }
            row++;
        }
    }

    private static int color(int lineColor, int panelColor) {
        return lineColor != 0 ? lineColor : panelColor;
    }

    /** {@code implodeArray}: the non-empty parts joined with spaces. */
    private static String join(PanelLine line) {
        StringBuilder builder = new StringBuilder();
        for (String part : new String[]{line.left(), line.center(), line.right()}) {
            if (part != null && !part.isEmpty()) {
                if (!builder.isEmpty()) {
                    builder.append(' ');
                }
                builder.append(part);
            }
        }
        return builder.toString();
    }

    private void draw(String text, float x, float y, int color, PoseStack poseStack, MultiBufferSource buffers) {
        font.drawInBatch(text, x, y, 0xFF000000 | color, false, poseStack.last().pose(), buffers,
                Font.DisplayMode.POLYGON_OFFSET, 0, LightTexture.FULL_BRIGHT);
    }

    @Override
    public AABB getRenderBoundingBox(InfoPanelBlockEntity panel) {
        return AABB.encapsulatingFullBlocks(panel.getScreenMin(), panel.getScreenMax());
    }
}
