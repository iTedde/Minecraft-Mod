package dev.craft.mffs.item.multitool;

import dev.craft.mffs.block.MffsMachineBlockEntity;
import dev.craft.mffs.security.MffsSecurity;
import dev.craft.mffs.security.SecurityRight;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/** {@code ItemSwitch} (spec 48 §8): toggles the switch value of a machine in switch mode 2 for 1 000 FE. */
public class SwitchMultitoolItem extends MultitoolItem {
    public static final int COST = 1000;

    public SwitchMultitoolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (level.isClientSide() || player == null
                || !(level.getBlockEntity(context.getClickedPos()) instanceof MffsMachineBlockEntity machine)
                || !MffsSecurity.isAccessGranted(machine, player, SecurityRight.EB)) {
            return InteractionResult.PASS;
        }
        if (!machine.isSwitchable()) {
            player.sendSystemMessage(Component.translatable("message.craft.mffs.multitool.not_switchable"));
            return InteractionResult.PASS;
        }
        if (!consumePower(stack, COST, true)) {
            player.sendSystemMessage(Component.translatable("message.craft.mffs.multitool.not_enough_charge"));
            return InteractionResult.PASS;
        }
        consumePower(stack, COST, false);
        machine.toggleSwitchValue();
        return InteractionResult.SUCCESS;
    }
}
