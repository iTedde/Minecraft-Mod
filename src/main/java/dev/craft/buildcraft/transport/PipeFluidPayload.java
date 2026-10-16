package dev.craft.buildcraft.transport;

import java.util.List;

import dev.craft.Craft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** {@code PacketLiquidUpdate}: the displayed content of the seven sections of a fluid pipe (always all of them). */
public record PipeFluidPayload(BlockPos pos, List<FluidStack> fluids) implements CustomPacketPayload {
    public static final Type<PipeFluidPayload> TYPE =
            new Type<>(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "pipe_fluid"));
    public static final StreamCodec<RegistryFriendlyByteBuf, PipeFluidPayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, PipeFluidPayload::pos,
            FluidStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list(7)), PipeFluidPayload::fluids,
            PipeFluidPayload::new);

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToClient(TYPE, STREAM_CODEC, PipeFluidPayload::handle);
    }

    private static void handle(PipeFluidPayload payload, IPayloadContext context) {
        if (context.player().level().getBlockEntity(payload.pos) instanceof FluidPipeBlockEntity pipe) {
            pipe.handleFluidPacket(payload);
        }
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
