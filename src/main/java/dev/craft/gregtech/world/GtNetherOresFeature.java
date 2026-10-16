package dev.craft.gregtech.world;

import java.util.function.Predicate;

import dev.craft.gregtech.GtBlocks;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * {@code GT_Worldgenerator.generateNether}: per chunk a random quantity q for each ore, q veins of size 2q (spec 07
 * §6.3). Placed once per chunk at the chunk corner.
 */
public final class GtNetherOresFeature extends Feature<NoneFeatureConfiguration> {
    private static final Predicate<BlockState> NETHERRACK = state -> state.is(Blocks.NETHERRACK);

    public GtNetherOresFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        int x = context.origin().getX();
        int z = context.origin().getZ();
        int quantity = (1 + random.nextInt(2)) * (1 + random.nextInt(2)) * (1 + random.nextInt(2))
                * (1 + random.nextInt(2));
        veins(level, random, x, z, GtOre.PYRITE, quantity, 0);
        quantity = (1 + random.nextInt(2)) * (1 + random.nextInt(4));
        veins(level, random, x, z, GtOre.CINNABAR, quantity, 64);
        quantity = (1 + random.nextInt(2)) * (1 + random.nextInt(2)) * (1 + random.nextInt(4));
        veins(level, random, x, z, GtOre.SPHALERITE, quantity, 32);
        return true;
    }

    /** {@code quantity} veins of size {@code 2 * quantity} at {@code minY + rand(64)}. */
    private static void veins(WorldGenLevel level, RandomSource random, int x, int z, GtOre ore, int quantity, int minY) {
        BlockState state = GtBlocks.ore(ore).get().defaultBlockState();
        for (int i = 0; i < quantity; i++) {
            int veinX = x + random.nextInt(16);
            int veinY = minY + random.nextInt(64);
            int veinZ = z + random.nextInt(16);
            GtVein.place(level, random, veinX, veinY, veinZ, quantity * 2, state, NETHERRACK);
        }
    }
}
