package dev.craft.chargepads.particle;

import dev.craft.chargepads.PadTier;
import dev.craft.chargepads.block.ChargePadBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

/**
 * Spark emission of an active pad (spec 47 §5.1, original {@code w.c} with the tier overrides of {@code ac}/{@code ad}/
 * {@code ab}). Called from {@code animateTick}, so only on the client.
 */
public final class ChargePadParticles {
    private ChargePadParticles() {
    }

    public static void spawn(Level level, BlockPos pos, PadTier tier, ChargePadBlockEntity pad, RandomSource random) {
        int expansion = pad.getClientFieldExpansion();
        float width = expansion * 2 + 0.9F;
        int x = pos.getX() - expansion;
        int z = pos.getZ() - expansion;
        int perColumn = tier == PadTier.CRYSTALIZOR ? 5 : 6;
        int lifetime = tier == PadTier.STATIC ? 5 : 14;
        double rise = tier == PadTier.STATIC ? 3.0 : 7.6;
        boolean damage = pad.isClientDamage();
        boolean drain = pad.isClientDrain();
        for (int column = 1 + expansion * 2; column > 0; column--) {
            for (int i = perColumn; i > 0; i--) {
                double px = x + 0.05F + random.nextFloat() * width;
                double py = pos.getY() + 0.2F + random.nextFloat() * 0.2F;
                double pz = z + 0.05F + random.nextFloat() * width;
                float red = damage || drain ? 1.0F : 0.0F;
                float base = damage ? 0.0F : 0.6F;
                float green = base + random.nextFloat() * 0.4F;
                float blue = tier == PadTier.LAPOTRONIC ? base + random.nextFloat() * 0.4F : 0.0F;
                level.addParticle(new ChargeSparkOptions(red, green, blue, lifetime), px, py, pz, 0.0, rise, 0.0);
            }
        }
    }
}
