package ivorius.psychedelicraft.block;

import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

public class VineStemBlock extends FlowerBlock implements BonemealableBlock {
    public static final MapCodec<VineStemBlock> CODEC = RecordCodecBuilder.<VineStemBlock>mapCodec(instance -> instance.group(
            BuiltInRegistries.BLOCK.byNameCodec().<Supplier<Block>>xmap(block -> () -> block, Supplier::get).fieldOf("lattice").forGetter(b -> b.lattice),
            BlockBehaviour.propertiesCodec()
    ).apply(instance, VineStemBlock::new));
    public static final BooleanProperty NORTH = PipeBlock.NORTH;
    public static final BooleanProperty EAST = PipeBlock.EAST;
    public static final BooleanProperty SOUTH = PipeBlock.SOUTH;
    public static final BooleanProperty WEST = PipeBlock.WEST;

    public static final IntegerProperty AGE = BlockStateProperties.AGE_4;
    public static final int MAX_AGE = BlockStateProperties.MAX_AGE_4;

    private final Supplier<Block> lattice;

    public VineStemBlock(Supplier<Block> lattice, Properties settings) {
        super(MobEffects.DIG_SLOWDOWN, 5, settings);
        this.lattice = lattice;
        registerDefaultState(defaultBlockState().setValue(AGE, 0).setValue(NORTH, false).setValue(SOUTH, false).setValue(EAST, false).setValue(WEST, false));
    }

    @Override
    public MapCodec<? extends VineStemBlock> codec() {
        return CODEC;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel world, BlockPos pos, RandomSource random) {
        performBonemeal(world, random, pos, state);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Level world = ctx.getLevel();
        BlockPos pos = ctx.getClickedPos();
        return super.getStateForPlacement(ctx)
                .setValue(NORTH, canConnect(world.getBlockState(pos.north()), Direction.SOUTH))
                .setValue(EAST, canConnect(world.getBlockState(pos.east()), Direction.WEST))
                .setValue(SOUTH, canConnect(world.getBlockState(pos.south()), Direction.NORTH))
                .setValue(WEST, canConnect(world.getBlockState(pos.west()), Direction.EAST));
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor world, BlockPos pos, BlockPos neighborPos) {
        if (direction.getAxis().isHorizontal()) {
            return state.setValue(PipeBlock.PROPERTY_BY_DIRECTION.get(direction), canConnect(neighborState, direction.getOpposite()));
        }
        return super.updateShape(state, direction, neighborState, world, pos, neighborPos);
    }

    public boolean canConnect(BlockState state, Direction dir) {
        return state.is(PSBlocks.MORNING_GLORY_LATTICE);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(NORTH, EAST, WEST, SOUTH, AGE);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader world, BlockPos pos, BlockState state) {
        return state.getValue(AGE) < MAX_AGE;
    }

    @Override
    public boolean isBonemealSuccess(Level world, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel world, RandomSource random, BlockPos pos, BlockState state) {
        if (state.getValue(AGE) < MAX_AGE) {
            world.setBlockAndUpdate(pos, state.cycle(AGE));
        }

        for (BlockPos mPos : BlockPos.spiralAround(pos, 1, Direction.NORTH, Direction.EAST)) {
            BlockState s = world.getBlockState(mPos);
            if (s.is(PSBlocks.LATTICE)) {
                world.setBlockAndUpdate(mPos, LatticeBlock.copyStateProperties(lattice.get().defaultBlockState(), s));
                return;
            }
        }
    }
}
