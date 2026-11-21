package ivorius.psychedelicraft.world.gen;

import java.util.function.Predicate;

import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

/**
 * Biome predicates (upstream: Fabric {@code BiomeSelectors}).
 */
public interface BiomeSelectors {
    static Predicate<BiomeSelectionContext> all() {
        return ctx -> true;
    }

    static Predicate<BiomeSelectionContext> foundInOverworld() {
        return ctx -> ctx.holder().is(BiomeTags.IS_OVERWORLD);
    }

    static Predicate<BiomeSelectionContext> tag(TagKey<Biome> tag) {
        return ctx -> ctx.holder().is(tag);
    }

    @SafeVarargs
    static Predicate<BiomeSelectionContext> includeByKey(ResourceKey<Biome>... keys) {
        return ctx -> {
            for (ResourceKey<Biome> key : keys) {
                if (ctx.holder().is(key)) {
                    return true;
                }
            }
            return false;
        };
    }
}
