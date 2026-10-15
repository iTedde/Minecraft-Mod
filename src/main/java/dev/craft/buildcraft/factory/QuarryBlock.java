package dev.craft.buildcraft.factory;

import com.mojang.serialization.MapCodec;
import dev.craft.buildcraft.BcBlockEntityTypes;
import dev.craft.buildcraft.item.BcWrenchItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * BuildCraft {@code BlockQuarry}. {@link #FACING} is the front, turned towards the player
 * ({@code Utils.get2dOrientation} against the block corner); the work area lies behind it.
 */
public class QuarryBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public QuarryBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(QuarryBlock::new);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /** BuildCraft {@code Utils.get2dOrientation(player, block)}, turned around. */
    static Direction facingTowards(double playerX, double playerZ, BlockPos pos) {
        double angle = Math.atan2(playerZ - pos.getZ(), playerX - pos.getX()) / Math.PI * 180.0 + 180.0;
        Direction orientation;
        if (angle < 45.0 || angle > 315.0) {
            orientation = Direction.EAST;
        } else if (angle < 135.0) {
            orientation = Direction.SOUTH;
        } else {
            orientation = angle < 225.0 ? Direction.WEST : Direction.NORTH;
        }
        return orientation.getOpposite();
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Player player = context.getPlayer();
        Direction facing = player == null ? Direction.SOUTH
                : facingTowards(player.getX(), player.getZ(), context.getClickedPos());
        return defaultBlockState().setValue(FACING, facing);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
                            ItemStack stack) {
        if (placer instanceof Player player && level.getBlockEntity(pos) instanceof QuarryBlockEntity quarry) {
            quarry.setPlacedBy(player);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (player.isShiftKeyDown() || !(stack.getItem() instanceof BcWrenchItem)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof QuarryBlockEntity quarry) {
            quarry.reinitialize();
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && !level.isClientSide()
                && level.getBlockEntity(pos) instanceof QuarryBlockEntity quarry) {
            quarry.onBreak();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new QuarryBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return level.isClientSide()
                ? createTickerHelper(type, BcBlockEntityTypes.QUARRY.get(), (l, p, s, quarry) -> quarry.clientTick())
                : createTickerHelper(type, BcBlockEntityTypes.QUARRY.get(), (l, p, s, quarry) -> quarry.serverTick());
    }
}
