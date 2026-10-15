package dev.craft.gregtech.machine;

import dev.craft.gregtech.GtBlockEntityTypes;
import dev.craft.gregtech.GtItems;
import dev.craft.gregtech.GtMachine;
import dev.craft.gregtech.menu.GtElectrolyzerMenu;
import dev.craft.gregtech.recipe.GtRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.Nullable;

/**
 * {@code GT_MetaTileEntity_Electrolyzer} (spec 13 §4): two inputs, four outputs and a water tank whose water appears
 * as "water" items in the holo slot 6. Not a basic machine: no facing, energy from all sides, no battery slot.
 */
public class ElectrolyzerBlockEntity extends GtMachineBlockEntity {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_CELLS = 1;
    public static final int SLOT_OUTPUT_1 = 2;
    public static final int SLOT_WATER = 6;
    public static final int OUTPUTS = 4;
    public static final int TANK_CAPACITY = 10000;

    private int progressTime;
    private int maxProgressTime;
    private int euPerTick;
    private int storedWater;
    private int displayErrorCode;
    private final ItemStack[] outputItems = {ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};
    private final IFluidHandler tank = new Tank();

    public ElectrolyzerBlockEntity(BlockPos pos, BlockState state) {
        super(GtBlockEntityTypes.get(GtMachine.INDUSTRIAL_ELECTROLYZER), pos, state, 7);
    }

    // --- values ---

    @Override
    public int maxEUInput() {
        return 128;
    }

    @Override
    public int maxEUStore() {
        return 10000;
    }

    @Override
    public int maxMJStore() {
        return maxEUStore();
    }

    @Override
    protected boolean isInputFacing(Direction side) {
        return true;
    }

    @Override
    public boolean isOverclockerUpgradable() {
        return true;
    }

    @Override
    public boolean isTransformerUpgradable() {
        return true;
    }

    @Override
    public boolean isBatteryUpgradable() {
        return true;
    }

    // --- tick ---

    @Override
    protected void onPostTick() {
        ItemStack water = inventory.get(SLOT_WATER);
        if (storedWater >= 1000 && water.getCount() < 64) {
            storedWater -= 1000;
            inventory.set(SLOT_WATER, water.isEmpty() ? new ItemStack(GtItems.WATER.get()) : water.copyWithCount(
                    water.getCount() + 1));
        }
        if (maxProgressTime > 0) {
            setActive(true);
            if (progressTime >= 0 && !decreaseStoredEnergy(euPerTick * (int) Math.pow(4.0, getOverclockers()),
                    false)) {
                // constant_need_of_energy: the recipe falls back to just before its start.
                setActive(false);
                progressTime = -10;
                displayErrorCode = 1;
            } else {
                progressTime += (int) Math.pow(2.0, getOverclockers());
                if (progressTime >= maxProgressTime) {
                    for (int i = 0; i < OUTPUTS; i++) {
                        addOutput(SLOT_OUTPUT_1 + i, outputItems[i]);
                        outputItems[i] = ItemStack.EMPTY;
                    }
                    progressTime = 0;
                    maxProgressTime = 0;
                    displayErrorCode = 0;
                }
            }
            setChanged();
        } else {
            setActive(false);
            if (getStoredEnergy() > 100) {
                checkRecipe();
            }
        }
    }

    /** Recipe for (slot 0, slot 1), otherwise for (tank water, slot 1). */
    private void checkRecipe() {
        if (!tryStart(SLOT_INPUT)) {
            tryStart(SLOT_WATER);
        }
    }

    private boolean tryStart(int inputSlot) {
        ItemStack input = inventory.get(inputSlot);
        ItemStack cells = inventory.get(SLOT_CELLS);
        GtRecipe recipe = GtRecipe.find(level, GtRecipe.Kind.ELECTROLYZER, input, cells, false);
        if (recipe == null || !spaceForOutput(recipe) || !recipe.isInputEqual(false, true, input, cells)) {
            return false;
        }
        inventory.set(inputSlot, input.isEmpty() ? ItemStack.EMPTY : input);
        inventory.set(SLOT_CELLS, cells.isEmpty() ? ItemStack.EMPTY : cells);
        maxProgressTime = recipe.getDuration();
        euPerTick = recipe.getEuPerTick();
        for (int i = 0; i < OUTPUTS; i++) {
            outputItems[i] = recipe.getOutput(i).copy();
        }
        setChanged();
        return true;
    }

    private boolean spaceForOutput(GtRecipe recipe) {
        for (int i = 0; i < OUTPUTS; i++) {
            ItemStack current = inventory.get(SLOT_OUTPUT_1 + i);
            ItemStack product = recipe.getOutput(i);
            if (!current.isEmpty() && !product.isEmpty() && (current.getCount() + product.getCount()
                    > current.getMaxStackSize() || !ItemStack.isSameItemSameComponents(current, product))) {
                return false;
            }
        }
        return true;
    }

    /** Puts {@code product} into {@code slot}; anything beyond the stack limit is lost (GT behaviour). */
    private void addOutput(int slot, ItemStack product) {
        if (product.isEmpty()) {
            return;
        }
        ItemStack current = inventory.get(slot);
        if (current.isEmpty()) {
            inventory.set(slot, product.copy());
        } else if (ItemStack.isSameItemSameComponents(current, product)) {
            inventory.set(slot, current.copyWithCount(Math.min(product.getMaxStackSize(),
                    product.getCount() + current.getCount())));
        }
    }

    // --- GUI values ---

    /** Progress bar height 0–10 ({@code GT_Container_Electrolyzer}). */
    public int getProgressScale() {
        return Math.max(0, Math.min(10, (progressTime > 0 ? 1 : 0)
                + progressTime * 10 / (maxProgressTime < 1 ? 1 : maxProgressTime)));
    }

    public int getDisplayErrorCode() {
        return displayErrorCode;
    }

    public int getStoredWater() {
        return storedWater;
    }

    // --- inventory and tank ---

    /** {@code getInvSideIndex}/{@code getInvSideLength}: top → slot 0, bottom → slot 1, sides → outputs. */
    @Override
    public IItemHandler getItemHandler(@Nullable Direction side) {
        if (side == null) {
            return new RangedWrapper(inventory, SLOT_INPUT, SLOT_WATER);
        }
        return switch (side) {
            case UP -> new RangedWrapper(inventory, SLOT_INPUT, SLOT_INPUT + 1);
            case DOWN -> new RangedWrapper(inventory, SLOT_CELLS, SLOT_CELLS + 1);
            default -> new RangedWrapper(inventory, SLOT_OUTPUT_1, SLOT_OUTPUT_1 + OUTPUTS);
        };
    }

    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        return tank;
    }

    /** Slots 0–5 ({@code isValidSlot}); the water in the holo slot is lost. */
    @Override
    public void dropContents() {
        inventory.set(SLOT_WATER, ItemStack.EMPTY);
        super.dropContents();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new GtElectrolyzerMenu(id, playerInventory, this);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        euPerTick = tag.getInt("mEUt");
        storedWater = tag.getInt("mStoredWater");
        progressTime = tag.getInt("mProgresstime");
        maxProgressTime = tag.getInt("mMaxProgresstime");
        for (int i = 0; i < OUTPUTS; i++) {
            outputItems[i] = ItemStack.parseOptional(registries, tag.getCompound("mOutputItem" + (i + 1)));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("mEUt", euPerTick);
        tag.putInt("mStoredWater", storedWater);
        tag.putInt("mProgresstime", progressTime);
        tag.putInt("mMaxProgresstime", maxProgressTime);
        for (int i = 0; i < OUTPUTS; i++) {
            if (!outputItems[i].isEmpty()) {
                tag.put("mOutputItem" + (i + 1), outputItems[i].save(registries));
            }
        }
    }

    /** Water only, 10 000 mB, fill from every side, never drained. */
    private final class Tank implements IFluidHandler {
        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int index) {
            return storedWater > 0 ? new FluidStack(Fluids.WATER, storedWater) : FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int index) {
            return TANK_CAPACITY;
        }

        @Override
        public boolean isFluidValid(int index, FluidStack stack) {
            return stack.is(Fluids.WATER);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || !resource.is(Fluids.WATER)) {
                return 0;
            }
            int filled = Math.min(resource.getAmount(), TANK_CAPACITY - storedWater);
            if (action.execute() && filled > 0) {
                storedWater += filled;
                setChanged();
            }
            return filled;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return FluidStack.EMPTY;
        }
    }
}
