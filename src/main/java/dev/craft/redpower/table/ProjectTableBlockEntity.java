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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.Nullable;

/**
 * RedPower {@code TileAdvBench}: crafting grid 0–8, plan slot 9, storage from 10 – 18 slots for the project table,
 * 27 for the large project table (spec 34).
 */
public class ProjectTableBlockEntity extends BlockEntity implements MenuProvider {
    public static final int GRID_SIZE = 9;
    public static final int PLAN_SLOT = 9;
    public static final int STORAGE_START = 10;
    public static final int STORAGE_SIZE = 18;
    public static final int LARGE_STORAGE_SIZE = 27;

    private final int storageSize;
    private final ItemStackHandler inventory;
    private final IItemHandler storage;
    /** Counts inventory changes so open menus know when to recompute the result. */
    private int changes;

    public ProjectTableBlockEntity(BlockPos pos, BlockState state) {
        this(RpBlockEntityTypes.PROJECT_TABLE.get(), pos, state, STORAGE_SIZE);
    }

    private ProjectTableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, int storageSize) {
        super(type, pos, state);
        this.storageSize = storageSize;
        inventory = createInventory(storageSize, this::onInventoryChanged);
        storage = new RangedWrapper(inventory, STORAGE_START, STORAGE_START + storageSize);
    }

    public static ProjectTableBlockEntity large(BlockPos pos, BlockState state) {
        return new ProjectTableBlockEntity(RpBlockEntityTypes.LARGE_PROJECT_TABLE.get(), pos, state,
                LARGE_STORAGE_SIZE);
    }

    /** The table's inventory; the plan slot holds one blank or written plan. Also used client side by the menu. */
    public static ItemStackHandler createInventory(int storageSize, Runnable onChanged) {
        return new ItemStackHandler(STORAGE_START + storageSize) {
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

    public int getStorageSize() {
        return storageSize;
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
        for (int i = 0; i < inventory.getSlots(); i++) {
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
