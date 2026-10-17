package dev.craft.buildcraft.factory;

import java.util.ArrayList;
import java.util.List;

import dev.craft.buildcraft.BcBlockEntityTypes;
import dev.craft.buildcraft.menu.AutoWorkbenchMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemHandlerHelper;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

/**
 * BuildCraft {@code TileAutoWorkbench} (spec 20 §2): the 3×3 grid is recipe and buffer at once. No power, no ticking;
 * a craft only happens when a pipe or the GUI takes the result.
 */
public class AutoWorkbenchBlockEntity extends BlockEntity implements MenuProvider, SpecialInventory {
    public static final int SIZE = 9;
    /** {@code getNearbyItem}: west, east, down, up, north, south. */
    private static final Direction[] NEIGHBOURS = {Direction.WEST, Direction.EAST, Direction.DOWN, Direction.UP,
            Direction.NORTH, Direction.SOUTH};

    private final ItemStackHandler grid = createGrid(this::onGridChanged);
    private final IItemHandler pipeHandler = new RoundRobinHandler();
    /** Counts grid changes so open menus know when to recompute the result. */
    private int changes;

    public AutoWorkbenchBlockEntity(BlockPos pos, BlockState state) {
        super(BcBlockEntityTypes.AUTO_WORKBENCH.get(), pos, state);
    }

    public static ItemStackHandler createGrid(Runnable onChanged) {
        return new ItemStackHandler(SIZE) {
            @Override
            protected void onContentsChanged(int slot) {
                onChanged.run();
            }
        };
    }

    private void onGridChanged() {
        changes++;
        setChanged();
    }

    public ItemStackHandler getGrid() {
        return grid;
    }

    /** Item capability: round-robin insertion only (spec 20 §4 F7), nothing can be taken. */
    public IItemHandler getPipeHandler() {
        return pipeHandler;
    }

    public int getChanges() {
        return changes;
    }

    /** {@code findRecipe}: the result of the grid as it is. */
    public ItemStack findRecipe() {
        List<ItemStack> items = new ArrayList<>(SIZE);
        for (int i = 0; i < SIZE; i++) {
            items.add(grid.getStackInSlot(i));
        }
        return craft(items);
    }

    private ItemStack craft(List<ItemStack> items) {
        if (level == null) {
            return ItemStack.EMPTY;
        }
        CraftingInput input = CraftingInput.of(3, 3, items);
        return level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level)
                .map(recipe -> recipe.value().assemble(input, level.registryAccess()))
                .orElse(ItemStack.EMPTY);
    }

    @Override
    public ItemStack extractItem(boolean doRemove) {
        return extractItem(doRemove, false);
    }

    /**
     * {@code extractItem(doRemove, removeRecipe)}: one item per filled slot. Without {@code removeRecipe} (pipes) one
     * item stays as pattern and a slot holding just that one is served from a neighbouring inventory instead. Missing
     * ingredients or no recipe take nothing. The ingredients are only looked up first (the original took and put them
     * back) and taken once the recipe is known.
     */
    public ItemStack extractItem(boolean doRemove, boolean removeRecipe) {
        int itemsToLeave = removeRecipe ? 0 : 1;
        List<Pointer> pointers = new ArrayList<>();
        List<ItemStack> items = new ArrayList<>(SIZE);
        for (int i = 0; i < SIZE; i++) {
            ItemStack stack = grid.getStackInSlot(i);
            if (stack.isEmpty()) {
                items.add(ItemStack.EMPTY);
                continue;
            }
            Pointer pointer = stack.getCount() <= itemsToLeave ? findNearby(stack, pointers)
                    : new Pointer(grid, null, i, stack.copyWithCount(1));
            if (pointer == null) {
                return ItemStack.EMPTY;
            }
            pointers.add(pointer);
            items.add(pointer.item);
        }
        ItemStack result = craft(items);
        if (result.isEmpty() || !doRemove) {
            return result;
        }
        for (Pointer pointer : pointers) {
            pointer.inventory.extractItem(pointer.slot, 1, false);
        }
        for (Pointer pointer : pointers) {
            ItemStack remainder = pointer.item.getCraftingRemainingItem();
            if (!remainder.isEmpty()) {
                putBack(pointer.inventory, pointer.slot, remainder);
            }
        }
        return result;
    }

    /**
     * The container item (bucket …) goes back to the slot the ingredient came from; if that slot is not empty any more
     * it goes anywhere into that inventory, else it drops (the original overwrote the slot, spec 20 §2.2).
     */
    private void putBack(IItemHandler inventory, int slot, ItemStack remainder) {
        ItemStack rest = inventory.insertItem(slot, remainder, false);
        if (!rest.isEmpty()) {
            rest = ItemHandlerHelper.insertItemStacked(inventory, rest, false);
        }
        if (!rest.isEmpty() && level != null) {
            Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY() + 1, worldPosition.getZ(), rest);
        }
    }

    /**
     * {@code getNearbyItem}: one matching item (same item, damage and components ignored) from the first neighbour
     * inventory that still has one beyond those already reserved; other special inventories (automatic crafting
     * tables) are skipped.
     */
    @Nullable
    private Pointer findNearby(ItemStack wanted, List<Pointer> reserved) {
        if (level == null) {
            return null;
        }
        for (Direction direction : NEIGHBOURS) {
            BlockPos pos = worldPosition.relative(direction);
            BlockEntity neighbour = level.getBlockEntity(pos);
            if (neighbour == null || neighbour instanceof SpecialInventory) {
                continue;
            }
            IItemHandler inventory = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, direction.getOpposite());
            if (inventory == null) {
                continue;
            }
            for (int slot = 0; slot < inventory.getSlots(); slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (stack.isEmpty() || !stack.is(wanted.getItem())) {
                    continue;
                }
                int taken = 1;
                for (Pointer pointer : reserved) {
                    if (pos.equals(pointer.pos) && pointer.slot == slot) {
                        taken++;
                    }
                }
                if (inventory.extractItem(slot, taken, true).getCount() >= taken) {
                    return new Pointer(inventory, pos, slot, stack.copyWithCount(1));
                }
            }
        }
        return null;
    }

    /** {@code StackPointer}: where an ingredient comes from ({@code pos} = neighbour, {@code null} for the grid). */
    private record Pointer(IItemHandler inventory, @Nullable BlockPos pos, int slot, ItemStack item) {
    }

    public void dropContents() {
        if (level == null) {
            return;
        }
        for (int i = 0; i < SIZE; i++) {
            ItemStack stack = grid.getStackInSlot(i);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX(), worldPosition.getY(), worldPosition.getZ(), stack);
                grid.setStackInSlot(i, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public Component getDisplayName() {
        return getBlockState().getBlock().getName();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new AutoWorkbenchMenu(id, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("stackList", grid.serializeNBT(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        grid.deserializeNBT(registries, tag.getCompound("stackList"));
    }

    /**
     * {@code TransactorRoundRobin}: item by item into the filled slot with the same item and the fewest items (the first
     * slot with a single item ends the search); empty slots stay empty so the recipe is kept. Simulation is exact.
     */
    private final class RoundRobinHandler implements IItemHandler {
        @Override
        public int getSlots() {
            return SIZE;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return grid.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (stack.isEmpty()) {
                return ItemStack.EMPTY;
            }
            int[] counts = new int[SIZE];
            for (int i = 0; i < SIZE; i++) {
                counts[i] = grid.getStackInSlot(i).getCount();
            }
            int added = 0;
            while (added < stack.getCount()) {
                int minCount = Integer.MAX_VALUE;
                int minSlot = -1;
                for (int i = 0; i < SIZE && minCount > 1; i++) {
                    ItemStack inSlot = grid.getStackInSlot(i);
                    if (!inSlot.isEmpty() && counts[i] < Math.min(inSlot.getMaxStackSize(), 64)
                            && counts[i] < minCount && ItemStack.isSameItemSameComponents(inSlot, stack)) {
                        minCount = counts[i];
                        minSlot = i;
                    }
                }
                if (minSlot < 0) {
                    break;
                }
                counts[minSlot]++;
                added++;
            }
            if (!simulate && added > 0) {
                for (int i = 0; i < SIZE; i++) {
                    ItemStack inSlot = grid.getStackInSlot(i);
                    if (!inSlot.isEmpty() && counts[i] != inSlot.getCount()) {
                        grid.setStackInSlot(i, inSlot.copyWithCount(counts[i]));
                    }
                }
            }
            return stack.copyWithCount(stack.getCount() - added);
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
            return true;
        }
    }
}
