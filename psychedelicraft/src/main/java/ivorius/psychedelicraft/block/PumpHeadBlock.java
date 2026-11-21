package ivorius.psychedelicraft.block;

import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.client.render.blocks.VoxelShapeUtil;
import ivorius.psychedelicraft.item.PSItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class PumpHeadBlock extends DirectionalBlock {
    public static final MapCodec<PumpHeadBlock> CODEC = simpleCodec(PumpHeadBlock::new);

    private static final VoxelShape UP_SHAPE = Shapes.or(
            box(3, 0, 3, 13, 13, 13),
            box(2, 12, 2, 14, 14, 14)
    );
    private static final VoxelShape NORTH_SHAPE = Shapes.or(
            box(3, 3, 3, 13, 13, 16),
            box(2, 2, 2, 14, 14, 4)
    );
    private static final VoxelShape SOUTH_SHAPE = VoxelShapeUtil.rotate(NORTH_SHAPE, Direction.NORTH);

    public PumpHeadBlock(Properties settings) {
        super(settings);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            default -> UP_SHAPE;
        };
    }

    @Override
    protected MapCodec<? extends PumpHeadBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader world, BlockPos pos, BlockState state) {
        return PSItems.PUMP.getDefaultInstance();
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected boolean useShapeForLightOcclusion(BlockState state) {
        return true;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    @Override
    public BlockState playerWillDestroy(Level world, BlockPos pos, BlockState state, Player player) {
        if (!world.isClientSide && player.getAbilities().instabuild) {
            checkSupport(state, world, pos);
        }

        return super.playerWillDestroy(world, pos, state, player);
    }

    @Override
    protected void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean moved) {
        if (!state.is(newState.getBlock())) {
            super.onRemove(state, world, pos, newState, moved);
            checkSupport(state, world, pos);
        }
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
        return direction.getOpposite() == state.getValue(FACING) && !state.canSurvive(world, pos)
            ? Blocks.AIR.defaultBlockState()
            : super.updateShape(state, direction, neighborState, world, pos, neighborPos);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        return isAttached(state, world.getBlockState(pos.relative(state.getValue(FACING).getOpposite())));
    }

    private void checkSupport(BlockState state, Level world, BlockPos pos) {
        BlockPos bodyPos = pos.relative(state.getValue(FACING).getOpposite());
        if (isAttached(state, world.getBlockState(bodyPos))) {
            world.destroyBlock(bodyPos, true);
        }
    }

    private boolean isAttached(BlockState headState, BlockState pistonState) {
        return pistonState.is(PSBlocks.PUMP) && pistonState.getValue(PumpBlock.POWERED) && getHeadFacing(pistonState.getValue(FACING)) == headState.getValue(FACING);
    }

    static Direction getHeadFacing(Direction facing) {
        return switch (facing) {
            case UP -> Direction.SOUTH;
            case DOWN -> Direction.NORTH;
            default -> Direction.UP;
        };
    }
}
