package dev.craft.lightshield;

import java.util.List;

import dev.craft.ic2.item.ElectricItem;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.keyboard.Ic2Keyboard;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentEffectComponents;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

/**
 * Electric light shield (spec 30): a shield that runs on EU instead of durability and lights its surroundings while
 * held. Empty, it cannot block at all.
 */
public class ElectricLightShieldItem extends ShieldItem implements ElectricItem {
    public static final int MAX_CHARGE = 10000;
    public static final int TRANSFER_LIMIT = 100;
    /** EU per durability point a vanilla shield would have lost. */
    public static final int EU_PER_POINT = 100;
    /** Light costs 1 EU every this many ticks (like the luminator). */
    public static final int LIGHT_INTERVAL = 4;
    /** Mending: one XP point repairs two durability points, i.e. 2 × {@link #EU_PER_POINT}. */
    public static final int EU_PER_XP = 2 * EU_PER_POINT;

    public ElectricLightShieldItem(Properties properties) {
        super(properties.stacksTo(1).setNoRepair());
    }

    @Override
    public int getMaxCharge() {
        return MAX_CHARGE;
    }

    @Override
    public int getTransferLimit() {
        return TRANSFER_LIMIT;
    }

    @Override
    public int getTier() {
        return 1;
    }

    @Override
    public boolean canProvideEnergy() {
        return false;
    }

    public static boolean isLightOn(ItemStack stack) {
        return stack.getOrDefault(LsDataComponents.LIGHT_ON.get(), true);
    }

    /** Whether {@code stack} is a switched-on, charged light shield. */
    public static boolean isLit(ItemStack stack) {
        return stack.getItem() instanceof ElectricLightShieldItem && isLightOn(stack)
                && ElectricItems.getCharge(stack) > 0;
    }

    /** Mode key + right click toggles the light; otherwise blocks like a shield, but only with charge. */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (Ic2Keyboard.isModeSwitchKeyDown(player)) {
            if (!level.isClientSide()) {
                boolean on = !isLightOn(stack);
                stack.set(LsDataComponents.LIGHT_ON.get(), on);
                player.sendSystemMessage(Component.translatable(on
                        ? "message.craft.light_shield.on" : "message.craft.light_shield.off"));
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }
        if (ElectricItems.getCharge(stack) <= 0) {
            return InteractionResultHolder.fail(stack);
        }
        return super.use(level, player, hand);
    }

    /** Lowers the shield as soon as it runs empty. */
    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack stack, int remainingUseDuration) {
        if (ElectricItems.getCharge(stack) <= 0) {
            entity.stopUsingItem();
        }
    }

    @Override
    public boolean canPerformAction(ItemStack stack, ItemAbility itemAbility) {
        if (itemAbility == ItemAbilities.SHIELD_BLOCK) {
            return ElectricItems.getCharge(stack) > 0;
        }
        return super.canPerformAction(stack, itemAbility);
    }

    /**
     * Takes the EU for a blocked hit of {@code damage}: like {@code Player.hurtCurrentlyUsedShield}, from 3 damage on
     * {@code 1 + ⌊damage⌋} points (reduced by Unbreaking), none in creative mode. If the charge does not cover it,
     * the hit is still blocked and the shield ends up empty.
     *
     * @return EU taken
     */
    public static int chargeForBlock(ItemStack stack, float damage, ServerLevel level, Player player) {
        if (damage < 3.0F || player.hasInfiniteMaterials()) {
            return 0;
        }
        int points = EnchantmentHelper.processDurabilityChange(level, stack, 1 + Mth.floor(damage));
        ElectricItems.chargeFromArmor(stack, player);
        return ElectricItems.discharge(stack, points * EU_PER_POINT, Integer.MAX_VALUE, true, false);
    }

    /**
     * Mending (spec 30 §5): XP charges a held light shield with Mending before vanilla repairs other items.
     *
     * @return XP left over
     */
    public static int repairWithXp(Player player, int xp) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (xp <= 0) {
                break;
            }
            if (!(stack.getItem() instanceof ElectricLightShieldItem item)
                    || !EnchantmentHelper.has(stack, EnchantmentEffectComponents.REPAIR_WITH_XP)) {
                continue;
            }
            int missing = item.getMaxCharge() - ElectricItems.getCharge(stack);
            if (missing <= 0) {
                continue;
            }
            int used = Math.min(xp, Mth.positiveCeilDiv(missing, EU_PER_XP));
            ElectricItems.charge(stack, Math.min(missing, used * EU_PER_XP), Integer.MAX_VALUE, true, false);
            xp -= used;
        }
        return xp;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return true;
    }

    @Override
    public int getEnchantmentValue() {
        return 10;
    }

    @Override
    public boolean isValidRepairItem(ItemStack stack, ItemStack repairCandidate) {
        return false;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        tooltip.add(Component.translatable(isLightOn(stack)
                ? "tooltip.craft.light_shield.on" : "tooltip.craft.light_shield.off").withStyle(ChatFormatting.GRAY));
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
