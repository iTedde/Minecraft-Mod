package dev.craft.compactsolars;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.generator.SolarPanelBlockEntity;
import dev.craft.ic2.storage.ElectricStorageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for the solar array multiblocks (phase 6.4b). Arrays are built on the floor (y 1). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class CompactSolarsGameTests {
    private static final String EMPTY = "empty_16x8x16";
    private static final String EMPTY_LARGE = "empty_32x8x32";
    private static final String BATCH = "compact_solars";

    private CompactSolarsGameTests() {
    }

    /** Builds an array of {@code type} with its controller at {@code centre} (relative). */
    private static SolarArrayBlockEntity build(GameTestHelper helper, SolarArrayType type, BlockPos centre) {
        String[] rows = SolarArrayPatterns.layout(type);
        int radius = type.getRadius();
        for (int z = 0; z < rows.length; z++) {
            for (int x = 0; x < rows[z].length(); x++) {
                BlockPos pos = centre.offset(x - radius, 0, z - radius);
                char symbol = rows[z].charAt(x);
                if (symbol == SolarArrayPatterns.PANEL) {
                    helper.setBlock(pos, Ic2Blocks.machine(Ic2Machine.SOLAR_PANEL).get());
                }
                for (SolarArrayType tier : SolarArrayType.values()) {
                    if (tier.getSymbol() == symbol) {
                        helper.setBlock(pos, CsBlocks.array(tier).get());
                    }
                }
            }
        }
        return helper.getBlockEntity(centre);
    }

    /** Clear noon, so panels with open sky see the sun. */
    private static void sunshine(GameTestHelper helper) {
        helper.getLevel().setDayTime(6000);
        helper.getLevel().setWeatherParameters(24000, 0, false, false);
    }

    /** Re-runs the sun check of all panels within {@code radius} of {@code centre}. */
    private static void refreshPanels(GameTestHelper helper, BlockPos centre, int radius) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                if (helper.getLevel().getBlockEntity(helper.absolutePos(centre.offset(x, 0, z)))
                        instanceof SolarPanelBlockEntity panel) {
                    panel.updateSunVisibility();
                }
            }
        }
    }

    private static long now(GameTestHelper helper) {
        return helper.getLevel().getGameTime();
    }

    @GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 60)
    public static void lvArrayFormsAndLeasesPanels(GameTestHelper helper) {
        BlockPos centre = new BlockPos(3, 1, 3);
        SolarArrayBlockEntity array = build(helper, SolarArrayType.LV, centre);
        helper.succeedWhen(() -> {
            helper.assertTrue(array.isFormed(), "LV array not formed");
            SolarPanelBlockEntity panel = helper.getBlockEntity(centre.east());
            helper.assertValueEqual(panel.getArrayLease().getOwner(now(helper)), array.getBlockPos(), "panel owner");
        });
    }

    @GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 80)
    public static void lvArrayDeliversOneEuPerSunnyPanel(GameTestHelper helper) {
        sunshine(helper);
        BlockPos centre = new BlockPos(3, 1, 3);
        SolarArrayBlockEntity array = build(helper, SolarArrayType.LV, centre);
        // A loose panel keeps producing itself (storage 1 without a consumer); leased panels produce nothing.
        BlockPos loose = new BlockPos(10, 1, 10);
        helper.setBlock(loose, Ic2Blocks.machine(Ic2Machine.SOLAR_PANEL).get());
        BlockPos batboxPos = centre.above();
        helper.setBlock(batboxPos, Ic2Blocks.machine(Ic2Machine.BATBOX).get());
        helper.runAfterDelay(1, () -> {
            refreshPanels(helper, centre, 1);
            refreshPanels(helper, loose, 0);
        });
        helper.runAfterDelay(30, () -> {
            helper.assertValueEqual(array.getSunnyPanels(), 8, "sunny panels");
            helper.assertTrue(array.isDelivering(), "array not delivering");
            SolarPanelBlockEntity leased = helper.getBlockEntity(centre.north());
            helper.assertValueEqual(leased.getStorage(), 0, "leased panel storage");
            SolarPanelBlockEntity free = helper.getBlockEntity(loose);
            helper.assertValueEqual(free.getStorage(), 1, "loose panel storage");
            ElectricStorageBlockEntity batbox = helper.getBlockEntity(batboxPos);
            int before = batbox.getEnergy();
            helper.runAfterDelay(10, () -> {
                helper.assertValueEqual(batbox.getEnergy() - before, 80, "BatBox gain over 10 ticks");
                helper.succeed();
            });
        });
    }

    @GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 80)
    public static void mvArrayTakesOverLvControllers(GameTestHelper helper) {
        sunshine(helper);
        BlockPos centre = new BlockPos(5, 1, 5);
        SolarArrayBlockEntity mv = build(helper, SolarArrayType.MV, centre);
        helper.runAfterDelay(1, () -> refreshPanels(helper, centre, 4));
        helper.runAfterDelay(50, () -> {
            helper.assertTrue(mv.isFormed(), "MV array not formed");
            helper.assertValueEqual(mv.getSunnyPanels(), 72, "sunny panels");
            helper.assertTrue(mv.isDelivering(), "MV array not delivering");
            SolarArrayBlockEntity lv = helper.getBlockEntity(centre.offset(-3, 0, -3));
            helper.assertValueEqual(lv.getLease().getOwner(now(helper)), mv.getBlockPos(), "LV controller owner");
            helper.assertFalse(lv.isDelivering(), "built-in LV controller delivers");
            SolarPanelBlockEntity panel = helper.getBlockEntity(centre.offset(-3, 0, -2));
            helper.assertValueEqual(panel.getArrayLease().getOwner(now(helper)), mv.getBlockPos(), "panel owner");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY_LARGE, batch = BATCH, timeoutTicks = 100)
    public static void hvArrayTakesOverMvControllers(GameTestHelper helper) {
        sunshine(helper);
        BlockPos centre = new BlockPos(13, 1, 13);
        SolarArrayBlockEntity hv = build(helper, SolarArrayType.HV, centre);
        BlockPos mfsuPos = centre.above();
        helper.setBlock(mfsuPos, Ic2Blocks.machine(Ic2Machine.MFSU).get());
        helper.runAfterDelay(1, () -> refreshPanels(helper, centre, 13));
        helper.runAfterDelay(50, () -> {
            helper.assertTrue(hv.isFormed(), "HV array not formed");
            helper.assertValueEqual(hv.getSunnyPanels(), 648, "sunny panels");
            helper.assertTrue(hv.isDelivering(), "HV array not delivering");
            SolarArrayBlockEntity mv = helper.getBlockEntity(centre.offset(-9, 0, -9));
            helper.assertValueEqual(mv.getLease().getOwner(now(helper)), hv.getBlockPos(), "MV controller owner");
            helper.assertFalse(mv.isDelivering(), "built-in MV controller delivers");
            ElectricStorageBlockEntity mfsu = helper.getBlockEntity(mfsuPos);
            int before = mfsu.getEnergy();
            // 1024 EU/t (2 per sunny panel, capped) in two HV packets
            helper.runAfterDelay(10, () -> {
                helper.assertValueEqual(mfsu.getEnergy() - before, 10240, "MFSU gain over 10 ticks");
                helper.succeed();
            });
        });
    }

    /** As built in survival: the MV arrays run on their own before the HV controller is placed. */
    @GameTest(template = EMPTY_LARGE, batch = BATCH, timeoutTicks = 140)
    public static void hvControllerPlacedIntoRunningMvArrays(GameTestHelper helper) {
        sunshine(helper);
        BlockPos centre = new BlockPos(13, 1, 13);
        build(helper, SolarArrayType.HV, centre);
        helper.setBlock(centre, Blocks.AIR);
        helper.runAfterDelay(1, () -> refreshPanels(helper, centre, 13));
        helper.runAfterDelay(40, () -> {
            SolarArrayBlockEntity mv = helper.getBlockEntity(centre.offset(-9, 0, -9));
            helper.assertTrue(mv.isDelivering(), "MV array not delivering on its own");
            helper.setBlock(centre, CsBlocks.array(SolarArrayType.HV).get());
            helper.runAfterDelay(50, () -> {
                SolarArrayBlockEntity hv = helper.getBlockEntity(centre);
                helper.assertTrue(hv.isFormed(), "HV array not formed");
                helper.assertValueEqual(hv.getSunnyPanels(), 648, "sunny panels");
                helper.assertTrue(hv.isDelivering(), "HV array not delivering");
                helper.assertFalse(mv.isDelivering(), "built-in MV controller delivers");
                helper.succeed();
            });
        });
    }

    @GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 120)
    public static void brokenArrayReleasesItsPanels(GameTestHelper helper) {
        BlockPos centre = new BlockPos(3, 1, 3);
        SolarArrayBlockEntity array = build(helper, SolarArrayType.LV, centre);
        helper.runAfterDelay(25, () -> {
            helper.assertTrue(array.isFormed(), "LV array not formed");
            helper.setBlock(centre.west(), Blocks.AIR);
        });
        helper.runAfterDelay(25 + SolarArrayBlockEntity.CHECK_INTERVAL + SolarArrayBlockEntity.LEASE_DURATION + 1,
                () -> {
                    helper.assertFalse(array.isFormed(), "array still formed");
                    SolarPanelBlockEntity panel = helper.getBlockEntity(centre.east());
                    helper.assertFalse(panel.getArrayLease().isHeld(now(helper)), "panel still leased");
                    helper.succeed();
                });
    }

    @GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 80)
    public static void overlappingArraysCountSharedPanelsOnce(GameTestHelper helper) {
        sunshine(helper);
        BlockPos first = new BlockPos(3, 1, 3);
        SolarArrayBlockEntity a = build(helper, SolarArrayType.LV, first);
        helper.runAfterDelay(25, () -> {
            // Second controller two blocks east: shares the east column of the first array.
            SolarArrayBlockEntity b = build(helper, SolarArrayType.LV, first.east(2));
            refreshPanels(helper, first, 3);
            helper.runAfterDelay(25, () -> {
                helper.assertTrue(a.isFormed() && b.isFormed(), "both arrays formed");
                helper.assertValueEqual(a.getSunnyPanels() + b.getSunnyPanels(), 13, "panels counted once");
                helper.succeed();
            });
        });
    }
}
