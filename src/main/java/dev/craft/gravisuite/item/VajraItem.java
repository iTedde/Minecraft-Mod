package dev.craft.gravisuite.item;

import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.item.tool.ElectricToolItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.level.BlockEvent;

/**
 * GraviSuite {@code ItemVajra} (spec 28 §9.3): 1 000 000 EU, transfer 10 000, tier 2; mines every block with drops at
 * efficiency 20 000 for 3 333 EU each, hits for 6 666 EU deal 25. Right-click removes a block with its drops
 * (precision mode).
 */
public class VajraItem extends ElectricToolItem {
    public static final int OPERATION_COST = 3333;
    public static final float EFFICIENCY = 20000.0F;
    public static final float HIT_DAMAGE = 25.0F;

    public VajraItem(Properties properties) {
        super(properties, 1000000, 10000, 2, OPERATION_COST, EFFICIENCY, null);
    }

    @Override
    public float getDestroySpeed(ItemStack stack, BlockState state) {
        return ElectricItems.canUse(stack, OPERATION_COST) ? EFFICIENCY : 0.5F;
    }

    @Override
    public boolean isCorrectToolForDrops(ItemStack stack, BlockState state) {
        return true;
    }

    /** 3 333 EU for every block, also with hardness 0. */
    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miningEntity) {
        if (!level.isClientSide()) {
            if (miningEntity instanceof Player player) {
                ElectricItems.use(stack, OPERATION_COST, player);
            } else {
                ElectricItems.discharge(stack, OPERATION_COST, getTier(), true, false);
            }
        }
        return true;
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (attacker instanceof Player player && ElectricItems.use(stack, OPERATION_COST * 2, player)) {
            target.hurt(player.damageSources().playerAttack(player), HIT_DAMAGE);
        }
        return false;
    }

    /**
     * Precision mode: every block but bedrock (here also no other unbreakable block) is replaced by air, its drops
     * (no tool, luck 0) fall at the block. Fires the break event so protection mods apply.
     */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        if (!canRemove(level, pos, state) || !ElectricItems.canUse(stack, OPERATION_COST)) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel serverLevel && player != null) {
            if (NeoForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos, state, player)).isCanceled()) {
                return InteractionResult.FAIL;
            }
            for (ItemStack drop : Block.getDrops(state, serverLevel, pos, level.getBlockEntity(pos))) {
                GsDrops.dropAsEntity(level, pos, drop);
            }
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            SoundType sound = state.getSoundType(level, pos, player);
            level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, sound.getStepSound(),
                    SoundSource.BLOCKS, (sound.getVolume() + 1.0F) / 2.0F, sound.getPitch() * 0.8F);
            ElectricItems.use(stack, OPERATION_COST, player);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    /**
     * Not bedrock (pack); of the other unbreakable blocks only those the pack could remove (end portal frame, end and
     * nether portal) – barrier, light, command/structure/jigsaw blocks and end gateway are excluded.
     */
    public static boolean canRemove(Level level, BlockPos pos, BlockState state) {
        if (state.isAir() || state.is(Blocks.BEDROCK)) {
            return false;
        }
        return state.getDestroySpeed(level, pos) >= 0.0F || state.is(Blocks.END_PORTAL_FRAME)
                || state.is(Blocks.END_PORTAL) || state.is(Blocks.NETHER_PORTAL);
    }
}
