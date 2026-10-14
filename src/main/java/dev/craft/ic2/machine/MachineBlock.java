package dev.craft.ic2.machine;

import java.util.function.BiFunction;

import com.mojang.serialization.MapCodec;
import dev.craft.ic2.Ic2Machine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** Block of an IC2 machine, generator or storage block (IC2 {@code BlockMultiID}). */
public class MachineBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private final Ic2Machine machine;
    private final BiFunction<BlockPos, BlockState, ? extends MachineBlockEntity> factory;

    public MachineBlock(Properties properties, Ic2Machine machine,
                        BiFunction<BlockPos, BlockState, ? extends MachineBlockEntity> factory) {
        super(properties);
        this.machine = machine;
        this.factory = factory;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ACTIVE, false));
    }

    public Ic2Machine getMachine() {
        return machine;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, placementFacing(machine.getFacingMode(), context.getPlayer()));
    }

    /** IC2 {@code onBlockPlacedBy}: the front faces the placer; storage blocks also up/down by pitch. */
    public static Direction placementFacing(FacingMode mode, @Nullable Player player) {
        if (player == null) {
            return mode == FacingMode.ALL ? Direction.UP : Direction.NORTH;
        }
        if (mode == FacingMode.ALL) {
            if (player.getXRot() >= 65.0F) {
                return Direction.UP;
            }
            if (player.getXRot() <= -65.0F) {
                return Direction.DOWN;
            }
        }
        return player.getDirection().getOpposite();
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return factory.apply(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return (tickLevel, pos, tickState, be) -> {
                if (be instanceof MachineBlockEntity machineEntity) {
                    machineEntity.clientTick();
                }
            };
        }
        return (tickLevel, pos, tickState, be) -> {
            if (be instanceof MachineBlockEntity machineEntity) {
                machineEntity.serverTick();
            }
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof MachineBlockEntity be
                && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(be, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof MachineBlockEntity be) {
            be.onBlockBreak();
            be.dropContents();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
