package dev.craft.thermalexpansion.item;

import java.util.List;

import dev.craft.thermalexpansion.TeDataComponents;
import dev.craft.thermalexpansion.block.PortableTankBlockEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

/** {@code ItemBlockTank}: stack size 1, the tooltip shows liquid and level. */
public class PortableTankItem extends BlockItem {
    public PortableTankItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        SimpleFluidContent content = stack.getOrDefault(TeDataComponents.TANK_LIQUID.get(), SimpleFluidContent.EMPTY);
        Component liquid = content.isEmpty() ? Component.translatable("tooltip.craft.te.tank.empty")
                : content.copy().getHoverName();
        tooltip.add(Component.translatable("tooltip.craft.te.tank.liquid", liquid));
        tooltip.add(Component.translatable("tooltip.craft.te.tank.level", content.getAmount(),
                PortableTankBlockEntity.MAX_LIQUID));
    }
}
