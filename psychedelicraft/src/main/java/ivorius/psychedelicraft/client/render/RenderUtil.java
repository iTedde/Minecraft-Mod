/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render;

import java.util.Random;

import org.joml.Vector3f;
import org.joml.Vector4f;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.CommonColors;
import net.minecraft.util.FastColor;

/**
 * Created by lukas on 25.10.14.
 * Updated by Sollace on 17 Jan 2023
 */
public class RenderUtil {
    private static final Vector4f POSITION_VECTOR = new Vector4f();
    private static final Vector3f NORMAL_VECTOR = new Vector3f();
    public static final int SCREEN_Z_OFFSET = -90;
    private static final Random RNG = new Random(0L);

    public static Random random(long seed) {
        RNG.setSeed(seed);
        return RNG;
    }

    public static void setColor(int color, boolean hasAlpha) {
        RenderSystem.setShaderColor(
                MathUtils.r(color),
                MathUtils.g(color),
                MathUtils.b(color),
                hasAlpha ? MathUtils.a(color) : 1
        );
    }

    private static VertexConsumer fastVertex(VertexConsumer buffer, PoseStack.Pose entry, float x, float y, float z) {
        entry.pose().transform(POSITION_VECTOR.set(x, y, z, 1));
        return buffer.addVertex(POSITION_VECTOR.x, POSITION_VECTOR.y, POSITION_VECTOR.z);
    }

    public static void vertex(VertexConsumer buffer, PoseStack matrices, float x, float y, float z, float u, float v, int light, int overlay) {
        matrices.last().pose().transform(POSITION_VECTOR.set(x, y, z, 1));
        matrices.last().normal().transform(NORMAL_VECTOR.set(0, 1, 0));
        buffer.addVertex(POSITION_VECTOR.x, POSITION_VECTOR.y, POSITION_VECTOR.z, CommonColors.WHITE, u, v, light, overlay, NORMAL_VECTOR.x, NORMAL_VECTOR.y, NORMAL_VECTOR.z);
    }

    public static void drawQuad(GuiGraphics context, ResourceLocation texture, float x0, float y0, float x1, float y1) {
        drawQuad(context, texture, x0, y0, x1, y1, 0);
    }

    public static void drawQuad(GuiGraphics context, ResourceLocation texture, float x0, float y0, float x1, float y1, float z) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.enableBlend();
        BufferBuilder buffer = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        PoseStack.Pose entry = context.pose().last();
        fastVertex(buffer, entry, x0, y1, z).setUv(0, 1);
        fastVertex(buffer, entry, x1, y1, z).setUv(1, 1);
        fastVertex(buffer, entry, x1, y0, z).setUv(1, 0);
        fastVertex(buffer, entry, x0, y0, z).setUv(0, 0);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.disableBlend();
    }

    public static void drawOverlay(GuiGraphics context, ResourceLocation texture, float alpha,
            int width, int height,
            float u0, float v0,
            float u1, float v1, int offset) {
        if (alpha <= 0) {
            return;
        }
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1, 1, 1, alpha);
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.enableBlend();
        BufferBuilder buffer = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        PoseStack.Pose entry = context.pose().last();
        fastVertex(buffer, entry, -offset, height + offset, SCREEN_Z_OFFSET).setUv(u0, v1);
        fastVertex(buffer, entry, width + offset, height + offset, SCREEN_Z_OFFSET).setUv(u1, v1);
        fastVertex(buffer, entry, width + offset, -offset, SCREEN_Z_OFFSET).setUv(u1, v0);
        fastVertex(buffer, entry, -offset, -offset, SCREEN_Z_OFFSET).setUv(u0, v0);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    public static void drawOverlay(GuiGraphics context, ResourceLocation texture, int color,
            int width, int height,
            float u0, float v0,
            float u1, float v1, int offset) {

        int alpha = FastColor.ARGB32.alpha(color);

        if (alpha <= 0) {
            return;
        }
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(
                FastColor.ARGB32.red(color) / 255F,
                FastColor.ARGB32.green(color) / 255F,
                FastColor.ARGB32.blue(color) / 255F,
                alpha / 255F
        );
        RenderSystem.setShaderTexture(0, texture);
        RenderSystem.enableBlend();
        BufferBuilder buffer = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        PoseStack.Pose entry = context.pose().last();
        fastVertex(buffer, entry, -offset, height + offset, SCREEN_Z_OFFSET).setUv(u0, v1).setColor(color);
        fastVertex(buffer, entry, width + offset, height + offset, SCREEN_Z_OFFSET).setUv(u1, v1).setColor(color);
        fastVertex(buffer, entry, width + offset, -offset, SCREEN_Z_OFFSET).setUv(u1, v0).setColor(color);
        fastVertex(buffer, entry, -offset, -offset, SCREEN_Z_OFFSET).setUv(u0, v0).setColor(color);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
    }

    public static void drawBuffer(RenderTarget frame, float r, float g, float b, float a) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, frame.getColorTextureId());
        RenderSystem.setShaderColor(r, g, b, a);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        BufferBuilder buffer = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(0, 0, 0).setUv(0, 1)
              .addVertex(0, frame.viewHeight, 0).setUv(0, 0)
              .addVertex(frame.viewWidth, frame.viewHeight, 0).setUv(1, 0)
              .addVertex(frame.viewWidth, 0, 0).setUv(1, 1);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.disableBlend();
        RenderSystem.setShaderColor(1, 1, 1, 1);
    }

    public static void drawRepeatingSprite(GuiGraphics context, TextureAtlasSprite sprite, int x, int y, int width, int height, float r, float g, float b, float a) {
        final int tileSize = 16;

        int tilesX = width / tileSize;
        int tilesY = height / tileSize;

        int remainedWidth = width % tileSize;
        int remainedHeight = height % tileSize;

        for (int tileX = 0; tileX <= tilesX; tileX ++) {
            for (int tileY = 0; tileY <= tilesY; tileY ++) {
                int w = tileX == tilesX ? remainedWidth : tileSize;
                int h = tileY == tilesY ? remainedHeight : tileSize;
                if (h > 0 && w > 0) {
                    context.blit(x + tileX * tileSize, y + tileY * tileSize, 0, w, h, sprite, r, g, b, a);
                }
            }
        }
    }
}
