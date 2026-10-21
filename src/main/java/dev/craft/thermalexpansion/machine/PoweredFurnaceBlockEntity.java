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
import org.jetbrains.annotations.Nullable;

/** {@code TileFurnace}: slot 0 input, slot 1 output; own recipes or any furnace recipe at 160 MJ. */
public class PoweredFurnaceBlockEntity extends TeMachineBlockEntity {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    /** Output side group. */
    private static final int GROUP_OUTPUT = 2;

    private int outputTracker;

    public PoweredFurnaceBlockEntity(BlockPos pos, BlockState state) {
        super(TeBlockEntityTypes.POWERED_FURNACE.get(), TeMachineType.POWERED_FURNACE, pos, state);
    }

    @Nullable
    private TeRecipe recipe(ItemStack stack) {
        TeRecipe recipe = TeRecipes.find(level, TeRecipe.Kind.FURNACE, stack);
        // FurnaceManager.getOutput: an own recipe needs its input amount.
        return recipe != null && stack.getCount() >= recipe.getInputs().get(0).count() ? recipe : null;
    }

    @Override
    protected boolean canStart() {
        TeRecipe recipe = recipe(inventory.get(SLOT_INPUT));
        if (recipe == null || provider.getEnergyStored() < recipe.getEnergy()) {
            return false;
        }
        return fits(SLOT_OUTPUT, recipe.getPrimary());
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
        return fits(SLOT_OUTPUT, recipe.getPrimary());
    }

    @Override
    protected void processStart() {
        ItemStack input = inventory.get(SLOT_INPUT);
        processInput = input.copy();
        TeRecipe recipe = recipe(input);
        processEnd = recipe == null ? TeRecipes.FURNACE_DEFAULT_ENERGY : recipe.getEnergy();
        // TileFurnace.processStart always takes a single item.
        input.shrink(1);
        inventory.set(SLOT_INPUT, input.isEmpty() ? ItemStack.EMPTY : input);
    }

    @Override
    protected void processFinish() {
        TeRecipe recipe = recipe(processInput);
        if (recipe != null) {
            addToSlot(SLOT_OUTPUT, recipe.getPrimary());
        }
    }

    @Override
    protected void transferProducts() {
        for (int i = outputTracker + 1; i <= outputTracker + 6; i++) {
            int side = i % 6;
            if (sides[side] == GROUP_OUTPUT && ejectItem(SLOT_OUTPUT, 4, Direction.from3DDataValue(side))) {
                outputTracker = side;
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
        outputTracker = tag.getInt("output");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("output", outputTracker);
    }
}
