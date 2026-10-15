package dev.craft.ic2.keyboard;

import dev.craft.Craft;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

/** Client → server: the IC2 key bit mask (see {@link Ic2Keyboard}). */
public record KeyStatePayload(int keyState) implements CustomPacketPayload {
    public static final Type<KeyStatePayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "key_state"));
    public static final StreamCodec<ByteBuf, KeyStatePayload> STREAM_CODEC =
            ByteBufCodecs.VAR_INT.map(KeyStatePayload::new, KeyStatePayload::keyState);

    @Override
    public Type<KeyStatePayload> type() {
        return TYPE;
    }
}
