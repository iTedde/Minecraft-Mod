package ivorius.psychedelicraft.particle;

import java.util.function.Function;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.Psychedelicraft;

public interface PSParticles {
    ParticleType<DrugDustParticleEffect> EXHALED_SMOKE = register("exhaled_smoke", DrugDustParticleEffect.createType());
    ParticleType<DrugDustParticleEffect> BUBBLE = register("bubble", DrugDustParticleEffect.createType());

    ParticleType<FluidParticleEffect> DRIPPING_FLUID = register("dripping_fluid", false, FluidParticleEffect::createCodec, FluidParticleEffect::createPacketCodec);
    ParticleType<FluidParticleEffect> FALLING_FLUID = register("falling_fluid", false, FluidParticleEffect::createCodec, FluidParticleEffect::createPacketCodec);
    ParticleType<FluidParticleEffect> FLUID_SPLASH = register("fluid_splash", false, FluidParticleEffect::createCodec, FluidParticleEffect::createPacketCodec);
    ParticleType<FluidParticleEffect> FLUID_BUBBLE = register("fluid_bubble", false, FluidParticleEffect::createCodec, FluidParticleEffect::createPacketCodec);

    static <T extends ParticleType<?>> T register(String name, T type) {
        return Registry.register(BuiltInRegistries.PARTICLE_TYPE, Psychedelicraft.id(name), type);
    }

    private static <T extends ParticleOptions> ParticleType<T> register(
            String name,
            boolean alwaysShow,
            Function<ParticleType<T>, MapCodec<T>> codecGetter,
            Function<ParticleType<T>, StreamCodec<? super RegistryFriendlyByteBuf, T>> packetCodecGetter
        ) {
            return Registry.register(BuiltInRegistries.PARTICLE_TYPE, Psychedelicraft.id(name), new ParticleType<T>(alwaysShow) {
                @Override
                public MapCodec<T> codec() {
                    return codecGetter.apply(this);
                }

                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, T> streamCodec() {
                    return packetCodecGetter.apply(this);
                }
            });
        }

    static void bootstrap() {}
}
