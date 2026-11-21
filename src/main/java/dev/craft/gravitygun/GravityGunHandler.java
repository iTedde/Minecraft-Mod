package dev.craft.gravitygun;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.EnderDragonPart;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.decoration.Painting;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;

/**
 * GraviGun server logic ({@code TickHandlerServer}, {@code EntityHelper}; spec 50 §3) with the pack config: no fuel,
 * charged gun also pulls blocks from afar, released blocks fall, most entities and players grabbable, thrown creepers
 * explode only from the charged gun.
 */
public final class GravityGunHandler {
    public static final double GRAB_RANGE = 4.0;
    public static final double HOLD_DISTANCE = 3.5;

    /** Holder → held entity. */
    private static final Map<LivingEntity, Entity> GRABBED = new HashMap<>();
    /** Players holding the use key without having caught anything ({@code holdingKey}). */
    private static final Set<UUID> PULLING = new HashSet<>();
    /** Thrown entity → speed of the last tick ({@code tossedEntities}). */
    private static final Map<Entity, Double> TOSSED = new HashMap<>();
    private static final Map<Entity, LivingEntity> TOSSER = new HashMap<>();
    /** Creepers thrown by a charged gun. */
    private static final Set<Entity> CHARGED_THROW = new HashSet<>();

    private GravityGunHandler() {
    }

    public static boolean isGrabbed(Entity entity) {
        return GRABBED.containsValue(entity);
    }

    public static Entity grabbed(LivingEntity holder) {
        return GRABBED.get(holder);
    }

    public static boolean isPulling(Player player) {
        return PULLING.contains(player.getUUID());
    }

    /** The gravity gun in the main hand, or empty. */
    public static ItemStack gun(LivingEntity holder) {
        ItemStack stack = holder.getMainHandItem();
        return stack.getItem() instanceof GravityGunItem ? stack : ItemStack.EMPTY;
    }

    private static boolean charged(LivingEntity holder) {
        return gun(holder).getItem() instanceof GravityGunItem item && item.isCharged();
    }

    private static double range(LivingEntity holder) {
        return charged(holder) ? 15.0 : 10.0;
    }

    /** {@code EntityHelper.canGrab}: everything but paintings, the ender dragon and its parts. */
    public static boolean canGrab(Entity entity) {
        return !(entity instanceof Painting) && !(entity instanceof EnderDragon) && !(entity instanceof EnderDragonPart)
                && !entity.isSpectator() && !(entity instanceof GrabbedBlock block && block.isRemoved());
    }

    /** Grabbable blocks: breakable, and a full cube, chest, glass, glowstone, cake, TNT, ice, leaves or anvil. */
    public static boolean canGrab(Level level, BlockPos pos, BlockState state) {
        if (state.isAir() || state.getDestroySpeed(level, pos) < 0.0F || state.is(Blocks.MOVING_PISTON)) {
            return false;
        }
        return state.isCollisionShapeFullBlock(level, pos) && state.isSolidRender(level, pos)
                || state.getBlock() instanceof ChestBlock || state.is(Blocks.GLASS) || state.is(Blocks.GLOWSTONE)
                || state.is(Blocks.CAKE) || state.is(Blocks.TNT) || state.is(Blocks.ICE) || state.is(BlockTags.LEAVES)
                || state.is(BlockTags.ANVIL);
    }

    /** {@code getEntityLook}: the nearest block or entity in the holder's line of sight. */
    public static HitResult look(LivingEntity holder, double range) {
        Vec3 eye = holder.getEyePosition();
        Vec3 end = eye.add(holder.getLookAngle().scale(range));
        BlockHitResult block = holder.level().clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE,
                ClipContext.Fluid.NONE, holder));
        double reach = block.getType() == HitResult.Type.MISS ? range : block.getLocation().distanceTo(eye);
        AABB box = holder.getBoundingBox().expandTowards(holder.getLookAngle().scale(reach)).inflate(1.0);
        EntityHitResult entity = ProjectileUtil.getEntityHitResult(holder, eye, eye.add(holder.getLookAngle()
                .scale(reach)), box, target -> target != holder && target.isPickable(), reach * reach);
        return entity != null ? entity : block;
    }

    private static void sound(LivingEntity holder, SoundEvent sound, float pitch) {
        holder.level().playSound(null, holder.getX(), holder.getY(), holder.getZ(), sound, SoundSource.PLAYERS, 0.3F,
                pitch);
    }

    public static void setGrabbed(LivingEntity holder, Entity entity) {
        GRABBED.entrySet().removeIf(entry -> entry.getValue() == entity && entry.getKey() != holder);
        if (entity == null) {
            GRABBED.remove(holder);
        } else {
            TOSSED.remove(entity);
            GRABBED.put(holder, entity);
        }
    }

    /** Use key pressed: drop what is held, or grab what is within 4 blocks, or start pulling. */
    public static void useKey(LivingEntity holder) {
        Entity held = grabbed(holder);
        if (held != null) {
            if (held instanceof AbstractHurtingProjectile fireball) {
                fireball.accelerationPower = fireball.getDeltaMovement().length() / 8.0;
            }
            setGrabbed(holder, null);
            sound(holder, SoundEvents.BEACON_DEACTIVATE, 1.4F);
            return;
        }
        tryGrab(holder);
    }

    /** {@code EntityHelper.tryGrab}. */
    public static void tryGrab(LivingEntity holder) {
        HitResult hit = look(holder, GRAB_RANGE);
        boolean success = false;
        if (hit.getType() == HitResult.Type.MISS) {
            if (holder instanceof Player player) {
                PULLING.add(player.getUUID());
            }
        } else if (hit instanceof BlockHitResult blockHit) {
            BlockPos pos = blockHit.getBlockPos();
            Level level = holder.level();
            boolean griefing = !(holder instanceof Zombie)
                    || level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING);
            if (griefing && canGrab(level, pos, level.getBlockState(pos))) {
                GrabbedBlock block = GrabbedBlock.lift(level, pos);
                level.addFreshEntity(block);
                setGrabbed(holder, block);
                success = true;
            }
        } else if (hit instanceof EntityHitResult entityHit) {
            Entity target = entityHit.getEntity();
            if (holder.getVehicle() != target && !holder.hasPassenger(target) && canGrab(target)) {
                setGrabbed(holder, target.getVehicle() != null ? target.getVehicle() : target);
                success = true;
            }
        }
        if (success) {
            sound(holder, SoundEvents.BEACON_POWER_SELECT, 1.6F);
        }
    }

    /** Use key released: stop pulling. */
    public static void releaseKey(Player player) {
        PULLING.remove(player.getUUID());
    }

    /** {@code EntityHelper.tryThrow}: throws what is held, or punts what is in range. */
    public static void tryThrow(LivingEntity holder) {
        Entity target = grabbed(holder);
        if (target == null) {
            HitResult hit = look(holder, range(holder));
            if (hit instanceof EntityHitResult entityHit && canGrab(entityHit.getEntity())) {
                target = entityHit.getEntity();
            }
        } else {
            setGrabbed(holder, null);
        }
        sound(holder, SoundEvents.FIREWORK_ROCKET_BLAST, 0.8F);
        if (target == null) {
            return;
        }
        double distance = Math.max(3.0, holder.distanceTo(target));
        Vec3 centre = target.getBoundingBox().getCenter();
        double amp = (charged(holder) ? 12.0 : 10.0) / distance * 0.3;
        Vec3 push = new Vec3((centre.x - holder.getX()) * amp, (centre.y - holder.getEyeY()) * 0.52,
                (centre.z - holder.getZ()) * amp);
        target.setDeltaMovement(target.getDeltaMovement().add(push));
        target.hurtMarked = true;
        if (target instanceof ServerPlayer player) {
            player.connection.send(new ClientboundSetEntityMotionPacket(player));
        } else if (target instanceof Creeper) {
            if (charged(holder)) {
                CHARGED_THROW.add(target);
            } else {
                CHARGED_THROW.remove(target);
            }
        } else if (target instanceof AbstractHurtingProjectile fireball) {
            fireball.accelerationPower = target.getDeltaMovement().length() / 8.0;
        }
        if (!target.onGround()) {
            TOSSED.put(target, target.getDeltaMovement().length());
            TOSSER.put(target, holder);
        }
    }

    /** Server tick of one level: pulling, holding and thrown entities. */
    public static void tick(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            if (PULLING.contains(player.getUUID())) {
                pull(player);
            }
        }
        for (Map.Entry<LivingEntity, Entity> entry : List.copyOf(GRABBED.entrySet())) {
            if (entry.getKey().level() == level) {
                hold(entry.getKey(), entry.getValue());
            }
        }
        for (Iterator<Map.Entry<Entity, Double>> it = TOSSED.entrySet().iterator(); it.hasNext();) {
            Map.Entry<Entity, Double> entry = it.next();
            if (entry.getKey().level() == level && tossed(level, entry.getKey(), entry)) {
                TOSSER.remove(entry.getKey());
                CHARGED_THROW.remove(entry.getKey());
                it.remove();
            }
        }
    }

    /** Holding the use key at nothing: pulls entities (and with the charged gun blocks) in, catching them at 4. */
    private static void pull(Player player) {
        if (gun(player).isEmpty()) {
            PULLING.remove(player.getUUID());
            return;
        }
        double range = range(player);
        HitResult hit = look(player, range);
        if (hit instanceof EntityHitResult entityHit && canGrab(entityHit.getEntity())) {
            Entity target = entityHit.getEntity();
            if (target instanceof GrabbedBlock block) {
                block.age = -2;
            }
            double distance = player.distanceTo(target);
            if (distance <= GRAB_RANGE) {
                sound(player, SoundEvents.BEACON_POWER_SELECT, 1.6F);
                setGrabbed(player, target);
                PULLING.remove(player.getUUID());
            } else {
                double mag = (charged(player) ? 0.02 : 0.01) * range / distance;
                Vec3 centre = target.getBoundingBox().getCenter();
                target.push((player.getX() - target.getX()) * mag, (player.getEyeY() - centre.y) * mag,
                        (player.getZ() - target.getZ()) * mag);
                target.hurtMarked = true;
            }
        } else if (hit instanceof BlockHitResult blockHit && hit.getType() == HitResult.Type.BLOCK && charged(player)) {
            BlockPos pos = blockHit.getBlockPos();
            if (canGrab(player.level(), pos, player.level().getBlockState(pos))) {
                GrabbedBlock block = GrabbedBlock.lift(player.level(), pos);
                block.age = -2;
                player.level().addFreshEntity(block);
            }
        }
    }

    /** {@code updateGrabbing}: keeps the held entity 3.5 blocks in front of the eyes. */
    private static void hold(LivingEntity holder, Entity held) {
        boolean holding = !gun(holder).isEmpty();
        if (held.isRemoved() || held == holder.getVehicle() || !holding || !holder.isAlive()
                || held.level() != holder.level()) {
            setGrabbed(holder, null);
            if (holding) {
                sound(holder, SoundEvents.BEACON_DEACTIVATE, 1.4F);
            }
            return;
        }
        Vec3 point = holder.getEyePosition().add(holder.getLookAngle().scale(HOLD_DISTANCE));
        if (held.position().distanceTo(point) > HOLD_DISTANCE) {
            if (held instanceof EnderMan && held.position().distanceTo(point) > 6.0) {
                setGrabbed(holder, null);
                sound(holder, SoundEvents.BEACON_DEACTIVATE, 1.4F);
                return;
            }
            held.setPos(holder.getX(), holder.getY(), holder.getZ());
        }
        if (held instanceof AbstractHurtingProjectile fireball) {
            fireball.accelerationPower = 0.0;
        }
        Vec3 centre = held.getBoundingBox().getCenter();
        held.setDeltaMovement(point.x - held.getX(), point.y - centre.y, point.z - held.getZ());
        held.fallDistance = 0.0F;
        held.setOnGround(false);
        held.hurtMarked = true;
        if (held instanceof FallingBlockEntity falling) {
            falling.time = 2;
        }
        if (held instanceof ServerPlayer player) {
            player.connection.send(new ClientboundSetEntityMotionPacket(player));
        }
    }

    /**
     * A thrown entity: hitting another entity hurts it ({@code ⌊2.5 × speed⌋}, scaled by the size ratio); hitting a
     * wall or the ground hurts itself. Creepers explode on impact (pack: charged gun only).
     *
     * @return whether it has landed
     */
    private static boolean tossed(ServerLevel level, Entity thrown, Map.Entry<Entity, Double> entry) {
        double speed = entry.getValue();
        LivingEntity tosser = TOSSER.get(thrown);
        Vec3 from = thrown.position();
        Vec3 to = from.add(thrown.getDeltaMovement());
        AABB path = thrown.getBoundingBox().expandTowards(thrown.getDeltaMovement()).inflate(1.0);
        Entity victim = null;
        double nearest = 0.0;
        for (Entity other : level.getEntities(thrown, path, Entity::isPickable)) {
            var clip = other.getBoundingBox().inflate(0.3).clip(from, to);
            if (clip.isPresent()) {
                double distance = from.distanceTo(clip.get());
                if (victim == null || distance < nearest) {
                    victim = other;
                    nearest = distance;
                }
            }
        }
        if (victim != null) {
            int damage = Mth.floor(2.5 * speed);
            double volume = Math.cbrt(thrown.getBbHeight() * thrown.getBbWidth() * thrown.getBbWidth());
            double volumeHit = Math.cbrt(victim.getBbHeight() * victim.getBbWidth() * victim.getBbWidth());
            damage = (int) (damage * (volume / volumeHit / 0.925));
            if (damage > 0) {
                victim.hurt(level.damageSources().thrown(thrown, tosser), damage);
            }
            if (thrown instanceof Creeper creeper) {
                explode(level, creeper);
            }
        }
        if (!thrown.horizontalCollision && !thrown.verticalCollision && !thrown.isRemoved()) {
            entry.setValue(thrown.getDeltaMovement().length());
            return false;
        }
        if (thrown instanceof Creeper creeper && !creeper.isRemoved()) {
            explode(level, creeper);
        }
        int damage = Mth.floor(2.5 * speed);
        if (damage > 0 && !thrown.isRemoved()) {
            thrown.hurt(level.damageSources().thrown(thrown, tosser), damage);
        }
        return true;
    }

    /** {@code creeperExplosion=2}: only creepers thrown by the charged gun explode. */
    private static void explode(ServerLevel level, Creeper creeper) {
        if (!CHARGED_THROW.remove(creeper) || creeper.isRemoved()) {
            return;
        }
        float radius = creeper.isPowered() ? 6.0F : 3.0F;
        level.explode(creeper, creeper.getX(), creeper.getY(), creeper.getZ(), radius, Level.ExplosionInteraction.MOB);
        creeper.discard();
    }

    /** {@code worldTick}: zombies holding a gravity gun grab, drop and throw at random (0.8 % per tick). */
    public static void zombieTick(Zombie zombie) {
        if (gun(zombie).isEmpty() || zombie.getRandom().nextFloat() >= 0.008F) {
            return;
        }
        if (zombie.getRandom().nextFloat() < 0.6F) {
            if (grabbed(zombie) != null) {
                setGrabbed(zombie, null);
                sound(zombie, SoundEvents.BEACON_DEACTIVATE, 1.4F);
            } else {
                tryGrab(zombie);
            }
        } else {
            tryThrow(zombie);
        }
    }

    /** Lightning within reach charges every normal gun in a player's main inventory. */
    public static void charge(Player player) {
        var items = player.getInventory().items;
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getItem() instanceof GravityGunItem item && !item.isCharged()) {
                items.set(i, new ItemStack(GravityGun.CHARGED_GRAVITY_GUN.get()));
            }
        }
    }

    /** Player left or the server stops. */
    public static void forget(Player player) {
        PULLING.remove(player.getUUID());
        GRABBED.remove(player);
    }

    public static void clear() {
        GRABBED.clear();
        PULLING.clear();
        TOSSED.clear();
        TOSSER.clear();
        CHARGED_THROW.clear();
    }
}
