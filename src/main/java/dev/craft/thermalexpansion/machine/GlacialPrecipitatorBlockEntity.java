package dev.craft.thermalexpansion.machine;

import java.util.List;

import dev.craft.thermalexpansion.TeBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

/**
 * {@code TileIceGen}: freezes water with MJ into 4 snowballs (500 mB, 80 MJ), a snow block (500 mB, 80 MJ) or ice
 * (1000 mB, 160 MJ).
 */
public class GlacialPrecipitatorBlockEntity extends SelectionMachineBlockEntity {
    static final List<ItemStack> PRODUCTS = List.of(new ItemStack(Items.SNOWBALL, 4), new ItemStack(Items.SNOW_BLOCK),
            new ItemStack(Items.ICE));
    private static final int[] PROCESS_WATER = {500, 500, 1000};
    private static final int[] PROCESS_ENERGY = {80, 80, 160};

    private final FluidTank tank = tank(Fluids.WATER, this::setChanged);

    public GlacialPrecipitatorBlockEntity(BlockPos pos, BlockState state) {
        super(TeBlockEntityTypes.GLACIAL_PRECIPITATOR.get(), TeMachineType.GLACIAL_PRECIPITATOR, pos, state);
    }

    /** Water in mB used for product {@code selection}. */
    public static int processWater(int selection) {
        return PROCESS_WATER[selection];
    }

    /** Energy in MJ used for product {@code selection}. */
    public static int processEnergy(int selection) {
        return PROCESS_ENERGY[selection];
    }

    /**
     * Room for the product: slot limit 64 and – an original quirk – the stack size of the product of the
     * <em>previous</em> selection.
     */
    private boolean fits(ItemStack product, ItemStack limit) {
        ItemStack output = inventory.get(0);
        int result = output.getCount() + product.getCount();
        return result <= 64 && result <= limit.getMaxStackSize();
    }

    @Override
    protected boolean canStart() {
        if (provider.getEnergyStored() < PROCESS_ENERGY[curSelection]
                || tank.getFluidAmount() < PROCESS_WATER[curSelection]) {
            return false;
        }
        ItemStack output = inventory.get(0);
        if (output.isEmpty()) {
            return true;
        }
        ItemStack product = product(curSelection);
        return output.is(product.getItem()) && fits(product, product(prevSelection));
    }

    @Override
    protected boolean canFinish() {
        if (processCur < processEnd) {
            return false;
        }
        ItemStack output = inventory.get(0);
        if (output.isEmpty()) {
            return true;
        }
        ItemStack product = product(prevSelection);
        return output.is(product.getItem()) && fits(product, product);
    }

    @Override
    protected void processStart() {
        processEnd = PROCESS_ENERGY[curSelection];
        tank.drain(PROCESS_WATER[curSelection], IFluidHandler.FluidAction.EXECUTE);
        prevSelection = curSelection;
    }

    @Nullable
    @Override
    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        return side == null || getSide(side) == GROUP_INPUT ? new TeInputTanks(true, tank) : null;
    }

    @Override
    public FluidStack getDisplayFluid(int index) {
        return index == 0 ? tank.getFluid() : FluidStack.EMPTY;
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        tank.setFluid(new FluidStack(Fluids.WATER, Math.min(tag.getInt("qty.water"), CAPACITY)));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("qty.water", tank.getFluidAmount());
    }
}
