package dev.craft.secretrooms;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.util.StringRepresentable;

/**
 * Camouflaged redstone ({@code BlockCamoWire}, spec 50 §4): a full camouflaged block that carries a signal on without
 * loss. It remembers the side it is powered from ({@link #SOURCE}) and gives 15 (strong) to every other side; it goes
 * off when that side no longer powers it.
 */
public class CamoRedstoneBlock extends CamoBlock {
    public enum Source implements StringRepresentable {
        NONE, DOWN, UP, NORTH, SOUTH, WEST, EAST;

        public Direction direction() {
            return this == NONE ? null : Direction.values()[ordinal() - 1];
        }

        public static Source of(Direction direction) {
            return values()[direction.ordinal() + 1];
        }

        @Override
        public String getSerializedName() {
            return name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    public static final MapCodec<CamoRedstoneBlock> CODEC = simpleCodec(CamoRedstoneBlock::new);
    public static final EnumProperty<Source> SOURCE = EnumProperty.create("source", Source.class);

    public CamoRedstoneBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(SOURCE, Source.NONE));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SOURCE);
    }

    /** {@code isBlockPoweredBy}: lit redstone dust, another camo redstone not fed from here, or any power source. */
    private boolean poweredFrom(Level level, BlockPos pos, Direction side) {
        BlockPos other = pos.relative(side);
        BlockState state = level.getBlockState(other);
        if (state.isAir()) {
            return false;
        }
        if (state.is(Blocks.REDSTONE_WIRE)) {
            return state.getValue(RedStoneWireBlock.POWER) > 0;
        }
        if (state.getBlock() instanceof CamoRedstoneBlock) {
            Source source = state.getValue(SOURCE);
            return source != Source.NONE && source.direction() != side.getOpposite();
        }
        return state.isSignalSource() && (state.getSignal(level, other, side) > 0
                || state.getDirectSignal(level, other, side) > 0) || level.getSignal(other, side) > 0
                && !(state.getBlock() instanceof CamoRedstoneBlock);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos from,
            boolean moving) {
        if (level.isClientSide()) {
            return;
        }
        Source current = state.getValue(SOURCE);
        if (current != Source.NONE && poweredFrom(level, pos, current.direction())) {
            return;
        }
        Source next = Source.NONE;
        for (Direction side : Direction.values()) {
            if (poweredFrom(level, pos, side)) {
                next = Source.of(side);
            }
        }
        if (next != current) {
            level.setBlock(pos, state.setValue(SOURCE, next), Block.UPDATE_ALL);
            for (Direction side : Direction.values()) {
                level.updateNeighborsAt(pos.relative(side), this);
            }
        }
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    /** {@code isProvidingStrongPower}: 15 to every side but the one it is fed from. */
    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        Source source = state.getValue(SOURCE);
        return source != Source.NONE && direction.getOpposite() != source.direction() ? 15 : 0;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getDirectSignal(state, level, pos, direction);
    }
}
