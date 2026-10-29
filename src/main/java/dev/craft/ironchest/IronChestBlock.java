package dev.craft.ironchest;

import com.mojang.serialization.MapCodec;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
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
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** Iron Chests {@code BlockIronChest} (spec 22 §1): never joins into double chests, drawn by its renderer. */
public class IronChestBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 14, 15);

    private final IronChestType type;
    private final MapCodec<IronChestBlock> codec;

    public IronChestBlock(IronChestType type, Properties properties) {
        super(properties);
        this.type = type;
        this.codec = simpleCodec(p -> new IronChestBlock(type, p));
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    public IronChestType getType() {
        return type;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return codec;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    /** Like the vanilla chest: mobs path onto the chest, not through it, so cats can sit on top. */
    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType type) {
        return false;
    }

    /** The front faces the player. */
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    /** Opens unless the block above is solid underneath; a sitting cat does not block it (unlike vanilla). */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hitResult) {
        if (level.getBlockState(pos.above()).isFaceSturdy(level, pos.above(), Direction.DOWN)) {
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof IronChestBlockEntity chest) {
            player.openMenu(chest, buf -> buf.writeVarInt(type.ordinal()));
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    /**
     * A copper chest changing its stage (oxidation, wax, axe, lightning; spec 35 §1) keeps its block entity and
     * contents, as the block entity type accepts every chest block.
     */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (newState.getBlock() instanceof IronChestBlock other && other.getType() == type) {
            return;
        }
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof IronChestBlockEntity chest) {
            Containers.dropContents(level, pos, chest);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    /** Not in the original (comparators came with 1.5); the vanilla fill level signal (user, 2026-10-20). */
    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return AbstractContainerMenu.getRedstoneSignalFromBlockEntity(level.getBlockEntity(pos));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new IronChestBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return createTickerHelper(type, IronChestBlockEntityTypes.IRON_CHEST.get(), level.isClientSide()
                ? (l, p, s, chest) -> chest.clientTick() : (l, p, s, chest) -> chest.serverTick());
    }
}
