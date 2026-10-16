package dev.craft.buildcraft.transport;

import dev.craft.Craft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** {@code PacketPowerUpdate}: the display stages (0–100) of the six sides and the overload flag. */
public record PipePowerPayload(BlockPos pos, byte[] displayPower, boolean overload) implements CustomPacketPayload {
    public static final Type<PipePowerPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "pipe_power"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PipePowerPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PipePowerPayload::pos,
            ByteBufCodecs.BYTE_ARRAY, PipePowerPayload::displayPower,
            ByteBufCodecs.BOOL, PipePowerPayload::overload,
            PipePowerPayload::new);

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(TYPE, STREAM_CODEC, PipePowerPayload::handle);
    }

    private static void handle(PipePowerPayload payload, IPayloadContext context) {
        if (payload.displayPower.length == 6
                && context.player().level().getBlockEntity(payload.pos) instanceof PowerPipeBlockEntity pipe) {
            pipe.handlePowerPacket(payload);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
