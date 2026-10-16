package dev.craft.buildcraft.transport;

import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;

import dev.craft.buildcraft.item.BcWrenchItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.common.ItemAbilities;

/**
 * BuildCraft {@code BlockGenericPipe}: one block per pipe type. The connections ({@code pipeConnectionsBuffer}) are
 * block state properties so the model shows the arms; wood and emerald keep their source side, iron its output side
 * in the block state instead of the metadata.
 */
public class GenericPipeBlock extends BaseEntityBlock {
    /** Wood/emerald source side, 0–5 = {@link Direction#get3DDataValue()}, 6 = none. */
    public static final IntegerProperty SOURCE = IntegerProperty.create("source", 0, 6);
    /** Iron output side, 0–5. */
    public static final IntegerProperty OUTPUT = IntegerProperty.create("output", 0, 5);

    private static final double MIN = 0.25;
    private static final double MAX = 0.75;
    private static final VoxelShape[] COLLISION = new VoxelShape[64];
    private static final VoxelShape[] SELECTION = new VoxelShape[64];

    static {
        for (int mask = 0; mask < 64; mask++) {
            VoxelShape shape = Shapes.box(MIN, MIN, MIN, MAX, MAX, MAX);
            for (Direction side : Direction.values()) {
                if ((mask & 1 << side.get3DDataValue()) != 0) {
                    shape = Shapes.or(shape, Shapes.create(arm(side)));
                }
            }
            COLLISION[mask] = shape;
            SELECTION[mask] = Shapes.create(shape.bounds());
        }
    }

    private final PipeType type;

    protected GenericPipeBlock(Properties properties, PipeType type) {
        super(properties);
        this.type = type;
        BlockState state = stateDefinition.any();
        for (BooleanProperty property : PipeBlock.PROPERTY_BY_DIRECTION.values()) {
            state = state.setValue(property, false);
        }
        registerDefaultState(state);
    }

    public static GenericPipeBlock create(Properties properties, PipeType type) {
        return switch (type.getMaterial()) {
            case WOOD, EMERALD -> new SourcePipeBlock(properties, type);
            case IRON -> new OutputPipeBlock(properties, type);
            default -> new GenericPipeBlock(properties, type);
        };
    }

    private static AABB arm(Direction side) {
        double minX = side == Direction.WEST ? 0.0 : MIN;
        double maxX = side == Direction.EAST ? 1.0 : MAX;
        double minY = side == Direction.DOWN ? 0.0 : MIN;
        double maxY = side == Direction.UP ? 1.0 : MAX;
        double minZ = side == Direction.NORTH ? 0.0 : MIN;
        double maxZ = side == Direction.SOUTH ? 1.0 : MAX;
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    public PipeType getType() {
        return type;
    }

    public static boolean isConnected(BlockState state, Direction side) {
        return state.getValue(PipeBlock.PROPERTY_BY_DIRECTION.get(side));
    }

    private static int connectionMask(BlockState state) {
        int mask = 0;
        for (Direction side : Direction.values()) {
            if (isConnected(state, side)) {
                mask |= 1 << side.get3DDataValue();
            }
        }
        return mask;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(PipeBlock.PROPERTY_BY_DIRECTION.values().toArray(BooleanProperty[]::new));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SELECTION[connectionMask(state)];
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                           CollisionContext context) {
        return COLLISION[connectionMask(state)];
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return type.getTransport() == PipeType.Transport.FLUIDS ? new FluidPipeBlockEntity(pos, state)
                : new ItemPipeBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> blockEntityType) {
        return (tickLevel, pos, tickState, be) -> {
            if (be instanceof PipeBlockEntity pipe) {
                pipe.tick();
            }
        };
    }

    /**
     * {@code onBlockActivated} without the gate and wire branches: a pipe in the hand places, a shield does nothing,
     * otherwise the pipe logic reacts (wrench: wood switches its source, iron its output).
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof BlockItem item && item.getBlock() instanceof GenericPipeBlock
                || stack.canPerformAction(ItemAbilities.SHIELD_BLOCK)) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        if (stack.getItem() instanceof BcWrenchItem && onWrench(state, level, pos)) {
            level.updateNeighborsAt(pos, this);
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /** {@code PipeLogicDiamond}/{@code PipeItemsEmerald.blockActivated}: the filter GUI. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof ItemPipeBlockEntity pipe) || pipe.getFilters().getSlots() == 0) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            player.openMenu(pipe, buf -> buf.writeVarInt(pipe.getFilters().getSlots()));
            level.updateNeighborsAt(pos, this);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    /** {@code PipeItemsObsidian.onEntityCollidedWithBlock}: touching items and arrows are pulled in for free. */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide() && type.getMaterial() == PipeMaterial.OBSIDIAN
                && level.getBlockEntity(pos) instanceof ItemPipeBlockEntity pipe && pipe.canSuck(entity, 0)) {
            pipe.pullItemIntoPipe(entity, 0);
        }
    }

    /** {@code PipeLogic.blockActivated} with a wrench; true if the logic used it. */
    protected boolean onWrench(BlockState state, Level level, BlockPos pos) {
        return false;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   BlockPos neighborPos, boolean movedByPiston) {
        if (level.getBlockEntity(pos) instanceof PipeBlockEntity pipe) {
            pipe.scheduleNeighborChange();
        }
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof PipeBlockEntity pipe) {
            pipe.dropContents();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    /** {@code PipeLogicWood}/{@code PipeLogicEmerald}: source side in the block state. */
    public static class SourcePipeBlock extends GenericPipeBlock {
        SourcePipeBlock(Properties properties, PipeType type) {
            super(properties, type);
            registerDefaultState(defaultBlockState().setValue(SOURCE, 6));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            super.createBlockStateDefinition(builder);
            builder.add(SOURCE);
        }

        @Override
        protected boolean onWrench(BlockState state, Level level, BlockPos pos) {
            if (!level.isClientSide() && level.getBlockEntity(pos) instanceof PipeBlockEntity pipe) {
                pipe.switchSource();
            }
            return true;
        }
    }

    /** {@code PipeLogicIron}: output side in the block state. */
    public static class OutputPipeBlock extends GenericPipeBlock {
        OutputPipeBlock(Properties properties, PipeType type) {
            super(properties, type);
            registerDefaultState(defaultBlockState().setValue(OUTPUT, 0));
        }

        @Override
        protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
            super.createBlockStateDefinition(builder);
            builder.add(OUTPUT);
        }

        /** {@code onBlockPlaced}: output 1 (up), then switched once. */
        @Override
        public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
                                ItemStack stack) {
            if (!level.isClientSide()) {
                level.setBlock(pos, state.setValue(OUTPUT, 1), Block.UPDATE_ALL);
                PipeConnections.switchIronOutput(level, pos);
            }
        }

        @Override
        protected boolean onWrench(BlockState state, Level level, BlockPos pos) {
            if (!level.isClientSide()) {
                PipeConnections.switchIronOutput(level, pos);
            }
            return true;
        }
    }
}
