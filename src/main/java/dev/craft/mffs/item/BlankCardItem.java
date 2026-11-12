package dev.craft.mffs.item;

import dev.craft.mffs.MffsItems;
import dev.craft.mffs.block.CapacitorBlockEntity;
import dev.craft.mffs.security.MffsSecurity;
import dev.craft.mffs.security.SecurityRight;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * {@code ItemCardEmpty} (spec 48 §6.2): right-click on a capacitor (right EB) makes a power link card; the security
 * station link follows in phase 48.4.
 */
public class BlankCardItem extends Item {
    public BlankCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (level.isClientSide() || player == null) {
            return InteractionResult.PASS;
        }
        if (level.getBlockEntity(context.getClickedPos()) instanceof CapacitorBlockEntity capacitor
                && MffsSecurity.isAccessGranted(capacitor, player, SecurityRight.EB)) {
            ItemStack card = new ItemStack(MffsItems.CARD_POWER_LINK.get());
            CardItem.link(card, capacitor, capacitor.getPowerStorageId(), capacitor.getDeviceName());
            giveCard(player, context.getHand(), context.getItemInHand(), card);
            player.sendSystemMessage(Component.translatable("message.craft.mffs.power_link_created"));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    /** The last blank card in hand becomes the new card; otherwise it goes into the inventory or is dropped. */
    static void giveCard(Player player, InteractionHand hand, ItemStack blank, ItemStack card) {
        blank.shrink(1);
        if (blank.isEmpty()) {
            player.setItemInHand(hand, card);
        } else if (!player.getInventory().add(card)) {
            player.drop(card, false);
        }
        player.inventoryMenu.broadcastChanges();
    }
}
