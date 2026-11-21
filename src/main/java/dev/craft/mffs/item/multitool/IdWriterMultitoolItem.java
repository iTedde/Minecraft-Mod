package dev.craft.mffs.item.multitool;

import dev.craft.mffs.MffsItems;
import dev.craft.mffs.block.MffsMachineBlockEntity;
import dev.craft.mffs.item.DataLinkCardItem;
import dev.craft.mffs.item.IdCardData;
import dev.craft.mffs.item.PersonalIdCardItem;
import dev.craft.mffs.security.MffsSecurity;
import dev.craft.mffs.security.SecurityRight;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * {@code ItemPersonalIDWriter} (spec 48 §8): right-click in the air writes an ID card for oneself, left-click on a
 * player one for him, right-click on an MFFS machine (right UCS) a data link card. Each costs 1 000 FE and one blank
 * card from the inventory.
 */
public class IdWriterMultitoolItem extends MultitoolItem {
    public static final int COST = 1000;

    public IdWriterMultitoolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            return super.use(level, player, hand);
        }
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            writeIdCard(stack, player, player);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /** {@code onLeftClickEntity}: an ID card for the hit player; the attack is cancelled. */
    @Override
    public boolean onLeftClickEntity(ItemStack stack, Player player, Entity entity) {
        if (!(entity instanceof Player target)) {
            return false;
        }
        if (!player.level().isClientSide()) {
            writeIdCard(stack, player, target);
        }
        return true;
    }

    private void writeIdCard(ItemStack tool, Player player, Player owner) {
        int slot = findBlankCard(player);
        if (slot < 0) {
            player.sendSystemMessage(Component.translatable("message.craft.mffs.multitool.need_blank_card"));
            return;
        }
        if (!consumePower(tool, COST, true)) {
            player.sendSystemMessage(Component.translatable("message.craft.mffs.multitool.not_enough_charge"));
            return;
        }
        consumePower(tool, COST, false);
        ItemStack card = new ItemStack(MffsItems.CARD_PERSONAL_ID.get());
        PersonalIdCardItem.setData(card, IdCardData.of(owner));
        giveCard(player, slot, card);
        player.sendSystemMessage(Component.translatable("message.craft.mffs.multitool.id_card_created"));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null || player.isShiftKeyDown()
                || !(level.getBlockEntity(context.getClickedPos()) instanceof MffsMachineBlockEntity machine)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!MffsSecurity.checkAccess(machine, player, SecurityRight.UCS)) {
            return InteractionResult.FAIL;
        }
        int slot = findBlankCard(player);
        if (slot < 0) {
            return InteractionResult.FAIL;
        }
        if (!consumePower(stack, COST, true)) {
            player.sendSystemMessage(Component.translatable("message.craft.mffs.multitool.not_enough_charge"));
            return InteractionResult.FAIL;
        }
        consumePower(stack, COST, false);
        ItemStack card = new ItemStack(MffsItems.CARD_DATA_LINK.get());
        DataLinkCardItem.link(card, machine);
        giveCard(player, slot, card);
        player.sendSystemMessage(Component.translatable("message.craft.mffs.multitool.data_link_created"));
        return InteractionResult.SUCCESS;
    }

    private static int findBlankCard(Player player) {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) {
            if (inventory.getItem(slot).is(MffsItems.CARD_BLANK.get())) {
                return slot;
            }
        }
        return -1;
    }

    /** The last blank card of the stack becomes the new card; otherwise it goes into the inventory or is dropped. */
    private static void giveCard(Player player, int slot, ItemStack card) {
        Inventory inventory = player.getInventory();
        ItemStack blank = inventory.getItem(slot);
        blank.shrink(1);
        if (blank.isEmpty()) {
            inventory.setItem(slot, card);
        } else if (!inventory.add(card)) {
            player.drop(card, false);
        }
        player.inventoryMenu.broadcastChanges();
    }
}
