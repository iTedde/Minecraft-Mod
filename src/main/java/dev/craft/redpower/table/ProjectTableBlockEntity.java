package dev.craft.redpower.table;

import dev.craft.redpower.RpBlockEntityTypes;
import dev.craft.redpower.RpItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.Nullable;

/** RedPower {@code TileAdvBench}: crafting grid 0–8, plan slot 9, storage 10–27. */
public class ProjectTableBlockEntity extends BlockEntity implements MenuProvider {
    public static final int GRID_SIZE = 9;
    public static final int PLAN_SLOT = 9;
    public static final int STORAGE_START = 10;
    public static final int STORAGE_SIZE = 18;
    public static final int SIZE = STORAGE_START + STORAGE_SIZE;

    private final ItemStackHandler inventory = createInventory(this::onInventoryChanged);
    private final IItemHandler storage = new RangedWrapper(inventory, STORAGE_START, SIZE);
    /** Counts inventory changes so open menus know when to recompute the result. */
    private int changes;

    public ProjectTableBlockEntity(BlockPos pos, BlockState state) {
        super(RpBlockEntityTypes.PROJECT_TABLE.get(), pos, state);
    }

    /** The table's inventory; the plan slot holds one blank or written plan. Also used client side by the menu. */
    public static ItemStackHandler createInventory(Runnable onChanged) {
        return new ItemStackHandler(SIZE) {
            @Override
            protected void onContentsChanged(int slot) {
                onChanged.run();
            }

            @Override
            public int getSlotLimit(int slot) {
                return slot == PLAN_SLOT ? 1 : super.getSlotLimit(slot);
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return slot != PLAN_SLOT || stack.is(RpItems.BLANK_PLAN) || stack.is(RpItems.PLAN);
            }
        };
    }

    private void onInventoryChanged() {
        changes++;
        setChanged();
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    /** Automation reaches the storage only ({@code getStartInventorySide 10}, {@code getSizeInventorySide 18}). */
    public IItemHandler getStorage() {
        return storage;
    }

    public int getChanges() {
        return changes;
    }

    public void dropContents() {
        if (level == null) {
            return;
        }
        for (int i = 0; i < SIZE; i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
                inventory.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new ProjectTableMenu(id, playerInventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
    }
}
