package dev.craft.mining;

import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.crop.CropCard;
import dev.craft.ic2.crop.Crops;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeMap;

import com.mojang.logging.LogUtils;
import dev.craft.Craft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.AzaleaBlock;
import net.minecraft.world.level.block.BambooStalkBlock;
import net.minecraft.world.level.block.BigDripleafBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CactusBlock;
import net.minecraft.world.level.block.CaveVinesBlock;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.FarmBlock;
import net.minecraft.world.level.block.FungusBlock;
import net.minecraft.world.level.block.GrowingPlantBlock;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.block.MangrovePropaguleBlock;
import net.minecraft.world.level.block.MushroomBlock;
import net.minecraft.world.level.block.NetherSproutsBlock;
import net.minecraft.world.level.block.NetherWartBlock;
import net.minecraft.world.level.block.PitcherCropBlock;
import net.minecraft.world.level.block.RootsBlock;
import net.minecraft.world.level.block.SeaPickleBlock;
import net.minecraft.world.level.block.SeagrassBlock;
import net.minecraft.world.level.block.SmallDripleafBlock;
import net.minecraft.world.level.block.SporeBlossomBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.SugarCaneBlock;
import net.minecraft.world.level.block.SweetBerryBushBlock;
import net.minecraft.world.level.block.WaterlilyBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.dimension.BuiltinDimensionTypes;
import net.minecraft.world.level.dimension.DimensionType;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Plant list of the farming dimension (spec 53 §3.1/§3.2), built once per server from all registered blocks: wild
 * plants of other mods, crops, saplings, flowers, mushrooms and water plants, each with the first soil it survives
 * on. Sorted by registry id so the gardens are the same in every world with the same mods.
 */
public final class FarmingPlants {
    private static final Logger LOGGER = LogUtils.getLogger();

    /** Blocks added to the list although no rule picks them (spec 53 §3.1). */
    public static final TagKey<Block> EXTRA = tag("farming_extra");
    /** Blocks left out although a rule picks them (spec 53 §3.1). */
    public static final TagKey<Block> EXCLUDED = tag("farming_excluded");

    /** Plant position in the probe; the soil is one below. */
    private static final BlockPos PLANT = new BlockPos(0, FarmingDimension.SURFACE_Y + 1, 0);
    private static final BlockPos SOIL = PLANT.below();
    /** Stack height of sugar cane, cactus, bamboo and other plants that grow on themselves. */
    static final int STACK = 3;

    private static @Nullable Snapshot cache;

    private FarmingPlants() {
    }

    private static TagKey<Block> tag(String name) {
        return TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath(Craft.MODID, name));
    }

    /**
     * How a bed holds its plant (spec 53 §3.2), in the order the soils are tried for land plants. Water plants try
     * {@link #UNDERWATER} first.
     */
    public enum Soil {
        GRASS(Blocks.GRASS_BLOCK.defaultBlockState()),
        FARMLAND(Blocks.FARMLAND.defaultBlockState().setValue(FarmBlock.MOISTURE, FarmBlock.MAX_MOISTURE)),
        SAND(Blocks.SAND.defaultBlockState()),
        SOUL_SAND(Blocks.SOUL_SAND.defaultBlockState()),
        MYCELIUM(Blocks.MYCELIUM.defaultBlockState()),
        PODZOL(Blocks.PODZOL.defaultBlockState()),
        MOSS(Blocks.MOSS_BLOCK.defaultBlockState()),
        CRIMSON_NYLIUM(Blocks.CRIMSON_NYLIUM.defaultBlockState()),
        WARPED_NYLIUM(Blocks.WARPED_NYLIUM.defaultBlockState()),
        END_STONE(Blocks.END_STONE.defaultBlockState()),
        /** Floating on water one block deep (lily pad). */
        WATER_SURFACE(Blocks.WATER.defaultBlockState()),
        /** In water one block deep over dirt (seagrass, kelp, rice …). */
        UNDERWATER(Blocks.DIRT.defaultBlockState()),
        /** On the side of a jungle log (cocoa). */
        JUNGLE_LOG(Blocks.GRASS_BLOCK.defaultBlockState()),
        /** Hanging from a moss block ceiling (glow berries, spore blossom). */
        MOSS_CEILING(Blocks.GRASS_BLOCK.defaultBlockState());

        private final BlockState block;

        Soil(BlockState block) {
            this.block = block;
        }

        /** Block under the plant (or the ground of the bed for the side and hanging soils). */
        public BlockState block() {
            return block;
        }

        boolean isLand() {
            return ordinal() <= END_STONE.ordinal();
        }
    }

    /**
     * One bed's plant.
     *
     * @param block  the listed block (the stem for melons and pumpkins)
     * @param soil   how the bed holds it
     * @param state  the placed (lower) state, grown up
     * @param fruit  fruit next to an attached stem, null for everything else
     * @param height blocks stacked on each other (1, or {@link #STACK})
     * @param sparse only every other cell, for plants that cannot stand next to each other (cactus)
     * @param wet    needs water next to its soil (sugar cane): the bed gets a water row in the middle
     * @param name   sign text
     * @param ic2    IC2 crop species planted on crop sticks in this bed, null for block plants
     */
    public record Plant(Block block, Soil soil, BlockState state, @Nullable BlockState fruit, int height,
                        boolean sparse, boolean wet, Component name, @Nullable CropCard ic2) {
        public boolean isDouble() {
            return state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF);
        }

        /** Upper half of a two block plant. */
        public BlockState upper() {
            BlockState upper = state.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
            return upper.hasProperty(BlockStateProperties.WATERLOGGED)
                    ? upper.setValue(BlockStateProperties.WATERLOGGED, false) : upper;
        }
    }

    /**
     * The finished list and the garden size.
     *
     * @param plants plants in bed order
     * @param side   beds per garden side, ⌈√n⌉ (at least 1)
     */
    public record PlantList(List<Plant> plants, int side) {
        public @Nullable Plant bed(int index) {
            return index < plants.size() ? plants.get(index) : null;
        }

        public Optional<Plant> find(Block block) {
            return plants.stream().filter(plant -> plant.block() == block).findFirst();
        }
    }

    private record Snapshot(RegistryAccess registries, PlantList list) {
    }

    /** List for this server's registries; built on first use (server start) and kept until the registries change. */
    public static synchronized PlantList get(RegistryAccess registries) {
        Snapshot snapshot = cache;
        if (snapshot == null || snapshot.registries() != registries) {
            snapshot = new Snapshot(registries, build(registries));
            cache = snapshot;
        }
        return snapshot.list();
    }

    private static PlantList build(RegistryAccess registries) {
        Holder<Biome> biome = registries.registryOrThrow(Registries.BIOME).getHolder(FarmingDimension.BIOME)
                .or(() -> registries.registryOrThrow(Registries.BIOME).getHolder(Biomes.PLAINS))
                .map(holder -> (Holder<Biome>) holder).orElseThrow();
        DimensionType type = registries.registryOrThrow(Registries.DIMENSION_TYPE)
                .getOptional(FarmingDimension.TYPE)
                .or(() -> registries.registryOrThrow(Registries.DIMENSION_TYPE)
                        .getOptional(BuiltinDimensionTypes.OVERWORLD))
                .orElseThrow();
        FarmingProbe probe = new FarmingProbe(registries, biome, type, FeatureFlags.REGISTRY.allFlags());

        List<Block> candidates = new ArrayList<>();
        Set<String> wildForms = new HashSet<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            if (block.asItem() == Items.AIR || block.defaultBlockState().is(EXCLUDED)) {
                continue;
            }
            if (isPlant(block) || block.defaultBlockState().is(EXTRA)) {
                candidates.add(block);
                String path = BuiltInRegistries.BLOCK.getKey(block).getPath();
                if (path.startsWith("wild_")) {
                    wildForms.add(path.substring("wild_".length()));
                }
            }
        }
        candidates.removeIf(block -> wildForms.contains(BuiltInRegistries.BLOCK.getKey(block).getPath()));
        candidates.sort(Comparator.comparing(block -> BuiltInRegistries.BLOCK.getKey(block).toString()));

        List<Plant> plants = new ArrayList<>();
        Map<String, Integer> perMod = new TreeMap<>();
        List<String> skipped = new ArrayList<>();
        for (Block block : candidates) {
            ResourceLocation id = BuiltInRegistries.BLOCK.getKey(block);
            if (block instanceof EntityBlock && !block.defaultBlockState().is(EXTRA)) {
                skipped.add(id + " (block entity)");
                continue;
            }
            Plant plant = resolve(probe, block);
            if (plant == null) {
                skipped.add(id + " (no soil)");
                continue;
            }
            plants.add(plant);
            perMod.merge(id.getNamespace(), 1, Integer::sum);
        }
        for (CropCard crop : Crops.all()) {
            if (crop != Crops.WEED) {
                plants.add(ic2Plant(crop));
                perMod.merge("ic2 crops", 1, Integer::sum);
            }
        }
        int side = Math.max(1, (int) Math.ceil(Math.sqrt(plants.size())));
        LOGGER.info("Farming dimension: {} plants in gardens of {}x{} beds, per mod {}", plants.size(), side, side,
                perMod);
        if (!skipped.isEmpty()) {
            LOGGER.info("Farming dimension: left out {}", skipped);
        }
        return new PlantList(List.copyOf(plants), side);
    }

    /**
     * IC2 crop bed (spec 53 §3.4): crop sticks on farmland, after the block plants in crop id order. The weed is
     * left out.
     */
    static Plant ic2Plant(CropCard crop) {
        return new Plant(Ic2Blocks.CROP.get(), Soil.FARMLAND, Ic2Blocks.CROP.get().defaultBlockState(), null, 1, false,
                false, crop.displayName(), crop);
    }

    /** Selection rules of spec 53 §3.1 for block plants; the IC2 crops come from {@link Crops} (§3.4). */
    static boolean isPlant(Block block) {
        if (BuiltInRegistries.BLOCK.getKey(block).getPath().startsWith("wild_")) {
            return true;
        }
        if (block instanceof CropBlock || block instanceof StemBlock || block instanceof NetherWartBlock
                || block instanceof CocoaBlock || block instanceof SweetBerryBushBlock
                || block instanceof SugarCaneBlock || block instanceof CactusBlock || block instanceof BambooStalkBlock
                || block instanceof CaveVinesBlock || block instanceof PitcherCropBlock) {
            return true;
        }
        BlockState state = block.defaultBlockState();
        if (state.is(BlockTags.SAPLINGS) || state.is(BlockTags.SMALL_FLOWERS) || state.is(BlockTags.TALL_FLOWERS)) {
            return true;
        }
        return block instanceof MushroomBlock || block instanceof FungusBlock || block instanceof RootsBlock
                || block instanceof NetherSproutsBlock || block instanceof AzaleaBlock
                || block instanceof MangrovePropaguleBlock || block instanceof SmallDripleafBlock
                || block instanceof BigDripleafBlock || block instanceof SporeBlossomBlock
                || block instanceof ChorusFlowerBlock || block instanceof WaterlilyBlock || block instanceof KelpBlock
                || block instanceof SeagrassBlock || block instanceof SeaPickleBlock;
    }

    /** First soil the plant survives on, with its grown state and bed shape; null if there is none. */
    static @Nullable Plant resolve(FarmingProbe probe, Block block) {
        BlockState base = grown(block);
        BlockState fruit = null;
        if (block instanceof StemBlock stem) {
            Optional<Block> attached = BuiltInRegistries.BLOCK.getOptional(stem.attachedStem);
            Optional<Block> fruitBlock = BuiltInRegistries.BLOCK.getOptional(stem.fruit);
            if (attached.isEmpty() || fruitBlock.isEmpty()) {
                return null;
            }
            base = attached.get().defaultBlockState();
            if (base.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                base = base.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.EAST);
            }
            fruit = fruitBlock.get().defaultBlockState();
        }
        Component name = (fruit != null ? fruit.getBlock() : block).asItem().getDescription();
        List<Soil> order = new ArrayList<>(List.of(Soil.values()));
        if (base.getFluidState().is(FluidTags.WATER)) {
            order.remove(Soil.UNDERWATER);
            order.addFirst(Soil.UNDERWATER);
        }
        for (Soil soil : order) {
            for (boolean wet : soil.isLand() ? new boolean[]{false, true} : new boolean[]{false}) {
                BlockState state = stateFor(base, soil);
                if (state == null || !survives(probe, soil, state, fruit, wet, false)) {
                    continue;
                }
                boolean sparse = !survives(probe, soil, state, fruit, wet, true);
                int height = 1;
                if (soil.isLand() && fruit == null && !state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                        && stacks(probe, soil, state, wet)) {
                    height = STACK;
                }
                return new Plant(block, soil, state, fruit, height, sparse, wet, name, null);
            }
        }
        return null;
    }

    /** Grown-up state (spec 53 §3.1): highest age, berries on; growing vines, cane, cactus and chorus stay young. */
    static BlockState grown(Block block) {
        BlockState state = block.defaultBlockState();
        if (block instanceof CropBlock crop) {
            return crop.getStateForAge(crop.getMaxAge());
        }
        if (block instanceof ChorusFlowerBlock || block instanceof SugarCaneBlock || block instanceof CactusBlock
                || block instanceof GrowingPlantBlock || block instanceof BambooStalkBlock
                || block instanceof MangrovePropaguleBlock) {
            return state.hasProperty(BlockStateProperties.BERRIES)
                    ? state.setValue(BlockStateProperties.BERRIES, true) : state;
        }
        for (Property<?> property : state.getProperties()) {
            if (property instanceof IntegerProperty age && age.getName().equals("age")) {
                state = state.setValue(age, age.getPossibleValues().stream().max(Integer::compare).orElseThrow());
            }
        }
        if (state.hasProperty(BlockStateProperties.BERRIES)) {
            state = state.setValue(BlockStateProperties.BERRIES, true);
        }
        return state;
    }

    /** Plant state for a soil: water logging, facing towards the log, hanging; null if the soil cannot hold it. */
    private static @Nullable BlockState stateFor(BlockState base, Soil soil) {
        BlockState state = base;
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            state = state.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
        }
        if (state.hasProperty(BlockStateProperties.WATERLOGGED)) {
            state = state.setValue(BlockStateProperties.WATERLOGGED, soil == Soil.UNDERWATER);
        }
        if (state.hasProperty(BlockStateProperties.HANGING)) {
            state = state.setValue(BlockStateProperties.HANGING, soil == Soil.MOSS_CEILING);
        }
        if (soil == Soil.UNDERWATER && !state.getFluidState().is(FluidTags.WATER)) {
            return null;
        }
        if (soil == Soil.JUNGLE_LOG) {
            if (!state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
                return null;
            }
            state = state.setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
        }
        return state;
    }

    /**
     * Builds the probe for one bed cell and asks {@code canSurvive}. With {@code neighbour} a second plant stands
     * east of the first (sparse test).
     */
    private static boolean survives(FarmingProbe probe, Soil soil, BlockState state, @Nullable BlockState fruit,
                                    boolean wet, boolean neighbour) {
        probe.clear();
        setUp(probe, soil, state, fruit, wet, BlockPos.ZERO);
        if (neighbour) {
            setUp(probe, soil, state, fruit, wet, fruit != null ? new BlockPos(0, 0, 1) : new BlockPos(1, 0, 0));
        }
        return check(probe, state, PLANT) && (!neighbour
                || check(probe, state, PLANT.offset(fruit != null ? new BlockPos(0, 0, 1) : new BlockPos(1, 0, 0))));
    }

    private static boolean stacks(FarmingProbe probe, Soil soil, BlockState state, boolean wet) {
        probe.clear();
        setUp(probe, soil, state, null, wet, BlockPos.ZERO);
        probe.set(PLANT.above(), state);
        return check(probe, state, PLANT.above()) && check(probe, state, PLANT);
    }

    private static void setUp(FarmingProbe probe, Soil soil, BlockState state, @Nullable BlockState fruit, boolean wet,
                              BlockPos offset) {
        BlockPos plant = PLANT.offset(offset);
        BlockPos ground = SOIL.offset(offset);
        switch (soil) {
            case WATER_SURFACE -> {
                probe.set(ground.below(), Blocks.DIRT.defaultBlockState());
                probe.set(ground, Blocks.WATER.defaultBlockState());
            }
            case UNDERWATER -> {
                probe.set(ground, soil.block());
                for (Direction side : Direction.Plane.HORIZONTAL) {
                    probe.setIfAbsent(plant.relative(side), Blocks.WATER.defaultBlockState());
                }
            }
            case JUNGLE_LOG -> {
                probe.set(ground, soil.block());
                probe.set(plant.north(), Blocks.JUNGLE_LOG.defaultBlockState());
            }
            case MOSS_CEILING -> probe.set(plant.above(), Blocks.MOSS_BLOCK.defaultBlockState());
            default -> {
                if (wet) {
                    for (Direction side : Direction.Plane.HORIZONTAL) {
                        probe.setIfAbsent(ground.relative(side), Blocks.WATER.defaultBlockState());
                    }
                }
                probe.set(ground, soil.block());
            }
        }
        probe.set(plant, state);
        if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
            BlockState upper = state.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);
            probe.set(plant.above(), upper.hasProperty(BlockStateProperties.WATERLOGGED)
                    ? upper.setValue(BlockStateProperties.WATERLOGGED, false) : upper);
        }
        if (fruit != null) {
            probe.set(plant.east(), fruit);
        }
    }

    private static boolean check(FarmingProbe probe, BlockState state, BlockPos pos) {
        try {
            if (!probe.getBlockState(pos).canSurvive(probe, pos)) {
                return false;
            }
            if (state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)) {
                return probe.getBlockState(pos.above()).canSurvive(probe, pos.above());
            }
            return true;
        } catch (RuntimeException e) {
            return false;
        }
    }
}
