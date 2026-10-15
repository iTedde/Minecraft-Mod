package dev.craft.forestry.engine;

import com.mojang.serialization.MapCodec;
import dev.craft.buildcraft.item.BcWrenchItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.joml.Vector3f;
import org.jetbrains.annotations.Nullable;

/**
 * Forestry {@code BlockEngine}: rendered by the block entity renderer, the output side is {@link #FACING}. Starts
 * facing west and turns to the first adjacent MJ receiver when placed or hit with a BuildCraft wrench.
 */
public class ElectricalEngineBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    /** Only the side opposite the output is solid ({@code isBlockSolidOnSide}). */
    private static final VoxelShape[] SUPPORT = {
            box(0, 12, 0, 16, 16, 16), box(0, 0, 0, 16, 4, 16), box(0, 0, 12, 16, 16, 16),
            box(0, 0, 0, 16, 16, 4), box(12, 0, 0, 16, 16, 16), box(0, 0, 0, 4, 16, 16)};
    /** {@code EntityBiodustFX}: redstone dust forced to pure green. */
    private static final DustParticleOptions BIODUST = new DustParticleOptions(new Vector3f(0.0F, 1.0F, 0.0F), 1.0F);

    public ElectricalEngineBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.WEST));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return MapCodec.unit(this);
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
    protected VoxelShape getBlockSupportShape(BlockState state, BlockGetter level, BlockPos pos) {
        return SUPPORT[state.getValue(FACING).get3DDataValue()];
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ElectricalEngineBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.isShiftKeyDown() && stack.getItem() instanceof BcWrenchItem) {
            if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ElectricalEngineBlockEntity engine) {
                engine.rotateEngine();
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ElectricalEngineBlockEntity engine) {
            player.openMenu(engine, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof ElectricalEngineBlockEntity engine) {
            engine.dropContents();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return (tickLevel, pos, tickState, be) -> {
                if (be instanceof ElectricalEngineBlockEntity engine) {
                    engine.clientTick();
                }
            };
        }
        return (tickLevel, pos, tickState, be) -> {
            if (be instanceof ElectricalEngineBlockEntity engine) {
                engine.serverTick();
            }
        };
    }

    /** {@code randomDisplayTick}: green dust on all four sides while powered by redstone. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!level.hasNeighborSignal(pos)) {
            return;
        }
        float x = pos.getX() + 0.5F;
        float y = pos.getY() + random.nextFloat() * 6.0F / 16.0F;
        float z = pos.getZ() + 0.5F;
        float offset = 0.52F;
        float spread = random.nextFloat() * 0.6F - 0.3F;
        level.addParticle(BIODUST, x - offset, y, z + spread, 0.0, 0.0, 0.0);
        level.addParticle(BIODUST, x + offset, y, z + spread, 0.0, 0.0, 0.0);
        level.addParticle(BIODUST, x + spread, y, z - offset, 0.0, 0.0, 0.0);
        level.addParticle(BIODUST, x + spread, y, z + offset, 0.0, 0.0, 0.0);
    }
}
