package dev.craft.ic2;

import com.mojang.serialization.Codec;
import dev.craft.Craft;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class Ic2DataComponents {
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Craft.MODID);

    /** Stored EU of an electric item (IC2 NBT {@code charge}). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> CHARGE =
            COMPONENTS.registerComponentType("charge", builder -> builder
                    .persistent(ExtraCodecs.NON_NEGATIVE_INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    /** Mining laser mode 0–6 (IC2 NBT {@code laserSetting}). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> LASER_MODE =
            COMPONENTS.registerComponentType("laser_mode", builder -> builder
                    .persistent(ExtraCodecs.NON_NEGATIVE_INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    /** Electric wrench lossless mode (IC2 NBT {@code losslessMode}). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> LOSSLESS =
            COMPONENTS.registerComponentType("lossless", builder -> builder
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL));

    /** Switched-on nano saber (IC2 uses a separate item id, {@code itemNanoSaber}). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> ACTIVE =
            COMPONENTS.registerComponentType("active", builder -> builder
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL));

    private Ic2DataComponents() {
    }
}
