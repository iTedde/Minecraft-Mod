package dev.craft.buildcraft.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import dev.craft.Craft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * Draws BuildCraft {@code EntityBlock}s (lasers, arm beams) the way {@code RenderEntityBlock} does: the box is cut
 * into unit cells, each cell shows the matching part of a 16×16 sprite. Coordinates are relative to {@code origin}.
 */
final class BlockBoxRenderer {
    static final ResourceLocation LASER_RED = texture("laser_red");
    static final ResourceLocation LASER_BLUE = texture("laser_blue");
    static final ResourceLocation LASER_STRIPES = texture("laser_stripes");
    static final ResourceLocation DRILL = texture("drill");
    static final ResourceLocation DRILL_HEAD = texture("drill_head");

    private BlockBoxRenderer() {
    }

    private static ResourceLocation texture(String name) {
        return ResourceLocation.fromNamespaceAndPath(Craft.MODID, "textures/entity/quarry/" + name + ".png");
    }

    /** BuildCraft {@code Utils.createLaser}: a 0.1 thick beam from the centre of {@code from} to the centre of {@code to}. */
    static void laser(PoseStack pose, MultiBufferSource buffers, Level level, BlockPos origin, ResourceLocation texture,
                      int x1, int y1, int z1, int x2, int y2, int z2) {
        if (x1 == x2 && y1 == y2 && z1 == z2) {
            return;
        }
        double sx = x2 - x1;
        double sy = y2 - y1;
        double sz = z2 - z1;
        double x = x1;
        double y = y1;
        double z = z1;
        if (sx != 0.0) {
            x += 0.5;
            y += 0.45;
            z += 0.45;
            sy = 0.1;
            sz = 0.1;
        } else if (sy != 0.0) {
            x += 0.45;
            y += 0.5;
            z += 0.45;
            sx = 0.1;
            sz = 0.1;
        } else {
            x += 0.45;
            y += 0.45;
            z += 0.5;
            sx = 0.1;
            sy = 0.1;
        }
        box(pose, buffers, level, origin, texture, x, y, z, sx, sy, sz);
    }

    /** BuildCraft {@code createLaserBox}: the 12 edges between the block centres of two corners. */
    static void laserBox(PoseStack pose, MultiBufferSource buffers, Level level, BlockPos origin,
                         ResourceLocation texture, int xMin, int yMin, int zMin, int xMax, int yMax, int zMax) {
        int[][] p = {{xMin, yMin, zMin}, {xMax, yMin, zMin}, {xMin, yMax, zMin}, {xMax, yMax, zMin},
                {xMin, yMin, zMax}, {xMax, yMin, zMax}, {xMin, yMax, zMax}, {xMax, yMax, zMax}};
        int[][] edges = {{0, 1}, {0, 2}, {2, 3}, {1, 3}, {4, 5}, {4, 6}, {5, 7}, {6, 7}, {0, 4}, {1, 5}, {2, 6},
                {3, 7}};
        for (int[] edge : edges) {
            int[] a = p[edge[0]];
            int[] b = p[edge[1]];
            laser(pose, buffers, level, origin, texture, a[0], a[1], a[2], b[0], b[1], b[2]);
        }
    }

    /** An {@code EntityBlock} at world position (x, y, z) with the given size. */
    static void box(PoseStack pose, MultiBufferSource buffers, Level level, BlockPos origin, ResourceLocation texture,
                    double x, double y, double z, double sx, double sy, double sz) {
        if (sx <= 0.0 || sy <= 0.0 || sz <= 0.0) {
            return;
        }
        VertexConsumer consumer = buffers.getBuffer(RenderType.entityCutoutNoCull(texture));
        PoseStack.Pose last = pose.last();
        BlockPos.MutableBlockPos lightPos = new BlockPos.MutableBlockPos();
        for (int i = 0; i < sx; i++) {
            for (int j = 0; j < sy; j++) {
                for (int k = 0; k < sz; k++) {
                    float x0 = (float) (x - origin.getX() + i);
                    float y0 = (float) (y - origin.getY() + j);
                    float z0 = (float) (z - origin.getZ() + k);
                    float mx = (float) Math.min(sx - i, 1.0);
                    float my = (float) Math.min(sy - j, 1.0);
                    float mz = (float) Math.min(sz - k, 1.0);
                    lightPos.set(Math.floor(x) + i, Math.floor(y) + j, Math.floor(z) + k);
                    int light = LevelRenderer.getLightColor(level, lightPos);
                    cell(consumer, last, light, x0, y0, z0, mx, my, mz);
                }
            }
        }
    }

    /** One cell from (x0, y0, z0) with size (mx, my, mz) ≤ 1; the UVs take the same part of the sprite. */
    private static void cell(VertexConsumer c, PoseStack.Pose pose, int light, float x0, float y0, float z0,
                             float mx, float my, float mz) {
        float x1 = x0 + mx;
        float y1 = y0 + my;
        float z1 = z0 + mz;
        // bottom / top: u = x, v = z
        quad(c, pose, light, x0, y0, z1, 0, mz, x0, y0, z0, 0, 0, x1, y0, z0, mx, 0, x1, y0, z1, mx, mz);
        quad(c, pose, light, x1, y1, z1, mx, mz, x1, y1, z0, mx, 0, x0, y1, z0, 0, 0, x0, y1, z1, 0, mz);
        // north / south: u = x, v = y (top of the sprite at the top)
        quad(c, pose, light, x0, y1, z0, mx, 0, x1, y1, z0, 0, 0, x1, y0, z0, 0, my, x0, y0, z0, mx, my);
        quad(c, pose, light, x0, y1, z1, 0, 0, x0, y0, z1, 0, my, x1, y0, z1, mx, my, x1, y1, z1, mx, 0);
        // west / east: u = z, v = y
        quad(c, pose, light, x0, y1, z1, mz, 0, x0, y1, z0, 0, 0, x0, y0, z0, 0, my, x0, y0, z1, mz, my);
        quad(c, pose, light, x1, y0, z1, 0, my, x1, y0, z0, mz, my, x1, y1, z0, mz, 0, x1, y1, z1, 0, 0);
    }

    private static void quad(VertexConsumer c, PoseStack.Pose pose, int light,
                             float ax, float ay, float az, float au, float av,
                             float bx, float by, float bz, float bu, float bv,
                             float cx, float cy, float cz, float cu, float cv,
                             float dx, float dy, float dz, float du, float dv) {
        vertex(c, pose, light, ax, ay, az, au, av);
        vertex(c, pose, light, bx, by, bz, bu, bv);
        vertex(c, pose, light, cx, cy, cz, cu, cv);
        vertex(c, pose, light, dx, dy, dz, du, dv);
    }

    private static void vertex(VertexConsumer c, PoseStack.Pose pose, int light, float x, float y, float z, float u,
                               float v) {
        // The original draws with lighting disabled: one normal for every face gives the same flat shade.
        c.addVertex(pose, x, y, z).setColor(255, 255, 255, 255).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light).setNormal(pose, 0.0F, 1.0F, 0.0F);
    }
}
