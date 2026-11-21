package dev.craft.mffs.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Data of a link card ({@code ItemCard}, spec 48 §6.2): the target block, its device ID (power link: {@code
 * CapacitorID}), the name shown as "Linkto" ({@code Areaname}) and whether the card is still valid.
 */
public record CardLink(GlobalPos target, int id, String name, boolean valid) {
    public static final Codec<CardLink> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            GlobalPos.CODEC.fieldOf("target").forGetter(CardLink::target),
            Codec.INT.fieldOf("id").forGetter(CardLink::id),
            Codec.STRING.fieldOf("name").forGetter(CardLink::name),
            Codec.BOOL.fieldOf("valid").forGetter(CardLink::valid)).apply(instance, CardLink::new));
    public static final StreamCodec<ByteBuf, CardLink> STREAM_CODEC = StreamCodec.composite(
            GlobalPos.STREAM_CODEC, CardLink::target,
            ByteBufCodecs.INT, CardLink::id,
            ByteBufCodecs.STRING_UTF8, CardLink::name,
            ByteBufCodecs.BOOL, CardLink::valid,
            CardLink::new);

    public CardLink withName(String newName) {
        return new CardLink(target, id, newName, valid);
    }

    public CardLink withTarget(GlobalPos newTarget) {
        return new CardLink(newTarget, id, name, valid);
    }

    public CardLink invalid() {
        return new CardLink(target, id, name, false);
    }
}
