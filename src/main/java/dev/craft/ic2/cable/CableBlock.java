package dev.craft.ic2.cable;

import java.util.Locale;
import java.util.Map;

import com.mojang.serialization.MapCodec;
import dev.craft.core.energy.EnergyConductor;
import dev.craft.core.energy.EnergySink;
import dev.craft.core.energy.EnergySource;
import dev.craft.ic2.Ic2BlockEntityTypes;
import dev.craft.ic2.Ic2Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.event.EventHooks;

/**
 * One block per {@link CableType}. The block state holds the visual connections and the construction foam state;
 * paint color and foam color are kept in the {@link CableBlockEntity}.
 */
public class CableBlock extends BaseEntityBlock {
    public static final Map<Direction, BooleanProperty> CONNECTIONS = PipeBlock.PROPERTY_BY_DIRECTION;
    public static final EnumProperty<Foam> FOAM = EnumProperty.create("foam", Foam.class);

    public static final float HARDNESS = 0.2F;
    public static final float WET_FOAM_HARDNESS = 0.01F;
    public static final float HARD_FOAM_HARDNESS = 3.0F;
    public static final float EXPLOSION_RESISTANCE = 6.0F;
    public static final float HARD_FOAM_EXPLOSION_RESISTANCE = 90.0F;

    private final CableType type;
    private final VoxelShape[] shapes = new VoxelShape[64];

    public CableBlock(CableType type, Properties properties) {
        super(properties);
        this.type = type;
        BlockState state = stateDefinition.any().setValue(FOAM, Foam.NONE);
        for (BooleanProperty property : CONNECTIONS.values()) {
            state = state.setValue(property, false);
        }
        registerDefaultState(state);
        for (int mask = 0; mask < shapes.length; mask++) {
            shapes[mask] = createShape(mask, type.getThickness() / 2.0);
        }
    }

    public CableType getCableType() {
        return type;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        throw new UnsupportedOperationException("cable blocks are not data driven");
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FOAM);
        CONNECTIONS.values().forEach(builder::add);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CableBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> blockEntityType) {
        boolean ticks = type == CableType.DETECTOR || type == CableType.SPLITTER || state.getValue(FOAM) == Foam.WET;
        return level.isClientSide || !ticks ? null
                : createTickerHelper(blockEntityType, Ic2BlockEntityTypes.CABLE.get(), CableBlockEntity::serverTick);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    // --- connections ---

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return withConnections(defaultBlockState(), context.getLevel(), context.getClickedPos(), null);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        return state.setValue(CONNECTIONS.get(direction), connects(level, pos, direction, null));
    }

    private static BlockState withConnections(BlockState state, BlockGetter level, BlockPos pos,
                                              CableBlockEntity self) {
        for (Direction direction : Direction.values()) {
            state = state.setValue(CONNECTIONS.get(direction), connects(level, pos, direction, self));
        }
        return state;
    }

    private static boolean connects(BlockGetter level, BlockPos pos, Direction direction, CableBlockEntity self) {
        BlockEntity neighbor = level.getBlockEntity(pos.relative(direction));
        if (neighbor == null) {
            return false;
        }
        if (self == null && level.getBlockEntity(pos) instanceof CableBlockEntity cable) {
            self = cable;
        }
        if (self != null) {
            return self.canInteractWith(neighbor);
        }
        // placement: the new cable is unpainted and connects to every energy tile
        return neighbor instanceof CableBlockEntity
                || neighbor instanceof EnergySink
                || neighbor instanceof EnergySource
                || neighbor instanceof EnergyConductor;
    }

    /** Recomputes the connections of the cable at {@code pos} and of its neighbours (after painting). */
    static void refreshConnections(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        BlockState updated = withConnections(state, level, pos, null);
        if (updated != state) {
            level.setBlock(pos, updated, Block.UPDATE_ALL);
        }
        updated.updateNeighbourShapes(level, pos, Block.UPDATE_ALL);
    }

    // --- shapes ---

    private static int connectionMask(BlockState state) {
        int mask = 0;
        for (Direction direction : Direction.values()) {
            if (state.getValue(CONNECTIONS.get(direction))) {
                mask |= 1 << direction.ordinal();
            }
        }
        return mask;
    }

    /** IC2 uses one box spanning the core and all connected sides, not the union of the arms. */
    private static VoxelShape createShape(int mask, double half) {
        double minX = 0.5 - half, minY = 0.5 - half, minZ = 0.5 - half;
        double maxX = 0.5 + half, maxY = 0.5 + half, maxZ = 0.5 + half;
        if ((mask & 1 << Direction.WEST.ordinal()) != 0) minX = 0;
        if ((mask & 1 << Direction.DOWN.ordinal()) != 0) minY = 0;
        if ((mask & 1 << Direction.NORTH.ordinal()) != 0) minZ = 0;
        if ((mask & 1 << Direction.EAST.ordinal()) != 0) maxX = 1;
        if ((mask & 1 << Direction.UP.ordinal()) != 0) maxY = 1;
        if ((mask & 1 << Direction.SOUTH.ordinal()) != 0) maxZ = 1;
        return Shapes.box(minX, minY, minZ, maxX, maxY, maxZ);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return state.getValue(FOAM) != Foam.NONE ? Shapes.block() : shapes[connectionMask(state)];
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                           CollisionContext context) {
        return state.getValue(FOAM) == Foam.HARD ? Shapes.block() : shapes[connectionMask(state)];
    }

    @Override
    protected VoxelShape getOcclusionShape(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(FOAM) == Foam.HARD ? Shapes.block() : Shapes.empty();
    }

    @Override
    protected int getLightBlock(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(FOAM) == Foam.HARD ? level.getMaxLightLevel() : 0;
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return state.getValue(FOAM) != Foam.HARD;
    }

    // --- hardness and resistance ---

    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        float hardness = switch (state.getValue(FOAM)) {
            case NONE -> HARDNESS;
            case WET -> WET_FOAM_HARDNESS;
            case HARD -> HARD_FOAM_HARDNESS;
        };
        int divisor = EventHooks.doPlayerHarvestCheck(player, state, level, pos) ? 30 : 100;
        return player.getDigSpeed(state, pos) / hardness / divisor;
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return state.getValue(FOAM) == Foam.HARD ? HARD_FOAM_EXPLOSION_RESISTANCE : EXPLOSION_RESISTANCE;
    }

    // --- foam ---

    /** Breaking a foamed cable only removes the foam. */
    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player,
                                       boolean willHarvest, FluidState fluid) {
        if (state.getValue(FOAM) != Foam.NONE) {
            if (!level.isClientSide) {
                setFoam(level, pos, Foam.NONE);
                level.updateNeighborsAt(pos, this);
            }
            return false;
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.is(Items.SAND) && state.getValue(FOAM) == Foam.WET) {
            if (!level.isClientSide) {
                setFoam(level, pos, Foam.HARD);
                stack.consume(1, player);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hit);
    }

    /**
     * Changes the foam state (IC2 {@code changeFoam}); wet foam gets the light gray foam color.
     *
     * @return whether the state changed
     */
    public static boolean setFoam(Level level, BlockPos pos, Foam foam) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof CableBlock) || state.getValue(FOAM) == foam) {
            return false;
        }
        level.setBlock(pos, state.setValue(FOAM, foam), Block.UPDATE_ALL);
        if (foam == Foam.WET && level.getBlockEntity(pos) instanceof CableBlockEntity cable) {
            cable.setFoamColor(CableBlockEntity.WET_FOAM_COLOR);
        }
        return true;
    }

    // --- insulation ---

    /**
     * Replaces the cable with {@code newType}, keeping connections, foam and colors (insulation cutter).
     *
     * @return whether the type changed
     */
    public static boolean changeType(Level level, BlockPos pos, CableType newType) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof CableBlock block) || block.type == newType
                || !(level.getBlockEntity(pos) instanceof CableBlockEntity old)) {
            return false;
        }
        int color = old.getColor();
        int foamColor = old.getFoamColor();
        BlockState newState = Ic2Blocks.cable(newType).get().defaultBlockState().setValue(FOAM, state.getValue(FOAM));
        for (BooleanProperty property : CONNECTIONS.values()) {
            newState = newState.setValue(property, state.getValue(property));
        }
        level.setBlock(pos, newState, Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof CableBlockEntity cable) {
            cable.copyFrom(color, foamColor);
        }
        return true;
    }

    // --- redstone ---

    @Override
    protected boolean isSignalSource(BlockState state) {
        return type == CableType.DETECTOR;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return type == CableType.DETECTOR && level.getBlockEntity(pos) instanceof CableBlockEntity cable
                && cable.isActive() ? 15 : 0;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return type == CableType.DETECTOR || type == CableType.SPLITTER;
    }

    public enum Foam implements StringRepresentable {
        NONE, WET, HARD;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
