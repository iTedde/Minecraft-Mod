package dev.craft.forestry.circuit;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/**
 * Forestry {@code CircuitLayout}: what a circuit board is soldered for. Only the Electrical Engine layout is in scope
 * (the farm layouts belong to Forestry farms).
 */
public enum CircuitLayout implements StringRepresentable {
    ENGINE_TIN("forestry.engine.tin", "engine_tin");

    public static final Codec<CircuitLayout> CODEC = StringRepresentable.fromEnum(CircuitLayout::values);
    public static final StreamCodec<ByteBuf, CircuitLayout> STREAM_CODEC =
            ByteBufCodecs.idMapper(index -> values()[index], CircuitLayout::ordinal);

    private final String uid;
    private final String name;

    CircuitLayout(String uid, String name) {
        this.uid = uid;
        this.name = name;
    }

    /** The registry default ({@code getDefaultLayout}): the first registered layout. */
    public static CircuitLayout getDefault() {
        return ENGINE_TIN;
    }

    public String getUid() {
        return uid;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    /** {@code layout.<uid>}: name shown in the soldering iron. */
    public String getNameKey() {
        return "layout.craft." + name;
    }

    /** {@code usage.<uid>}: headline of the board tooltip. */
    public String getUsageKey() {
        return "usage.craft." + name;
    }

    /** {@code advanceLayout}: the next registered layout, wrapping around. */
    public CircuitLayout next() {
        return values()[(ordinal() + 1) % values().length];
    }

    /** {@code regressLayout}: the previous registered layout, wrapping around. */
    public CircuitLayout previous() {
        return values()[(ordinal() + values().length - 1) % values().length];
    }
}
