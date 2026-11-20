/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.effect;

import java.util.*;
import java.util.stream.IntStream;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import org.lwjgl.opengl.GL11;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import ivorius.psychedelicraft.client.PsychedelicraftClient;
import ivorius.psychedelicraft.client.render.RenderUtil;
import ivorius.psychedelicraft.entity.drug.Drug;
import ivorius.psychedelicraft.entity.drug.DrugProperties;

/**
 * Created by lukas on 21.02.14.
 * Updated by Sollace on 15 Jan 2023
 */
public class MotionBlurScreenEffect implements ScreenEffect {
    private static final int MAX_SAMPLES = 30;
    private static final float SAMPLE_FREQUENCY = 0.5f;

    private float previousTicks;
    private int currentSample;
    private GlTextureSet textures;

    public float motionBlur;

    @Override
    public void update(float tickDelta) {
        motionBlur = PsychedelicraftClient.getConfig().doMotionBlur.get() && Minecraft.getInstance().player != null
                ? DrugProperties.of(Minecraft.getInstance().player).getModifier(Drug.MOTION_BLUR)
                : 0;
    }

    @Override
    public void render(GuiGraphics context, Window window, float tickDelta) {

        if (Minecraft.getInstance().player == null) {
            return;
        }

        int screenWidth = window.getGuiScaledWidth();
        int screenHeight = window.getGuiScaledHeight();

        if (motionBlur > 0) {
            if (textures != null && textures.sizeChanged(screenWidth, screenHeight)) {
                close();
            }

            if (textures == null) {
                textures = new GlTextureSet(MAX_SAMPLES, screenWidth, screenHeight);
            }

            tickDelta += Minecraft.getInstance().player.tickCount;

            if (previousTicks > tickDelta) {
                previousTicks = tickDelta;
            } else if (previousTicks + SAMPLE_FREQUENCY * MAX_SAMPLES < tickDelta) {
                previousTicks = tickDelta - SAMPLE_FREQUENCY * MAX_SAMPLES;
            }

            while (previousTicks + SAMPLE_FREQUENCY <= tickDelta) {
                currentSample++;
                currentSample %= MAX_SAMPLES;
                textures.getTexture(currentSample).sample();
                previousTicks += SAMPLE_FREQUENCY;
            }

            textures.drawToScreen(context, currentSample);
        } else if (textures != null) {
            currentSample++;
            currentSample %= MAX_SAMPLES;
            textures.getTexture(currentSample).reset();
        }
    }

    @Override
    public void close() {
        if (textures != null) {
            textures.close();
            textures = null;
        }
    }

    private class GlTextureSet implements AutoCloseable {
        private final int width;
        private final int height;
        private final List<GlTexture> textures;

        public GlTextureSet(int samples, int width, int height) {
            this.width = width;
            this.height = height;
            textures = IntStream.range(1, samples + 1)
                    .mapToObj(i -> new GlTexture(i, width, height))
                    .toList();
        }

        public GlTexture getTexture(int sample) {
            return textures.get(sample);
        }

        public boolean sizeChanged(int width, int height) {
            return this.width != width || this.height != height;
        }

        public void drawToScreen(GuiGraphics context, int currentSample) {
            for (int i = 0; i < textures.size(); i++) {
                textures.get((i + currentSample) % textures.size()).drawToScreen(context);
            }
        }

        @Override
        public void close() {
            textures.forEach(GlTexture::close);
        }
    }

    private class GlTexture implements AutoCloseable {

        private final TextureTarget output;

        private final int sample;

        private boolean prepared;

        public GlTexture(int sample, int width, int height) {
            this.sample = sample;
            output = new TextureTarget(width, height, true, Minecraft.ON_OSX);
            output.viewWidth = width;
            output.viewHeight = height;
        }

        public void sample() {
            RenderTarget input = Minecraft.getInstance().getMainRenderTarget();
            // TODO: https://en.wikibooks.org/wiki/OpenGL_Programming/Motion_Blur

            GlStateManager._glBindFramebuffer(GlConst.GL_READ_FRAMEBUFFER, input.frameBufferId);
            GlStateManager._glBindFramebuffer(GlConst.GL_DRAW_FRAMEBUFFER, output.frameBufferId);
            GlStateManager._glBlitFrameBuffer(0, 0, input.width, input.height, 0, 0, output.width, output.height, GL11.GL_COLOR_BUFFER_BIT, GlConst.GL_NEAREST);
            GlStateManager._glBindFramebuffer(GlConst.GL_FRAMEBUFFER, 0);

            input.bindWrite(true);

            prepared = true;
        }

        public void reset() {
            prepared = false;
        }

        public void drawToScreen(GuiGraphics context) {
            float alpha = Math.min(1, sample * 0.002f * motionBlur);

            if (prepared && alpha > 0) {
                RenderUtil.drawBuffer(output, 1, 1, 1, alpha);
            }
        }

        @Override
        public void close() {
            output.destroyBuffers();
        }
    }
}
