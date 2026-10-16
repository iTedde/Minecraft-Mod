package dev.craft.forestry.machine;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import dev.craft.forestry.FrBlockEntityTypes;
import dev.craft.forestry.FrRecipes;
import dev.craft.forestry.menu.FabricatorMenu;
import dev.craft.forestry.recipe.FabricatorRecipe;
import dev.craft.forestry.recipe.FabricatorSmelting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

/**
 * Forestry {@code MachineFabricator} (Thermionic Fabricator): heats up by 25 per work step, melts glass, panes and
 * sand into liquid glass and crafts the shaped recipes with liquid glass when the result is taken.
 */
public class FabricatorBlockEntity extends ForestryMachineBlockEntity {
    public static final int MAX_HEAT = 5000;
    public static final int TANK_CAPACITY = 2000;
    public static final int SLOT_METAL = 0;
    public static final int SLOT_PLAN = 1;
    public static final int SLOT_RESULT = 2;
    public static final int SLOT_CRAFTING_1 = 3;
    public static final int SLOT_INVENTORY_1 = 12;
    public static final int SLOT_INVENTORY_COUNT = 18;
    public static final int SLOT_COUNT = 30;

    private final FluidTank moltenTank = new FluidTank(TANK_CAPACITY) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };
    private int heat;
    private FluidStack pendingSmelt = FluidStack.EMPTY;
    private final IItemHandler automation = new Automation(null);

    public FabricatorBlockEntity(BlockPos pos, BlockState state) {
        super(FrBlockEntityTypes.FABRICATOR.get(), pos, state, SLOT_COUNT, 5, 110, 25, 250);
    }

    public FluidTank getMoltenTank() {
        return moltenTank;
    }

    /** {@code MachineFabricator.isWorking}: not above the maximum heat. */
    @Override
    public boolean isWorking() {
        return heat <= MAX_HEAT;
    }

    public int getHeat() {
        return heat;
    }

    public IItemHandler getItemHandler(@Nullable Direction side) {
        return side == null ? automation : new Automation(side);
    }

    // --- machine ---

    @Override
    protected void updateServerSide() {
        if (!moltenTank.isEmpty()) {
            FabricatorSmelting smelt = FabricatorSmelting.find(moltenTank.getFluid());
            if (smelt != null && heat < smelt.meltingPoint()) {
                moltenTank.drain(5, IFluidHandler.FluidAction.EXECUTE);
            }
        }
        if (!pendingSmelt.isEmpty()) {
            int filled = moltenTank.fill(pendingSmelt.copy(), IFluidHandler.FluidAction.EXECUTE);
            pendingSmelt.shrink(filled);
            if (pendingSmelt.getAmount() <= 0) {
                pendingSmelt = FluidStack.EMPTY;
            }
            setChanged();
        } else if (moltenTank.getFluidAmount() < moltenTank.getCapacity()
                && !inventory.getStackInSlot(SLOT_METAL).isEmpty()) {
            FabricatorSmelting smelt = FabricatorSmelting.find(inventory.getStackInSlot(SLOT_METAL));
            if (smelt != null && smelt.meltingPoint() <= heat) {
                inventory.extractItem(SLOT_METAL, 1, false);
                pendingSmelt = smelt.product();
            }
        }
        if (heat > 2500) {
            heat -= 2;
        } else if (heat > 0) {
            heat--;
        }
    }

    /** {@code doWork = addHeat(25)}: nothing (and no energy used) once the heat is at its maximum. */
    @Override
    protected boolean machineWork() {
        if (heat >= MAX_HEAT) {
            return false;
        }
        heat = Math.min(MAX_HEAT, heat + 25);
        return true;
    }

    /** {@code getMeltingPoint}: of the liquid in the tank, else of the item in the melting slot, else 0. */
    public int getMeltingPoint() {
        FabricatorSmelting smelt = null;
        if (!moltenTank.isEmpty()) {
            smelt = FabricatorSmelting.find(moltenTank.getFluid());
        } else if (!inventory.getStackInSlot(SLOT_METAL).isEmpty()) {
            smelt = FabricatorSmelting.find(inventory.getStackInSlot(SLOT_METAL));
        }
        return smelt == null ? 0 : smelt.meltingPoint();
    }

    // --- crafting ---

    private List<ItemStack> craftingGrid() {
        List<ItemStack> grid = new ArrayList<>(9);
        for (int i = 0; i < 9; i++) {
            grid.add(inventory.getStackInSlot(SLOT_CRAFTING_1 + i));
        }
        return grid;
    }

    private Optional<FabricatorRecipe> findRecipe() {
        if (level == null) {
            return Optional.empty();
        }
        FabricatorRecipe.Input input = new FabricatorRecipe.Input(inventory.getStackInSlot(SLOT_PLAN),
                moltenTank.getFluid(), CraftingInput.of(3, 3, craftingGrid()));
        return level.getRecipeManager().getRecipeFor(FrRecipes.FABRICATOR.get(), input, level)
                .map(RecipeHolder::value);
    }

    /** {@code getResult}: what the result slot shows. */
    public ItemStack getResult() {
        return findRecipe().map(recipe -> recipe.getResult().copy()).orElse(ItemStack.EMPTY);
    }

    /**
     * {@code takeResult}: one set of the grid ingredients from the stock, or – only if {@code consumeRecipe} – one
     * item from every filled grid slot; the liquid glass of the recipe is drained.
     */
    public ItemStack takeResult(boolean consumeRecipe) {
        Optional<FabricatorRecipe> found = findRecipe();
        if (found.isEmpty()) {
            return ItemStack.EMPTY;
        }
        FabricatorRecipe recipe = found.get();
        List<ItemStack> grid = craftingGrid();
        int stockEnd = SLOT_INVENTORY_1 + SLOT_INVENTORY_COUNT;
        if (ItemSets.removeSet(inventory, SLOT_INVENTORY_1, stockEnd, grid, false)) {
            ItemSets.removeSet(inventory, SLOT_INVENTORY_1, stockEnd, grid, true);
        } else {
            if (!consumeRecipe) {
                return ItemStack.EMPTY;
            }
            for (int i = 0; i < 9; i++) {
                if (!inventory.getStackInSlot(SLOT_CRAFTING_1 + i).isEmpty()) {
                    inventory.extractItem(SLOT_CRAFTING_1 + i, 1, false);
                }
            }
        }
        moltenTank.drain(recipe.getMolten().getAmount(), IFluidHandler.FluidAction.EXECUTE);
        return recipe.getResult().copy();
    }

    // --- menu, drops, persistence ---

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new FabricatorMenu(id, playerInventory, this);
    }

    @Override
    protected int[] droppedSlots() {
        int[] slots = new int[SLOT_COUNT];
        for (int i = 0; i < SLOT_COUNT; i++) {
            slots[i] = i;
        }
        return slots;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("Heat", heat);
        if (!moltenTank.isEmpty()) {
            tag.put("MoltenTank", moltenTank.getFluid().save(registries));
        }
        if (!pendingSmelt.isEmpty()) {
            tag.put("PendingSmelt", pendingSmelt.save(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        heat = tag.getInt("Heat");
        moltenTank.setFluid(FluidStack.parseOptional(registries, tag.getCompound("MoltenTank")));
        pendingSmelt = FluidStack.parseOptional(registries, tag.getCompound("PendingSmelt"));
    }

    /**
     * BuildCraft {@code ISpecialInventory} access: slot 0 delivers the result if the stock holds the ingredients
     * ({@code extractItem}); inserting puts meltable items coming from above into the melting slot and everything
     * else into the stock ({@code addItem}).
     */
    private final class Automation implements IItemHandler {
        @Nullable
        private final Direction side;

        Automation(@Nullable Direction side) {
            this.side = side;
        }

        @Override
        public int getSlots() {
            return 1 + SLOT_INVENTORY_COUNT + 1;
        }

        /** 0 = result, 1 = melting slot, 2.. = stock. */
        private int inventorySlot(int slot) {
            return slot == 1 ? SLOT_METAL : SLOT_INVENTORY_1 + slot - 2;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            if (slot == 0) {
                List<ItemStack> grid = craftingGrid();
                return ItemSets.removeSet(inventory, SLOT_INVENTORY_1, SLOT_INVENTORY_1 + SLOT_INVENTORY_COUNT,
                        grid, false) ? getResult() : ItemStack.EMPTY;
            }
            return inventory.getStackInSlot(inventorySlot(slot));
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            return isItemValid(slot, stack) ? inventory.insertItem(inventorySlot(slot), stack, simulate) : stack;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot != 0 || amount <= 0) {
                return ItemStack.EMPTY;
            }
            ItemStack result = getStackInSlot(0);
            if (result.isEmpty() || amount < result.getCount()) {
                return ItemStack.EMPTY;
            }
            return simulate ? result : takeResult(false);
        }

        @Override
        public int getSlotLimit(int slot) {
            return 64;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            if (slot == 0) {
                return false;
            }
            boolean meltsFromAbove = side == Direction.UP && FabricatorSmelting.find(stack) != null;
            return slot == 1 ? meltsFromAbove : !meltsFromAbove;
        }
    }
}
