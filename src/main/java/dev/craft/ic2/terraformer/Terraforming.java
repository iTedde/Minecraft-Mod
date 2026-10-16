package dev.craft.ic2.terraformer;

import dev.craft.ic2.Ic2Blocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;

/**
 * Helpers of IC2 {@code TileEntityTerra} and the 1.4.7 block groups the blueprints work with, mapped to 1.21
 * (spec 04, terraformer; {@code abweichungen.md}).
 */
public final class Terraforming {
    /** IC2 returns −1 for "nothing found"; negative heights are valid in 1.21. */
    public static final int NONE = Integer.MIN_VALUE;

    private Terraforming() {
    }

    public static BlockPos pos(int x, int y, int z) {
        return new BlockPos(x, y, z);
    }

    public static BlockState get(ServerLevel level, int x, int y, int z) {
        return level.getBlockState(pos(x, y, z));
    }

    public static void set(ServerLevel level, int x, int y, int z, BlockState state) {
        level.setBlock(pos(x, y, z), state, Block.UPDATE_ALL);
    }

    public static void set(ServerLevel level, int x, int y, int z, Block block) {
        set(level, x, y, z, block.defaultBlockState());
    }

    /** IC2 {@code getFirstSolidBlockFrom}: first normal cube at or below {@code y} (above the world bottom). */
    public static int getFirstSolidBlockFrom(ServerLevel level, int x, int z, int y) {
        for (; y > level.getMinBuildHeight(); y--) {
            BlockPos pos = pos(x, y, z);
            if (level.getBlockState(pos).isRedstoneConductor(level, pos)) {
                return y;
            }
        }
        return NONE;
    }

    /** IC2 {@code getFirstBlockFrom}: first non-air block at or below {@code y}. */
    public static int getFirstBlockFrom(ServerLevel level, int x, int z, int y) {
        for (; y > level.getMinBuildHeight(); y--) {
            if (!get(level, x, y, z).isAir()) {
                return y;
            }
        }
        return NONE;
    }

    /**
     * IC2 {@code switchGround}. Upwards: the lowest {@code from} block of the column ending at {@code y} becomes
     * {@code to}. Downwards: below the {@code to} blocks starting at {@code y}, the first {@code from} block becomes
     * {@code to}.
     */
    public static boolean switchGround(ServerLevel level, Block from, Block to, int x, int y, int z, boolean upwards) {
        if (upwards) {
            int saveY = ++y;
            while (get(level, x, y - 1, z).is(from)) {
                y--;
            }
            if (saveY == y) {
                return false;
            }
            set(level, x, y, z, to);
            return true;
        }
        while (get(level, x, y, z).is(to)) {
            y--;
        }
        if (get(level, x, y, z).is(from)) {
            set(level, x, y, z, to);
            return true;
        }
        return false;
    }

    /**
     * IC2 {@code setBiomeAt}: 1.4.7 stores one biome per column; 1.21 stores 4×4×4 cells, so the whole column of
     * cells containing (x, z) changes. The chunk's biomes are sent to the players again.
     */
    public static void setBiomeAt(ServerLevel level, int x, int z, ResourceKey<Biome> biome) {
        Holder<Biome> holder = level.registryAccess().registryOrThrow(Registries.BIOME).getHolderOrThrow(biome);
        ChunkAccess chunk = level.getChunk(pos(x, 0, z));
        int qx = QuartPos.fromBlock(x);
        int qz = QuartPos.fromBlock(z);
        chunk.fillBiomesFromNoise((cx, cy, cz, sampler) -> cx == qx && cz == qz ? holder : chunk.getNoiseBiome(cx, cy, cz),
                level.getChunkSource().randomState().sampler());
        chunk.setUnsaved(true);
        level.getChunkSource().chunkMap.resendBiomesForChunks(List.of(chunk));
    }

    // --- 1.4.7 block groups ---

    /** IC2 tall grass (block 31, meta 1). */
    public static boolean isTallGrass(BlockState state) {
        return state.is(Blocks.SHORT_GRASS);
    }

    /** IC2 {@code ItemTFBPCultivation.plantIDs}. */
    public static boolean isPlant(BlockState state) {
        return isTallGrass(state) || state.is(Blocks.POPPY) || state.is(Blocks.DANDELION) || state.is(BlockTags.SAPLINGS)
                || state.is(Blocks.WHEAT) || state.is(Blocks.RED_MUSHROOM) || state.is(Blocks.BROWN_MUSHROOM)
                || state.is(Blocks.PUMPKIN) || state.is(Ic2Blocks.RUBBER_SAPLING.get());
    }

    /** 1.4.7 leaves (all types) and rubber leaves. */
    public static boolean isLeaves(BlockState state) {
        return state.is(BlockTags.LEAVES) || state.is(Ic2Blocks.RUBBER_LEAVES.get());
    }

    /** 1.4.7 logs (block 17) and rubber wood. */
    public static boolean isLog(BlockState state) {
        return state.is(BlockTags.LOGS_THAT_BURN) || state.is(Ic2Blocks.RUBBER_WOOD.get());
    }

    public static boolean isWater(BlockState state) {
        return state.is(Blocks.WATER);
    }
}
