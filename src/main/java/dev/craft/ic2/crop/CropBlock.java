package dev.craft.ic2.crop;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import dev.craft.ic2.Ic2BlockEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Crop stick (IC2 {@code BlockCrop}): only on farmland, breaks when the farmland goes. The full block is the outline;
 * the collision box is IC2's 0.2–0.8 × 0.7 box. The rendered sprite of {@code crops_0.png} is the {@link #SPRITE}
 * state, set by the tile. Clicks go to the tile; a planted crop drops nothing extra when broken, only the stick.
 */
public class CropBlock extends BaseEntityBlock {
    public static final MapCodec<CropBlock> CODEC = simpleCodec(CropBlock::new);
    /** Highest sprite index used on {@code crops_0.png}. */
    public static final int MAX_SPRITE = 51;
    public static final IntegerProperty SPRITE = IntegerProperty.create("sprite", 0, MAX_SPRITE);
    private static final VoxelShape COLLISION = Block.box(3.2, 0.0, 3.2, 12.8, 11.2, 12.8);

    public CropBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(SPRITE, 0));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SPRITE);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
            CollisionContext context) {
        return COLLISION;
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return level.getBlockState(pos.below()).is(Blocks.FARMLAND);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
            BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (level.isClientSide()) {
            return;
        }
        if (!canSurvive(state, level, pos)) {
            level.destroyBlock(pos, true);
        } else if (level.getBlockEntity(pos) instanceof CropBlockEntity crop) {
            crop.onNeighbourChange();
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof CropBlockEntity crop) {
            crop.onBlockDestroyed();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CropBlockEntity crop) {
            crop.onEntityCollision(entity);
        }
    }

    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof CropBlockEntity crop) {
            crop.leftclick(player);
        }
    }

    /**
     * IC2 calls the tile for every right click with whatever is held (main hand only); if the tile does not use the
     * click, the held item gets its own use (seed bags plant themselves that way).
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
            Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (hand != InteractionHand.MAIN_HAND) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        if (level.isClientSide()) {
            return ItemInteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof CropBlockEntity crop && crop.rightclick(player)) {
            player.inventoryMenu.broadcastChanges();
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return level.getBlockEntity(pos) instanceof CropBlockEntity crop && crop.emitRedstone() ? 15 : 0;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof CropBlockEntity crop ? crop.getEmittedLight() : 0;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CropBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
            BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, Ic2BlockEntityTypes.CROP.get(),
                CropBlockEntity::serverTick);
    }
}
