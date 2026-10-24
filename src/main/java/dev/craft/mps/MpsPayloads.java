package dev.craft.mps;

import dev.craft.Craft;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client → server requests of the Tinker Table GUI (spec 29 §12); slot = player inventory index. */
public final class MpsPayloads {
    private MpsPayloads() {
    }

    public record Install(int slot, String module) implements CustomPacketPayload {
        public static final Type<Install> TYPE = payloadType("mps_install");
        public static final StreamCodec<ByteBuf, Install> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Install::slot, ByteBufCodecs.stringUtf8(64), Install::module, Install::new);

        @Override
        public Type<Install> type() {
            return TYPE;
        }
    }

    public record Salvage(int slot, String module) implements CustomPacketPayload {
        public static final Type<Salvage> TYPE = payloadType("mps_salvage");
        public static final StreamCodec<ByteBuf, Salvage> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Salvage::slot, ByteBufCodecs.stringUtf8(64), Salvage::module, Salvage::new);

        @Override
        public Type<Salvage> type() {
            return TYPE;
        }
    }

    public record Tweak(int slot, String module, String tweak, double value) implements CustomPacketPayload {
        public static final Type<Tweak> TYPE = payloadType("mps_tweak");
        public static final StreamCodec<ByteBuf, Tweak> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Tweak::slot, ByteBufCodecs.stringUtf8(64), Tweak::module,
                ByteBufCodecs.stringUtf8(64), Tweak::tweak, ByteBufCodecs.DOUBLE, Tweak::value, Tweak::new);

        @Override
        public Type<Tweak> type() {
            return TYPE;
        }
    }

    private static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> payloadType(String name) {
        return new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(Craft.MODID, name));
    }

    static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToServer(Install.TYPE, Install.STREAM_CODEC,
                        (payload, context) -> TinkerActions.install(context.player(), payload.slot(),
                                payload.module()))
                .playToServer(Salvage.TYPE, Salvage.STREAM_CODEC,
                        (payload, context) -> TinkerActions.salvage(context.player(), payload.slot(),
                                payload.module(), context.player().getRandom()))
                .playToServer(Tweak.TYPE, Tweak.STREAM_CODEC, MpsPayloads::tweak);
    }

    private static void tweak(Tweak payload, IPayloadContext context) {
        TinkerActions.tweak(context.player(), payload.slot(), payload.module(), payload.tweak(), payload.value());
    }
}
