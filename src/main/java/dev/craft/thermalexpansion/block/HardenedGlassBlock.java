package dev.craft.thermalexpansion.block;

import dev.craft.thermalexpansion.item.CrescentHammerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * {@code BlockHardenedGlass}: blast-proof glass that drops nothing when broken (silk touch works); sneak-right-click with
 * a wrench dismantles it into the player's hands ({@code IDismantleable}).
 */
public class HardenedGlassBlock extends TransparentBlock {
    public HardenedGlassBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.isShiftKeyDown() || !CrescentHammerItem.isWrench(stack)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide()) {
            dismantle(level, pos, state, player);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    /** {@code dismantleBlock}: removes the block and gives it to the player (or drops it). */
    public static void dismantle(Level level, BlockPos pos, BlockState state, Player player) {
        ItemStack drop = new ItemStack(state.getBlock());
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        if (!player.getInventory().add(drop)) {
            Block.popResource(level, pos, drop);
        }
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }
}
