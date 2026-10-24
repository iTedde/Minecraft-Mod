package dev.craft.mps;

import java.util.HashMap;
import java.util.Map;

import com.mojang.serialization.Codec;
import dev.craft.Craft;
import dev.craft.mps.module.ModuleData;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** The MPS item tag {@code mmmpsmod} as data components (spec 29 §3.3). */
public final class MpsDataComponents {
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Craft.MODID);

    /** Installed modules by name. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Map<String, ModuleData>>> MODULES =
            COMPONENTS.registerComponentType("mps_modules", builder -> builder
                    .persistent(Codec.unboundedMap(Codec.STRING, ModuleData.CODEC))
                    .networkSynchronized(ByteBufCodecs.map(HashMap::new, ByteBufCodecs.STRING_UTF8,
                            ModuleData.STREAM_CODEC)));

    /** {@code Current Energy} in joules; absent means 0. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Double>> ENERGY =
            COMPONENTS.registerComponentType("mps_energy", builder -> builder
                    .persistent(Codec.DOUBLE)
                    .networkSynchronized(ByteBufCodecs.DOUBLE));

    /** Power tool {@code Mode}: name of the selected right-click module. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<String>> MODE =
            COMPONENTS.registerComponentType("mps_mode", builder -> builder
                    .persistent(Codec.STRING)
                    .networkSynchronized(ByteBufCodecs.STRING_UTF8));

    private MpsDataComponents() {
    }
}
