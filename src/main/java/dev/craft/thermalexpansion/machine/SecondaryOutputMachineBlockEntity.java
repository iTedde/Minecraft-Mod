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
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * {@code TilePulverizer} and {@code TileSawmill}: slot 0 input, slots 1–2 primary output, slot 3 secondary output with
 * a chance in percent. Side groups: 2 primary, 3 secondary, 4 both (ejected), 5 all outputs open to pipes.
 */
public class SecondaryOutputMachineBlockEntity extends TeMachineBlockEntity {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_PRIMARY_1 = 1;
    public static final int SLOT_PRIMARY_2 = 2;
    public static final int SLOT_SECONDARY = 3;

    private final TeRecipe.Kind kind;
    private int outputTrackerPrimary;
    private int outputTrackerSecondary;

    public SecondaryOutputMachineBlockEntity(BlockEntityType<?> blockEntityType, TeMachineType type,
                                             TeRecipe.Kind kind, BlockPos pos, BlockState state) {
        super(blockEntityType, type, pos, state);
        this.kind = kind;
    }

    public static SecondaryOutputMachineBlockEntity pulverizer(BlockPos pos, BlockState state) {
        return new SecondaryOutputMachineBlockEntity(TeBlockEntityTypes.PULVERIZER.get(), TeMachineType.PULVERIZER,
                TeRecipe.Kind.PULVERIZER, pos, state);
    }

    public static SecondaryOutputMachineBlockEntity sawmill(BlockPos pos, BlockState state) {
        return new SecondaryOutputMachineBlockEntity(TeBlockEntityTypes.SAWMILL.get(), TeMachineType.SAWMILL,
                TeRecipe.Kind.SAWMILL, pos, state);
    }

    @Nullable
    private TeRecipe recipe(ItemStack stack) {
        return TeRecipes.find(level, kind, stack);
    }

    @Override
    protected boolean canStart() {
        TeRecipe recipe = recipe(inventory.get(SLOT_INPUT));
        if (recipe == null || provider.getEnergyStored() < recipe.getEnergy()) {
            return false;
        }
        return outputsFit(recipe);
    }

    @Override
    protected boolean canFinish() {
        if (processCur < processEnd) {
            return false;
        }
        TeRecipe recipe = recipe(processInput);
        if (recipe == null) {
            processCur = 0.0F;
            active = false;
            return false;
        }
        return outputsFit(recipe);
    }

    /** The secondary must fit slot 3; the primary may be split over slots 1 and 2. */
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
        ItemStack input = inventory.get(SLOT_INPUT);
        processInput = input.copy();
        TeRecipe recipe = recipe(input);
        processEnd = recipe.getEnergy();
        input.shrink(recipe.getInputs().get(0).count());
        inventory.set(SLOT_INPUT, input.isEmpty() ? ItemStack.EMPTY : input);
    }

    @Override
    protected void processFinish() {
        TeRecipe recipe = recipe(processInput);
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
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("output.primary", outputTrackerPrimary);
        tag.putInt("output.secondary", outputTrackerSecondary);
    }
}
