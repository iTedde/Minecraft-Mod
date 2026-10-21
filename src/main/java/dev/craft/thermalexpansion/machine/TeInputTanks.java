package dev.craft.thermalexpansion.machine;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;

/**
 * The tanks of {@code TileIceGen}, {@code TileRockGen} and {@code TileWaterGen} seen from one side: a fill goes to the
 * tank that takes the liquid, nothing can be drained ({@code drain} returns {@code null} in the original). The
 * accumulator only shows its tank ({@code fill} returns 0).
 */
final class TeInputTanks implements IFluidHandler {
    private final boolean fillable;
    private final FluidTank[] tanks;

    TeInputTanks(boolean fillable, FluidTank... tanks) {
        this.fillable = fillable;
        this.tanks = tanks;
    }

    @Override
    public int getTanks() {
        return tanks.length;
    }

    @Override
    public FluidStack getFluidInTank(int index) {
        return tanks[index].getFluid();
    }

    @Override
    public int getTankCapacity(int index) {
        return tanks[index].getCapacity();
    }

    @Override
    public boolean isFluidValid(int index, FluidStack stack) {
        return fillable && tanks[index].isFluidValid(stack);
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        if (!fillable) {
            return 0;
        }
        for (FluidTank tank : tanks) {
            if (tank.isFluidValid(resource)) {
                return tank.fill(resource, action);
            }
        }
        return 0;
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
