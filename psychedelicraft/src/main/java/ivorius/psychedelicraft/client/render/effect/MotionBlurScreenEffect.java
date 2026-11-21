/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.effect;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import org.lwjgl.opengl.GL11;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.GlStateManager.DestFactor;
import com.mojang.blaze3d.platform.GlStateManager.SourceFactor;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.entity.drug.Drug;
import ivorius.psychedelicraft.entity.drug.DrugProperties;

/**
 * Created by lukas on 21.02.14.
 * Updated by Sollace on 15 Jan 2023
 * <p>
 * Port: the template drew 30 delayed copies of the main framebuffer on top of the world with depth testing still on and
 * re-sampled the already blended picture, which washed the screen out towards white and hid the HUD drawn afterwards.
 * Replaced by a single accumulation buffer: the history is blended over the current frame, then the result becomes the
 * new history. The history weight matches the template's summed sample weights (30 samples, {@code i * 0.002}).
 */
public class MotionBlurScreenEffect implements ScreenEffect {
    /** Sum of the template's per-sample alphas: 0.002 * (1 + 2 + ... + 30). */
    private static final float HISTORY_WEIGHT = 0.002F * 30 * 31 / 2;
    private static final float MAX_HISTORY = 0.9F;

    private TextureTarget history;
    private boolean historyValid;

    public float motionBlur;

    @Override
    public void update(float tickDelta) {
        motionBlur = PsychedelicraftClient.getConfig().doMotionBlur.get() && Minecraft.getInstance().player != null
                ? DrugProperties.of(Minecraft.getInstance().player).getModifier(Drug.MOTION_BLUR)
                : 0;
    }

    @Override
    public void render(GuiGraphics context, Window window, float tickDelta) {
        if (Minecraft.getInstance().player == null || motionBlur <= 0) {
            historyValid = false;
            return;
        }

        RenderTarget main = Minecraft.getInstance().getMainRenderTarget();
        if (history != null && (history.width != main.width || history.height != main.height)) {
            close();
        }
        if (history == null) {
            history = new TextureTarget(main.width, main.height, false, Minecraft.ON_OSX);
            historyValid = false;
        }

        if (historyValid) {
            drawHistory(window.getGuiScaledWidth(), window.getGuiScaledHeight(), Math.min(MAX_HISTORY, HISTORY_WEIGHT * motionBlur));
        }
        capture(main);
        historyValid = true;
    }

    private void drawHistory(int width, int height, float alpha) {
        boolean depthTest = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderTexture(0, history.getColorTextureId());
        RenderSystem.setShaderColor(1, 1, 1, alpha);
        BufferBuilder buffer = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(0, 0, 0).setUv(0, 1)
              .addVertex(0, height, 0).setUv(0, 0)
              .addVertex(width, height, 0).setUv(1, 0)
              .addVertex(width, 0, 0).setUv(1, 1);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        RenderSystem.setShaderColor(1, 1, 1, 1);
        RenderSystem.disableBlend();
        RenderSystem.depthMask(true);
        if (depthTest) {
            RenderSystem.enableDepthTest();
        }
    }

    private void capture(RenderTarget main) {
        GlStateManager._glBindFramebuffer(GlConst.GL_READ_FRAMEBUFFER, main.frameBufferId);
        GlStateManager._glBindFramebuffer(GlConst.GL_DRAW_FRAMEBUFFER, history.frameBufferId);
        GlStateManager._glBlitFrameBuffer(0, 0, main.width, main.height, 0, 0, history.width, history.height, GL11.GL_COLOR_BUFFER_BIT, GlConst.GL_NEAREST);
        // the world leaves arbitrary alpha in the main target; the history must be opaque so only the blend alpha counts
        GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, history.frameBufferId);
        RenderSystem.colorMask(false, false, false, true);
        GlStateManager._clearColor(0, 0, 0, 1);
        GlStateManager._clear(GL11.GL_COLOR_BUFFER_BIT, Minecraft.ON_OSX);
        RenderSystem.colorMask(true, true, true, true);
        main.bindWrite(true);
    }

    @Override
    public void close() {
        if (history != null) {
            history.destroyBuffers();
            history = null;
        }
        historyValid = false;
    }
}
