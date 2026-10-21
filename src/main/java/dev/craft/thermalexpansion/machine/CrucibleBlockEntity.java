package dev.craft.thermalexpansion.machine;

import dev.craft.thermalexpansion.TeBlockEntityTypes;
import dev.craft.thermalexpansion.recipe.TeFluidRecipe;
import dev.craft.thermalexpansion.recipe.TeRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

/**
 * {@code TileCrucible}: melts slot 0 into the tank (e.g. stone into lava); side group 1 input, 2 liquid output (drain
 * and push, 100 mB per tick).
 */
public class CrucibleBlockEntity extends TankMachineBlockEntity {
    private static final int GROUP_OUTPUT = 2;

    public CrucibleBlockEntity(BlockPos pos, BlockState state) {
        super(TeBlockEntityTypes.MAGMA_CRUCIBLE.get(), TeMachineType.MAGMA_CRUCIBLE, pos, state);
    }

    /** {@code CrucibleManager.getRecipe}: by item only – fewer snowballs than the recipe asks for still melt. */
    @Nullable
    private TeFluidRecipe recipe(ItemStack stack) {
        return TeRecipes.find(level, TeFluidRecipe.Kind.CRUCIBLE, stack);
    }

    private boolean fits(FluidStack output) {
        return tank.fill(output, IFluidHandler.FluidAction.SIMULATE) == output.getAmount();
    }

    @Override
    protected boolean canStart() {
        TeFluidRecipe recipe = recipe(inventory.get(0));
        return recipe != null && provider.getEnergyStored() >= recipe.getEnergy() && fits(recipe.getFluid());
    }

    @Override
    protected boolean canFinish() {
        if (processCur < processEnd) {
            return false;
        }
        TeFluidRecipe recipe = TeRecipes.find(level, TeFluidRecipe.Kind.CRUCIBLE, processInput);
        if (recipe == null) {
            processCur = 0.0F;
            active = false;
            return false;
        }
        return fits(recipe.getFluid());
    }

    @Override
    protected void processStart() {
        ItemStack input = inventory.get(0);
        processInput = input.copy();
        TeFluidRecipe recipe = recipe(input);
        processEnd = recipe.getEnergy();
        input.shrink(recipe.getInput().count());
        inventory.set(0, input.isEmpty() ? ItemStack.EMPTY : input);
    }

    @Override
    protected void processFinish() {
        TeFluidRecipe recipe = TeRecipes.find(level, TeFluidRecipe.Kind.CRUCIBLE, processInput);
        processInput = ItemStack.EMPTY;
        if (recipe != null) {
            tank.fill(recipe.getFluid(), IFluidHandler.FluidAction.EXECUTE);
        }
    }

    @Override
    protected void transferProducts() {
    }

    @Override
    public void serverTick() {
        transferLiquid(GROUP_OUTPUT);
        super.serverTick();
    }

    /** Never filled from outside; drained on liquid output sides. */
    @Nullable
    @Override
    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        return side == null || getSide(side) == GROUP_OUTPUT ? drainOnly() : null;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new TeMachineMenu(id, playerInventory, this);
    }
}
