package dev.craft.enderstorage;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * EnderStorage {@code ItemEnderPouch} (spec 27 §4): opens the item storage of its frequency anywhere; sneaking on an
 * ender chest takes over the chest's frequency and owner.
 */
public class EnderPouchItem extends Item {
    public EnderPouchItem(Properties properties) {
        super(properties.stacksTo(1).component(EsDataComponents.FREQUENCY.get(), Frequency.GLOBAL));
    }

    /** {@code onItemUseFirst}: sneaking on an ender chest (not the tank) copies its frequency. */
    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isSecondaryUseActive()
                || !(context.getLevel().getBlockEntity(context.getClickedPos()) instanceof EnderChestBlockEntity chest)) {
            return InteractionResult.PASS;
        }
        if (!context.getLevel().isClientSide()) {
            stack.set(EsDataComponents.FREQUENCY.get(), chest.getFrequency());
        }
        return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
    }

    /** {@code onItemRightClick}: not sneaking → the storage, title "Ender Pouch". */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isSecondaryUseActive()) {
            return InteractionResultHolder.pass(stack);
        }
        if (level instanceof ServerLevel serverLevel) {
            Frequency frequency = stack.getOrDefault(EsDataComponents.FREQUENCY.get(), Frequency.GLOBAL);
            EnderChestBlock.openStorage(player, EnderStorageData.get(serverLevel.getServer()).item(frequency),
                    frequency, Component.translatable("container.craft.ender_pouch"));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        EnderStorageItem.appendFrequency(stack, tooltip);
    }
}
