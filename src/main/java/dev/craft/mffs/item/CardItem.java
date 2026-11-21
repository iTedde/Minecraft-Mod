package dev.craft.mffs.item;

import dev.craft.mffs.MffsDataComponents;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

/**
 * {@code ItemCard} (spec 48 §6.2): a card that links to a device; tooltip "Linkto", world, coordinates and validity.
 * Machines drop such cards as blank cards.
 */
public class CardItem extends Item {
    public CardItem(Properties properties) {
        super(properties);
    }

    public static CardLink getLink(ItemStack stack) {
        return stack.get(MffsDataComponents.CARD_LINK.get());
    }

    public static void setLink(ItemStack stack, CardLink link) {
        stack.set(MffsDataComponents.CARD_LINK.get(), link);
    }

    /** {@code setInformation} + {@code setforArea}: a fresh, valid link to {@code target}. */
    public static void link(ItemStack stack, BlockEntity target, int id, String name) {
        setLink(stack, new CardLink(GlobalPos.of(target.getLevel().dimension(), target.getBlockPos()), id, name,
                true));
    }

    public static boolean isValid(ItemStack stack) {
        CardLink link = getLink(stack);
        return link != null && link.valid();
    }

    /** {@code Functions.setIteminSlot}: put the card in, or copy its data over a blank card lying there. */
    public static boolean installInSlot(ItemStack stack, Player player,
                                        dev.craft.mffs.block.MffsMachineBlockEntity machine, int slot, String card) {
        ItemStack present = machine.getStack(slot);
        if (present.isEmpty()) {
            machine.setStack(slot, stack.copy());
            stack.setCount(0);
            player.sendSystemMessage(Component.translatable("message.craft.mffs.card_installed", card));
            machine.setChanged();
            return true;
        }
        if (present.getItem() instanceof BlankCardItem) {
            machine.setStack(slot, stack.copyWithCount(1));
            player.sendSystemMessage(Component.translatable("message.craft.mffs.card_copied", card));
            machine.setChanged();
            return true;
        }
        player.sendSystemMessage(Component.translatable("message.craft.mffs.slot_not_empty"));
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        CardLink link = getLink(stack);
        tooltip.add(Component.translatable("tooltip.craft.mffs.link_to", link != null ? link.name() : "not set"));
        if (link != null) {
            tooltip.add(Component.translatable("tooltip.craft.mffs.world",
                    link.target().dimension().location().toString()));
            BlockPos pos = link.target().pos();
            tooltip.add(Component.translatable("tooltip.craft.mffs.coords", pos.getX(), pos.getY(), pos.getZ()));
            tooltip.add(link.valid()
                    ? Component.translatable("tooltip.craft.mffs.valid").withStyle(ChatFormatting.DARK_GREEN)
                    : Component.translatable("tooltip.craft.mffs.invalid").withStyle(ChatFormatting.DARK_RED));
        }
    }

    /** Whether the card's target dimension is {@code level}'s. */
    protected static boolean sameDimension(CardLink link, Level level) {
        return link.target().dimension().equals(level.dimension());
    }
}
