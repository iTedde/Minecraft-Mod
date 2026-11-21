package dev.craft.secretrooms;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.Shapes;

/**
 * How a camouflaged block picks its look (spec 50 §4, {@code BlockCamoFull.getIdCamoStyle},
 * {@code BlockOneWay.getOtherProperties}). Only full cubes count (opaque, one-way glass, or full bounds like glass);
 * a camouflaged neighbour passes on its own camouflage.
 */
public final class CamoSelection {
    private CamoSelection() {
    }

    /** The look of the neighbour at {@code pos}, or null if it does not count. */
    public static BlockState lookOf(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return null;
        }
        if (level.getBlockEntity(pos) instanceof CamoBlockEntity camo && state.getBlock() instanceof CamoBlock) {
            return camo.getCamo();
        }
        if (state.getBlock() instanceof OneWayGlassBlock
                || state.isSolidRender(level, pos)
                || state.getShape(level, pos) == Shapes.block()) {
            return state;
        }
        return null;
    }

    /**
     * Full camouflage: a matching pair of opposite neighbours wins (down/up, west/east, north/south; two equal pairs:
     * that block); otherwise the first counting neighbour (down, up, west, east, north, south) – the original's
     * majority count fails on an index error and falls back to exactly that; with none, stone.
     */
    public static BlockState full(Level level, BlockPos pos) {
        Direction[] order = {Direction.DOWN, Direction.UP, Direction.WEST, Direction.EAST, Direction.NORTH,
                Direction.SOUTH};
        BlockState[] looks = new BlockState[6];
        List<BlockState> present = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            looks[i] = lookOf(level, pos.relative(order[i]));
            if (looks[i] != null) {
                present.add(looks[i]);
            }
        }
        if (present.size() == 1) {
            return present.get(0);
        }
        BlockState vertical = pair(looks[0], looks[1]);
        BlockState westEast = pair(looks[2], looks[3]);
        BlockState northSouth = pair(looks[4], looks[5]);
        BlockState result = null;
        if (vertical != null && westEast != null && vertical.is(westEast.getBlock())) {
            result = westEast;
        } else if (vertical != null && northSouth != null && vertical.is(northSouth.getBlock())) {
            result = vertical;
        } else if (northSouth != null && westEast != null && northSouth.is(westEast.getBlock())) {
            result = westEast;
        } else if (vertical != null) {
            result = vertical;
        } else if (westEast != null) {
            result = westEast;
        } else if (northSouth != null) {
            result = northSouth;
        }
        if (result != null) {
            return result;
        }
        return present.isEmpty() ? Blocks.STONE.defaultBlockState() : present.get(0);
    }

    private static BlockState pair(BlockState a, BlockState b) {
        return a != null && b != null && a.is(b.getBlock()) ? a : null;
    }

    /**
     * One-way glass: the four neighbours around the camouflaged face's axis ({@code getOtherProperties}); a block
     * that appears twice wins, else the first; with none, stone.
     */
    public static BlockState oneWay(Level level, BlockPos pos, Direction facing) {
        List<Direction> sides = new ArrayList<>();
        switch (facing.getAxis()) {
            case Y -> sides.addAll(List.of(Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH));
            case Z -> sides.addAll(List.of(Direction.UP, Direction.DOWN, Direction.EAST, Direction.WEST));
            case X -> sides.addAll(List.of(Direction.UP, Direction.DOWN, Direction.SOUTH, Direction.NORTH));
        }
        List<BlockState> looks = new ArrayList<>();
        for (Direction side : sides) {
            BlockState look = lookOf(level, pos.relative(side));
            if (look != null) {
                looks.add(look);
            }
        }
        for (int i = 0; i < looks.size(); i++) {
            for (int j = i + 1; j < looks.size(); j++) {
                if (looks.get(i).is(looks.get(j).getBlock())) {
                    return looks.get(i);
                }
            }
        }
        return looks.isEmpty() ? Blocks.STONE.defaultBlockState() : looks.get(0);
    }
}
