package ivorius.psychedelicraft.client.render;

import static org.lwjgl.opengl.GL11.GL_BLEND;

import org.lwjgl.opengl.*;

import com.mojang.blaze3d.platform.GlConst;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

/**
 * Created by lukas on 16.11.14.
 * Updated by Sollace on 4 Jan 2023
 */
public class GLStateProxy {
    public static final int DEFAULT_TEXTURE = GlConst.GL_TEXTURE0;
    public static final int LIGHTMAP_TEXTURE = GlConst.GL_TEXTURE1;

    private static final TextureManager TEXURE_MANAGER = Minecraft.getInstance().getTextureManager();

    private static boolean usesScreenTexCoords;
    private static boolean resolutionSet;
    private static final float[] resolution = new float[2];

    public static boolean isColorSafeMode() {
        RenderSystem.assertOnRenderThread();
        return (GL11.glIsEnabled(GL_BLEND) && getBlendDFactor() != GL11.GL_ONE_MINUS_SRC_ALPHA);
    }

    private static int getBlendDFactor() {
        // XXXX: (Sollace) accessor to GlStateManager.BLEND.dstFactorRGB;
        RenderSystem.assertOnRenderThread();
        return GL14.glGetInteger(GL14.GL_BLEND_DST_RGB);
    }

    public static void setResolution(float width, float height) {
        if (!RenderSystem.isOnRenderThread()) {
            RenderSystem.recordRenderCall(() -> _setResolution(width, height));
        } else {
            _setResolution(width, height);
        }
    }

    private static void _setResolution(float width, float height) {
        resolution[0] = 1F / width;
        resolution[1] = 1F / height;
        resolutionSet = true;
    }

    public static float[] getResolution() {
        RenderSystem.assertOnRenderThread();
        if (!resolutionSet) {
            Window window = Minecraft.getInstance().getWindow();
            resolution[0] = 1F / window.getWidth();
            resolution[1] = 1F / window.getHeight();
        }
        return resolution;
    }

    public static void clearResolution() {
        RenderSystem.assertOnRenderThread();
        resolutionSet = false;
    }

    public static void enableTexCoords() {
        RenderSystem.assertOnRenderThread();
        usesScreenTexCoords = true;
    }

    public static void disableScreenTexCoords() {
        RenderSystem.assertOnRenderThread();
        usesScreenTexCoords = false;
    }

    public static boolean getUsesScreenTexCoords() {
        return usesScreenTexCoords;
    }

    public static int getTextureId(ResourceLocation texture) {
        RenderSystem.assertOnRenderThread();
        TEXURE_MANAGER.bindForSetup(texture); // Allocate texture. MOJANG!
        return TEXURE_MANAGER.getTexture(texture).getId();
    }

    public enum ShadeMode {
        FLAT,
        SMOOTH
    }
}
