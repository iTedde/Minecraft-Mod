package dev.craft.mps;

import dev.craft.Craft;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client → server requests of the Tinker Table GUI and the movement input (spec 29 §12); slot = inventory index. */
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

    /** {@code MusePacketToggleRequest}: a keybinding switches a module on all equipped modular items. */
    public record Toggle(String module, boolean active) implements CustomPacketPayload {
        public static final Type<Toggle> TYPE = payloadType("mps_toggle");
        public static final StreamCodec<ByteBuf, Toggle> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.stringUtf8(64), Toggle::module, ByteBufCodecs.BOOL, Toggle::active, Toggle::new);

        @Override
        public Type<Toggle> type() {
            return TYPE;
        }
    }

    /** {@code MusePacketPlayerUpdate}: the movement keys and the client's motion, sent every client tick. */
    public record Input(MpsInput input, double motionX, double motionY, double motionZ)
            implements CustomPacketPayload {
        public static final Type<Input> TYPE = payloadType("mps_input");
        public static final StreamCodec<ByteBuf, Input> STREAM_CODEC = StreamCodec.composite(
                MpsInput.STREAM_CODEC, Input::input, ByteBufCodecs.DOUBLE, Input::motionX,
                ByteBufCodecs.DOUBLE, Input::motionY, ByteBufCodecs.DOUBLE, Input::motionZ, Input::new);

        @Override
        public Type<Input> type() {
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
                .playToServer(Tweak.TYPE, Tweak.STREAM_CODEC, MpsPayloads::tweak)
                .playToServer(Input.TYPE, Input.STREAM_CODEC, MpsPayloads::input)
                .playToServer(Toggle.TYPE, Toggle.STREAM_CODEC, (payload, context) ->
                        MpsEnvironment.toggleForPlayer(context.player(), payload.module(), payload.active()));
    }

    /** The server takes over the client's motion like the original, so costs follow the real movement. */
    private static void input(Input payload, IPayloadContext context) {
        MpsMovement.setInput(context.player(), payload.input());
        context.player().setDeltaMovement(payload.motionX(), payload.motionY(), payload.motionZ());
    }

    private static void tweak(Tweak payload, IPayloadContext context) {
        TinkerActions.tweak(context.player(), payload.slot(), payload.module(), payload.tweak(), payload.value());
    }
}
