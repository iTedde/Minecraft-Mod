package dev.craft.ic2.item.tool;

import dev.craft.ic2.item.ElectricItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Electric hoe (IC2 {@code ItemElectricToolHoe}). Every broken block costs 50 EU extra, and tilling uses the energy
 * before it checks the block – both as in IC2.
 */
public class ElectricHoeItem extends ElectricToolItem {
    public static final int OPERATION_COST = 50;
    public static final TagKey<Block> MINEABLE = mineableTag("electric_hoe");

    public ElectricHoeItem(Properties properties) {
        super(properties, 10000, 100, 1, OPERATION_COST, 16.0F, MINEABLE);
    }

    @Override
    protected void onBlockStartBreak(ItemStack stack, Level level, BlockPos pos, BlockState state, Player player) {
        ElectricItems.use(stack, OPERATION_COST, player);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        ItemStack stack = context.getItemInHand();
        if (player == null || !player.mayUseItemAt(pos, context.getClickedFace(), stack)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return ElectricItems.canUse(stack, OPERATION_COST) && canTill(level, pos, context.getClickedFace())
                    ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (!ElectricItems.use(stack, OPERATION_COST, player) || !canTill(level, pos, context.getClickedFace())) {
            return InteractionResult.PASS;
        }
        SoundType sound = Blocks.FARMLAND.defaultBlockState().getSoundType();
        level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, sound.getStepSound(),
                SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
        level.setBlock(pos, Blocks.FARMLAND.defaultBlockState(), Block.UPDATE_ALL);
        return InteractionResult.SUCCESS;
    }

    /** Grass only from the side or top with air above; dirt always. */
    private static boolean canTill(Level level, BlockPos pos, Direction side) {
        BlockState state = level.getBlockState(pos);
        if (state.is(Blocks.DIRT)) {
            return true;
        }
        return state.is(Blocks.GRASS_BLOCK) && side != Direction.DOWN && level.getBlockState(pos.above()).isAir();
    }
}
