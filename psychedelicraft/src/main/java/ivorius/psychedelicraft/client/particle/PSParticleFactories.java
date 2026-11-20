package ivorius.psychedelicraft.client.particle;

import org.joml.Vector3f;

import ivorius.psychedelicraft.particle.DrugDustParticleEffect;
import ivorius.psychedelicraft.particle.FluidParticleEffect;
import ivorius.psychedelicraft.particle.PSParticles;
import ivorius.psychedelicraft.util.MathUtils;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.DripParticle;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SplashParticle;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;

/**
 * @author Sollace
 * @since 1 Jan 2023
 */
public interface PSParticleFactories {
    static void bootstrap(net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(PSParticles.EXHALED_SMOKE, createFactory(ExhaledSmokeParticle::new));
        event.registerSpriteSet(PSParticles.BUBBLE, PSParticleFactories.<DrugDustParticleEffect>createFactory(FluidBubbleParticle::new));
        event.registerSpriteSet(PSParticles.FLUID_SPLASH, createFactory(createSplash()));
        event.registerSpriteSet(PSParticles.FLUID_BUBBLE, PSParticleFactories.<FluidParticleEffect>createFactory(FluidBubbleParticle::new));
        event.registerSpriteSet(PSParticles.DRIPPING_FLUID, createFactory(PSParticleFactories::createDrippingFluid));
        event.registerSpriteSet(PSParticles.FALLING_FLUID, createFactory(PSParticleFactories::createFallingFluid));
    }

    static ParticleSupplier<FluidParticleEffect> createSplash() {
        return (effect, provider, clientWorld, d, e, f, g, h, i) -> setColor(new SplashParticle.Provider(provider).createParticle(ParticleTypes.SPLASH, clientWorld, d, e, f, g, h, i), effect);
    }

    static Particle createDrippingFluid(FluidParticleEffect type, ClientLevel world,
            double x, double y, double z,
            double velocityX, double velocityY, double velocityZ) {
        return setColor(new DripParticle.DripHangParticle(world, x, y, z, type.fluid().fluid().getPhysical().getStandingFluid(), new FluidParticleEffect(PSParticles.FALLING_FLUID, type.fluid())), type);
    }

    static Particle createFallingFluid(FluidParticleEffect type, ClientLevel world,
            double x, double y, double z,
            double velocityX, double velocityY, double velocityZ) {
        return setColor(new DripParticle.FallAndLandParticle(world, x, y, z, type.fluid().fluid().getPhysical().getStandingFluid(), new FluidParticleEffect(PSParticles.FLUID_SPLASH, type.fluid())), type);
    }

    static Particle setColor(Particle particle, FluidParticleEffect effect) {
        Vector3f color = MathUtils.unpackRgb(effect.fluid().fluid().getColor(effect.fluid()));
        particle.setColor(color.x, color.y, color.z);
        return particle;
    }

    private static <T extends ParticleOptions> net.minecraft.client.particle.ParticleEngine.SpriteParticleRegistration<T> createFactory(ParticleSupplier<T> supplier) {
        return provider -> (effect, world, x, y, z, dx, dy, dz) -> supplier.get(effect, provider, world, x, y, z, dx, dy, dz);
    }

    private static <T extends ParticleOptions> net.minecraft.client.particle.ParticleEngine.SpriteParticleRegistration<T> createFactory(ParticleProvider<T> supplier) {
        return provider -> (effect, world, x, y, z, dx, dy, dz) -> {
            var particle = supplier.createParticle(effect, world, x, y, z, dx, dy, dz);
            if (particle instanceof TextureSheetParticle b) {
                b.pickSprite(provider);
            }
            return particle;
        };
    }

    interface ParticleSupplier<T extends ParticleOptions> {
        Particle get(T effect, SpriteSet provider, ClientLevel world, double x, double y, double z, double dx, double dy, double dz);
    }
}
