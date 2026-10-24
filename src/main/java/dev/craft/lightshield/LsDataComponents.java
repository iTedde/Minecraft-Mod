package dev.craft.lightshield;

import com.mojang.serialization.Codec;
import dev.craft.Craft;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class LsDataComponents {
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Craft.MODID);

    /** Light switched on (spec 30 §4); missing means on. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> LIGHT_ON =
            COMPONENTS.registerComponentType("light_on", builder -> builder
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL));

    private LsDataComponents() {
    }
}
