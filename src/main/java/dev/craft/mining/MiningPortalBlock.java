package dev.craft.mining;

import java.util.Optional;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.MapCodec;
import net.minecraft.BlockUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.NetherPortalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.portal.DimensionTransition;
import net.minecraft.world.level.portal.PortalShape;
import net.minecraft.world.phys.Vec3;
import org.slf4j.Logger;

import javax.annotation.Nullable;

/**
 * Mining portal (spec 18 §4.2): behaves like the nether portal block, but links the overworld and the mining dimension
 * 1:1 and spawns no zombified piglins.
 */
public final class MiningPortalBlock extends NetherPortalBlock {
    public static final MapCodec<NetherPortalBlock> CODEC = simpleCodec(MiningPortalBlock::new);
    private static final Logger LOGGER = LogUtils.getLogger();

    public MiningPortalBlock(Properties properties) {
        super(properties);
    }

    @Override
    public MapCodec<NetherPortalBlock> codec() {
        return CODEC;
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level,
                                     BlockPos pos, BlockPos facingPos) {
        Direction.Axis axis = state.getValue(AXIS);
        boolean sideways = facing.getAxis() != axis && facing.getAxis().isHorizontal();
        if (!sideways && !facingState.is(this) && !new MiningPortalShape(level, pos, axis).isComplete()) {
            return Blocks.AIR.defaultBlockState();
        }
        return state;
    }

    @Nullable
    @Override
    public DimensionTransition getPortalDestination(ServerLevel level, Entity entity, BlockPos pos) {
        ResourceKey<Level> targetKey = level.dimension() == MiningDimension.LEVEL ? Level.OVERWORLD : MiningDimension.LEVEL;
        ServerLevel target = level.getServer().getLevel(targetKey);
        if (target == null) {
            return null;
        }
        WorldBorder border = target.getWorldBorder();
        BlockPos exit = border.clampToBounds(entity.getX(), entity.getY(), entity.getZ());
        Optional<BlockPos> existing = MiningPortalForcer.findClosestPortal(target, exit);
        BlockUtil.FoundRectangle rectangle;
        DimensionTransition.PostDimensionTransition after;
        if (existing.isPresent()) {
            BlockPos found = existing.get();
            BlockState foundState = target.getBlockState(found);
            rectangle = BlockUtil.getLargestRectangleAround(found, foundState.getValue(AXIS), MiningPortalShape.MAX_SIZE,
                    Direction.Axis.Y, MiningPortalShape.MAX_SIZE, p -> target.getBlockState(p) == foundState);
            after = DimensionTransition.PLAY_PORTAL_SOUND.then(e -> e.placePortalTicket(found));
        } else {
            Direction.Axis axis = level.getBlockState(pos).getOptionalValue(AXIS).orElse(Direction.Axis.X);
            Optional<BlockUtil.FoundRectangle> created = MiningPortalForcer.createPortal(target, exit, axis);
            if (created.isEmpty()) {
                LOGGER.error("Unable to create a mining portal, likely target out of world border");
                return null;
            }
            rectangle = created.get();
            after = DimensionTransition.PLAY_PORTAL_SOUND.then(DimensionTransition.PLACE_PORTAL_TICKET);
        }
        return transition(entity, pos, rectangle, target, after);
    }

    /** Keeps the entity's relative position and motion in the portal, like the nether portal. */
    private static DimensionTransition transition(Entity entity, BlockPos pos, BlockUtil.FoundRectangle rectangle,
                                                  ServerLevel target, DimensionTransition.PostDimensionTransition after) {
        BlockState state = entity.level().getBlockState(pos);
        Direction.Axis axis = Direction.Axis.X;
        Vec3 offset = new Vec3(0.5, 0.0, 0.0);
        if (state.hasProperty(AXIS)) {
            axis = state.getValue(AXIS);
            BlockUtil.FoundRectangle source = BlockUtil.getLargestRectangleAround(pos, axis, MiningPortalShape.MAX_SIZE,
                    Direction.Axis.Y, MiningPortalShape.MAX_SIZE, p -> entity.level().getBlockState(p) == state);
            offset = entity.getRelativePortalPosition(axis, source);
        }
        BlockPos corner = rectangle.minCorner;
        Direction.Axis targetAxis = target.getBlockState(corner).getOptionalValue(AXIS).orElse(Direction.Axis.X);
        EntityDimensions size = entity.getDimensions(entity.getPose());
        boolean sameAxis = axis == targetAxis;
        Vec3 speed = entity.getDeltaMovement();
        Vec3 motion = sameAxis ? speed : new Vec3(speed.z, speed.y, -speed.x);
        double along = size.width() / 2.0 + (rectangle.axis1Size - size.width()) * offset.x();
        double up = (rectangle.axis2Size - size.height()) * offset.y();
        double across = 0.5 + offset.z();
        boolean alongX = targetAxis == Direction.Axis.X;
        Vec3 position = new Vec3(corner.getX() + (alongX ? along : across), corner.getY() + up,
                corner.getZ() + (alongX ? across : along));
        position = PortalShape.findCollisionFreePosition(position, target, entity, size);
        return new DimensionTransition(target, position, motion, entity.getYRot() + (sameAxis ? 0 : 90),
                entity.getXRot(), after);
    }
}
