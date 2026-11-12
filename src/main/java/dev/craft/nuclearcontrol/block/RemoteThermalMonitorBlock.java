package dev.craft.nuclearcontrol.block;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import dev.craft.ic2.item.WrenchItem;
import dev.craft.nuclearcontrol.NcBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Remote thermal monitor (spec 46 §3.2): a full block whose display ({@link #FACING}) faces the player – up or down
 * when looking at least 65° down or up. Sneaking with an IC2 wrench turns the display ({@code IRotation}).
 */
public class RemoteThermalMonitorBlock extends BaseEntityBlock {
    public static final MapCodec<RemoteThermalMonitorBlock> CODEC = simpleCodec(RemoteThermalMonitorBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    public RemoteThermalMonitorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
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
            if (stack.getItem() instanceof WrenchItem
                    && level.getBlockEntity(pos) instanceof RemoteThermalMonitorBlockEntity monitor) {
                if (!level.isClientSide()) {
                    monitor.rotate();
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
        return ThermalMonitorBlockEntity.openMenu(level, pos, player);
    }

    /** {@code breakBlock}: the inventory falls out. */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock())
                && level.getBlockEntity(pos) instanceof RemoteThermalMonitorBlockEntity monitor) {
            for (int slot = 0; slot < monitor.getInventory().getSlots(); slot++) {
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(),
                        monitor.getInventory().getStackInSlot(slot));
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return level.getBlockEntity(pos) instanceof RemoteThermalMonitorBlockEntity monitor
                && monitor.emitsSignalTowards(pos.relative(direction.getOpposite())) ? 15 : 0;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RemoteThermalMonitorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                          BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type,
                NcBlockEntityTypes.REMOTE_THERMAL_MONITOR.get(), (l, p, s, monitor) -> monitor.serverTick());
    }
}
