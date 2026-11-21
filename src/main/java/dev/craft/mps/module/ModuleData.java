package dev.craft.mps.module;

import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * One installed module of an item (MPS module tag): {@code Active} and the slider values (0…1) by tradeoff name.
 * Immutable; changes return a copy.
 */
public record ModuleData(boolean active, Map<String, Double> tweaks) {
    public static final ModuleData NEW = new ModuleData(true, Map.of());

    public static final Codec<ModuleData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.BOOL.optionalFieldOf("active", true).forGetter(ModuleData::active),
            Codec.unboundedMap(Codec.STRING, Codec.DOUBLE).optionalFieldOf("tweaks", Map.of())
                    .forGetter(ModuleData::tweaks)
    ).apply(instance, ModuleData::new));

    public static final StreamCodec<ByteBuf, ModuleData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, ModuleData::active,
            ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8, ByteBufCodecs.DOUBLE), ModuleData::tweaks,
            ModuleData::new);

    public ModuleData {
        tweaks = Map.copyOf(tweaks);
    }

    /** {@code getDoubleOrZero(moduleTag, tradeoff)}. */
    public double tweak(String name) {
        return tweaks.getOrDefault(name, 0.0);
    }

    public ModuleData withActive(boolean value) {
        return new ModuleData(value, tweaks);
    }

    public ModuleData withTweak(String name, double value) {
        Map<String, Double> copy = new HashMap<>(tweaks);
        copy.put(name, value);
        return new ModuleData(active, copy);
    }
}
