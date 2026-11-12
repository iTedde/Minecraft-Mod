package dev.craft.mffs;

import dev.craft.Craft;
import dev.craft.mffs.block.MffsMachineBlockEntity;
import dev.craft.mffs.menu.MffsMachineMenu;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * {@code NetworkHandlerClient.fireTileEntityEvent} (spec 48 §13): GUI button and name input events for the machine
 * of the player's open MFFS menu.
 */
public final class MffsPayloads {
    private MffsPayloads() {
    }

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(MachineEvent.TYPE, MachineEvent.STREAM_CODEC, MffsPayloads::handle);
    }

    public static void send(BlockPos pos, int key, String value) {
        PacketDistributor.sendToServer(new MachineEvent(pos, key, value));
    }

    private static void handle(MachineEvent payload, IPayloadContext context) {
        if (!(context.player().containerMenu instanceof MffsMachineMenu menu) || !menu.getPos().equals(payload.pos())
                || !menu.stillValid(context.player())) {
            return;
        }
        if (context.player().level().getBlockEntity(payload.pos()) instanceof MffsMachineBlockEntity machine) {
            machine.handleEvent(payload.key(), payload.value());
        }
    }

    public record MachineEvent(BlockPos pos, int key, String value) implements CustomPacketPayload {
        public static final Type<MachineEvent> TYPE =
                new Type<>(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "mffs_machine_event"));
        public static final StreamCodec<ByteBuf, MachineEvent> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, MachineEvent::pos,
                ByteBufCodecs.VAR_INT, MachineEvent::key,
                ByteBufCodecs.stringUtf8(8), MachineEvent::value,
                MachineEvent::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }
}
