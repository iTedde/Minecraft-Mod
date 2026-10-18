package dev.craft.ic2.generator;

import dev.craft.Craft;
import dev.craft.core.energy.OutputReadout.OutputReading;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.machine.ElectricMachineBlockEntity;
import dev.craft.ic2.machine.RecipeMachineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Look-at overlay: the output meter of a generator and the consumption meter of a machine (one-second averages). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class OutputReadoutGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos POS = new BlockPos(3, 2, 3);

    private OutputReadoutGameTests() {
    }

    private static GeneratorBlockEntity generator(GameTestHelper helper) {
        helper.setBlock(POS, Ic2Blocks.machine(Ic2Machine.GENERATOR).get());
        return helper.getBlockEntity(POS);
    }

    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void burningGeneratorReadsItsProduction(GameTestHelper helper) {
        GeneratorBlockEntity generator = generator(helper);
        generator.getInventory().set(1, new ItemStack(Items.COAL));
        helper.runAfterDelay(50, () -> {
            OutputReading reading = generator.getOutputReading();
            helper.assertTrue(reading.unit() == OutputReading.Unit.EU, "unit EU/t");
            helper.assertTrue(Math.abs(reading.rate() - generator.getProduction()) < 1.0E-6,
                    "rate " + reading.rate() + " != production " + generator.getProduction());
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void runningMaceratorReadsItsConsumption(GameTestHelper helper) {
        helper.setBlock(POS, Ic2Blocks.machine(Ic2Machine.MACERATOR).get());
        RecipeMachineBlockEntity macerator = helper.getBlockEntity(POS);
        macerator.getInventory().set(ElectricMachineBlockEntity.SLOT_INPUT, new ItemStack(Items.IRON_ORE));
        // one redstone = one whole operation, so the macerator runs on its battery slot without any cable
        macerator.getInventory().set(ElectricMachineBlockEntity.SLOT_BATTERY, new ItemStack(Items.REDSTONE));
        helper.runAfterDelay(50, () -> {
            OutputReading reading = macerator.getConsumptionReading();
            helper.assertTrue(reading.unit() == OutputReading.Unit.EU, "unit EU/t");
            helper.assertTrue(Math.abs(reading.rate() - macerator.getEnergyConsume()) < 1.0E-6,
                    "rate " + reading.rate() + " != consumption " + macerator.getEnergyConsume());
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void idleMaceratorReadsZero(GameTestHelper helper) {
        helper.setBlock(POS, Ic2Blocks.machine(Ic2Machine.MACERATOR).get());
        RecipeMachineBlockEntity macerator = helper.getBlockEntity(POS);
        macerator.getInventory().set(ElectricMachineBlockEntity.SLOT_BATTERY, new ItemStack(Items.REDSTONE));
        helper.runAfterDelay(50, () -> {
            helper.assertTrue(macerator.getConsumptionReading().rate() == 0.0, "idle macerator uses nothing");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void idleGeneratorReadsZero(GameTestHelper helper) {
        GeneratorBlockEntity generator = generator(helper);
        helper.runAfterDelay(50, () -> {
            helper.assertTrue(generator.getOutputReading().rate() == 0.0, "idle generator produces nothing");
            helper.succeed();
        });
    }
}
