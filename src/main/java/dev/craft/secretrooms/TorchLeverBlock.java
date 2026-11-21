package dev.craft.secretrooms;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Torch lever ({@code BlockTorchLever}, spec 50 §4): looks and lights like a torch (14), but a right click switches it
 * like a lever – weak signal 15 to every side, strong into the block it hangs on. {@link #FACING} UP = standing.
 */
public class TorchLeverBlock extends Block {
    public static final MapCodec<TorchLeverBlock> CODEC = simpleCodec(TorchLeverBlock::new);
    public static final DirectionProperty FACING = DirectionProperty.create("facing",
            direction -> direction != Direction.DOWN);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public TorchLeverBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP).setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
    }

    private static Direction support(BlockState state) {
        return state.getValue(FACING).getOpposite();
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        return facing == Direction.UP ? Blocks.TORCH.defaultBlockState().getShape(level, pos)
                : Blocks.WALL_TORCH.defaultBlockState().setValue(BlockStateProperties.HORIZONTAL_FACING, facing)
                .getShape(level, pos);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction support = support(state);
        BlockPos on = pos.relative(support);
        return level.getBlockState(on).isFaceSturdy(level, on, support.getOpposite());
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        for (Direction direction : context.getNearestLookingDirections()) {
            if (direction != Direction.UP) {
                BlockState state = defaultBlockState().setValue(FACING, direction.getOpposite());
                if (state.canSurvive(context.getLevel(), context.getClickedPos())) {
                    return state;
                }
            }
        }
        return null;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbor, LevelAccessor level,
            BlockPos pos, BlockPos neighborPos) {
        return direction == support(state) && !state.canSurvive(level, pos) ? Blocks.AIR.defaultBlockState() : state;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (!level.isClientSide()) {
            boolean on = !state.getValue(POWERED);
            level.setBlock(pos, state.setValue(POWERED, on), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.LEVER_CLICK, SoundSource.BLOCKS, 0.3F, on ? 0.6F : 0.5F);
            level.updateNeighborsAt(pos, this);
            level.updateNeighborsAt(pos.relative(support(state)), this);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) && support(state) == direction.getOpposite() ? 15 : 0;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && state.getValue(POWERED)) {
            level.updateNeighborsAt(pos, this);
            level.updateNeighborsAt(pos.relative(support(state)), this);
        }
        super.onRemove(state, level, pos, newState, moving);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Direction facing = state.getValue(FACING);
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.7;
        double z = pos.getZ() + 0.5;
        if (facing != Direction.UP) {
            Direction back = facing.getOpposite();
            x += 0.27 * back.getStepX();
            y += 0.22;
            z += 0.27 * back.getStepZ();
        }
        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0, 0, 0);
        level.addParticle(ParticleTypes.FLAME, x, y, z, 0, 0, 0);
    }
}
