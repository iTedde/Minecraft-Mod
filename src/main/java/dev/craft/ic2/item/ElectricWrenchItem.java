package dev.craft.ic2.item;

import dev.craft.ic2.Ic2DataComponents;
import dev.craft.ic2.keyboard.Ic2Keyboard;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Electric wrench (IC2 {@code ItemToolWrenchElectric}): a wrench whose durability points cost 50 EU each. Mode key +
 * right click toggles the lossless mode, which always drops the original block for 200 points.
 */
public class ElectricWrenchItem extends WrenchItem implements ElectricItem {
    public static final int EU_PER_DAMAGE = 50;

    public ElectricWrenchItem(Properties properties) {
        super(properties.setNoRepair(), false);
    }

    @Override
    public int getMaxCharge() {
        return 12000;
    }

    @Override
    public int getTransferLimit() {
        return 250;
    }

    @Override
    public int getTier() {
        return 1;
    }

    @Override
    public boolean canProvideEnergy() {
        return false;
    }

    @Override
    public boolean canTakeDamage(ItemStack stack, int amount) {
        int energy = amount * EU_PER_DAMAGE;
        return ElectricItems.discharge(stack, energy, Integer.MAX_VALUE, true, true) == energy;
    }

    @Override
    protected void damage(ItemStack stack, int amount, Player player, InteractionHand hand) {
        ElectricItems.use(stack, EU_PER_DAMAGE * amount, player);
    }

    @Override
    protected boolean overrideWrenchSuccessRate(ItemStack stack) {
        return stack.getOrDefault(Ic2DataComponents.LOSSLESS.get(), false);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        if (context.getPlayer() != null && Ic2Keyboard.isModeSwitchKeyDown(context.getPlayer())) {
            return InteractionResult.PASS;
        }
        return super.onItemUseFirst(stack, context);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && Ic2Keyboard.isModeSwitchKeyDown(player)) {
            boolean lossless = !overrideWrenchSuccessRate(stack);
            stack.set(Ic2DataComponents.LOSSLESS.get(), lossless);
            player.sendSystemMessage(Component.translatable(lossless
                    ? "message.craft.wrench.lossless_enabled" : "message.craft.wrench.lossless_disabled"));
        }
        return InteractionResultHolder.pass(stack);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return true;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return ElectricItems.barWidth(this, stack);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return ElectricItems.barColor(this, stack);
    }
}
