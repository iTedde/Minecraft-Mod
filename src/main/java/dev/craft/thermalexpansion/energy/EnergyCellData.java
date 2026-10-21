package dev.craft.thermalexpansion.energy;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Item data of a dismantled Redstone Energy Cell (1.4.7 NBT {@code energy.stored}, {@code energy.send},
 * {@code energy.receive}, {@code red.disable}, {@code red.state}).
 */
public record EnergyCellData(float energy, int send, int receive, boolean redstoneDisable, boolean redstoneState) {
    /** {@code ItemBlockEnergyCell.placeBlockAt} without NBT: empty, send 50, receive 100, redstone low. */
    public static final EnergyCellData DEFAULT = new EnergyCellData(0.0F, EnergyCellBlockEntity.DEFAULT_SEND,
            EnergyCellBlockEntity.DEFAULT_RECEIVE, false, false);
    public static final EnergyCellData FULL = new EnergyCellData(EnergyCellBlockEntity.STORAGE,
            EnergyCellBlockEntity.DEFAULT_SEND, EnergyCellBlockEntity.DEFAULT_RECEIVE, false, false);

    public static final Codec<EnergyCellData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.FLOAT.fieldOf("energy").forGetter(EnergyCellData::energy),
            Codec.INT.fieldOf("send").forGetter(EnergyCellData::send),
            Codec.INT.fieldOf("receive").forGetter(EnergyCellData::receive),
            Codec.BOOL.fieldOf("redstone_disable").forGetter(EnergyCellData::redstoneDisable),
            Codec.BOOL.fieldOf("redstone_state").forGetter(EnergyCellData::redstoneState)
    ).apply(instance, EnergyCellData::new));

    public static final StreamCodec<ByteBuf, EnergyCellData> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT, EnergyCellData::energy,
            ByteBufCodecs.VAR_INT, EnergyCellData::send,
            ByteBufCodecs.VAR_INT, EnergyCellData::receive,
            ByteBufCodecs.BOOL, EnergyCellData::redstoneDisable,
            ByteBufCodecs.BOOL, EnergyCellData::redstoneState,
            EnergyCellData::new);

    /** {@code getScaledEnergyStored(150)}: the 1.4.7 item damage is 151 minus this. */
    public int scaledEnergy(int scale) {
        return (int) (energy * scale / EnergyCellBlockEntity.STORAGE);
    }
}
