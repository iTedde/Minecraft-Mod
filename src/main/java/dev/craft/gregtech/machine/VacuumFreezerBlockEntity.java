package dev.craft.gregtech.machine;

import dev.craft.core.multiblock.BlockMatcher;
import dev.craft.core.multiblock.MultiblockPattern;
import dev.craft.gregtech.GtBlockEntityTypes;
import dev.craft.gregtech.GtMachine;
import dev.craft.gregtech.block.GtCasingBlock;
import dev.craft.gregtech.menu.GtVacuumFreezerMenu;
import dev.craft.gregtech.recipe.GtRecipe;
import dev.craft.ic2.machine.SideAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.RangedWrapper;
import org.jetbrains.annotations.Nullable;

/**
 * {@code GT_MetaTileEntity_VacuumFreezer} (spec 13 §11): sits on top of a hollow 3 × 3 × 3 cube of machine casings and
 * cools coolant cells, hot tungstensteel ingots and water cells.
 */
public class VacuumFreezerBlockEntity extends GtMachineBlockEntity {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    private static final int UPDATE_DELAY = 5;

    /** Advanced casings on the six face centres, reinforced casings on edges and corners, air in the middle. */
    public static final MultiblockPattern PATTERN = MultiblockPattern.builder()
            .layer("RRR", "RAR", "RRR")
            .layer("RAR", "A_A", "RAR")
            .layer("RRR", "RAR", "RRR")
            .layer("   ", " K ", "   ")
            .controller('K', BlockMatcher.ANY)
            .where('R', casing(GtCasingBlock.Type.REINFORCED))
            .where('A', casing(GtCasingBlock.Type.ADVANCED))
            .where('_', BlockMatcher.state(BlockState::isAir))
            .build();

    private int progressTime;
    private int maxProgressTime;
    private int euPerTick;
    private int update = UPDATE_DELAY;
    private boolean machine;
    private int displayErrorCode;
    private ItemStack outputItem1 = ItemStack.EMPTY;

    public VacuumFreezerBlockEntity(BlockPos pos, BlockState state) {
        super(GtBlockEntityTypes.get(GtMachine.VACUUM_FREEZER), pos, state, 2);
    }

    private static BlockMatcher casing(GtCasingBlock.Type type) {
        return BlockMatcher.state(state -> state.getBlock() instanceof GtCasingBlock block && block.getType() == type);
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
        return facing == Direction.DOWN;
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

    private boolean checkMachine() {
        return PATTERN.match(level, worldPosition, Direction.NORTH).isPresent();
    }

    // --- tick ---

    @Override
    protected void onPostTick() {
        if (update-- == 0) {
            machine = checkMachine();
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
                    addOutput(outputItem1);
                    outputItem1 = ItemStack.EMPTY;
                    progressTime = 0;
                    maxProgressTime = 0;
                }
            }
            setChanged();
        } else if (getStoredEnergy() > 100) {
            checkRecipe();
        }
    }

    private void checkRecipe() {
        if (!machine) {
            return;
        }
        ItemStack input = inventory.get(SLOT_INPUT);
        GtRecipe recipe = GtRecipe.find(level, GtRecipe.Kind.VACUUM_FREEZER, input, ItemStack.EMPTY, false);
        if (recipe == null || !spaceForOutput(recipe.getOutput(0))
                || !recipe.isInputEqual(true, true, input, ItemStack.EMPTY)) {
            return;
        }
        inventory.set(SLOT_INPUT, input.isEmpty() ? ItemStack.EMPTY : input);
        maxProgressTime = recipe.getDuration();
        euPerTick = recipe.getEuPerTick();
        outputItem1 = recipe.getOutput(0).copy();
        setChanged();
    }

    private boolean spaceForOutput(ItemStack product) {
        ItemStack current = inventory.get(SLOT_OUTPUT);
        return current.isEmpty() || product.isEmpty() || current.getCount() + product.getCount()
                <= current.getMaxStackSize() && ItemStack.isSameItemSameComponents(current, product);
    }

    /** Puts {@code product} into the output slot; anything beyond the stack limit is lost (GT behaviour). */
    private void addOutput(ItemStack product) {
        if (product.isEmpty()) {
            return;
        }
        ItemStack current = inventory.get(SLOT_OUTPUT);
        if (current.isEmpty()) {
            inventory.set(SLOT_OUTPUT, product.copy());
        } else if (ItemStack.isSameItemSameComponents(current, product)) {
            inventory.set(SLOT_OUTPUT, current.copyWithCount(Math.min(product.getMaxStackSize(),
                    product.getCount() + current.getCount())));
        }
    }

    // --- GUI values ---

    /** Progress arrow width 0–20 ({@code GT_Container_VacuumFreezer}). */
    public int getProgressScale() {
        return Math.max(0, Math.min(20, (progressTime > 0 ? 1 : 0)
                + progressTime * 20 / (maxProgressTime < 1 ? 1 : maxProgressTime)));
    }

    public boolean isMachine() {
        return machine;
    }

    public int getProgressTime() {
        return progressTime;
    }

    public int getMaxProgressTime() {
        return maxProgressTime;
    }

    public int getDisplayErrorCode() {
        return displayErrorCode;
    }

    // --- inventory ---

    /**
     * {@code getInvSideIndex}/{@code getInvSideLength} on absolute sides: top → input, everything else → output.
     * The output sides also put recipe inputs into the input slots (deviation, see {@code abweichungen.md}).
     */
    @Override
    public IItemHandler getItemHandler(@Nullable Direction side) {
        if (side == null) {
            return inventory;
        }
        return side == Direction.UP ? new RangedWrapper(inventory, SLOT_INPUT, SLOT_INPUT + 1)
                : new SideAccess(inventory)
                        .input(SLOT_INPUT, stack -> GtRecipe.isInput(level, GtRecipe.Kind.VACUUM_FREEZER, 0, stack))
                        .outputs(SLOT_OUTPUT, SLOT_OUTPUT);
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new GtVacuumFreezerMenu(id, playerInventory, this);
    }

    // --- persistence (GT field names) ---

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        update = UPDATE_DELAY;
        euPerTick = tag.getInt("mEUt");
        progressTime = tag.getInt("mProgresstime");
        maxProgressTime = tag.getInt("mMaxProgresstime");
        outputItem1 = ItemStack.parseOptional(registries, tag.getCompound("mOutputItem1"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("mEUt", euPerTick);
        tag.putInt("mProgresstime", progressTime);
        tag.putInt("mMaxProgresstime", maxProgressTime);
        if (!outputItem1.isEmpty()) {
            tag.put("mOutputItem1", outputItem1.save(registries));
        }
    }
}
