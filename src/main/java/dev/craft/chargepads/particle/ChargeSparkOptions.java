package dev.craft.chargepads.particle;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/** Coloured charge pad spark (spec 47 §5.1, original particle {@code j}): colour and base lifetime. */
public record ChargeSparkOptions(float red, float green, float blue, int lifetime) implements ParticleOptions {
    public static final MapCodec<ChargeSparkOptions> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.FLOAT.fieldOf("red").forGetter(ChargeSparkOptions::red),
                    Codec.FLOAT.fieldOf("green").forGetter(ChargeSparkOptions::green),
                    Codec.FLOAT.fieldOf("blue").forGetter(ChargeSparkOptions::blue),
                    Codec.INT.fieldOf("lifetime").forGetter(ChargeSparkOptions::lifetime))
            .apply(instance, ChargeSparkOptions::new));
    public static final StreamCodec<ByteBuf, ChargeSparkOptions> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, ChargeSparkOptions::red,
            ByteBufCodecs.FLOAT, ChargeSparkOptions::green,
            ByteBufCodecs.FLOAT, ChargeSparkOptions::blue,
            ByteBufCodecs.VAR_INT, ChargeSparkOptions::lifetime,
            ChargeSparkOptions::new);

    @Override
    public ParticleType<?> getType() {
        return CpParticles.CHARGE_SPARK.get();
    }
}
