package dev.craft.nuclearcontrol.item;

import java.util.List;

import dev.craft.nuclearcontrol.NcDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * Sensor location card ({@code ItemCardBase}): holds the target position and shows it as
 * {@code x: …, y: …, z: …}. Phase 46.1 has the reactor card; its display on the information panel follows in 46.2.
 */
public class SensorLocationCardItem extends Item {
    public SensorLocationCardItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        BlockPos target = stack.get(NcDataComponents.SENSOR_TARGET.get());
        if (target != null) {
            tooltip.add(Component.literal(String.format("x: %d, y: %d, z: %d", target.getX(), target.getY(),
                    target.getZ())));
        }
    }
}
