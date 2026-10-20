package dev.craft.compactsolars;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.generator.SolarPanelBlockEntity;
import dev.craft.ic2.storage.ElectricStorageBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * In-world tests for the 3×3 solar array multiblocks (user, 2026-10-20). Arrays are built on the floor (y 1);
 * storage blocks sit below the centre so they do not shade it.
 */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class CompactSolarsGameTests {
    private static final String EMPTY = "empty_16x8x16";
    private static final String BATCH = "compact_solars";

    private CompactSolarsGameTests() {
    }

    private static Block panel() {
        return Ic2Blocks.machine(Ic2Machine.SOLAR_PANEL).get();
    }

    private static Block array(SolarArrayType type) {
        return CsBlocks.array(type).get();
    }

    /** Places an array of {@code type} at {@code centre} (relative) and fills its ring with {@code ring}. */
    private static SolarArrayBlockEntity build(GameTestHelper helper, SolarArrayType type, BlockPos centre,
                                               Block ring) {
        for (int x = -1; x <= 1; x++) {
            for (int z = -1; z <= 1; z++) {
                helper.setBlock(centre.offset(x, 0, z), x == 0 && z == 0 ? array(type) : ring);
            }
        }
        return helper.getBlockEntity(centre);
    }

    /** Clear noon, so parts with open sky see the sun. */
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
        SolarArrayBlockEntity array = build(helper, SolarArrayType.LV, centre, panel());
        helper.succeedWhen(() -> {
            helper.assertTrue(array.isFormed(), "LV array not formed");
            SolarPanelBlockEntity panel = helper.getBlockEntity(centre.east());
            helper.assertValueEqual(panel.getArrayLease().getOwner(now(helper)), array.getBlockPos(), "panel owner");
        });
    }

    @GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 80)
    public static void lvArrayDeliversItsPanelsWithBonus(GameTestHelper helper) {
        sunshine(helper);
        BlockPos centre = new BlockPos(3, 1, 3);
        SolarArrayBlockEntity array = build(helper, SolarArrayType.LV, centre, panel());
        // A loose panel keeps producing itself (storage 1 without a consumer); leased panels produce nothing.
        BlockPos loose = new BlockPos(10, 1, 10);
        helper.setBlock(loose, panel());
        BlockPos batboxPos = centre.below();
        helper.setBlock(batboxPos, Ic2Blocks.machine(Ic2Machine.BATBOX).get());
        helper.runAfterDelay(1, () -> {
            refreshPanels(helper, centre, 1);
            refreshPanels(helper, loose, 0);
        });
        helper.runAfterDelay(30, () -> {
            helper.assertValueEqual(array.getSunnyValue(), 16, "sunny value");
            helper.assertTrue(array.isDelivering(), "array not delivering");
            SolarPanelBlockEntity leased = helper.getBlockEntity(centre.north());
            helper.assertValueEqual(leased.getStorage(), 0, "leased panel storage");
            SolarPanelBlockEntity free = helper.getBlockEntity(loose);
            helper.assertValueEqual(free.getStorage(), 1, "loose panel storage");
            ElectricStorageBlockEntity batbox = helper.getBlockEntity(batboxPos);
            int before = batbox.getEnergy();
            // (8 + 8) · 1.2 = 19 EU/t in packets of 8
            helper.runAfterDelay(10, () -> {
                helper.assertValueEqual(batbox.getEnergy() - before, 190, "BatBox gain over 10 ticks");
                helper.succeed();
            });
        });
    }

    @GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 80)
    public static void mvArrayLeasesLvArraysOfItsRing(GameTestHelper helper) {
        sunshine(helper);
        BlockPos centre = new BlockPos(5, 1, 5);
        SolarArrayBlockEntity mv = build(helper, SolarArrayType.MV, centre, array(SolarArrayType.LV));
        BlockPos mfePos = centre.below();
        helper.setBlock(mfePos, Ic2Blocks.machine(Ic2Machine.MFE).get());
        helper.runAfterDelay(50, () -> {
            helper.assertTrue(mv.isFormed(), "MV array not formed");
            helper.assertValueEqual(mv.getSunnyValue(), 128, "sunny value");
            helper.assertTrue(mv.isDelivering(), "MV array not delivering");
            SolarArrayBlockEntity lv = helper.getBlockEntity(centre.offset(-1, 0, -1));
            helper.assertValueEqual(lv.getLease().getOwner(now(helper)), mv.getBlockPos(), "LV array owner");
            helper.assertFalse(lv.isDelivering(), "LV array in the ring delivers");
            ElectricStorageBlockEntity mfe = helper.getBlockEntity(mfePos);
            int before = mfe.getEnergy();
            // (64 + 8 · 8) · 1.4 = 179 EU/t
            helper.runAfterDelay(10, () -> {
                helper.assertValueEqual(mfe.getEnergy() - before, 1790, "MFE gain over 10 ticks");
                helper.succeed();
            });
        });
    }

    @GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 80)
    public static void hvArrayWithMvRingDeliversInHvPackets(GameTestHelper helper) {
        sunshine(helper);
        BlockPos centre = new BlockPos(5, 1, 5);
        SolarArrayBlockEntity hv = build(helper, SolarArrayType.HV, centre, array(SolarArrayType.MV));
        BlockPos mfsuPos = centre.below();
        helper.setBlock(mfsuPos, Ic2Blocks.machine(Ic2Machine.MFSU).get());
        helper.runAfterDelay(50, () -> {
            helper.assertTrue(hv.isFormed(), "HV array not formed");
            helper.assertValueEqual(hv.getSunnyValue(), 1024, "sunny value");
            ElectricStorageBlockEntity mfsu = helper.getBlockEntity(mfsuPos);
            int before = mfsu.getEnergy();
            // (512 + 8 · 64) · 1.5 = 1536 EU/t in three packets of 512
            helper.runAfterDelay(10, () -> {
                helper.assertValueEqual(mfsu.getEnergy() - before, 15360, "MFSU gain over 10 ticks");
                helper.succeed();
            });
        });
    }

    @GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 80)
    public static void ringMixesPanelsAndArraysUpToTheCentresTier(GameTestHelper helper) {
        sunshine(helper);
        BlockPos centre = new BlockPos(5, 1, 5);
        SolarArrayBlockEntity mv = build(helper, SolarArrayType.MV, centre, panel());
        helper.setBlock(centre.north(), array(SolarArrayType.MV));
        helper.setBlock(centre.south(), array(SolarArrayType.MV));
        helper.setBlock(centre.east(), array(SolarArrayType.LV));
        helper.setBlock(centre.west(), array(SolarArrayType.LV));
        helper.runAfterDelay(1, () -> refreshPanels(helper, centre, 1));
        helper.runAfterDelay(50, () -> {
            helper.assertTrue(mv.isFormed(), "MV array not formed");
            // 64 + 2 · 64 + 2 · 8 + 4 · 1
            helper.assertValueEqual(mv.getSunnyValue(), 212, "sunny value");
            helper.setBlock(centre.east(), array(SolarArrayType.HV));
            helper.runAfterDelay(SolarArrayBlockEntity.CHECK_INTERVAL + 1, () -> {
                helper.assertFalse(mv.isFormed(), "MV array formed with an HV array in its ring");
                helper.assertFalse(mv.isDelivering(), "MV array delivers");
                helper.succeed();
            });
        });
    }

    @GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 80)
    public static void shadedPartsCountNothing(GameTestHelper helper) {
        sunshine(helper);
        BlockPos centre = new BlockPos(5, 1, 5);
        SolarArrayBlockEntity mv = build(helper, SolarArrayType.MV, centre, array(SolarArrayType.LV));
        helper.setBlock(centre.above(), Blocks.STONE);
        helper.setBlock(centre.north().above(), Blocks.STONE);
        helper.runAfterDelay(50, () -> {
            helper.assertValueEqual(mv.getSunnyValue(), 7 * 8, "sunny value");
            helper.succeed();
        });
    }

    /** As built in survival: an MV array runs on its own until it ends up in the ring of an HV array. */
    @GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 140)
    public static void hvArrayTakesOverARunningMvArray(GameTestHelper helper) {
        sunshine(helper);
        BlockPos centre = new BlockPos(5, 1, 5);
        BlockPos mvPos = centre.east();
        SolarArrayBlockEntity mv = build(helper, SolarArrayType.MV, mvPos, panel());
        for (int z = -1; z <= 1; z++) {
            helper.setBlock(centre.offset(-1, 0, z), panel());
        }
        helper.runAfterDelay(1, () -> refreshPanels(helper, centre, 2));
        helper.runAfterDelay(40, () -> {
            helper.assertTrue(mv.isDelivering(), "MV array not delivering on its own");
            helper.setBlock(centre, array(SolarArrayType.HV));
            helper.runAfterDelay(50, () -> {
                SolarArrayBlockEntity hv = helper.getBlockEntity(centre);
                helper.assertTrue(hv.isFormed(), "HV array not formed");
                // 512 + 64 + 7 panels, four of them taken over from the MV array
                helper.assertValueEqual(hv.getSunnyValue(), 583, "sunny value");
                helper.assertTrue(hv.isDelivering(), "HV array not delivering");
                helper.assertValueEqual(mv.getLease().getOwner(now(helper)), hv.getBlockPos(), "MV array owner");
                helper.assertFalse(mv.isDelivering(), "MV array in the ring delivers");
                helper.succeed();
            });
        });
    }

    @GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 120)
    public static void brokenArrayReleasesItsPanels(GameTestHelper helper) {
        BlockPos centre = new BlockPos(3, 1, 3);
        SolarArrayBlockEntity array = build(helper, SolarArrayType.LV, centre, panel());
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

    /** Only the 9 blocks of one field work together: a field sharing a block with another one delivers nothing. */
    @GameTest(template = EMPTY, batch = BATCH, timeoutTicks = 160)
    public static void fieldSharingABlockWithAnotherDeliversNothing(GameTestHelper helper) {
        sunshine(helper);
        BlockPos first = new BlockPos(3, 1, 3);
        SolarArrayBlockEntity a = build(helper, SolarArrayType.LV, first, panel());
        helper.runAfterDelay(25, () -> {
            // Second array two blocks east: shares the east column of the first one.
            SolarArrayBlockEntity b = build(helper, SolarArrayType.LV, first.east(2), panel());
            refreshPanels(helper, first, 3);
            helper.runAfterDelay(25, () -> {
                helper.assertValueEqual(a.getSunnyValue(), 16, "first field");
                helper.assertValueEqual(b.getSunnyValue(), 0, "second field");
                helper.assertFalse(b.isDelivering(), "second field delivers");
                SolarPanelBlockEntity own = helper.getBlockEntity(first.east(3));
                helper.assertFalse(own.getArrayLease().isHeld(now(helper)), "blocked field holds its own panels");
                // Without the first field the shared panels lapse and the second one takes over.
                helper.setBlock(first, Blocks.AIR);
                helper.runAfterDelay(SolarArrayBlockEntity.LEASE_DURATION + SolarArrayBlockEntity.CHECK_INTERVAL + 1,
                        () -> {
                            helper.assertValueEqual(b.getSunnyValue(), 16, "second field after the first is gone");
                            helper.succeed();
                        });
            });
        });
    }
}
