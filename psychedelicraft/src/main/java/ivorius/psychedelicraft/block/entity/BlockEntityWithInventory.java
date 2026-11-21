/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.block.entity;

import java.util.stream.Stream;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BlockEntityWithInventory extends BaseContainerBlockEntity implements WorldlyContainer {
    protected static final int[] NO_SLOTS = new int[0];

    private final NonNullList<ItemStack> inventory;

    public BlockEntityWithInventory(BlockEntityType<? extends BlockEntityWithInventory> type, BlockPos pos, BlockState state, int size) {
        super(type, pos, state);
        inventory = NonNullList.withSize(size, ItemStack.EMPTY);
    }

    protected Stream<ItemStack> getStacks() {
        return inventory.stream();
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return inventory;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> inventory) {
        for (int i = 0; i < getContainerSize(); i++) {
            this.inventory.set(i, i >= inventory.size() ? ItemStack.EMPTY : inventory.get(i));
        }
        setChanged();
    }

    @Override
    public int getContainerSize() {
        return inventory.size();
    }

    @Override
    public void clearContent() {
        super.clearContent();
        setChanged();
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack removed = ContainerHelper.takeItem(inventory, slot);
        if (!removed.isEmpty()) {
            setChanged();
        }
        return removed;
    }

    @Override
    public void setChanged() {
        super.setChanged();
        if (level instanceof ServerLevel sw) {
            sw.getChunkSource().blockChanged(getBlockPos());
        }
        if (level != null) {
            if (getBlockState().hasAnalogOutputSignal()) {
                level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
            }
            level.blockUpdated(worldPosition, getBlockState().getBlock());
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    protected Component getDefaultName() {
        return getBlockState().getBlock().getName();
    }


    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction direction) {
        return true;
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
    protected void loadAdditional(CompoundTag nbt, Provider lookup) {
        super.loadAdditional(nbt, lookup);
        inventory.clear();
        ContainerHelper.loadAllItems(nbt, inventory, lookup);
    }

    @Override
    protected void saveAdditional(CompoundTag nbt, Provider lookup) {
        super.saveAdditional(nbt, lookup);
        ContainerHelper.saveAllItems(nbt, inventory, lookup);
    }
}
