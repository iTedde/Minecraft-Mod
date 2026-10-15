package dev.craft.ic2.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Rubber leaves (IC2 {@code BlockRubLeaves}): birch foliage colour, 1/35 sapling drop (loot table), shearable.
 * Decay uses the vanilla leaf distance (see {@code docs/abweichungen.md}).
 */
public class RubberLeavesBlock extends LeavesBlock {
    public static final MapCodec<RubberLeavesBlock> CODEC = simpleCodec(RubberLeavesBlock::new);

    public RubberLeavesBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<? extends LeavesBlock> codec() {
        return CODEC;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 20;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 30;
    }
}
