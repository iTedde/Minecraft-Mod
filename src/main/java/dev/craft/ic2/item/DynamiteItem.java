package dev.craft.ic2.item;

import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.block.DynamiteEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * IC2 {@code ItemDynamite} (spec 05 §7.1): right click throws it; plain dynamite used on a block places a dynamite
 * stick on that face instead (and always takes one, also in creative). Dispensers shoot it like other projectiles.
 */
public class DynamiteItem extends Item implements ProjectileItem {
    private final boolean sticky;

    public DynamiteItem(Properties properties, boolean sticky) {
        super(properties);
        this.sticky = sticky;
    }

    /** Never throws when a block is clicked: places the stick there if the spot is air and has a solid neighbour. */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (sticky) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        BlockState stick = Ic2Blocks.DYNAMITE_STICK.get().defaultBlockState();
        if (level.getBlockState(pos).isAir() && stick.canSurvive(level, pos)) {
            if (!level.isClientSide()) {
                level.setBlock(pos, stick, Block.UPDATE_ALL);
            }
            context.getItemInHand().shrink(1);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        stack.consume(1, player);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ARROW_SHOOT,
                SoundSource.PLAYERS, 0.5F, 0.4F / (level.random.nextFloat() * 0.4F + 0.8F));
        if (!level.isClientSide()) {
            level.addFreshEntity(new DynamiteEntity(level, player, sticky));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        return new DynamiteEntity(level, pos.x(), pos.y(), pos.z(), sticky);
    }

    /** Minecraft 1.4.7 dispensers aimed 0.1 upwards. */
    @Override
    public void shoot(Projectile projectile, double x, double y, double z, float velocity, float inaccuracy) {
        projectile.shoot(x, y + 0.1, z, velocity, inaccuracy);
    }
}
