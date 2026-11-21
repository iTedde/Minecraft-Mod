/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity;

import ivorius.psychedelicraft.PSDamageTypes;
import ivorius.psychedelicraft.fluid.Combustable;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.Level.ExplosionInteraction;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class MolotovCocktailEntity extends ThrowableItemProjectile {

    public MolotovCocktailEntity(EntityType<MolotovCocktailEntity> type, Level world) {
        super(type, world);
    }

    public MolotovCocktailEntity(Level world, LivingEntity owner) {
        super(PSEntities.MOLOTOV_COCKTAIL, owner, world);
    }

    public MolotovCocktailEntity(Level world, double x, double y, double z) {
        super(PSEntities.MOLOTOV_COCKTAIL, x, y, z, world);
    }

    @Override
    protected Item getDefaultItem() {
        return PSItems.MOLOTOV_COCKTAIL;
    }

    @Override
    public ItemStack getItem() {
        ItemStack stack = super.getItem();
        stack.setEntityRepresentation(this);
        return stack;
    }

    public ItemFluids getFluids() {
        return ItemFluids.of(getItem());
    }

    @Override
    public void tick() {
        super.tick();
        ItemStack stack = getItem();
        if (Combustable.fromStack(stack).getFireStrength(ItemFluids.of(stack)) > 0) {
            spawnParticles(1);
        }
    }

    private void spawnParticles(float spread) {
        level().addParticle(ParticleTypes.FLAME,
                level().getRandom().triangle(getX(), 0.5 * spread),
                level().getRandom().triangle(getY(), 0.5 * spread),
                level().getRandom().triangle(getZ(), 0.5 * spread),
                0, 0, 0
        );

        level().addParticle(ParticleTypes.LAVA,
                level().getRandom().triangle(getX(), 0.5 * spread),
                level().getRandom().triangle(getY() + getBbHeight(), 0.5 * spread),
                level().getRandom().triangle(getZ(), 0.5 * spread),
                0, 0, 0
        );
    }


    @Override
    protected void onHitEntity(EntityHitResult hit) {
        super.onHitEntity(hit);
        damageEntity(hit.getEntity(), 1);
        ItemFluids stack = getFluids();
        float explosionStrength = Combustable.fromStack(stack).getExplosionStrength(stack);
        if (explosionStrength > 0) {
            level().getEntities(this, getBoundingBox().inflate(explosionStrength), i -> i.distanceTo(this) <= explosionStrength).forEach(e -> {
                damageEntity(hit.getEntity(), 1 - (e.distanceTo(this) / explosionStrength));
            });
        }
    }

    private void damageEntity(Entity entity, float percentageScale) {
        ItemFluids stack = getFluids();
        Combustable combustable = Combustable.fromStack(stack);
        float explosionStrength = combustable.getExplosionStrength(stack);
        float fireStrength = combustable.getFireStrength(stack);
        entity.hurt(PSDamageTypes.create(level(), getOwner(), this, PSDamageTypes.molotov(entity, getOwner())), percentageScale * Math.max(4, explosionStrength * 0.6F + fireStrength * 0.3F));
        if (fireStrength > 0) {
            entity.isOnFire();
            entity.setRemainingFireTicks((int)Math.max(10, 3 * fireStrength));
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        super.onHit(hitResult);
        if (level().isClientSide || isRemoved()) {
            return;
        }

        playSound(SoundEvents.GLASS_BREAK, 1, 1);

        ItemFluids stack = getFluids();
        Combustable combustable = Combustable.fromStack(stack);
        float explosionStrength = combustable.getExplosionStrength(stack);
        float fireStrength = combustable.getFireStrength(stack);

        if (fireStrength > 0) {
            for (int i = 0; i < fireStrength * 2; i++) {
                level().addParticle(ParticleTypes.FLAME,
                        level().getRandom().triangle(getX(), 0.5 * fireStrength),
                        level().getRandom().triangle(getY(), 0.5 * fireStrength),
                        level().getRandom().triangle(getZ(), 0.5 * fireStrength),
                        0, 0, 0
                );

                level().addParticle(ParticleTypes.LAVA,
                        level().getRandom().triangle(getX(), 0.5 * fireStrength),
                        level().getRandom().triangle(getY() + getBbHeight(), 0.5 * fireStrength),
                        level().getRandom().triangle(getZ(), 0.5 * fireStrength),
                        0, 0, 0
                );
            }
        }

        if (explosionStrength > 0) {
            level().explode(
                    this,
                    damageSources().thrown(this, getOwner()),
                    new ExplosionDamageCalculator() {
                        @Override
                        public boolean shouldBlockExplode(Explosion explosion, BlockGetter world, BlockPos pos, BlockState state, float power) {
                            return state.canBeReplaced();
                        }
                    },
                    position(),
                    explosionStrength,
                    fireStrength > 0, ExplosionInteraction.MOB
            );
        } else {
            playSound(SoundEvents.FIRE_EXTINGUISH, 1, 1);
        }

        if (stack.isIn(FluidTags.LAVA)) {
            BlockPos pos = BlockPos.containing(hitResult.getLocation());
            BlockState replacedState = level().getBlockState(pos);
            if (replacedState.getDestroySpeed(level(), pos) >= 0) {
                if (replacedState.is(BlockTags.CAULDRONS)) {
                    level().setBlockAndUpdate(pos, Blocks.LAVA_CAULDRON.defaultBlockState());
                } else {
                    if (!replacedState.isAir()) {
                        level().destroyBlock(pos, true);
                    }
                    level().setBlockAndUpdate(BlockPos.containing(hitResult.getLocation()), Blocks.LAVA.defaultBlockState());
                }
                level().gameEvent(this, GameEvent.FLUID_PLACE, pos);
            }
        }

        discard();
    }
}
