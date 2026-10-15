package dev.craft.ic2.storage;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.machine.MachineBlock;
import dev.craft.ic2.machine.MachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for MFE, MFSU and the transformers (phase 6.1). Placed blocks face north. */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class StorageGameTests {
    private static final String EMPTY = "empty_8x4x8";

    private StorageGameTests() {
    }

    private static <T extends MachineBlockEntity> T place(GameTestHelper helper, BlockPos pos, Ic2Machine machine) {
        helper.setBlock(pos, Ic2Blocks.machine(machine).get());
        return helper.getBlockEntity(pos);
    }

    @GameTest(template = EMPTY)
    public static void storageValues(GameTestHelper helper) {
        ElectricStorageBlockEntity mfe = place(helper, new BlockPos(1, 1, 1), Ic2Machine.MFE);
        ElectricStorageBlockEntity mfsu = place(helper, new BlockPos(3, 1, 1), Ic2Machine.MFSU);
        helper.assertValueEqual(mfe.getMaxStorage(), 600000, "MFE capacity");
        helper.assertValueEqual(mfe.getMaxSafeInput(), 128, "MFE input");
        helper.assertValueEqual(mfsu.getMaxStorage(), 10000000, "MFSU capacity");
        helper.assertValueEqual(mfsu.getTier(), 3, "MFSU tier");
        // Fills up to maxStorage + output − 1.
        for (int i = 0; i < 4; i++) {
            mfe.injectEnergy(Direction.EAST, 128);
        }
        helper.assertValueEqual(mfe.demandsEnergy(), 600000 - 512, "MFE demand");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void mfeExplodesOnHvPacket(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        ElectricStorageBlockEntity mfe = place(helper, pos, Ic2Machine.MFE);
        helper.runAfterDelay(1, () -> {
            mfe.injectEnergy(Direction.EAST, 512);
            helper.assertBlockNotPresent(Ic2Blocks.machine(Ic2Machine.MFE).get(), pos);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void transformerStepsDownToFourPackets(GameTestHelper helper) {
        TransformerBlockEntity transformer = place(helper, new BlockPos(2, 1, 2), Ic2Machine.MV_TRANSFORMER);
        ElectricStorageBlockEntity mfe = place(helper, new BlockPos(3, 1, 2), Ic2Machine.MFE);
        helper.runAfterDelay(1, () -> {
            // Input on the front with up to 512 EU per packet; buffer fills to 1024 + 512 − 1.
            helper.assertValueEqual(transformer.injectEnergy(Direction.NORTH, 512), 0, "accepted");
            helper.assertValueEqual(transformer.injectEnergy(Direction.NORTH, 512), 0, "accepted");
            helper.assertValueEqual(transformer.getEnergy(), 1024, "buffer");
            helper.runAfterDelay(1, () -> {
                // One tick: four packets of 128 EU into the MFE, the rest stays in the buffer.
                helper.assertValueEqual(mfe.getEnergy(), 512, "MFE energy after one tick");
                helper.assertValueEqual(transformer.getEnergy(), 512, "transformer buffer");
                helper.assertBlockPresent(Ic2Blocks.machine(Ic2Machine.MFE).get(), new BlockPos(3, 1, 2));
                helper.succeed();
            });
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void transformerStepsUpWithRedstone(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        TransformerBlockEntity transformer = place(helper, pos, Ic2Machine.LV_TRANSFORMER);
        ElectricStorageBlockEntity mfe = place(helper, pos.north(), Ic2Machine.MFE);
        helper.setBlock(pos.east(), Blocks.REDSTONE_BLOCK);
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(transformer.isStepUp(), "step-up mode");
            helper.assertBlockProperty(pos, MachineBlock.ACTIVE, true);
            helper.assertValueEqual(transformer.getMaxSafeInput(), 32, "input limited to low voltage");
            for (int i = 0; i < 5; i++) {
                transformer.injectEnergy(Direction.EAST, 32);
            }
            helper.runAfterDelay(1, () -> {
                // One 128 EU packet out of the front, 32 EU remain.
                helper.assertValueEqual(mfe.getEnergy(), 128, "MFE energy");
                helper.assertValueEqual(transformer.getEnergy(), 32, "transformer buffer");
                helper.succeed();
            });
        });
    }

    @GameTest(template = EMPTY)
    public static void transformerOvervoltage(GameTestHelper helper) {
        BlockPos lv = new BlockPos(1, 1, 2);
        BlockPos hv = new BlockPos(5, 1, 2);
        TransformerBlockEntity lvTransformer = place(helper, lv, Ic2Machine.LV_TRANSFORMER);
        TransformerBlockEntity hvTransformer = place(helper, hv, Ic2Machine.HV_TRANSFORMER);
        helper.runAfterDelay(1, () -> {
            // The HV transformer accepts any packet in step-down mode.
            helper.assertValueEqual(hvTransformer.getMaxSafeInput(), Integer.MAX_VALUE, "HV input unlimited");
            hvTransformer.injectEnergy(Direction.NORTH, 8192);
            helper.assertBlockPresent(Ic2Blocks.machine(Ic2Machine.HV_TRANSFORMER).get(), hv);
            lvTransformer.injectEnergy(Direction.NORTH, 512);
            helper.assertBlockNotPresent(Ic2Blocks.machine(Ic2Machine.LV_TRANSFORMER).get(), lv);
            helper.succeed();
        });
    }
}
