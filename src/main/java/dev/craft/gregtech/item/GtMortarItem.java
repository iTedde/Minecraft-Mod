package dev.craft.gregtech.item;

import java.util.List;

import dev.craft.ic2.Ic2Items;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * Iron mortar ({@code GT_Mortar_Item}): stays in the grid with one more damage per craft; at full damage it turns into
 * iron dust instead.
 */
public class GtMortarItem extends Item {
    public GtMortarItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack stack) {
        if (stack.getDamageValue() >= stack.getMaxDamage()) {
            return new ItemStack(Ic2Items.IRON_DUST.get());
        }
        ItemStack used = stack.copyWithCount(1);
        used.setDamageValue(stack.getDamageValue() + 1);
        return used;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("tooltip.craft.iron_mortar").withStyle(ChatFormatting.GRAY));
    }
}
