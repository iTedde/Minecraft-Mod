package dev.craft.buildcraft.factory;

import com.mojang.serialization.MapCodec;
import dev.craft.buildcraft.BcBlockEntityTypes;
import dev.craft.buildcraft.fluid.LiquidContainers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

/**
 * BuildCraft {@code BlockTank}: glass box 2–14 px wide. {@link #JOINED} (a tank below) switches the side texture
 * from 96 to 97.
 */
public class TankBlock extends BaseEntityBlock {
    public static final BooleanProperty JOINED = BooleanProperty.create("joined");
    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    public TankBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(JOINED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(TankBlock::new);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(JOINED);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(JOINED,
                context.getLevel().getBlockState(context.getClickedPos().below()).is(this));
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return direction == Direction.DOWN ? state.setValue(JOINED, neighborState.is(this)) : state;
    }

    /** {@code onBlockActivated}: empty a filled container into the tank, or fill an empty one from it. */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack current, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (current.isEmpty() || !(level.getBlockEntity(pos) instanceof TankBlockEntity tank)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        FluidStack liquid = LiquidContainers.getLiquidForFilledItem(current);
        if (!liquid.isEmpty()) {
            if (!level.isClientSide()) {
                int qty = tank.fill(liquid, IFluidHandler.FluidAction.EXECUTE);
                if (qty != 0 && !player.getAbilities().instabuild) {
                    player.setItemInHand(hand, LiquidContainers.consumeItem(current));
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        FluidStack available = tank.getCompositeFluid();
        if (!available.isEmpty()) {
            ItemStack filled = LiquidContainers.fillLiquidContainer(available, current);
            liquid = LiquidContainers.getLiquidForFilledItem(filled);
            if (!liquid.isEmpty()) {
                if (level.isClientSide()) {
                    return ItemInteractionResult.SUCCESS;
                }
                if (!player.getAbilities().instabuild) {
                    if (current.getCount() > 1) {
                        if (!player.getInventory().add(filled)) {
                            return ItemInteractionResult.FAIL;
                        }
                        player.setItemInHand(hand, LiquidContainers.consumeItem(current));
                    } else {
                        player.setItemInHand(hand, filled);
                    }
                }
                tank.drain(liquid.getAmount(), IFluidHandler.FluidAction.EXECUTE);
                return ItemInteractionResult.CONSUME;
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TankBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return level.isClientSide() ? null
                : createTickerHelper(type, BcBlockEntityTypes.TANK.get(), (l, p, s, tank) -> tank.serverTick());
    }
}
