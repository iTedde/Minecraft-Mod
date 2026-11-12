package dev.craft.nuclearcontrol.block;

import java.util.EnumMap;
import java.util.Map;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Flat Nuclear Control blocks that need a solid face behind them ({@code solidBlockRequired}: thermal monitor,
 * alarms). {@link #FACING} points away from the supporting block (the clicked face); the box is {@code inset} px in
 * from the sides and 7 px high ({@code blockSize}). Without support the block drops as an item.
 */
public abstract class AttachedBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final int HEIGHT = 7;

    private final Map<Direction, VoxelShape> shapes = new EnumMap<>(Direction.class);

    protected AttachedBlock(Properties properties, int inset) {
        super(properties);
        for (Direction facing : Direction.values()) {
            shapes.put(facing, box(facing, inset));
        }
    }

    /** Box of a plate with the given inset, lying on the face opposite to {@code facing}. */
    public static VoxelShape box(Direction facing, int inset) {
        int a = inset;
        int b = 16 - inset;
        return switch (facing) {
            case UP -> Block.box(a, 0, a, b, HEIGHT, b);
            case DOWN -> Block.box(a, 16 - HEIGHT, a, b, 16, b);
            case NORTH -> Block.box(a, a, 16 - HEIGHT, b, b, 16);
            case SOUTH -> Block.box(a, a, 0, b, b, HEIGHT);
            case WEST -> Block.box(16 - HEIGHT, a, a, 16, b, b);
            case EAST -> Block.box(0, a, a, HEIGHT, b, b);
        };
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes.get(state.getValue(FACING));
    }

    /** {@code isBlockSolidOnSide} of the block behind. */
    public static boolean isSupported(LevelReader level, BlockPos pos, Direction facing) {
        BlockPos support = pos.relative(facing.getOpposite());
        return level.getBlockState(support).isFaceSturdy(level, support, facing);
    }

    /** The clicked face if it holds the block, otherwise the first face (IC2 order down…east) that does. */
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clicked = context.getClickedFace();
        if (isSupported(context.getLevel(), context.getClickedPos(), clicked)) {
            return defaultBlockState().setValue(FACING, clicked);
        }
        for (Direction facing : Direction.values()) {
            if (isSupported(context.getLevel(), context.getClickedPos(), facing)) {
                return defaultBlockState().setValue(FACING, facing);
            }
        }
        return null;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return isSupported(level, pos, state.getValue(FACING));
    }

    /** {@code onNeighborBlockChange}: drop the block as an item when the support is gone. */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   BlockPos neighborPos, boolean movedByPiston) {
        if (!level.isClientSide() && !canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
            return;
        }
        onNeighborChanged(state, level, pos);
    }

    protected void onNeighborChanged(BlockState state, Level level, BlockPos pos) {
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
