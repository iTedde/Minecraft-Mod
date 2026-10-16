package dev.craft.ic2.block;

import com.mojang.serialization.MapCodec;
import dev.craft.ic2.Ic2Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Wet construction foam (IC2 {@code BlockFoam}): no collision, replaceable, drops nothing. Each random tick, and
 * {@value #TICK_RATE} ticks after a failed attempt, it hardens into a light gray {@link ConstructionFoamWallBlock} if
 * {@code light · 6 ≥ random(1000)}; sand hardens it at once.
 */
public class ConstructionFoamBlock extends Block {
    public static final MapCodec<ConstructionFoamBlock> CODEC = simpleCodec(ConstructionFoamBlock::new);
    /** IC2 {@code tickRate()}. */
    public static final int TICK_RATE = 500;
    /** Wall color after hardening (light gray). */
    public static final int HARDENED_COLOR = 7;

    public ConstructionFoamBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    /**
     * IC2 {@code canPlaceBlockAt}: air, fire or a liquid – where the sprayer may put foam.
     */
    public static boolean canFoamReplace(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.is(Blocks.FIRE) || !state.getFluidState().isEmpty() && state.canBeReplaced();
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        tick(state, level, pos, random);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (level.getMaxLocalRawBrightness(pos) * 6 >= level.random.nextInt(1000)) {
            harden(level, pos);
        } else {
            level.scheduleTick(pos, this, TICK_RATE);
        }
    }

    public static void harden(Level level, BlockPos pos) {
        level.setBlock(pos, Ic2Blocks.CONSTRUCTION_FOAM_WALL.get().withColor(HARDENED_COLOR), Block.UPDATE_ALL);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!stack.is(Items.SAND)) {
            return super.useItemOn(stack, state, level, pos, player, hand, hit);
        }
        if (!level.isClientSide) {
            harden(level, pos);
            stack.consume(1, player);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return 0;
    }
}
