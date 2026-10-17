package dev.craft.ic2.brewing;

import com.mojang.serialization.MapCodec;
import dev.craft.ic2.Ic2BlockEntityTypes;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2DataComponents;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.block.ScaffoldBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
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

/**
 * Booze barrel block (IC2 {@code BlockBarrel}), set up from a barrel item on a wooden scaffold. A treetap sits on one
 * horizontal side ({@link #TAPPED} + {@link #FACING}, the IC2 {@code treetapSide}). Punching the barrel removes the
 * treetap (losing one litre) or, without treetap, turns it back into a scaffold and drops the barrel with its
 * contents. Broken otherwise (explosion) it drops a scaffold and an empty barrel.
 */
public class BarrelBlock extends BaseEntityBlock {
    public static final MapCodec<BarrelBlock> CODEC = simpleCodec(BarrelBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty TAPPED = BooleanProperty.create("tapped");

    public BarrelBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(TAPPED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, TAPPED);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    public static boolean isTapped(BlockState state) {
        return state.getValue(TAPPED);
    }

    /** True if the barrel carries a treetap on the given side. */
    public static boolean isTappedOn(BlockState state, Direction side) {
        return state.getValue(TAPPED) && state.getValue(FACING) == side;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (hand != InteractionHand.MAIN_HAND || !(level.getBlockEntity(pos) instanceof BarrelBlockEntity barrel)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide) {
            return isIngredient(stack) ? ItemInteractionResult.SUCCESS
                    : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        return barrel.rightClick(player) ? ItemInteractionResult.CONSUME
                : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private static boolean isIngredient(ItemStack stack) {
        return stack.is(Items.WATER_BUCKET) || stack.is(Ic2Items.WATER_CELL.get()) || stack.is(Items.WHEAT)
                || stack.is(Ic2Items.HOPS.get()) || stack.is(Items.SUGAR_CANE);
    }

    /** IC2 {@code onBlockClicked}. */
    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (level.isClientSide || !(level.getBlockEntity(pos) instanceof BarrelBlockEntity barrel)) {
            return;
        }
        if (isTapped(state)) {
            popResource(level, pos, new ItemStack(Ic2Items.TREETAP.get()));
            level.setBlock(pos, state.setValue(TAPPED, false), Block.UPDATE_ALL);
            barrel.drainLiquid(1);
        } else {
            popResource(level, pos, barrelItem(barrel.calculateValue()));
            level.setBlock(pos, Ic2Blocks.WOODEN_SCAFFOLD.get().defaultBlockState()
                    .setValue(ScaffoldBlock.STRENGTH, 0), Block.UPDATE_ALL);
        }
    }

    public static ItemStack barrelItem(int value) {
        ItemStack stack = new ItemStack(Ic2Items.BOOZE_BARREL.get());
        if (value != 0) {
            stack.set(Ic2DataComponents.BOOZE.get(), value);
        }
        return stack;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        return new ItemStack(Ic2Items.BOOZE_BARREL.get());
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BarrelBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, Ic2BlockEntityTypes.BARREL.get(),
                BarrelBlockEntity::serverTick);
    }
}
