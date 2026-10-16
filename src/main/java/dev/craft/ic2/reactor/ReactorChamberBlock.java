package dev.craft.ic2.reactor;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * IC2 {@code BlockReactorChamber}: only placeable next to exactly one reactor; pops off as an item when that no
 * longer holds. Right-click opens the reactor GUI; smoke and flames by reactor heat like the reactor itself.
 */
public class ReactorChamberBlock extends BaseEntityBlock {
    public ReactorChamberBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ReactorChamberBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return countReactors(context.getLevel(), context.getClickedPos()) == 1 ? defaultBlockState() : null;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbor, BlockPos neighborPos,
                                   boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighbor, neighborPos, movedByPiston);
        if (!level.isClientSide() && countReactors(level, pos) != 1) {
            popResource(level, pos, new ItemStack(this));
            level.removeBlock(pos, false);
        }
    }

    /** Number of adjacent nuclear reactors. */
    public static int countReactors(LevelReader level, BlockPos pos) {
        int count = 0;
        for (Direction direction : Direction.values()) {
            if (level.getBlockEntity(pos.relative(direction)) instanceof NuclearReactorBlockEntity) {
                count++;
            }
        }
        return count;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!(level.getBlockEntity(pos) instanceof ReactorChamberBlockEntity chamber)) {
            return InteractionResult.PASS;
        }
        NuclearReactorBlockEntity reactor = chamber.getReactor();
        if (reactor == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(reactor, reactor.getBlockPos());
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (level.getBlockEntity(pos) instanceof ReactorChamberBlockEntity chamber
                && chamber.getReactor() instanceof NuclearReactorBlockEntity reactor) {
            NuclearReactorBlock.puff(level, pos, random, reactor.getHeat());
        }
    }
}
