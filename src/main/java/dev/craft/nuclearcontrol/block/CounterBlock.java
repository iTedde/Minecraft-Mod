package dev.craft.nuclearcontrol.block;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.craft.nuclearcontrol.NcBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Energy or average counter (spec 46 §7): a full block whose input side ({@link #FACING}) faces the player when
 * placed – up or down when looking at least 65° down or up; the wrench sets it to the clicked side.
 */
public class CounterBlock extends BaseEntityBlock {
    public static final MapCodec<CounterBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    propertiesCodec(),
                    com.mojang.serialization.Codec.BOOL.fieldOf("average").forGetter(block -> block.average))
            .apply(instance, CounterBlock::new));
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private final boolean average;

    public CounterBlock(Properties properties, boolean average) {
        super(properties);
        this.average = average;
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
        return defaultBlockState().setValue(FACING, facingTowards(context));
    }

    static Direction facingTowards(BlockPlaceContext context) {
        Player player = context.getPlayer();
        if (player != null && player.getXRot() >= 65.0F) {
            return Direction.UP;
        }
        if (player != null && player.getXRot() <= -65.0F) {
            return Direction.DOWN;
        }
        return context.getHorizontalDirection().getOpposite();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CounterBlockEntity counter
                && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(counter, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof CounterBlockEntity counter) {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(),
                    counter.getInventory().getStackInSlot(0));
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return average ? new AverageCounterBlockEntity(pos, state) : new EnergyCounterBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                          BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return average
                ? createTickerHelper(type, NcBlockEntityTypes.AVERAGE_COUNTER.get(), (l, p, s, c) -> c.serverTick())
                : createTickerHelper(type, NcBlockEntityTypes.ENERGY_COUNTER.get(), (l, p, s, c) -> c.serverTick());
    }
}
