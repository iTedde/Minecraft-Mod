package dev.craft.gregtech.machine;

import dev.craft.gregtech.GtBlockEntityTypes;
import dev.craft.gregtech.GtMachine;
import dev.craft.gregtech.menu.GtTankMachineMenu;
import dev.craft.gregtech.recipe.GtRecipe;
import dev.craft.ic2.machine.SideAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.Nullable;

/**
 * Industrial electrolyzer and centrifuge (spec 13 §4, §9): two inputs, four outputs and a one-fluid tank whose
 * contents appear as holo items in slot 6, which the machine can also use as recipe input. Not a basic machine: no
 * facing, energy from all sides, no battery slot.
 */
public abstract class GtTankMachineBlockEntity extends GtMachineBlockEntity {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_CELLS = 1;
    public static final int SLOT_OUTPUT_1 = 2;
    public static final int SLOT_TANK = 6;
    public static final int OUTPUTS = 4;
    public static final int TANK_CAPACITY = 10000;

    private final GtMachine machine;
    private int progressTime;
    private int maxProgressTime;
    private int euPerTick;
    private int storedLiquid;
    private int displayErrorCode;
    private final ItemStack[] outputItems = {ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};
    private final IFluidHandler tank = new Tank();

    protected GtTankMachineBlockEntity(GtMachine machine, BlockPos pos, BlockState state) {
        super(GtBlockEntityTypes.get(machine), pos, state, 7);
        this.machine = machine;
    }

    /** Recipe list of the machine. */
    protected abstract GtRecipe.Kind recipeKind();

    /** The only fluid the tank accepts. */
    protected abstract Fluid tankFluid();

    /** Holo item standing for 1000 mB in slot 6. */
    protected abstract Item holoItem();

    /** NBT key of the stored fluid amount ({@code mStoredWater}/{@code mStoredLava}). */
    protected abstract String storedLiquidKey();

    // --- values ---

    @Override
    public int maxEUInput() {
        return machine.getMaxInput();
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
        ItemStack holo = inventory.get(SLOT_TANK);
        if (storedLiquid >= 1000 && holo.getCount() < 64) {
            storedLiquid -= 1000;
            inventory.set(SLOT_TANK, holo.isEmpty() ? new ItemStack(holoItem()) : holo.copyWithCount(
                    holo.getCount() + 1));
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

    /** Recipe for (slot 0, slot 1), otherwise for (tank contents, slot 1). */
    private void checkRecipe() {
        if (!tryStart(SLOT_INPUT)) {
            tryStart(SLOT_TANK);
        }
    }

    private boolean tryStart(int inputSlot) {
        ItemStack input = inventory.get(inputSlot);
        ItemStack cells = inventory.get(SLOT_CELLS);
        GtRecipe recipe = GtRecipe.find(level, recipeKind(), input, cells, false);
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

    /** Progress scale 0–10 ({@code GT_Container_Electrolyzer}/{@code GT_Container_Centrifuge}). */
    public int getProgressScale() {
        return Math.max(0, Math.min(10, (progressTime > 0 ? 1 : 0)
                + progressTime * 10 / (maxProgressTime < 1 ? 1 : maxProgressTime)));
    }

    public int getDisplayErrorCode() {
        return displayErrorCode;
    }

    /** Fluid in the tank in mB (without the holo items). */
    public int getStoredLiquid() {
        return storedLiquid;
    }

    // --- inventory and tank ---

    /**
     * {@code getInvSideIndex}/{@code getInvSideLength}: top → slot 0, bottom → slot 1, sides → outputs.
     * The output sides also put recipe inputs into the input slots (deviation, see {@code abweichungen.md}).
     */
    @Override
    public IItemHandler getItemHandler(@Nullable Direction side) {
        if (side == null) {
            return new RangedWrapper(inventory, SLOT_INPUT, SLOT_TANK);
        }
        return switch (side) {
            case UP -> new RangedWrapper(inventory, SLOT_INPUT, SLOT_INPUT + 1);
            case DOWN -> new RangedWrapper(inventory, SLOT_CELLS, SLOT_CELLS + 1);
            default -> new SideAccess(inventory)
                    .input(SLOT_INPUT, stack -> GtRecipe.isInput(level, recipeKind(), 0, stack))
                    .input(SLOT_CELLS, stack -> !GtRecipe.isInput(level, recipeKind(), 0, stack)
                            && GtRecipe.isInput(level, recipeKind(), 1, stack))
                    .outputs(SLOT_OUTPUT_1, SLOT_OUTPUT_1 + OUTPUTS - 1);
        };
    }

    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        return tank;
    }

    /** Slots 0–5 ({@code isValidSlot}); the holo items are lost. */
    @Override
    public void dropContents() {
        inventory.set(SLOT_TANK, ItemStack.EMPTY);
        super.dropContents();
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new GtTankMachineMenu(id, playerInventory, this);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        euPerTick = tag.getInt("mEUt");
        storedLiquid = tag.getInt(storedLiquidKey());
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
        tag.putInt(storedLiquidKey(), storedLiquid);
        tag.putInt("mProgresstime", progressTime);
        tag.putInt("mMaxProgresstime", maxProgressTime);
        for (int i = 0; i < OUTPUTS; i++) {
            if (!outputItems[i].isEmpty()) {
                tag.put("mOutputItem" + (i + 1), outputItems[i].save(registries));
            }
        }
    }

    /** One fluid only, 10 000 mB, fill from every side, never drained. */
    private final class Tank implements IFluidHandler {
        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int index) {
            return storedLiquid > 0 ? new FluidStack(tankFluid(), storedLiquid) : FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int index) {
            return TANK_CAPACITY;
        }

        @Override
        public boolean isFluidValid(int index, FluidStack stack) {
            return stack.is(tankFluid());
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.isEmpty() || !resource.is(tankFluid())) {
                return 0;
            }
            int filled = Math.min(resource.getAmount(), TANK_CAPACITY - storedLiquid);
            if (action.execute() && filled > 0) {
                storedLiquid += filled;
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
