package dev.craft.mffs.block;

import dev.craft.core.CraftDamageTypes;
import dev.craft.mffs.MffsConfig;
import dev.craft.mffs.MffsDevices;
import dev.craft.mffs.field.ForceFieldMap;
import dev.craft.mffs.field.ForceFieldType;
import dev.craft.mffs.security.MffsSecurity;
import dev.craft.mffs.security.SecurityRight;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * {@code BlockForceField} (spec 48 §9.4): unbreakable, explosion resistance 999 (each query costs the owning projector
 * 10 FE), no drops, no item, no redstone conduction, no selection box (hidden client-side). Full collision, the zapper
 * type 1/16 smaller so touching it hurts. Neighbours of the same type hide their faces. A removed neighbour makes the
 * field refill empty claimed positions around it at once; a random tick removes blocks without an active owner.
 */
public class ForceFieldBlock extends Block {
    public static final EnumProperty<ForceFieldType> TYPE = EnumProperty.create("type", ForceFieldType.class);
    private static final VoxelShape ZAPPER_SHAPE = Block.box(1, 1, 1, 15, 15, 15);
    private static final int SHOCK = 10;

    public ForceFieldBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(TYPE, ForceFieldType.DEFAULT));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(TYPE);
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos,
                                           CollisionContext context) {
        return state.getValue(TYPE) == ForceFieldType.ZAPPER ? ZAPPER_SHAPE : Shapes.block();
    }

    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacent, Direction direction) {
        return adjacent.is(this) && adjacent.getValue(TYPE) == state.getValue(TYPE)
                || super.skipRendering(state, adjacent, direction);
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }

    @Override
    protected float getShadeBrightness(BlockState state, BlockGetter level, BlockPos pos) {
        return 1.0F;
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, LevelReader level, BlockPos pos,
                                       Player player) {
        return ItemStack.EMPTY;
    }

    private static ProjectorBlockEntity owner(Level level, ForceFieldMap.Stack stack) {
        return MffsDevices.get(level, MffsDevices.Kind.PROJECTOR, stack.getProjectorId())
                instanceof ProjectorBlockEntity projector ? projector : null;
    }

    private static boolean granted(MffsMachineBlockEntity machine, Player player, SecurityRight right) {
        return machine == null || MffsSecurity.checkAccess(machine, player, right);
    }

    private static void shock(Entity entity, int amount) {
        entity.hurt(entity.level().damageSources().source(CraftDamageTypes.FIELD_SHOCK), amount);
    }

    private static void warn(Player player) {
        player.sendSystemMessage(Component.translatable("message.craft.mffs.high_energy_field"));
    }

    /**
     * Left click: access type 0 shocks; 2/3 without right SR shocks – and the original checks SR a second time, so
     * that player is shocked twice. Then the random-tick check runs.
     */
    @Override
    protected void attack(BlockState state, Level level, BlockPos pos, Player player) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        ForceFieldMap.Stack stack = ForceFieldMap.get(level, pos);
        if (stack != null) {
            ProjectorBlockEntity projector = owner(level, stack);
            if (projector != null) {
                switch (projector.getAccessType()) {
                    case 0 -> {
                        shock(player, SHOCK);
                        warn(player);
                    }
                    case 2, 3 -> {
                        if (!granted(projector, player, SecurityRight.SR)) {
                            shock(player, SHOCK);
                            warn(player);
                        }
                    }
                    default -> {
                    }
                }
            }
            if (!granted(projector, player, SecurityRight.SR)) {
                shock(player, SHOCK);
                warn(player);
            }
        }
        randomTick(state, serverLevel, pos, serverLevel.getRandom());
    }

    /**
     * Touch: the zapper type hurts every living entity (10); the other types are full blocks, a player inside one
     * takes 20 without and 1 with access (access type 1, or right FFB at the capacitor's/own station for 2/3).
     */
    @Override
    protected void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide()) {
            return;
        }
        if (state.getValue(TYPE) == ForceFieldType.ZAPPER) {
            if (entity instanceof LivingEntity) {
                shock(entity, SHOCK);
            }
            return;
        }
        if (!(entity instanceof Player player)) {
            return;
        }
        ForceFieldMap.Stack stack = ForceFieldMap.get(level, pos);
        ProjectorBlockEntity projector = stack != null ? owner(level, stack) : null;
        if (projector == null) {
            return;
        }
        boolean pass = switch (projector.getAccessType()) {
            case 1 -> true;
            case 2 -> granted((MffsMachineBlockEntity) MffsDevices.get(level, MffsDevices.Kind.CAPACITOR,
                    stack.getCapacitorId()), player, SecurityRight.FFB);
            case 3 -> granted(projector, player, SecurityRight.FFB);
            default -> false;
        };
        shock(player, pass ? 1 : 20);
        warn(player);
    }

    /** {@code onNeighborBlockChange}: unless the neighbour became a field block, refill the air around. */
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock,
                                   BlockPos neighborPos, boolean movedByPiston) {
        if (level.isClientSide() || level.getBlockState(neighborPos).is(this)) {
            return;
        }
        for (BlockPos around : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            if (level.getBlockState(around).isAir()) {
                refill(level, around.immutable());
            }
        }
    }

    /**
     * {@code breakBlock} as called by the neighbour check: an inactive owner gives up its claim, an active one places
     * the block again at once – creation cost 10 FE for the default type, 20 for the others (original).
     */
    private void refill(Level level, BlockPos pos) {
        ForceFieldMap.Stack stack = ForceFieldMap.get(level, pos);
        if (stack == null || stack.isEmpty()) {
            return;
        }
        ProjectorBlockEntity projector = owner(level, stack);
        if (projector == null) {
            return;
        }
        if (!projector.isActive()) {
            stack.removeByProjector(stack.getProjectorId());
            return;
        }
        level.setBlock(pos, defaultBlockState().setValue(TYPE, stack.getType()), Block.UPDATE_ALL);
        stack.setSync(true);
        int cost = MffsConfig.FIELD_BLOCK_COST * MffsConfig.FIELD_BLOCK_CREATE_MODIFIER;
        if (stack.getType() != ForceFieldType.DEFAULT) {
            cost *= MffsConfig.FIELD_BLOCK_ZAPPER_MODIFIER;
        }
        projector.consumePower(cost, false);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    /** {@code updateTick}: an inactive owner gives up its claim; without any claim the block disappears. */
    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        ForceFieldMap.Stack stack = ForceFieldMap.get(level, pos);
        if (stack != null && !stack.isEmpty()) {
            ProjectorBlockEntity projector = owner(level, stack);
            if (projector != null && !projector.isActive()) {
                stack.removeByProjector(stack.getProjectorId());
            }
        }
        if (stack == null || stack.isEmpty()) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        }
    }

    /** Every explosion query costs the owning projector the creation price of one block. */
    @Override
    public float getExplosionResistance(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        if (level instanceof Level world && !world.isClientSide()) {
            ForceFieldMap.Stack stack = ForceFieldMap.get(world, pos);
            ProjectorBlockEntity projector = stack != null && !stack.isEmpty() ? owner(world, stack) : null;
            if (projector != null) {
                projector.consumePower(MffsConfig.FIELD_BLOCK_COST * MffsConfig.FIELD_BLOCK_CREATE_MODIFIER, false);
            }
        }
        return 999.0F;
    }

    /** Zapper fields give off particles ("townaura", pack {@code renderZapperParticles=true}). */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(TYPE) == ForceFieldType.ZAPPER) {
            level.addParticle(ParticleTypes.MYCELIUM, pos.getX() + Math.random() + 0.2,
                    pos.getY() + Math.random() + 0.2, pos.getZ() + Math.random() + 0.2, 0.0, 0.0, 0.0);
        }
    }
}
