package dev.craft.ic2.block;

import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * IC2 {@code BlockITNT} on {@code BlockIC2Explosive} (spec 05 §7). Industrial TNT can be broken by hand, so every way
 * of removing it (player, flint and steel, redstone) primes it; it never drops.
 */
public class IndustrialTntBlock extends Block {
    public IndustrialTntBlock(Properties properties) {
        super(properties);
    }

    /** {@code getExplosionEntity}, centred on the block. */
    protected Ic2ExplosiveEntity createEntity(Level level, BlockPos pos) {
        return new IndustrialTntEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    /** IC2 {@code canExplodeByHand}: industrial TNT primes when broken, the nuke drops itself. */
    protected boolean canExplodeByHand() {
        return true;
    }

    /** IC2 {@code onIgnite}: called when the block is primed by a player or redstone (not by explosions). */
    protected void onIgnite(Level level, BlockPos pos, @Nullable Player player) {
    }

    /** {@code removeBlockByPlayer} without a player (redstone): removes the block and primes it. */
    public void ignite(Level level, BlockPos pos) {
        ignite(level, pos, null);
    }

    /** {@code removeBlockByPlayer}: removes the block and primes it with the full fuse. */
    public void ignite(Level level, BlockPos pos, @Nullable Player player) {
        if (level.isClientSide()) {
            return;
        }
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        onIgnite(level, pos, player);
        Ic2ExplosiveEntity entity = createEntity(level, pos);
        level.addFreshEntity(entity);
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.TNT_PRIMED, SoundSource.BLOCKS,
                1.0F, 1.0F);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!oldState.is(state.getBlock()) && level.hasNeighborSignal(pos)) {
            ignite(level, pos);
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   BlockPos neighborPos, boolean movedByPiston) {
        if (neighborBlock.defaultBlockState().isSignalSource() && level.hasNeighborSignal(pos)) {
            ignite(level, pos);
        }
    }

    /**
     * Breaking it (also in creative) primes it, or drops the block if it cannot be set off by hand; returning false
     * skips the loot table.
     */
    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player,
                                       boolean willHarvest, FluidState fluid) {
        if (canExplodeByHand()) {
            ignite(level, pos, player);
        } else if (!level.isClientSide()) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            popResource(level, pos, new ItemStack(this));
        }
        return false;
    }

    /** Flint and steel primes it without taking damage. */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!stack.is(Items.FLINT_AND_STEEL)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
        }
        ignite(level, pos, player);
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    /** {@code onBlockDestroyedByExplosion}: short random fuse, no sound. */
    @Override
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        if (level.isClientSide()) {
            return;
        }
        Ic2ExplosiveEntity entity = createEntity(level, pos);
        int fuse = entity.getFuseLength();
        entity.setFuse(level.random.nextInt(fuse / 4) + fuse / 8);
        level.addFreshEntity(entity);
    }

    @Override
    public boolean dropFromExplosion(Explosion explosion) {
        return false;
    }
}
