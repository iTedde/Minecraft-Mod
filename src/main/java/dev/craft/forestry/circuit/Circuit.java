package dev.craft.forestry.circuit;

import java.util.List;
import java.util.stream.IntStream;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/**
 * The Electrical Engine circuits of Forestry ({@code CircuitElectricChoke/Boost/Efficiency}): each changes the EU per
 * cycle, the MJ per cycle and the EU storage ({@code 2 · euChange}) of the engine.
 */
public enum Circuit implements StringRepresentable {
    ELECTRIC_CHOKE_1("forestry.energyChoke1", "electric_choke_1", 1, -2, -1, 2),
    ELECTRIC_BOOST_1("forestry.energyBoost1", "electric_boost_1", 2, 7, 2, 2),
    ELECTRIC_BOOST_2("forestry.energyBoost2", "electric_boost_2", 2, 15, 4, 2),
    ELECTRIC_EFFICIENCY_1("forestry.energyEfficiency1", "electric_efficiency_1", 1, -1, 0, 1);

    public static final Codec<Circuit> CODEC = StringRepresentable.fromEnum(Circuit::values);
    public static final StreamCodec<ByteBuf, Circuit> STREAM_CODEC =
            ByteBufCodecs.idMapper(index -> values()[index], Circuit::ordinal);

    private final String uid;
    private final String name;
    private final int limit;
    private final int euChange;
    private final int mjChange;
    private final int descriptionLines;

    Circuit(String uid, String name, int limit, int euChange, int mjChange, int descriptionLines) {
        this.uid = uid;
        this.name = name;
        this.limit = limit;
        this.euChange = euChange;
        this.mjChange = mjChange;
        this.descriptionLines = descriptionLines;
    }

    public String getUid() {
        return uid;
    }

    @Override
    public String getSerializedName() {
        return name;
    }

    /** Maximum number of this circuit on one board. */
    public int getLimit() {
        return limit;
    }

    public int getEuChange() {
        return euChange;
    }

    public int getMjChange() {
        return mjChange;
    }

    public String getNameKey() {
        return "circuit.craft." + name;
    }

    /** Translation keys of the tooltip lines ({@code descriptions}). */
    public List<String> getDescriptionKeys() {
        return IntStream.range(0, descriptionLines)
                .mapToObj(line -> "circuit.craft." + name + ".desc" + line).toList();
    }
}
