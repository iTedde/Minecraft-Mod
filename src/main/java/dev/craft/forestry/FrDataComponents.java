package dev.craft.forestry;

import dev.craft.Craft;
import dev.craft.forestry.circuit.CircuitBoard;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class FrDataComponents {
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Craft.MODID);

    /** Soldered circuits of a circuit board (Forestry NBT {@code LY}/{@code CA.I*}). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CircuitBoard>> CIRCUIT_BOARD =
            COMPONENTS.registerComponentType("circuit_board", builder -> builder
                    .persistent(CircuitBoard.CODEC)
                    .networkSynchronized(CircuitBoard.STREAM_CODEC));

    private FrDataComponents() {
    }
}
