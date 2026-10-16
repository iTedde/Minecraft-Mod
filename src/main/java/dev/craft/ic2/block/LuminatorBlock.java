package dev.craft.ic2.block;

import com.mojang.serialization.MapCodec;
import dev.craft.ic2.Ic2BlockEntityTypes;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.cable.CableBlock;
import dev.craft.ic2.item.ElectricItem;
import dev.craft.ic2.item.ElectricItems;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FenceBlock;
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
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Luminator (IC2 {@code BlockLuminator}, dark and lit block merged into {@link #LIT}). It sticks to the face it was
 * placed on ({@link #FACING} = that face's direction, the IC2 metadata) and needs a supporting block there. On a
 * fence, iron fence, cable, glass or reinforced glass ({@link #FULL_COVER}) it covers the whole face, otherwise it is a
 * small fixture (always a plate on the floor). Breaking it drops nothing.
 */
public class LuminatorBlock extends BaseEntityBlock {
    public static final MapCodec<LuminatorBlock> CODEC = simpleCodec(LuminatorBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final BooleanProperty FULL_COVER = BooleanProperty.create("full_cover");
    private static final Map<Direction, VoxelShape> SMALL_SHAPES = new EnumMap<>(Direction.class);
    private static final Map<Direction, VoxelShape> FULL_SHAPES = new EnumMap<>(Direction.class);

    static {
        SMALL_SHAPES.put(Direction.DOWN, Block.box(4, 13, 4, 12, 16, 12));
        SMALL_SHAPES.put(Direction.UP, Block.box(0, 0, 0, 16, 1, 16));
        SMALL_SHAPES.put(Direction.NORTH, Block.box(6, 3, 14, 10, 13, 16));
        SMALL_SHAPES.put(Direction.SOUTH, Block.box(6, 3, 0, 10, 13, 2));
        SMALL_SHAPES.put(Direction.WEST, Block.box(14, 3, 6, 16, 13, 10));
        SMALL_SHAPES.put(Direction.EAST, Block.box(0, 3, 6, 2, 13, 10));
        FULL_SHAPES.put(Direction.DOWN, Block.box(0, 15, 0, 16, 16, 16));
        FULL_SHAPES.put(Direction.UP, Block.box(0, 0, 0, 16, 1, 16));
        FULL_SHAPES.put(Direction.NORTH, Block.box(0, 0, 15, 16, 16, 16));
        FULL_SHAPES.put(Direction.SOUTH, Block.box(0, 0, 0, 16, 16, 1));
        FULL_SHAPES.put(Direction.WEST, Block.box(15, 0, 0, 16, 16, 16));
        FULL_SHAPES.put(Direction.EAST, Block.box(0, 0, 0, 1, 16, 16));
    }

    public LuminatorBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP).setValue(LIT, false)
                .setValue(FULL_COVER, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT, FULL_COVER);
    }

    /** Shape of the luminator box (IC2 {@code getBoxOfLuminator}); the same for collision, selection and model. */
    public static VoxelShape shape(Direction facing, boolean fullCover) {
        return (fullCover ? FULL_SHAPES : SMALL_SHAPES).get(facing);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shape(state.getValue(FACING), state.getValue(FULL_COVER));
    }

    // --- support ---

    /** IC2 {@code isSupportingBlock}: a normal cube or a special supporter. */
    public static boolean isSupportingBlock(BlockGetter level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return !state.isAir() && (state.isRedstoneConductor(level, pos) || isSpecialSupporter(state));
    }

    /** IC2 {@code isSpecialSupporter}: fences, iron fences, cables, reinforced glass and glass. */
    public static boolean isSpecialSupporter(BlockState state) {
        return state.getBlock() instanceof FenceBlock || state.getBlock() instanceof IronFenceBlock
                || state.getBlock() instanceof CableBlock || state.is(Ic2Blocks.REINFORCED_GLASS.get())
                || state.is(Blocks.GLASS);
    }

    private static BlockPos supportPos(BlockPos pos, Direction facing) {
        return pos.relative(facing.getOpposite());
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getClickedFace();
        BlockPos support = supportPos(context.getClickedPos(), facing);
        if (!isSupportingBlock(context.getLevel(), support)) {
            return null;
        }
        return defaultBlockState().setValue(FACING, facing)
                .setValue(FULL_COVER, isSpecialSupporter(context.getLevel().getBlockState(support)));
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return isSupportingBlock(level, supportPos(pos, state.getValue(FACING)));
    }

    /** IC2 removes an unsupported luminator without drops. */
    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Direction facing = state.getValue(FACING);
        if (direction != facing.getOpposite()) {
            return state;
        }
        if (!canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return state.setValue(FULL_COVER, isSpecialSupporter(neighborState));
    }

    // --- interaction ---

    /** Right click with an electric item tops the luminator up (tier 2, ignoring the transfer limit). */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(stack.getItem() instanceof ElectricItem)
                || !(level.getBlockEntity(pos) instanceof LuminatorBlockEntity luminator)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        int transfer = LuminatorBlockEntity.MAX_ENERGY - luminator.getEnergy();
        if (transfer <= 0) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide) {
            luminator.charge(ElectricItems.discharge(stack, transfer, 2, true, false));
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    /** A lit luminator sets touching monsters on fire: 20 s for undead, 10 s otherwise. */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (state.getValue(LIT) && entity instanceof Monster) {
            entity.igniteForSeconds(entity.getType().is(EntityTypeTags.UNDEAD) ? 20 : 10);
        }
    }

    // --- block entity ---

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new LuminatorBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                          BlockEntityType<T> blockEntityType) {
        return level.isClientSide ? null : createTickerHelper(blockEntityType, Ic2BlockEntityTypes.LUMINATOR.get(),
                LuminatorBlockEntity::serverTick);
    }
}
