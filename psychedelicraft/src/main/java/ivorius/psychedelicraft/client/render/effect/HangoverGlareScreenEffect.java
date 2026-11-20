package ivorius.psychedelicraft.client.render.effect;

import com.mojang.blaze3d.platform.Window;

import ivorius.psychedelicraft.entity.effect.PSEffects;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;

/**
 * Craft addition (spec 52 §6): from hangover level II on, daylight under the open sky glares –
 * a white overlay plus extra bloom through the original bloom shader. Shade is the remedy.
 */
public class HangoverGlareScreenEffect implements ScreenEffect {
    private static final float MAX_OVERLAY = 0.45F;
    private static final float MAX_BLOOM = 1.5F;

    private static float prevGlare;
    private static float glare;

    /**
     * @return additional bloom strength for the bloom shader
     */
    public static float getBloom(float tickDelta) {
        return Mth.lerp(tickDelta, prevGlare, glare) * MAX_BLOOM;
    }

    @Override
    public boolean shouldApply(float tickDelta) {
        return glare > 0 || prevGlare > 0;
    }

    @Override
    public void update(float tickDelta) {
        Player player = Minecraft.getInstance().player;
        if (player == null || Minecraft.getInstance().isPaused()) {
            return;
        }
        prevGlare = glare;
        glare = MathUtils.approach(glare, getTargetGlare(player), 0.02F);
    }

    private static float getTargetGlare(Player player) {
        MobEffectInstance hangover = player.getEffect(PSEffects.HANGOVER);
        if (hangover == null || hangover.getAmplifier() < 1 || !player.level().isDay()) {
            return 0;
        }
        BlockPos eyes = BlockPos.containing(player.getEyePosition());
        if (!player.level().canSeeSky(eyes)) {
            return 0;
        }
        // stronger at noon, weaker in rain; level III glares more than level II
        float sun = Mth.clamp(1 - player.level().getRainLevel(1) * 0.5F, 0, 1);
        return sun * (hangover.getAmplifier() >= 2 ? 1 : 0.7F);
    }

    @Override
    public void render(GuiGraphics context, Window window, float tickDelta) {
        float value = Mth.lerp(tickDelta, prevGlare, glare);
        if (value <= 0) {
            return;
        }
        int alpha = (int)(value * MAX_OVERLAY * 255);
        context.fill(0, 0, window.getGuiScaledWidth(), window.getGuiScaledHeight(), FastColor.ARGB32.color(alpha, 255, 255, 245));
    }

    @Override
    public void close() {
        prevGlare = 0;
        glare = 0;
    }
}
