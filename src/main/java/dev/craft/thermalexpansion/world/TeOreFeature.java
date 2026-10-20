package dev.craft.thermalexpansion.world;

import java.util.List;
import java.util.function.Supplier;

import dev.craft.gregtech.world.GtVein;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * CoFH {@code WorldGenMulti} (spec 26 §3): one 1.4.7 minable vein of {@code size} at the placement origin, every
 * replaced block chosen by weight from {@code entries} – stone gets the ore, deepslate its deepslate variant.
 */
public final class TeOreFeature extends Feature<NoneFeatureConfiguration> {
    /** A weighted ore with its deepslate variant. */
    public record Entry(Supplier<? extends Block> ore, Supplier<? extends Block> deepslate, int weight) {
    }

    private final List<Entry> entries;
    private final int size;
    private final int totalWeight;

    public TeOreFeature(List<Entry> entries, int size) {
        super(NoneFeatureConfiguration.CODEC);
        this.entries = entries;
        this.size = size;
        this.totalWeight = entries.stream().mapToInt(Entry::weight).sum();
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        // The origin already is blockX + rand(16), y; WorldGenMinable centres the vein at +8 like GT.
        GtVein.place(context.level(), context.random(), context.origin().getX(), context.origin().getY(),
                context.origin().getZ(), size, this::choose);
        return true;
    }

    private BlockState choose(RandomSource random, BlockState existing) {
        boolean stone = existing.is(BlockTags.STONE_ORE_REPLACEABLES);
        if (!stone && !existing.is(BlockTags.DEEPSLATE_ORE_REPLACEABLES)) {
            return null;
        }
        // WeightedRandom.getRandomItem
        int roll = random.nextInt(totalWeight);
        for (Entry entry : entries) {
            roll -= entry.weight();
            if (roll < 0) {
                return (stone ? entry.ore() : entry.deepslate()).get().defaultBlockState();
            }
        }
        return null;
    }
}
