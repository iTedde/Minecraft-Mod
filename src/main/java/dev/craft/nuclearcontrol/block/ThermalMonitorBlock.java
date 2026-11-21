package dev.craft.nuclearcontrol.block;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import dev.craft.nuclearcontrol.NcBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Thermal monitor (spec 46 §3.1). {@link #STATUS} selects the front texture ({@code modifyTextureIndex}): 0 below the
 * heat level, 1 at or above it, 2 without reactor.
 */
public class ThermalMonitorBlock extends AttachedBlock {
    public static final MapCodec<ThermalMonitorBlock> CODEC = simpleCodec(ThermalMonitorBlock::new);
    public static final IntegerProperty STATUS = IntegerProperty.create("status", 0, 2);

    public ThermalMonitorBlock(Properties properties) {
        super(properties, 1);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP).setValue(STATUS, 0));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(STATUS);
    }

    public static int statusOf(int onFire) {
        return onFire == 1 ? 1 : onFire == 0 ? 0 : 2;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        return ThermalMonitorBlockEntity.openMenu(level, pos, player);
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return level.getBlockEntity(pos) instanceof ThermalMonitorBlockEntity monitor
                && monitor.emitsSignalTowards(pos.relative(direction.getOpposite())) ? 15 : 0;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ThermalMonitorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                          BlockEntityType<T> type) {
        return level.isClientSide() ? null : createTickerHelper(type, NcBlockEntityTypes.THERMAL_MONITOR.get(),
                (l, p, s, monitor) -> monitor.serverTick());
    }
}
