package dev.craft.redpower;

import dev.craft.Craft;
import dev.craft.redpower.table.Plan;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class RpDataComponents {
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Craft.MODID);

    /** Written plan of the project table (RedPower NBT {@code result}/{@code requires}). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Plan>> PLAN =
            COMPONENTS.registerComponentType("plan", builder -> builder
                    .persistent(Plan.CODEC)
                    .networkSynchronized(Plan.STREAM_CODEC));

    private RpDataComponents() {
    }
}
