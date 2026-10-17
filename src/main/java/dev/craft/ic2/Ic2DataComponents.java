package dev.craft.ic2;

import dev.craft.ic2.crop.CropSeedItem;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.craft.Craft;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
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

    /**
     * Filled tin can variant (IC2 metadata 0–5, effects in {@code ItemTinCan}); default 0, set in
     * {@code Ic2.modifyDefaultComponents} so recipe ingredients can tell meta 0 from meta 1.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CAN_META =
            COMPONENTS.registerComponentType("can_meta", builder -> builder
                    .persistent(ExtraCodecs.NON_NEGATIVE_INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    /** Fuel quality of a filled fuel can (IC2 NBT {@code value}). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> FUEL_VALUE =
            COMPONENTS.registerComponentType("fuel_value", builder -> builder
                    .persistent(ExtraCodecs.NON_NEGATIVE_INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    /** Stored heat of a coolant cell, heat exchanger or vent (IC2 NBT {@code heat}). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> REACTOR_HEAT =
            COMPONENTS.registerComponentType("reactor_heat", builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    /**
     * Wear of reflectors and condensators beyond the 10 000 damage steps (IC2 {@code ItemGradualInt}, NBT
     * {@code advDmg}); may become negative like in the original.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> ADV_DAMAGE =
            COMPONENTS.registerComponentType("adv_damage", builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    /**
     * GregTech thorium/plutonium cell (NBT {@code durability}, {@code pulse}, {@code output}): cycles used and the two
     * pulse counters of the slow (negative pulse rate) cells.
     */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> NUCLEAR_DURABILITY =
            COMPONENTS.registerComponentType("nuclear_durability", builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> PULSE_COUNT =
            COMPONENTS.registerComponentType("pulse_count", builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> OUTPUT_COUNT =
            COMPONENTS.registerComponentType("output_count", builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    /** Last block position of the static boots (IC2 NBT {@code x}/{@code z}). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Position>> LAST_POSITION =
            COMPONENTS.registerComponentType("last_position", builder -> builder
                    .persistent(Position.CODEC)
                    .networkSynchronized(Position.STREAM_CODEC));

    /** Teleporter linked by the frequency transmitter (IC2 NBT {@code targetSet}, {@code targetX/Y/Z}). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> TELEPORTER_TARGET =
            COMPONENTS.registerComponentType("teleporter_target", builder -> builder
                    .persistent(BlockPos.CODEC)
                    .networkSynchronized(BlockPos.STREAM_CODEC));

    /** Painter automatic refill mode (IC2 NBT {@code autoRefill}). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> AUTO_REFILL =
            COMPONENTS.registerComponentType("auto_refill", builder -> builder
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL));

    /** Last EU meter reading (IC2 NBT {@code lastMeasuredTileEntityX/Y/Z}, {@code lastTotalEnergy*}, time). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Measurement>> EU_MEASUREMENT =
            COMPONENTS.registerComponentType("eu_measurement", builder -> builder
                    .persistent(Measurement.CODEC)
                    .networkSynchronized(Measurement.STREAM_CODEC));

    /** Seed bag data (IC2 NBT {@code id}, {@code growth}, {@code gain}, {@code resistance}, {@code scan}). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CropSeedItem.Seed>> CROP_SEED =
            COMPONENTS.registerComponentType("crop_seed", builder -> builder
                    .persistent(CropSeedItem.Seed.CODEC)
                    .networkSynchronized(CropSeedItem.Seed.STREAM_CODEC));

    public record Measurement(BlockPos pos, long emitted, long sunken, long time) {
        public static final Codec<Measurement> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(Measurement::pos),
                Codec.LONG.fieldOf("emitted").forGetter(Measurement::emitted),
                Codec.LONG.fieldOf("sunken").forGetter(Measurement::sunken),
                Codec.LONG.fieldOf("time").forGetter(Measurement::time)
        ).apply(instance, Measurement::new));
        public static final StreamCodec<ByteBuf, Measurement> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, Measurement::pos,
                ByteBufCodecs.VAR_LONG, Measurement::emitted,
                ByteBufCodecs.VAR_LONG, Measurement::sunken,
                ByteBufCodecs.VAR_LONG, Measurement::time,
                Measurement::new);
    }

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
