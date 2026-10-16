package dev.craft.powerplants.block;

import java.util.function.BiFunction;

import com.mojang.serialization.MapCodec;
import dev.craft.ic2.machine.MachineBlock;
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
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Controller block of a power plant multiblock: horizontal facing towards the player who places it (out of the
 * structure), active flag, opens the controller's menu and drops its inventory.
 */
public class PlantControllerBlock extends BaseEntityBlock {
    private final BiFunction<BlockPos, BlockState, ? extends PlantControllerBlockEntity> factory;

    public PlantControllerBlock(Properties properties,
                                BiFunction<BlockPos, BlockState, ? extends PlantControllerBlockEntity> factory) {
        super(properties);
        this.factory = factory;
        registerDefaultState(stateDefinition.any().setValue(MachineBlock.FACING, Direction.NORTH)
                .setValue(MachineBlock.ACTIVE, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(MachineBlock.FACING, MachineBlock.ACTIVE);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(MachineBlock.FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return factory.apply(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return null;
        }
        return (tickLevel, pos, tickState, be) -> {
            if (be instanceof PlantControllerBlockEntity controller) {
                controller.serverTick();
            }
        };
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof PlantControllerBlockEntity be
                && player instanceof ServerPlayer serverPlayer) {
            if (be.hasMenu()) {
                serverPlayer.openMenu(be, buf -> buf.writeBlockPos(pos));
            } else {
                be.onUse(player);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide() && !state.is(oldState.getBlock())) {
            PlantPartBlock.notifyControllers(level, pos);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof PlantControllerBlockEntity be) {
            be.dropContents();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
