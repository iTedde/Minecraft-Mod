package dev.craft.ic2.machine;

import dev.craft.ic2.Ic2Machine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.Nullable;

/**
 * Base of IC2 machines, generators and storage blocks with an inventory (IC2 {@code TileEntityMachine} +
 * {@code TileEntityBlock}). Facing and the active flag live in the block state.
 */
public abstract class MachineBlockEntity extends BlockEntity implements Wrenchable, MenuProvider {
    protected final MachineInventory inventory;

    protected MachineBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int slots) {
        super(type, pos, state);
        this.inventory = new MachineInventory(slots, this::onInventoryChanged);
    }

    public MachineInventory getInventory() {
        return inventory;
    }

    public Ic2Machine getMachine() {
        return ((MachineBlock) getBlockState().getBlock()).getMachine();
    }

    /** Called on every inventory change (IC2 {@code onInventoryChanged}). */
    protected void onInventoryChanged() {
        setChanged();
    }

    public void serverTick() {
    }

    public void clientTick() {
    }

    // --- facing / active ---

    @Override
    public Direction getFacing() {
        return getBlockState().getValue(MachineBlock.FACING);
    }

    @Override
    public boolean wrenchCanSetFacing(Player player, Direction side) {
        return false;
    }

    @Override
    public void setFacing(Direction facing) {
        if (level != null && MachineBlock.FACING.getPossibleValues().contains(facing)) {
            level.setBlock(worldPosition, getBlockState().setValue(MachineBlock.FACING, facing), Block.UPDATE_ALL);
        }
    }

    public boolean isActive() {
        return getBlockState().getValue(MachineBlock.ACTIVE);
    }

    public void setActive(boolean active) {
        if (level != null && !isRemoved() && isActive() != active) {
            level.setBlock(worldPosition, getBlockState().setValue(MachineBlock.ACTIVE, active), Block.UPDATE_ALL);
        }
    }

    // --- wrench ---

    @Override
    public float getWrenchDropRate() {
        return getMachine().getWrenchDropRate();
    }

    @Override
    public ItemStack getWrenchDrop(Player player) {
        return new ItemStack(getBlockState().getBlock());
    }

    // --- inventory ---

    /**
     * Item access for the given side; IC2 sided inventories expose exactly one slot per side
     * ({@code getStartInventorySide}/{@code getSizeInventorySide}).
     */
    public IItemHandler getItemHandler(@Nullable Direction side) {
        if (side == null) {
            return inventory;
        }
        int slot = getSlotForSide(side);
        return new RangedWrapper(inventory, slot, slot + 1);
    }

    /** IC2 {@code getStartInventorySide}; default: bottom → 1, top → 0, sides → 2. */
    protected int getSlotForSide(Direction side) {
        return switch (side) {
            case DOWN -> 1;
            case UP -> 0;
            default -> 2;
        };
    }

    /** IC2 {@code onBlockBreak}: called before the inventory is dropped. */
    public void onBlockBreak() {
    }

    public void dropContents() {
        if (level == null) {
            return;
        }
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.get(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
                inventory.set(i, ItemStack.EMPTY);
            }
        }
    }

    public boolean stillValid(Player player) {
        return level != null && level.getBlockEntity(worldPosition) == this
                && player.distanceToSqr(worldPosition.getX() + 0.5, worldPosition.getY() + 0.5,
                worldPosition.getZ() + 0.5) <= 64.0;
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    // --- persistence ---

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("Items"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Items", inventory.serializeNBT(registries));
    }
}
