package dev.craft.ic2.personal;

import dev.craft.ic2.machine.MachineBlockEntity;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * IC2 {@code IPersonalBlock}: the first player to use the block becomes its owner (spec 04, personal blocks). The
 * owner is kept as UUID; the name is only shown.
 */
public abstract class PersonalBlockEntity extends MachineBlockEntity {
    @Nullable
    private UUID owner;
    private String ownerName = "";

    protected PersonalBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int slots) {
        super(type, pos, state, slots);
    }

    @Nullable
    public UUID getOwner() {
        return owner;
    }

    public String getOwnerName() {
        return ownerName;
    }

    public boolean isOwnedBy(@Nullable UUID player) {
        return owner != null && owner.equals(player);
    }

    /** Same owner as {@code other} (IC2 compares the owner names, "null" included). */
    public boolean sameOwner(PersonalBlockEntity other) {
        return owner == null ? other.owner == null : owner.equals(other.owner);
    }

    /** IC2 {@code canAccess}: claims an unowned block for the player. Only call on the server. */
    public boolean canAccess(Player player) {
        if (owner == null) {
            owner = player.getUUID();
            ownerName = player.getGameProfile().getName();
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
            return true;
        }
        return owner.equals(player.getUUID());
    }

    @Override
    public boolean wrenchCanRemove(Player player) {
        return canAccess(player);
    }

    /** The owner's ("open") or the customer's ("closed") menu. */
    protected abstract AbstractContainerMenu createMenu(int id, Inventory inventory, boolean open);

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return createMenu(id, inventory, canAccess(player));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        owner = tag.hasUUID("owner") ? tag.getUUID("owner") : null;
        ownerName = tag.getString("ownerName");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (owner != null) {
            tag.putUUID("owner", owner);
        }
        tag.putString("ownerName", ownerName);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = new CompoundTag();
        if (owner != null) {
            tag.putUUID("owner", owner);
        }
        tag.putString("ownerName", ownerName);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        owner = tag.hasUUID("owner") ? tag.getUUID("owner") : null;
        ownerName = tag.getString("ownerName");
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
