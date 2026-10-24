package dev.craft.buildcraft.engine;

import com.mojang.serialization.MapCodec;
import dev.craft.buildcraft.item.ToolWrench;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
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
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/** BuildCraft {@code BlockEngine}: rendered by the block entity renderer, the output side is {@link #FACING}. */
public class EngineBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    /** Only the side opposite the output is solid (BuildCraft {@code isBlockSolidOnSide}). */
    private static final VoxelShape[] SUPPORT = {
            box(0, 12, 0, 16, 16, 16), box(0, 0, 0, 16, 4, 16), box(0, 0, 12, 16, 16, 16),
            box(0, 0, 0, 16, 16, 4), box(12, 0, 0, 16, 16, 16), box(0, 0, 0, 4, 16, 16)};

    private final EngineType type;

    public EngineBlock(Properties properties, EngineType type) {
        super(properties);
        this.type = type;
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.UP));
    }

    public EngineType getEngineType() {
        return type;
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
        return type.create(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return (tickLevel, pos, tickState, be) -> {
                if (be instanceof EngineBlockEntity engine) {
                    engine.clientTick();
                }
            };
        }
        return (tickLevel, pos, tickState, be) -> {
            if (be instanceof EngineBlockEntity engine) {
                engine.serverTick();
            }
        };
    }

    /** BuildCraft {@code onPostBlockPlaced}: output up, then turned to the first adjacent MJ receiver. */
    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
                            ItemStack stack) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof EngineBlockEntity engine) {
            engine.switchOrientation();
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!player.isShiftKeyDown() && ToolWrench.canWrenchWith(stack, player, pos)) {
            if (!level.isClientSide() && level.getBlockEntity(pos) instanceof EngineBlockEntity engine) {
                engine.switchOrientation();
            }
            ToolWrench.wrenchUsedWith(stack, player, pos);
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (player.isShiftKeyDown() || !type.hasMenu()) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof EngineBlockEntity engine
                && engine instanceof MenuProvider menu
                && player instanceof ServerPlayer serverPlayer) {
            serverPlayer.openMenu(menu, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   BlockPos neighborPos, boolean movedByPiston) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof EngineBlockEntity engine) {
            engine.checkRedstonePower();
        }
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState,
                            boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof EngineBlockEntity engine) {
            engine.onBlockBreak();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    /** BuildCraft {@code randomDisplayTick}: red dust on all four sides while burning. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!(level.getBlockEntity(pos) instanceof EngineBlockEntity engine) || !engine.isClientBurning()) {
            return;
        }
        float x = pos.getX() + 0.5F;
        float y = pos.getY() + random.nextFloat() * 6.0F / 16.0F;
        float z = pos.getZ() + 0.5F;
        float offset = 0.52F;
        float spread = random.nextFloat() * 0.6F - 0.3F;
        level.addParticle(DustParticleOptions.REDSTONE, x - offset, y, z + spread, 0.0, 0.0, 0.0);
        level.addParticle(DustParticleOptions.REDSTONE, x + offset, y, z + spread, 0.0, 0.0, 0.0);
        level.addParticle(DustParticleOptions.REDSTONE, x + spread, y, z - offset, 0.0, 0.0, 0.0);
        level.addParticle(DustParticleOptions.REDSTONE, x + spread, y, z + offset, 0.0, 0.0, 0.0);
    }
}
