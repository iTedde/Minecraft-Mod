package dev.craft.mining;

import java.util.Comparator;
import java.util.Optional;

import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiRecord;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Finds or builds the exit mining portal (spec 18 §4.3). Search radius 16 in both directions; a new 2×3 portal goes to
 * the closest free spot like the vanilla portal forcer (on the flat surface in the mining dimension), otherwise onto
 * a platform like the nether portal.
 */
public final class MiningPortalForcer {
    private static final int SEARCH_RADIUS = 16;

    private MiningPortalForcer() {
    }

    public static Optional<BlockPos> findClosestPortal(ServerLevel level, BlockPos pos) {
        PoiManager poi = level.getPoiManager();
        poi.ensureLoadedAndValid(level, pos, SEARCH_RADIUS);
        WorldBorder border = level.getWorldBorder();
        return poi.getInSquare(type -> type.is(MiningBlocks.MINING_PORTAL_POI.getKey()), pos, SEARCH_RADIUS,
                        PoiManager.Occupancy.ANY)
                .map(PoiRecord::getPos)
                .filter(border::isWithinBounds)
                .filter(p -> level.getBlockState(p).hasProperty(NetherPortalBlock.AXIS))
                .min(Comparator.<BlockPos>comparingDouble(p -> p.distSqr(pos)).thenComparingInt(BlockPos::getY));
    }

    public static Optional<BlockUtil.FoundRectangle> createPortal(ServerLevel level, BlockPos pos, Direction.Axis axis) {
        Direction along = Direction.get(Direction.AxisDirection.POSITIVE, axis);
        BlockPos corner = findFreeSpot(level, pos, along);
        if (corner == null) {
            int top = Math.min(level.getMaxBuildHeight(), level.getMinBuildHeight() + level.getLogicalHeight()) - 1;
            int minY = Math.max(level.getMinBuildHeight() + 1, 70);
            int maxY = top - 9;
            if (maxY < minY) {
                return Optional.empty();
            }
            corner = level.getWorldBorder().clampToBounds(new BlockPos(pos.getX() - along.getStepX(),
                    Mth.clamp(pos.getY(), minY, maxY), pos.getZ() - along.getStepZ()));
            buildPlatform(level, corner, along);
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int a = -1; a < 3; a++) {
            for (int y = -1; y < 4; y++) {
                if (a == -1 || a == 2 || y == -1 || y == 3) {
                    level.setBlock(cursor.setWithOffset(corner, a * along.getStepX(), y, a * along.getStepZ()),
                            Blocks.SMOOTH_STONE.defaultBlockState(), 3);
                }
            }
        }
        BlockState portal = MiningBlocks.MINING_PORTAL.get().defaultBlockState().setValue(NetherPortalBlock.AXIS, axis);
        for (int a = 0; a < 2; a++) {
            for (int y = 0; y < 3; y++) {
                level.setBlock(cursor.setWithOffset(corner, a * along.getStepX(), y, a * along.getStepZ()), portal, 18);
            }
        }
        return Optional.of(new BlockUtil.FoundRectangle(corner.immutable(), 2, 3));
    }

    /**
     * Closest spot within 16 blocks where the frame stands on solid ground in free space, preferring spots with free
     * space on both sides (vanilla {@code PortalForcer.createPortal}); null if there is none.
     */
    private static BlockPos findFreeSpot(ServerLevel level, BlockPos pos, Direction along) {
        WorldBorder border = level.getWorldBorder();
        int top = Math.min(level.getMaxBuildHeight(), level.getMinBuildHeight() + level.getLogicalHeight()) - 1;
        BlockPos best = null;
        double bestDistance = -1;
        BlockPos fallback = null;
        double fallbackDistance = -1;
        BlockPos.MutableBlockPos scratch = new BlockPos.MutableBlockPos();
        for (BlockPos.MutableBlockPos column : BlockPos.spiralAround(pos, SEARCH_RADIUS, Direction.EAST, Direction.SOUTH)) {
            int height = Math.min(top, level.getHeight(Heightmap.Types.MOTION_BLOCKING, column.getX(), column.getZ()));
            if (!border.isWithinBounds(column) || !border.isWithinBounds(column.move(along, 1))) {
                continue;
            }
            column.move(along.getOpposite(), 1);
            for (int y = height; y >= level.getMinBuildHeight(); y--) {
                column.setY(y);
                if (!isReplaceable(level, column)) {
                    continue;
                }
                int airTop = y;
                while (y > level.getMinBuildHeight() && isReplaceable(level, column.move(Direction.DOWN))) {
                    y--;
                }
                int gap = airTop - y;
                if (y + 4 > top || (gap > 0 && gap < 3)) {
                    continue;
                }
                column.setY(y);
                if (!canHostFrame(level, column, scratch, along, 0)) {
                    continue;
                }
                double distance = pos.distSqr(column);
                if (canHostFrame(level, column, scratch, along, -1) && canHostFrame(level, column, scratch, along, 1)
                        && (bestDistance == -1 || bestDistance > distance)) {
                    bestDistance = distance;
                    best = column.immutable();
                }
                if (bestDistance == -1 && (fallbackDistance == -1 || fallbackDistance > distance)) {
                    fallbackDistance = distance;
                    fallback = column.immutable();
                }
            }
        }
        return best != null ? best : fallback;
    }

    private static boolean isReplaceable(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.canBeReplaced() && state.getFluidState().isEmpty();
    }

    private static boolean canHostFrame(ServerLevel level, BlockPos origin, BlockPos.MutableBlockPos pos,
                                        Direction along, int sideOffset) {
        Direction side = along.getClockWise();
        for (int a = -1; a < 3; a++) {
            for (int y = -1; y < 4; y++) {
                pos.setWithOffset(origin, along.getStepX() * a + side.getStepX() * sideOffset, y,
                        along.getStepZ() * a + side.getStepZ() * sideOffset);
                if (y < 0 ? !level.getBlockState(pos).isSolid() : !isReplaceable(level, pos)) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Fallback: 3×2 platform under the portal with air above, like the nether portal. */
    private static void buildPlatform(ServerLevel level, BlockPos corner, Direction along) {
        Direction side = along.getClockWise();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int s = -1; s < 2; s++) {
            for (int a = 0; a < 2; a++) {
                for (int y = -1; y < 3; y++) {
                    pos.setWithOffset(corner, a * along.getStepX() + s * side.getStepX(), y,
                            a * along.getStepZ() + s * side.getStepZ());
                    level.setBlockAndUpdate(pos, y < 0 ? Blocks.SMOOTH_STONE.defaultBlockState()
                            : Blocks.AIR.defaultBlockState());
                }
            }
        }
    }
}
