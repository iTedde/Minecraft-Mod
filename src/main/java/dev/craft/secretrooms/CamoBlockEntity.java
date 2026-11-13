package dev.craft.secretrooms;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

/**
 * {@code TileEntityCamoFull} / {@code TileEntityCamo} (spec 50 §4): the block state a camouflaged block looks like,
 * chosen once when it is placed. Synced to the client, where the camouflage model reads it from the model data.
 */
public class CamoBlockEntity extends BlockEntity {
    public static final ModelProperty<BlockState> CAMO = new ModelProperty<>();

    private BlockState camo = Blocks.STONE.defaultBlockState();
    private boolean chosen;

    public CamoBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public CamoBlockEntity(BlockPos pos, BlockState state) {
        this(SecretRooms.CAMO_BLOCK_ENTITY.get(), pos, state);
    }

    public BlockState getCamo() {
        return camo;
    }

    public boolean isChosen() {
        return chosen;
    }

    public void setCamo(BlockState camo) {
        this.camo = camo;
        chosen = true;
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
            level.getLightEngine().checkBlock(worldPosition);
        }
    }

    @Override
    public ModelData getModelData() {
        return ModelData.builder().with(CAMO, camo).build();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Camo", NbtUtils.writeBlockState(camo));
        tag.putBoolean("Chosen", chosen);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        camo = NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK), tag.getCompound("Camo"));
        chosen = tag.getBoolean("Chosen");
        if (level != null && level.isClientSide()) {
            requestModelDataUpdate();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_ALL);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet,
            HolderLookup.Provider registries) {
        super.onDataPacket(connection, packet, registries);
        requestModelDataUpdate();
    }
}
