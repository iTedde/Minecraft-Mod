package dev.craft.thermalexpansion;

import dev.craft.Craft;
import dev.craft.thermalexpansion.energy.EnergyCellData;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Data components of Thermal Expansion (spec 26). */
public final class TeDataComponents {
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Craft.MODID);

    /** Liquid of a dismantled portable tank (1.4.7 NBT {@code liquid.itemID}/{@code liquid.amount}). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<SimpleFluidContent>> TANK_LIQUID =
            COMPONENTS.registerComponentType("tank_liquid", builder -> builder
                    .persistent(SimpleFluidContent.CODEC)
                    .networkSynchronized(SimpleFluidContent.STREAM_CODEC));

    /** Charge, limits and redstone mode of a dismantled energy cell. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<EnergyCellData>> ENERGY_CELL =
            COMPONENTS.registerComponentType("energy_cell", builder -> builder
                    .persistent(EnergyCellData.CODEC)
                    .networkSynchronized(EnergyCellData.STREAM_CODEC));

    private TeDataComponents() {
    }
}
