package ivorius.psychedelicraft.fluid.physical;

import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.StateHolder;
import net.minecraft.world.level.block.state.properties.Property;
import ivorius.psychedelicraft.item.component.ItemFluids;

public record FluidStateManager (Set<FluidProperty<?>> properties) {

    <O, S extends StateHolder<O, S>> void appendProperties(StateDefinition.Builder<O, S> builder) {
        properties.forEach(p -> builder.add(p.property));
    }

    <O, S extends StateHolder<O, S>> S copyStateValues(StateHolder<?, ?> from, S to) {
        for (FluidProperty<?> property : properties) {
            to = copyStateValue(from, to, property.property);
        }
        return to;
    }

    <O, S extends StateHolder<O, S>> S computeAverage(Stream<? extends StateHolder<?, ?>> states, S to) {
        return states.findFirst().map(state -> copyStateValues(state, to)).orElse(to);
    }

    public void writeAttributes(StateHolder<?, ?> state, Map<String, Integer> attributes) {
        for (FluidProperty<?> property : properties) {
            property.write(state, attributes);
        }
    }

    public <O, S extends StateHolder<O, S>> S readAttributes(S state, ItemFluids stack) {
        for (FluidProperty<?> property : properties) {
            state = property.read(state, stack);
        }
        return state;
    }

    private <O, S extends StateHolder<O, S>, T extends Comparable<T>> S copyStateValue(StateHolder<?, ?> from, S to, Property<T> property) {
        return from.getOptionalValue(property).map(v -> to.trySetValue(property, v)).orElse(to);
    }

    public record FluidProperty<T extends Comparable<T>>(
            Property<T> property,
            BiConsumer<Map<String, Integer>, T> writer,
            Function<ItemFluids, T> reader) {
        void write(StateHolder<?, ?> state, Map<String, Integer> attributes) {
            writer.accept(attributes, state.getValue(property));
        }

        <O, S extends StateHolder<O, S>> S read(S state, ItemFluids stack) {
            return state.setValue(property, reader.apply(stack));
        }
    }
}
