package dev.craft.buildcraft.factory;

import com.mojang.serialization.MapCodec;
import dev.craft.buildcraft.BcBlockEntityTypes;
import dev.craft.buildcraft.fluid.LiquidContainers;
import dev.craft.buildcraft.item.ToolWrench;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

/** BuildCraft {@code BlockRefinery}: drawn by the block entity renderer, turned towards the player when placed. */
public class RefineryBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public RefineryBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.SOUTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(RefineryBlock::new);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Player player = context.getPlayer();
        Direction facing = player == null ? Direction.SOUTH
                : QuarryBlock.facingTowards(player.getX(), player.getZ(), context.getClickedPos());
        return defaultBlockState().setValue(FACING, facing);
    }

    /** BC wrench: W → S, E → N, N → W, S → E. */
    private static Direction rotate(Direction facing) {
        return switch (facing) {
            case WEST -> Direction.SOUTH;
            case EAST -> Direction.NORTH;
            case NORTH -> Direction.WEST;
            default -> Direction.EAST;
        };
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (player.isShiftKeyDown()) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        if (ToolWrench.canWrenchWith(stack, player, pos)) {
            if (!level.isClientSide()) {
                level.setBlock(pos, state.setValue(FACING, rotate(state.getValue(FACING))), 3);
            }
            ToolWrench.wrenchUsedWith(stack, player, pos);
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        FluidStack liquid = LiquidContainers.getLiquidForFilledItem(stack);
        if (!liquid.isEmpty()) {
            if (!level.isClientSide() && level.getBlockEntity(pos) instanceof RefineryBlockEntity refinery) {
                int qty = refinery.fill(liquid, IFluidHandler.FluidAction.EXECUTE);
                if (qty != 0 && !player.getAbilities().instabuild) {
                    player.setItemInHand(hand, LiquidContainers.consumeItem(stack));
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hitResult) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof RefineryBlockEntity refinery) {
            player.openMenu(refinery, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new RefineryBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return level.isClientSide()
                ? createTickerHelper(type, BcBlockEntityTypes.REFINERY.get(), (l, p, s, r) -> r.clientTick())
                : createTickerHelper(type, BcBlockEntityTypes.REFINERY.get(), (l, p, s, r) -> r.serverTick());
    }
}
