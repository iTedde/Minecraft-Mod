package dev.craft.ic2.block;

import dev.craft.core.explosion.PointExplosion;
import dev.craft.ic2.Ic2EntityTypes;
import dev.craft.ic2.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * IC2 {@code EntityDynamite}/{@code EntityStickyDynamite} (spec 05 §7.1): thrown or primed dynamite. It flies without
 * hitting entities, bounces off blocks (sticky dynamite stays on the block and burns four times as fast), is kept from
 * exploding while in water, and goes off with a 3×3×3 {@link PointExplosion}. Only {@code inGround} is saved, as in IC2.
 */
public class DynamiteEntity extends Projectile implements ItemSupplier {
    public static final int FUSE = 100;
    private static final EntityDataAccessor<Integer> DATA_FUSE =
            SynchedEntityData.defineId(DynamiteEntity.class, EntityDataSerializers.INT);

    private boolean inGround;
    private int ticksInGround;
    private BlockPos stickPos = BlockPos.ZERO;

    public DynamiteEntity(EntityType<? extends DynamiteEntity> type, Level level) {
        super(type, level);
    }

    /** Primed by a dynamite stick block or shot from a dispenser: placed at (x, y, z) without motion. */
    public DynamiteEntity(Level level, double x, double y, double z, boolean sticky) {
        this(sticky ? Ic2EntityTypes.STICKY_DYNAMITE.get() : Ic2EntityTypes.DYNAMITE.get(), level);
        setPos(x, y, z);
    }

    /** Thrown by a player: starts slightly below and beside the eyes with speed 1. */
    public DynamiteEntity(Level level, LivingEntity thrower, boolean sticky) {
        this(sticky ? Ic2EntityTypes.STICKY_DYNAMITE.get() : Ic2EntityTypes.DYNAMITE.get(), level);
        setOwner(thrower);
        float yaw = thrower.getYRot() * Mth.DEG_TO_RAD;
        float pitch = thrower.getXRot() * Mth.DEG_TO_RAD;
        setPos(thrower.getX() - Mth.cos(yaw) * 0.16F, thrower.getEyeY() - 0.1F, thrower.getZ() - Mth.sin(yaw) * 0.16F);
        shoot(-Mth.sin(yaw) * Mth.cos(pitch), -Mth.sin(pitch), Mth.cos(yaw) * Mth.cos(pitch), 1.0F, 1.0F);
    }

    public boolean isSticky() {
        return getType() == Ic2EntityTypes.STICKY_DYNAMITE.get();
    }

    public int getFuse() {
        return entityData.get(DATA_FUSE);
    }

    public void setFuse(int fuse) {
        entityData.set(DATA_FUSE, fuse);
    }

    public boolean isInGround() {
        return inGround;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_FUSE, FUSE);
    }

    /** IC2 {@code setThrowableHeading}: Gaussian spread of 0.0075 per unit of inaccuracy. */
    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        Vec3 motion = new Vec3(x, y, z).normalize()
                .add(random.nextGaussian() * 0.0075 * inaccuracy, random.nextGaussian() * 0.0075 * inaccuracy,
                        random.nextGaussian() * 0.0075 * inaccuracy)
                .scale(velocity);
        setDeltaMovement(motion);
        ticksInGround = 0;
    }

    @Override
    public void tick() {
        super.tick();
        boolean server = !level().isClientSide();
        int fuse = getFuse() - 1;
        if (server) {
            setFuse(fuse);
        }
        if (fuse < 0) {
            if (level() instanceof ServerLevel serverLevel) {
                discard();
                new PointExplosion(serverLevel, getX(), getY(), getZ(), 1.0F, 1.0F, 0.8F).explode(1);
            }
            return;
        }
        if (!server && fuse < FUSE && fuse % 2 == 0) {
            level().addParticle(ParticleTypes.SMOKE, getX(), getY() + 0.5, getZ(), 0.0, 0.0, 0.0);
        }

        if (inGround) {
            if (++ticksInGround >= 200 && server) {
                discard();
            }
            if (isSticky()) {
                if (server) {
                    setFuse(getFuse() - 3);
                }
                setDeltaMovement(Vec3.ZERO);
                if (!level().getBlockState(stickPos).isAir()) {
                    return;
                }
            }
        }

        Vec3 start = position();
        Vec3 motion = getDeltaMovement();
        BlockHitResult hit = level().clip(new ClipContext(start, start.add(motion), ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, this));
        Vec3 next;
        if (hit.getType() != HitResult.Type.MISS) {
            Vec3 remain = hit.getLocation().subtract(start);
            double length = remain.length();
            stickPos = hit.getBlockPos();
            next = length > 0.0 ? hit.getLocation().subtract(remain.scale(0.05 / length)) : hit.getLocation();
            motion = new Vec3(motion.x * (0.75F - random.nextFloat()), motion.y * -0.3F,
                    motion.z * (0.75F - random.nextFloat()));
            inGround = true;
        } else {
            next = start.add(motion);
            inGround = false;
        }

        double drag = 0.98;
        if (isInWater()) {
            if (server) {
                setFuse(getFuse() + 2000);
            }
            for (int i = 0; i < 4; i++) {
                level().addParticle(ParticleTypes.BUBBLE, next.x - motion.x * 0.25, next.y - motion.y * 0.25,
                        next.z - motion.z * 0.25, motion.x, motion.y, motion.z);
            }
            drag = 0.75;
        }
        setDeltaMovement(motion.scale(drag).add(0.0, -0.04, 0.0));
        setPos(next);
    }

    /** Both kinds are drawn with the dynamite icon (IC2 {@code RenderFlyingItem} sprite 62). */
    @Override
    public ItemStack getItem() {
        return new ItemStack(Ic2Items.DYNAMITE.get());
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putByte("inGround", (byte) (inGround ? 1 : 0));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        inGround = tag.getByte("inGround") == 1;
    }
}
