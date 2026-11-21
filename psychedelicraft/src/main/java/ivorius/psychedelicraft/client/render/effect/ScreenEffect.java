package ivorius.psychedelicraft.client.render.effect;

import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import net.minecraft.client.gui.GuiGraphics;

public interface ScreenEffect extends AutoCloseable {
    default boolean shouldApply(float tickDelta) {
        return true;
    }

    void update(float tickDelta);

    void render(GuiGraphics context, Window window, float tickDelta);

    @Deprecated
    static void drawScreen(int screenWidth, int screenHeight) {
        BufferBuilder buffer = Tesselator.getInstance().begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        buffer.addVertex(0, 0, 0).setUv(0, 1)
              .addVertex(0, screenHeight, 0).setUv(0, 0)
              .addVertex(screenWidth, screenHeight, 0).setUv(1, 0)
              .addVertex(screenWidth, 0, 0).setUv(1, 1);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    interface PingPong {
        void pingPong();
    }
}
