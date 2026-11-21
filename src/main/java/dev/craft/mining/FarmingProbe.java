package dev.craft.mining;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ColorResolver;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.lighting.LevelLightEngine;
import net.minecraft.world.level.chunk.LightChunk;
import net.minecraft.world.level.chunk.LightChunkGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * A few blocks in an otherwise empty, fully lit world (spec 53 §3.2): the soil test asks a plant's {@code canSurvive}
 * here before the farming dimension exists. Everything not set is air; light is 15 and the sky is visible, like the
 * noon of the farming dimension. Plants that need more of a real level throw and count as not surviving.
 */
final class FarmingProbe implements LevelReader {
    private final Map<BlockPos, BlockState> blocks = new HashMap<>();
    private final RegistryAccess registries;
    private final Holder<Biome> biome;
    private final DimensionType type;
    private final FeatureFlagSet features;
    private final WorldBorder border = new WorldBorder();
    /** Light engine without block or sky light; {@link #getBrightness} answers 15 instead. */
    private final LevelLightEngine light = new LevelLightEngine(new LightChunkGetter() {
        @Override
        public LightChunk getChunkForLighting(int x, int z) {
            return null;
        }

        @Override
        public BlockGetter getLevel() {
            return FarmingProbe.this;
        }
    }, false, false);

    FarmingProbe(RegistryAccess registries, Holder<Biome> biome, DimensionType type, FeatureFlagSet features) {
        this.registries = registries;
        this.biome = biome;
        this.type = type;
        this.features = features;
    }

    void clear() {
        blocks.clear();
    }

    void set(BlockPos pos, BlockState state) {
        blocks.put(pos.immutable(), state);
    }

    void setIfAbsent(BlockPos pos, BlockState state) {
        blocks.putIfAbsent(pos.immutable(), state);
    }

    @Override
    public BlockState getBlockState(BlockPos pos) {
        return blocks.getOrDefault(pos, Blocks.AIR.defaultBlockState());
    }

    @Override
    public FluidState getFluidState(BlockPos pos) {
        return getBlockState(pos).getFluidState();
    }

    @Override
    public BlockEntity getBlockEntity(BlockPos pos) {
        return null;
    }

    @Override
    public int getHeight() {
        return type.height();
    }

    @Override
    public int getMinBuildHeight() {
        return type.minY();
    }

    @Override
    public ChunkAccess getChunk(int x, int z, ChunkStatus status, boolean require) {
        return null;
    }

    @Override
    public boolean hasChunk(int x, int z) {
        return true;
    }

    @Override
    public int getHeight(Heightmap.Types types, int x, int z) {
        return FarmingDimension.SURFACE_Y + 1;
    }

    @Override
    public int getSkyDarken() {
        return 0;
    }

    @Override
    public BiomeManager getBiomeManager() {
        return new BiomeManager(this, 0L);
    }

    @Override
    public Holder<Biome> getNoiseBiome(int x, int y, int z) {
        return biome;
    }

    @Override
    public Holder<Biome> getUncachedNoiseBiome(int x, int y, int z) {
        return biome;
    }

    @Override
    public boolean isClientSide() {
        return false;
    }

    @Override
    public int getSeaLevel() {
        return FarmingDimension.SURFACE_Y - 1;
    }

    @Override
    public DimensionType dimensionType() {
        return type;
    }

    @Override
    public RegistryAccess registryAccess() {
        return registries;
    }

    @Override
    public FeatureFlagSet enabledFeatures() {
        return features;
    }

    @Override
    public float getShade(Direction direction, boolean shade) {
        return 1.0F;
    }

    @Override
    public LevelLightEngine getLightEngine() {
        return light;
    }

    @Override
    public int getBrightness(LightLayer layer, BlockPos pos) {
        return 15;
    }

    @Override
    public int getRawBrightness(BlockPos pos, int amount) {
        return 15;
    }

    @Override
    public boolean canSeeSky(BlockPos pos) {
        return true;
    }

    @Override
    public int getBlockTint(BlockPos pos, ColorResolver resolver) {
        return 0;
    }

    @Override
    public WorldBorder getWorldBorder() {
        return border;
    }

    @Override
    public BlockGetter getChunkForCollisions(int x, int z) {
        return this;
    }

    @Override
    public List<VoxelShape> getEntityCollisions(Entity entity, AABB box) {
        return List.of();
    }
}
