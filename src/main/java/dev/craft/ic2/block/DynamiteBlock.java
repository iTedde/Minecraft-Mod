package dev.craft.ic2.block;

import java.util.Map;
import javax.annotation.Nullable;

import dev.craft.ic2.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * IC2 {@code BlockDynamite} (spec 05 §7.1): a placed dynamite stick, attached like a torch to the solid block in
 * {@link #SUPPORT}. Breaking it or a redstone signal primes it (fuse 40), losing all support or an explosion sets it
 * off almost at once (fuse 5). It never drops anything. The same class is used with and without a linked
 * Dynamite-O-Mote.
 */
public class DynamiteBlock extends Block {
    /** Side of the supporting block; IC2 checks them in this order (metadata 5, 1, 2, 3, 4). */
    public static final DirectionProperty SUPPORT = DirectionProperty.create("support", Direction.DOWN,
            Direction.WEST, Direction.EAST, Direction.NORTH, Direction.SOUTH);
    public static final int IGNITE_FUSE = 40;
    public static final int EXPLODED_FUSE = 5;

    private static final Map<Direction, VoxelShape> SHAPES = Map.of(
            Direction.DOWN, Block.box(6.4, 0.0, 6.4, 9.6, 9.6, 9.6),
            Direction.WEST, Block.box(0.0, 3.2, 5.6, 4.8, 12.8, 10.4),
            Direction.EAST, Block.box(11.2, 3.2, 5.6, 16.0, 12.8, 10.4),
            Direction.NORTH, Block.box(5.6, 3.2, 0.0, 10.4, 12.8, 4.8),
            Direction.SOUTH, Block.box(5.6, 3.2, 11.2, 10.4, 12.8, 16.0));

    public DynamiteBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(SUPPORT, Direction.DOWN));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SUPPORT);
    }

    /** First solid neighbour in IC2 order, or null; "solid" is IC2's {@code isBlockNormalCube}. */
    @Nullable
    public static Direction findSupport(LevelReader level, BlockPos pos) {
        for (Direction side : SUPPORT.getPossibleValues()) {
            if (isSolid(level, pos.relative(side))) {
                return side;
            }
        }
        return null;
    }

    private static boolean isSolid(LevelReader level, BlockPos pos) {
        return level.getBlockState(pos).isRedstoneConductor(level, pos);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return findSupport(level, pos) != null;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(SUPPORT));
    }

    /** IC2 {@code onBlockAdded}: primes when powered, otherwise attaches to the first solid neighbour. */
    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (level.isClientSide() || oldState.is(state.getBlock())) {
            return;
        }
        if (level.hasNeighborSignal(pos)) {
            ignite(level, pos);
            return;
        }
        Direction support = findSupport(level, pos);
        if (support == null) {
            explode(level, pos);
        } else if (support != state.getValue(SUPPORT)) {
            level.setBlock(pos, state.setValue(SUPPORT, support), Block.UPDATE_CLIENTS);
        }
    }

    /**
     * Primes on a redstone signal, goes off when no solid neighbour is left and silently vanishes when only its own
     * support is gone (IC2 drops it with {@code quantityDropped = 0}).
     */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   BlockPos neighborPos, boolean movedByPiston) {
        if (level.isClientSide()) {
            return;
        }
        // IC2 1.4.7 got the id of the new neighbour block; 1.21 passes the old one, so look at the current state
        if (level.getBlockState(neighborPos).isSignalSource() && level.hasNeighborSignal(pos)) {
            ignite(level, pos);
        } else if (findSupport(level, pos) == null) {
            explode(level, pos);
        } else if (!isSolid(level, pos.relative(state.getValue(SUPPORT)))) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    /** IC2 {@code onBlockDestroyedByPlayer}: breaking it (also in creative) primes it. */
    @Override
    public void destroy(LevelAccessor level, BlockPos pos, BlockState state) {
        if (level instanceof Level world && !world.isClientSide()) {
            spawnPrimed(world, pos, IGNITE_FUSE, true);
        }
    }

    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        if (!level.isClientSide()) {
            spawnPrimed(level, pos, EXPLODED_FUSE, false);
        }
    }

    @Override
    public boolean dropFromExplosion(Explosion explosion) {
        return false;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(Ic2Items.DYNAMITE.get());
    }

    /** Redstone: removes the block and primes it with fuse 40 and the fuse sound. */
    public static void ignite(Level level, BlockPos pos) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        spawnPrimed(level, pos, IGNITE_FUSE, true);
    }

    /** {@code onBlockDestroyedByExplosion} + removal: support lost or set off by the Dynamite-O-Mote (fuse 5). */
    public static void explode(Level level, BlockPos pos) {
        spawnPrimed(level, pos, EXPLODED_FUSE, false);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
    }

    private static void spawnPrimed(Level level, BlockPos pos, int fuse, boolean sound) {
        DynamiteEntity entity = new DynamiteEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, false);
        entity.setFuse(fuse);
        level.addFreshEntity(entity);
        if (sound) {
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.TNT_PRIMED,
                    SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }
}
