package dev.craft.secretrooms;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Camouflaged lever, button and pressure plates ({@code BlockCamoLever}, {@code BlockCamoButton},
 * {@code BlockCamoPlate}; spec 50 §4): full camouflaged blocks that switch like their vanilla model. Weak signal 15 to
 * every side while on, strong signal into the block they were placed against ({@link #ATTACHED}); the plates press
 * when stood on and check again every 20 ticks.
 */
public class CamoSwitchBlock extends CamoBlock {
    public enum Kind { LEVER, BUTTON, PLATE_ALL, PLATE_PLAYERS }

    public static final MapCodec<CamoSwitchBlock> CODEC = simpleCodec(p -> new CamoSwitchBlock(p, Kind.LEVER));
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    /** Towards the block it was placed against. */
    public static final DirectionProperty ATTACHED = BlockStateProperties.FACING;
    public static final int DELAY = 20;

    private final Kind kind;

    public CamoSwitchBlock(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
        registerDefaultState(stateDefinition.any().setValue(POWERED, false).setValue(ATTACHED, Direction.DOWN));
    }

    public Kind kind() {
        return kind;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWERED, ATTACHED);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(ATTACHED, context.getClickedFace().getOpposite());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
            BlockHitResult hit) {
        if (kind != Kind.LEVER && kind != Kind.BUTTON) {
            return InteractionResult.PASS;
        }
        if (kind == Kind.BUTTON && state.getValue(POWERED)) {
            return InteractionResult.CONSUME;
        }
        if (!level.isClientSide()) {
            boolean on = kind == Kind.BUTTON || !state.getValue(POWERED);
            setPowered(level, pos, state, on);
            if (kind == Kind.BUTTON) {
                level.scheduleTick(pos, this, DELAY);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private void setPowered(Level level, BlockPos pos, BlockState state, boolean on) {
        level.setBlock(pos, state.setValue(POWERED, on), Block.UPDATE_ALL);
        level.playSound(null, pos, kind == Kind.LEVER ? SoundEvents.LEVER_CLICK : SoundEvents.STONE_BUTTON_CLICK_ON,
                SoundSource.BLOCKS, 0.3F, on ? 0.6F : 0.5F);
        level.updateNeighborsAt(pos, this);
        level.updateNeighborsAt(pos.relative(state.getValue(ATTACHED)), this);
    }

    /** Plates: something stepping on the top. */
    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide() && (kind == Kind.PLATE_ALL || kind == Kind.PLATE_PLAYERS)
                && !state.getValue(POWERED) && triggers(entity)) {
            setPowered(level, pos, state, true);
            level.scheduleTick(pos, this, DELAY);
        }
        super.stepOn(level, pos, state, entity);
    }

    private boolean triggers(Entity entity) {
        return !entity.isSpectator() && !entity.isIgnoringBlockTriggers()
                && (kind == Kind.PLATE_ALL || entity instanceof Player);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(POWERED)) {
            return;
        }
        if (kind == Kind.PLATE_ALL || kind == Kind.PLATE_PLAYERS) {
            AABB top = new AABB(pos.getX(), pos.getY() + 1.0, pos.getZ(), pos.getX() + 1.0, pos.getY() + 1.25,
                    pos.getZ() + 1.0);
            if (!level.getEntities((Entity) null, top, this::triggers).isEmpty()) {
                level.scheduleTick(pos, this, DELAY);
                return;
            }
        }
        if (kind != Kind.LEVER) {
            setPowered(level, pos, state, false);
        }
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) && state.getValue(ATTACHED) == direction.getOpposite() ? 15 : 0;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean moving) {
        if (!state.is(newState.getBlock()) && state.getValue(POWERED)) {
            level.updateNeighborsAt(pos, this);
            level.updateNeighborsAt(pos.relative(state.getValue(ATTACHED)), this);
        }
        super.onRemove(state, level, pos, newState, moving);
    }
}
