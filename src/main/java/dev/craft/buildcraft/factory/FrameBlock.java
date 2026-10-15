package dev.craft.buildcraft.factory;

import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Quarry frame, BuildCraft {@code BlockFrame}: drawn and collided like a legacy pipe (core 0.25–0.75 plus arms to
 * neighbouring frames), drops nothing. {@link #DECAYING} (meta 1) frames vanish on random ticks with 4/10 chance.
 */
public class FrameBlock extends Block {
    public static final MapCodec<FrameBlock> CODEC = simpleCodec(FrameBlock::new);
    public static final BooleanProperty DECAYING = BooleanProperty.create("decaying");
    private static final VoxelShape CORE = box(4, 4, 4, 12, 12, 12);
    private static final Map<Direction, VoxelShape> ARMS = Map.of(
            Direction.DOWN, box(4, 0, 4, 12, 4, 12), Direction.UP, box(4, 12, 4, 12, 16, 12),
            Direction.NORTH, box(4, 4, 0, 12, 12, 4), Direction.SOUTH, box(4, 4, 12, 12, 12, 16),
            Direction.WEST, box(0, 4, 4, 4, 12, 12), Direction.EAST, box(12, 4, 4, 16, 12, 12));
    private final Map<BlockState, VoxelShape> shapes = new HashMap<>();

    public FrameBlock(Properties properties) {
        super(properties);
        BlockState state = stateDefinition.any().setValue(DECAYING, false);
        for (Direction side : Direction.values()) {
            state = state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(side), false);
        }
        registerDefaultState(state);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(DECAYING);
        PipeBlock.PROPERTY_BY_DIRECTION.values().forEach(builder::add);
    }

    /** Frame state for the given position, connected to the frames around it. */
    public BlockState connectedState(BlockGetter level, BlockPos pos, boolean decaying) {
        BlockState state = defaultBlockState().setValue(DECAYING, decaying);
        for (Direction side : Direction.values()) {
            state = state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(side),
                    level.getBlockState(pos.relative(side)).is(this));
        }
        return state;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction), neighborState.is(this));
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes.computeIfAbsent(state, s -> {
            VoxelShape shape = CORE;
            for (Direction side : Direction.values()) {
                if (s.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(side))) {
                    shape = Shapes.or(shape, ARMS.get(side));
                }
            }
            return shape;
        });
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(DECAYING);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(DECAYING) && random.nextInt(10) > 5) {
            level.removeBlock(pos, false);
        }
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }
}
