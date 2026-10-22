package dev.craft.thermalexpansion.transport;

import java.util.List;

import javax.annotation.Nullable;

import dev.craft.buildcraft.transport.ItemPipeBlockEntity;
import dev.craft.buildcraft.transport.PipeConnections;
import dev.craft.buildcraft.transport.TravelingItem;
import dev.craft.thermalexpansion.TeBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;

/**
 * {@code TileTeleportItem}: one buffer slot that only takes a stack while the tesseract can send and the buffer is
 * empty. An inserted stack goes straight on to the partners: per partner its sides in turn, an item pipe takes the
 * whole stack, otherwise the inventory. What is left stays in the buffer and is retried every 40 ticks. The buffer
 * is not dropped when the block is broken (no {@code TileInventory} in the original).
 */
public class ItemTesseractBlockEntity extends TesseractBlockEntity {
    private ItemStack buffer = ItemStack.EMPTY;

    private final IItemHandler itemHandler = new IItemHandler() {
        @Override
        public int getSlots() {
            return 1;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return ItemStack.EMPTY;
        }

        /** {@code getSizeInventorySide} + {@code setInventorySlotContents}: the whole stack is taken. */
        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty() || !acceptsItems()) {
                return stack;
            }
            if (!simulate) {
                sendItem(stack.copy());
            }
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return ItemStack.EMPTY;
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return acceptsItems();
        }
    };

    public ItemTesseractBlockEntity(BlockPos pos, BlockState state) {
        super(TeBlockEntityTypes.ITEM_TESSERACT.get(), TesseractType.ITEM, pos, state);
    }

    @Nullable
    public IItemHandler getItemHandler(@Nullable Direction side) {
        return itemHandler;
    }

    public ItemStack getBuffer() {
        return buffer;
    }

    private boolean acceptsItems() {
        return level != null && !level.isClientSide() && getFrequency() != -1 && canSend()
                && redstoneControlOrDisable() && buffer.isEmpty();
    }

    @Override
    public void serverTick() {
        if (level.getGameTime() % 40L == 0L && !buffer.isEmpty()) {
            ItemStack stack = buffer;
            buffer = ItemStack.EMPTY;
            sendItem(stack);
        }
    }

    private void sendItem(ItemStack stack) {
        List<TesseractBlockEntity> outputs = getValidOutputs();
        for (int i = outputTrackerRemote; i < outputs.size() && !stack.isEmpty(); i++) {
            stack = ((ItemTesseractBlockEntity) outputs.get(i)).receiveItem(stack);
        }
        for (int i = 0; i < outputs.size() && i < outputTrackerRemote && !stack.isEmpty(); i++) {
            stack = ((ItemTesseractBlockEntity) outputs.get(i)).receiveItem(stack);
        }
        incrementOutputTrackerRemote(outputs.size() - 1);
        if (!stack.isEmpty()) {
            buffer = stack;
        }
        setChanged();
    }

    /** {@code receiveItem}: the rest after the adjacent pipes and inventories. */
    private ItemStack receiveItem(ItemStack stack) {
        for (int side = outputTrackerAdjacent; side < 6 && !stack.isEmpty(); side++) {
            stack = addToAdjacent(Direction.from3DDataValue(side), stack);
        }
        for (int side = 0; side < outputTrackerAdjacent && !stack.isEmpty(); side++) {
            stack = addToAdjacent(Direction.from3DDataValue(side), stack);
        }
        incrementOutputTrackerAdjacent();
        setChanged();
        return stack;
    }

    /** {@code Utils.addToAdjPipe}, else {@code addToAdjInventory} (other item tesseracts take nothing). */
    private ItemStack addToAdjacent(Direction side, ItemStack stack) {
        BlockPos neighbour = worldPosition.relative(side);
        if (level.getBlockEntity(neighbour) instanceof ItemPipeBlockEntity pipe
                && PipeConnections.canConnect(level, neighbour, side.getOpposite())) {
            pipe.entityEntering(new TravelingItem(stack.copy(), worldPosition.getX() + 0.5 + side.getStepX() * 0.5,
                    worldPosition.getY() + ItemPipeBlockEntity.PIPE_FLOOR + side.getStepY() * 0.5,
                    worldPosition.getZ() + 0.5 + side.getStepZ() * 0.5), side);
            return ItemStack.EMPTY;
        }
        if (level.getBlockEntity(neighbour) instanceof ItemTesseractBlockEntity) {
            return stack;
        }
        IItemHandler inventory = level.getCapability(Capabilities.ItemHandler.BLOCK, neighbour, side.getOpposite());
        if (inventory == null || inventory.getSlots() == 0) {
            return stack;
        }
        return ItemHandlerHelper.insertItemStacked(inventory, stack, false);
    }

    /** {@code Utils.isAdjacentPipe} or {@code isAdjacentInventory}. */
    @Override
    protected boolean hasAdjacentTarget(Direction side) {
        BlockPos neighbour = worldPosition.relative(side);
        return level.getBlockEntity(neighbour) instanceof ItemPipeBlockEntity
                || level.getCapability(Capabilities.ItemHandler.BLOCK, neighbour, side.getOpposite()) != null;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        buffer = tag.contains("inventory") ? ItemStack.parseOptional(registries, tag.getCompound("inventory"))
                : ItemStack.EMPTY;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!buffer.isEmpty()) {
            tag.put("inventory", buffer.save(registries));
        }
    }
}
