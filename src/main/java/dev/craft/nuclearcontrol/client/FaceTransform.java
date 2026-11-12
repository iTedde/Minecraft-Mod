package dev.craft.nuclearcontrol.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.core.Direction;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Turns the pose so that, around the block centre, local +y is the display normal ({@code facing}), +x the reading
 * direction and +z "down" on the display – the orientation of a monitor lying on the floor in the original renderers.
 * Displays on walls read upright, displays on the ceiling with "up" to the south.
 */
final class FaceTransform {
    private FaceTransform() {
    }

    static void apply(PoseStack poseStack, Direction facing) {
        Vector3f reading;
        Vector3f textUp;
        switch (facing) {
            case UP -> {
                reading = new Vector3f(1, 0, 0);
                textUp = new Vector3f(0, 0, -1);
            }
            case DOWN -> {
                reading = new Vector3f(1, 0, 0);
                textUp = new Vector3f(0, 0, 1);
            }
            case NORTH -> {
                reading = new Vector3f(-1, 0, 0);
                textUp = new Vector3f(0, 1, 0);
            }
            case SOUTH -> {
                reading = new Vector3f(1, 0, 0);
                textUp = new Vector3f(0, 1, 0);
            }
            case WEST -> {
                reading = new Vector3f(0, 0, 1);
                textUp = new Vector3f(0, 1, 0);
            }
            default -> {
                reading = new Vector3f(0, 0, -1);
                textUp = new Vector3f(0, 1, 0);
            }
        }
        Vector3f normal = new Vector3f(facing.getStepX(), facing.getStepY(), facing.getStepZ());
        Vector3f down = textUp.negate(new Vector3f());
        Matrix3f rotation = new Matrix3f(reading, normal, down);
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.last().pose().mul(new Matrix4f(rotation));
        poseStack.last().normal().mul(rotation);
    }
}
