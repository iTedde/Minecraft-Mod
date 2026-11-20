/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.client.render.effect.*;
import ivorius.psychedelicraft.client.render.shader.PostEffectRenderer;
import ivorius.psychedelicraft.client.render.shader.ShaderContext;
import ivorius.psychedelicraft.client.sound.ClientDrugMusicManager;
import ivorius.psychedelicraft.entity.drug.Drug;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.hallucination.DriftingCamera;
import ivorius.psychedelicraft.entity.drug.hallucination.Hallucination;
import ivorius.psychedelicraft.entity.drug.hallucination.HallucinationManager;
import java.lang.Math;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;

/**
 * Created by lukas on 17.02.14.
 */
public class DrugRenderer {
    public static final DrugRenderer INSTANCE = new DrugRenderer();

    private final EnvironmentalScreenEffect environmentalEffects = new EnvironmentalScreenEffect();
    private final ScreenEffect screenEffects = CompoundScreenEffect.of(
            new LensFlareScreenEffect(),
            new WarmthOverlayScreenEffect(),
            new AlcoholOverlayScreenEffect(),
            new PowerOverlayScreenEffect(),
            environmentalEffects,
            new TirednessScreenEffect(),
            new HangoverGlareScreenEffect(),
            new MotionBlurScreenEffect()
    );

    private final PostEffectRenderer postEffects = new PostEffectRenderer();

    private final ClientDrugMusicManager musicManager = new ClientDrugMusicManager();

    public ScreenEffect getScreenEffects() {
        return screenEffects;
    }

    public PostEffectRenderer getPostEffects() {
        return postEffects;
    }

    public EnvironmentalScreenEffect getEnvironmentalEffects() {
        return environmentalEffects;
    }

    public ClientDrugMusicManager getMusicManager() {
        return musicManager;
    }

    public void update(DrugProperties drugProperties, LivingEntity entity) {
        getScreenEffects().update(ShaderContext.tickDelta());
        musicManager.update(drugProperties);
    }

    public void distortScreen(PoseStack matrices, Camera camera, float tickDelta) {
        if (Minecraft.getInstance().player == null) {
            return;
        }

        DrugProperties properties = DrugProperties.of(Minecraft.getInstance().player);

        float wobblyness = Math.min(1, properties.getModifier(Drug.VIEW_WOBBLYNESS));

        float tick = properties.asEntity().tickCount + tickDelta;

        DriftingCamera driftingCam = properties.getHallucinations().getCamera();

        matrices.mulPose(Axis.XP.rotationDegrees(camera.getXRot()));
        matrices.mulPose(Axis.YP.rotationDegrees(camera.getYRot() + 180.0f));

        Vec3 cameraOffset = driftingCam.getPosition();
        Vec3 prevCameraOffset = driftingCam.getPrevPosition();
        matrices.translate(
             Mth.lerp(tickDelta, prevCameraOffset.x, cameraOffset.x),
             Mth.lerp(tickDelta, prevCameraOffset.y, cameraOffset.y),
             Mth.lerp(tickDelta, prevCameraOffset.z, cameraOffset.z)
        );
        Vec3 cameraRoll = driftingCam.getRotation();
        Vec3 prevCameraRoll = driftingCam.getPrevRotation();
        matrices.mulPose(new Quaternionf().rotateXYZ(
                (float)Mth.lerp(tickDelta, prevCameraRoll.x, cameraRoll.x),
                (float)Mth.lerp(tickDelta, prevCameraRoll.y, cameraRoll.y),
                (float)Mth.lerp(tickDelta, prevCameraRoll.z, cameraRoll.z)
        ));

        matrices.mulPose(Axis.YN.rotationDegrees(camera.getYRot() + 180.0f));
        matrices.mulPose(Axis.XN.rotationDegrees(camera.getXRot()));

        if (wobblyness > 0) {
            float f4 = Mth.square(5F / (wobblyness * wobblyness + 5F) - wobblyness * 0.04F);

            float sin1 = Mth.sin(tick / 150 * Mth.PI);
            float sin2 = Mth.sin(tick / 170 * Mth.PI);
            float sin3 = Mth.sin(tick / 190 * Mth.PI);

            float yz = tick * 3F * Mth.DEG_TO_RAD;
            Quaternionf rotation = new Quaternionf().rotateXYZ(0, yz, yz);
            matrices.mulPose(rotation);
            matrices.scale(
                    1F / (f4 + (wobblyness * sin1) / 2),
                    1F / (f4 + (wobblyness * sin2) / 2),
                    1F / (f4 + (wobblyness * sin3) / 2)
            );
            matrices.mulPose(rotation.invert());
        }

        matrices.translate(
                DrugEffectInterpreter.getCameraShiftX(properties, tick),
                DrugEffectInterpreter.getCameraShiftY(properties, tick),
                0
        );
    }

    public void distortHand(PoseStack matrices) {
        Minecraft client = Minecraft.getInstance();
        if (client == null) {
            return;
        }
        float ticks = ShaderContext.ticks();

        DrugProperties.of((Entity)client.player).ifPresent(drugProperties -> {
            float shiftX = DrugEffectInterpreter.getHandShiftX(drugProperties, ticks);
            float shiftY = DrugEffectInterpreter.getHandShiftY(drugProperties, ticks);
            matrices.translate(shiftX, shiftY, 0);
        });
    }

    public void poseModel(Player player, HumanoidModel<?> model) {
        ModelPart head = model.getHead();
        ModelPart leftArm = model.leftArm;
        ModelPart rightArm = model.rightArm;

        DrugProperties properties = DrugProperties.of(player);
        float tick = ShaderContext.ticks();
        float shiftX = DrugEffectInterpreter.getHandShiftX(properties, tick) * 2;

        leftArm.xRot += shiftX;
        rightArm.xRot -= shiftX;

        float shiftY = DrugEffectInterpreter.getHandShiftY(properties, tick);

        leftArm.zRot += shiftY;
        rightArm.zRot -= shiftY;

        head.xRot += DrugEffectInterpreter.getCameraShiftX(properties, tick);
        head.yRot += DrugEffectInterpreter.getCameraShiftY(properties, tick);
        head.zRot = DrugEffectInterpreter.getAlcohol(properties);

        if (model instanceof PlayerModel<?> pem) {
            pem.hat.copyFrom(head);
            pem.leftSleeve.copyFrom(leftArm);
            pem.rightSleeve.copyFrom(rightArm);
        }
    }

    public void onAfterRenderWorld() {
        Minecraft client = Minecraft.getInstance();
        RenderPhase.SCREEN.push();
        try {
            postEffects.render(client.getTimer().getGameTimeDeltaPartialTick(false));
        } finally {
            RenderPhase.pop();
        }
    }

    public void onRenderOverlay(GuiGraphics context, DeltaTracker tickCounter) {
        Minecraft client = Minecraft.getInstance();
        RenderPhase.SCREEN.push();
        try {
            getScreenEffects().render(context, client.getWindow(), tickCounter.getGameTimeDeltaPartialTick(false));
        } finally {
            RenderPhase.pop();
        }
    }

    public void renderAllHallucinations(PoseStack matrices, MultiBufferSource vertices, Camera camera, float tickDelta, DrugProperties drugProperties) {
        HallucinationManager hallucinations = drugProperties.getHallucinations();
        float alpha = Mth.clamp(hallucinations.getEntityHallucinationStrength() * 15, 0, 1);
        float forcedAlpha = hallucinations.getEntities().getForcedAlpha(tickDelta);
        if (forcedAlpha > 0) {
            alpha += forcedAlpha;
            alpha /= 2F;
        }

        for (Hallucination h : hallucinations.getEntities()) {
            try {
                h.render(matrices, vertices, camera, tickDelta, alpha);
            } catch (Throwable t) {
                Psychedelicraft.LOGGER.fatal("Exception occured whilst rendering hallucination ", t);
                h.setDead();
                // clean up the matrix stack after an error
                while (!matrices.clear()) {
                    matrices.popPose();
                }
            }
        }
    }

}
