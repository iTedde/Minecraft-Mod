package dev.craft.buildcraft.transport;

import dev.craft.Craft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * {@code PacketPipeTransportContent}: the server sends every item that enters a pipe or bounces back, the client then
 * moves it on its own until the next packet.
 */
public record PipeItemPayload(BlockPos pos, int id, ItemStack stack, double x, double y, double z, float speed,
                              byte input, byte output, boolean toCenter) implements CustomPacketPayload {
    public static final Type<PipeItemPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "pipe_item"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PipeItemPayload> STREAM_CODEC =
            StreamCodec.of(PipeItemPayload::write, PipeItemPayload::read);

    static PipeItemPayload of(BlockPos pos, TravelingItem item) {
        return new PipeItemPayload(pos, item.id, item.stack.copy(), item.x, item.y, item.z, item.speed,
                (byte) item.input.get3DDataValue(), (byte) (item.output == null ? 6 : item.output.get3DDataValue()),
                item.toCenter);
    }

    private static void write(RegistryFriendlyByteBuf buf, PipeItemPayload payload) {
        buf.writeBlockPos(payload.pos);
        buf.writeVarInt(payload.id);
        ItemStack.STREAM_CODEC.encode(buf, payload.stack);
        buf.writeDouble(payload.x);
        buf.writeDouble(payload.y);
        buf.writeDouble(payload.z);
        buf.writeFloat(payload.speed);
        buf.writeByte(payload.input);
        buf.writeByte(payload.output);
        buf.writeBoolean(payload.toCenter);
    }

    private static PipeItemPayload read(RegistryFriendlyByteBuf buf) {
        return new PipeItemPayload(buf.readBlockPos(), buf.readVarInt(), ItemStack.STREAM_CODEC.decode(buf),
                buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readFloat(), buf.readByte(), buf.readByte(),
                buf.readBoolean());
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(TYPE, STREAM_CODEC, PipeItemPayload::handle);
    }

    private static void handle(PipeItemPayload payload, IPayloadContext context) {
        if (context.player().level().getBlockEntity(payload.pos) instanceof ItemPipeBlockEntity pipe) {
            pipe.handleItemPacket(payload);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
