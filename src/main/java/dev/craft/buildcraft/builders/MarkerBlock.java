package dev.craft.buildcraft.builders;

import com.mojang.serialization.MapCodec;
import dev.craft.buildcraft.BcBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Landmark, BuildCraft {@code BlockMarker}: a torch-like block. {@link #FACING} points away from the supporting
 * block. Like the original ({@code onBlockAdded}) the clicked side is ignored: the landmark always attaches to the
 * first full neighbour in the order −X, +X, −Z, +Z, below, above.
 */
public class MarkerBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    /** Directions the landmark points to, in the order of the original's support search. */
    private static final Direction[] PLACEMENT_ORDER = {Direction.EAST, Direction.WEST, Direction.SOUTH,
            Direction.NORTH, Direction.UP, Direction.DOWN};
    private static final double W = 0.15 * 16;
    private static final double H = 0.65 * 16;
    private static final VoxelShape[] SHAPES = {
            box(8 - W, 16 - H, 8 - W, 8 + W, 16, 8 + W), // DOWN: hangs from the ceiling
            box(8 - W, 0, 8 - W, 8 + W, H, 8 + W), // UP
            box(8 - W, 8 - W, 16 - H, 8 + W, 8 + W, 16), // NORTH: on the +Z wall
            box(8 - W, 8 - W, 0, 8 + W, 8 + W, H), // SOUTH
            box(16 - H, 8 - W, 8 - W, 16, 8 + W, 8 + W), // WEST
            box(0, 8 - W, 8 - W, H, 8 + W, 8 + W)}; // EAST

    public MarkerBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(MarkerBlock::new);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES[state.getValue(FACING).get3DDataValue()];
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                           CollisionContext context) {
        return Shapes.empty();
    }

    /** BuildCraft {@code canPlaceTorch}: the neighbour must render as a normal (full, opaque) block. */
    private static boolean canAttachTo(LevelReader level, BlockPos pos) {
        return level.getBlockState(pos).isSolidRender(level, pos);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        for (Direction facing : PLACEMENT_ORDER) {
            if (canAttachTo(context.getLevel(), context.getClickedPos().relative(facing.getOpposite()))) {
                return defaultBlockState().setValue(FACING, facing);
            }
        }
        return null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return canAttachTo(level, pos.relative(state.getValue(FACING).getOpposite()));
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return canSurvive(state, level, pos) ? state : Blocks.AIR.defaultBlockState();
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   BlockPos neighborPos, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof MarkerBlockEntity marker) {
            marker.updateSignals();
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof MarkerBlockEntity marker) {
            marker.tryConnection();
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()
                && level.getBlockEntity(pos) instanceof MarkerBlockEntity marker) {
            marker.destroy();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MarkerBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, BcBlockEntityTypes.MARKER.get(),
                (lvl, pos, st, marker) -> marker.serverTick());
    }
}
