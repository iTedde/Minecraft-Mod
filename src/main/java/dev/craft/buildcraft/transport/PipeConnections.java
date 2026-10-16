package dev.craft.buildcraft.transport;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

/** Connection rules of the pipes ({@code Utils.checkPipesConnections}) and the iron pipe's output switching. */
public final class PipeConnections {
    private PipeConnections() {
    }

    /** Whether the pipe at {@code pos} and its neighbour on {@code side} are connected (both agree). */
    public static boolean canConnect(Level level, BlockPos pos, Direction side) {
        if (!(level.getBlockEntity(pos) instanceof PipeBlockEntity pipe)) {
            return false;
        }
        BlockEntity other = level.getBlockEntity(pos.relative(side));
        if (other == null) {
            return false;
        }
        if (other instanceof PipeBlockEntity otherPipe) {
            return pipe.acceptsPipe(otherPipe) && otherPipe.acceptsPipe(pipe);
        }
        return pipe.getPipeType().getMaterial().connectsToNonPipes() && pipe.acceptsNonPipe(other, side);
    }

    /** Item inventory on the neighbour's side facing the pipe (only block entities, like the original). */
    @Nullable
    public static IItemHandler itemHandler(Level level, BlockPos pos, Direction side) {
        BlockPos neighbour = pos.relative(side);
        if (level.getBlockEntity(neighbour) == null) {
            return null;
        }
        return level.getCapability(Capabilities.ItemHandler.BLOCK, neighbour, side.getOpposite());
    }

    @Nullable
    public static IFluidHandler fluidHandler(Level level, BlockPos pos, Direction side) {
        BlockPos neighbour = pos.relative(side);
        if (level.getBlockEntity(neighbour) == null) {
            return null;
        }
        return level.getCapability(Capabilities.FluidHandler.BLOCK, neighbour, side.getOpposite());
    }

    /**
     * {@code PipeLogicIron.switchPosition}: the next side (from the current one + 1) with a pipe, an inventory or a
     * tank, skipping wood/emerald and structure pipes. Whether the side is really connected is not checked.
     */
    public static void switchIronOutput(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.hasProperty(GenericPipeBlock.OUTPUT)) {
            return;
        }
        int next = state.getValue(GenericPipeBlock.OUTPUT);
        for (int i = 0; i < 6; i++) {
            if (++next > 5) {
                next = 0;
            }
            Direction side = Direction.from3DDataValue(next);
            BlockEntity tile = level.getBlockEntity(pos.relative(side));
            if (tile instanceof PipeBlockEntity pipe) {
                PipeType type = pipe.getPipeType();
                if (type.getMaterial().isWood() || type.getTransport() == PipeType.Transport.STRUCTURE) {
                    continue;
                }
            }
            if (tile instanceof PipeBlockEntity || itemHandler(level, pos, side) != null
                    || fluidHandler(level, pos, side) != null) {
                level.setBlock(pos, state.setValue(GenericPipeBlock.OUTPUT, next), Block.UPDATE_ALL);
                return;
            }
        }
    }
}
