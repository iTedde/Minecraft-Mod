package dev.craft.lightshield;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;

/**
 * Flashlight setting of the light shield (spec 51 §2): range and cone width, each as a stage 1–4.
 * Range 16/32/48/64 blocks, full cone angle 15/30/45/60°, cost range × width EU every 2 ticks.
 */
public record Flashlight(int range, int width) {
    public static final int MIN_STAGE = 1;
    public static final int MAX_STAGE = 4;
    public static final Flashlight DEFAULT = new Flashlight(2, 2);
    /** Light costs {@link #euPerInterval()} every this many ticks. */
    public static final int COST_INTERVAL = 2;

    public static final Codec<Flashlight> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("range").forGetter(Flashlight::range),
            Codec.INT.fieldOf("width").forGetter(Flashlight::width)).apply(instance, Flashlight::of));
    public static final StreamCodec<ByteBuf, Flashlight> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, Flashlight::range, ByteBufCodecs.VAR_INT, Flashlight::width, Flashlight::of);

    /** Clamped to the valid stages. */
    public static Flashlight of(int range, int width) {
        return new Flashlight(Mth.clamp(range, MIN_STAGE, MAX_STAGE), Mth.clamp(width, MIN_STAGE, MAX_STAGE));
    }

    public int rangeBlocks() {
        return 16 * range;
    }

    /** Full opening angle in degrees. */
    public int widthDegrees() {
        return 15 * width;
    }

    public int euPerInterval() {
        return range * width;
    }

    public double euPerTick() {
        return euPerInterval() / (double) COST_INTERVAL;
    }

    /** "0.5", "2", "8" – without a trailing ".0". */
    public String euPerTickText() {
        return euPerInterval() % COST_INTERVAL == 0 ? Integer.toString(euPerInterval() / COST_INTERVAL)
                : euPerInterval() / COST_INTERVAL + "." + 5;
    }

    /** Ticks {@code charge} EU last with this setting. */
    public long runtimeTicks(int charge) {
        return (long) charge * COST_INTERVAL / euPerInterval();
    }
}
