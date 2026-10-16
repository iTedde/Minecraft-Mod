package dev.craft.ic2.generator;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Machine;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Tests for the rebalanced water mill and wind mill (phase 9.1, spec 14 §6–7). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class GeneratorBalancingGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos MILL = new BlockPos(3, 2, 3);
    private static final BlockState FLOWING = Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 1);

    private GeneratorBalancingGameTests() {
    }

    private static WaterMillBlockEntity mill(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, Ic2Blocks.machine(Ic2Machine.WATER_MILL).get());
        return helper.getBlockEntity(pos);
    }

    /** Sources yield 0.02 EU/t, flowing water 0.04 EU/t, counted in the 5×5×3 area (not the old 3×3×3 cube). */
    @GameTest(template = EMPTY, timeoutTicks = 5)
    public static void waterMillCountsSourcesAndFlowingWater(GameTestHelper helper) {
        WaterMillBlockEntity mill = mill(helper, MILL);
        helper.setBlock(MILL.offset(2, 0, 0), Blocks.WATER);
        helper.setBlock(MILL.offset(-2, 1, 2), Blocks.WATER);
        helper.setBlock(MILL.offset(0, -1, -2), FLOWING);
        helper.setBlock(MILL.offset(3, 0, 0), Blocks.WATER);
        mill.updateWaterCount();
        helper.assertValueEqual(mill.getWaterBlocks(), 3, "water blocks");
        helper.assertTrue(Math.abs(mill.getFlow() - 8.0) < 1e-9, "flow 2 + 2 + 4 hundredths, got " + mill.getFlow());
        helper.succeed();
    }

    /** A water block inside the areas of two mills counts half for each. */
    @GameTest(template = EMPTY, timeoutTicks = 5)
    public static void overlappingMillsShareWater(GameTestHelper helper) {
        WaterMillBlockEntity first = mill(helper, MILL.west());
        WaterMillBlockEntity second = mill(helper, MILL.east());
        helper.setBlock(MILL, FLOWING);
        first.updateWaterCount();
        second.updateWaterCount();
        helper.assertTrue(Math.abs(first.getFlow() - 2.0) < 1e-9, "first gets half, got " + first.getFlow());
        helper.assertTrue(Math.abs(second.getFlow() - 2.0) < 1e-9, "second gets half, got " + second.getFlow());
        helper.succeed();
    }

    /** 74 flowing blocks would give 2.96 EU/t; the mill is capped at 2 EU/t. */
    @GameTest(template = EMPTY, timeoutTicks = 5)
    public static void waterMillIsCappedAtTwoEuPerTick(GameTestHelper helper) {
        for (BlockPos pos : BlockPos.betweenClosed(MILL.offset(-2, -1, -2), MILL.offset(2, 1, 2))) {
            helper.setBlock(pos, FLOWING);
        }
        WaterMillBlockEntity mill = mill(helper, MILL);
        mill.updateWaterCount();
        helper.assertValueEqual(mill.getWaterBlocks(), 74, "water blocks");
        helper.assertTrue(Math.abs(mill.getFlow() - WaterMillBlockEntity.MAX_YIELD) < 1e-9, "capped flow");
        helper.succeed();
    }

    /** Spec examples: y 128, wind 15 → 3.2; thunder ×3, rain ×1.5; nothing below the obstruction height. */
    @GameTest(template = EMPTY, timeoutTicks = 5)
    public static void windFormulaUsesBalancedFactors(GameTestHelper helper) {
        assertClose(helper, WindMillBlockEntity.subproduction(15, 128, 0, false, false), 3.2, "clear");
        assertClose(helper, WindMillBlockEntity.subproduction(15, 128, 0, true, true), 9.6, "thunder");
        assertClose(helper, WindMillBlockEntity.subproduction(15, 128, 0, false, true), 4.8, "rain");
        assertClose(helper, WindMillBlockEntity.subproduction(15, 200, 0, false, false), 6.8, "y 200");
        helper.assertTrue(WindMillBlockEntity.subproduction(15, 70, 10, false, false) <= 0.0, "obstructed");
        helper.assertValueEqual(WindMillBlockEntity.getOverheatScaled(12.5, 14), 0, "no overheat at threshold");
        helper.assertValueEqual(WindMillBlockEntity.getOverheatScaled(18.75, 14), 7, "half overheat");
        helper.assertValueEqual(WindMillBlockEntity.getOverheatScaled(25.0, 14), 14, "full overheat");
        helper.succeed();
    }

    private static void assertClose(GameTestHelper helper, double actual, double expected, String what) {
        helper.assertTrue(Math.abs(actual - expected) < 1e-9, what + ": expected " + expected + ", got " + actual);
    }
}
