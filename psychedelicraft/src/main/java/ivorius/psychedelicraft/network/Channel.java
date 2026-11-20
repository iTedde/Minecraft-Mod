package ivorius.psychedelicraft.network;

import java.util.function.Function;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

/**
 * Server to client packets (upstream: Fabwork {@code SimpleNetworking}).
 */
public interface Channel {
    S2CPacketType<MsgDrugProperties> UPDATE_DRUG_PROPERTIES = new S2CPacketType<>("update_drug_properties", MsgDrugProperties::new);
    S2CPacketType<MsgHallucinate> HALLUCINATE = new S2CPacketType<>("hallucinate", MsgHallucinate::new);

    static void bootstrap() { }

    static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(Psychedelicraft.DEFAULT_NAMESPACE).versioned("1");
        UPDATE_DRUG_PROPERTIES.register(registrar);
        HALLUCINATE.register(registrar);
    }

    /**
     * A packet that is handled on the receiving player's side.
     */
    interface HandledPacket extends CustomPacketPayload {
        void toBuffer(FriendlyByteBuf buffer);

        void handle(Player sender);
    }

    final class S2CPacketType<T extends HandledPacket> {
        private final CustomPacketPayload.Type<T> type;
        private final StreamCodec<RegistryFriendlyByteBuf, T> codec;

        S2CPacketType(String name, Function<FriendlyByteBuf, T> reader) {
            this.type = new CustomPacketPayload.Type<>(Psychedelicraft.id(name));
            this.codec = StreamCodec.of((buf, packet) -> packet.toBuffer(buf), reader::apply);
        }

        public CustomPacketPayload.Type<T> type() {
            return type;
        }

        void register(net.neoforged.neoforge.network.registration.PayloadRegistrar registrar) {
            registrar.playToClient(type, codec, (packet, context) -> packet.handle(context.player()));
        }

        public void sendToPlayer(T packet, ServerPlayer player) {
            PacketDistributor.sendToPlayer(player, packet);
        }

        public void sendToSurroundingPlayers(T packet, Entity entity) {
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(entity, packet);
        }
    }
}
