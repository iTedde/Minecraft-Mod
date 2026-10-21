package dev.craft.thermalexpansion.machine;

import dev.craft.thermalexpansion.TeBlockEntityTypes;
import dev.craft.thermalexpansion.recipe.TeFluidRecipe;
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
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

/**
 * {@code TileTransposer}: fill mode puts tank liquid into the item in slot 0, extraction mode ({@code reverse}) empties
 * it into the tank; the result goes to slot 1. The mode button sets {@code reverseFlag}, which takes effect between two
 * operations. Side groups: 1 input (and liquid input while filling), 2 output, 3 liquid output (extraction), 4 open.
 */
public class TransposerBlockEntity extends TankMachineBlockEntity {
    public static final int SLOT_INPUT = 0;
    public static final int SLOT_OUTPUT = 1;
    private static final int GROUP_INPUT = 1;
    private static final int GROUP_OUTPUT = 2;
    private static final int GROUP_LIQUID_OUTPUT = 3;

    private boolean reverse;
    private boolean reverseFlag;
    private int outputTracker;

    public TransposerBlockEntity(BlockPos pos, BlockState state) {
        super(TeBlockEntityTypes.LIQUID_TRANSPOSER.get(), TeMachineType.LIQUID_TRANSPOSER, pos, state);
    }

    public boolean isReverse() {
        return reverse;
    }

    public boolean getReverseFlag() {
        return reverseFlag;
    }

    /** The mode packet: applies at once when no operation is running. */
    public void setMode(boolean mode) {
        reverseFlag = mode;
        if (processCur == 0.0F) {
            reverse = reverseFlag;
        }
        sync();
    }

    @Nullable
    private TeFluidRecipe fillRecipe(ItemStack stack) {
        return TeRecipes.findFill(level, stack, tank.getFluid());
    }

    @Nullable
    private TeFluidRecipe extractionRecipe(ItemStack stack) {
        return TeRecipes.find(level, TeFluidRecipe.Kind.EXTRACT, stack);
    }

    /** Output fits slot 1 (stack limit of the item and of the slot, 64). */
    private boolean outputFits(ItemStack output) {
        if (output.isEmpty()) {
            return true;
        }
        ItemStack inSlot = inventory.get(SLOT_OUTPUT);
        if (inSlot.isEmpty()) {
            return true;
        }
        if (!ItemStack.isSameItemSameComponents(inSlot, output)) {
            return false;
        }
        int result = inSlot.getCount() + output.getCount();
        return result <= 64 && result <= output.getMaxStackSize();
    }

    @Override
    protected boolean canStart() {
        ItemStack input = inventory.get(SLOT_INPUT);
        if (input.isEmpty()) {
            return false;
        }
        if (!reverse) {
            TeFluidRecipe recipe = fillRecipe(input);
            if (recipe == null || provider.getEnergyStored() < recipe.getEnergy()
                    || tank.getFluidAmount() < recipe.getFluid().getAmount()) {
                return false;
            }
            return outputFits(recipe.getOutput());
        }
        TeFluidRecipe recipe = extractionRecipe(input);
        if (recipe == null || provider.getEnergyStored() < recipe.getEnergy()) {
            return false;
        }
        FluidStack liquid = recipe.getFluid();
        if (tank.fill(liquid, IFluidHandler.FluidAction.SIMULATE) != liquid.getAmount()) {
            return false;
        }
        return outputFits(recipe.getOutput());
    }

    @Override
    protected boolean canFinish() {
        if (processCur < processEnd) {
            return false;
        }
        TeFluidRecipe recipe = reverse ? extractionRecipe(processInput) : fillRecipe(processInput);
        if (recipe == null) {
            processCur = 0.0F;
            active = false;
            return false;
        }
        return outputFits(recipe.getOutput());
    }

    @Override
    protected void processStart() {
        ItemStack input = inventory.get(SLOT_INPUT);
        processInput = input.copy();
        TeFluidRecipe recipe = reverse ? extractionRecipe(input) : fillRecipe(input);
        processEnd = recipe.getEnergy();
        input.shrink(1);
        inventory.set(SLOT_INPUT, input.isEmpty() ? ItemStack.EMPTY : input);
    }

    @Override
    protected void processFinish() {
        if (!reverse) {
            TeFluidRecipe recipe = fillRecipe(processInput);
            if (recipe != null) {
                addToSlot(SLOT_OUTPUT, recipe.getOutput());
                tank.drain(recipe.getFluid().getAmount(), IFluidHandler.FluidAction.EXECUTE);
            }
        } else {
            TeFluidRecipe recipe = extractionRecipe(processInput);
            if (recipe != null) {
                ItemStack output = recipe.getOutput();
                if (!output.isEmpty() && level.random.nextInt(100) < recipe.getChance()) {
                    addToSlot(SLOT_OUTPUT, output);
                }
                tank.fill(recipe.getFluid(), IFluidHandler.FluidAction.EXECUTE);
            }
        }
        reverse = reverseFlag;
        sync();
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
    public void serverTick() {
        if (reverse) {
            transferLiquid(GROUP_LIQUID_OUTPUT);
        }
        super.serverTick();
    }

    /** Filling: the tank takes liquid on input sides; extraction: it gives liquid on liquid output sides. */
    @Nullable
    @Override
    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        if (reverse) {
            return side == null || getSide(side) == GROUP_LIQUID_OUTPUT ? drainOnly() : null;
        }
        return side == null || getSide(side) == GROUP_INPUT ? fillOnly() : null;
    }

    private IFluidHandler fillOnly() {
        return new IFluidHandler() {
            @Override
            public int getTanks() {
                return 1;
            }

            @Override
            public FluidStack getFluidInTank(int index) {
                return tank.getFluid();
            }

            @Override
            public int getTankCapacity(int index) {
                return MAX_LIQUID;
            }

            @Override
            public boolean isFluidValid(int index, FluidStack stack) {
                return true;
            }

            @Override
            public int fill(FluidStack resource, FluidAction action) {
                return reverse ? 0 : tank.fill(resource, action);
            }

            @Override
            public FluidStack drain(FluidStack resource, FluidAction action) {
                return FluidStack.EMPTY;
            }

            @Override
            public FluidStack drain(int maxDrain, FluidAction action) {
                return FluidStack.EMPTY;
            }
        };
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new TeMachineMenu(id, playerInventory, this);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        outputTracker = tag.getInt("output");
        reverse = tag.getBoolean("process.mode");
        reverseFlag = reverse;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("output", outputTracker);
        tag.putBoolean("process.mode", reverse);
    }
}
