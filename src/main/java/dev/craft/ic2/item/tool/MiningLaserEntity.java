package dev.craft.ic2.item.tool;

import java.util.List;
import java.util.Optional;

import dev.craft.core.explosion.Ic2Explosion;
import dev.craft.ic2.Ic2EntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.TntBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Mining laser shot (IC2 {@code EntityMiningLaser}): flies one block per tick, loses power per block it breaks and
 * 0.5 per tick in the air, and is not saved with the world.
 */
public class MiningLaserEntity extends Projectile {
    public static final double SPEED = 1.0;

    private float range;
    private float power;
    private int blockBreaks;
    private boolean explosive;
    private boolean smelt;
    private int ticksInAir;

    public MiningLaserEntity(EntityType<? extends MiningLaserEntity> type, Level level) {
        super(type, level);
    }

    public MiningLaserEntity(Level level, LivingEntity owner, float range, float power, int blockBreaks,
                             boolean explosive, boolean smelt, double yawDeg, double pitchDeg, double y) {
        this(Ic2EntityTypes.MINING_LASER.get(), level);
        setOwner(owner);
        double yaw = Math.toRadians(yawDeg);
        double pitch = Math.toRadians(pitchDeg);
        setPos(owner.getX() - Math.cos(yaw) * 0.16, y, owner.getZ() - Math.sin(yaw) * 0.16);
        setLaserHeading(-Math.sin(yaw) * Math.cos(pitch), -Math.sin(pitch), Math.cos(yaw) * Math.cos(pitch));
        this.range = range;
        this.power = power;
        this.blockBreaks = blockBreaks;
        this.explosive = explosive;
        this.smelt = smelt;
    }

    /** Start height of a shot: 0.1 below the eyes. */
    public static double startY(LivingEntity shooter) {
        return shooter.getEyeY() - 0.1;
    }

    private void setLaserHeading(double x, double y, double z) {
        Vec3 motion = new Vec3(x, y, z).normalize().scale(SPEED);
        setDeltaMovement(motion);
        setYRot((float) Math.toDegrees(Math.atan2(motion.x, motion.z)));
        setXRot((float) Math.toDegrees(Math.atan2(motion.y, motion.horizontalDistance())));
        yRotO = getYRot();
        xRotO = getXRot();
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide() && (range < 1.0F || power <= 0.0F || blockBreaks <= 0)) {
            if (explosive) {
                explode();
            }
            discard();
            return;
        }
        ticksInAir++;
        Vec3 motion = getDeltaMovement();
        Vec3 oldPos = position();
        Vec3 newPos = oldPos.add(motion);
        HitResult hit = level().clip(new ClipContext(oldPos, newPos, ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE, this));
        if (hit.getType() == HitResult.Type.MISS) {
            hit = null;
        } else {
            newPos = hit.getLocation();
        }
        Entity hitEntity = findEntity(oldPos, newPos, motion);
        if (!level().isClientSide() && (hitEntity != null || hit != null)) {
            if (explosive) {
                explode();
                discard();
                return;
            }
            if (hitEntity != null) {
                hitEntity(hitEntity);
                discard();
            } else {
                hitBlock(((BlockHitResult) hit).getBlockPos());
            }
        } else {
            power -= 0.5F;
        }
        setPos(getX() + motion.x, getY() + motion.y, getZ() + motion.z);
        range -= (float) motion.length();
        if (isInWater()) {
            discard();
        }
    }

    /** The nearest pickable entity on the path; the shooter only after 5 ticks. */
    private Entity findEntity(Vec3 oldPos, Vec3 newPos, Vec3 motion) {
        Entity owner = getOwner();
        Entity nearest = null;
        double nearestDistance = 0.0;
        List<Entity> entities = level().getEntities(this, getBoundingBox().expandTowards(motion).inflate(1.0));
        for (Entity entity : entities) {
            if (!entity.isPickable() || entity == owner && ticksInAir < 5) {
                continue;
            }
            Optional<Vec3> clip = entity.getBoundingBox().inflate(0.3).clip(oldPos, newPos);
            if (clip.isPresent()) {
                double distance = oldPos.distanceTo(clip.get());
                if (distance < nearestDistance || nearestDistance == 0.0) {
                    nearest = entity;
                    nearestDistance = distance;
                }
            }
        }
        return nearest;
    }

    private void hitEntity(Entity entity) {
        int damage = (int) power;
        if (damage > 0) {
            entity.igniteForSeconds(damage * (smelt ? 2 : 1));
            entity.hurt(damageSources().source(DamageTypes.ARROW, this, getOwner()), damage);
        }
    }

    private void hitBlock(BlockPos pos) {
        ServerLevel level = (ServerLevel) level();
        BlockState state = level.getBlockState(pos);
        if (!canMine(state)) {
            discard();
            return;
        }
        Explosion explosion = new Explosion(level, this, getX(), getY(), getZ(), 0.0F, false,
                Explosion.BlockInteraction.KEEP);
        power -= (state.getExplosionResistance(level, pos, explosion) + 0.3F) / 10.0F;
        if (power < 0.0F) {
            return;
        }
        boolean removeBlock = true;
        boolean dropBlock = true;
        if (state.getBlock() instanceof TntBlock) {
            state.onCaughtFire(level, pos, null, getOwner() instanceof LivingEntity living ? living : null);
        } else if (smelt) {
            if (state.is(BlockTags.MINEABLE_WITH_AXE)) {
                dropBlock = false;
            } else {
                boolean blockSet = false;
                for (ItemStack drop : Block.getDrops(state, level, pos, level.getBlockEntity(pos), this,
                        ItemStack.EMPTY)) {
                    ItemStack result = level.getRecipeManager()
                            .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(drop), level)
                            .map(recipe -> recipe.value().getResultItem(level.registryAccess()).copy())
                            .orElse(ItemStack.EMPTY);
                    if (result.isEmpty()) {
                        continue;
                    }
                    if (!blockSet && result.getItem() instanceof BlockItem blockItem
                            && blockItem.getBlock() != state.getBlock()) {
                        blockSet = true;
                        removeBlock = false;
                        dropBlock = false;
                        level.setBlock(pos, blockItem.getBlock().defaultBlockState(), Block.UPDATE_ALL);
                    } else {
                        dropBlock = false;
                        spawnSmelted(level, pos, result);
                    }
                    power = 0.0F;
                }
            }
        }
        if (removeBlock) {
            if (dropBlock) {
                for (ItemStack drop : Block.getDrops(state, level, pos, level.getBlockEntity(pos), this,
                        ItemStack.EMPTY)) {
                    if (level.random.nextFloat() <= 0.9F) {
                        Block.popResource(level, pos, drop);
                    }
                }
                state.spawnAfterBreak(level, pos, ItemStack.EMPTY, true);
            }
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            if (level.random.nextInt(10) == 0 && state.ignitedByLava()) {
                level.setBlock(pos, BaseFireBlock.getState(level, pos), Block.UPDATE_ALL);
            }
        }
        blockBreaks--;
    }

    private static void spawnSmelted(Level level, BlockPos pos, ItemStack stack) {
        float spread = 0.7F;
        double x = level.random.nextFloat() * spread + (1.0F - spread) * 0.5;
        double y = level.random.nextFloat() * spread + (1.0F - spread) * 0.5;
        double z = level.random.nextFloat() * spread + (1.0F - spread) * 0.5;
        ItemEntity item = new ItemEntity(level, pos.getX() + x, pos.getY() + y, pos.getZ() + z, stack);
        item.setPickUpDelay(10);
        level.addFreshEntity(item);
    }

    /** IC2 {@code unmineableBlocks}: brick, obsidian, water, lava, bedrock, glass. */
    public static boolean canMine(BlockState state) {
        return !state.is(Blocks.BRICKS) && !state.is(Blocks.OBSIDIAN) && !state.is(Blocks.WATER)
                && !state.is(Blocks.LAVA) && !state.is(Blocks.BEDROCK) && !state.is(Blocks.GLASS);
    }

    private void explode() {
        if (level() instanceof ServerLevel level) {
            new Ic2Explosion(level, getX(), getY(), getZ(), 5.0F, 0.85F, 0.55F).explode();
        }
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        double size = getBoundingBox().getSize() * 10.0;
        if (Double.isNaN(size)) {
            size = 1.0;
        }
        size *= 64.0 * getViewScale();
        return distance < size * size;
    }

    @Override
    public void lerpMotion(double x, double y, double z) {
        super.lerpMotion(x, y, z);
        if (xRotO == 0.0F && yRotO == 0.0F) {
            setYRot((float) (Mth.atan2(x, z) * Mth.RAD_TO_DEG));
            setXRot((float) (Mth.atan2(y, Math.sqrt(x * x + z * z)) * Mth.RAD_TO_DEG));
            yRotO = getYRot();
            xRotO = getXRot();
        }
    }
}
