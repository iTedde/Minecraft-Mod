package dev.craft.thermalexpansion.transport;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * {@code TileConduitRoot}: one byte per side ({@link #NONE}, {@link #CONDUIT} of the same network, {@link #OTHER} = a
 * tank or MJ receiver), the node mode ({@link #MODE_OUTPUT}/{@link #MODE_INPUT}, wrench) and the round-robin side.
 * A conduit with an {@link #OTHER} side is a node. The networks themselves are rebuilt by flood fill whenever a
 * conduit has none ({@code updateGridIfNull}); a change breaks the network up into the stored shares first.
 */
public abstract class ConduitBlockEntity extends BlockEntity {
    public static final byte NONE = 0;
    public static final byte CONDUIT = 1;
    public static final byte OTHER = 2;
    public static final byte MODE_OUTPUT = 0;
    public static final byte MODE_INPUT = 1;

    protected final byte[] sides = new byte[6];
    protected byte mode = MODE_OUTPUT;
    protected byte outputTracker;

    protected ConduitBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public byte getSide(Direction side) {
        return sides[side.get3DDataValue()];
    }

    public byte getMode() {
        return mode;
    }

    public boolean isNode() {
        for (byte side : sides) {
            if (side == OTHER) {
                return true;
            }
        }
        return false;
    }

    public abstract void serverTick();

    /** {@code onNeighborBlockChange}. */
    public abstract void onNeighborBlockChange();

    /** {@code wrenchAction} on the clicked side; false lets the wrench switch the mode. */
    public abstract boolean wrenchAction(Direction side);

    /** {@code changeMode}: nodes only. */
    public abstract void changeMode();

    /** Leaves the network (broken or unloaded). */
    protected abstract void leaveGrid();

    /** {@code increaseTracker}: the next side (after the current) with a tank or receiver, else 0. */
    protected void increaseTracker() {
        for (int i = outputTracker + 1; i < 6; i++) {
            if (sides[i] > CONDUIT) {
                outputTracker = (byte) i;
                return;
            }
        }
        for (int i = 0; i < outputTracker + 1 && i < 6; i++) {
            if (sides[i] > CONDUIT) {
                outputTracker = (byte) i;
                return;
            }
        }
        outputTracker = 0;
    }

    /** Puts the connections into the block state (shape) and sends them to the client. */
    protected void syncSides() {
        if (level == null || level.isClientSide()) {
            return;
        }
        BlockState state = getBlockState();
        BlockState updated = state;
        for (Direction direction : Direction.values()) {
            updated = updated.setValue(ConduitBlock.connection(direction), sides[direction.get3DDataValue()] != NONE);
        }
        if (updated != state) {
            level.setBlock(worldPosition, updated, Block.UPDATE_ALL);
        }
        sync();
    }

    protected void sync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Nullable
    protected BlockEntity neighbour(Direction side) {
        BlockPos pos = worldPosition.relative(side);
        return level.isLoaded(pos) ? level.getBlockEntity(pos) : null;
    }

    /** Broken or unloaded ({@code onChunkUnload}; the unloaded conduits keep their saved shares). */
    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide()) {
            leaveGrid();
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        loadClientData(tag, registries);
        outputTracker = (byte) Math.floorMod(tag.getByte("output"), 6);
    }

    protected void loadClientData(CompoundTag tag, HolderLookup.Provider registries) {
        if (tag.contains("side.array", Tag.TAG_BYTE_ARRAY)) {
            byte[] stored = tag.getByteArray("side.array");
            for (int i = 0; i < 6; i++) {
                sides[i] = i < stored.length && stored[i] >= NONE && stored[i] <= OTHER ? stored[i] : NONE;
            }
        }
        mode = (byte) (tag.getByte("mode") % 2);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        saveClientData(tag, registries);
        tag.putByte("output", outputTracker);
    }

    protected void saveClientData(CompoundTag tag, HolderLookup.Provider registries) {
        tag.putByteArray("side.array", sides.clone());
        tag.putByte("mode", mode);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveClientData(tag, registries);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        loadClientData(tag, registries);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet,
                             HolderLookup.Provider registries) {
        handleUpdateTag(packet.getTag(), registries);
    }
}
