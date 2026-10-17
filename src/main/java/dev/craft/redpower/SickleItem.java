package dev.craft.redpower;

import java.util.Set;

import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DiggerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * RedPower {@code ItemSickle} (spec 19 §3): fast on leaves; breaking leaves also breaks the leaves in the 3×3×3 cube
 * around, breaking anything else cuts grass, ferns and dead bushes in the 5×5 square of its layer. One point of
 * durability per use that removed something; mining other blocks costs nothing.
 */
public class SickleItem extends DiggerItem {
    public static final int ENCHANTABILITY = 20;
    private static final int LEAF_RADIUS = 1;
    private static final int CROP_RADIUS = 2;
    /**
     * 1.4.7 cuts every {@code BlockFlower} whose material is not the lily pad's ({@code Material.plants}): only tall
     * grass/ferns and dead bushes ({@code Material.vine}). Flowers, saplings, crops and mushrooms stay.
     */
    private static final Set<Block> CUT = Set.of(Blocks.SHORT_GRASS, Blocks.FERN, Blocks.DEAD_BUSH);

    public SickleItem(Tier tier, Properties properties) {
        super(tier, BlockTags.LEAVES, properties);
    }

    @Override
    public int getEnchantmentValue() {
        return ENCHANTABILITY;
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity entity) {
        if (level.isClientSide || !(entity instanceof Player player)) {
            return false;
        }
        boolean used = state.is(BlockTags.LEAVES)
                ? cut(level, player, stack, BlockPos.betweenClosed(pos.offset(-LEAF_RADIUS, -LEAF_RADIUS, -LEAF_RADIUS),
                        pos.offset(LEAF_RADIUS, LEAF_RADIUS, LEAF_RADIUS)), true)
                : cut(level, player, stack, BlockPos.betweenClosed(pos.offset(-CROP_RADIUS, 0, -CROP_RADIUS),
                        pos.offset(CROP_RADIUS, 0, CROP_RADIUS)), false);
        if (used) {
            stack.hurtAndBreak(1, entity, EquipmentSlot.MAINHAND);
        }
        return used;
    }

    private static boolean cut(Level level, Player player, ItemStack stack, Iterable<BlockPos> area, boolean leaves) {
        boolean used = false;
        for (BlockPos target : area) {
            BlockState state = level.getBlockState(target);
            if (leaves ? !state.is(BlockTags.LEAVES) : !CUT.contains(state.getBlock())) {
                continue;
            }
            BlockPos pos = target.immutable();
            if (state.canHarvestBlock(level, pos, player)) {
                state.getBlock().playerDestroy(level, player, pos, state, level.getBlockEntity(pos), stack);
            }
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            used = true;
        }
        return used;
    }
}
