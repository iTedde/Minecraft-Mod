package ivorius.psychedelicraft.client.render.effect;

import java.util.Random;
import java.util.stream.IntStream;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.Window;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat.Mode;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.entity.drug.*;
import ivorius.psychedelicraft.entity.drug.type.PowerDrug;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

public class PowerOverlayScreenEffect extends DrugOverlayScreenEffect<PowerDrug> {
    private static final ResourceLocation POWER_PARTICLE_TEXTURE = Psychedelicraft.id("textures/drug/power/particle.png");
    private static final ResourceLocation[] LIGHTNING_TEXTURES = IntStream.range(0, 4)
            .mapToObj(i -> Psychedelicraft.id("textures/drug/power/lightning_" + i + ".png"))
            .toArray(ResourceLocation[]::new);

    public PowerOverlayScreenEffect() {
        super(DrugType.POWER);
    }

    @Override
    protected void render(GuiGraphics context, Window window, float tickDelta, DrugProperties properties, PowerDrug drug) {

        Player entity = properties.asEntity();

        int width = window.getGuiScaledWidth();
        int height = window.getGuiScaledHeight();

        float power = (float)drug.getActiveValue();
        Random powerR = new Random(entity.tickCount); // 20 changes / sec is alright
        int powerParticles = Mth.floor(powerR.nextFloat() * 200.0f * power);
        if (powerParticles > 0) {
            renderRandomParticles(context, powerParticles, width / 10, Mth.ceil(height / 10 * power), width, height, powerR);
        }

        Random powerLR = new Random(entity.tickCount / 2 * 21124871824l); // Chaos principle doesn't apply ;_;
        float lightningChance = (power - 0.5f) * 0.1f;
        int powerLightnings = 0;
        while (powerLR.nextFloat() < lightningChance && powerLightnings < 3) {
            powerLightnings++;
        }

        if (powerLightnings > 0) {
            int lightningW = height;

            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.blendFuncSeparate(
                    GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE,
                    GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO
            );
            Tesselator tessellator = Tesselator.getInstance();

            for (int i = 0; i < powerLightnings; i++) {
                float lX = powerLR.nextInt(width + lightningW) - lightningW;
                lX += (powerLR.nextFloat() - 0.5f) * lightningW * tickDelta * 2.0f;
                int lIndex = powerLR.nextInt(LIGHTNING_TEXTURES.length);
                boolean upsideDown = powerLR.nextBoolean();
                float lightningTime = ((entity.tickCount % 2) + tickDelta) * 0.5f;

                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, (0.05f + power * 0.1f) * (1.0f - lightningTime));
                RenderSystem.setShaderTexture(0, LIGHTNING_TEXTURES[lIndex]);

                BufferBuilder buffer = tessellator.begin(Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
                buffer.addVertex(lX, height, -90F).setUv(0, upsideDown ? 0 : 1)
                      .addVertex(lX + lightningW, height, -90F).setUv(1, upsideDown ? 0 : 1)
                      .addVertex(lX + lightningW, 0, -90F).setUv(1, upsideDown ? 1 : 0)
                      .addVertex(lX, 0, -90F).setUv(0, upsideDown ? 1 : 0);
                BufferUploader.drawWithShader(buffer.buildOrThrow());
            }
            RenderSystem.setShaderColor(1, 1, 1, 1);
            RenderSystem.defaultBlendFunc();
        }
    }

    private void renderRandomParticles(GuiGraphics context, int number, int width, int height, int screenWidth, int screenHeight, Random rand) {
        for (int i = 0; i < number; i++) {
            int x = rand.nextInt(screenWidth + width) - width;
            int y = rand.nextInt(screenHeight + height) - height;

            context.blit(POWER_PARTICLE_TEXTURE, x, y, -90, 0, 0, width, height, 16, 16);
        }
    }
}
