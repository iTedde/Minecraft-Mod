package ivorius.psychedelicraft.config;

import ivorius.psychedelicraft.world.gen.BiomeSelectionContext;
import ivorius.psychedelicraft.world.gen.BiomeSelectors;

import java.util.List;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Stream;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

public interface BiomeSelector {
    Predicate<BiomeSelectionContext> ALL = BiomeSelectors.all();
    Predicate<BiomeSelectionContext> NONE = ctx -> false;
    Predicate<BiomeSelectionContext> COLD = ctx -> ctx.getBiome().getBaseTemperature() < 0.15F;
    Predicate<BiomeSelectionContext> DRY = ctx -> !ctx.getBiome().hasPrecipitation();

    static Predicate<BiomeSelectionContext> compile(List<String> included, List<String> excluded, Predicate<BiomeSelectionContext> dynamicInclusion) {
        var include = compile(included, NONE, Stream::anyMatch).or(dynamicInclusion);
        var exclude = compile(excluded, ALL, Stream::noneMatch);

        return include.and(exclude);
    }

    static Predicate<BiomeSelectionContext> compile(List<String> predicates, Predicate<BiomeSelectionContext> fallback,
            BiPredicate<Stream<Predicate<BiomeSelectionContext>>, Predicate<Predicate<BiomeSelectionContext>>> combiner) {

        if (predicates == null || predicates.isEmpty()) {
            return fallback;
        }

        var selectors = predicates.stream().map(BiomeSelector::compile).toList();

        if (selectors.size() == 1) {
            return selectors.get(0);
        }
        return ctx -> combiner.test(selectors.stream(), a -> a.test(ctx));
    }

    static Predicate<BiomeSelectionContext> compile(String selector) {
        if (selector.startsWith("#")) {
            return BiomeSelectors.tag(TagKey.create(Registries.BIOME, ResourceLocation.parse(selector.substring(1))));
        }

        return ofId(ResourceLocation.parse(selector));
    }

    static Predicate<BiomeSelectionContext> ofId(ResourceLocation tagId) {
        return ctx -> ctx.getBiomeRegistryEntry().is(tagId);
    }
}
