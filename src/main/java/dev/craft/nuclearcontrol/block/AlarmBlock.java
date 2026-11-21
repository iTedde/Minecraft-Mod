package dev.craft.nuclearcontrol.block;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import dev.craft.nuclearcontrol.NcBlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
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
 * Howler alarm and industrial alarm (spec 46 §4). The industrial alarm's lamp ({@link #LAMP} 0/1/2 = light 0/7/15)
 * selects the front texture and the light level.
 */
public class AlarmBlock extends AttachedBlock {
    public static final MapCodec<AlarmBlock> CODEC = simpleCodec(properties -> new AlarmBlock(properties, false));
    public static final IntegerProperty LAMP = IntegerProperty.create("lamp", 0, 2);
    private static final int[] LIGHT = {0, 7, 15};

    private final boolean industrial;

    public AlarmBlock(Properties properties, boolean industrial) {
        super(properties, 2);
        this.industrial = industrial;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP).setValue(LAMP, 0));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LAMP);
    }

    public boolean isIndustrial() {
        return industrial;
    }

    public static int lightLevel(BlockState state) {
        return LIGHT[state.getValue(LAMP)];
    }

    public static int lampOf(int light) {
        return light >= 15 ? 2 : light >= 7 ? 1 : 0;
    }

    @Override
    protected void onNeighborChanged(BlockState state, Level level, BlockPos pos) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof AlarmBlockEntity alarm) {
            alarm.checkPowered();
        }
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof AlarmBlockEntity alarm
                && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(alarm, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new AlarmBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                          BlockEntityType<T> type) {
        return createTickerHelper(type, NcBlockEntityTypes.ALARM.get(), level.isClientSide()
                ? (l, p, s, alarm) -> alarm.clientTick() : (l, p, s, alarm) -> alarm.serverTick());
    }
}
