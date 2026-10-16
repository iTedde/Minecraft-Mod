package dev.craft.buildcraft.transport;

import dev.craft.buildcraft.BcBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** BuildCraft {@code PipeTransportStructure}: no transport, connects to structure pipes and every block entity. */
public class StructurePipeBlockEntity extends PipeBlockEntity {
    public StructurePipeBlockEntity(BlockPos pos, BlockState state) {
        super(BcBlockEntityTypes.STRUCTURE_PIPE.get(), pos, state);
    }

    @Override
    protected boolean acceptsNonPipe(BlockEntity other, Direction side) {
        return true;
    }

    @Override
    protected void tickTransport() {
    }

    @Override
    public void dropContents() {
    }
}
