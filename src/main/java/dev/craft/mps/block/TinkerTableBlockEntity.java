package dev.craft.mps.block;

import dev.craft.mps.MpsBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** {@code TileEntityTinkerTable}: no data, only there for the animated model. */
public class TinkerTableBlockEntity extends BlockEntity {
    public TinkerTableBlockEntity(BlockPos pos, BlockState state) {
        super(MpsBlocks.TINKER_TABLE_ENTITY.get(), pos, state);
    }
}
