package dev.craft.thermalexpansion.energy;

import java.util.List;

import dev.craft.thermalexpansion.TeDataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

/**
 * {@code ItemBlockEnergyCell}: stack size 1, rarity uncommon; the 1.4.7 damage (151 − charge · 150 / 600 000) is the
 * charge bar. Tooltip: charge, send/receive, redstone mode.
 */
public class EnergyCellItem extends BlockItem {
    /** {@code setMaxDamage(151)}. */
    private static final int MAX_DAMAGE = 151;

    public EnergyCellItem(Block block, Properties properties) {
        super(block, properties);
    }

    private static EnergyCellData data(ItemStack stack) {
        return stack.getOrDefault(TeDataComponents.ENERGY_CELL.get(), EnergyCellData.DEFAULT);
    }

    private static int damage(ItemStack stack) {
        return MAX_DAMAGE - data(stack).scaledEnergy(150);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F - damage(stack) * 13.0F / MAX_DAMAGE);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        float health = Math.max(0.0F, (MAX_DAMAGE - damage(stack)) / (float) MAX_DAMAGE);
        return Mth.hsvToRgb(health / 3.0F, 1.0F, 1.0F);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        EnergyCellData data = data(stack);
        tooltip.add(Component.translatable("tooltip.craft.te.cell.charge", (int) data.energy()));
        tooltip.add(Component.translatable("tooltip.craft.te.cell.send_receive", data.send(), data.receive()));
        String signal = data.redstoneDisable() ? "tooltip.craft.te.cell.signal_disabled"
                : data.redstoneState() ? "tooltip.craft.te.cell.signal_high" : "tooltip.craft.te.cell.signal_low";
        tooltip.add(Component.translatable(signal));
    }
}
