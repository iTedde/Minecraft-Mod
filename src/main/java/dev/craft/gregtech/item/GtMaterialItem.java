package dev.craft.gregtech.item;

import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** {@code GT_MetaItem_*} entry: one tooltip line (chemical formula, or a description for components). */
public class GtMaterialItem extends Item {
    private final Component tooltip;

    public GtMaterialItem(Properties properties, Component tooltip) {
        super(properties);
        this.tooltip = tooltip;
    }

    /** Material with its chemical formula. */
    public static GtMaterialItem formula(Properties properties, String formula) {
        return new GtMaterialItem(properties, Component.literal(formula));
    }

    /** Component with a translated description ({@code tooltip.craft.<name>}). */
    public static GtMaterialItem described(Properties properties, String name) {
        return new GtMaterialItem(properties, Component.translatable("tooltip.craft." + name));
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(tooltip.copy().withStyle(ChatFormatting.GRAY));
    }
}
