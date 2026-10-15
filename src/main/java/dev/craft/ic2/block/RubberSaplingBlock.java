package dev.craft.ic2.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Rubber sapling (IC2 {@code BlockRubSapling}): grows into a rubber tree with 1/30 per random tick at light level 9
 * or more above it; bone meal always grows it.
 */
public class RubberSaplingBlock extends BushBlock implements BonemealableBlock {
    public static final MapCodec<RubberSaplingBlock> CODEC = simpleCodec(RubberSaplingBlock::new);
    private static final VoxelShape SHAPE = box(2.0, 0.0, 2.0, 14.0, 12.0, 14.0);
    public static final int MIN_LIGHT = 9;
    public static final int GROW_CHANCE = 30;

    public RubberSaplingBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getMaxLocalRawBrightness(pos.above()) >= MIN_LIGHT && random.nextInt(GROW_CHANCE) == 0) {
            RubberTree.grow(level, pos, random);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        RubberTree.grow(level, pos, random);
    }
}
