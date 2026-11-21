package dev.craft.mining;

import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.crop.CropBlock;
import dev.craft.ic2.crop.CropBlockEntity;
import dev.craft.ic2.crop.CropCard;
import dev.craft.ic2.crop.MetalCrop;
import dev.craft.mining.FarmingPlants.Plant;
import dev.craft.mining.FarmingPlants.PlantList;
import dev.craft.mining.FarmingPlants.Soil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import org.jetbrains.annotations.Nullable;

/**
 * Garden grid of the farming dimension (spec 53 §3.3): beds of 7×7 blocks (5×5 plants inside a path ring), one plant
 * per bed in list order, k×k beds per garden, gardens repeat everywhere. Every column is filled from its own
 * coordinates only, so chunks fit together in any generation order.
 */
public final class FarmingGardens extends Feature<NoneFeatureConfiguration> {
    public static final int BED = 7;
    private static final int INNER = 5;
    private static final int CENTER = 2;
    /** Rotation of the sign in the north-west corner of a bed: facing north-west, out onto the paths. */
    private static final int SIGN_ROTATION = 6;
    private static final int FLAGS = 2;

    public FarmingGardens() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        PlantList plants = FarmingPlants.get(level.registryAccess());
        ChunkPos chunk = new ChunkPos(context.origin());
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = chunk.getMinBlockX(); x <= chunk.getMaxBlockX(); x++) {
            for (int z = chunk.getMinBlockZ(); z <= chunk.getMaxBlockZ(); z++) {
                fillColumn(level, pos, x, z, FarmingDimension.SURFACE_Y, bedPlant(plants, x, z),
                        Math.floorMod(x, BED), Math.floorMod(z, BED));
            }
        }
        return true;
    }

    /** Plant of the bed containing this column; null for the empty beds at the end of a garden. */
    public static @Nullable Plant bedPlant(PlantList plants, int x, int z) {
        int garden = BED * plants.side();
        int bedX = Math.floorMod(x, garden) / BED;
        int bedZ = Math.floorMod(z, garden) / BED;
        return plants.bed(bedZ * plants.side() + bedX);
    }

    /** Places one bed with its north-west path corner at {@code corner} (y = grass layer); used by the game tests. */
    public static void placeBed(WorldGenLevel level, BlockPos corner, @Nullable Plant plant) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int lx = 0; lx < BED; lx++) {
            for (int lz = 0; lz < BED; lz++) {
                fillColumn(level, pos, corner.getX() + lx, corner.getZ() + lz, corner.getY(), plant, lx, lz);
            }
        }
    }

    /**
     * Corner of an exit portal in the farming dimension (spec 53 §4): on the path line nearest to {@code pos}
     * across the portal axis, standing on the path layer, so the portal never lands in a bed.
     */
    public static BlockPos exitCorner(BlockPos pos, Direction.Axis axis) {
        int y = FarmingDimension.SURFACE_Y + 1;
        if (axis == Direction.Axis.X) {
            return new BlockPos(pos.getX(), y, nearestPathLine(pos.getZ()));
        }
        return new BlockPos(nearestPathLine(pos.getX()), y, pos.getZ());
    }

    /** Nearest coordinate with bed offset 0 (the west/north path ring of a bed). */
    static int nearestPathLine(int coordinate) {
        return Math.floorDiv(coordinate + BED / 2, BED) * BED;
    }

    private static void fillColumn(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int z, int surface,
                                   @Nullable Plant plant, int lx, int lz) {
        if (lx == 0 || lz == 0 || lx == BED - 1 || lz == BED - 1) {
            set(level, pos.set(x, surface, z), Blocks.DIRT_PATH.defaultBlockState());
            if (lx == 0 && lz == 0 && plant != null) {
                placeSign(level, pos.set(x, surface + 1, z), plant);
            }
            return;
        }
        set(level, pos.set(x, surface, z), Blocks.GRASS_BLOCK.defaultBlockState());
        if (plant != null) {
            fillCell(level, pos, x, z, surface, plant, lx - 1, lz - 1);
        }
    }

    private static void fillCell(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int z, int surface,
                                 Plant plant, int i, int j) {
        boolean corner = (i == 0 || i == INNER - 1) && (j == 0 || j == INNER - 1);
        boolean center = i == CENTER && j == CENTER;
        boolean skip = plant.sparse() && (i + j) % 2 == 1;
        switch (plant.soil()) {
            case WATER_SURFACE -> {
                set(level, pos.set(x, surface - 1, z), Blocks.DIRT.defaultBlockState());
                set(level, pos.set(x, surface, z), Blocks.WATER.defaultBlockState());
                if (!skip) {
                    set(level, pos.set(x, surface + 1, z), plant.state());
                }
            }
            case UNDERWATER -> {
                set(level, pos.set(x, surface - 1, z), Blocks.DIRT.defaultBlockState());
                set(level, pos.set(x, surface, z), skip ? Blocks.WATER.defaultBlockState() : plant.state());
            }
            case JUNGLE_LOG -> {
                if (center) {
                    for (int h = 1; h <= FarmingPlants.STACK; h++) {
                        set(level, pos.set(x, surface + h, z), Blocks.JUNGLE_LOG.defaultBlockState());
                    }
                } else if (Math.abs(i - CENTER) + Math.abs(j - CENTER) == 1) {
                    Direction toLog = i < CENTER ? Direction.EAST : i > CENTER ? Direction.WEST
                            : j < CENTER ? Direction.SOUTH : Direction.NORTH;
                    BlockState pod = plant.state().setValue(BlockStateProperties.HORIZONTAL_FACING, toLog);
                    for (int h = 1; h <= FarmingPlants.STACK; h++) {
                        set(level, pos.set(x, surface + h, z), pod);
                    }
                }
            }
            case MOSS_CEILING -> {
                int top = surface + FarmingPlants.STACK + 1;
                set(level, pos.set(x, top, z), Blocks.MOSS_BLOCK.defaultBlockState());
                if (corner) {
                    for (int y = surface + 1; y < top; y++) {
                        set(level, pos.set(x, y, z), Blocks.MOSS_BLOCK.defaultBlockState());
                    }
                } else if (!skip) {
                    set(level, pos.set(x, top - 1, z), plant.state());
                }
            }
            default -> fillLand(level, pos, x, z, surface, plant, i, j, center, skip);
        }
    }

    /**
     * Land soils: the soil under every cell, a covered water block in the middle of farmland (or a covered water row
     * for plants that need water next to their soil), melons and pumpkins as stem + fruit pairs.
     */
    private static void fillLand(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int z, int surface,
                                 Plant plant, int i, int j, boolean center, boolean skip) {
        boolean water = plant.wet() ? j == CENTER : plant.soil() == Soil.FARMLAND && center;
        if (water) {
            set(level, pos.set(x, surface, z), Blocks.WATER.defaultBlockState());
            set(level, pos.set(x, surface + 1, z), Blocks.LILY_PAD.defaultBlockState());
            return;
        }
        if (plant.fruit() != null) {
            if (i == 1 || i == INNER - 2) {
                set(level, pos.set(x, surface + 1, z), plant.fruit());
            } else if (i != CENTER) {
                set(level, pos.set(x, surface, z), plant.soil().block());
                set(level, pos.set(x, surface + 1, z), plant.state().setValue(BlockStateProperties.HORIZONTAL_FACING,
                        i == 0 ? Direction.EAST : Direction.WEST));
            } else {
                set(level, pos.set(x, surface, z), plant.soil().block());
            }
            return;
        }
        set(level, pos.set(x, surface, z), plant.soil().block());
        if (skip || plant.wet() && Math.abs(j - CENTER) != 1) {
            return;
        }
        if (plant.ic2() != null) {
            placeIc2Crop(level, pos, x, z, surface, plant.ic2());
            return;
        }
        set(level, pos.set(x, surface + 1, z), plant.state());
        if (plant.isDouble()) {
            set(level, pos.set(x, surface + 2, z), plant.upper());
        } else {
            for (int h = 2; h <= plant.height(); h++) {
                set(level, pos.set(x, surface + h, z), plant.state());
            }
        }
    }

    /**
     * Grown IC2 crop on a crop stick (spec 53 §3.4); metal crops get their ore block under the farmland so the last
     * stage could grow back.
     */
    private static void placeIc2Crop(WorldGenLevel level, BlockPos.MutableBlockPos pos, int x, int z, int surface,
                                     CropCard crop) {
        if (crop instanceof MetalCrop metal) {
            set(level, pos.set(x, surface - 1, z), metal.ore().defaultBlockState());
        }
        pos.set(x, surface + 1, z);
        CropBlockEntity probe = new CropBlockEntity(pos.immutable(), Ic2Blocks.CROP.get().defaultBlockState());
        probe.plantWild(crop);
        set(level, pos, Ic2Blocks.CROP.get().defaultBlockState().setValue(CropBlock.SPRITE, probe.getSprite()));
        if (level.getBlockEntity(pos) instanceof CropBlockEntity entity) {
            entity.plantWild(crop);
        }
    }

    private static void placeSign(WorldGenLevel level, BlockPos pos, Plant plant) {
        set(level, pos, Blocks.OAK_SIGN.defaultBlockState().setValue(StandingSignBlock.ROTATION, SIGN_ROTATION));
        if (level.getBlockEntity(pos) instanceof SignBlockEntity sign) {
            labelSign(sign, plant.name(), level.registryAccess());
        }
    }

    /**
     * Writes the plant name on both sides and waxes the sign. During chunk generation the sign has no level yet and
     * {@code setText}/{@code setWaxed} would crash in {@code markUpdated}, so the texts go through the saved data.
     */
    static void labelSign(SignBlockEntity sign, Component name, HolderLookup.Provider registries) {
        CompoundTag tag = sign.saveWithoutMetadata(registries);
        tag.put("front_text", SignText.DIRECT_CODEC.encodeStart(NbtOps.INSTANCE,
                sign.getFrontText().setMessage(1, name)).getOrThrow());
        tag.put("back_text", SignText.DIRECT_CODEC.encodeStart(NbtOps.INSTANCE,
                sign.getBackText().setMessage(1, name)).getOrThrow());
        tag.putBoolean("is_waxed", true);
        sign.loadWithComponents(tag, registries);
    }

    private static void set(WorldGenLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, FLAGS);
    }
}
