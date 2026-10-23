package dev.craft.enderstorage;

import dev.craft.Craft;
import net.minecraft.core.component.DataComponentType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class EsDataComponents {
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Craft.MODID);

    /** Frequency and owner of an ender chest, tank or pouch (EnderStorage item damage + NBT {@code owner}). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Frequency>> FREQUENCY =
            COMPONENTS.registerComponentType("ender_frequency", builder -> builder
                    .persistent(Frequency.CODEC)
                    .networkSynchronized(Frequency.STREAM_CODEC));

    private EsDataComponents() {
    }
}
