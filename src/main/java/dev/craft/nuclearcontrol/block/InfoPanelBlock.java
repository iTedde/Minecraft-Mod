package dev.craft.nuclearcontrol.block;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import dev.craft.ic2.item.WrenchItem;
import dev.craft.nuclearcontrol.NcBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Industrial information panel (spec 46 §5.1): a full block whose front ({@link #FACING}) faces the player – up or
 * down when looking at least 65° down or up. {@link #LIT} gives light 7 while the screen is on. Sneaking with an IC2
 * wrench turns the text ({@code IRotation}).
 */
public class InfoPanelBlock extends BaseEntityBlock {
    public static final MapCodec<InfoPanelBlock> CODEC = simpleCodec(InfoPanelBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final int LIGHT = 7;

    public InfoPanelBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }

    public static int lightLevel(BlockState state) {
        return state.getValue(LIT) ? LIGHT : 0;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    /** {@code onBlockPlacedBy}: pitch ≥ 65 → up, ≤ −65 → down, else towards the player. */
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Player player = context.getPlayer();
        Direction facing = context.getHorizontalDirection().getOpposite();
        if (player != null && player.getXRot() >= 65.0F) {
            facing = Direction.UP;
        } else if (player != null && player.getXRot() <= -65.0F) {
            facing = Direction.DOWN;
        }
        return defaultBlockState().setValue(FACING, facing);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            if ((stack.getItem() instanceof WrenchItem || stack.getItem() instanceof dev.craft.omnitools.OmniWrenchItem)
                    && level.getBlockEntity(pos) instanceof InfoPanelBlockEntity panel) {
                if (!level.isClientSide()) {
                    panel.rotate();
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide());
            }
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof InfoPanelBlockEntity panel
                && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(panel, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof InfoPanelBlockEntity panel) {
            panel.checkPowered();
        }
    }

    /** {@code breakBlock}: the inventory falls out, the extenders become free. */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof InfoPanelBlockEntity panel) {
            for (int slot = 0; slot < panel.getInventory().getSlots(); slot++) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(),
                        panel.getInventory().getStackInSlot(slot));
            }
            panel.releaseScreen();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InfoPanelBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                          BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, NcBlockEntityTypes.INFORMATION_PANEL.get(),
                (l, p, s, panel) -> panel.serverTick());
    }
}
