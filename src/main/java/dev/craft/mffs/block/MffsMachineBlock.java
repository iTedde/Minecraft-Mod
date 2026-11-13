package dev.craft.mffs.block;

import com.mojang.serialization.MapCodec;
import dev.craft.mffs.item.BlankCardItem;
import dev.craft.mffs.item.PowerLinkCardItem;
import dev.craft.mffs.item.ProjectorModuleItem;
import dev.craft.mffs.item.multitool.MultitoolItem;
import dev.craft.mffs.security.MffsSecurity;
import dev.craft.mffs.security.SecurityRight;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
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
import net.minecraft.world.phys.BlockHitResult;

/**
 * {@code BlockMFFSBase} (spec 48 §5): unbreakable by hand (only the multitool or an IC2 wrench removes it), explosion
 * resistance 100 (999 while active), facing towards the player at placement (looking ≥ 65° up/down: up/down),
 * texture front/back/rest with active variants.
 */
public class MffsMachineBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private final BiFunction<BlockPos, BlockState, ? extends MffsMachineBlockEntity> factory;

    public MffsMachineBlock(Properties properties,
                            BiFunction<BlockPos, BlockState, ? extends MffsMachineBlockEntity> factory) {
        super(properties);
        this.factory = factory;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(ACTIVE, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        throw new UnsupportedOperationException("MFFS machines are not data driven");
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE);
    }

    /** {@code onBlockPlacedBy}: pitch ≥ 65 → up, ≤ −65 → down, else the horizontal side facing the player. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return defaultBlockState();
        }
        return defaultBlockState().setValue(FACING, placementFacing(player.getYRot(), player.getXRot()));
    }

    public static Direction placementFacing(float yaw, float pitch) {
        int rounded = Math.round(pitch);
        if (rounded >= 65) {
            return Direction.UP;
        }
        if (rounded <= -65) {
            return Direction.DOWN;
        }
        return switch (Mth.floor(yaw * 4.0F / 360.0F + 0.5) & 3) {
            case 0 -> Direction.NORTH;
            case 1 -> Direction.EAST;
            case 2 -> Direction.SOUTH;
            default -> Direction.WEST;
        };
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return factory.apply(pos, state);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return (tickLevel, pos, tickState, blockEntity) -> {
            if (blockEntity instanceof MffsMachineBlockEntity machine) {
                machine.serverTick();
            }
        };
    }

    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return state.getValue(ACTIVE) ? 999.0F : 100.0F;
    }

    /**
     * Items that act on the machine themselves keep the GUI closed: multitool, blank card, power link card, lever
     * module (later also security link cards).
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (stack.getItem() instanceof MultitoolItem || stack.getItem() instanceof BlankCardItem
                || stack.getItem() instanceof PowerLinkCardItem || stack.getItem() instanceof ProjectorModuleItem
                || stack.is(Items.LEVER)) {
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
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
        if (level.getBlockEntity(pos) instanceof MffsMachineBlockEntity machine && player instanceof ServerPlayer
                && MffsSecurity.checkAccess(machine, player, SecurityRight.EB)) {
            player.openMenu(machine, buf -> buf.writeBlockPos(pos));
        }
        return InteractionResult.CONSUME;
    }

    /** {@code breakBlock}: the slots fall out (data cards as blank cards), the forced chunk is released. */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level instanceof ServerLevel serverLevel
                && level.getBlockEntity(pos) instanceof MffsMachineBlockEntity machine) {
            machine.dropPlugins();
            machine.releaseChunk(serverLevel);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
