package ivorius.psychedelicraft.block;

import java.util.HashSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.block.GlassTubeBlock.IODirection;
import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.component.Impurities;
import ivorius.psychedelicraft.recipe.FluidMound;

public class PumpBlock extends DirectionalBlock implements PipeInsertable {
    public static final MapCodec<PumpBlock> CODEC = simpleCodec(PumpBlock::new);

    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public PumpBlock(Properties settings) {
        super(settings);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH).setValue(POWERED, false));
    }

    @Override
    protected MapCodec<? extends PumpBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, POWERED);
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
    protected void neighborChanged(BlockState state, Level world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
        super.neighborChanged(state, world, pos, sourceBlock, sourcePos, notify);
        boolean powered = world.hasNeighborSignal(pos);
        if (powered != state.getValue(POWERED)) {
            activate(state, world, pos, powered);
        }
    }

    @Override
    protected void onPlace(BlockState state, Level world, BlockPos pos, BlockState oldState, boolean notify) {
        if (!oldState.is(state.getBlock())) {
            boolean powered = world.hasNeighborSignal(pos);
            if (powered) {
                activate(state, world, pos, powered);
            }
        }
    }

    private void activate(BlockState state, Level world, BlockPos pos, boolean powered) {
        Direction facing = state.getValue(FACING);
        Direction headFacing = PumpHeadBlock.getHeadFacing(facing);
        BlockPos headPos = pos.relative(headFacing);
        BlockState headState = world.getBlockState(headPos);

        if (headState.isRedstoneConductor(world, headPos) && !headState.is(PSBlocks.PUMP_HEAD)) {
            return;
        }
        world.setBlockAndUpdate(pos, state.setValue(POWERED, powered));

        if (powered) {
            if (!headState.is(PSBlocks.PUMP_HEAD)) {
                world.destroyBlock(headPos, true);
                BlockState newHeadState = pushEntitiesUp(headState, PSBlocks.PUMP_HEAD.defaultBlockState().setValue(FACING, headFacing), world, headPos);
                world.setBlock(headPos, newHeadState, Block.UPDATE_CLIENTS);
            }
        } else {
            if (headState.is(PSBlocks.PUMP_HEAD)) {
                world.setBlock(headPos, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }

        world.playSound(null, pos, powered ? SoundEvents.PISTON_EXTEND : SoundEvents.PISTON_CONTRACT, SoundSource.BLOCKS, 0.5F, world.random.nextFloat() * 0.25F + 0.6F);
        world.gameEvent(powered ? GameEvent.BLOCK_ACTIVATE : GameEvent.BLOCK_DEACTIVATE, pos, GameEvent.Context.of(state));
        if (!powered) {
            return;
        }

        if (!(world instanceof ServerLevel sw)) {
            return;
        }

        @Nullable
        BlockPos inPos = findInputPos(world, pos.relative(facing));
        if (inPos == null) {
            return;
        }

        BlockState inFluid = world.getBlockState(inPos);
        if (inFluid.getFluidState().isEmpty()) {
            return;
        }

        BlockPos outPos = pos.relative(facing.getOpposite());
        BlockState outState = world.getBlockState(outPos);
        outState.getOptionalValue(GlassTubeBlock.IN).orElse(IODirection.NONE).getDirection().ifPresent(inDirection -> {
            if (inDirection == facing) {
                for (int i = 0; i < 10; i++) {
                    PipeInsertable.tryInsert(sw, outPos, facing.getOpposite(), PipeFluids.of(FluidMound.of(
                            SimpleFluid.of(inFluid.getFluidState().getType()).getDefaultStack(FluidVolumes.BUCKET / 10)
                    ), Impurities.EMPTY, 0));
                }
                if (inFluid.getBlock() instanceof BucketPickup drainable) {
                    drainable.pickupBlock(null, world, inPos, inFluid);
                }
            }
        });
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return super.getStateForPlacement(ctx)
                .setValue(FACING, ctx.getNearestLookingDirection().getOpposite());
    }

    @Override
    public boolean acceptsConnectionFrom(LevelAccessor world, BlockState state, BlockPos pos, BlockState neighborState, BlockPos neighborPos, Direction direction, boolean input) {
        return !input && direction == state.getValue(FACING) && neighborState.getBlock() instanceof GlassTubeBlock;
    }

    @Nullable
    static BlockPos findInputPos(LevelAccessor world, BlockPos initial) {
        return findInputPos(world, initial.mutable(), new HashSet<>(), 10);
    }

    @Nullable
    static BlockPos findInputPos(LevelAccessor world, BlockPos.MutableBlockPos mutable, Set<BlockPos> visited, int maxDepth) {
        BlockPos pos = mutable.immutable();
        if (!visited.add(pos)) {
            return null;
        }
        FluidState state = world.getFluidState(pos);

        if (state.isEmpty()) {
            return null;
        }

        if (state.isSource()) {
            return pos;
        }

        @Nullable
        BlockPos nearest = null;
        SimpleFluid main = SimpleFluid.of(state.getType());
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            FluidState neighbor = world.getFluidState(mutable.set(pos).move(dir));
            SimpleFluid fluid = SimpleFluid.of(neighbor.getType());
            if (fluid == main) {
                BlockPos p = findInputPos(world, mutable, visited, maxDepth - 1);
                if (p != null && (nearest == null || pos.distSqr(nearest) > pos.distSqr(p))) {
                    nearest = p;
                }
            }
        }

        if (nearest == null) {
            return findInputPos(world, mutable.move(Direction.DOWN), visited, maxDepth - 1);
        }

        return nearest;
    }
}
