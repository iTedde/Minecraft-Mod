package dev.craft.ic2.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;

/**
 * Rubber wood (IC2 {@code BlockRubWood}). IC2's metadata becomes {@link #RESIN} plus {@link #FACING} for the side of
 * the resin hole: 0 → {@code none} (placed by a player), 1 → {@code natural}, 2–5 → {@code wet}, 6–11 → {@code dry}.
 * Dry holes turn wet again with 1/200 per update (scheduled every 100 ticks and on random ticks).
 */
public class RubberWoodBlock extends Block {
    public static final MapCodec<RubberWoodBlock> CODEC = simpleCodec(RubberWoodBlock::new);
    public static final EnumProperty<Resin> RESIN = EnumProperty.create("resin", Resin.class);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final int TICK_RATE = 100;
    public static final int REFILL_CHANCE = 200;

    public RubberWoodBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(RESIN, Resin.NONE).setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(RESIN, FACING);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(RESIN) == Resin.DRY;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        tick(state, level, pos, random);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(RESIN) != Resin.DRY) {
            return;
        }
        if (random.nextInt(REFILL_CHANCE) == 0) {
            level.setBlock(pos, state.setValue(RESIN, Resin.WET), Block.UPDATE_ALL);
        } else {
            level.scheduleTick(pos, this, TICK_RATE);
        }
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 20;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 4;
    }

    public enum Resin implements StringRepresentable {
        NONE("none"),
        NATURAL("natural"),
        WET("wet"),
        DRY("dry");

        private final String name;

        Resin(String name) {
            this.name = name;
        }

        /** Whether the block has a resin hole (wet or dry). */
        public boolean hasHole() {
            return this == WET || this == DRY;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
