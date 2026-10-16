package dev.craft.gregtech.item;

import dev.craft.ic2.item.reactor.HeatStorageItem;
import dev.craft.ic2.reactor.Reactor;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * {@code GT_CoolantCell_Item}: 60k/180k/360k helium coolant cell. Heat handling like the IC2 coolant cell, but only 100
 * damage steps and a positive explosion contribution {@code 1 + capacity / 30000}.
 */
public class GtCoolantCellItem extends HeatStorageItem {
    public GtCoolantCellItem(Properties properties, int heatStorage) {
        super(properties, heatStorage, 100);
    }

    @Override
    public float influenceExplosion(Reactor reactor, ItemStack stack) {
        return 1.0F + heatStorage / 30000.0F;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.translatable("tooltip.craft.stored_heat", getHeatOfStack(stack))
                .withStyle(ChatFormatting.GRAY));
    }
}
