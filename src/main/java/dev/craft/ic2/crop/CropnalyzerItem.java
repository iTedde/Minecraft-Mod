package dev.craft.ic2.crop;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Cropnalyzer (IC2 {@code ItemCropnalyzer}): right click opens the scan GUI ({@link CropnalyzerMenu}). Right clicking a
 * planted crop with it prints the crop's scan result instead (handled by the crop).
 */
public class CropnalyzerItem extends Item {
    public CropnalyzerItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        // The GUI is bound to a hotbar slot; IC2 only ever uses the held item.
        if (hand != InteractionHand.MAIN_HAND) {
            return InteractionResultHolder.pass(stack);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            int slot = player.getInventory().selected;
            serverPlayer.openMenu(new SimpleMenuProvider((id, inventory, p) -> new CropnalyzerMenu(id, inventory, slot),
                    Component.translatable("item.craft.cropnalyzer")), buf -> buf.writeVarInt(slot));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
