package dev.craft.mining;

import java.util.Arrays;

import dev.craft.Craft;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.FixedBiomeSource;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Craft portals (spec 18 §4, spec 53 §4) and the farming dimension terrain (spec 53 §2). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class PortalGameTests {
    private static final String EMPTY = "empty_16x8x16";
    /** Lower left corner of the 4×5 frame along X; the inner area is x 4–5, y 2–4. */
    private static final BlockPos FRAME = new BlockPos(3, 1, 4);
    private static final BlockPos INSIDE = FRAME.offset(1, 1, 0);

    private PortalGameTests() {
    }

    /** 2×3 frame without corners along X. */
    private static void buildFrame(GameTestHelper helper, Block frame) {
        for (int x = 1; x <= 2; x++) {
            helper.setBlock(FRAME.offset(x, 0, 0), frame);
            helper.setBlock(FRAME.offset(x, 4, 0), frame);
        }
        for (int y = 1; y <= 3; y++) {
            helper.setBlock(FRAME.offset(0, y, 0), frame);
            helper.setBlock(FRAME.offset(3, y, 0), frame);
        }
    }

    private static void assertFilled(GameTestHelper helper, Block portal) {
        for (int x = 1; x <= 2; x++) {
            for (int y = 1; y <= 3; y++) {
                helper.assertBlockPresent(portal, FRAME.offset(x, y, 0));
            }
        }
    }

    @GameTest(template = EMPTY)
    public static void fireLightsMiningPortal(GameTestHelper helper) {
        buildFrame(helper, Blocks.SMOOTH_STONE);
        helper.setBlock(INSIDE, Blocks.FIRE);
        helper.succeedWhen(() -> assertFilled(helper, MiningBlocks.MINING_PORTAL.get()));
    }

    @GameTest(template = EMPTY)
    public static void fireLightsFarmingPortal(GameTestHelper helper) {
        buildFrame(helper, Blocks.MOSSY_COBBLESTONE);
        helper.setBlock(INSIDE, Blocks.FIRE);
        helper.succeedWhen(() -> assertFilled(helper, MiningBlocks.FARMING_PORTAL.get()));
    }

    /** A plain cobblestone frame stays unlit; the fire just burns. */
    @GameTest(template = EMPTY)
    public static void otherFrameStaysUnlit(GameTestHelper helper) {
        buildFrame(helper, Blocks.COBBLESTONE);
        helper.setBlock(INSIDE, Blocks.FIRE);
        helper.runAfterDelay(5, () -> {
            helper.assertBlockNotPresent(MiningBlocks.FARMING_PORTAL.get(), INSIDE);
            helper.assertBlockNotPresent(MiningBlocks.MINING_PORTAL.get(), INSIDE);
            helper.succeed();
        });
    }

    /** Breaking a frame block makes the farming portal collapse. */
    @GameTest(template = EMPTY)
    public static void farmingPortalCollapsesWithFrame(GameTestHelper helper) {
        buildFrame(helper, Blocks.MOSSY_COBBLESTONE);
        helper.setBlock(INSIDE, Blocks.FIRE);
        helper.runAfterDelay(2, () -> {
            assertFilled(helper, MiningBlocks.FARMING_PORTAL.get());
            helper.setBlock(FRAME.offset(0, 2, 0), Blocks.AIR);
            helper.succeedWhen(() -> {
                for (int x = 1; x <= 2; x++) {
                    for (int y = 1; y <= 3; y++) {
                        helper.assertBlockPresent(Blocks.AIR, FRAME.offset(x, y, 0));
                    }
                }
            });
        });
    }

    /**
     * Farming dimension (spec 53 §2): the game test server only loads the overworld (its level stems are its own), so
     * the dimension type and biome are checked in the worldgen registries and the noise column is generated directly
     * from the noise settings – solid up to y 64, air above (dirt/grass come from the surface rules and are checked
     * in the client).
     */
    @GameTest(template = EMPTY)
    public static void farmingDimensionIsFlat(GameTestHelper helper) {
        RegistryAccess registries = helper.getLevel().registryAccess();
        DimensionType type = registries.registryOrThrow(Registries.DIMENSION_TYPE).get(FarmingDimension.TYPE);
        Biome biome = registries.registryOrThrow(Registries.BIOME).get(FarmingDimension.BIOME);
        if (type == null || biome == null) {
            helper.fail("farming dimension type or biome missing");
            return;
        }
        helper.assertTrue(type.fixedTime().orElse(-1) == 6000L, "fixed noon");
        helper.assertTrue(type.minY() == 0 && type.height() == 256, "height 0-256");
        helper.assertTrue(type.bedWorks(), "beds work");
        helper.assertFalse(biome.hasPrecipitation(), "no rain");
        helper.assertTrue(Arrays.stream(MobCategory.values())
                .allMatch(category -> biome.getMobSettings().getMobs(category).isEmpty()), "no mob spawns");
        NoiseBasedChunkGenerator generator = new NoiseBasedChunkGenerator(
                new FixedBiomeSource(registries.registryOrThrow(Registries.BIOME).getHolderOrThrow(FarmingDimension.BIOME)),
                registries.registryOrThrow(Registries.NOISE_SETTINGS).getHolderOrThrow(FarmingDimension.NOISE_SETTINGS));
        RandomState random = RandomState.create(generator.generatorSettings().value(),
                registries.lookupOrThrow(Registries.NOISE), 1234L);
        LevelHeightAccessor height = LevelHeightAccessor.create(type.minY(), type.height());
        for (int[] column : new int[][]{{0, 0}, {1000, -1000}, {-12345, 6789}}) {
            NoiseColumn noise = generator.getBaseColumn(column[0], column[1], height, random);
            for (int y = 1; y <= FarmingDimension.SURFACE_Y; y++) {
                helper.assertTrue(noise.getBlock(y).is(Blocks.STONE), "solid at y " + y);
            }
            for (int y = FarmingDimension.SURFACE_Y + 1; y < 256; y++) {
                helper.assertTrue(noise.getBlock(y).isAir(), "air at y " + y);
            }
        }
        helper.succeed();
    }
}
