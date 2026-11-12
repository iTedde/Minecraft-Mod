package dev.craft.chargepads.block;

import java.util.List;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.craft.chargepads.ChargePadsConfig;
import dev.craft.chargepads.CpBlockEntityTypes;
import dev.craft.chargepads.PadTier;
import dev.craft.core.CraftDamageTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
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
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Charge pad block (spec 47 §2.1, §4, §6; original {@code f}): flat 16 × 4 × 16 px, {@link #ACTIVE} while a player
 * stands on it (original metadata bit 8). Active pads give a redstone signal of 15 down and to the four sides.
 * Broken without a wrench the pad never drops itself and zaps everyone nearby.
 */
public class ChargePadBlock extends BaseEntityBlock {
    public static final MapCodec<ChargePadBlock> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    propertiesCodec(),
                    com.mojang.serialization.Codec.STRING.xmap(PadTier::valueOf, PadTier::name).fieldOf("tier")
                            .forGetter(ChargePadBlock::getTier))
            .apply(instance, ChargePadBlock::new));
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0);
    /** Field check every 10 ticks while players are found ({@code tickRate}). */
    public static final int CHECK_DELAY = 10;
    /** Player detection box above the pad ({@code g_}): 2 px inset, half a block high. */
    private static final double INSET = 0.125;

    private final PadTier tier;

    public ChargePadBlock(Properties properties, PadTier tier) {
        super(properties);
        this.tier = tier;
        registerDefaultState(stateDefinition.any().setValue(ACTIVE, false));
    }

    public PadTier getTier() {
        return tier;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ACTIVE);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    /**
     * Opens the GUI; with a drain or damage module only from the bottom face (any other face consumes the click).
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ChargePadBlockEntity pad
                && player instanceof ServerPlayer serverPlayer
                && (!pad.hasEmitterModule() || hit.getDirection() == Direction.DOWN)) {
            serverPlayer.openMenu(pad, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    // --- activation (§4) ---

    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!level.isClientSide() && !state.getValue(ACTIVE)) {
            checkField(state, (ServerLevel) level, pos);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(ACTIVE)) {
            checkField(state, level, pos);
        }
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return state.getValue(ACTIVE);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        tick(state, level, pos, random);
    }

    /**
     * Original {@code g_}: players in the small box (grown by the field expansion when active) switch the pad on;
     * without players, or with an empty small box while the pad reported nothing to charge, it switches off.
     */
    private void checkField(BlockState state, ServerLevel level, BlockPos pos) {
        boolean active = state.getValue(ACTIVE);
        AABB box = new AABB(pos.getX() + INSET, pos.getY(), pos.getZ() + INSET,
                pos.getX() + 1 - INSET, pos.getY() + 0.5, pos.getZ() + 1 - INSET);
        List<Player> players = level.getEntitiesOfClass(Player.class, box);
        boolean nothingToCharge = false;
        ChargePadBlockEntity pad = null;
        if (active && level.getBlockEntity(pos) instanceof ChargePadBlockEntity found) {
            pad = found;
            if (players.isEmpty()) {
                nothingToCharge = pad.isNothingToCharge();
            }
            int expansion = pad.getFieldExpansion();
            if (expansion > 0) {
                players = level.getEntitiesOfClass(Player.class, box.inflate(expansion, 0.0, expansion));
            }
        }
        boolean found = !players.isEmpty();
        boolean switched = false;
        if (found && !active) {
            switched = true;
            level.setBlock(pos, state.setValue(ACTIVE, true), Block.UPDATE_ALL);
            playClick(level, pos, 0.6F);
        }
        if (active && (!found || nothingToCharge)) {
            switched = true;
            if (pad != null) {
                pad.setNothingToCharge(false);
            }
            level.setBlock(pos, state.setValue(ACTIVE, false), Block.UPDATE_ALL);
            playClick(level, pos, 0.5F);
        }
        if (switched) {
            updateSignalNeighbours(level, pos);
        }
        if (found) {
            level.scheduleTick(pos, this, CHECK_DELAY);
        }
    }

    private static void playClick(Level level, BlockPos pos, float pitch) {
        level.playSound(null, pos.getX() + 0.5, pos.getY() + 0.25, pos.getZ() + 0.5,
                SoundEvents.STONE_BUTTON_CLICK_ON, SoundSource.BLOCKS, 0.3F, pitch);
    }

    /** Neighbours below and at the four sides (the strong signal also reaches their neighbours). */
    private void updateSignalNeighbours(Level level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (direction != Direction.UP) {
                level.updateNeighborsAt(pos.relative(direction), this);
            }
        }
    }

    // --- redstone (§4) ---

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    /** {@code direction} points from the asking block to the pad: {@code DOWN} = asked from above, no signal. */
    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(ACTIVE) && direction != Direction.DOWN ? 15 : 0;
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return getSignal(state, level, pos, direction);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos,
                                      @Nullable Direction direction) {
        return true;
    }

    // --- dangers (§6) ---

    /** Falling-zap before the normal fall damage (1.4.7 calls {@code onFallenUpon} before {@code fall}). */
    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        if (!level.isClientSide() && ChargePadsConfig.FALLING_ZAP && tier.isZappingFall(fallDistance)
                && entity instanceof LivingEntity living) {
            living.hurt(level.damageSources().source(CraftDamageTypes.ELECTROCUTION), tier.getFallDamage());
        }
        super.fallOn(level, state, pos, entity, fallDistance);
    }

    /** {@code removeBlockByPlayer}: non-creative players get the discharge zap and lose part of the inventory. */
    @Override
    public boolean onDestroyedByPlayer(BlockState state, Level level, BlockPos pos, Player player, boolean willHarvest,
                                       FluidState fluid) {
        if (!level.isClientSide() && !player.getAbilities().instabuild
                && level.getBlockEntity(pos) instanceof ChargePadBlockEntity pad) {
            pad.onBrokenByPlayer(player);
        }
        return super.onDestroyedByPlayer(state, level, pos, player, willHarvest, fluid);
    }

    /** {@code breakBlock}: the inventory always falls out (spread 0.7, pickup delay 10). */
    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof ChargePadBlockEntity pad) {
            for (int slot = 0; slot < ChargePadBlockEntity.INVENTORY_SIZE; slot++) {
                dropScattered(level, pos, pad.getInventory().getStackInSlot(slot));
            }
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    public static void dropScattered(Level level, BlockPos pos, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        double spread = 0.7;
        double x = level.random.nextFloat() * spread + (1.0 - spread) * 0.5;
        double y = level.random.nextFloat() * spread + (1.0 - spread) * 0.5;
        double z = level.random.nextFloat() * spread + (1.0 - spread) * 0.5;
        ItemEntity item = new ItemEntity(level, pos.getX() + x, pos.getY() + y, pos.getZ() + z, stack.copy());
        item.setPickUpDelay(10);
        level.addFreshEntity(item);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ChargePadBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                          BlockEntityType<T> type) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(type, CpBlockEntityTypes.CHARGE_PAD.get(), (l, p, s, pad) -> pad.serverTick());
    }
}
