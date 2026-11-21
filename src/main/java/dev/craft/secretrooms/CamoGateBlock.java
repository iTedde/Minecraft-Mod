package dev.craft.secretrooms;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/**
 * Camouflaged gate ({@code BlockCamoGate}, spec 50 §4): a redstone signal at the gate or the block above pushes out
 * a wall of up to 10 gate extensions towards {@link #FACING}; without signal they are removed again. Kept 1:1: the
 * first position is filled even where a non-full block stands, further ones only in air; a downward gate never
 * retracts (the original checks its own position instead of the extension).
 */
public class CamoGateBlock extends CamoBlock {
    public static final MapCodec<CamoGateBlock> CODEC = simpleCodec(CamoGateBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final int MAX_SIZE = 10;

    public CamoGateBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    /** {@code setDefaultDirection}: towards the player; straight up/down when standing close above/below. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Player player = context.getPlayer();
        BlockPos pos = context.getClickedPos();
        Direction facing = context.getHorizontalDirection().getOpposite();
        if (player != null && Math.abs(player.getX() - pos.getX()) < 2.0
                && Math.abs(player.getZ() - pos.getZ()) < 2.0) {
            double eye = player.getEyeY();
            if (eye - pos.getY() > 2.0) {
                facing = Direction.UP;
            } else if (pos.getY() - eye > 0.0) {
                facing = Direction.DOWN;
            }
        }
        return defaultBlockState().setValue(FACING, facing);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos from,
            boolean moving) {
        if (!level.isClientSide()) {
            level.scheduleTick(pos, this, 1);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.hasNeighborSignal(pos) || level.hasNeighborSignal(pos.above())) {
            build(level, pos, state.getValue(FACING));
        } else {
            destroy(level, pos, state.getValue(FACING));
        }
    }

    private static void build(Level level, BlockPos pos, Direction facing) {
        BlockPos first = pos.relative(facing);
        for (int i = 1; i <= MAX_SIZE; i++) {
            BlockPos target = pos.relative(facing, i);
            if (level.getBlockState(target).is(SecretRooms.CAMO_GATE_EXTENSION.get())) {
                continue;
            }
            if (!level.getBlockState(target).isAir() && !breakable(level, first)) {
                break;
            }
            level.setBlock(target, SecretRooms.CAMO_GATE_EXTENSION.get().defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    /** {@code isBreakable}: air, or a non-full block that is neither camouflage nor one-way glass. */
    private static boolean breakable(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || !(state.getBlock() instanceof CamoBlock)
                && !state.isCollisionShapeFullBlock(level, pos);
    }

    private static void destroy(Level level, BlockPos pos, Direction facing) {
        for (int i = 1; i <= MAX_SIZE; i++) {
            BlockPos check = facing == Direction.DOWN ? pos : pos.relative(facing, i);
            if (level.getBlockState(check).is(SecretRooms.CAMO_GATE_EXTENSION.get())) {
                level.setBlock(pos.relative(facing, i), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),
                        Block.UPDATE_ALL);
            }
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock())) {
            destroy(level, pos, state.getValue(FACING));
        }
        super.onRemove(state, level, pos, newState, moving);
    }
}
