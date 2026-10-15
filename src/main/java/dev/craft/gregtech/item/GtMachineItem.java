package dev.craft.gregtech.item;

import java.util.List;

import dev.craft.gregtech.GtMachine;
import dev.craft.gregtech.machine.GtMachineBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;

/**
 * {@code GT_MetaMachine_Item}: tooltip with description, input voltage, possible upgrades and the upgrades carried
 * in the block entity data of a wrenched machine (placing restores them).
 */
public class GtMachineItem extends BlockItem {
    private final GtMachine machine;

    public GtMachineItem(GtMachineBlock block, Properties properties) {
        super(block, properties);
        this.machine = block.getMachine();
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag flag) {
        lines.add(Component.literal(machine.getDescription()).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("tooltip.craft.gt.max_input", 32).withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("tooltip.craft.gt.possible_upgrades", "O T B M")
                .withStyle(ChatFormatting.GRAY));
        CustomData data = stack.get(DataComponents.BLOCK_ENTITY_DATA);
        if (data == null) {
            return;
        }
        CompoundTag tag = data.copyTag();
        if (tag.getBoolean("mMJConverter")) {
            lines.add(Component.translatable("tooltip.craft.gt.mj_converter").withStyle(ChatFormatting.GRAY));
        }
        upgradeLine(lines, tag, "mOverclockers", "overclockers");
        upgradeLine(lines, tag, "mTransformers", "transformers");
        upgradeLine(lines, tag, "mBatteries", "batteries");
        upgradeLine(lines, tag, "mLiBatteries", "li_batteries");
        upgradeLine(lines, tag, "mRSEnergyCells", "energy_cells");
    }

    private static void upgradeLine(List<Component> lines, CompoundTag tag, String key, String name) {
        int count = tag.getByte(key);
        if (count > 0) {
            lines.add(Component.translatable("tooltip.craft.gt." + name, count).withStyle(ChatFormatting.GRAY));
        }
    }
}
