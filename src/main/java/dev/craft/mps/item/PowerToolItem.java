package dev.craft.mps.item;

import dev.craft.mps.MpsEnergy;
import dev.craft.mps.MpsTool;
import dev.craft.mps.module.ModuleTarget;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * MPS {@code ItemPowerTool} (spec 29 §1, §8): does nothing without modules; attack damage 1, not enchantable, not
 * repairable. Mining speed and harvesting come from {@link MpsTool}'s events, right click runs the selected mode.
 */
public class PowerToolItem extends Item implements ModularItem {
    public PowerToolItem(Properties properties) {
        super(properties.stacksTo(1));
    }

    @Override
    public ModuleTarget getTarget() {
        return ModuleTarget.TOOL;
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            MpsTool.meleeAssist(stack, target, player);
        }
        return true;
    }

    /** Every mined block costs its type's energy, also blocks without hardness. */
    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        if (!level.isClientSide()) {
            double drain = MpsTool.miningCost(stack, state, miner);
            if (miner instanceof Player player) {
                MpsEnergy.drainPlayerEnergy(player, drain);
            } else {
                MpsEnergy.discharge(stack, drain);
            }
        }
        return true;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        return MpsTool.rightClick(player, level, stack, hand) ? InteractionResultHolder.consume(stack)
                : InteractionResultHolder.pass(stack);
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return MpsTool.USE_DURATION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.BOW;
    }

    /** Plasma Cannon fires on release. */
    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity entity, int timeLeft) {
        if (entity instanceof Player player) {
            MpsTool.releasePlasma(player, level, stack, timeLeft);
        }
    }

    /** Energy changes must neither restart the swing animation nor reset the mining progress. */
    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged || !newStack.is(oldStack.getItem());
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack) {
        return !newStack.is(oldStack.getItem());
    }
}
