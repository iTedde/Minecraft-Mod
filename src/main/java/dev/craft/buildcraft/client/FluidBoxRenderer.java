package dev.craft.buildcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.inventory.InventoryMenu;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Liquid boxes of {@code RenderTank} and {@code RenderRefinery}: one box with the still texture of the liquid, the
 * texture cut to the box bounds like a 1.4.7 block face, drawn without shading.
 */
final class FluidBoxRenderer {
    private FluidBoxRenderer() {
    }

    /** Liquid level of the 100 display lists: {@code (int)(amount / capacity * 99) / 100}. */
    static float level(int amount, int capacity) {
        return (int) ((float) amount / capacity * 99.0F) / 100.0F;
    }

    static void render(PoseStack pose, MultiBufferSource buffers, FluidStack fluid, float x0, float y0, float z0,
                       float x1, float y1, float z1, int light) {
        if (fluid.isEmpty() || y1 <= y0) {
            return;
        }
        IClientFluidTypeExtensions extensions = IClientFluidTypeExtensions.of(fluid.getFluid());
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS)
                .apply(extensions.getStillTexture(fluid));
        int color = extensions.getTintColor(fluid);
        VertexConsumer c = buffers.getBuffer(RenderType.entityTranslucent(InventoryMenu.BLOCK_ATLAS));
        PoseStack.Pose last = pose.last();
        float u0 = sprite.getU(x0);
        float u1 = sprite.getU(x1);
        float vz0 = sprite.getV(z0);
        float vz1 = sprite.getV(z1);
        float uz0 = sprite.getU(z0);
        float uz1 = sprite.getU(z1);
        float vTop = sprite.getV(1.0F - y1);
        float vBottom = sprite.getV(1.0F - y0);
        // bottom, top
        quad(c, last, color, light, x0, y0, z1, u0, vz1, x0, y0, z0, u0, vz0, x1, y0, z0, u1, vz0, x1, y0, z1, u1, vz1);
        quad(c, last, color, light, x1, y1, z1, u1, vz1, x1, y1, z0, u1, vz0, x0, y1, z0, u0, vz0, x0, y1, z1, u0, vz1);
        // north, south
        quad(c, last, color, light, x0, y1, z0, u0, vTop, x1, y1, z0, u1, vTop, x1, y0, z0, u1, vBottom,
                x0, y0, z0, u0, vBottom);
        quad(c, last, color, light, x0, y1, z1, u0, vTop, x0, y0, z1, u0, vBottom, x1, y0, z1, u1, vBottom,
                x1, y1, z1, u1, vTop);
        // west, east
        quad(c, last, color, light, x0, y1, z1, uz1, vTop, x0, y1, z0, uz0, vTop, x0, y0, z0, uz0, vBottom,
                x0, y0, z1, uz1, vBottom);
        quad(c, last, color, light, x1, y0, z1, uz1, vBottom, x1, y0, z0, uz0, vBottom, x1, y1, z0, uz0, vTop,
                x1, y1, z1, uz1, vTop);
    }

    private static void quad(VertexConsumer c, PoseStack.Pose pose, int color, int light,
                             float ax, float ay, float az, float au, float av,
                             float bx, float by, float bz, float bu, float bv,
                             float cx, float cy, float cz, float cu, float cv,
                             float dx, float dy, float dz, float du, float dv) {
        vertex(c, pose, color, light, ax, ay, az, au, av);
        vertex(c, pose, color, light, bx, by, bz, bu, bv);
        vertex(c, pose, color, light, cx, cy, cz, cu, cv);
        vertex(c, pose, color, light, dx, dy, dz, du, dv);
    }

    private static void vertex(VertexConsumer c, PoseStack.Pose pose, int color, int light, float x, float y,
                               float z, float u, float v) {
        c.addVertex(pose, x, y, z).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
