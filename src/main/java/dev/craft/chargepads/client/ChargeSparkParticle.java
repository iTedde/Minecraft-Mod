package dev.craft.chargepads.client;

import dev.craft.chargepads.particle.ChargeSparkOptions;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;

/**
 * Charge pad spark (original {@code j}): the base particle's randomised, normalised start motion, vertical motion
 * × 0.2, size 0.02 without collision, no gravity, damping 0.99, lifetime base / (0.2 … 1.0).
 */
public class ChargeSparkParticle extends TextureSheetParticle {
    protected ChargeSparkParticle(ClientLevel level, double x, double y, double z, double xd, double yd, double zd,
                                  ChargeSparkOptions options) {
        super(level, x, y, z, xd, yd, zd);
        setColor(options.red(), options.green(), options.blue());
        setSize(0.02F, 0.02F);
        quadSize *= random.nextFloat() * 0.5F + 0.5F;
        this.yd *= 0.2;
        lifetime = (int) (options.lifetime() / (Math.random() * 0.8 + 0.2));
        hasPhysics = false;
        gravity = 0.0F;
    }

    @Override
    public void tick() {
        xo = x;
        yo = y;
        zo = z;
        move(xd, yd, zd);
        xd *= 0.99;
        yd *= 0.99;
        zd *= 0.99;
        if (lifetime-- <= 0) {
            remove();
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    public record Provider(SpriteSet sprites) implements ParticleProvider<ChargeSparkOptions> {
        @Override
        public Particle createParticle(ChargeSparkOptions options, ClientLevel level, double x, double y, double z,
                                       double xd, double yd, double zd) {
            ChargeSparkParticle particle = new ChargeSparkParticle(level, x, y, z, xd, yd, zd, options);
            particle.pickSprite(sprites);
            return particle;
        }
    }
}
