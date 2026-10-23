package dev.craft.enderstorage;

import java.util.EnumMap;
import java.util.Map;
import javax.annotation.Nullable;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.SimpleMenuProvider;
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
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * EnderStorage {@code BlockEnderStorage} for the chest (spec 27 §2): stone, hardness 20, always drops itself with its
 * frequency; right clicks are told apart by the part of the chest that was hit (body, colour buttons 1–3, latch).
 */
public class EnderChestBlock extends BaseEntityBlock {
    public static final MapCodec<EnderChestBlock> CODEC = simpleCodec(EnderChestBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public static final int PART_BODY = 0;
    public static final int PART_LATCH = 4;

    /** Parts of the chest facing south ({@code addTraceableCuboids}): body, buttons 1–3, latch. */
    private static final AABB[] PARTS = {
            new AABB(0.0625, 0.0, 0.0625, 0.9375, 0.875, 0.9375),
            button(0), button(1), button(2),
            new AABB(0.4375, 0.4375, 0.9375, 0.5625, 0.6875, 1.0)};
    private static final Map<Direction, VoxelShape> SHAPES = new EnumMap<>(Direction.class);

    static {
        for (Direction facing : Direction.Plane.HORIZONTAL) {
            VoxelShape shape = Shapes.empty();
            for (AABB part : PARTS) {
                shape = Shapes.or(shape, Shapes.create(toWorld(part, facing)));
            }
            SHAPES.put(facing, shape.optimize());
        }
    }

    public EnderChestBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    /** {@code EnderDyeButton}: 2 × 1 × 4 pixels on the lid, 3 pixels apart. */
    private static AABB button(int index) {
        double x = 0.25 + 0.1875 * index;
        return new AABB(x, 0.875, 0.375, x + 0.125, 0.9375, 0.625);
    }

    /** Quarter turns from south ({@code rotation} 0 = south, 1 = west, 2 = north, 3 = east). */
    private static int rotation(Direction facing) {
        return facing.get2DDataValue();
    }

    private static AABB toWorld(AABB box, Direction facing) {
        Vec3 a = turn(new Vec3(box.minX, box.minY, box.minZ), rotation(facing));
        Vec3 b = turn(new Vec3(box.maxX, box.maxY, box.maxZ), rotation(facing));
        return new AABB(a, b);
    }

    /** {@code rotateMeta}: {@code turns} quarter turns of -90° around the block's vertical axis. */
    private static Vec3 turn(Vec3 point, int turns) {
        double x = point.x - 0.5;
        double z = point.z - 0.5;
        for (int i = 0; i < turns; i++) {
            double newX = -z;
            z = x;
            x = newX;
        }
        return new Vec3(x + 0.5, point.y, z + 0.5);
    }

    /** The part hit at {@code location}; buttons and latch only while the lid is closed. */
    public static int hitPart(BlockState state, BlockPos pos, Vec3 location, boolean lidOpen) {
        if (lidOpen) {
            return PART_BODY;
        }
        Vec3 local = turn(location.subtract(pos.getX(), pos.getY(), pos.getZ()),
                (4 - rotation(state.getValue(FACING))) % 4);
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
        builder.add(FACING);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPES.get(state.getValue(FACING));
    }

    /** {@code onPlaced}: the front faces the player. */
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

    /** Dye on a button, diamond on the latch; anything else opens the chest. */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        boolean special = stack.is(Items.DIAMOND) || DyeColor.getColor(stack) != null;
        if (level.isClientSide()) {
            return special ? ItemInteractionResult.SUCCESS : ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!special || !(level.getBlockEntity(pos) instanceof EnderChestBlockEntity chest)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        int part = hitPart(state, pos, hit.getLocation(), chest.isLidOpen());
        Frequency frequency = chest.getFrequency();
        if (part == PART_LATCH && stack.is(Items.DIAMOND) && !frequency.isPersonal()) {
            chest.setFrequency(frequency.withOwner(player));
            stack.consume(1, player);
            return ItemInteractionResult.SUCCESS;
        }
        DyeColor dye = DyeColor.getColor(stack);
        if (part >= 1 && part <= 3 && dye != null) {
            if (frequency.colour(part - 1) == dye.getId()) {
                return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
            }
            chest.setFrequency(frequency.withColour(part - 1, dye.getId()));
            stack.consume(1, player);
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /**
     * Sneaking with an empty hand on the latch of a personal chest makes it global and hands out the diamond (the
     * original checks no owner); otherwise the chest opens.
     */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        if (!(level.getBlockEntity(pos) instanceof EnderChestBlockEntity chest)) {
            return InteractionResult.PASS;
        }
        Frequency frequency = chest.getFrequency();
        if (player.isSecondaryUseActive() && frequency.isPersonal()
                && hitPart(state, pos, hit.getLocation(), chest.isLidOpen()) == PART_LATCH) {
            if (!player.getAbilities().instabuild && !player.getInventory().add(new ItemStack(Items.DIAMOND))) {
                return InteractionResult.PASS;
            }
            chest.setFrequency(frequency.global());
            return InteractionResult.SUCCESS;
        }
        openStorage(player, chest.getStorage(), frequency, Component.translatable("container.craft.ender_chest"));
        return InteractionResult.CONSUME;
    }

    /** {@code openSMPGui}: the shared storage with the owner's name for the screen. */
    public static void openStorage(Player player, EnderItemStorage storage, Frequency frequency, Component title) {
        player.openMenu(new SimpleMenuProvider((id, inventory, p) ->
                new EnderStorageMenu(id, inventory, storage, frequency.ownerName()), title),
                buf -> buf.writeUtf(frequency.ownerName()));
    }

    /** {@code getPickBlock}: the chest with its frequency and owner. */
    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        ItemStack stack = super.getCloneItemStack(level, pos, state);
        if (level.getBlockEntity(pos) instanceof EnderChestBlockEntity chest) {
            stack.set(EsDataComponents.FREQUENCY.get(), chest.getFrequency());
        }
        return stack;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new EnderChestBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        return createTickerHelper(type, EsBlockEntityTypes.ENDER_CHEST.get(), level.isClientSide()
                ? (l, p, s, chest) -> chest.clientTick() : (l, p, s, chest) -> chest.serverTick());
    }
}
