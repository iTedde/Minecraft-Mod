package dev.craft.ic2.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Rubber mat (IC2 {@code BlockRubberSheet}, "trampoline"): a 2 px sheet held by horizontal neighbors. Hanging over
 * air it bounces falling entities back up, or tears when a living entity lands on an unsupported sheet.
 */
public class RubberMatBlock extends Block {
    public static final MapCodec<RubberMatBlock> CODEC = simpleCodec(RubberMatBlock::new);
    /** IC2 metadata 1: cached result of {@link #canSupportWeight}. */
    public static final BooleanProperty SUPPORTED = BooleanProperty.create("supported");
    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 2, 16);

    public RubberMatBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(SUPPORTED, false));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SUPPORTED);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            if (isSupporter(level, pos.relative(direction))) {
                return true;
            }
        }
        return false;
    }

    private boolean isSupporter(BlockGetter level, BlockPos pos) {
        return isNormalCube(level, pos) || level.getBlockState(pos).is(this);
    }

    private static boolean isNormalCube(BlockGetter level, BlockPos pos) {
        return level.getBlockState(pos).isRedstoneConductor(level, pos);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, BlockPos neighborPos,
                                   boolean movedByPiston) {
        if (state.getValue(SUPPORTED)) {
            state = state.setValue(SUPPORTED, false);
            level.setBlock(pos, state, Block.UPDATE_NONE);
        }
        if (!state.canSurvive(level, pos)) {
            level.destroyBlock(pos, true);
        }
    }

    /**
     * IC2 {@code canSupportWeight}: a chain of mats along x or z must end in a normal cube on both sides, or rest on
     * one somewhere below the chain.
     */
    public boolean canSupportWeight(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getValue(SUPPORTED)) {
            return true;
        }
        for (Direction.Axis axis : new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z}) {
            if (isChainHeld(level, pos, Direction.fromAxisAndDirection(axis, Direction.AxisDirection.NEGATIVE))
                    && isChainHeld(level, pos, Direction.fromAxisAndDirection(axis, Direction.AxisDirection.POSITIVE))) {
                if (!level.isClientSide()) {
                    level.setBlock(pos, state.setValue(SUPPORTED, true), Block.UPDATE_NONE);
                }
                return true;
            }
        }
        return false;
    }

    private boolean isChainHeld(Level level, BlockPos start, Direction direction) {
        BlockPos pos = start;
        while (true) {
            if (isNormalCube(level, pos)) {
                return true;
            }
            if (!level.getBlockState(pos).is(this)) {
                return false;
            }
            if (isNormalCube(level, pos.below())) {
                return true;
            }
            pos = pos.relative(direction);
        }
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isEmptyBlock(pos.below())) {
            return;
        }
        if (entity instanceof LivingEntity && !canSupportWeight(level, pos)) {
            if (!level.isClientSide()) {
                level.removeBlock(pos, false);
            }
            return;
        }
        Vec3 motion = entity.getDeltaMovement();
        if (motion.y > -0.4) {
            return;
        }
        entity.resetFallDistance();
        double bounce;
        if (entity instanceof LivingEntity living && living.jumping) {
            bounce = -1.3;
        } else if (entity instanceof Player player && player.isShiftKeyDown()) {
            bounce = -0.1;
        } else {
            bounce = -0.8;
        }
        entity.setDeltaMovement(motion.x * 1.1, motion.y * bounce, motion.z * 1.1);
    }
}
