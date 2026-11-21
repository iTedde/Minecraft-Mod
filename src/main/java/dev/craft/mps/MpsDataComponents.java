package dev.craft.mps;

import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.craft.Craft;
import dev.craft.mps.module.ModuleData;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The MPS item tag {@code mmmpsmod} as data components (spec 29 §3.3). */
public final class MpsDataComponents {
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Craft.MODID);

    /** Installed modules by name. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Map<String, ModuleData>>> MODULES =
            COMPONENTS.registerComponentType("mps_modules", builder -> builder
                    .persistent(Codec.unboundedMap(Codec.STRING, ModuleData.CODEC))
                    .networkSynchronized(ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8,
                            ModuleData.STREAM_CODEC)));

    /** {@code Current Energy} in joules; absent means 0. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Double>> ENERGY =
            COMPONENTS.registerComponentType("mps_energy", builder -> builder
                    .persistent(Codec.DOUBLE)
                    .networkSynchronized(ByteBufCodecs.DOUBLE));

    /** Power tool {@code Mode}: name of the selected right-click module. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> MODE =
            COMPONENTS.registerComponentType("mps_mode", builder -> builder
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8));

    /** Helmet Auto-Feeder buffer ({@code Food}/{@code Saturation}). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FoodBuffer>> FOOD =
            COMPONENTS.registerComponentType("mps_food", builder -> builder
                    .persistent(FoodBuffer.CODEC)
                    .networkSynchronized(FoodBuffer.STREAM_CODEC));

    /** Leggings Kinetic Generator: block position ({@code x}/{@code z}) of the last payout. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<KineticPosition>> KINETIC =
            COMPONENTS.registerComponentType("mps_kinetic", builder -> builder
                    .persistent(KineticPosition.CODEC)
                    .networkSynchronized(KineticPosition.STREAM_CODEC));

    public record FoodBuffer(double food, double saturation) {
        public static final FoodBuffer EMPTY = new FoodBuffer(0.0, 0.0);
        public static final Codec<FoodBuffer> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.DOUBLE.optionalFieldOf("food", 0.0).forGetter(FoodBuffer::food),
                Codec.DOUBLE.optionalFieldOf("saturation", 0.0).forGetter(FoodBuffer::saturation)
        ).apply(instance, FoodBuffer::new));
        public static final StreamCodec<ByteBuf, FoodBuffer> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.DOUBLE, FoodBuffer::food, ByteBufCodecs.DOUBLE, FoodBuffer::saturation,
                FoodBuffer::new);
    }

    public record KineticPosition(int x, int z) {
        public static final Codec<KineticPosition> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("x").forGetter(KineticPosition::x),
                Codec.INT.fieldOf("z").forGetter(KineticPosition::z)
        ).apply(instance, KineticPosition::new));
        public static final StreamCodec<ByteBuf, KineticPosition> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, KineticPosition::x, ByteBufCodecs.VAR_INT, KineticPosition::z,
                KineticPosition::new);
    }

    private MpsDataComponents() {
    }
}
