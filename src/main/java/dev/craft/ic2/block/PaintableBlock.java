package dev.craft.ic2.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/** IC2 {@code IPaintableBlock}: blocks the painter can color. */
public interface PaintableBlock {
    /**
     * Colors the block at {@code pos}.
     *
     * @param color dye metadata 0 (black) – 15 (white)
     * @return whether the block changed
     */
    boolean colorBlock(Level level, BlockPos pos, int color);
}
