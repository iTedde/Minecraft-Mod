package dev.craft.nuclearcontrol.item;

import java.util.List;

import dev.craft.nuclearcontrol.card.CardData;
import dev.craft.nuclearcontrol.card.PanelCard;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** {@code ItemCardBase}: stacks to 1; the tooltip shows the title and {@code x: …, y: …, z: …} of the target. */
public abstract class PanelCardItem extends Item implements PanelCard {
    protected PanelCardItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CardData card = CardData.of(stack);
        BlockPos target = card.getTarget();
        if (target != null) {
            if (!card.getTitle().isEmpty()) {
                tooltip.add(Component.literal(card.getTitle()));
            }
            tooltip.add(Component.literal(String.format("x: %d, y: %d, z: %d", target.getX(), target.getY(),
                    target.getZ())));
        }
    }

    /** {@code (displaySettings & bit) > 0}. */
    protected static boolean shows(int settings, int bit) {
        return (settings & bit) != 0;
    }
}
