package dev.craft.mffs.item.multitool;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** {@code ItemManuelBook} (spec 48 §8, §11.6): right-click opens the guide (client only). */
public class GuideMultitoolItem extends MultitoolItem {
    public GuideMultitoolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            return super.use(level, player, hand);
        }
        if (level.isClientSide()) {
            dev.craft.mffs.client.GuideScreen.open();
        }
        return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
    }
}
