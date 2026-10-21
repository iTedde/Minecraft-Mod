package dev.craft.thermalexpansion.transport;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiFunction;

import com.mojang.serialization.MapCodec;
import dev.craft.thermalexpansion.item.CrescentHammerItem;
import dev.craft.thermalexpansion.machine.TeMachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * {@code BlockConduit}: Material.glass, hardness 3, resistance 90; a 4–12 px core with an arm per connected side, drawn
 * by its block entity renderer. The wrench joins liquiducts on the clicked side ({@code wrenchAction}) or switches a
 * node between output and input; sneaking with it dismantles the conduit.
 */
public class ConduitBlock extends BaseEntityBlock {
    /** Light of the liquiduct (its liquid); the energy conduit always has 7. */
    public static final IntegerProperty LIGHT = IntegerProperty.create("light", 0, 15);
    private static final Map<Direction, BooleanProperty> CONNECTIONS = new EnumMap<>(Direction.class);
    private static final VoxelShape CORE = Block.box(4.0, 4.0, 4.0, 12.0, 12.0, 12.0);
    private static final Map<Direction, VoxelShape> ARMS = new EnumMap<>(Direction.class);

    static {
        for (Direction direction : Direction.values()) {
            CONNECTIONS.put(direction, BooleanProperty.create(direction.getSerializedName()));
        }
        ARMS.put(Direction.DOWN, Block.box(4.0, 0.0, 4.0, 12.0, 4.0, 12.0));
        ARMS.put(Direction.UP, Block.box(4.0, 12.0, 4.0, 12.0, 16.0, 12.0));
        ARMS.put(Direction.NORTH, Block.box(4.0, 4.0, 0.0, 12.0, 12.0, 4.0));
        ARMS.put(Direction.SOUTH, Block.box(4.0, 4.0, 12.0, 12.0, 12.0, 16.0));
        ARMS.put(Direction.WEST, Block.box(0.0, 4.0, 4.0, 4.0, 12.0, 12.0));
        ARMS.put(Direction.EAST, Block.box(12.0, 4.0, 4.0, 16.0, 12.0, 12.0));
    }

    private final boolean energy;
    private final BiFunction<BlockPos, BlockState, ? extends ConduitBlockEntity> factory;

    public ConduitBlock(Properties properties, boolean energy,
                        BiFunction<BlockPos, BlockState, ? extends ConduitBlockEntity> factory) {
        super(properties.lightLevel(state -> energy ? 7 : state.getValue(LIGHT)));
        this.energy = energy;
        this.factory = factory;
        BlockState state = stateDefinition.any().setValue(LIGHT, 0);
        for (BooleanProperty property : CONNECTIONS.values()) {
            state = state.setValue(property, false);
        }
        registerDefaultState(state);
    }

    public static BooleanProperty connection(Direction direction) {
        return CONNECTIONS.get(direction);
    }

    public boolean isEnergy() {
        return energy;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIGHT);
        CONNECTIONS.values().forEach(builder::add);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        VoxelShape shape = CORE;
        for (Direction direction : Direction.values()) {
            if (state.getValue(connection(direction))) {
                shape = Shapes.or(shape, ARMS.get(direction));
            }
        }
        return shape;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return factory.apply(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return null;
        }
        return (tickLevel, pos, tickState, be) -> {
            if (be instanceof ConduitBlockEntity conduit) {
                conduit.serverTick();
            }
        };
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
                            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ConduitBlockEntity conduit) {
            conduit.onNeighborBlockChange();
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ConduitBlockEntity conduit) {
            conduit.onNeighborBlockChange();
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!CrescentHammerItem.isWrench(stack)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ConduitBlockEntity conduit) {
            if (player.isShiftKeyDown()) {
                TeMachineBlock.dismantle(level, pos);
            } else if (!conduit.wrenchAction(hit.getDirection())) {
                conduit.changeMode();
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }
}
