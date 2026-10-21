package dev.craft.thermalexpansion.machine;

import dev.craft.thermalexpansion.TeBlockEntityTypes;
import dev.craft.thermalexpansion.recipe.TeRecipe;
import dev.craft.thermalexpansion.recipe.TeRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * {@code TileSmelter}: two inputs (slots 0, 1, any order), primary output over slots 2–3, secondary output slot 4 with a
 * chance. Side groups: 1 both inputs, 2 primary, 3 secondary, 4 both outputs, 5 input 1, 6 input 2, 7 open.
 */
public class SmelterBlockEntity extends TeMachineBlockEntity {
    public static final int SLOT_INPUT_1 = 0;
    public static final int SLOT_INPUT_2 = 1;
    public static final int SLOT_PRIMARY_1 = 2;
    public static final int SLOT_PRIMARY_2 = 3;
    public static final int SLOT_SECONDARY = 4;

    private ItemStack processInput2 = ItemStack.EMPTY;
    private int outputTrackerPrimary;
    private int outputTrackerSecondary;

    public SmelterBlockEntity(BlockPos pos, BlockState state) {
        super(TeBlockEntityTypes.INDUCTION_SMELTER.get(), TeMachineType.INDUCTION_SMELTER, pos, state);
    }

    @Override
    protected boolean canStart() {
        ItemStack first = inventory.get(SLOT_INPUT_1);
        ItemStack second = inventory.get(SLOT_INPUT_2);
        TeRecipe recipe = TeRecipes.findSmelter(level, first, second);
        if (recipe == null) {
            return false;
        }
        int primaryCount = recipe.getInputs().get(0).count();
        int secondaryCount = recipe.getInputs().get(1).count();
        if (recipe.getInputs().get(0).ingredient().test(first)) {
            if (primaryCount > first.getCount() || secondaryCount > second.getCount()) {
                return false;
            }
        } else if (primaryCount > second.getCount() || secondaryCount > first.getCount()) {
            return false;
        }
        return provider.getEnergyStored() >= recipe.getEnergy() && outputsFit(recipe);
    }

    @Override
    protected boolean canFinish() {
        if (processCur < processEnd) {
            return false;
        }
        TeRecipe recipe = TeRecipes.findSmelter(level, processInput, processInput2);
        if (recipe == null) {
            processCur = 0.0F;
            active = false;
            return false;
        }
        return outputsFit(recipe);
    }

    private boolean outputsFit(TeRecipe recipe) {
        ItemStack primary = recipe.getPrimary();
        ItemStack secondary = recipe.getSecondary();
        if (!secondary.isEmpty() && !fits(SLOT_SECONDARY, secondary)) {
            return false;
        }
        ItemStack out1 = inventory.get(SLOT_PRIMARY_1);
        ItemStack out2 = inventory.get(SLOT_PRIMARY_2);
        if (out1.isEmpty() || out2.isEmpty()) {
            return true;
        }
        boolean same1 = ItemStack.isSameItemSameComponents(out1, primary);
        boolean same2 = ItemStack.isSameItemSameComponents(out2, primary);
        if (!same1 && !same2) {
            return false;
        }
        if (!same1) {
            return out2.getCount() + primary.getCount() <= primary.getMaxStackSize();
        }
        if (!same2) {
            return out1.getCount() + primary.getCount() <= primary.getMaxStackSize();
        }
        return out1.getCount() + out2.getCount() + primary.getCount() <= primary.getMaxStackSize() * 2;
    }

    @Override
    protected void processStart() {
        ItemStack first = inventory.get(SLOT_INPUT_1);
        ItemStack second = inventory.get(SLOT_INPUT_2);
        processInput = first.copy();
        processInput2 = second.copy();
        TeRecipe recipe = TeRecipes.findSmelter(level, first, second);
        processEnd = recipe.getEnergy();
        int primaryCount = recipe.getInputs().get(0).count();
        int secondaryCount = recipe.getInputs().get(1).count();
        if (recipe.getInputs().get(0).ingredient().test(first)) {
            first.shrink(primaryCount);
            second.shrink(secondaryCount);
        } else {
            second.shrink(primaryCount);
            first.shrink(secondaryCount);
        }
        inventory.set(SLOT_INPUT_1, first.isEmpty() ? ItemStack.EMPTY : first);
        inventory.set(SLOT_INPUT_2, second.isEmpty() ? ItemStack.EMPTY : second);
    }

    @Override
    protected void processFinish() {
        TeRecipe recipe = TeRecipes.findSmelter(level, processInput, processInput2);
        if (recipe == null) {
            return;
        }
        ItemStack primary = recipe.getPrimary();
        ItemStack out1 = inventory.get(SLOT_PRIMARY_1);
        if (out1.isEmpty()) {
            inventory.set(SLOT_PRIMARY_1, primary);
        } else if (ItemStack.isSameItemSameComponents(out1, primary)) {
            int result = out1.getCount() + primary.getCount();
            if (result <= primary.getMaxStackSize()) {
                out1.grow(primary.getCount());
                inventory.set(SLOT_PRIMARY_1, out1);
            } else {
                int stackDiff = primary.getMaxStackSize() - out1.getCount();
                out1.grow(stackDiff);
                inventory.set(SLOT_PRIMARY_1, out1);
                addToSlot(SLOT_PRIMARY_2, primary.copyWithCount(primary.getCount() - stackDiff));
            }
        } else {
            addToSlot(SLOT_PRIMARY_2, primary);
        }
        ItemStack secondary = recipe.getSecondary();
        if (!secondary.isEmpty() && level.random.nextInt(100) < recipe.getChance()) {
            addToSlot(SLOT_SECONDARY, secondary);
        }
    }

    @Override
    protected void transferProducts() {
        for (int i = outputTrackerPrimary + 1; i <= outputTrackerPrimary + 6; i++) {
            int side = i % 6;
            if (sides[side] == 2 || sides[side] == 4) {
                Direction dir = Direction.from3DDataValue(side);
                if (ejectItem(SLOT_PRIMARY_1, 4, dir)) {
                    if (!ejectItem(SLOT_PRIMARY_2, 4, dir)) {
                        ejectItem(SLOT_PRIMARY_1, 4, dir);
                    }
                    outputTrackerPrimary = side;
                    break;
                }
                if (ejectItem(SLOT_PRIMARY_2, 4, dir)) {
                    if (!ejectItem(SLOT_PRIMARY_1, 4, dir)) {
                        ejectItem(SLOT_PRIMARY_2, 4, dir);
                    }
                    outputTrackerPrimary = side;
                    break;
                }
            }
        }
        for (int i = outputTrackerSecondary + 1; i <= outputTrackerSecondary + 6; i++) {
            int side = i % 6;
            if ((sides[side] == 3 || sides[side] == 4)
                    && ejectItem(SLOT_SECONDARY, 4, Direction.from3DDataValue(side))) {
                outputTrackerSecondary = side;
                break;
            }
        }
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new TeMachineMenu(id, playerInventory, this);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        outputTrackerPrimary = tag.getInt("output.primary");
        outputTrackerSecondary = tag.getInt("output.secondary");
        processInput2 = ItemStack.parseOptional(registries, tag.getCompound("process.inv2"));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("output.primary", outputTrackerPrimary);
        tag.putInt("output.secondary", outputTrackerSecondary);
        if (!processInput2.isEmpty()) {
            tag.put("process.inv2", processInput2.save(registries));
        }
    }
}
