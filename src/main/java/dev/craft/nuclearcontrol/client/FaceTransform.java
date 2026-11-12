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

    /** Reading direction (right on the display). */
    static Direction reading(Direction facing) {
        return switch (facing) {
            case UP, DOWN, SOUTH -> Direction.EAST;
            case NORTH -> Direction.WEST;
            case WEST -> Direction.SOUTH;
            case EAST -> Direction.NORTH;
        };
    }

    /** "Up" on the display. */
    static Direction up(Direction facing) {
        return switch (facing) {
            case UP -> Direction.NORTH;
            case DOWN -> Direction.SOUTH;
            default -> Direction.UP;
        };
    }

    static void apply(PoseStack poseStack, Direction facing) {
        Vector3f reading = reading(facing).step();
        Vector3f normal = facing.step();
        Vector3f down = up(facing).step().negate();
        Matrix3f rotation = new Matrix3f(reading, normal, down);
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.last().pose().mul(new Matrix4f(rotation));
        poseStack.last().normal().mul(rotation);
    }
}
