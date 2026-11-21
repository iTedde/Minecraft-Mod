package dev.craft.chargepads.particle;

import com.mojang.serialization.MapCodec;
import dev.craft.Craft;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class CpParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(Registries.PARTICLE_TYPE, Craft.MODID);

    public static final DeferredHolder<ParticleType<?>, ParticleType<ChargeSparkOptions>> CHARGE_SPARK =
            PARTICLE_TYPES.register("charge_spark", () -> new ParticleType<ChargeSparkOptions>(false) {
                @Override
                public MapCodec<ChargeSparkOptions> codec() {
                    return ChargeSparkOptions.CODEC;
                }

                @Override
                public StreamCodec<? super RegistryFriendlyByteBuf, ChargeSparkOptions> streamCodec() {
                    return ChargeSparkOptions.STREAM_CODEC;
                }
            });

    private CpParticles() {
    }
}
