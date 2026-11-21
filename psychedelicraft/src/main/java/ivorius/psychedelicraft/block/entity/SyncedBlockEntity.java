package ivorius.psychedelicraft.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class SyncedBlockEntity extends BlockEntity {

    protected SyncedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public final Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public final CompoundTag getUpdateTag(Provider lookup) {
        CompoundTag compound = super.getUpdateTag(lookup);
        saveAdditional(compound, lookup);
        return compound;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level instanceof ServerLevel sw) {
            sw.getChunkSource().blockChanged(getBlockPos());
        }
    }
}
