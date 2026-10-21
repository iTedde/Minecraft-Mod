package dev.craft.thermalexpansion.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import org.jetbrains.annotations.Nullable;

/**
 * TE machines with a 10 000 mB tank ({@code TileCrucible}, {@code TileTransposer}): the liquid is pushed into adjacent
 * tanks on the liquid output sides, at most 100 mB per tick and side (reihum).
 */
public abstract class TankMachineBlockEntity extends TeMachineBlockEntity {
    public static final int MAX_LIQUID = 10000;
    /** {@code RATE}. */
    private static final int RATE = 100;

    protected final FluidTank tank = new FluidTank(MAX_LIQUID) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };
    private int outputTrackerLiquid;

    protected TankMachineBlockEntity(BlockEntityType<?> blockEntityType, TeMachineType type, BlockPos pos,
                                     BlockState state) {
        super(blockEntityType, type, pos, state);
    }

    public FluidStack getTankFluid() {
        return tank.getFluid();
    }

    /** The fluid capability on {@code side}, or {@code null}. */
    @Nullable
    public abstract IFluidHandler getFluidHandler(@Nullable Direction side);

    /** {@code transferLiquid}: up to 100 mB into the first adjacent tank on a side of {@code group}. */
    protected void transferLiquid(int group) {
        if (tank.isEmpty()) {
            return;
        }
        for (int i = outputTrackerLiquid + 1; i <= outputTrackerLiquid + 6; i++) {
            int side = i % 6;
            if (sides[side] != group) {
                continue;
            }
            Direction direction = Direction.from3DDataValue(side);
            IFluidHandler target = level.getCapability(Capabilities.FluidHandler.BLOCK,
                    worldPosition.relative(direction), direction.getOpposite());
            if (target == null) {
                continue;
            }
            int quantity = target.fill(tank.getFluid().copyWithAmount(Math.min(tank.getFluidAmount(), RATE)),
                    IFluidHandler.FluidAction.EXECUTE);
            if (quantity > 0) {
                tank.drain(quantity, IFluidHandler.FluidAction.EXECUTE);
                outputTrackerLiquid = side;
                break;
            }
        }
    }

    /** {@code getTanks}/{@code drain} of the original for one side: the tank, drain only. */
    protected IFluidHandler drainOnly() {
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
                return false;
            }

            @Override
            public int fill(FluidStack resource, FluidAction action) {
                return 0;
            }

            @Override
            public FluidStack drain(FluidStack resource, FluidAction action) {
                return tank.drain(resource, action);
            }

            @Override
            public FluidStack drain(int maxDrain, FluidAction action) {
                return tank.drain(maxDrain, action);
            }
        };
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        outputTrackerLiquid = tag.getInt("output.liquid");
        tank.setFluid(FluidStack.parseOptional(registries, tag.getCompound("tank")));
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("output.liquid", outputTrackerLiquid);
        if (!tank.isEmpty()) {
            tag.put("tank", tank.getFluid().save(registries));
        }
    }
}
