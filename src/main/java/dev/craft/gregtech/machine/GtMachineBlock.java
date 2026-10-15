package dev.craft.gregtech.machine;

import java.util.function.BiConsumer;

import com.mojang.serialization.MapCodec;
import dev.craft.gregtech.GtMachine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * Block of a GregTech machine ({@code GT_BlockMetaID_Machine}). {@link #FACING} is GT's {@code mFacing} (set by the
 * wrench, all six sides), {@link #FRONT} the fixed {@code mMainFacing} that carries the front texture.
 */
public class GtMachineBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final EnumProperty<Front> FRONT = EnumProperty.create("front", Front.class);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private final GtMachine machine;

    public GtMachineBlock(Properties properties, GtMachine machine) {
        super(properties);
        this.machine = machine;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(FRONT, Front.NONE)
                .setValue(ACTIVE, false));
    }

    public GtMachine getMachine() {
        return machine;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FRONT, ACTIVE);
    }

    /**
     * {@code onBlockPlacedBy}: up/down by pitch only if the facing is valid, which a fresh basic machine (no main
     * facing yet) never allows – so it always faces the placer horizontally; that side becomes the front. Other
     * machines have no valid facing at all.
     */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        if (!machine.isBasicMachine()) {
            return defaultBlockState();
        }
        Player player = context.getPlayer();
        Direction facing = player == null ? Direction.DOWN : player.getDirection().getOpposite();
        return defaultBlockState().setValue(FACING, facing).setValue(FRONT, Front.of(facing));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return machine.getFactory().apply(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return (tickLevel, pos, tickState, be) -> {
            if (be instanceof GtMachineBlockEntity machineEntity) {
                machineEntity.serverTick();
            }
        };
    }

    /** Rightclick with an item: first try to install it as upgrade, otherwise open the GUI. */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof GtMachineBlockEntity be
                && be.isAccessAllowed(player) && be.installUpgrade(player, stack)) {
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof GtMachineBlockEntity be && be.isAccessAllowed(player)
                && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(be, pos);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    /** {@code removeBlockByPlayer}: survival players set off the stored energy; an exploded machine drops nothing. */
    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest,
                                       FluidState fluid) {
        if (!level.isClientSide() && !player.getAbilities().instabuild
                && level.getBlockEntity(pos) instanceof GtMachineBlockEntity be && be.doEnergyExplosion()) {
            return false;
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    /**
     * {@code dropBlockAsItemWithChance} below chance 1 (any explosion larger than 1): no machine parts, the stored
     * energy explodes instead; the inventory still drops when the block is removed.
     */
    @Override
    protected void onExplosionHit(BlockState state, Level level, BlockPos pos, Explosion explosion,
                                  BiConsumer<ItemStack, BlockPos> dropConsumer) {
        if (explosion.getBlockInteraction() == Explosion.BlockInteraction.TRIGGER_BLOCK
                || !(level instanceof ServerLevel) || explosion.radius() <= 1.0F) {
            super.onExplosionHit(state, level, pos, explosion, dropConsumer);
            return;
        }
        if (level.getBlockEntity(pos) instanceof GtMachineBlockEntity be) {
            be.doEnergyExplosion();
        }
        if (level.getBlockState(pos).is(this)) {
            state.onBlockExploded(level, pos, explosion);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof GtMachineBlockEntity be) {
            be.dropContents();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction side) {
        return true;
    }

    // --- machines_flammable ---

    @Override
    public boolean isFlammable(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return true;
    }

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 0;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return 100;
    }

    @Override
    public boolean isFireSource(BlockState state, net.minecraft.world.level.LevelReader level, BlockPos pos,
                                Direction direction) {
        return true;
    }

    /** {@code mMainFacing}: none until the machine first faces a horizontal side. */
    public enum Front implements StringRepresentable {
        NONE("none", null),
        NORTH("north", Direction.NORTH),
        SOUTH("south", Direction.SOUTH),
        WEST("west", Direction.WEST),
        EAST("east", Direction.EAST);

        private final String name;
        @Nullable
        private final Direction direction;

        Front(String name, @Nullable Direction direction) {
            this.name = name;
            this.direction = direction;
        }

        @Nullable
        public Direction getDirection() {
            return direction;
        }

        public static Front of(Direction direction) {
            return switch (direction) {
                case NORTH -> NORTH;
                case SOUTH -> SOUTH;
                case WEST -> WEST;
                case EAST -> EAST;
                default -> NONE;
            };
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }
}
