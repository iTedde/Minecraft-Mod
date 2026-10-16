package dev.craft.gregtech.world;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementFilter;
import net.minecraft.world.level.levelgen.placement.PlacementModifierType;

/**
 * GT checks the biome once per chunk at block (chunk + 8, chunk + 8) ({@code GT_Worldgenerator.generateSurface}); 1.21
 * biomes are 3D, so the surface biome there decides for every ore of the chunk, not the biome at the ore position.
 */
public final class ChunkBiomeFilter extends PlacementFilter {
    public static final MapCodec<ChunkBiomeFilter> CODEC = TagKey.hashedCodec(Registries.BIOME).fieldOf("biomes")
            .xmap(ChunkBiomeFilter::new, filter -> filter.biomes);

    private final TagKey<Biome> biomes;

    private ChunkBiomeFilter(TagKey<Biome> biomes) {
        this.biomes = biomes;
    }

    public static ChunkBiomeFilter of(TagKey<Biome> biomes) {
        return new ChunkBiomeFilter(biomes);
    }

    @Override
    protected boolean shouldPlace(PlacementContext context, RandomSource random, BlockPos pos) {
        int x = SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(pos.getX()), 8);
        int z = SectionPos.sectionToBlockCoord(SectionPos.blockToSectionCoord(pos.getZ()), 8);
        int y = context.getHeight(Heightmap.Types.WORLD_SURFACE_WG, x, z);
        return context.getLevel().getBiome(new BlockPos(x, y, z)).is(biomes);
    }

    @Override
    public PlacementModifierType<?> type() {
        return GtWorldGen.CHUNK_BIOME.get();
    }
}
