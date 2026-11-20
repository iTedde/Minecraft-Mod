/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.client.render;

import org.joml.Vector2f;

import ivorius.psychedelicraft.client.render.shader.ShaderContext;
import ivorius.psychedelicraft.entity.drug.Drug;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.util.SmoothDouble;

/**
 * Created by lukas on 23.02.14.
 * Updated by Sollace on 4 Jan 2023
 */
public class SmoothCameraHelper {
    public static final SmoothCameraHelper INSTANCE = new SmoothCameraHelper();

    private final SmoothDouble xSmoother = new SmoothDouble();
    private final SmoothDouble ySmoother = new SmoothDouble();

    private float lastTickDelta;

    private Vector2f cursorDelta = new Vector2f();

    private Vector2f smoothedCursor = new Vector2f();
    private Vector2f prevCursorDelta = new Vector2f();

    public void setCursorDelta(float deltaX, float deltaY) {
        prevCursorDelta.x = deltaX;
        prevCursorDelta.y = deltaY;
    }

    public void applyCameraChange() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isWindowActive() && !mc.isPaused() && mc.player != null) {
            DrugProperties properties = DrugProperties.of(mc.player);
            if (properties.getModifier(Drug.HEAD_MOTION_INERTNESS) > 0) {
                Vector2f angles = getAngles();

                if (!mc.options.smoothCamera) {
                    angles.sub(getOriginalAngles());
                }

                mc.player.turn(angles.x, angles.y);
            }
        }
    }

    public void tick(DrugProperties properties) {
        float multiplier = 1 - Mth.clamp(properties.getModifier(Drug.HEAD_MOTION_INERTNESS), 0, 1) * 0.9F;
        float speed = getSpeed();
        smoothedCursor.set(
                (float)xSmoother.getNewDeltaValue(cursorDelta.x, multiplier * speed),
                (float)ySmoother.getNewDeltaValue(cursorDelta.y, multiplier * speed)
        );
        lastTickDelta = 0;
        cursorDelta.set(0, 0);
    }

    private Vector2f getAngles() {
        float speed = getSpeed();
        cursorDelta.add(prevCursorDelta.x * speed, prevCursorDelta.y * speed);

        float tickDelta = ShaderContext.tickDelta();
        float progress = tickDelta - lastTickDelta;
        lastTickDelta = tickDelta;

        return smoothedCursor.mul(progress, progress * getYSignum(), new Vector2f());
    }

    private Vector2f getOriginalAngles() {
        float speed = getSpeed();
        return prevCursorDelta.mul(speed, speed * getYSignum(), new Vector2f());
    }

    private float getYSignum() {
        return Minecraft.getInstance().options.invertYMouse().get() ? -1 : 1;
    }

    private float getSpeed() {
        float sensitivity = Minecraft.getInstance().options.sensitivity().get().floatValue() * 0.6F + 0.2F;
        sensitivity = sensitivity * sensitivity * sensitivity;
        if (!Minecraft.getInstance().player.isScoping()) {
            sensitivity *= 8;
        }
        return sensitivity;
    }
}
