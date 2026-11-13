package dev.craft.mffs.block;

import dev.craft.mffs.MffsBlockEntityTypes;
import java.util.Objects;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;

/**
 * {@code TileEntityForceField} (spec 48 §9.3/§14): only force fields of the camouflage type have one. It holds the
 * block whose look the field takes (slot 11 of the projector; water/lava for the buckets); none means the default
 * camouflage texture. The client model reads it via {@link #CAMOUFLAGE}.
 */
public class ForceFieldBlockEntity extends BlockEntity {
    public static final ModelProperty<BlockState> CAMOUFLAGE = new ModelProperty<>();

    private BlockState camouflage;

    public ForceFieldBlockEntity(BlockPos pos, BlockState state) {
        super(MffsBlockEntityTypes.FORCE_FIELD.get(), pos, state);
    }

    public BlockState getCamouflage() {
        return camouflage;
    }

    /** Sets the look and sends it to the clients when it changed. */
    public void setCamouflage(BlockState camouflage) {
        if (Objects.equals(this.camouflage, camouflage)) {
            return;
        }
        this.camouflage = camouflage;
        setChanged();
        if (level != null && !level.isClientSide()) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public ModelData getModelData() {
        return camouflage != null ? ModelData.builder().with(CAMOUFLAGE, camouflage).build() : ModelData.EMPTY;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (camouflage != null) {
            tag.put("camouflage", NbtUtils.writeBlockState(camouflage));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        BlockState previous = camouflage;
        camouflage = tag.contains("camouflage")
                ? NbtUtils.readBlockState(registries.lookupOrThrow(Registries.BLOCK), tag.getCompound("camouflage"))
                : null;
        if (level != null && level.isClientSide() && !Objects.equals(previous, camouflage)) {
            requestModelDataUpdate();
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_IMMEDIATE);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet,
                             HolderLookup.Provider registries) {
        loadAdditional(packet.getTag(), registries);
    }
}
