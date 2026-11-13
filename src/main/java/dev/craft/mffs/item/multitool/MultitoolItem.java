package dev.craft.mffs.item.multitool;

import dev.craft.mffs.MffsItems;
import dev.craft.mffs.energy.ForceEnergyItem;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * {@code ItemMultitool} (spec 48 §8): one tool with modes, each its own item; sneak + right-click (in the air)
 * switches to the next mode with the same charge. Stores 1 000 000 FE, charged in the capacitor at 50 000 per step.
 */
public abstract class MultitoolItem extends Item implements ForceEnergyItem {
    public static final int MAX_POWER = 1000000;

    protected MultitoolItem(Properties properties) {
        super(properties);
    }

    /** Modes in switching order; the debugger is not part of it. */
    private static List<Supplier<? extends MultitoolItem>> modes() {
        return List.of(MffsItems.MULTITOOL_WRENCH, MffsItems.MULTITOOL_SWITCH, MffsItems.MULTITOOL_ID_WRITER,
                MffsItems.MULTITOOL_FIELD_TELEPORTER, MffsItems.MULTITOOL_GUIDE);
    }

    @Override
    public int getMaximumPower(ItemStack stack) {
        return MAX_POWER;
    }

    @Override
    public int getPowerTransferRate() {
        return 50000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) {
            return InteractionResultHolder.pass(stack);
        }
        List<Supplier<? extends MultitoolItem>> modes = modes();
        int next = 0;
        for (int i = 0; i < modes.size(); i++) {
            if (modes.get(i).get() == this) {
                next = (i + 1) % modes.size();
            }
        }
        MultitoolItem nextMode = modes.get(next).get();
        ItemStack switched = new ItemStack(nextMode);
        nextMode.chargeItem(switched, getAvailablePower(stack), false);
        return InteractionResultHolder.sidedSuccess(switched, level.isClientSide());
    }

    /** Sends the original's "[MultiTool] Fail: not enough …" message. */
    protected static void notEnough(Player player, String key, int amount) {
        player.sendSystemMessage(Component.translatable(key, amount));
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return ForceEnergyItem.barWidth(getAvailablePower(stack), MAX_POWER);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return 0x55FF55;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.craft.mffs.force_energy", getAvailablePower(stack), MAX_POWER));
    }

    /** Creative tab: fully charged ({@code getSubItems}). */
    public ItemStack charged() {
        ItemStack stack = new ItemStack(this);
        setAvailablePower(stack, MAX_POWER);
        return stack;
    }
}
