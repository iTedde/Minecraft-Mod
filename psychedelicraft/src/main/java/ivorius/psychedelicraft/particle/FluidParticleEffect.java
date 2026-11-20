package ivorius.psychedelicraft.particle;

import com.mojang.serialization.MapCodec;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;

public record FluidParticleEffect(ParticleType<FluidParticleEffect> type, ItemFluids fluid) implements ParticleOptions {
    public static MapCodec<FluidParticleEffect> createCodec(ParticleType<FluidParticleEffect> type) {
        return ItemFluids.CODEC.xmap(fluid -> new FluidParticleEffect(type, fluid), effect -> effect.fluid()).fieldOf("fluid");
    }

    public static StreamCodec<? super RegistryFriendlyByteBuf, FluidParticleEffect> createPacketCodec(ParticleType<FluidParticleEffect> type) {
        return ItemFluids.PACKET_CODEC.map(fluid -> new FluidParticleEffect(type, fluid), effect -> effect.fluid());
    }

    @Override
    public ParticleType<?> getType() {
        return type;
    }
}
