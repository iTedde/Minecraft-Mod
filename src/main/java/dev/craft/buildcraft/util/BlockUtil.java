package dev.craft.buildcraft.util;

import dev.craft.buildcraft.BcBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;

/** BuildCraft {@code BlockUtil}: which blocks machines may replace and which count as empty. */
public final class BlockUtil {
    private BlockUtil() {
    }

    /** Not bedrock-like (hardness &lt; 0), not lava and not oil. */
    public static boolean canChangeBlock(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return true;
        }
        if (state.getDestroySpeed(level, pos) < 0.0F) {
            return false;
        }
        return !state.is(BcBlocks.OIL.get()) && !state.is(Blocks.LAVA);
    }

    /**
     * BuildCraft {@code softBlocks}: air, every fluid block ({@code BlockFluid}), every plant ({@code IPlantable}:
     * flowers, saplings, crops, stems, grass, mushrooms, sugar cane, cactus, nether wart), snow layers and vines.
     */
    public static boolean isSoftBlock(BlockGetter level, BlockPos pos) {
        return isSoft(level.getBlockState(pos));
    }

    public static boolean isSoft(BlockState state) {
        return state.isAir() || state.getBlock() instanceof LiquidBlock || state.getBlock() instanceof BushBlock
                || state.getBlock() instanceof SugarCaneBlock || state.getBlock() instanceof CactusBlock
                || state.getBlock() instanceof SnowLayerBlock || state.getBlock() instanceof VineBlock;
    }
}
