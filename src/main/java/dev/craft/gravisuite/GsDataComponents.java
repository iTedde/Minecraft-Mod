package dev.craft.gravisuite;

import com.mojang.serialization.Codec;
import dev.craft.Craft;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** GraviSuite item NBT as data components; hover mode and toggle timer reuse the IC2 jetpack components. */
public final class GsDataComponents {
    public static final DeferredRegister.DataComponents COMPONENTS = DeferredRegister.createDataComponents(Craft.MODID);

    /** Engine of the advanced jetpack / flight of the gravitation chestplate (GraviSuite NBT {@code isFlyActive}). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Boolean>> FLY_ACTIVE =
            COMPONENTS.registerComponentType("fly_active", builder -> builder
                    .persistent(Codec.BOOL)
                    .networkSynchronized(ByteBufCodecs.BOOL));

    /** GraviTool NBT {@code toolMode} 1–4; absent means hoe without the mode name (see {@code GraviToolItem}). */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> TOOL_MODE =
            COMPONENTS.registerComponentType("tool_mode", builder -> builder
                    .persistent(Codec.INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    private GsDataComponents() {
    }
}
