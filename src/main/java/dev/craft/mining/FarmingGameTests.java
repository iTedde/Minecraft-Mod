package dev.craft.mining;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.mining.FarmingPlants.Plant;
import dev.craft.mining.FarmingPlants.PlantList;
import dev.craft.mining.FarmingPlants.Soil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CocoaBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Plant list and garden grid of the farming dimension (spec 53 §3, §4). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class FarmingGameTests {
    private static final String EMPTY = "empty_16x8x16";
    /** North-west path corner of the test bed, on the grass layer. */
    private static final BlockPos BED = new BlockPos(2, 2, 2);

    private FarmingGameTests() {
    }

    private static PlantList plants(GameTestHelper helper) {
        return FarmingPlants.get(helper.getLevel().registryAccess());
    }

    private static Plant plant(GameTestHelper helper, Block block) {
        return plants(helper).find(block).orElseThrow(() -> new AssertionError(
                "missing in the plant list: " + BuiltInRegistries.BLOCK.getKey(block)));
    }

    private static void assertSoil(GameTestHelper helper, Block block, Soil soil) {
        Plant plant = plant(helper, block);
        helper.assertTrue(plant.soil() == soil, BuiltInRegistries.BLOCK.getKey(block) + " on " + plant.soil()
                + ", expected " + soil);
    }

    /** Vanilla cases of spec 53 §3.1/§3.2: each plant with its soil and grown state. */
    @GameTest(template = EMPTY)
    public static void plantListFindsSoils(GameTestHelper helper) {
        assertSoil(helper, Blocks.WHEAT, Soil.FARMLAND);
        helper.assertTrue(plant(helper, Blocks.WHEAT).state().getValue(CropBlock.AGE) == 7, "wheat grown");
        assertSoil(helper, Blocks.POPPY, Soil.GRASS);
        assertSoil(helper, Blocks.OAK_SAPLING, Soil.GRASS);
        assertSoil(helper, Ic2Blocks.RUBBER_SAPLING.get(), Soil.GRASS);
        assertSoil(helper, Blocks.SUNFLOWER, Soil.GRASS);
        helper.assertTrue(plant(helper, Blocks.SUNFLOWER).isDouble(), "sunflower two blocks");
        assertSoil(helper, Blocks.SUGAR_CANE, Soil.GRASS);
        helper.assertTrue(plant(helper, Blocks.SUGAR_CANE).wet(), "sugar cane next to water");
        helper.assertTrue(plant(helper, Blocks.SUGAR_CANE).height() == FarmingPlants.STACK, "sugar cane stacked");
        assertSoil(helper, Blocks.CACTUS, Soil.SAND);
        helper.assertTrue(plant(helper, Blocks.CACTUS).sparse(), "cactus every other cell");
        assertSoil(helper, Blocks.BAMBOO, Soil.GRASS);
        assertSoil(helper, Blocks.SWEET_BERRY_BUSH, Soil.GRASS);
        assertSoil(helper, Blocks.NETHER_WART, Soil.SOUL_SAND);
        assertSoil(helper, Blocks.BROWN_MUSHROOM, Soil.MYCELIUM);
        assertSoil(helper, Blocks.SMALL_DRIPLEAF, Soil.MOSS);
        assertSoil(helper, Blocks.CHORUS_FLOWER, Soil.END_STONE);
        assertSoil(helper, Blocks.LILY_PAD, Soil.WATER_SURFACE);
        assertSoil(helper, Blocks.KELP, Soil.UNDERWATER);
        assertSoil(helper, Blocks.SEAGRASS, Soil.UNDERWATER);
        assertSoil(helper, Blocks.SEA_PICKLE, Soil.UNDERWATER);
        assertSoil(helper, Blocks.COCOA, Soil.JUNGLE_LOG);
        helper.assertTrue(plant(helper, Blocks.COCOA).state().getValue(CocoaBlock.AGE) == CocoaBlock.MAX_AGE,
                "cocoa grown");
        assertSoil(helper, Blocks.CAVE_VINES, Soil.MOSS_CEILING);
        helper.assertTrue(plant(helper, Blocks.CAVE_VINES).state().getValue(BlockStateProperties.BERRIES),
                "glow berries on");
        assertSoil(helper, Blocks.SPORE_BLOSSOM, Soil.MOSS_CEILING);
        Plant melon = plant(helper, Blocks.MELON_STEM);
        helper.assertTrue(melon.soil() == Soil.FARMLAND && melon.fruit() != null && melon.fruit().is(Blocks.MELON)
                && melon.state().is(Blocks.ATTACHED_MELON_STEM), "melon as attached stem with fruit");
        plant(helper, Blocks.PITCHER_CROP);
        plant(helper, Blocks.TORCHFLOWER_CROP);
        plant(helper, Blocks.CRIMSON_FUNGUS);
        helper.assertTrue(plants(helper).find(Blocks.ATTACHED_MELON_STEM).isEmpty(), "no attached stem entry");
        helper.assertTrue(plants(helper).find(Blocks.KELP_PLANT).isEmpty(), "no kelp body (no item)");
        helper.assertTrue(plants(helper).find(Blocks.FIRE).isEmpty(), "no fire");
        List<Plant> list = plants(helper).plants();
        for (int i = 1; i < list.size(); i++) {
            String before = BuiltInRegistries.BLOCK.getKey(list.get(i - 1).block()).toString();
            String after = BuiltInRegistries.BLOCK.getKey(list.get(i).block()).toString();
            helper.assertTrue(before.compareTo(after) < 0, "sorted by id: " + before + " before " + after);
        }
        helper.succeed();
    }

    /**
     * Spec 53 §1 "darf nicht passieren": every listed plant is built in its bed and every plant block placed there
     * survives on its spot. Beds are built one after the other in the same place.
     */
    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void everyBedHoldsItsPlant(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos corner = helper.absolutePos(BED);
        for (Plant plant : plants(helper).plants()) {
            clear(level, corner);
            FarmingGardens.placeBed(level, corner, plant);
            int found = 0;
            for (BlockPos pos : BlockPos.betweenClosed(corner.offset(0, 1, 0),
                    corner.offset(FarmingGardens.BED - 1, 4, FarmingGardens.BED - 1))) {
                BlockState state = level.getBlockState(pos);
                if (state.is(plant.state().getBlock())) {
                    found++;
                    helper.assertTrue(state.canSurvive(level, pos), BuiltInRegistries.BLOCK.getKey(plant.block())
                            + " cannot stay at " + pos.subtract(corner));
                }
            }
            if (plant.soil() == Soil.UNDERWATER) {
                for (BlockPos pos : BlockPos.betweenClosed(corner, corner.offset(FarmingGardens.BED - 1, 0,
                        FarmingGardens.BED - 1))) {
                    if (level.getBlockState(pos).is(plant.state().getBlock())) {
                        found++;
                        helper.assertTrue(level.getBlockState(pos).canSurvive(level, pos),
                                BuiltInRegistries.BLOCK.getKey(plant.block()) + " cannot stay under water");
                    }
                }
            }
            helper.assertTrue(found > 0, BuiltInRegistries.BLOCK.getKey(plant.block()) + " not placed");
        }
        clear(level, corner);
        helper.succeed();
    }

    private static void clear(ServerLevel level, BlockPos corner) {
        for (BlockPos pos : BlockPos.betweenClosed(corner.offset(0, -1, 0),
                corner.offset(FarmingGardens.BED - 1, 5, FarmingGardens.BED - 1))) {
            level.setBlock(pos, pos.getY() < corner.getY() ? Blocks.DIRT.defaultBlockState()
                    : Blocks.AIR.defaultBlockState(), 2);
        }
    }

    /** Wheat bed: farmland with covered water in the middle, 24 grown plants, sign with the name at the corner. */
    @GameTest(template = EMPTY)
    public static void wheatBedLayout(GameTestHelper helper) {
        Plant wheat = plant(helper, Blocks.WHEAT);
        FarmingGardens.placeBed(helper.getLevel(), helper.absolutePos(BED), wheat);
        helper.assertBlockPresent(Blocks.DIRT_PATH, BED);
        helper.assertBlockPresent(Blocks.DIRT_PATH, BED.offset(6, 0, 3));
        helper.assertBlockPresent(Blocks.WATER, BED.offset(3, 0, 3));
        helper.assertBlockPresent(Blocks.LILY_PAD, BED.offset(3, 1, 3));
        int wheatCount = 0;
        for (int x = 1; x <= 5; x++) {
            for (int z = 1; z <= 5; z++) {
                if (helper.getBlockState(BED.offset(x, 1, z)).is(Blocks.WHEAT)) {
                    wheatCount++;
                    helper.assertBlockPresent(Blocks.FARMLAND, BED.offset(x, 0, z));
                }
            }
        }
        helper.assertTrue(wheatCount == 24, "24 wheat, got " + wheatCount);
        helper.assertBlockPresent(Blocks.OAK_SIGN, BED.above());
        if (!(helper.getBlockEntity(BED.above()) instanceof SignBlockEntity sign)
                || !sign.getFrontText().getMessage(1, false).equals(wheat.name()) || !sign.isWaxed()) {
            helper.fail("sign with the plant name");
            return;
        }
        helper.succeed();
    }

    /** Every plant appears exactly once per garden; gardens repeat (spec 53 §3.3). */
    @GameTest(template = EMPTY)
    public static void gardenHoldsEveryPlantOnce(GameTestHelper helper) {
        PlantList list = plants(helper);
        int garden = FarmingGardens.BED * list.side();
        helper.assertTrue(list.side() * list.side() >= list.plants().size()
                && (list.side() - 1) * (list.side() - 1) < list.plants().size(), "side = ceil(sqrt(n))");
        Map<Block, Integer> count = new HashMap<>();
        int originX = -3 * garden;
        int originZ = 5 * garden;
        for (int bx = 0; bx < list.side(); bx++) {
            for (int bz = 0; bz < list.side(); bz++) {
                int x = originX + bx * FarmingGardens.BED + 3;
                int z = originZ + bz * FarmingGardens.BED + 3;
                Plant plant = FarmingGardens.bedPlant(list, x, z);
                if (plant != null) {
                    count.merge(plant.block(), 1, Integer::sum);
                }
                helper.assertTrue(plant == FarmingGardens.bedPlant(list, x + garden, z - 7 * garden),
                        "gardens repeat");
            }
        }
        helper.assertTrue(count.size() == list.plants().size() && count.values().stream().allMatch(c -> c == 1),
                "each plant once per garden");
        helper.succeed();
    }

    /** A new exit portal in the farming dimension stands on a path line, never in a bed (spec 53 §4). */
    @GameTest(template = EMPTY)
    public static void exitPortalOnPath(GameTestHelper helper) {
        for (int[] p : new int[][]{{0, 0}, {3, 4}, {-4, -3}, {123, -456}, {-1000, 999}}) {
            BlockPos pos = new BlockPos(p[0], 80, p[1]);
            BlockPos alongX = FarmingGardens.exitCorner(pos, Direction.Axis.X);
            BlockPos alongZ = FarmingGardens.exitCorner(pos, Direction.Axis.Z);
            helper.assertTrue(Math.floorMod(alongX.getZ(), FarmingGardens.BED) == 0
                    && Math.abs(alongX.getZ() - pos.getZ()) <= 3 && alongX.getX() == pos.getX(), "X portal on path");
            helper.assertTrue(Math.floorMod(alongZ.getX(), FarmingGardens.BED) == 0
                    && Math.abs(alongZ.getX() - pos.getX()) <= 3 && alongZ.getZ() == pos.getZ(), "Z portal on path");
            helper.assertTrue(alongX.getY() == FarmingDimension.SURFACE_Y + 1, "portal on the surface");
        }
        helper.succeed();
    }
}
