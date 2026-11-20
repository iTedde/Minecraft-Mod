package ivorius.psychedelicraft.network;

import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;

import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.hallucination.AbstractEntityHallucination;

public record MsgHallucinate (
        int entityId,
        ResourceLocation hallucination,
        Optional<BlockPos> position
    ) implements Channel.HandledPacket {

    @Override
    public Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type() {
        return Channel.HALLUCINATE.type();
    }

    public MsgHallucinate(FriendlyByteBuf buffer) {
        this(buffer.readVarInt(), buffer.readResourceLocation(), buffer.readOptional(buf -> buf.readBlockPos()));
    }

    @Override
    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeVarInt(entityId);
        buffer.writeResourceLocation(hallucination);
        buffer.writeOptional(position, (buf, pos) -> buf.writeBlockPos(pos));
    }

    @Override
    public void handle(Player sender) {
        DrugProperties.of(sender.level().getEntity(entityId)).ifPresent(properties -> {
            if (properties.getHallucinations().getEntities().addHallucination(hallucination, true) instanceof AbstractEntityHallucination e) {
                position.map(BlockPos::getCenter).ifPresent(e.getEntity()::setPos);
            }
        });
    }
}
