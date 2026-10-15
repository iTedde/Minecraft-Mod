package dev.craft.buildcraft.factory;

import dev.craft.buildcraft.BcBlocks;
import dev.craft.buildcraft.util.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * The quarry's frame blueprint, 1:1 with BuildCraft {@code BptBuilderBlueprint} for a blueprint of frames on the 12
 * edges and air everywhere else. The clear list runs top-down, the build list bottom-up (both x, then z); frames are
 * not opaque, so the primary list stays empty. No slot has requirements, so every list is a plain cursor over its
 * fixed order.
 */
final class FrameBlueprint {
    /** A blueprint position; {@code clear} = "ClearIfInvalid" mode, otherwise "Build". */
    record Slot(BlockPos pos, boolean frame, boolean clear) {
        boolean isValid(Level level) {
            BlockState state = level.getBlockState(pos);
            return frame ? state.is(BcBlocks.FRAME.get()) && !state.getValue(FrameBlock.DECAYING) : state.isAir();
        }
    }

    private final int x;
    private final int y;
    private final int z;
    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;
    private final int total;
    private int clearCursor;
    private int buildCursor;
    private boolean done;

    FrameBlueprint(int x, int y, int z, int sizeX, int sizeY, int sizeZ) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.total = sizeX * sizeY * sizeZ;
    }

    boolean isDone() {
        return done;
    }

    private boolean isFrame(int i, int j, int k) {
        boolean edgeX = i == 0 || i == sizeX - 1;
        boolean edgeZ = k == 0 || k == sizeZ - 1;
        return (j == 0 || j == sizeY - 1) && (edgeX || edgeZ) || edgeX && edgeZ;
    }

    private Slot slot(int index, boolean clear) {
        int k = index % sizeZ;
        int i = index / sizeZ % sizeX;
        int layer = index / (sizeZ * sizeX);
        int j = clear ? sizeY - 1 - layer : layer;
        return new Slot(new BlockPos(x + i, y + j, z + k), isFrame(i, j, k), clear);
    }

    /** BuildCraft {@code getNextBlock}: the next invalid slot, or {@code null}; updates {@link #isDone()}. */
    @Nullable
    Slot getNextBlock(Level level) {
        Slot result = null;
        if (clearCursor < total) {
            while (clearCursor < total) {
                Slot slot = slot(clearCursor++, true);
                if (!slot.isValid(level) && !BlockUtil.isSoftBlock(level, slot.pos())) {
                    result = slot;
                    break;
                }
            }
        } else {
            while (buildCursor < total) {
                Slot slot = slot(buildCursor++, false);
                if (!slot.isValid(level)) {
                    result = slot;
                    break;
                }
            }
        }
        done = clearCursor >= total && buildCursor >= total;
        return result;
    }

    /** {@code BptBlock.buildBlock}: places the frame or air of a "Build" slot. */
    static void build(Level level, Slot slot) {
        FrameBlock frame = BcBlocks.FRAME.get();
        level.setBlock(slot.pos(), slot.frame() ? frame.connectedState(level, slot.pos(), false)
                : Blocks.AIR.defaultBlockState(), 3);
    }
}
