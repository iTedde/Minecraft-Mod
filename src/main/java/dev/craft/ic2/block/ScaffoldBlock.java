package dev.craft.ic2.block;

import com.mojang.serialization.MapCodec;
import dev.craft.ic2.keyboard.Ic2Keyboard;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

/**
 * Wooden or iron scaffold (IC2 {@code BlockScaffold}). The IC2 metadata is the {@link #STRENGTH}: a scaffold on a
 * pillar (a scaffold column standing on a solid block) has the standard strength, otherwise the strongest neighbor
 * below or beside it minus one; below 0 it breaks. A pillar can be reinforced with two sticks or an iron fence, which
 * raises its strength. Players inside climb it (forward key) and take no fall damage.
 */
public class ScaffoldBlock extends Block {
    public static final MapCodec<ScaffoldBlock> CODEC = simpleCodec(properties -> new ScaffoldBlock(properties, 2, 5,
            () -> Items.STICK, 2));
    public static final IntegerProperty STRENGTH = IntegerProperty.create("strength", 0, 12);
    /** IC2 {@code tickDelay}. */
    public static final int TICK_DELAY = 1;
    private static final VoxelShape COLLISION_SHAPE = Block.box(1, 0, 1, 15, 16, 15);

    private final int standardStrength;
    private final int reinforcedStrength;
    private final Supplier<? extends Item> reinforcement;
    private final int reinforcementCount;

    /**
     * @param reinforcement      item that reinforces a pillar and drops again from a reinforced scaffold
     * @param reinforcementCount how many of it a reinforcement takes
     */
    public ScaffoldBlock(Properties properties, int standardStrength, int reinforcedStrength,
                         Supplier<? extends Item> reinforcement, int reinforcementCount) {
        super(properties);
        this.standardStrength = standardStrength;
        this.reinforcedStrength = reinforcedStrength;
        this.reinforcement = reinforcement;
        this.reinforcementCount = reinforcementCount;
        registerDefaultState(stateDefinition.any().setValue(STRENGTH, standardStrength));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STRENGTH);
    }

    public int getStandardStrength() {
        return standardStrength;
    }

    public int getReinforcedStrength() {
        return reinforcedStrength;
    }

    public boolean isReinforced(BlockState state) {
        return state.getValue(STRENGTH) == reinforcedStrength;
    }

    // --- shape and entities ---

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                           CollisionContext context) {
        return COLLISION_SHAPE;
    }

    /** IC2 {@code onEntityCollidedWithBlock}: players do not fall faster than 0.15 and climb with the forward key. */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (!(entity instanceof Player player)) {
            return;
        }
        player.resetFallDistance();
        Vec3 motion = player.getDeltaMovement();
        double motionY = motion.y;
        if (motionY < -0.15) {
            motionY = -0.15;
        }
        if (Ic2Keyboard.isForwardKeyDown(player) && motionY < 0.2) {
            motionY = 0.2;
        }
        if (motionY != motion.y) {
            player.setDeltaMovement(motion.x, motionY, motion.z);
        }
    }

    // --- strength ---

    /** IC2 {@code canPlaceBlockAt}: replaceable and supported. Places with the computed strength. */
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        int strength = getStrengthFrom(context.getLevel(), context.getClickedPos());
        return strength <= -1 ? null : defaultBlockState().setValue(STRENGTH, strength);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   BlockPos neighborPos, boolean movedByPiston) {
        level.scheduleTick(pos, this, TICK_DELAY);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        updateStrength(state, level, pos);
    }

    private void updateStrength(BlockState state, Level level, BlockPos pos) {
        int ownStrength = state.getValue(STRENGTH);
        if (ownStrength >= reinforcedStrength) {
            if (!isPillar(level, pos)) {
                ownStrength = getStrengthFrom(level, pos);
                popResource(level, pos, new ItemStack(reinforcement.get(), reinforcementCount));
            }
        } else {
            ownStrength = getStrengthFrom(level, pos);
        }
        if (ownStrength <= -1) {
            level.removeBlock(pos, false);
            popResource(level, pos, new ItemStack(this));
        } else if (ownStrength != state.getValue(STRENGTH)) {
            level.setBlock(pos, state.setValue(STRENGTH, ownStrength), Block.UPDATE_ALL);
        }
    }

    /** Whether the scaffold column through {@code pos} stands on a normal cube. */
    public boolean isPillar(BlockGetter level, BlockPos pos) {
        BlockPos.MutableBlockPos cursor = pos.mutable();
        while (level.getBlockState(cursor).is(this)) {
            cursor.move(Direction.DOWN);
        }
        return level.getBlockState(cursor).isRedstoneConductor(level, cursor);
    }

    public int getStrengthFrom(BlockGetter level, BlockPos pos) {
        int strength = 0;
        if (isPillar(level, pos.below())) {
            strength = standardStrength + 1;
        }
        strength = compareStrengthTo(level, pos.below(), strength);
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            strength = compareStrengthTo(level, pos.relative(direction), strength);
        }
        return strength - 1;
    }

    private int compareStrengthTo(BlockGetter level, BlockPos pos, int strength) {
        BlockState state = level.getBlockState(pos);
        int s = state.is(this) ? Math.min(state.getValue(STRENGTH), reinforcedStrength) : 0;
        return Math.max(s, strength);
    }

    // --- interaction ---

    /** Reinforces a pillar with two sticks (wood) or one iron fence (IC2 takes them in creative mode as well). */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (player.isShiftKeyDown() || !stack.is(reinforcement.get()) || stack.getCount() < reinforcementCount) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (isReinforced(state) || !isPillar(level, pos)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        if (!level.isClientSide) {
            stack.shrink(reinforcementCount);
            level.setBlock(pos, state.setValue(STRENGTH, reinforcedStrength), Block.UPDATE_ALL);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    /** Left click with a scaffold of this kind extends the column on top. */
    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        ItemStack held = player.getMainHandItem();
        if (level.isClientSide || !held.is(asItem())) {
            return;
        }
        BlockPos.MutableBlockPos top = pos.mutable();
        while (level.getBlockState(top).is(this)) {
            top.move(Direction.UP);
        }
        if (top.getY() >= level.getMaxBuildHeight() || !level.getBlockState(top).canBeReplaced()) {
            return;
        }
        int strength = getStrengthFrom(level, top);
        if (strength <= -1) {
            return;
        }
        level.setBlock(top, defaultBlockState().setValue(STRENGTH, strength), Block.UPDATE_ALL);
        held.consume(1, player);
    }

    // --- fire (wooden scaffold: IC2 setBurnProperties(8, 20)) ---

    @Override
    public int getFlammability(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return isWooden() ? 20 : 0;
    }

    @Override
    public int getFireSpreadSpeed(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return isWooden() ? 8 : 0;
    }

    private boolean isWooden() {
        return reinforcement.get() == Items.STICK;
    }
}
