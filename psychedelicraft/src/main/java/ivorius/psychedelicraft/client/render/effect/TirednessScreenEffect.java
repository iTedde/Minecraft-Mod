/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render.effect;

import com.mojang.blaze3d.platform.Window;
import ivorius.psychedelicraft.PSSounds;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.render.RenderUtil;
import ivorius.psychedelicraft.entity.drug.Drug;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/**
 * @author Sollace
 * @since 19 April 2023
 */
public class TirednessScreenEffect implements ScreenEffect {
    private static final ResourceLocation EYELID_OVERLAY = Psychedelicraft.id("textures/environment/eyelid_overlay.png");

    private float prevOverlayOpacity;
    private float overlayOpacity;

    private int ticksBlinking;

    private long lastPlayTime;

    @Override
    public void update(float tickDelta) {

        Player entity = Minecraft.getInstance().player;
        if (entity == null || Minecraft.getInstance().isPaused()) {
            return;
        }
        float baseDrowsyness = Mth.clamp(DrugProperties.of(entity).getModifier(Drug.DROWSYNESS)
                + DrugProperties.of(entity).getCardiacArrestProgress(tickDelta), 0, 1);

        prevOverlayOpacity = overlayOpacity;

        float drowsyness = Math.max(0, baseDrowsyness - 0.6F);

        overlayOpacity = MathUtils.approach(overlayOpacity, ticksBlinking > 0 ? drowsyness * 0.9F + Mth.sin(entity.tickCount / 10F) : 0, 0.03F);
        if (drowsyness > 0.3F && overlayOpacity > 0.6F) {
            long now = System.currentTimeMillis();
            if (now > (lastPlayTime + 1000)) {
                lastPlayTime = now;
                entity.level().playLocalSound(entity.getX(), entity.getY(), entity.getZ(),
                    PSSounds.ENTITY_PLAYER_HEARTBEAT,
                    SoundSource.AMBIENT, drowsyness, 0.3F, false);
            }
        }

        if (drowsyness < 0.2F) {
            ticksBlinking = Math.max(ticksBlinking - 1, 0);
            return;
        }

        if (--ticksBlinking <= 0 && (ticksBlinking < -300 || entity.level().random.nextFloat() < baseDrowsyness)) {
            ticksBlinking = (int)entity.level().random.triangle(300, 200);
            entity.displayClientMessage(Component.literal("I really should get to bed..."), true);
        }
    }

    @Override
    public void render(GuiGraphics context, Window window, float tickDelta) {
        float opacity = Mth.lerp(tickDelta, prevOverlayOpacity, overlayOpacity);
        RenderUtil.drawOverlay(context, EYELID_OVERLAY, opacity * 0.8F, window.getGuiScaledWidth(), window.getGuiScaledHeight(), 0, 0, 1, 1, (int)(opacity * 5.8F));
    }

    @Override
    public void close() {

    }
}
