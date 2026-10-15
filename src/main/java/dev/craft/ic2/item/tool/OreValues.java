package dev.craft.ic2.item.tool;

import java.util.List;
import java.util.Map;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;

/**
 * IC2 {@code valuableOres} as used by the scanners (and later the miner): the pack values per ore, deepslate and
 * nether variants count like the base ore, every other ore 1.
 */
public final class OreValues {
    private static final List<Map.Entry<TagKey<Block>, Integer>> VALUES = List.of(
            Map.entry(Tags.Blocks.ORES_COAL, 1),
            Map.entry(Tags.Blocks.ORES_GOLD, 3),
            Map.entry(Tags.Blocks.ORES_IRON, 4),
            Map.entry(Tags.Blocks.ORES_LAPIS, 3),
            Map.entry(Tags.Blocks.ORES_REDSTONE, 3),
            Map.entry(Tags.Blocks.ORES_DIAMOND, 5),
            Map.entry(Tags.Blocks.ORES_EMERALD, 5),
            Map.entry(Tags.Blocks.ORES_COPPER, 2),
            Map.entry(common("ores/tin"), 2),
            Map.entry(common("ores/uranium"), 4),
            Map.entry(Tags.Blocks.ORES, 1));

    private OreValues() {
    }

    private static TagKey<Block> common(String path) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", path));
    }

    public static int valueOf(BlockState state) {
        for (Map.Entry<TagKey<Block>, Integer> entry : VALUES) {
            if (state.is(entry.getKey())) {
                return entry.getValue();
            }
        }
        return 0;
    }
}
