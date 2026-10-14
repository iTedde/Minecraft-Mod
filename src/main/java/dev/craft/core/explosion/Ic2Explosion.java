package dev.craft.core.explosion;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import dev.craft.Craft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * IC2 {@code ExplosionIC2}: ray based explosion used for machine overvoltage, reactors, nukes and ITNT
 * (see {@code docs/spec/01-eu-system.md} §4). Original quirks are kept on purpose: block coordinates are truncated
 * with {@code (int)} instead of floored, and the entity loop always hits the entry found by the binary search.
 */
public final class Ic2Explosion {
    /** Blocks with absorption above 1000 stop rays instead of being skipped (IC2 {@code ExplosionWhitelist}). */
    public static final TagKey<Block> WHITELIST =
            TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(Craft.MODID, "explosion_whitelist"));

    public static final float MACHINE_POWER = 2.5F;
    public static final float MACHINE_DROP_RATE = 0.75F;
    public static final float MACHINE_ENTITY_DAMAGE = 0.75F;

    private static final double DROP_POWER_LIMIT = 8.0;
    private static final double DAMAGE_AT_DROP_POWER_LIMIT = 32.0;
    private static final double ACCELERATION_AT_DROP_POWER_LIMIT = 0.7;
    private static final int SECONDARY_RAY_COUNT = 5;

    private final Random rayRandom = new Random();
    private final ServerLevel level;
    private final double x;
    private final double y;
    private final double z;
    private final float power;
    private final float dropRate;
    private final float entityDamage;
    private final DamageSource damageSource;
    private final Explosion vanillaExplosion;
    private final BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
    private final List<Map.Entry<Integer, Entity>> entitiesInRange = new ArrayList<>();
    /** Block position to "drop items" flag; insertion ordered like the processing order. */
    private final Map<BlockPos, Boolean> destroyedBlocks = new LinkedHashMap<>();

    public Ic2Explosion(ServerLevel level, double x, double y, double z, float power, float dropRate,
                        float entityDamage, DamageSource damageSource) {
        this.level = level;
        this.x = x;
        this.y = y;
        this.z = z;
        this.power = power;
        this.dropRate = dropRate;
        this.entityDamage = entityDamage;
        this.damageSource = damageSource;
        this.vanillaExplosion = new Explosion(level, null, x, y, z, power, false, Explosion.BlockInteraction.DESTROY);
    }

    public Ic2Explosion(ServerLevel level, double x, double y, double z, float power, float dropRate,
                        float entityDamage) {
        this(level, x, y, z, power, dropRate, entityDamage, level.damageSources().explosion(null, null));
    }

    /** IC2 {@code explodeMachineAt}: removes the block without drops and explodes with power 2.5. */
    public static void explodeMachineAt(ServerLevel level, BlockPos pos) {
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        new Ic2Explosion(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                MACHINE_POWER, MACHINE_DROP_RATE, MACHINE_ENTITY_DAMAGE).explode();
    }

    public float getEntityDamage() {
        return entityDamage;
    }

    public void explode() {
        if (power <= 0.0F) {
            return;
        }
        double maxDistance = power / 0.4;
        List<Entity> entities = level.getEntities((Entity) null,
                new AABB(x - maxDistance, y - maxDistance, z - maxDistance,
                        x + maxDistance, y + maxDistance, z + maxDistance));
        boolean entitiesAreInRange = !entities.isEmpty();
        if (entitiesAreInRange) {
            for (Entity entity : entities) {
                if (entity instanceof LivingEntity || entity instanceof ItemEntity) {
                    double dx = entity.getX() - x;
                    double dy = entity.getY() - y;
                    double dz = entity.getZ() - z;
                    entitiesInRange.add(new SimpleEntry<>((int) (dx * dx + dy * dy + dz * dz), entity));
                }
            }
            entitiesInRange.sort(Map.Entry.comparingByKey());
        }

        int steps = (int) Math.ceil(Math.PI / Math.atan(1.0 / maxDistance));
        for (int phiN = 0; phiN < 2 * steps; phiN++) {
            for (int thetaN = 0; thetaN < steps; thetaN++) {
                double phi = Math.PI * 2 / steps * phiN;
                double theta = Math.PI / steps * thetaN;
                shootRay(x, y, z, phi, theta, power, entitiesAreInRange && phiN % 8 == 0 && thetaN % 8 == 0);
            }
        }

        float pitch = (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F;
        level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 4.0F, pitch);
        level.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);

        Map<Long, List<Drop>> drops = new LinkedHashMap<>();
        for (Map.Entry<BlockPos, Boolean> entry : destroyedBlocks.entrySet()) {
            BlockPos pos = entry.getKey();
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) {
                continue;
            }
            if (entry.getValue()) {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                for (ItemStack stack : Block.getDrops(state, level, pos, blockEntity)) {
                    if (level.random.nextFloat() <= dropRate) {
                        long column = (long) (pos.getX() / 2) << 32 | (pos.getZ() / 2) & 0xFFFFFFFFL;
                        addDrop(drops.computeIfAbsent(column, k -> new ArrayList<>()), stack, pos.getY());
                    }
                }
            }
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            state.getBlock().wasExploded(level, pos, vanillaExplosion);
        }

        for (Map.Entry<Long, List<Drop>> entry : drops.entrySet()) {
            int columnX = (int) (entry.getKey() >> 32);
            int columnZ = (int) (long) entry.getKey();
            for (Drop drop : entry.getValue()) {
                int count = drop.count;
                int maxStackSize = drop.stack.getMaxStackSize();
                while (count > 0) {
                    int stackSize = Math.min(count, Math.min(64, maxStackSize));
                    ItemEntity item = new ItemEntity(level,
                            (columnX + level.random.nextFloat()) * 2.0, drop.maxY + 0.5,
                            (columnZ + level.random.nextFloat()) * 2.0,
                            drop.stack.copyWithCount(stackSize));
                    item.setPickUpDelay(10);
                    level.addFreshEntity(item);
                    count -= stackSize;
                }
            }
        }
    }

    private static void addDrop(List<Drop> column, ItemStack stack, int y) {
        for (Drop drop : column) {
            if (ItemStack.isSameItemSameComponents(drop.stack, stack)) {
                drop.count += stack.getCount();
                drop.maxY = Math.max(drop.maxY, y);
                return;
            }
        }
        column.add(new Drop(stack.copyWithCount(1), stack.getCount(), y));
    }

    private void shootRay(double x, double y, double z, double phi, double theta, double power,
                          boolean killEntities) {
        double deltaX = Math.sin(theta) * Math.cos(phi);
        double deltaY = Math.cos(theta);
        double deltaZ = Math.sin(theta) * Math.sin(phi);
        int step = 0;

        while (true) {
            cursor.set((int) x, (int) y, (int) z);
            BlockState state = level.isLoaded(cursor) ? level.getBlockState(cursor) : Blocks.AIR.defaultBlockState();
            boolean isBlock = !state.isAir();
            double absorption = 0.5;
            if (isBlock) {
                absorption += (explosionResistance(state) + 4.0) * 0.3;
            }

            if (absorption > 1000.0 && !state.is(WHITELIST)) {
                absorption = 0.5;
            } else {
                if (absorption > power) {
                    break;
                }
                if (isBlock) {
                    BlockPos position = cursor.immutable();
                    if (power > DROP_POWER_LIMIT) {
                        destroyedBlocks.put(position, false);
                    } else {
                        destroyedBlocks.putIfAbsent(position, true);
                    }
                }
            }

            if (killEntities && (step + 4) % 8 == 0 && power >= 0.25) {
                hitEntities(x, y, z, step, power);
            }

            if (absorption > 10.0) {
                for (int i = 0; i < SECONDARY_RAY_COUNT; i++) {
                    shootRay(x, y, z, rayRandom.nextDouble() * 2.0 * Math.PI, rayRandom.nextDouble() * Math.PI,
                            absorption * 0.4, false);
                }
            }

            power -= absorption;
            x += deltaX;
            y += deltaY;
            z += deltaZ;
            if (y <= level.getMinBuildHeight() || y >= level.getMaxBuildHeight()) {
                break;
            }
            step++;
        }
    }

    /** IC2 entity damage step; deliberately indexes the binary search result instead of the loop variable. */
    private void hitEntities(double rayX, double rayY, double rayZ, int step, double power) {
        if (entitiesInRange.isEmpty()) {
            return;
        }
        int index;
        if (step != 4) {
            int distanceMin = step * step - 25;
            int indexStart = 0;
            int indexEnd = entitiesInRange.size() - 1;
            do {
                index = (indexStart + indexEnd) / 2;
                int distance = entitiesInRange.get(index).getKey();
                if (distance < distanceMin) {
                    indexStart = index + 1;
                } else if (distance > distanceMin) {
                    indexEnd = index - 1;
                } else {
                    indexEnd = index;
                }
            } while (indexStart < indexEnd);
        } else {
            index = 0;
        }

        int distanceMax = step * step + 25;
        for (int i = index; i < entitiesInRange.size() && index < entitiesInRange.size()
                && entitiesInRange.get(index).getKey() < distanceMax; i++) {
            Entity entity = entitiesInRange.get(index).getValue();
            double ex = entity.getX() - rayX;
            double ey = entity.getY() - rayY;
            double ez = entity.getZ() - rayZ;
            if (ex * ex + ey * ey + ez * ez <= 25.0) {
                entity.hurt(damageSource, (int) (DAMAGE_AT_DROP_POWER_LIMIT * power / DROP_POWER_LIMIT));
                double dx = entity.getX() - x;
                double dy = entity.getY() - y;
                double dz = entity.getZ() - z;
                double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                double factor = ACCELERATION_AT_DROP_POWER_LIMIT * power / DROP_POWER_LIMIT / distance;
                entity.setDeltaMovement(entity.getDeltaMovement().add(new Vec3(dx * factor, dy * factor, dz * factor)));
                entity.hurtMarked = true;
                if (!entity.isAlive()) {
                    entitiesInRange.remove(i);
                    i--;
                }
            }
        }
    }

    /**
     * Explosion resistance in 1.21 units; IC2 lowered some vanilla values globally ({@code setResistance(60)} on
     * obsidian, enchanting table, ender chest, anvil → 36; {@code setResistance(30)} on water and still lava → 18).
     * Here the changed values apply to IC2 explosions only.
     */
    private float explosionResistance(BlockState state) {
        if (state.is(Blocks.OBSIDIAN) || state.is(Blocks.ENCHANTING_TABLE) || state.is(Blocks.ENDER_CHEST)
                || state.is(Blocks.ANVIL) || state.is(Blocks.CHIPPED_ANVIL) || state.is(Blocks.DAMAGED_ANVIL)) {
            return 36.0F;
        }
        if (state.getBlock() instanceof LiquidBlock) {
            if (state.getFluidState().is(Fluids.WATER) || state.getFluidState().is(Fluids.FLOWING_WATER)) {
                return 18.0F;
            }
            // still lava had resistance 30 (→ 18), flowing lava none
            return state.getFluidState().isSource() ? 18.0F : 0.0F;
        }
        return state.getExplosionResistance(level, cursor, vanillaExplosion);
    }

    private static final class Drop {
        final ItemStack stack;
        int count;
        int maxY;

        Drop(ItemStack stack, int count, int maxY) {
            this.stack = stack;
            this.count = count;
            this.maxY = maxY;
        }
    }
}
