package dev.craft.ic2.block;

import dev.craft.core.explosion.Ic2Explosion;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/**
 * IC2 {@code EntityIC2Explosive} (spec 05 §7): primed IC2 explosive that falls and bounces like TNT and goes off with an
 * {@link Ic2Explosion} at its centre when the fuse runs out.
 */
public abstract class Ic2ExplosiveEntity extends Entity {
    private static final EntityDataAccessor<Integer> DATA_FUSE =
            SynchedEntityData.defineId(Ic2ExplosiveEntity.class, EntityDataSerializers.INT);

    protected Ic2ExplosiveEntity(EntityType<? extends Ic2ExplosiveEntity> type, Level level) {
        super(type, level);
        blocksBuilding = true;
        setFuse(getFuseLength());
    }

    /**
     * Places the entity centred on (x, y, z) with the initial hop. IC2 feeds the radian angle to sin/cos as degrees, so
     * the hop always points (almost) north.
     */
    protected void prime(double x, double y, double z) {
        setPos(x, y - getBbHeight() / 2.0, z);
        float angle = (float) (Math.random() * Math.PI * 2.0) * (float) Math.PI / 180.0F;
        setDeltaMovement(-Mth.sin(angle) * 0.02F, 0.2F, -Mth.cos(angle) * 0.02F);
        xo = getX();
        yo = getY();
        zo = getZ();
    }

    public abstract int getFuseLength();

    protected abstract float getPower();

    protected abstract float getDropRate();

    protected abstract float getEntityDamage();

    /** Block drawn by the renderer ({@code renderBlock}). */
    public abstract BlockState getRenderState();

    public int getFuse() {
        return entityData.get(DATA_FUSE);
    }

    public void setFuse(int fuse) {
        entityData.set(DATA_FUSE, fuse);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        builder.define(DATA_FUSE, 80);
    }

    @Override
    protected MovementEmission getMovementEmission() {
        return MovementEmission.NONE;
    }

    @Override
    public boolean isPickable() {
        return !isRemoved();
    }

    @Override
    public void tick() {
        setDeltaMovement(getDeltaMovement().add(0.0, -0.04, 0.0));
        move(MoverType.SELF, getDeltaMovement());
        setDeltaMovement(getDeltaMovement().scale(0.98));
        if (onGround()) {
            setDeltaMovement(getDeltaMovement().multiply(0.7, -0.5, 0.7));
        }
        int fuse = getFuse();
        setFuse(fuse - 1);
        if (fuse <= 0) {
            discard();
            if (level() instanceof ServerLevel serverLevel) {
                new Ic2Explosion(serverLevel, getX(), getY() + getBbHeight() / 2.0, getZ(), getPower(), getDropRate(),
                        getEntityDamage()).explode();
            }
        } else if (level().isClientSide()) {
            level().addParticle(ParticleTypes.SMOKE, getX(), getY() + getBbHeight() / 2.0 + 0.5, getZ(), 0.0, 0.0,
                    0.0);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putByte("Fuse", (byte) getFuse());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setFuse(tag.getByte("Fuse"));
    }
}
