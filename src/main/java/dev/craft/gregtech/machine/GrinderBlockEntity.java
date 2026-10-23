package dev.craft.gregtech.machine;

import dev.craft.core.multiblock.BlockMatcher;
import dev.craft.core.multiblock.MultiblockPattern;
import dev.craft.gregtech.GtBlockEntityTypes;
import dev.craft.gregtech.GtMachine;
import dev.craft.gregtech.block.GtCasingBlock;
import dev.craft.gregtech.menu.GtGrinderMenu;
import dev.craft.gregtech.recipe.GtRecipe;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.machine.SideAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.Nullable;

/**
 * {@code GT_MetaTileEntity_Grinder} (spec 13 §10): controller in front of a 3 × 3 × 3 cube of casings around a water
 * block. Grinds an ore with a cell from slot 1, or – if no recipe matches – with water cells drawn from its tank.
 */
public class GrinderBlockEntity extends GtMachineBlockEntity {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_CELL = 1;
    public static final int SLOT_OUTPUT_1 = 2;
    public static final int OUTPUTS = 4;
    public static final int TANK_CAPACITY = 10000;
    private static final int UPDATE_DELAY = 5;

    /**
     * Written for a controller facing north: the cube lies behind it (south), its middle layer at the controller's
     * height holds reinforced casings around the water core, top and bottom layers standard casings.
     */
    public static final MultiblockPattern PATTERN = MultiblockPattern.builder()
            .layer("   ", "SSS", "SSS", "SSS")
            .layer(" K ", "RRR", "RWR", "RRR")
            .layer("   ", "SSS", "SSS", "SSS")
            .controller('K', BlockMatcher.ANY)
            .where('S', casing(GtCasingBlock.Type.STANDARD))
            .where('R', casing(GtCasingBlock.Type.REINFORCED))
            .where('W', BlockMatcher.blocks(Blocks.WATER))
            .build();

    private final IFluidHandler tank = new Tank();
    private int progressTime;
    private int maxProgressTime;
    private int euPerTick;
    private int waterAmount;
    private int update = UPDATE_DELAY;
    private boolean machine;
    private int displayErrorCode;
    private final ItemStack[] outputItems = {ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY};

    public GrinderBlockEntity(BlockPos pos, BlockState state) {
        super(GtBlockEntityTypes.get(GtMachine.INDUSTRIAL_GRINDER), pos, state, 2 + OUTPUTS);
    }

    private static BlockMatcher casing(GtCasingBlock.Type type) {
        return BlockMatcher.state(state -> state.getBlock() instanceof GtCasingBlock casing
                && casing.getType() == type);
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
    public boolean isFacingValid(Direction facing) {
        return facing.getAxis().isHorizontal();
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

    // --- structure ---

    @Override
    public void onMachineBlockUpdate() {
        update = UPDATE_DELAY;
    }

    // --- tick ---

    @Override
    protected void onPostTick() {
        if (update-- == 0) {
            machine = PATTERN.match(level, worldPosition, getFacing()).isPresent();
        }
        setActive(machine);
        if (machine && maxProgressTime > 0) {
            if (progressTime >= 0 && !decreaseStoredEnergy(euPerTick * (int) Math.pow(4.0, getOverclockers()),
                    false)) {
                // constant_need_of_energy: the recipe falls back to just before its start.
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
        } else if (getStoredEnergy() > 100) {
            checkRecipe();
        }
    }

    /** Recipe for (slot 0, slot 1); only if there is none, for (slot 0, water cells from the tank). */
    private void checkRecipe() {
        if (!machine) {
            return;
        }
        ItemStack input = inventory.get(SLOT_INPUT);
        ItemStack cells = inventory.get(SLOT_CELL);
        GtRecipe recipe = GtRecipe.find(level, GtRecipe.Kind.GRINDER, input, cells, false);
        if (recipe != null) {
            if (spaceForOutput(recipe) && recipe.isInputEqual(false, true, input, cells)) {
                inventory.set(SLOT_INPUT, input.isEmpty() ? ItemStack.EMPTY : input);
                inventory.set(SLOT_CELL, cells.isEmpty() ? ItemStack.EMPTY : cells);
                start(recipe, false);
            }
            return;
        }
        if (waterAmount < 1000) {
            return;
        }
        ItemStack water = new ItemStack(Ic2Items.WATER_CELL.get(), waterAmount / 1000);
        recipe = GtRecipe.find(level, GtRecipe.Kind.GRINDER, input, water, false);
        // The empty cell output is dropped below, but its space is still checked (GT quirk).
        if (recipe != null && spaceForOutput(recipe) && recipe.isInputEqual(false, false, input, water)) {
            input.shrink(recipe.getInputs().get(0).count());
            inventory.set(SLOT_INPUT, input.isEmpty() ? ItemStack.EMPTY : input);
            waterAmount -= recipe.getInputs().get(1).count() * 1000;
            start(recipe, true);
        }
    }

    private void start(GtRecipe recipe, boolean fromTank) {
        maxProgressTime = recipe.getDuration();
        euPerTick = recipe.getEuPerTick();
        for (int i = 0; i < OUTPUTS; i++) {
            outputItems[i] = recipe.getOutput(i).copy();
        }
        if (fromTank && outputItems[3].is(Ic2Items.EMPTY_CELL.get())) {
            outputItems[3] = ItemStack.EMPTY;
        }
        setChanged();
    }

    private boolean spaceForOutput(GtRecipe recipe) {
        for (int i = 0; i < OUTPUTS; i++) {
            if (!fits(inventory.get(SLOT_OUTPUT_1 + i), recipe.getOutput(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean fits(ItemStack current, ItemStack product) {
        return current.isEmpty() || product.isEmpty() || current.getCount() + product.getCount()
                <= current.getMaxStackSize() && ItemStack.isSameItemSameComponents(current, product);
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

    /** Progress arrow width 0–20 ({@code GT_Container_Grinder}). */
    public int getProgressScale() {
        return Math.max(0, Math.min(20, (progressTime > 0 ? 1 : 0)
                + progressTime * 20 / (maxProgressTime < 1 ? 1 : maxProgressTime)));
    }

    public boolean isMachine() {
        return machine;
    }

    public int getDisplayErrorCode() {
        return displayErrorCode;
    }

    public int getProgressTime() {
        return progressTime;
    }

    public int getWaterAmount() {
        return waterAmount;
    }

    // --- inventory and tank ---

    /**
     * {@code getInvSideIndex}/{@code getInvSideLength}: top → slot 0, bottom → slot 1, sides → outputs.
     * The output sides also put recipe inputs into the input slots (deviation, see {@code abweichungen.md}).
     */
    @Override
    public IItemHandler getItemHandler(@Nullable Direction side) {
        if (side == null) {
            return inventory;
        }
        return switch (side) {
            case UP -> new RangedWrapper(inventory, SLOT_INPUT, SLOT_INPUT + 1);
            case DOWN -> new RangedWrapper(inventory, SLOT_CELL, SLOT_CELL + 1);
            default -> new SideAccess(inventory)
                    .input(SLOT_INPUT, stack -> GtRecipe.isInput(level, GtRecipe.Kind.GRINDER, 0, stack))
                    .input(SLOT_CELL, stack -> !GtRecipe.isInput(level, GtRecipe.Kind.GRINDER, 0, stack)
                            && GtRecipe.isInput(level, GtRecipe.Kind.GRINDER, 1, stack))
                    .outputs(SLOT_OUTPUT_1, SLOT_OUTPUT_1 + OUTPUTS - 1);
        };
    }

    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        return tank;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new GtGrinderMenu(id, playerInventory, this);
    }

    // --- persistence (GT field names) ---

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        update = UPDATE_DELAY;
        euPerTick = tag.getInt("mEUt");
        progressTime = tag.getInt("mProgresstime");
        maxProgressTime = tag.getInt("mMaxProgresstime");
        waterAmount = tag.getInt("mWaterAmount");
        for (int i = 0; i < OUTPUTS; i++) {
            outputItems[i] = ItemStack.parseOptional(registries, tag.getCompound("mOutputItem" + (i + 1)));
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("mEUt", euPerTick);
        tag.putInt("mProgresstime", progressTime);
        tag.putInt("mMaxProgresstime", maxProgressTime);
        tag.putInt("mWaterAmount", waterAmount);
        for (int i = 0; i < OUTPUTS; i++) {
            if (!outputItems[i].isEmpty()) {
                tag.put("mOutputItem" + (i + 1), outputItems[i].save(registries));
            }
        }
    }

    /** Water only, 10 000 mB, fill from every side, never drained ({@code getTankPressure} −100). */
    private final class Tank implements IFluidHandler {
        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int index) {
            return waterAmount > 0 ? new FluidStack(Fluids.WATER, waterAmount) : FluidStack.EMPTY;
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
            int filled = Math.min(resource.getAmount(), TANK_CAPACITY - waterAmount);
            if (action.execute() && filled > 0) {
                waterAmount += filled;
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
