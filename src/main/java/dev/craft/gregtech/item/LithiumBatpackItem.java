package dev.craft.gregtech.item;

import java.util.List;

import dev.craft.ic2.item.armor.EnergyPackItem;
import dev.craft.ic2.item.armor.Ic2ArmorMaterials;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/**
 * GT {@code GT_EnergyArmor_Item} 58 (spec 13 §6.4): chest storage of 600 000 EU, 128 EU/t, tier 1, no protection and
 * no specials. Worn, it feeds electric tools like any energy providing armor.
 */
public class LithiumBatpackItem extends EnergyPackItem {
    public LithiumBatpackItem(Properties properties) {
        super(Ic2ArmorMaterials.LITHIUM_BATPACK, properties, 600000, 128, 1);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.literal("Tier: " + getTier()));
    }
}
