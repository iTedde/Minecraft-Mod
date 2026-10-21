package dev.craft.thermalexpansion.machine;

import java.util.function.BiFunction;

import com.mojang.serialization.MapCodec;
import dev.craft.thermalexpansion.item.CrescentHammerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
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

/**
 * Thermal Expansion {@code BlockMachine} + {@code BlockTERoot}: hardness 15, {@code setResistance(25)} = 15; the
 * wrench turns the machine, sneaking with it dismantles (the machine pops out, the inventory drops).
 */
public class TeMachineBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private final TeMachineType type;
    private final BiFunction<BlockPos, BlockState, ? extends TeMachineBlockEntity> factory;

    public TeMachineBlock(Properties properties, TeMachineType type,
                          BiFunction<BlockPos, BlockState, ? extends TeMachineBlockEntity> factory) {
        super(properties.lightLevel(state -> state.getValue(ACTIVE) ? type.getLightValue() : 0));
        this.type = type;
        this.factory = factory;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ACTIVE, false));
    }

    public TeMachineType getMachineType() {
        return type;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE);
    }

    /** {@code onBlockPlacedBy}: the front faces the player. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
                            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TeMachineBlockEntity machine) {
            machine.initFacing(state.getValue(FACING));
        }
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
                                                                  BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return null;
        }
        return (tickLevel, pos, tickState, be) -> {
            if (be instanceof TeMachineBlockEntity machine) {
                machine.serverTick();
            }
        };
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!CrescentHammerItem.isWrench(stack)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TeMachineBlockEntity machine) {
            if (player.isShiftKeyDown()) {
                dismantle(level, pos);
            } else {
                machine.rotate();
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    /** {@code dismantleBlock}: the block becomes air (dropping its inventory) and pops out as an item. */
    public static void dismantle(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        float f = 0.3F;
        ItemEntity entity = new ItemEntity(level, pos.getX() + level.random.nextFloat() * f + (1.0F - f) * 0.5,
                pos.getY() + level.random.nextFloat() * f + (1.0F - f) * 0.5,
                pos.getZ() + level.random.nextFloat() * f + (1.0F - f) * 0.5, new ItemStack(state.getBlock()));
        entity.setPickUpDelay(10);
        level.addFreshEntity(entity);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TeMachineBlockEntity machine
                && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(machine, buf -> {
                buf.writeVarInt(type.ordinal());
                buf.writeBlockPos(pos);
            });
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof TeMachineBlockEntity machine) {
            machine.dropContents();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
