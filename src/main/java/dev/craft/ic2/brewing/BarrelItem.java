package dev.craft.ic2.brewing;

import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.block.ScaffoldBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Booze barrel item (IC2 {@code ItemBarrel}, stack size 1): used on a non-reinforced wooden scaffold it turns the
 * scaffold into a barrel block with the stored contents. The stack shrinks unless in creative mode.
 */
public class BarrelItem extends Item {
    public BarrelItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public Component getName(ItemStack stack) {
        int amount = Booze.getAmount(Booze.getValue(stack));
        return amount > 0 ? Component.translatable(getDescriptionId() + ".filled", amount)
                : Component.translatable(getDescriptionId());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        ScaffoldBlock scaffold = Ic2Blocks.WOODEN_SCAFFOLD.get();
        if (!state.is(scaffold) || state.getValue(ScaffoldBlock.STRENGTH) >= scaffold.getReinforcedStrength()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            level.setBlock(pos, Ic2Blocks.BARREL.get().defaultBlockState(), Block.UPDATE_ALL);
            if (level.getBlockEntity(pos) instanceof BarrelBlockEntity barrel) {
                barrel.set(Booze.getValue(context.getItemInHand()));
            }
            if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
