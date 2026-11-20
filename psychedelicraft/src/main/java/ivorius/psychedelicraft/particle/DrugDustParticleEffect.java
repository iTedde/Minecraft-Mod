package ivorius.psychedelicraft.particle;

import net.minecraft.network.RegistryFriendlyByteBuf;
import com.mojang.serialization.MapCodec;
import java.util.concurrent.atomic.AtomicReference;
import org.joml.Vector3f;

import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;

public class DrugDustParticleEffect extends DustParticleOptions {
    static ParticleType<DrugDustParticleEffect> createType() {
        AtomicReference<ParticleType<DrugDustParticleEffect>> type = new AtomicReference<>();
        MapCodec<DrugDustParticleEffect> codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ExtraCodecs.VECTOR3F.fieldOf("color").forGetter(DustParticleOptions::getColor),
                SCALE.fieldOf("scale").forGetter(DustParticleOptions::getScale)
            ).apply(instance, (color, scale) -> new DrugDustParticleEffect(type.get(), color, scale)));
        StreamCodec<RegistryFriendlyByteBuf, DrugDustParticleEffect> streamCodec = StreamCodec.composite(
                ByteBufCodecs.VECTOR3F, DustParticleOptions::getColor,
                ByteBufCodecs.FLOAT, DustParticleOptions::getScale,
                (color, scale) -> new DrugDustParticleEffect(type.get(), color, scale)
            );
        type.set(new ParticleType<DrugDustParticleEffect>(false) {
            @Override
            public MapCodec<DrugDustParticleEffect> codec() {
                return codec;
            }

            @Override
            public StreamCodec<? super RegistryFriendlyByteBuf, DrugDustParticleEffect> streamCodec() {
                return streamCodec;
            }
        });
        return type.get();
    }

    private final ParticleType<DrugDustParticleEffect> type;

    public DrugDustParticleEffect(ParticleType<DrugDustParticleEffect> type, Vector3f color, float scale) {
        super(color, scale);
        this.type = type;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public ParticleType<DustParticleOptions> getType() {
        return (ParticleType)type;
    }
}
