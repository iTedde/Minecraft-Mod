package dev.craft.redpower.table;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import dev.craft.redpower.RpDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** RedPower {@code ItemPlan}: named after its result, lists the required items. */
public class PlanItem extends Item {
    public PlanItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        Plan plan = stack.get(RpDataComponents.PLAN);
        return plan == null ? super.getName(stack)
                : Component.translatable(getDescriptionId() + ".named", plan.result().getHoverName());
    }

    /** One line per different item: "{@code n} x {@code name}". */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        Plan plan = stack.get(RpDataComponents.PLAN);
        if (plan == null) {
            return;
        }
        Map<Item, ItemStack> first = new LinkedHashMap<>();
        Map<Item, Integer> counts = new LinkedHashMap<>();
        for (Plan.Requirement requirement : plan.requires()) {
            first.putIfAbsent(requirement.item().getItem(), requirement.item());
            counts.merge(requirement.item().getItem(), 1, Integer::sum);
        }
        counts.forEach((item, count) -> lines.add(Component.literal(count + " x ")
                .append(first.get(item).getHoverName()).withStyle(ChatFormatting.GRAY)));
    }
}
