package ivorius.psychedelicraft.util;

import org.joml.Vector3d;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.joml.Vector4fc;

import it.unimi.dsi.fastutil.doubles.Double2DoubleFunction;
import it.unimi.dsi.fastutil.floats.Float2FloatFunction;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

public interface MathUtils {
    Vector4f ZERO = new Vector4f(0, 0, 0, 0);
    Vector4f TEMP_VECTOR = new Vector4f();

    static float nearValue(float from, float to, float delta, float adjustmentRate) {
        return approach(Mth.lerp(delta, from, to), to, adjustmentRate);
    }

    static float approach(float value, float target, float adjustmentRate) {
        if (value > target) {
            return Math.max(value - adjustmentRate, target);
        }

        if (value < target) {
            return Math.min(value + adjustmentRate, target);
        }

        return value;
    }

    static double nearValue(double from, double to, double delta, double adjustmentRate) {
        return approach(Mth.lerp(delta, from, to), to, adjustmentRate);
    }

    static double approach(double value, double target, double adjustmentRate) {
        if (value > target) {
            return Math.max(value - adjustmentRate, target);
        }

        if (value < target) {
            return Math.min(value + adjustmentRate, target);
        }

        return value;
    }

    static Vector3f unpackRgb(int c) {
        return new Vector3f(r(c), g(c), b(c));
    }

    static Vector4f unpackArgb(int c) {
        return new Vector4f(a(c), r(c), g(c), b(c));
    }

    static float a(int c) {
        return FastColor.ARGB32.alpha(c) / 255F;
    }

    static float r(int c) {
        return FastColor.ARGB32.red(c) / 255F;
    }

    static float g(int c) {
        return FastColor.ARGB32.green(c) / 255F;
    }

    static float b(int c) {
        return FastColor.ARGB32.blue(c) / 255F;
    }

    static int withAlpha(int color, float alpha) {
        return (color & 0xFFFFFF) | ((int)(alpha * 255) & 0xFF) << 24;
    }

    static int mixColors(int left, int right, float progress) {
        return FastColor.ARGB32.colorFromFloat(
                Mth.lerp(a(left), a(right), progress),
                Mth.lerp(r(left), r(right), progress),
                Mth.lerp(g(left), g(right), progress),
                Mth.lerp(b(left), b(right), progress)
        );
    }

    @Deprecated
    static int packArgb(float a, float r, float g, float b) {
        return FastColor.ARGB32.colorFromFloat(a, r, g, b);
    }

    static Vector4fc mixColorsDynamic(Vector3fc color, Vector4f colorBase, float alpha) {
        if (alpha > 0) {
            float max = alpha + colorBase.w;
            return colorBase.set(
                    Mth.lerp(alpha / max, colorBase.x, color.x()),
                    Mth.lerp(alpha / max, colorBase.y, color.y()),
                    Mth.lerp(alpha / max, colorBase.z, color.z()),
                    Mth.clamp(max, 0, 1)
            );
        }
        return colorBase;
    }

    static Vector3f lerp(float delta, Vector3f a, Vector3fc b) {
        return a.set(
                Mth.lerp(delta, a.x(), b.x()),
                Mth.lerp(delta, a.y(), b.y()),
                Mth.lerp(delta, a.z(), b.z())
        );
    }

    static Vector3f lerp(float delta, Vector3fc a, Vector3fc b, Vector3f out) {
        return out.set(
                Mth.lerp(delta, a.x(), b.x()),
                Mth.lerp(delta, a.y(), b.y()),
                Mth.lerp(delta, a.z(), b.z())
        );
    }

    static float randomColor(RandomSource random, int ticksExisted, float base, float sway, float... speed) {
        for (float s : speed) {
            base *= 1.0f + Mth.sin(ticksExisted * s) * sway;
        }
        return base;
    }

    static float progress(float metric, float delta) {
        return 1 - (delta / (1 + metric));
    }

    static float progress(float metric) {
        return progress(metric, 1F);
    }

    static float mixEaseInOut(float v1, float v2, float delta) {
        return cubicMix(v1, v1, v2, v2, delta);
    }

    static float easeZeroToOne(float delta) {
        return cubicMix(0, 0, 1, 1, Mth.clamp(delta, 0, 1));
    }

    /**
     * Maps a value into a 0-1 range based on where it lies between a given min and max.
     */
    static float project(float value, float min, float max) {
        return Mth.clamp(Mth.inverseLerp(value, min, max), 0, 1);
    }

    static Vector3d cubicMix(Vector3d v1, Vector3d v2, Vector3d v3, Vector3d v4, double delta, Vector3d dest) {
        return dest.set(
            cubicMix(v1.x, v2.x, v3.x, v4.x, delta),
            cubicMix(v1.y, v2.y, v3.y, v4.y, delta),
            cubicMix(v1.z, v2.z, v3.z, v4.z, delta)
        );
    }

    static float cubicMix(float v1, float v2, float v3, float v4, float delta) {
        return (float)Mth.lerp3(delta, delta, delta, v1, v2, v2, v3, v2, v3, v3, v4);
    }

    static double cubicMix(double v1, double v2, double v3, double v4, double delta) {
        return Mth.lerp3(delta, delta, delta, v1, v2, v2, v3, v2, v3, v3, v4);
    }

    static Vector3f apply(Vector3f vector, Float2FloatFunction function) {
        return vector.set(
                function.get(vector.x),
                function.get(vector.y),
                function.get(vector.z)
        );
    }

    static Vector4f apply(Vector4f vector, Float2FloatFunction function) {
        return vector.set(
                function.get(vector.x),
                function.get(vector.y),
                function.get(vector.z),
                function.get(vector.w)
        );
    }

    static Vector3d apply(Vector3d vector, Double2DoubleFunction function) {
        return new Vector3d(
                function.applyAsDouble(vector.x),
                function.applyAsDouble(vector.y),
                function.applyAsDouble(vector.z)
        );
    }
}
