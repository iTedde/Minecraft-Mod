package dev.craft.gregtech.machine;

import dev.craft.Craft;
import dev.craft.gregtech.GtBlocks;
import dev.craft.gregtech.GtItems;
import dev.craft.gregtech.GtMachine;
import dev.craft.gregtech.block.GtCasingBlock;
import dev.craft.ic2.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for the industrial blast furnace and its casings (phase 6b.3b). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class BlastFurnaceGameTests {
    private static final String EMPTY = "empty_16x8x16";
    /** Controller faces north (default state); the shell lies south of it. */
    private static final BlockPos CONTROLLER = new BlockPos(5, 1, 3);

    private BlastFurnaceGameTests() {
    }

    /** Builds the 3 × 3 × 4 shell behind the controller; the two core cells get {@code lava} or stay air. */
    private static BlastFurnaceBlockEntity build(GameTestHelper helper, GtCasingBlock.Type type, boolean lava) {
        helper.setBlock(CONTROLLER, GtBlocks.machine(GtMachine.INDUSTRIAL_BLAST_FURNACE).get());
        BlastFurnaceBlockEntity furnace = helper.getBlockEntity(CONTROLLER);
        for (int y = 0; y < 4; y++) {
            for (int dz = 1; dz <= 3; dz++) {
                for (int dx = -1; dx <= 1; dx++) {
                    BlockPos pos = CONTROLLER.offset(dx, y, dz);
                    boolean core = dx == 0 && dz == 2 && (y == 1 || y == 2);
                    if (!core) {
                        helper.setBlock(pos, GtBlocks.casing(type).get());
                    } else if (lava) {
                        helper.setBlock(pos, Blocks.LAVA);
                    }
                }
            }
        }
        furnace.skipStartupForTest();
        return furnace;
    }

    @GameTest(template = EMPTY, timeoutTicks = 30)
    public static void standardCasingsGive1020K(GameTestHelper helper) {
        BlastFurnaceBlockEntity furnace = build(helper, GtCasingBlock.Type.STANDARD, false);
        helper.succeedWhen(() -> {
            helper.assertTrue(furnace.isMachine(), "structure formed");
            helper.assertValueEqual(furnace.getHeatCapacity(), 34 * 30, "heat");
            helper.assertBlockProperty(CONTROLLER, GtMachineBlock.ACTIVE, true);
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 30)
    public static void lavaCoreAdds500K(GameTestHelper helper) {
        BlastFurnaceBlockEntity furnace = build(helper, GtCasingBlock.Type.REINFORCED, true);
        helper.succeedWhen(() -> helper.assertValueEqual(furnace.getHeatCapacity(), 34 * 50 + 2 * 250, "heat"));
    }

    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void removingCasingBreaksStructure(GameTestHelper helper) {
        BlastFurnaceBlockEntity furnace = build(helper, GtCasingBlock.Type.STANDARD, false);
        helper.runAfterDelay(10, () -> {
            helper.assertTrue(furnace.isMachine(), "formed first");
            helper.setBlock(CONTROLLER.offset(1, 3, 3), Blocks.AIR);
        });
        helper.runAfterDelay(20, () -> {
            helper.assertFalse(furnace.isMachine(), "broken");
            helper.assertValueEqual(furnace.getHeatCapacity(), 0, "heat");
            helper.assertBlockProperty(CONTROLLER, GtMachineBlock.ACTIVE, false);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 140)
    public static void smeltsSteelDust(GameTestHelper helper) {
        BlastFurnaceBlockEntity furnace = build(helper, GtCasingBlock.Type.STANDARD, false);
        furnace.setStoredEnergyForTest(15000);
        furnace.getInventory().set(BlastFurnaceBlockEntity.SLOT_INPUT_1, new ItemStack(GtItems.STEEL_DUST.get()));
        helper.succeedWhen(() -> {
            helper.assertTrue(furnace.getInventory().get(BlastFurnaceBlockEntity.SLOT_OUTPUT_1)
                    .is(GtItems.STEEL_INGOT.get()), "steel ingot");
            helper.assertValueEqual(furnace.getStoredEU(), 15000 - 100 * 128, "energy used");
        });
    }

    /** Shapeless: coal dust in the first and refined iron in the second slot still starts the steel recipe. */
    @GameTest(template = EMPTY, timeoutTicks = 30)
    public static void steelRecipeIsShapeless(GameTestHelper helper) {
        BlastFurnaceBlockEntity furnace = build(helper, GtCasingBlock.Type.STANDARD, false);
        furnace.setStoredEnergyForTest(10000);
        furnace.getInventory().set(BlastFurnaceBlockEntity.SLOT_INPUT_1, new ItemStack(Ic2Items.COAL_DUST.get(), 3));
        furnace.getInventory().set(BlastFurnaceBlockEntity.SLOT_INPUT_2,
                new ItemStack(Ic2Items.REFINED_IRON_INGOT.get()));
        helper.succeedWhen(() -> {
            helper.assertTrue(furnace.getProgressTime() > 0, "running");
            helper.assertValueEqual(furnace.getInventory().get(BlastFurnaceBlockEntity.SLOT_INPUT_1).getCount(), 1,
                    "two coal dust used");
            helper.assertTrue(furnace.getInventory().get(BlastFurnaceBlockEntity.SLOT_INPUT_2).isEmpty(),
                    "ingot used");
        });
    }

    /** Aluminium needs 1700 K; standard casings reach only 1020 K. */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void aluminiumNeedsMoreHeat(GameTestHelper helper) {
        BlastFurnaceBlockEntity furnace = build(helper, GtCasingBlock.Type.STANDARD, false);
        furnace.setStoredEnergyForTest(10000);
        furnace.getInventory().set(BlastFurnaceBlockEntity.SLOT_INPUT_1, new ItemStack(GtItems.ALUMINIUM_DUST.get()));
        helper.runAfterDelay(30, () -> {
            helper.assertTrue(furnace.isMachine(), "formed");
            helper.assertValueEqual(furnace.getProgressTime(), 0, "not started");
            helper.assertValueEqual(furnace.getInventory().get(BlastFurnaceBlockEntity.SLOT_INPUT_1).getCount(), 1,
                    "dust kept");
            helper.succeed();
        });
    }

    /** Four Kanthal coils raise the tier to 1 (+500 K); Cupronickel coils are not accepted. */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void kanthalCoilsAdd500K(GameTestHelper helper) {
        BlastFurnaceBlockEntity furnace = build(helper, GtCasingBlock.Type.STANDARD, false);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack cupronickel = new ItemStack(GtItems.CUPRONICKEL_HEATING_COIL.get(), 4);
        helper.assertFalse(furnace.onRightclick(player, cupronickel), "cupronickel opens the GUI");
        ItemStack three = new ItemStack(GtItems.KANTHAL_HEATING_COIL.get(), 3);
        helper.assertTrue(furnace.onRightclick(player, three), "click used up");
        helper.assertValueEqual(furnace.getHeatingCoilTier(), 0, "three coils do nothing");
        ItemStack kanthal = new ItemStack(GtItems.KANTHAL_HEATING_COIL.get(), 5);
        helper.assertTrue(furnace.onRightclick(player, kanthal), "coils installed");
        helper.assertValueEqual(kanthal.getCount(), 1, "four coils used");
        helper.assertValueEqual(furnace.getHeatingCoilTier(), 1, "tier");
        helper.succeedWhen(() -> helper.assertValueEqual(furnace.getHeatCapacity(), 34 * 30 + 500, "heat"));
    }
}
