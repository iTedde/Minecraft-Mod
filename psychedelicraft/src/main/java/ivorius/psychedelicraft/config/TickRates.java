package ivorius.psychedelicraft.config;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.resources.ResourceKey;
import com.mojang.serialization.Codec;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.fluid.alcohol.TickRate;

public record TickRates (Map<ResourceKey<SimpleFluid>, TickRate> values) {
    public static final Codec<TickRates> CODEC = Codec.unboundedMap(
        ResourceKey.codec(SimpleFluid.REGISTRY_KEY),
        TickRate.CODEC
    ).xmap(Function.identity(), values -> {
        var mutable = new HashMap<>(TickRate.getDefaults());
        mutable.putAll(values);
        return Map.copyOf(mutable);
    }).xmap(TickRates::new, TickRates::values);
}
