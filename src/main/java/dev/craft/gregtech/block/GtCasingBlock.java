package dev.craft.gregtech.block;

import java.util.Map;

import dev.craft.gregtech.machine.MachineBlockUpdates;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

/**
 * GT machine casing ({@code GT_BlockMetaID_Block} 13–15): building block of the multiblock machines. Placing or
 * removing one notifies the machines around it ({@code causeMachineUpdate}, spec 13 §5.1). Each side property tells
 * whether the neighbour there is a casing of the same type; the model picks the connected texture from it.
 */
public class GtCasingBlock extends Block {
    /** Texture variant on a side by its missing (unconnected) edges: bits left 1, top 2, right 4, bottom 8. */
    private static final int[] VARIANT_BY_MISSING_EDGES = {6, 2, 3, 8, 4, 1, 9, 1, 5, 11, 0, 0, 10, 1, 0, 7};
    /** Neighbours at the left, top, right and bottom edge of each side's texture (default model UVs). */
    private static final Map<Direction, Direction[]> EDGES = Map.of(
            Direction.DOWN, new Direction[]{Direction.WEST, Direction.SOUTH, Direction.EAST, Direction.NORTH},
            Direction.UP, new Direction[]{Direction.WEST, Direction.NORTH, Direction.EAST, Direction.SOUTH},
            Direction.NORTH, new Direction[]{Direction.EAST, Direction.UP, Direction.WEST, Direction.DOWN},
            Direction.SOUTH, new Direction[]{Direction.WEST, Direction.UP, Direction.EAST, Direction.DOWN},
            Direction.WEST, new Direction[]{Direction.NORTH, Direction.UP, Direction.SOUTH, Direction.DOWN},
            Direction.EAST, new Direction[]{Direction.SOUTH, Direction.UP, Direction.NORTH, Direction.DOWN});

    private final Type type;

    public GtCasingBlock(Properties properties, Type type) {
        super(properties);
        this.type = type;
        BlockState state = stateDefinition.any();
        for (Direction side : Direction.values()) {
            state = state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(side), false);
        }
        registerDefaultState(state);
    }

    public Type getType() {
        return type;
    }

    /**
     * Connected texture variant (0–11, offset from {@link Type#getConnectedTexture()}) of {@code side}: 7 when the
     * neighbour in front of it is a matching casing, otherwise chosen by which edges lack one ({@code getBlockTexture}).
     */
    public static int textureVariant(BlockState state, Direction side) {
        if (connected(state, side)) {
            return 7;
        }
        Direction[] edges = EDGES.get(side);
        int missing = 0;
        for (int i = 0; i < edges.length; i++) {
            if (!connected(state, edges[i])) {
                missing |= 1 << i;
            }
        }
        return VARIANT_BY_MISSING_EDGES[missing];
    }

    private static boolean connected(BlockState state, Direction side) {
        return state.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(side));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        PipeBlock.PROPERTY_BY_DIRECTION.values().forEach(builder::add);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return connectedState(context.getLevel(), context.getClickedPos());
    }

    /** Casing state for the given position, connected to the matching casings around it. */
    public BlockState connectedState(BlockGetter level, BlockPos pos) {
        BlockState state = defaultBlockState();
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
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!oldState.is(state.getBlock())) {
            MachineBlockUpdates.cause(level, pos);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (!state.is(newState.getBlock())) {
            MachineBlockUpdates.cause(level, pos);
        }
    }

    /** Casing kinds with their blast furnace heat and effective explosion resistance. */
    public enum Type {
        STANDARD("standard_machine_casing", 13, 96, 30, 30.0F),
        REINFORCED("reinforced_machine_casing", 14, 112, 50, 60.0F),
        ADVANCED("gt_advanced_machine_casing", 15, 224, 70, 30.0F);

        private final String name;
        private final int texture;
        private final int connectedTexture;
        private final int heat;
        private final float resistance;

        Type(String name, int texture, int connectedTexture, int heat, float resistance) {
            this.name = name;
            this.texture = texture;
            this.connectedTexture = connectedTexture;
            this.heat = heat;
            this.resistance = resistance;
        }

        public String getName() {
            return name;
        }

        /** Index on GregTech's {@code blocks.png} (= metadata), used for the item. */
        public int getTexture() {
            return texture;
        }

        /** First of the 12 connected texture variants on {@code blocks.png} for the placed block. */
        public int getConnectedTexture() {
            return connectedTexture;
        }

        /** Heat each casing adds to the industrial blast furnace (K). */
        public int getHeat() {
            return heat;
        }

        public float getResistance() {
            return resistance;
        }
    }
}
