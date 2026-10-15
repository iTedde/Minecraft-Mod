package dev.craft.ic2.item.tool;

import java.util.List;

import dev.craft.Craft;
import dev.craft.ic2.item.ElectricItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.stats.Stats;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.IShearable;

/**
 * Chainsaw (IC2 {@code ItemElectricToolChainsaw}): wood material and its block list; a hit costs 100 EU for 9
 * extra damage, and it shears blocks and entities for 100 EU each.
 */
public class ChainsawItem extends ElectricToolItem {
    public static final int OPERATION_COST = 100;
    public static final float EXTRA_DAMAGE = 9.0F;
    public static final TagKey<Block> MINEABLE = mineableTag("chainsaw");
    /** Blocks that were Forge {@code IShearable} in 1.4.7 (leaves, tall grass, vines, dead bush). */
    public static final TagKey<Block> SHEARABLE =
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(Craft.MODID, "chainsaw_shearable"));

    public ChainsawItem(Properties properties) {
        super(properties, 10000, 100, 1, OPERATION_COST, 12.0F, MINEABLE);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player && ElectricItems.use(stack, OPERATION_COST, player)) {
            target.hurt(player.damageSources().playerAttack(player), EXTRA_DAMAGE);
        }
        return false;
    }

    @Override
    protected void onBlockStartBreak(ItemStack stack, Level level, BlockPos pos, BlockState state, Player player) {
        if (state.is(SHEARABLE) && ElectricItems.use(stack, OPERATION_COST, player)) {
            RandomSource random = level.random;
            float spread = 0.7F;
            double x = random.nextFloat() * spread + (1.0F - spread) * 0.5;
            double y = random.nextFloat() * spread + (1.0F - spread) * 0.5;
            double z = random.nextFloat() * spread + (1.0F - spread) * 0.5;
            ItemEntity item = new ItemEntity(level, pos.getX() + x, pos.getY() + y, pos.getZ() + z,
                    new ItemStack(state.getBlock()));
            item.setPickUpDelay(10);
            level.addFreshEntity(item);
            player.awardStat(Stats.BLOCK_MINED.get(state.getBlock()));
        }
    }

    /** Energy is used before the shearable check, so already shorn sheep cost 100 EU as in IC2. */
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player player, LivingEntity target,
                                                  InteractionHand hand) {
        Level level = target.level();
        if (level.isClientSide() || !(target instanceof IShearable shearable)
                || !ElectricItems.use(stack, OPERATION_COST, player)) {
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

    /** Whether {@code entity} holds a chainsaw as selected item (IC2 plays the idle loop then). */
    public static boolean isEquipped(Entity entity) {
        return entity instanceof LivingEntity living && living.getMainHandItem().getItem() instanceof ChainsawItem;
    }
}
