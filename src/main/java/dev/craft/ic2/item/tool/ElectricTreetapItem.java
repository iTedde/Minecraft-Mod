package dev.craft.ic2.item.tool;

import dev.craft.ic2.block.RubberWoodBlock;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.item.TreetapItem;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** Electric treetap (IC2 {@code ItemTreetapElectric}): the treetap for 50 EU per use on rubber wood. */
public class ElectricTreetapItem extends ElectricToolItem {
    public static final int OPERATION_COST = 50;

    public ElectricTreetapItem(Properties properties) {
        super(properties, 10000, 100, 1, OPERATION_COST, 6.0F, null);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (!(level.getBlockState(context.getClickedPos()).getBlock() instanceof RubberWoodBlock) || player == null) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return ElectricItems.canUse(stack, OPERATION_COST) ? InteractionResult.SUCCESS : InteractionResult.PASS;
        }
        if (!ElectricItems.use(stack, OPERATION_COST, player)) {
            return InteractionResult.PASS;
        }
        TreetapItem.attemptExtract(player, level, context.getClickedPos(), context.getClickedFace());
        return InteractionResult.SUCCESS;
    }
}
