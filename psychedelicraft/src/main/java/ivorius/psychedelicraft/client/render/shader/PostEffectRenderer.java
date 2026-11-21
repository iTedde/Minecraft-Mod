package ivorius.psychedelicraft.client.render.shader;

import java.util.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.PostChain;
import com.mojang.blaze3d.systems.RenderSystem;

import ivorius.psychedelicraft.client.PsychedelicraftClient;

public class PostEffectRenderer {
    private List<LoadedShader> shaders = new ArrayList<>();

    public void render(float tickDelta) {
        if (PsychedelicraftClient.getConfig().shader2DEnabled.get()) {
            RenderSystem.enableDepthTest();

            if (shaders.size() == 1) {
                shaders.get(0).process(tickDelta);
            } else {
                shaders.forEach(shader -> shader.process(tickDelta));
            }

            Minecraft.getInstance().getMainRenderTarget().bindWrite(true);
            RenderSystem.disableDepthTest();
        }
    }

    public void setupDimensions(int width, int height) {
        shaders.forEach(shader -> shader.resize(width, height));
    }

    public void onShadersLoaded(List<LoadedShader> shaders) {
        List<LoadedShader> oldShaders = this.shaders;
        this.shaders = shaders;
        oldShaders.forEach(PostChain::close);
    }
}
