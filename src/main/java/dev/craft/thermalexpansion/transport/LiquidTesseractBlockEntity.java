package dev.craft.thermalexpansion.transport;

import java.util.List;

import javax.annotation.Nullable;

import dev.craft.thermalexpansion.TeBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

/**
 * {@code TileTeleportLiquid}: no tank. Filling a sender (frequency set, redstone condition) passes the liquid at once
 * to the tanks next to its partners, in turn; the counters move on when simulating too. Nothing can be drained.
 */
public class LiquidTesseractBlockEntity extends TesseractBlockEntity {
    private final IFluidHandler fluidHandler = new IFluidHandler() {
        @Override
        public int getTanks() {
            return 1;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return FluidStack.EMPTY;
        }

        @Override
        public int getTankCapacity(int tank) {
            return 0;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return true;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (getFrequency() == -1 || !canSend() || level.isClientSide() || !redstoneControlOrDisable()) {
                return 0;
            }
            return sendLiquid(resource.copy(), action);
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

    public LiquidTesseractBlockEntity(BlockPos pos, BlockState state) {
        super(TeBlockEntityTypes.LIQUID_TESSERACT.get(), TesseractType.LIQUID, pos, state);
    }

    @Nullable
    public IFluidHandler getFluidHandler(@Nullable Direction side) {
        return fluidHandler;
    }

    private int sendLiquid(FluidStack stack, IFluidHandler.FluidAction action) {
        List<TesseractBlockEntity> outputs = getValidOutputs();
        int startAmount = stack.getAmount();
        if (outputs.isEmpty() || startAmount <= 0) {
            return 0;
        }
        for (int i = outputTrackerRemote; i < outputs.size() && !stack.isEmpty(); i++) {
            ((LiquidTesseractBlockEntity) outputs.get(i)).receiveLiquid(stack, action);
        }
        for (int i = 0; i < outputs.size() && i < outputTrackerRemote && !stack.isEmpty(); i++) {
            ((LiquidTesseractBlockEntity) outputs.get(i)).receiveLiquid(stack, action);
        }
        incrementOutputTrackerRemote(outputs.size() - 1);
        setChanged();
        return startAmount - stack.getAmount();
    }

    /** {@code receiveLiquid}: into the adjacent tanks in turn; {@code stack} shrinks by what they took. */
    private void receiveLiquid(FluidStack stack, IFluidHandler.FluidAction action) {
        for (int side = outputTrackerAdjacent; side < 6 && !stack.isEmpty(); side++) {
            stack.shrink(addToAdjacentTank(Direction.from3DDataValue(side), stack.copy(), action));
        }
        for (int side = 0; side < outputTrackerAdjacent && side < 6 && !stack.isEmpty(); side++) {
            stack.shrink(addToAdjacentTank(Direction.from3DDataValue(side), stack.copy(), action));
        }
        incrementOutputTrackerAdjacent();
        setChanged();
    }

    private int addToAdjacentTank(Direction side, FluidStack stack, IFluidHandler.FluidAction action) {
        BlockPos neighbour = worldPosition.relative(side);
        if (level.getBlockEntity(neighbour) instanceof LiquidTesseractBlockEntity) {
            return 0;
        }
        IFluidHandler tank = level.getCapability(Capabilities.FluidHandler.BLOCK, neighbour, side.getOpposite());
        return tank != null ? tank.fill(stack, action) : 0;
    }

    /** {@code Utils.isAdjacentTank}. */
    @Override
    protected boolean hasAdjacentTarget(Direction side) {
        return level.getCapability(Capabilities.FluidHandler.BLOCK, worldPosition.relative(side),
                side.getOpposite()) != null;
    }
}
