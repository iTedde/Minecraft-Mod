package dev.craft.ic2.item;

import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2DataComponents;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.machine.TeleporterBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * IC2 {@code ItemFrequencyTransmitter}: right-click a teleporter to store it, then another one to link both;
 * right-click into the air unlinks (spec 05 §4).
 */
public class FrequencyTransmitterItem extends Item {
    public FrequencyTransmitterItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && stack.has(Ic2DataComponents.TELEPORTER_TARGET.get())) {
            stack.remove(Ic2DataComponents.TELEPORTER_TARGET.get());
            player.sendSystemMessage(Component.translatable("message.craft.frequency_transmitter.unlinked"));
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        if (!(level.getBlockEntity(context.getClickedPos()) instanceof TeleporterBlockEntity teleporter)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        Player player = context.getPlayer();
        BlockPos target = stack.get(Ic2DataComponents.TELEPORTER_TARGET.get());
        if (target != null && !level.getBlockState(target).is(Ic2Blocks.machine(Ic2Machine.TELEPORTER).get())) {
            target = null;
        }
        BlockPos pos = teleporter.getBlockPos();
        String message;
        if (target == null) {
            target = pos;
            message = "linked";
        } else if (pos.equals(target)) {
            message = "self";
        } else if (target.equals(teleporter.getTarget())) {
            message = "unchanged";
        } else {
            teleporter.setTarget(target);
            if (level.getBlockEntity(target) instanceof TeleporterBlockEntity other && other.getTarget() == null) {
                other.setTarget(pos);
            }
            message = "established";
        }
        stack.set(Ic2DataComponents.TELEPORTER_TARGET.get(), target);
        if (player != null) {
            player.sendSystemMessage(Component.translatable("message.craft.frequency_transmitter." + message));
        }
        return InteractionResult.SUCCESS;
    }
}
