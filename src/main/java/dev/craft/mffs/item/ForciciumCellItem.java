package dev.craft.mffs.item;

import dev.craft.mffs.MffsDataComponents;
import dev.craft.mffs.MffsItems;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * {@code ItemForcicumCell} (spec 48 §2): holds up to 1 000 forcicium. Right-click toggles active; an active cell
 * that is not full pulls one forcicium per inventory tick from the player's inventory (first one found).
 */
public class ForciciumCellItem extends Item {
    public static final int MAX_LEVEL = 1000;

    public ForciciumCellItem(Properties properties) {
        super(properties);
    }

    public int getMaxLevel() {
        return MAX_LEVEL;
    }

    public static int getLevel(ItemStack stack) {
        return stack.getOrDefault(MffsDataComponents.FORCICIUM_LEVEL.get(), 0);
    }

    public static void setLevel(ItemStack stack, int level) {
        stack.set(MffsDataComponents.FORCICIUM_LEVEL.get(), level);
    }

    /** {@code useForcecium}. */
    public static boolean useForcicium(ItemStack stack, int count) {
        if (count > getLevel(stack)) {
            return false;
        }
        setLevel(stack, getLevel(stack) - count);
        return true;
    }

    public static boolean isActive(ItemStack stack) {
        return stack.getOrDefault(MffsDataComponents.CELL_ACTIVE.get(), false);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            boolean active = !isActive(stack);
            stack.set(MffsDataComponents.CELL_ACTIVE.get(), active);
            player.sendSystemMessage(Component.translatable(active ? "message.craft.mffs.cell_active"
                    : "message.craft.mffs.cell_inactive"));
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean selected) {
        if (level.isClientSide() || !isActive(stack) || getLevel(stack) >= MAX_LEVEL
                || !(entity instanceof Player player)) {
            return;
        }
        for (Slot slot : player.inventoryMenu.slots) {
            ItemStack found = slot.getItem();
            if (found.is(MffsItems.FORCICIUM.get())) {
                setLevel(stack, getLevel(stack) + 1);
                found.shrink(1);
                slot.setChanged();
                break;
            }
        }
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Math.round(13.0F * getLevel(stack) / MAX_LEVEL);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x55FF55;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.craft.mffs.forcicium_cell", getLevel(stack), MAX_LEVEL));
    }
}
