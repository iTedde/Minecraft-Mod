package dev.craft.nuclearcontrol.item;

import java.util.List;
import java.util.function.Supplier;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * GregTech sensor kit ({@code GT_SensorKit_Item}, spec 46 §8): right click on a GregTech machine that gives
 * information → GregTech sensor card, without the chat message of the Nuclear Control kits.
 */
public class GtSensorKitItem extends SensorKitItem {
    public GtSensorKitItem(Properties properties, Supplier<? extends Item> card) {
        super(properties, card, SensorKitItem::gregTech);
    }

    @Override
    protected void announce(Player player) {
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.craft.nuclearcontrol.gt_sensor_kit"));
    }
}
