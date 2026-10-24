package dev.craft.mps.entity;

import dev.craft.mps.MpsTool;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * MPS {@code EntityPlasmaBolt} (spec 29 §8.3): a throwable without gravity at 1 block/tick (vanilla drag), size =
 * charge ticks up to 50; an entity hit takes size/50 × Damage At Full Charge, every hit explodes with size/50 × 3 ×
 * Explosiveness. Dies in water and after 200 ticks; not saved.
 */
public class PlasmaBoltEntity extends ThrowableProjectile {
    public static final int MAX_LIFETIME = 200;
    /** {@code EntityPlayer.getEyeHeight()} on the 1.4.7 server, used in the spawn offset. */
    private static final double SHOOTER_EYE_HEIGHT = 0.12;
    private static final EntityDataAccessor<Byte> SIZE =
            SynchedEntityData.defineId(PlasmaBoltEntity.class, EntityDataSerializers.BYTE);

    private double damagingness;
    private double explosiveness;

    public PlasmaBoltEntity(EntityType<? extends PlasmaBoltEntity> type, Level level) {
        super(type, level);
    }

    public PlasmaBoltEntity(Level level, Player shooter, double explosiveness, double damagingness, int chargeTicks) {
        this(MpsEntityTypes.PLASMA_BOLT.get(), level);
        setOwner(shooter);
        int size = Math.min(chargeTicks, 50);
        entityData.set(SIZE, (byte) size);
        this.explosiveness = explosiveness;
        this.damagingness = damagingness;
        Vec3 direction = shooter.getLookAngle();
        setDeltaMovement(direction);
        double r = size / 50.0 - direction.y * SHOOTER_EYE_HEIGHT;
        Vec3 pos = MpsTool.muzzle(shooter, r);
        setPos(pos.x, pos.y, pos.z);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(SIZE, (byte) 0);
    }

    public int getSize() {
        return entityData.get(SIZE);
    }

    @Override
    protected double getDefaultGravity() {
        return 0.0;
    }

    @Override
    public void tick() {
        super.tick();
        if (isRemoved()) {
            return;
        }
        if (tickCount > MAX_LIFETIME) {
            discard();
        }
        if (isInWater()) {
            discard();
            flames(getSize(), 1.0, 0.1);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        double damage = getSize() / 50.0 * damagingness;
        result.getEntity().hurt(damageSources().thrown(this, getOwner()), (int) damage);
    }

    /** Every impact explodes (with block damage), leaves 8 flames and removes the bolt. */
    @Override
    protected void onHit(HitResult result) {
        if (level().isClientSide()) {
            return;
        }
        super.onHit(result);
        level().explode(this, getX(), getY(), getZ(), (float) (getSize() / 50.0 * 3.0 * explosiveness),
                Level.ExplosionInteraction.BLOCK);
        flames(8, 0.1, 0.1);
        discard();
    }

    private void flames(int count, double spreadXY, double spreadZ) {
        if (level() instanceof ServerLevel serverLevel) {
            for (int i = 0; i < count; i++) {
                serverLevel.sendParticles(ParticleTypes.FLAME, getX() + random.nextDouble() * spreadXY,
                        getY() + random.nextDouble() * spreadXY, getZ() + random.nextDouble() * spreadZ, 1, 0.0, 0.0,
                        0.0, 0.0);
            }
        }
    }

    /** Not saved, like the original's {@code readEntityFromNBT} that kills it. */
    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        discard();
    }
}
