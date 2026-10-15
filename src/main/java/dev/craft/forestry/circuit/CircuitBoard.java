package dev.craft.forestry.circuit;

import java.util.List;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * Forestry {@code CircuitBoard} as data component of a circuit board item: the layout it was soldered for and the
 * circuits in socket order. The board type is the item itself. Freshly made boards carry no component.
 */
public record CircuitBoard(CircuitLayout layout, List<Circuit> circuits) {
    public static final Codec<CircuitBoard> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            CircuitLayout.CODEC.fieldOf("layout").forGetter(CircuitBoard::layout),
            Circuit.CODEC.listOf().fieldOf("circuits").forGetter(CircuitBoard::circuits)
    ).apply(instance, CircuitBoard::new));
    public static final StreamCodec<ByteBuf, CircuitBoard> STREAM_CODEC = StreamCodec.composite(
            CircuitLayout.STREAM_CODEC, CircuitBoard::layout,
            Circuit.STREAM_CODEC.apply(ByteBufCodecs.list()), CircuitBoard::circuits,
            CircuitBoard::new);

    public CircuitBoard {
        circuits = List.copyOf(circuits);
    }

    /** {@code addTooltip}: "§6<usage>:" and one " - <description>" line per circuit description. */
    public void addTooltip(Consumer<Component> tooltip) {
        tooltip.accept(Component.translatable(layout.getUsageKey()).append(":").withStyle(ChatFormatting.GOLD));
        for (Circuit circuit : circuits) {
            for (String key : circuit.getDescriptionKeys()) {
                tooltip.accept(Component.literal(" - ").append(Component.translatable(key)));
            }
        }
    }
}
