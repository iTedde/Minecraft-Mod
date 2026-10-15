package dev.craft.gregtech.item;

import java.util.List;

import dev.craft.ic2.item.BatteryItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * GT {@code GT_EnergyStore_Item} 56/57 (spec 13 §6.4): 100 000 EU, 128 EU/t, tier 1. Like the RE battery the empty
 * item stacks to 16 and the charged one to 1, but a right click does nothing.
 */
public class LithiumBatteryItem extends BatteryItem {
    public LithiumBatteryItem(Properties properties) {
        super(properties, 100000, 128, 1, true);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.literal("Tier: " + getTier()));
    }
}
