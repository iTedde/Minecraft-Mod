package dev.craft.mffs.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * {@code ItemAccessCard}: validity in minutes, the station's device ID ({@code linkID}) and name ({@code Areaname}),
 * plus the game time of the last minute step (per item instead of the original's shared tick counter, spec 48 §14).
 */
public record AccessLicense(int validity, int stationId, String areaName, long lastStep) {
    public static final Codec<AccessLicense> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.fieldOf("validity").forGetter(AccessLicense::validity),
            Codec.INT.fieldOf("station_id").forGetter(AccessLicense::stationId),
            Codec.STRING.fieldOf("area_name").forGetter(AccessLicense::areaName),
            Codec.LONG.optionalFieldOf("last_step", 0L).forGetter(AccessLicense::lastStep))
            .apply(instance, AccessLicense::new));
    public static final StreamCodec<ByteBuf, AccessLicense> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, AccessLicense::validity,
            ByteBufCodecs.INT, AccessLicense::stationId,
            ByteBufCodecs.STRING_UTF8, AccessLicense::areaName,
            ByteBufCodecs.VAR_LONG, AccessLicense::lastStep,
            AccessLicense::new);

    public AccessLicense withValidity(int minutes) {
        return new AccessLicense(minutes, stationId, areaName, lastStep);
    }

    public AccessLicense withAreaName(String name) {
        return new AccessLicense(validity, stationId, name, lastStep);
    }

    public AccessLicense withLastStep(long time) {
        return new AccessLicense(validity, stationId, areaName, time);
    }
}
