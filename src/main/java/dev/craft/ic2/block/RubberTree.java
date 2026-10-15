package dev.craft.ic2.block;

import java.util.ArrayList;
import java.util.List;

import dev.craft.ic2.Ic2Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;

/** Rubber tree shape, 1:1 with IC2 {@code WorldGenRubTree.grow}; used by the sapling and the world generation. */
public final class RubberTree {
    public static final int MAX_HEIGHT = 8;

    private RubberTree() {
    }

    /** Grows a rubber tree at {@code pos} (air or a rubber sapling). */
    public static boolean grow(LevelAccessor level, BlockPos pos, RandomSource random) {
        int h = getGrowHeight(level, pos);
        if (h < 2) {
            return false;
        }
        int height = h / 2;
        h -= h / 2;
        height += random.nextInt(h + 1);
        int treeHoleChance = 25;
        List<BlockPos> leaves = new ArrayList<>();
        BlockState wood = Ic2Blocks.RUBBER_WOOD.get().defaultBlockState();
        for (int i = 0; i < height; i++) {
            BlockPos trunk = pos.above(i);
            if (random.nextInt(100) <= treeHoleChance) {
                treeHoleChance -= 10;
                level.setBlock(trunk, wood.setValue(RubberWoodBlock.RESIN, RubberWoodBlock.Resin.WET)
                        .setValue(RubberWoodBlock.FACING, Direction.from3DDataValue(random.nextInt(4) + 2)),
                        Block.UPDATE_ALL);
            } else {
                level.setBlock(trunk, wood.setValue(RubberWoodBlock.RESIN, RubberWoodBlock.Resin.NATURAL),
                        Block.UPDATE_ALL);
            }
            if (height < 4 || height < 7 && i > 1 || i > 2) {
                for (int a = pos.getX() - 2; a <= pos.getX() + 2; a++) {
                    for (int b = pos.getZ() - 2; b <= pos.getZ() + 2; b++) {
                        int c = Math.max(1, i + 4 - height);
                        boolean gen = a > pos.getX() - 2 && a < pos.getX() + 2 && b > pos.getZ() - 2 && b < pos.getZ() + 2
                                || a > pos.getX() - 2 && a < pos.getX() + 2 && random.nextInt(c) == 0
                                || b > pos.getZ() - 2 && b < pos.getZ() + 2 && random.nextInt(c) == 0;
                        BlockPos leaf = new BlockPos(a, trunk.getY(), b);
                        if (gen && level.isEmptyBlock(leaf)) {
                            placeLeaves(level, leaf, leaves);
                        }
                    }
                }
            }
        }
        // The loop condition draws a new random number on every iteration, as in IC2.
        for (int i = 0; i <= height / 4 + random.nextInt(2); i++) {
            BlockPos top = pos.above(height + i);
            if (level.isEmptyBlock(top)) {
                placeLeaves(level, top, leaves);
            }
        }
        updateLeafDistances(level, leaves);
        return true;
    }

    /** 1 + free air blocks above {@code pos} (max. 8), or 0 if the ground cannot hold a sapling. */
    public static int getGrowHeight(LevelAccessor level, BlockPos pos) {
        BlockState sapling = Ic2Blocks.RUBBER_SAPLING.get().defaultBlockState();
        BlockState here = level.getBlockState(pos);
        if (!sapling.canSurvive(level, pos) || !here.isAir() && !here.is(Ic2Blocks.RUBBER_SAPLING.get())) {
            return 0;
        }
        int height = 1;
        BlockPos.MutableBlockPos cursor = pos.mutable();
        while (level.isEmptyBlock(cursor.move(Direction.UP)) && height < MAX_HEIGHT) {
            height++;
        }
        return height;
    }

    private static void placeLeaves(LevelAccessor level, BlockPos pos, List<BlockPos> placed) {
        level.setBlock(pos, Ic2Blocks.RUBBER_LEAVES.get().defaultBlockState(), Block.UPDATE_ALL);
        placed.add(pos.immutable());
    }

    /** Sets the vanilla leaf distance of the new leaves so that they do not decay right away. */
    private static void updateLeafDistances(LevelAccessor level, List<BlockPos> leaves) {
        boolean changed = true;
        while (changed) {
            changed = false;
            for (BlockPos pos : leaves) {
                BlockState state = level.getBlockState(pos);
                if (!(state.getBlock() instanceof LeavesBlock)) {
                    continue;
                }
                int distance = LeavesBlock.DECAY_DISTANCE;
                for (Direction direction : Direction.values()) {
                    distance = Math.min(distance, LeavesBlock.getOptionalDistanceAt(
                            level.getBlockState(pos.relative(direction))).orElse(LeavesBlock.DECAY_DISTANCE) + 1);
                }
                if (distance < state.getValue(LeavesBlock.DISTANCE)) {
                    level.setBlock(pos, state.setValue(LeavesBlock.DISTANCE, distance), Block.UPDATE_CLIENTS);
                    changed = true;
                }
            }
        }
    }
}
