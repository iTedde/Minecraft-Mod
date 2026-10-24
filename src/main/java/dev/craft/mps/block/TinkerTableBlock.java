package dev.craft.mps.block;

import com.mojang.serialization.MapCodec;
import dev.craft.mps.client.TinkerScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * MPS {@code BlockTinkerTable} (spec 29 §4): right-click opens the tinker GUI on the client, the server only handles
 * the install, salvage and slider requests; sneaking places blocks as usual.
 */
public class TinkerTableBlock extends BaseEntityBlock {
    public static final MapCodec<TinkerTableBlock> CODEC = simpleCodec(TinkerTableBlock::new);

    public TinkerTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            TinkerScreen.open();
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TinkerTableBlockEntity(pos, state);
    }
}
