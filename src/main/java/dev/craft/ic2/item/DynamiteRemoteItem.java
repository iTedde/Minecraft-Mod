package dev.craft.ic2.item;

import java.util.ArrayList;
import java.util.List;

import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2DataComponents;
import dev.craft.ic2.Ic2Sounds;
import dev.craft.ic2.block.DynamiteBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * IC2 {@code ItemRemote}, the Dynamite-O-Mote (spec 05 §7.1): right click on a dynamite stick links it, on a linked
 * stick unlinks it; any other right click sets off all linked sticks and clears the list. The positions carry no
 * dimension, as in IC2.
 */
public class DynamiteRemoteItem extends Item {
    public DynamiteRemoteItem(Properties properties) {
        super(properties);
    }

    public static List<BlockPos> getLinks(ItemStack stack) {
        return stack.getOrDefault(Ic2DataComponents.REMOTE_LINKS.get(), List.of());
    }

    private static void setLinks(ItemStack stack, List<BlockPos> links) {
        if (links.isEmpty()) {
            stack.remove(Ic2DataComponents.REMOTE_LINKS.get());
        } else {
            stack.set(Ic2DataComponents.REMOTE_LINKS.get(), List.copyOf(links));
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        boolean stick = state.is(Ic2Blocks.DYNAMITE_STICK.get());
        if (!stick && !state.is(Ic2Blocks.DYNAMITE_STICK_WITH_REMOTE.get())) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ItemStack stack = context.getItemInHand();
        List<BlockPos> links = new ArrayList<>(getLinks(stack));
        if (stick) {
            links.add(pos.immutable());
            level.setBlock(pos, Ic2Blocks.DYNAMITE_STICK_WITH_REMOTE.get().defaultBlockState()
                    .setValue(DynamiteBlock.SUPPORT, state.getValue(DynamiteBlock.SUPPORT)), Block.UPDATE_ALL);
        } else if (links.remove(pos)) {
            level.setBlock(pos, Ic2Blocks.DYNAMITE_STICK.get().defaultBlockState()
                    .setValue(DynamiteBlock.SUPPORT, state.getValue(DynamiteBlock.SUPPORT)), Block.UPDATE_ALL);
        } else if (context.getPlayer() != null) {
            context.getPlayer().sendSystemMessage(Component.translatable("message.craft.dynamite_o_mote.not_linked"));
        }
        setLinks(stack, links);
        return InteractionResult.SUCCESS;
    }

    /** {@code launchRemotes}: sets off every linked stick that is still there (fuse 5). */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            level.playSound(null, player.getX(), player.getY(), player.getZ(), Ic2Sounds.DYNAMITE_O_MOTE.get(),
                    SoundSource.PLAYERS, 1.0F, 1.0F);
            for (BlockPos pos : getLinks(stack)) {
                if (level.getBlockState(pos).is(Ic2Blocks.DYNAMITE_STICK_WITH_REMOTE.get())) {
                    DynamiteBlock.explode(level, pos);
                }
            }
            setLinks(stack, List.of());
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        int count = getLinks(stack).size();
        if (count > 0) {
            tooltip.add(Component.translatable("item.craft.dynamite_o_mote.linked", count));
        }
    }
}
