package dev.craft.ic2.item;

import dev.craft.ic2.Ic2Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.piston.PistonBaseBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Sticky resin (IC2 {@code ItemResin}): right-clicking the front of a retracted piston turns it into a sticky
 * piston; right-clicking the top of a block places a resin sheet on it.
 */
public class ResinItem extends Item {
    public ResinItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction side = context.getClickedFace();
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.PISTON) && !state.getValue(PistonBaseBlock.EXTENDED)
                && state.getValue(PistonBaseBlock.FACING) == side) {
            if (!level.isClientSide()) {
                level.setBlock(pos, Blocks.STICKY_PISTON.defaultBlockState().setValue(PistonBaseBlock.FACING, side),
                        Block.UPDATE_ALL);
            }
            consume(context.getPlayer(), context.getItemInHand());
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (side != Direction.UP) {
            return InteractionResult.PASS;
        }
        BlockPos above = pos.above();
        BlockState sheet = Ic2Blocks.RESIN_SHEET.get().defaultBlockState();
        if (!level.isEmptyBlock(above) || !sheet.canSurvive(level, above)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            level.setBlock(above, sheet, Block.UPDATE_ALL);
        }
        consume(context.getPlayer(), context.getItemInHand());
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static void consume(@Nullable Player player, ItemStack stack) {
        if (player == null || !player.getAbilities().instabuild) {
            stack.shrink(1);
        }
    }
}
