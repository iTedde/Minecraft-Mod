/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity;

import ivorius.psychedelicraft.ParticleHelper;
import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.block.PSBlocks;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.SynchedEntityData.Builder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import java.util.function.Supplier;

import org.joml.Vector3f;

import com.google.common.base.Suppliers;

/**
 * Created by lukas on 03.03.14.
 */
public class RealityRiftEntity extends Entity {
    public static final float CLOSING_DECAY_RATE = 0.05F;
    public static final float RIFT_DECAY_RATE = CLOSING_DECAY_RATE / 1200F;
    public static final float ANIMATION_CHANGE_RATE = CLOSING_DECAY_RATE / 10F;
    public static final float RIFT_COLLAPSE_THRESHOLD = 0.9F;
    public static final float CRITICAL_RIFT_BLEED_AMOUNT = 0.2F;
    public static final double AFFECT_PER_BLOCK = 0.0005;
    private static final EntityDataAccessor<Float> SIZE = SynchedEntityData.defineId(RealityRiftEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> INSTABILITY = SynchedEntityData.defineId(RealityRiftEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> CLOSING = SynchedEntityData.defineId(RealityRiftEntity.class, EntityDataSerializers.BOOLEAN);

    private float prevVvisualRiftSize;
    private float visualRiftSize;

    public static void spawn(Entity entity) {
        RealityRiftEntity rift = PSEntities.REALITY_RIFT.create(entity.level());
        rift.setPos(
                entity.getX() + (entity.level().getRandom().nextDouble() - 0.5) * 100,
                entity.getY() + (entity.level().getRandom().nextDouble() - 0.5) * 100,
                entity.getZ() + (entity.level().getRandom().nextDouble() - 0.5) * 100
        );
        entity.level().addFreshEntity(rift);
    }

    RealityRiftEntity(EntityType<RealityRiftEntity> type, Level par1World) {
        super(type, par1World);
        setRiftSize((float)level().getRandom().triangle(0.5F, 0.5F));
    }

    @Override
    public AABB getBoundingBoxForCulling() {
        return getBoundingBox().inflate(20);
    }

    @Override
    protected void defineSynchedData(Builder builder) {
        builder.define(SIZE, 0F).define(CLOSING, false).define(INSTABILITY, 0F);
    }

    public float getRiftSize() {
        return getEntityData().get(SIZE);
    }

    public float getRiftSize(float tickDelta) {
        return Mth.lerp(tickDelta, prevVvisualRiftSize, visualRiftSize);
    }

    public void setRiftSize(float size) {
        getEntityData().set(SIZE, Math.max(0, size));
    }

    public void addToRift(float size) {
        setRiftSize(getRiftSize() + size);
    }

    public float takeFromRift(float size) {
        if (isCritical()) {
            return CRITICAL_RIFT_BLEED_AMOUNT;
        }

        float riftSize = getRiftSize();
        float newVal = Math.max(riftSize - size, 0);

        setRiftSize(newVal);

        return riftSize - newVal;
    }

    public float getInstability() {
        return getEntityData().get(INSTABILITY);
    }

    public void setInstability(float instability) {
        getEntityData().set(INSTABILITY, Math.max(0, instability));
    }

    public boolean isRiftClosing() {
        return getEntityData().get(CLOSING);
    }

    public void setRiftClosing(boolean closing) {
        getEntityData().set(CLOSING, closing);
    }

    public boolean isCritical() {
        return ((getInstability() > 0) || (getRiftSize() > 3)) && !isRiftClosing();
    }

//    @Override
//    public boolean interactFirst(EntityPlayer par1EntityPlayer)
//    {
//        ItemStack heldItem = par1EntityPlayer.getHeldItem();
//        if (!isRiftClosing() && heldItem.getItem() == Psychedelicraft.itemRiftObtainer && heldItem.getItemDamage() == 0)
//        {
//            heldItem.setItemDamage(1);
//            setRiftClosing(true);
//
//            return true;
//        }
//
//        return super.interactFirst(par1EntityPlayer);
//    }

    @Override
    public boolean isIgnoringBlockTriggers() {
        return true;
    }

//    @Override
//    public boolean canBeCollidedWith() // Need this for right click interaction
//    {
//        return !isDead;
//    }

    @Override
    public Component getDisplayName() {
        return super.getDisplayName().copy().withStyle(ChatFormatting.OBFUSCATED);
    }

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(Vec3.ZERO);

        if (level() instanceof ServerLevel) {
            if (Psychedelicraft.getConfig().randomTicksUntilRiftSpawn.get() == 0) {
                kill();
                return;
            }

            emitEffects();

            if (isCritical()) {
                spreadCorruption();
            }

            if (isRiftClosing()) {
                setRiftSize(getRiftSize() - CLOSING_DECAY_RATE);
            } else if (!isCritical()) {
                setRiftSize(getRiftSize() - RIFT_DECAY_RATE);
            }

            if (getInstability() >= RIFT_COLLAPSE_THRESHOLD) {
                setRiftClosing(true);
            }

            if (visualRiftSize <= 0 && getRiftSize() <= 0) {
                remove(Entity.RemovalReason.KILLED);
            }
        } else {
            Vec3 pos = position();
            Supplier<Vec3> particlePositionSupplier = () -> {
                float distance = random.nextFloat() * random.nextFloat();
                return ParticleHelper.apply(pos, x -> x + (random.nextFloat() * 8 - 4) * distance).add(0, getBbHeight() / 2F, 0);
            };
            ParticleHelper.spawnParticles(level(), ParticleTypes.LARGE_SMOKE, particlePositionSupplier, Suppliers.ofInstance(Vec3.ZERO), random.nextInt(3));
            ParticleHelper.spawnParticles(level(), new DustParticleOptions(new Vector3f(1, 0.5F, 0.5F), 1), particlePositionSupplier, Suppliers.ofInstance(new Vec3(-10, -10, -10)), random.nextInt(2));
            ParticleHelper.spawnParticles(level(), ParticleTypes.ENCHANT, Suppliers.ofInstance(pos.add(0, 1 + (getBbHeight() / 2F), 0)), () -> {
                float distance = random.nextFloat() * random.nextFloat();
                return ParticleHelper.apply(Vec3.ZERO, x -> x + (random.nextFloat() * 8 - 4) * distance).add(0, getBbHeight() / 2F, 0);
            }, 1);
        }

        prevVvisualRiftSize = visualRiftSize;
        visualRiftSize = MathUtils.nearValue(visualRiftSize, getRiftSize(), CLOSING_DECAY_RATE, ANIMATION_CHANGE_RATE);
    }

    private void emitEffects() {
        float searchDistance = 5 + getInstability() * 50;
        boolean critical = isCritical();
        for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(searchDistance), EntitySelector.NO_CREATIVE_OR_SPECTATOR)) {
            double dist = entity.distanceTo(this);
            double effect = (searchDistance - dist) * AFFECT_PER_BLOCK * getRiftSize();

            if (effect > 0) {
                DrugProperties.of(entity).ifPresentOrElse(drugProperties -> {
                    drugProperties.addToDrug(DrugType.ZERO, effect * 20);
                    drugProperties.addToDrug(DrugType.POWER, effect * 200);
                }, () -> {
                    if (critical) {
                        entity.hurt(damageSources().magic(), (float)effect * 20);
                    }
                });
            }
        }
    }

    private void spreadCorruption() {
        float prevS = getInstability();
        float newS = Math.min(prevS + 0.001f, 1);
        setInstability(newS);

        float prevDesRange = prevS * 50;
        float newDesRange = newS * 50;

        if (prevDesRange < newDesRange) {
            int desRange = Mth.ceil(newDesRange);
            BlockPos center = blockPosition();
            BlockPos.withinManhattan(center, desRange, desRange, desRange).forEach(p -> {
                if (p.closerThan(center, newDesRange) && !p.closerThan(center, prevDesRange) && !level().isEmptyBlock(p)) {
                    level().setBlockAndUpdate(p, PSBlocks.GLITCH.defaultBlockState());
                }
            });
        }
    }

    @Override
    public void kill() {
        setRiftClosing(true);
        gameEvent(GameEvent.ENTITY_DIE);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("riftSize", Tag.TAG_FLOAT)) {
            setRiftSize(compound.getFloat("riftSize"));
        }
        if (compound.contains("isRiftClosing")) {
            setRiftClosing(compound.getBoolean("isRiftClosing"));
        }
        if (compound.contains("instability", Tag.TAG_FLOAT)) {
            setInstability(compound.getFloat("instability"));
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        compound.putFloat("riftSize", getRiftSize());
        compound.putBoolean("isRiftClosing", isRiftClosing());
        compound.putFloat("instability", getInstability());
    }
}
