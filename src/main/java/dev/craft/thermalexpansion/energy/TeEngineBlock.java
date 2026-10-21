package dev.craft.thermalexpansion.energy;

import java.util.function.BiFunction;

import com.mojang.serialization.MapCodec;
import dev.craft.thermalexpansion.item.CrescentHammerItem;
import dev.craft.thermalexpansion.machine.TeMachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import org.jetbrains.annotations.Nullable;

/**
 * {@code BlockEngine}: Material.iron, hardness 0.5, drawn by its block entity renderer. Placed facing up, then turned to
 * the first neighbour that takes MJ. A wrench repairs a broken engine, otherwise turns it; sneaking with it dismantles
 * the engine; a filled liquid container fills its tank. Smokes while broken.
 */
public class TeEngineBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");

    private final TeEngineType type;
    private final BiFunction<BlockPos, BlockState, ? extends TeEngineBlockEntity> factory;

    public TeEngineBlock(Properties properties, TeEngineType type,
                         BiFunction<BlockPos, BlockState, ? extends TeEngineBlockEntity> factory) {
        super(properties.lightLevel(state -> state.getValue(ACTIVE) ? 7 : 0));
        this.type = type;
        this.factory = factory;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP).setValue(ACTIVE, false));
    }

    public TeEngineType getEngineType() {
        return type;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, ACTIVE);
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
        return (tickLevel, pos, tickState, be) -> {
            if (be instanceof TeEngineBlockEntity engine) {
                if (tickLevel.isClientSide()) {
                    engine.clientTick();
                } else {
                    engine.serverTick();
                }
            }
        };
    }

    /** {@code onBlockPlacedBy}: {@code setFacing(1); rotateBlock()}. */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
                            ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TeEngineBlockEntity engine) {
            engine.rotate();
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof TeEngineBlockEntity engine)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        boolean wrench = CrescentHammerItem.isWrench(stack);
        if (!player.isShiftKeyDown() && wrench && engine.isShutdown()) {
            if (!level.isClientSide()) {
                engine.repair();
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        if (!wrench && FluidUtil.getFluidContained(stack).isPresent()) {
            IFluidHandler handler = engine.getFluidHandler(null);
            if (handler != null && FluidUtil.interactWithFluidHandler(player, hand, handler)) {
                return ItemInteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        if (!wrench) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide()) {
            if (player.isShiftKeyDown()) {
                TeMachineBlock.dismantle(level, pos);
            } else {
                engine.rotate();
            }
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof TeEngineBlockEntity engine
                && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(engine, buf -> {
                buf.writeVarInt(type.ordinal());
                buf.writeBlockPos(pos);
            });
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    /** {@code randomDisplayTick}: smoke on three sides of a broken engine. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof TeEngineBlockEntity engine) || !engine.isShutdown()) {
            return;
        }
        float x = pos.getX() + 0.5F;
        float y = pos.getY() + random.nextFloat() * 6.0F / 16.0F;
        float z = pos.getZ() + 0.5F;
        float offset = 0.52F;
        float spread = random.nextFloat() * 0.6F - 0.3F;
        level.addParticle(ParticleTypes.SMOKE, x - offset, y, z + spread, 0.0, 0.0, 0.0);
        level.addParticle(ParticleTypes.SMOKE, x + offset, y, z + spread, 0.0, 0.0, 0.0);
        level.addParticle(ParticleTypes.SMOKE, x + spread, y, z - offset, 0.0, 0.0, 0.0);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof TeEngineBlockEntity engine) {
            engine.dropContents();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }
}
