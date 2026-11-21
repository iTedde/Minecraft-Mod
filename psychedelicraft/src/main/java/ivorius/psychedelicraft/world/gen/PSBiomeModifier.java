package ivorius.psychedelicraft.world.gen;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;
import net.neoforged.neoforge.common.world.BiomeModifier;
import net.neoforged.neoforge.common.world.ModifiableBiomeInfo;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * Adds Psychedelicraft's plant features to the biomes selected in code and in the config
 * (upstream: Fabric {@code BiomeModifications.addFeature}). Used from
 * {@code data/psychedelicraft/neoforge/biome_modifier/plants.json}, which lists the placed features.
 */
public record PSBiomeModifier(HolderSet<PlacedFeature> features) implements BiomeModifier {
    public static final MapCodec<PSBiomeModifier> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            PlacedFeature.LIST_CODEC.fieldOf("features").forGetter(PSBiomeModifier::features)
    ).apply(i, PSBiomeModifier::new));

    record Entry(ResourceKey<PlacedFeature> key, Predicate<BiomeSelectionContext> predicate, GenerationStep.Decoration step) {}

    static final List<Entry> ENTRIES = new ArrayList<>();

    static void add(ResourceKey<PlacedFeature> key, Predicate<BiomeSelectionContext> predicate, GenerationStep.Decoration step) {
        ENTRIES.add(new Entry(key, predicate, step));
    }

    public static void bootstrap() {
        net.minecraft.core.Registry.register(NeoForgeRegistries.BIOME_MODIFIER_SERIALIZERS, Psychedelicraft.id("plants"), CODEC);
    }

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase != Phase.ADD) {
            return;
        }
        BiomeSelectionContext context = new BiomeSelectionContext(biome);
        for (Entry entry : ENTRIES) {
            if (!entry.predicate().test(context)) {
                continue;
            }
            features.stream()
                    .filter(f -> f.is(entry.key()))
                    .findFirst()
                    .ifPresent(feature -> builder.getGenerationSettings().addFeature(entry.step(), feature));
        }
    }

    @Override
    public MapCodec<? extends BiomeModifier> codec() {
        return CODEC;
    }

}
