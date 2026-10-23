package dev.craft.enderstorage;

import java.util.EnumMap;
import java.util.Map;
import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.fluids.FluidUtil;

/**
 * EnderStorage {@code BlockEnderStorage} for the tank (spec 27 §2, §5): like the chest, but the body takes and fills
 * liquid containers, part 4 is the pressure valve knob, it connects to redstone and glows with its liquid.
 */
public class EnderTankBlock extends BaseEntityBlock {
    public static final MapCodec<EnderTankBlock> CODEC = simpleCodec(EnderTankBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    /** {@code getLightValue}: light of the stored liquid, set by the server. */
    public static final IntegerProperty LIGHT = BlockStateProperties.LEVEL;

    public static final int PART_BODY = 0;
    public static final int PART_VALVE = 4;

    /**
     * Parts of the tank facing south ({@code addTraceableCuboids}, valve to the front as drawn): body, the buttons
     * ({@code selection_button} × 0.8) and the valve knob.
     */
    private static final AABB[] PARTS = {
            new AABB(0.15, 0.0, 0.15, 0.85, 0.916, 0.85),
            button(0), button(1), button(2),
            new AABB(0.338, 0.268, 0.8, 0.642, 0.565, 0.9)};
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    static {
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            VoxelShape shape = Shapes.empty();
            for (AABB part : PARTS) {
                shape = Shapes.or(shape, Shapes.create(EnderChestBlock.toWorld(part, facing)));
            }
            SHAPES.put(facing, shape.optimize());
        }
    }

    public EnderTankBlock(Properties properties) {
        super(properties.lightLevel(state -> state.getValue(LIGHT)));
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIGHT, 0));
    }

    private static AABB button(int index) {
        double x = 0.35 + 0.15 * index;
        return new AABB(x - 0.05, 0.91, 0.4, x + 0.05, 0.96, 0.6);
    }

    /** The part hit at {@code location}. */
    public static int hitPart(BlockState state, BlockPos pos, Vec3 location) {
        Vec3 local = EnderChestBlock.turn(location.subtract(pos.getX(), pos.getY(), pos.getZ()),
                (4 - state.getValue(FACING).get2DDataValue()) % 4);
        for (int part = 1; part < PARTS.length; part++) {
            if (PARTS[part].inflate(1.0E-3).contains(local)) {
                return part;
            }
        }
        return PART_BODY;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIGHT);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    /** {@code onPlaced}: the valve faces the player. */
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    /** Material rock: without a pickaxe 1/100 instead of 1/30 of the dig speed per tick, but it always drops. */
    @Override
    protected float getDestroyProgress(BlockState state, Player player, BlockGetter level, BlockPos pos) {
        float hardness = state.getDestroySpeed(level, pos);
        if (hardness < 0.0F) {
            return 0.0F;
        }
        boolean pickaxe = player.getMainHandItem().isCorrectToolForDrops(state);
        return player.getDigSpeed(state, pos) / hardness / (pickaxe ? 30.0F : 100.0F);
    }

    /** {@code redstoneInteraction}: wires connect to the tank. */
    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        return true;
    }

    /**
     * Diamond on the valve of a global tank makes it personal, dye on a button changes that colour, anything else on
     * the valve turns it; elsewhere a liquid container is emptied into the tank or filled from it.
     */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        int part = hitPart(state, pos, hit.getLocation());
        DyeColor dye = DyeColor.getColor(stack);
        if (level.isClientSide()) {
            boolean handled = part == PART_VALVE || part != PART_BODY && dye != null
                    || FluidUtil.getFluidHandler(stack).isPresent();
            return handled ? ItemInteractionResult.SUCCESS : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!(level.getBlockEntity(pos) instanceof EnderTankBlockEntity tank)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        Frequency frequency = tank.getFrequency();
        if (part == PART_VALVE) {
            if (stack.is(Items.DIAMOND) && !frequency.isPersonal()) {
                tank.setFrequency(frequency.withOwner(player));
                stack.consume(1, player);
            } else {
                tank.invert();
            }
            return ItemInteractionResult.SUCCESS;
        }
        if (part != PART_BODY && dye != null) {
            if (frequency.colour(part - 1) == dye.getId()) {
                return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
            }
            tank.setFrequency(frequency.withColour(part - 1, dye.getId()));
            stack.consume(1, player);
            return ItemInteractionResult.SUCCESS;
        }
        // activate: body and buttons (without dye) take or fill containers.
        if (FluidUtil.interactWithFluidHandler(player, hand, tank.getStorage())) {
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /**
     * On the valve: sneaking on a personal tank makes it global and hands out the diamond (no owner check, as in the
     * original), otherwise the valve turns.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (hitPart(state, pos, hit.getLocation()) != PART_VALVE) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof EnderTankBlockEntity tank)) {
            return InteractionResult.PASS;
        }
        Frequency frequency = tank.getFrequency();
        if (player.isSecondaryUseActive() && frequency.isPersonal()) {
            if (!player.getAbilities().instabuild && !player.getInventory().add(new ItemStack(Items.DIAMOND))) {
                return InteractionResult.PASS;
            }
            tank.setFrequency(frequency.global());
            return InteractionResult.SUCCESS;
        }
        tank.invert();
        return InteractionResult.SUCCESS;
    }

    /** {@code getPickBlock}: the tank with its frequency and owner. */
    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(level, pos, state);
        if (level.getBlockEntity(pos) instanceof EnderTankBlockEntity tank) {
            stack.set(EsDataComponents.FREQUENCY.get(), tank.getFrequency());
        }
        return stack;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnderTankBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return createTickerHelper(type, EsBlockEntityTypes.ENDER_TANK.get(), level.isClientSide()
                ? (l, p, s, tank) -> tank.clientTick() : (l, p, s, tank) -> tank.serverTick());
    }
}
