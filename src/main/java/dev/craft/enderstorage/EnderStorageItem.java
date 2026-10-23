package dev.craft.enderstorage;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

/**
 * EnderStorage {@code ItemEnderStorage}: the chest or the tank with its frequency. Tooltip: the owner as in
 * the original, plus the three colours so it can be told apart from the vanilla ender chest (user, 2026-10-23).
 */
public class EnderStorageItem extends BlockItem {
    public EnderStorageItem(Block block, Properties properties) {
        super(block, properties.component(EsDataComponents.FREQUENCY.get(), Frequency.GLOBAL));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        appendFrequency(stack, tooltip);
    }

    /** Colours left to right, then the owner's name for personal ones. */
    public static void appendFrequency(ItemStack stack, List<Component> tooltip) {
        Frequency frequency = stack.getOrDefault(EsDataComponents.FREQUENCY.get(), Frequency.GLOBAL);
        tooltip.add(Component.translatable("tooltip.craft.ender_frequency",
                        colourName(frequency, 0), colourName(frequency, 1), colourName(frequency, 2))
                .withStyle(ChatFormatting.GRAY));
        if (frequency.isPersonal()) {
            tooltip.add(Component.literal(frequency.ownerName()).withStyle(ChatFormatting.GRAY));
        }
    }

    private static Component colourName(Frequency frequency, int button) {
        return Component.translatable("color.minecraft." + frequency.dyeColour(button).getName());
    }
}
