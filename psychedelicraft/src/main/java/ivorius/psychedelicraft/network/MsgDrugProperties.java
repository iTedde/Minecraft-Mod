package ivorius.psychedelicraft.network;


import ivorius.psychedelicraft.entity.drug.DrugProperties;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;

public record MsgDrugProperties (
        int entityId,
        CompoundTag compound
    ) implements Channel.HandledPacket {

    @Override
    public Type<? extends net.minecraft.network.protocol.common.custom.CustomPacketPayload> type() {
        return Channel.UPDATE_DRUG_PROPERTIES.type();
    }

    public MsgDrugProperties(DrugProperties properties, Provider lookup) {
        this(properties.asEntity().getId(), properties.toTrackedNbt(new CompoundTag(), lookup));
    }

    public MsgDrugProperties(FriendlyByteBuf buffer) {
        this(buffer.readVarInt(), buffer.readNbt());
    }

    @Override
    public void toBuffer(FriendlyByteBuf buffer) {
        buffer.writeVarInt(entityId);
        buffer.writeNbt(compound);
    }

    @Override
    public void handle(Player sender) {
        DrugProperties.of(sender.level().getEntity(entityId)).ifPresent(e -> e.fromTrackedNbt(compound, sender.registryAccess()));
    }
}
