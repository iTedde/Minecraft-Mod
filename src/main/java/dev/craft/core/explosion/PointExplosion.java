package dev.craft.core.explosion;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * IC2 {@code PointExplosion} (spec 05 §7): destroys every block in a cube around a block position whose resistance is
 * at most ten times the power, then hurts entities like a small vanilla explosion of twice the power. Original quirks:
 * the centre is the block corner, negative x/z are truncated and then decremented, and no particles are shown (IC2 only
 * spawned them on the server).
 */
public final class PointExplosion {
    private final ServerLevel level;
    private final int x;
    private final int y;
    private final int z;
    private final float power;
    private final float dropRate;
    private final float entityDamage;
    private final Explosion vanillaExplosion;

    public PointExplosion(ServerLevel level, double x, double y, double z, float power, float dropRate,
                          float entityDamage) {
        this.level = level;
        int blockX = (int) x;
        int blockZ = (int) z;
        this.x = blockX < 0 ? blockX - 1 : blockX;
        this.y = (int) y;
        this.z = blockZ < 0 ? blockZ - 1 : blockZ;
        this.power = power;
        this.dropRate = dropRate;
        this.entityDamage = entityDamage;
        this.vanillaExplosion = new Explosion(level, null, this.x, this.y, this.z, power, false,
                Explosion.BlockInteraction.DESTROY);
    }

    /** {@code doExplosionA} + {@code doExplosionB} for a cube reaching {@code radius} blocks in every direction. */
    public void explode(int radius) {
        List<BlockPos> destroyed = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(x - radius, y - radius, z - radius, x + radius, y + radius,
                z + radius)) {
            BlockState state = level.getBlockState(pos);
            float resistance = state.isAir() ? 0.0F
                    : Ic2Explosion.explosionResistance(level, pos, state, vanillaExplosion);
            if (power >= resistance / 10.0F) {
                destroyed.add(pos.immutable());
            }
        }
        hurtEntities(power * 2.0F);

        float pitch = (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F;
        level.playSound(null, x, y, z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.BLOCKS, 4.0F, pitch);
        for (BlockPos pos : destroyed) {
            BlockState state = level.getBlockState(pos);
            if (state.isAir()) {
                continue;
            }
            BlockEntity blockEntity = level.getBlockEntity(pos);
            for (ItemStack stack : Block.getDrops(state, level, pos, blockEntity)) {
                if (level.random.nextFloat() <= dropRate) {
                    Block.popResource(level, pos, stack);
                }
            }
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
            state.getBlock().wasExploded(level, pos, vanillaExplosion);
        }
    }

    private void hurtEntities(float size) {
        AABB area = new AABB(Mth.floor(x - size - 1.0), Mth.floor(y - size - 1.0), Mth.floor(z - size - 1.0),
                Mth.floor(x + size + 1.0), Mth.floor(y + size + 1.0), Mth.floor(z + size + 1.0));
        Vec3 centre = new Vec3(x, y, z);
        for (Entity entity : level.getEntities((Entity) null, area)) {
            double distance = Math.sqrt(entity.distanceToSqr(centre)) / size;
            if (distance > 1.0) {
                continue;
            }
            double impact = (1.0 - distance) * Explosion.getSeenPercent(centre, entity);
            entity.hurt(level.damageSources().explosion(null, null),
                    (int) (((impact * impact + impact) / 2.0 * 8.0 * size + 1.0) * entityDamage));
            Vec3 offset = entity.position().subtract(centre);
            double length = offset.length();
            if (length > 0.0) {
                entity.setDeltaMovement(entity.getDeltaMovement().add(offset.scale(impact / length)));
                entity.hurtMarked = true;
            }
        }
    }
}
