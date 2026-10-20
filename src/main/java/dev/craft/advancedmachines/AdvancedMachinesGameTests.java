package dev.craft.advancedmachines;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for Advanced Machines (spec 25 §3–§4). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class AdvancedMachinesGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos POS = new BlockPos(2, 1, 2);

    private AdvancedMachinesGameTests() {
    }

    private static AdvancedMachineBlockEntity place(GameTestHelper helper, Ic2Machine machine) {
        helper.setBlock(POS, Ic2Blocks.machine(machine).get());
        return helper.getBlockEntity(POS);
    }

    /** At full speed (7500) progress grows by 250 per tick: one ore is done after 16 ticks, at 3 EU/t. */
    @GameTest(template = EMPTY, timeoutTicks = 30)
    public static void fullSpeedMaceratesIn16Ticks(GameTestHelper helper) {
        AdvancedMachineBlockEntity machine = place(helper, Ic2Machine.ROTARY_MACERATOR);
        machine.setEnergy(1000);
        machine.setSpeed(AdvancedMachineBlockEntity.BASE_MAX_SPEED);
        machine.getInventory().set(AdvancedMachineType.SLOT_INPUT, new ItemStack(Items.IRON_ORE));
        helper.runAfterDelay(18, () -> {
            helper.assertTrue(machine.getInventory().get(2).is(Ic2Items.IRON_DUST.get()), "iron dust");
            helper.assertValueEqual(machine.getInventory().get(2).getCount(), 2, "two dust");
            helper.assertTrue(machine.getEnergy() >= 1000 - 3 * 18, "3 EU per tick at full speed");
            helper.succeed();
        });
    }

    /** Spinning up costs 3 EU per tick and adds 1 speed per tick without overclockers. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void spinUpCosts3EuPerTick(GameTestHelper helper) {
        AdvancedMachineBlockEntity machine = place(helper, Ic2Machine.SINGULARITY_COMPRESSOR);
        machine.setEnergy(1000);
        machine.getInventory().set(AdvancedMachineType.SLOT_INPUT, new ItemStack(Ic2Items.COAL_BALL.get()));
        helper.runAfterDelay(10, () -> {
            helper.assertValueEqual(machine.getSpeed() * 3 + machine.getEnergy(), 1000, "speed · 3 + energy");
            helper.assertTrue(machine.getSpeed() >= 9, "spun up");
            helper.succeed();
        });
    }

    /** A redstone signal keeps the machine spinning without an input. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void redstoneSignalKeepsSpinning(GameTestHelper helper) {
        AdvancedMachineBlockEntity machine = place(helper, Ic2Machine.CENTRIFUGE_EXTRACTOR);
        machine.setEnergy(1000);
        helper.setBlock(POS.east(), Blocks.REDSTONE_BLOCK);
        helper.runAfterDelay(5, () -> {
            helper.assertTrue(machine.getSpeed() >= 4, "spinning");
            helper.succeed();
        });
    }

    /** Without work the speed drops by 4 per tick. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void idleMachineSpinsDown(GameTestHelper helper) {
        AdvancedMachineBlockEntity machine = place(helper, Ic2Machine.CENTRIFUGE_EXTRACTOR);
        machine.setSpeed(10);
        helper.runAfterDelay(4, () -> {
            helper.assertValueEqual(machine.getSpeed(), 0, "speed");
            helper.succeed();
        });
    }

    /** Coal in the supplement slot turns iron dust into refined iron dust. */
    @GameTest(template = EMPTY, timeoutTicks = 30)
    public static void coalSupplementMakesRefinedIronDust(GameTestHelper helper) {
        AdvancedMachineBlockEntity machine = place(helper, Ic2Machine.ROTARY_MACERATOR);
        machine.setEnergy(1000);
        machine.setSpeed(AdvancedMachineBlockEntity.BASE_MAX_SPEED);
        machine.getInventory().set(AdvancedMachineType.SLOT_INPUT, new ItemStack(Items.IRON_INGOT));
        machine.getInventory().set(RotaryMaceratorBlockEntity.SLOT_SUPPLEMENT, new ItemStack(Items.COAL));
        helper.runAfterDelay(18, () -> {
            helper.assertTrue(machine.getInventory().get(2).is(AdvancedMachinesItems.REFINED_IRON_DUST.get()),
                    "refined iron dust");
            helper.assertValueEqual(((RotaryMaceratorBlockEntity) machine).getSupplementedItemsLeft(),
                    RotaryMaceratorBlockEntity.ITEMS_PER_COAL - 1, "coal lasts 128");
            helper.assertTrue(machine.getInventory().get(RotaryMaceratorBlockEntity.SLOT_SUPPLEMENT).is(Items.COAL),
                    "coal not used yet");
            helper.succeed();
        });
    }

    /** Transformer upgrades raise the safe input by ×4 each; storage upgrades add 5000. */
    @GameTest(template = EMPTY, timeoutTicks = 10)
    public static void upgradesRaiseInputAndStorage(GameTestHelper helper) {
        AdvancedMachineBlockEntity machine = place(helper, Ic2Machine.SINGULARITY_COMPRESSOR);
        machine.getInventory().set(3, new ItemStack(Ic2Items.TRANSFORMER_UPGRADE.get()));
        machine.getInventory().set(4, new ItemStack(Ic2Items.ENERGY_STORAGE_UPGRADE.get()));
        machine.setOverclockRates();
        helper.assertValueEqual(machine.getMaxSafeInput(), 128, "max input");
        helper.assertValueEqual(machine.getMaxEnergy(), 5000 + 5000 + 128 - 1, "max energy");
        helper.assertValueEqual(machine.getTier(), 2, "tier");
        helper.assertValueEqual(machine.injectEnergy(Direction.UP, 100), 0, "accepts 100");
        helper.succeed();
    }

    /** Redstone in the energy slot adds the whole storage at once. */
    @GameTest(template = EMPTY, timeoutTicks = 10)
    public static void redstoneFillsStorage(GameTestHelper helper) {
        AdvancedMachineBlockEntity machine = place(helper, Ic2Machine.CENTRIFUGE_EXTRACTOR);
        machine.getInventory().set(AdvancedMachineType.SLOT_ENERGY, new ItemStack(Items.REDSTONE, 2));
        helper.runAfterDelay(1, () -> {
            helper.assertTrue(machine.getEnergy() >= AdvancedMachineBlockEntity.BASE_ENERGY, "filled");
            helper.succeed();
        });
    }
}
