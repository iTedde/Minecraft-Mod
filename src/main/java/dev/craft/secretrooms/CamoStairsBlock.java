package dev.craft.secretrooms;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Camouflaged stairs ({@code BlockCamoStair}, spec 50 §4): looks like a full camouflaged block, but one walks on it
 * like on stairs – the collision is a half slab plus a raised half towards {@link #FACING}.
 */
public class CamoStairsBlock extends CamoBlock {
    public static final MapCodec<CamoStairsBlock> CODEC = simpleCodec(CamoStairsBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;

    public CamoStairsBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(HALF, Half.BOTTOM));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        boolean top = face == Direction.DOWN
                || face != Direction.UP && context.getClickLocation().y - context.getClickedPos().getY() > 0.5;
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection())
                .setValue(HALF, top ? Half.TOP : Half.BOTTOM);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        boolean top = state.getValue(HALF) == Half.TOP;
        VoxelShape slab = top ? box(0, 8, 0, 16, 16, 16) : box(0, 0, 0, 16, 8, 16);
        double y0 = top ? 0 : 8;
        double y1 = top ? 8 : 16;
        VoxelShape step = switch (state.getValue(FACING)) {
            case EAST -> box(8, y0, 0, 16, y1, 16);
            case WEST -> box(0, y0, 0, 8, y1, 16);
            case SOUTH -> box(0, y0, 8, 16, y1, 16);
            default -> box(0, y0, 0, 16, y1, 8);
        };
        return Shapes.or(slab, step);
    }
}
