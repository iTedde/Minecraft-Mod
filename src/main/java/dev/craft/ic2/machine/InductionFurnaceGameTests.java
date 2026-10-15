package dev.craft.ic2.machine;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Machine;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for the IC2 induction furnace (phase 6b.3a). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class InductionFurnaceGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos POS = new BlockPos(2, 1, 2);

    private InductionFurnaceGameTests() {
    }

    private static InductionFurnaceBlockEntity place(GameTestHelper helper) {
        helper.setBlock(POS, Ic2Blocks.machine(Ic2Machine.INDUCTION_FURNACE).get());
        return helper.getBlockEntity(POS);
    }

    /** At 9000 heat the progress grows by 300 per tick: both slot pairs smelt together after 14 ticks. */
    @GameTest(template = EMPTY, timeoutTicks = 30)
    public static void hotFurnaceSmeltsBothInputs(GameTestHelper helper) {
        InductionFurnaceBlockEntity furnace = place(helper);
        furnace.setEnergyForTest(10000);
        furnace.setHeatForTest(9000);
        furnace.getInventory().set(InductionFurnaceBlockEntity.SLOT_INPUT_1, new ItemStack(Items.RAW_IRON, 2));
        furnace.getInventory().set(InductionFurnaceBlockEntity.SLOT_INPUT_2, new ItemStack(Items.SAND));
        helper.succeedWhen(() -> {
            helper.assertTrue(furnace.getInventory().get(InductionFurnaceBlockEntity.SLOT_OUTPUT_1)
                    .is(Items.IRON_INGOT), "iron ingot");
            helper.assertTrue(furnace.getInventory().get(InductionFurnaceBlockEntity.SLOT_OUTPUT_2)
                    .is(Items.GLASS), "glass");
            helper.assertValueEqual(furnace.getInventory().get(InductionFurnaceBlockEntity.SLOT_INPUT_1).getCount(),
                    1, "raw iron left");
        });
    }

    /** A redstone signal heats the empty furnace at 1 EU per tick. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void redstoneSignalKeepsHeating(GameTestHelper helper) {
        InductionFurnaceBlockEntity furnace = place(helper);
        furnace.setEnergyForTest(1000);
        helper.setBlock(POS.east(), Blocks.REDSTONE_BLOCK);
        helper.runAfterDelay(10, () -> {
            helper.assertValueEqual(furnace.getHeat() + furnace.getEnergy(), 1000, "heat + energy");
            helper.assertTrue(furnace.getHeat() >= 9, "heated");
            helper.succeed();
        });
    }

    /** Without work, signal or energy the heat drops by 4 per tick. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void idleFurnaceCoolsDown(GameTestHelper helper) {
        InductionFurnaceBlockEntity furnace = place(helper);
        furnace.setHeatForTest(10);
        helper.runAfterDelay(5, () -> {
            helper.assertValueEqual(furnace.getHeat(), 0, "heat");
            helper.succeed();
        });
    }

    /** Redstone in the battery slot adds the full capacity of 10 000 EU. */
    @GameTest(template = EMPTY, timeoutTicks = 10)
    public static void redstoneDustAddsFullCapacity(GameTestHelper helper) {
        InductionFurnaceBlockEntity furnace = place(helper);
        furnace.setEnergyForTest(5000);
        furnace.getInventory().set(InductionFurnaceBlockEntity.SLOT_BATTERY, new ItemStack(Items.REDSTONE));
        helper.succeedWhen(() -> {
            helper.assertTrue(furnace.getInventory().get(InductionFurnaceBlockEntity.SLOT_BATTERY).isEmpty(),
                    "redstone used");
            helper.assertValueEqual(furnace.getEnergy(), 15000, "energy");
        });
    }
}
