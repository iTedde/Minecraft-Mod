package ivorius.psychedelicraft.block;

import ivorius.psychedelicraft.block.entity.FluidProcessingBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.state.BlockState;

abstract class FluidMachineBlock<T extends FluidProcessingBlockEntity> extends BlockWithFluid<T> {

    protected FluidMachineBlock(Properties settings) {
        super(settings);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter world, BlockPos pos, Direction direction) {
        // signal 0-15 to indicate progress
        return direction.getAxis() == Axis.Y ? 0 : toRedstoneSignal(getProgress(world, pos), 15);
    }

    protected float getProgress(BlockGetter world, BlockPos pos) {
        return world.getBlockEntity(pos, getBlockEntityType())
                .map(e -> e.getProgress(1))
                .orElse(0F);
    }
}
