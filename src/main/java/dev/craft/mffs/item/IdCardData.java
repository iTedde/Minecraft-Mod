package dev.craft.mffs.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.craft.mffs.security.SecurityRight;
import io.netty.buffer.ByteBuf;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

/**
 * Data of a personal ID card and an access license ({@code ItemCardPersonalID}: {@code name} and the {@code rights}
 * compound): owner name for the tooltip, owner UUID for the comparison (spec 48 §14), rights as a bit mask.
 */
public record IdCardData(String owner, Optional<UUID> ownerId, int rights) {
    public static final IdCardData EMPTY = new IdCardData("", Optional.empty(), 0);
    public static final Codec<IdCardData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.optionalFieldOf("owner", "").forGetter(IdCardData::owner),
            UUIDUtil.CODEC.optionalFieldOf("owner_id").forGetter(IdCardData::ownerId),
            Codec.INT.optionalFieldOf("rights", 0).forGetter(IdCardData::rights)).apply(instance, IdCardData::new));
    public static final StreamCodec<ByteBuf, IdCardData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, IdCardData::owner,
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), IdCardData::ownerId,
            ByteBufCodecs.VAR_INT, IdCardData::rights,
            IdCardData::new);

    public static IdCardData of(Player player) {
        return new IdCardData(player.getGameProfile().getName(), Optional.of(player.getUUID()), 0);
    }

    public boolean hasRight(SecurityRight right) {
        return (rights & right.mask()) != 0;
    }

    public IdCardData withRight(SecurityRight right, boolean value) {
        return new IdCardData(owner, ownerId, value ? rights | right.mask() : rights & ~right.mask());
    }

    /** Owner comparison over the UUID; cards without one (never written by a player) belong to nobody. */
    public boolean isOwner(Player player) {
        return ownerId.isPresent() && ownerId.get().equals(player.getUUID());
    }
}
