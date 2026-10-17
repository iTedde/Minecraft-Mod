package dev.craft.ic2.brewing;

import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2DataComponents;
import dev.craft.ic2.Ic2Items;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Empty stone mug (IC2 {@code ItemMug}, stack size 1): used on the treetap side of a barrel it draws one litre into a
 * booze mug carrying the barrel's packed value.
 */
public class StoneMugItem extends Item {
    public StoneMugItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockState state = level.getBlockState(context.getClickedPos());
        if (!state.is(Ic2Blocks.BARREL.get())
                || !BarrelBlock.isTappedOn(state, context.getClickedFace())
                || !(level.getBlockEntity(context.getClickedPos()) instanceof BarrelBlockEntity barrel)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        int value = barrel.calculateValue();
        if (!barrel.drainLiquid(1)) {
            return InteractionResult.FAIL;
        }
        ItemStack booze = new ItemStack(Ic2Items.BOOZE_MUG.get());
        booze.set(Ic2DataComponents.BOOZE.get(), value);
        ItemStack mug = context.getItemInHand();
        Player player = context.getPlayer();
        if (mug.getCount() > 1 && player != null) {
            mug.shrink(1);
            if (!player.getInventory().add(booze)) {
                player.drop(booze, false);
            }
        } else if (player != null) {
            player.setItemInHand(context.getHand(), booze);
        }
        return InteractionResult.CONSUME;
    }
}
