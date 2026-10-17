package dev.craft.powerplants.waterturbine;

import java.util.ArrayList;
import java.util.List;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.machine.MachineBlock;
import dev.craft.ic2.storage.ElectricStorageBlockEntity;
import dev.craft.powerplants.PpBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Tests for the water turbine (phase 9.4, spec 14 §5). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class WaterTurbineGameTests {
    private static final String EMPTY = "empty_8x4x8";
    /** Cube x 2–4, y 1–3, z 2–4; controller in the middle of the north face, facing north. */
    private static final BlockPos CONTROLLER = new BlockPos(3, 2, 2);
    private static final BlockPos CONSUMER = new BlockPos(3, 2, 1);

    private WaterTurbineGameTests() {
    }

    private static WaterTurbineBlockEntity build(GameTestHelper helper) {
        for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(2, 1, 2), new BlockPos(4, 3, 4))) {
            if (!pos.equals(CONTROLLER)) {
                helper.setBlock(pos, PpBlocks.WATER_TURBINE_CASING.get());
            }
        }
        helper.setBlock(CONTROLLER, PpBlocks.WATER_TURBINE.get().defaultBlockState()
                .setValue(MachineBlock.FACING, Direction.NORTH));
        return helper.getBlockEntity(CONTROLLER);
    }

    /** Ring positions (7×7×3 around the cube) except the consumer in front of the controller. */
    private static List<BlockPos> ring() {
        List<BlockPos> ring = new ArrayList<>();
        for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(0, 1, 0), new BlockPos(6, 3, 6))) {
            boolean cube = pos.getX() >= 2 && pos.getX() <= 4 && pos.getZ() >= 2 && pos.getZ() <= 4;
            if (!cube && !pos.equals(CONSUMER)) {
                ring.add(pos.immutable());
            }
        }
        return ring;
    }

    /** Places flowing water (no sources) and recounts at once, before the water drains away. */
    private static void flood(GameTestHelper helper, WaterTurbineBlockEntity turbine, int flowing, int sources) {
        List<BlockPos> ring = ring();
        for (int i = 0; i < flowing + sources; i++) {
            helper.setBlock(ring.get(i), i < flowing
                    ? Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 1)
                    : Blocks.WATER.defaultBlockState());
        }
        turbine.updateWaterCount();
    }

    /** 0.25 EU/t per flowing block from 16 blocks on, × 1.5 in rivers, at most 32 EU/t. */
    @GameTest(template = EMPTY)
    public static void outputThresholdAndCap(GameTestHelper helper) {
        helper.assertValueEqual(WaterTurbineBlockEntity.output(15, true), 0.0, "below 16");
        helper.assertValueEqual(WaterTurbineBlockEntity.output(16, false), 4.0, "16 flowing");
        helper.assertValueEqual(WaterTurbineBlockEntity.output(100, false), 25.0, "100 flowing");
        helper.assertValueEqual(WaterTurbineBlockEntity.output(40, true), 15.0, "river");
        helper.assertValueEqual(WaterTurbineBlockEntity.output(120, false), 30.0, "full ring");
        helper.assertValueEqual(WaterTurbineBlockEntity.output(120, true), 32.0, "cap");
        helper.assertValueEqual(ring().size() + 1, 120, "ring size");
        helper.succeed();
    }

    /** Only flowing water counts: 15 flowing + sources give nothing. */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void sourcesDoNotCount(GameTestHelper helper) {
        WaterTurbineBlockEntity turbine = build(helper);
        helper.runAtTickTime(5, () -> {
            helper.assertTrue(turbine.isFormed(), "structure formed");
            flood(helper, turbine, 15, 10);
            helper.assertValueEqual(turbine.getFlowing(), 15, "flowing");
            helper.assertValueEqual(turbine.getFlow(), 0.0, "EU/t");
        });
        helper.runAtTickTime(20, () -> {
            helper.assertValueEqual(turbine.getBuffer(), 0.0, "no energy");
            helper.succeed();
        });
    }

    /** 16 flowing blocks feed 4 EU/t (6 EU/t in a river) into an MFE in front of the controller. */
    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void sixteenFlowingBlocksFeedTheNet(GameTestHelper helper) {
        WaterTurbineBlockEntity turbine = build(helper);
        helper.setBlock(CONSUMER, Ic2Blocks.machine(Ic2Machine.MFE).get());
        ElectricStorageBlockEntity mfe = helper.getBlockEntity(CONSUMER);
        int[] start = new int[1];
        helper.runAtTickTime(5, () -> {
            flood(helper, turbine, 16, 0);
            helper.assertValueEqual(turbine.getFlowing(), 16, "flowing");
            helper.assertValueEqual(turbine.getFlow(), WaterTurbineBlockEntity.output(16, turbine.isInRiver()),
                    "EU/t");
            start[0] = mfe.getEnergy();
        });
        helper.runAtTickTime(35, () -> {
            helper.assertValueEqual(mfe.getEnergy() - start[0], (int) (30 * turbine.getFlow()), "energy delivered");
            helper.setBlock(CONSUMER, Blocks.AIR);
            helper.succeed();
        });
    }

    /** Every casing block conducts: an MFE beside a side casing gets the same energy as one at the controller. */
    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void casingDeliversEnergy(GameTestHelper helper) {
        BlockPos beside = new BlockPos(5, 2, 4);
        WaterTurbineBlockEntity turbine = build(helper);
        helper.setBlock(beside, Ic2Blocks.machine(Ic2Machine.MFE).get());
        ElectricStorageBlockEntity mfe = helper.getBlockEntity(beside);
        int[] start = new int[1];
        helper.runAtTickTime(5, () -> {
            flood(helper, turbine, 16, 0);
            start[0] = mfe.getEnergy();
        });
        helper.runAtTickTime(35, () -> {
            helper.assertTrue(turbine.getFlow() > 0, "turbine runs");
            helper.assertValueEqual(mfe.getEnergy() - start[0], (int) (30 * turbine.getFlow()),
                    "energy through the casing");
            helper.setBlock(beside, Blocks.AIR);
            helper.succeed();
        });
    }

    /** A missing casing breaks the structure: no energy despite water. */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void missingCasingStopsTheTurbine(GameTestHelper helper) {
        WaterTurbineBlockEntity turbine = build(helper);
        helper.setBlock(new BlockPos(3, 2, 3), Blocks.AIR);
        helper.runAtTickTime(5, () -> flood(helper, turbine, 30, 0));
        helper.runAtTickTime(20, () -> {
            helper.assertFalse(turbine.isFormed(), "not formed");
            helper.assertValueEqual(turbine.getBuffer(), 0.0, "no energy");
            helper.assertFalse(turbine.getBlockState().getValue(MachineBlock.ACTIVE), "inactive");
            helper.succeed();
        });
    }
}
