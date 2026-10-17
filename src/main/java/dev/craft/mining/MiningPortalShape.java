package dev.craft.mining;

import java.util.Optional;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Rectangular smooth stone frame with an inner size of 2×3 up to 21×21, standing along X or Z (spec 18 §4.1); same
 * rules as the nether portal frame, corners optional.
 */
public final class MiningPortalShape {
    public static final int MAX_SIZE = 21;
    private static final int MIN_WIDTH = 2;
    private static final int MIN_HEIGHT = 3;

    private final LevelAccessor level;
    private final Direction.Axis axis;
    private final Direction right;
    @Nullable
    private final BlockPos bottomLeft;
    private final int width;
    private final int height;
    private int portalBlocks;

    public MiningPortalShape(LevelAccessor level, BlockPos pos, Direction.Axis axis) {
        this.level = level;
        this.axis = axis;
        this.right = axis == Direction.Axis.X ? Direction.WEST : Direction.SOUTH;
        this.bottomLeft = findBottomLeft(pos);
        this.width = bottomLeft == null ? 0 : measureWidth();
        this.height = width == 0 ? 0 : measureHeight();
    }

    /** An empty, valid frame around {@code pos} along either axis (the given one first). */
    public static Optional<MiningPortalShape> findEmpty(LevelAccessor level, BlockPos pos, Direction.Axis axis) {
        MiningPortalShape shape = new MiningPortalShape(level, pos, axis);
        if (shape.isValid() && shape.portalBlocks == 0) {
            return Optional.of(shape);
        }
        Direction.Axis other = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
        shape = new MiningPortalShape(level, pos, other);
        return shape.isValid() && shape.portalBlocks == 0 ? Optional.of(shape) : Optional.empty();
    }

    public static boolean isFrame(BlockState state) {
        return state.is(Blocks.SMOOTH_STONE);
    }

    private static boolean isEmpty(BlockState state) {
        return state.isAir() || state.is(BlockTags.FIRE) || state.is(MiningBlocks.MINING_PORTAL.get());
    }

    @Nullable
    private BlockPos findBottomLeft(BlockPos pos) {
        int minY = Math.max(level.getMinBuildHeight(), pos.getY() - MAX_SIZE);
        while (pos.getY() > minY && isEmpty(level.getBlockState(pos.below()))) {
            pos = pos.below();
        }
        Direction left = right.getOpposite();
        int distance = distanceToFrame(pos, left) - 1;
        return distance < 0 ? null : pos.relative(left, distance);
    }

    private int measureWidth() {
        int distance = distanceToFrame(bottomLeft, right);
        return distance >= MIN_WIDTH && distance <= MAX_SIZE ? distance : 0;
    }

    /** Steps along the row until the side frame; every step needs empty space on frame. 0 if there is none. */
    private int distanceToFrame(BlockPos start, Direction direction) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int i = 0; i <= MAX_SIZE; i++) {
            pos.set(start).move(direction, i);
            BlockState state = level.getBlockState(pos);
            if (!isEmpty(state)) {
                return isFrame(state) ? i : 0;
            }
            if (!isFrame(level.getBlockState(pos.move(Direction.DOWN)))) {
                return 0;
            }
        }
        return 0;
    }

    private int measureHeight() {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int rows = 0;
        rows:
        for (; rows < MAX_SIZE; rows++) {
            if (!isFrame(level.getBlockState(pos.set(bottomLeft).move(Direction.UP, rows).move(right, -1)))
                    || !isFrame(level.getBlockState(pos.set(bottomLeft).move(Direction.UP, rows).move(right, width)))) {
                break;
            }
            for (int i = 0; i < width; i++) {
                BlockState state = level.getBlockState(pos.set(bottomLeft).move(Direction.UP, rows).move(right, i));
                if (!isEmpty(state)) {
                    break rows;
                }
                if (state.is(MiningBlocks.MINING_PORTAL.get())) {
                    portalBlocks++;
                }
            }
        }
        if (rows < MIN_HEIGHT) {
            return 0;
        }
        for (int i = 0; i < width; i++) {
            if (!isFrame(level.getBlockState(pos.set(bottomLeft).move(Direction.UP, rows).move(right, i)))) {
                return 0;
            }
        }
        return rows;
    }

    public boolean isValid() {
        return bottomLeft != null && width >= MIN_WIDTH && height >= MIN_HEIGHT;
    }

    public boolean isComplete() {
        return isValid() && portalBlocks == width * height;
    }

    public void createPortalBlocks() {
        BlockState portal = MiningBlocks.MINING_PORTAL.get().defaultBlockState().setValue(NetherPortalBlock.AXIS, axis);
        BlockPos.betweenClosed(bottomLeft, bottomLeft.relative(Direction.UP, height - 1).relative(right, width - 1))
                .forEach(pos -> level.setBlock(pos, portal, 18));
    }
}
