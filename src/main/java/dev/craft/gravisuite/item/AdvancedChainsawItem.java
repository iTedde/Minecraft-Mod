package dev.craft.gravisuite.item;

import java.util.List;

import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.item.tool.ChainsawItem;
import dev.craft.ic2.item.tool.ElectricToolItem;
import net.minecraft.core.BlockPos;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.IShearable;

/**
 * GraviSuite {@code ItemAdvChainsaw} (spec 28 §9.2): 15 000 EU, transfer 500, tier 2, efficiency 35, 100 EU per
 * block; the chainsaw's blocks plus wool, pumpkin, melon, cactus and snow layer. Hitting and shearing cost
 * 2 × 100 EU, no sounds.
 */
public class AdvancedChainsawItem extends ElectricToolItem {
    public static final int OPERATION_COST = 100;
    public static final float HIT_DAMAGE = 13.0F;
    public static final TagKey<Block> MINEABLE = mineableTag("advanced_chainsaw");

    public AdvancedChainsawItem(Properties properties) {
        super(properties, 15000, 500, 2, OPERATION_COST, 35.0F, MINEABLE);
    }

    /** Two successful uses of 100 EU deal 13 (the vanilla hit's 1 is part of it), otherwise 1. */
    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player) {
            boolean powered = ElectricItems.use(stack, OPERATION_COST, player)
                    && ElectricItems.use(stack, OPERATION_COST, player);
            target.hurt(player.damageSources().playerAttack(player), powered ? HIT_DAMAGE : 1.0F);
        }
        return false;
    }

    /** Shears without a player, so the two 100 EU uses are not recharged from armor. */
    @Override
    protected void onBlockStartBreak(ItemStack stack, Level level, BlockPos pos, BlockState state, Player player) {
        if (state.is(ChainsawItem.SHEARABLE) && ElectricItems.use(stack, OPERATION_COST, null)
                && ElectricItems.use(stack, OPERATION_COST, null)) {
            GsDrops.dropAsEntity(level, pos, new ItemStack(state.getBlock()));
            player.awardStat(Stats.BLOCK_MINED.get(state.getBlock()));
        }
    }

    /** Original error as in IC2: 200 EU are used before the shearable check. */
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target,
                                                  InteractionHand hand) {
        Level level = target.level();
        if (level.isClientSide() || !(target instanceof IShearable shearable)
                || !ElectricItems.use(stack, OPERATION_COST * 2, player)) {
            return InteractionResult.PASS;
        }
        BlockPos pos = target.blockPosition();
        if (shearable.isShearable(player, stack, level, pos)) {
            List<ItemStack> drops = shearable.onSheared(player, stack, level, pos);
            RandomSource random = target.getRandom();
            for (ItemStack drop : drops) {
                ItemEntity item = target.spawnAtLocation(drop, 1.0F);
                if (item != null) {
                    item.setDeltaMovement(item.getDeltaMovement().add(
                            (random.nextFloat() - random.nextFloat()) * 0.1F,
                            random.nextFloat() * 0.05F,
                            (random.nextFloat() - random.nextFloat()) * 0.1F));
                }
            }
        }
        return InteractionResult.SUCCESS;
    }
}
