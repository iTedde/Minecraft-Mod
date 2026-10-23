package dev.craft.enderstorage;

import java.util.Optional;
import java.util.UUID;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;

/**
 * EnderStorage frequency (spec 27 §1): three wool colours {@code c1 << 8 | c2 << 4 | c3} and the owner, empty for
 * "global". The owner is kept as UUID with the name for display (deviation: the original keys by name).
 */
public record Frequency(int colours, Optional<UUID> owner, String ownerName) {
    public static final Frequency GLOBAL = new Frequency(0, Optional.empty(), "");

    public static final Codec<Frequency> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.intRange(0, 4095).fieldOf("colours").forGetter(Frequency::colours),
            UUIDUtil.CODEC.optionalFieldOf("owner").forGetter(Frequency::owner),
            Codec.STRING.optionalFieldOf("owner_name", "").forGetter(Frequency::ownerName)
    ).apply(instance, Frequency::new));

    public static final StreamCodec<ByteBuf, Frequency> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, Frequency::colours,
            UUIDUtil.STREAM_CODEC.apply(ByteBufCodecs::optional), Frequency::owner,
            ByteBufCodecs.STRING_UTF8, Frequency::ownerName,
            Frequency::new);

    /** {@code getFreqFromColours}. */
    public static Frequency of(int colour1, int colour2, int colour3) {
        return new Frequency((colour1 & 15) << 8 | (colour2 & 15) << 4 | colour3 & 15, Optional.empty(), "");
    }

    /** {@code getColourFromFreq}: wool colour 0–15 of button 0–2. */
    public int colour(int button) {
        return colours >> (8 - 4 * button) & 15;
    }

    public Frequency withColour(int button, int colour) {
        int shift = 8 - 4 * button;
        return new Frequency(colours & ~(15 << shift) | (colour & 15) << shift, owner, ownerName);
    }

    public boolean isPersonal() {
        return owner.isPresent();
    }

    public Frequency withOwner(Player player) {
        return new Frequency(colours, Optional.of(player.getUUID()), player.getGameProfile().getName());
    }

    public Frequency global() {
        return new Frequency(colours, Optional.empty(), "");
    }

    /** {@code EnderStorageManager.getStorage} key {@code "<freq>|<owner>|<type>"}. */
    public String key(String type) {
        return colours + "|" + owner.map(UUID::toString).orElse("global") + "|" + type;
    }

    public DyeColor dyeColour(int button) {
        return DyeColor.byId(colour(button));
    }

    public CompoundTag save() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("freq", colours);
        owner.ifPresent(uuid -> tag.putUUID("owner", uuid));
        if (!ownerName.isEmpty()) {
            tag.putString("owner_name", ownerName);
        }
        return tag;
    }

    public static Frequency load(CompoundTag tag) {
        Optional<UUID> owner = tag.hasUUID("owner") ? Optional.of(tag.getUUID("owner")) : Optional.empty();
        return new Frequency(tag.getInt("freq") & 4095, owner, tag.getString("owner_name"));
    }
}
