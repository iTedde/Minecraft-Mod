/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.entity.drug.influence;

import java.util.Optional;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.joml.Vector3f;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.entity.drug.DrugType;

public record DrugInfluence(DrugType<?> drugType, int delay, double factor, double base, double target, Optional<Vector3f> color) {
    public static final Codec<Vector3f> COLOR_CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.FLOAT.fieldOf("r").forGetter(Vector3f::x),
            Codec.FLOAT.fieldOf("g").forGetter(Vector3f::y),
            Codec.FLOAT.fieldOf("b").forGetter(Vector3f::z)
    ).apply(i, Vector3f::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, Optional<Vector3f>> COLOR_PACKET_CODEC = ByteBufCodecs.optional(StreamCodec.composite(
        ByteBufCodecs.FLOAT, Vector3f::x,
        ByteBufCodecs.FLOAT, Vector3f::y,
        ByteBufCodecs.FLOAT, Vector3f::z,
        Vector3f::new
    ));
    public static final Codec<DrugInfluence> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DrugType.REGISTRY.byNameCodec().fieldOf("drugType").forGetter(DrugInfluence::drugType),
            Codec.INT.fieldOf("delay").forGetter(DrugInfluence::delay),
            Codec.DOUBLE.fieldOf("influenceSpeed").forGetter(DrugInfluence::factor),
            Codec.DOUBLE.fieldOf("influenceSpeedPlus").forGetter(DrugInfluence::base),
            Codec.DOUBLE.fieldOf("maxInfluence").forGetter(DrugInfluence::target),
            COLOR_CODEC.optionalFieldOf("color").forGetter(DrugInfluence::color)
    ).apply(instance, DrugInfluence::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, DrugInfluence> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.registry(DrugType.REGISTRY.key()), DrugInfluence::drugType,
            ByteBufCodecs.INT, DrugInfluence::delay,
            ByteBufCodecs.DOUBLE, DrugInfluence::factor,
            ByteBufCodecs.DOUBLE, DrugInfluence::base,
            ByteBufCodecs.DOUBLE, DrugInfluence::target,
            COLOR_PACKET_CODEC, DrugInfluence::color,
            DrugInfluence::new
    );

    public DrugInfluence(DrugType<?> drugType, int delay, double factor, double base, double target) {
        this(drugType, delay, factor, base, target, Optional.empty());
    }

    public DrugInfluence(DrugType<?> drugType, int delay, double factor, double base, double target, Vector3f color) {
        this(drugType, delay, factor, base, target, Optional.of(color));
    }

    public boolean isOf(DrugType<?> type) {
        return drugType() == type;
    }

    public DrugInfluence copyWithTarget(double target) {
        return new DrugInfluence(drugType, delay, factor, base, target, color);
    }

    public DrugInfluence copyWithDelay(int delay) {
        return new DrugInfluence(drugType, delay, factor, base, target, color);
    }
}
