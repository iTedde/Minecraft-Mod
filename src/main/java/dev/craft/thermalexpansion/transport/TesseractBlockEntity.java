package dev.craft.thermalexpansion.transport;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import dev.craft.thermalexpansion.TeRedstoneControl;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * {@code TileTeleportRoot}: frequency (−1 = disabled), owner, mode (send, receive, both) and access (public, friends,
 * private). Enabled tesseracts are in the {@link TesseractRegistry} under their owner key and frequency; partners are
 * the other entries of the same channel whose redstone condition holds. Senders and receivers serve their partners
 * and their neighbours in turn ({@code outputTrackerRemote}, {@code outputTrackerAdjacent}).
 */
public abstract class TesseractBlockEntity extends BlockEntity implements MenuProvider, TeRedstoneControl {
    public static final String PUBLIC = "_public_";
    public static final int MAX_FREQUENCY = 999;
    public static final int MAX_NAME_LENGTH = 20;
    public static final byte MODE_SEND = 0;
    public static final byte MODE_RECEIVE = 1;
    public static final byte MODE_BOTH = 2;
    public static final byte ACCESS_PUBLIC = 0;
    public static final byte ACCESS_FRIENDS = 1;
    public static final byte ACCESS_PRIVATE = 2;

    private final TesseractType type;
    private int frequency = -1;
    @Nullable
    private UUID owner;
    private String ownerName = "";
    private byte mode = MODE_BOTH;
    private byte access = ACCESS_PUBLIC;
    private boolean redstoneDisable;
    private boolean redstoneState;
    protected int outputTrackerAdjacent;
    protected int outputTrackerRemote;

    protected TesseractBlockEntity(BlockEntityType<?> blockEntityType, TesseractType type, BlockPos pos,
                                   BlockState state) {
        super(blockEntityType, pos, state);
        this.type = type;
    }

    public TesseractType getTesseractType() {
        return type;
    }

    public int getFrequency() {
        return frequency;
    }

    public byte getMode() {
        return mode;
    }

    public byte getAccess() {
        return access;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public boolean isPublic() {
        return access == ACCESS_PUBLIC;
    }

    public boolean isFriends() {
        return access == ACCESS_FRIENDS;
    }

    public boolean isPrivate() {
        return access == ACCESS_PRIVATE;
    }

    public boolean canSend() {
        return mode != MODE_RECEIVE;
    }

    public boolean canReceive() {
        return mode != MODE_SEND;
    }

    /** {@code getOwner}: the registry key; friends-only tesseracts are kept under the owner. */
    public String getOwnerKey() {
        return isPublic() ? PUBLIC : String.valueOf(owner);
    }

    /** The name-list category, {@code <owner key>.<type>}. */
    public String getNameCategory() {
        return getOwnerKey() + "." + type.getName();
    }

    public boolean isOwner(Player player) {
        return owner != null && owner.equals(player.getUUID());
    }

    /** {@code openGui}: public, or the owner, or an operator. */
    public boolean canAccess(Player player) {
        return isPublic() || isOwner(player) || player.hasPermissions(2);
    }

    public void setOwner(Player player) {
        owner = player.getUUID();
        ownerName = player.getGameProfile().getName();
        sync();
    }

    public boolean redstoneControlOrDisable() {
        return redstoneDisable || level.hasNeighborSignal(worldPosition) == redstoneState;
    }

    @Override
    public boolean getRedstoneDisable() {
        return redstoneDisable;
    }

    @Override
    public boolean getRedstoneState() {
        return redstoneState;
    }

    @Override
    public void setRedstone(boolean disable, boolean state) {
        redstoneDisable = disable;
        redstoneState = state;
        sync();
    }

    /** {@code TeleportPacket.TILE_INFO}: leave the registry, take the new settings, enter it again. */
    public void setTileInfo(int frequency, byte mode, byte access) {
        removeFromRegistry();
        this.mode = mode;
        this.access = access;
        this.frequency = frequency;
        addToRegistry();
        BlockState state = getBlockState();
        boolean active = frequency != -1;
        if (state.getValue(TesseractBlock.ACTIVE) != active) {
            level.setBlock(worldPosition, state.setValue(TesseractBlock.ACTIVE, active), Block.UPDATE_ALL);
        }
        sync();
    }

    /** {@code getValidInputs}: the other senders of the channel, if both redstone conditions hold. */
    public List<TesseractBlockEntity> getValidInputs() {
        return valid(TesseractRegistry.senders(this));
    }

    /** {@code getValidOutputs}: the other receivers of the channel, if both redstone conditions hold. */
    public List<TesseractBlockEntity> getValidOutputs() {
        return valid(TesseractRegistry.receivers(this));
    }

    private List<TesseractBlockEntity> valid(List<TesseractBlockEntity> linked) {
        List<TesseractBlockEntity> valid = new ArrayList<>();
        if (linked.isEmpty() || !redstoneControlOrDisable()) {
            return valid;
        }
        for (TesseractBlockEntity tesseract : linked) {
            if (tesseract != this && tesseract.redstoneControlOrDisable()) {
                valid.add(tesseract);
            }
        }
        return valid;
    }

    private void addToRegistry() {
        if (level != null && !level.isClientSide() && frequency != -1) {
            TesseractRegistry.add(this);
        }
    }

    private void removeFromRegistry() {
        if (level != null && !level.isClientSide()) {
            TesseractRegistry.remove(this);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        addToRegistry();
    }

    @Override
    public void setRemoved() {
        removeFromRegistry();
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        removeFromRegistry();
        super.onChunkUnloaded();
    }

    /** {@code incrementOutputTrackerRemote}: wraps after {@code max}. */
    protected void incrementOutputTrackerRemote(int max) {
        outputTrackerRemote++;
        if (outputTrackerRemote > max) {
            outputTrackerRemote = 0;
        }
    }

    /** {@code incrementOutputTrackerAdjacent}: on to the next side with something to serve. */
    protected void incrementOutputTrackerAdjacent() {
        outputTrackerAdjacent++;
        for (int side = outputTrackerAdjacent; side < 6; side++) {
            if (hasAdjacentTarget(Direction.from3DDataValue(side))) {
                outputTrackerAdjacent = side;
                return;
            }
        }
        for (int side = 0; side < outputTrackerAdjacent && side < 6; side++) {
            if (hasAdjacentTarget(Direction.from3DDataValue(side))) {
                outputTrackerAdjacent = side;
                return;
            }
        }
        outputTrackerAdjacent = 0;
    }

    /** Whether the neighbour on {@code side} can take what this tesseract carries. */
    protected abstract boolean hasAdjacentTarget(Direction side);

    public void serverTick() {
    }

    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getCenter()) <= 64.0;
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new TesseractMenu(id, playerInventory, this);
    }

    protected void sync() {
        setChanged();
        if (level != null) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        loadClientData(tag);
        outputTrackerAdjacent = tag.getInt("output.adjacent");
        outputTrackerRemote = tag.getInt("output.remote");
    }

    private void loadClientData(CompoundTag tag) {
        frequency = tag.contains("frequency") ? tag.getInt("frequency") : -1;
        owner = tag.hasUUID("owner") ? tag.getUUID("owner") : null;
        ownerName = tag.getString("owner.name");
        mode = tag.getByte("mode");
        access = tag.getByte("flag.access");
        redstoneDisable = tag.getBoolean("red.disable");
        redstoneState = tag.getBoolean("red.state");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        saveClientData(tag);
        tag.putInt("output.adjacent", outputTrackerAdjacent);
        tag.putInt("output.remote", outputTrackerRemote);
    }

    private void saveClientData(CompoundTag tag) {
        tag.putInt("frequency", frequency);
        if (owner != null) {
            tag.putUUID("owner", owner);
        }
        tag.putString("owner.name", ownerName);
        tag.putByte("mode", mode);
        tag.putByte("flag.access", access);
        tag.putBoolean("red.disable", redstoneDisable);
        tag.putBoolean("red.state", redstoneState);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveClientData(tag);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        loadClientData(tag);
    }

    @Override
    public void onDataPacket(Connection connection, ClientboundBlockEntityDataPacket packet,
                             HolderLookup.Provider registries) {
        handleUpdateTag(packet.getTag(), registries);
    }
}
