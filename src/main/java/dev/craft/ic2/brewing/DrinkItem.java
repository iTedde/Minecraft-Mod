package dev.craft.ic2.brewing;

import dev.craft.ic2.Ic2Items;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/**
 * Filled mug (IC2 {@code ItemMugCoffee}, {@code ItemBooze}): stack size 1, always drinkable (32 ticks), applies its
 * effects on the server and turns into an empty stone mug, in creative mode too.
 */
public abstract class DrinkItem extends Item {
    protected DrinkItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 32;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof Player player) {
            drink(stack, level, player);
        }
        return new ItemStack(Ic2Items.STONE_MUG.get());
    }

    protected abstract void drink(ItemStack stack, Level level, Player player);
}
