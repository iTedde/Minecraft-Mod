package dev.craft.thermalexpansion.block;

import com.mojang.serialization.MapCodec;
import dev.craft.thermalexpansion.TeDataComponents;
import dev.craft.thermalexpansion.item.CrescentHammerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

/**
 * {@code BlockTank}: glass material, hardness 3, {@code setResistance(150)} = 90, bounds 2–14 px. A liquid container
 * fills or empties it (any other container does nothing), the wrench switches the output mode, sneaking with it
 * dismantles the tank with its liquid. Broken normally it drops empty.
 */
public class PortableTankBlock extends BaseEntityBlock {
    private static final VoxelShape SHAPE = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    public PortableTankBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(PortableTankBlock::new);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PortableTankBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return null;
        }
        return (tickLevel, pos, tickState, be) -> {
            if (be instanceof PortableTankBlockEntity tank) {
                tank.serverTick();
            }
        };
    }

    /** {@code ItemBlockTank.placeBlockAt} + {@code onBlockAdded}: the liquid of the item, the mode from below. */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
                            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof PortableTankBlockEntity tank) {
            SimpleFluidContent content = stack.get(TeDataComponents.TANK_LIQUID.get());
            if (content != null && !content.isEmpty()) {
                tank.getTank().setFluid(content.copy());
            }
            tank.placeBlock();
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof PortableTankBlockEntity tank) {
            tank.onNeighborBlockChange();
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof PortableTankBlockEntity tank)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        boolean wrench = CrescentHammerItem.isWrench(stack);
        if (player.isShiftKeyDown()) {
            if (!wrench) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            if (!level.isClientSide()) {
                dismantle(level, pos, tank);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        IFluidHandler handler = tank.getFluidHandler(null);
        if (handler != null && FluidUtil.interactWithFluidHandler(player, hand, handler)) {
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        if (FluidUtil.getFluidHandler(stack).isPresent()) {
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        if (wrench) {
            if (!level.isClientSide()) {
                tank.changeMode();
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /** {@code dismantleBlock}: the tank pops out with its liquid. */
    private static void dismantle(Level level, BlockPos pos, PortableTankBlockEntity tank) {
        ItemStack drop = new ItemStack(level.getBlockState(pos).getBlock());
        if (!tank.getTank().isEmpty()) {
            drop.set(TeDataComponents.TANK_LIQUID.get(), SimpleFluidContent.copyOf(tank.getTank().getFluid()));
        }
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        float f = 0.3F;
        ItemEntity entity = new ItemEntity(level, pos.getX() + level.random.nextFloat() * f + (1.0F - f) * 0.5,
                pos.getY() + level.random.nextFloat() * f + (1.0F - f) * 0.5,
                pos.getZ() + level.random.nextFloat() * f + (1.0F - f) * 0.5, drop);
        entity.setPickUpDelay(10);
        level.addFreshEntity(entity);
    }
}
