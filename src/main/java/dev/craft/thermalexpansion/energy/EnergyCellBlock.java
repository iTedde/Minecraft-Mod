package dev.craft.thermalexpansion.energy;

import com.mojang.serialization.MapCodec;
import dev.craft.thermalexpansion.TeDataComponents;
import dev.craft.thermalexpansion.item.CrescentHammerItem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * {@code BlockEnergyCell}: Material.iron, hardness 20, {@code setResistance(150)} = 90, drawn by its block entity
 * renderer, light 0–10 ({@link #CHARGE}). Sneaking with a wrench dismantles it with charge, limits and redstone mode;
 * broken normally it drops an empty cell.
 */
public class EnergyCellBlock extends BaseEntityBlock {
    /** {@code getLightValue}: {@code min(10, energy · 11 / 600 000)}. */
    public static final IntegerProperty CHARGE = IntegerProperty.create("charge", 0, 10);

    public EnergyCellBlock(Properties properties) {
        super(properties.lightLevel(state -> state.getValue(CHARGE)));
        registerDefaultState(stateDefinition.any().setValue(CHARGE, 0));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(EnergyCellBlock::new);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(CHARGE);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnergyCellBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return null;
        }
        return (tickLevel, pos, tickState, be) -> {
            if (be instanceof EnergyCellBlockEntity cell) {
                cell.serverTick();
            }
        };
    }

    /** {@code ItemBlockEnergyCell.placeBlockAt}. */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
                            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof EnergyCellBlockEntity cell) {
            cell.load(stack.getOrDefault(TeDataComponents.ENERGY_CELL.get(), EnergyCellData.DEFAULT));
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.isShiftKeyDown() || !CrescentHammerItem.isWrench(stack)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof EnergyCellBlockEntity cell) {
            dismantle(level, pos, cell);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof EnergyCellBlockEntity cell
                && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(cell, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    /** {@code dismantleBlock}: the cell pops out with its data. */
    private static void dismantle(Level level, BlockPos pos, EnergyCellBlockEntity cell) {
        ItemStack drop = new ItemStack(level.getBlockState(pos).getBlock());
        drop.set(TeDataComponents.ENERGY_CELL.get(), cell.toData());
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        float f = 0.3F;
        ItemEntity entity = new ItemEntity(level, pos.getX() + level.random.nextFloat() * f + (1.0F - f) * 0.5,
                pos.getY() + level.random.nextFloat() * f + (1.0F - f) * 0.5,
                pos.getZ() + level.random.nextFloat() * f + (1.0F - f) * 0.5, drop);
        entity.setPickUpDelay(10);
        level.addFreshEntity(entity);
    }
}
