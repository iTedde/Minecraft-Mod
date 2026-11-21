package dev.craft.nuclearcontrol;

import dev.craft.Craft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.component.CustomData;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NcDataComponents {
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Craft.MODID);

    /** Target of a sensor location card (card NBT {@code x}/{@code y}/{@code z}). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<BlockPos>> SENSOR_TARGET =
            COMPONENTS.registerComponentType("sensor_target", builder -> builder
                    .persistent(BlockPos.CODEC)
                    .networkSynchronized(BlockPos.STREAM_CODEC));

    /** Values of an information panel card ({@code CardWrapperImpl}): title, state, last readings, text lines. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<CustomData>> CARD_DATA =
            COMPONENTS.registerComponentType("card_data", builder -> builder
                    .persistent(CustomData.CODEC)
                    .networkSynchronized(CustomData.STREAM_CODEC));

    private NcDataComponents() {
    }
}
