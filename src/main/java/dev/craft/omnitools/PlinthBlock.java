package dev.craft.omnitools;

import com.mojang.serialization.MapCodec;
import dev.craft.buildcraft.item.ToolWrench;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
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
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Mystical Plinth ({@code BlockPlinth}, spec 50 §1.2): ¾ high; with the Forge Lexicon on it ({@link #LEXICON}) it
 * glows (15), carries the lexicon's block entity and opens its GUI. Sneaking with a wrench takes the lexicon (and its
 * contents) off, or dismantles an empty plinth.
 */
public class PlinthBlock extends BaseEntityBlock {
    public static final MapCodec<PlinthBlock> CODEC = simpleCodec(PlinthBlock::new);
    public static final BooleanProperty LEXICON = BooleanProperty.create("lexicon");
    private static final VoxelShape SHAPE = box(0, 0, 0, 16, 12, 16);

    public PlinthBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(LEXICON, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LEXICON);
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
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return state.getValue(LEXICON) ? new LexiconBlockEntity(pos, state) : null;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> type) {
        return level.isClientSide() && state.getValue(LEXICON)
                ? createTickerHelper(type, OmniTools.LEXICON_BLOCK_ENTITY.get(), LexiconBlockEntity::clientTick)
                : null;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.isShiftKeyDown()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!ToolWrench.isWrench(stack)) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide()) {
            if (state.getValue(LEXICON)) {
                removeLexicon(level, pos, state);
            } else {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), new ItemStack(this));
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (player.isShiftKeyDown() || !(level.getBlockEntity(pos) instanceof LexiconBlockEntity book)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            player.openMenu(book);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    /** Takes the lexicon off: contents and lexicon drop, the plinth stays. */
    public static void removeLexicon(Level level, BlockPos pos, BlockState state) {
        if (level.getBlockEntity(pos) instanceof LexiconBlockEntity book) {
            book.dropContents();
        }
        Containers.dropItemStack(level, pos.getX(), pos.getY() + 0.75, pos.getZ(), LexiconBlockEntity.lexiconItem());
        // The block keeps its block entity across a state change; the empty plinth has none.
        level.removeBlockEntity(pos);
        level.setBlock(pos, state.setValue(LEXICON, false), Block.UPDATE_ALL);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && state.getValue(LEXICON)) {
            if (level.getBlockEntity(pos) instanceof LexiconBlockEntity book) {
                book.dropContents();
            }
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), LexiconBlockEntity.lexiconItem());
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
