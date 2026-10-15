package dev.craft.ic2;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.craft.Craft;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class Ic2DataComponents {
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Craft.MODID);

    /** Stored EU of an electric item (IC2 NBT {@code charge}). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CHARGE =
            COMPONENTS.registerComponentType("charge", builder -> builder
                    .persistent(ExtraCodecs.NON_NEGATIVE_INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    /** Mining laser mode 0–6 (IC2 NBT {@code laserSetting}). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> LASER_MODE =
            COMPONENTS.registerComponentType("laser_mode", builder -> builder
                    .persistent(ExtraCodecs.NON_NEGATIVE_INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    /** Electric wrench lossless mode (IC2 NBT {@code losslessMode}). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> LOSSLESS =
            COMPONENTS.registerComponentType("lossless", builder -> builder
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL));

    /** Switched-on nano saber (IC2 uses a separate item id, {@code itemNanoSaber}). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> ACTIVE =
            COMPONENTS.registerComponentType("active", builder -> builder
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL));

    /** Jetpack hover mode (IC2 NBT {@code hoverMode}). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> HOVER_MODE =
            COMPONENTS.registerComponentType("hover_mode", builder -> builder
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL));

    /** Jetpack mode switch cooldown in ticks (IC2 NBT {@code toggleTimer}). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TOGGLE_TIMER =
            COMPONENTS.registerComponentType("toggle_timer", builder -> builder
                    .persistent(ExtraCodecs.NON_NEGATIVE_INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    /** Last block position of the static boots (IC2 NBT {@code x}/{@code z}). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Position>> LAST_POSITION =
            COMPONENTS.registerComponentType("last_position", builder -> builder
                    .persistent(Position.CODEC)
                    .networkSynchronized(Position.STREAM_CODEC));

    public record Position(int x, int z) {
        public static final Codec<Position> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("x").forGetter(Position::x),
                Codec.INT.fieldOf("z").forGetter(Position::z)
        ).apply(instance, Position::new));
        public static final StreamCodec<ByteBuf, Position> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Position::x,
                ByteBufCodecs.VAR_INT, Position::z,
                Position::new);
    }

    private Ic2DataComponents() {
    }
}
