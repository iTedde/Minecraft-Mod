package dev.craft.gregtech.machine;

import dev.craft.Craft;
import dev.craft.gregtech.GtBlocks;
import dev.craft.gregtech.GtItems;
import dev.craft.gregtech.GtMachine;
import dev.craft.gregtech.block.GtCasingBlock;
import dev.craft.gregtech.world.GtOre;
import dev.craft.ic2.Ic2Items;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandlerModifiable;

/** In-world tests for the industrial grinder (phase 8d.2). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class GrinderGameTests {
    private static final String EMPTY = "empty_16x8x16";
    /** Controller faces north (default state); the cube lies south of it, one layer below to one above. */
    private static final BlockPos CONTROLLER = new BlockPos(5, 2, 3);

    private GrinderGameTests() {
    }

    /** Builds the 3 × 3 × 3 cube behind the controller; the core gets water if {@code water}. */
    private static GrinderBlockEntity build(GameTestHelper helper, boolean water) {
        helper.setBlock(CONTROLLER, GtBlocks.machine(GtMachine.INDUSTRIAL_GRINDER).get());
        GrinderBlockEntity grinder = helper.getBlockEntity(CONTROLLER);
        for (int dy = -1; dy <= 1; dy++) {
            for (int dz = 1; dz <= 3; dz++) {
                for (int dx = -1; dx <= 1; dx++) {
                    BlockPos pos = CONTROLLER.offset(dx, dy, dz);
                    if (dx == 0 && dy == 0 && dz == 2) {
                        if (water) {
                            helper.setBlock(pos, Blocks.WATER);
                        }
                    } else {
                        helper.setBlock(pos, GtBlocks.casing(dy == 0 ? GtCasingBlock.Type.REINFORCED
                                : GtCasingBlock.Type.STANDARD).get());
                    }
                }
            }
        }
        grinder.skipStartupForTest();
        return grinder;
    }

    private static IItemHandlerModifiable inventory(GrinderBlockEntity grinder) {
        return grinder.getInventory();
    }

    private static ItemStack output(GrinderBlockEntity grinder, int index) {
        return inventory(grinder).getStackInSlot(GrinderBlockEntity.SLOT_OUTPUT_1 + index);
    }

    @GameTest(template = EMPTY, timeoutTicks = 30)
    public static void structureForms(GameTestHelper helper) {
        GrinderBlockEntity grinder = build(helper, true);
        helper.succeedWhen(() -> {
            helper.assertTrue(grinder.isMachine(), "structure formed");
            helper.assertBlockProperty(CONTROLLER, GtMachineBlock.ACTIVE, true);
        });
    }

    /** Without the water core, or with standard casings in the middle layer, the structure is incomplete. */
    @GameTest(template = EMPTY, timeoutTicks = 30)
    public static void needsWaterAndReinforcedMiddle(GameTestHelper helper) {
        GrinderBlockEntity dry = build(helper, false);
        helper.runAfterDelay(20, () -> {
            helper.assertFalse(dry.isMachine(), "no water");
            helper.setBlock(CONTROLLER.offset(0, 0, 2), Blocks.WATER);
            helper.setBlock(CONTROLLER.offset(1, 0, 1), GtBlocks.casing(GtCasingBlock.Type.STANDARD).get());
        });
        helper.runAfterDelay(28, () -> {
            helper.assertFalse(dry.isMachine(), "standard casing in the middle layer");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 140)
    public static void grindsIronOreWithWaterCell(GameTestHelper helper) {
        GrinderBlockEntity grinder = build(helper, true);
        grinder.setStoredEnergyForTest(15000);
        inventory(grinder).setStackInSlot(GrinderBlockEntity.SLOT_INPUT, new ItemStack(Items.IRON_ORE));
        inventory(grinder).setStackInSlot(GrinderBlockEntity.SLOT_CELL, new ItemStack(Ic2Items.WATER_CELL.get()));
        helper.succeedWhen(() -> {
            helper.assertTrue(output(grinder, 0).is(Ic2Items.IRON_DUST.get()) && output(grinder, 0).getCount() == 2,
                    "2 iron dust");
            helper.assertTrue(output(grinder, 1).is(GtItems.SMALL_TIN_DUST.get()), "small tin dust");
            helper.assertTrue(output(grinder, 2).is(GtItems.SMALL_NICKEL_DUST.get()), "small nickel dust");
            helper.assertTrue(output(grinder, 3).is(Ic2Items.EMPTY_CELL.get()), "empty cell");
            helper.assertValueEqual(grinder.getStoredEU(), 15000 - 100 * 128, "energy used");
        });
    }

    /** Without a cell the tank supplies the water; the empty cell output is dropped. */
    @GameTest(template = EMPTY, timeoutTicks = 140)
    public static void grindsFromTank(GameTestHelper helper) {
        GrinderBlockEntity grinder = build(helper, true);
        IFluidHandler tank = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
                helper.absolutePos(CONTROLLER), Direction.UP);
        helper.assertValueEqual(tank.fill(new FluidStack(Fluids.LAVA, 1000), IFluidHandler.FluidAction.EXECUTE), 0,
                "lava refused");
        helper.assertValueEqual(tank.fill(new FluidStack(Fluids.WATER, 1500), IFluidHandler.FluidAction.EXECUTE),
                1500, "water accepted");
        helper.assertTrue(tank.drain(1000, IFluidHandler.FluidAction.EXECUTE).isEmpty(), "never drained");
        grinder.setStoredEnergyForTest(15000);
        inventory(grinder).setStackInSlot(GrinderBlockEntity.SLOT_INPUT, new ItemStack(Items.REDSTONE_ORE));
        helper.succeedWhen(() -> {
            helper.assertTrue(output(grinder, 0).is(Items.REDSTONE) && output(grinder, 0).getCount() == 10,
                    "10 redstone");
            helper.assertTrue(output(grinder, 1).is(GtItems.SMALL_GLOWSTONE_DUST.get()), "small glowstone dust");
            helper.assertTrue(output(grinder, 3).isEmpty(), "no empty cell");
            helper.assertValueEqual(grinder.getWaterAmount(), 500, "1000 mB used");
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 140)
    public static void waterBucketReturnsBucket(GameTestHelper helper) {
        GrinderBlockEntity grinder = build(helper, true);
        grinder.setStoredEnergyForTest(15000);
        inventory(grinder).setStackInSlot(GrinderBlockEntity.SLOT_INPUT, new ItemStack(GtItems.ore(GtOre.RUBY).get()));
        inventory(grinder).setStackInSlot(GrinderBlockEntity.SLOT_CELL, new ItemStack(Items.WATER_BUCKET));
        helper.succeedWhen(() -> {
            helper.assertTrue(output(grinder, 0).is(GtItems.RUBY.get()), "ruby");
            helper.assertValueEqual(output(grinder, 1).getCount(), 6, "6 small ruby dust");
            helper.assertTrue(output(grinder, 2).is(GtItems.SMALL_RED_GARNET_DUST.get()), "small red garnet dust");
            helper.assertTrue(output(grinder, 3).is(Items.BUCKET), "empty bucket");
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 140)
    public static void mercuryCellGivesMorePlatinum(GameTestHelper helper) {
        GrinderBlockEntity grinder = build(helper, true);
        grinder.setStoredEnergyForTest(15000);
        inventory(grinder).setStackInSlot(GrinderBlockEntity.SLOT_INPUT,
                new ItemStack(GtItems.ore(GtOre.SHELDONITE).get()));
        inventory(grinder).setStackInSlot(GrinderBlockEntity.SLOT_CELL, new ItemStack(GtItems.MERCURY_CELL.get()));
        helper.succeedWhen(() -> {
            helper.assertValueEqual(output(grinder, 0).getCount(), 3, "3 platinum dust");
            helper.assertTrue(output(grinder, 1).is(GtItems.NICKEL_DUST.get()), "nickel dust");
            helper.assertValueEqual(output(grinder, 2).getCount(), 2, "2 iridium nuggets");
            helper.assertTrue(output(grinder, 3).is(Ic2Items.EMPTY_CELL.get()), "empty cell");
        });
    }

    /** 16 netherrack per run, 1600 ticks; a full fourth output slot blocks even the tank path (GT quirk). */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void fullCellSlotBlocksTankRecipe(GameTestHelper helper) {
        GrinderBlockEntity grinder = build(helper, true);
        grinder.getFluidHandler(null).fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
        grinder.setStoredEnergyForTest(15000);
        inventory(grinder).setStackInSlot(GrinderBlockEntity.SLOT_INPUT, new ItemStack(Items.NETHERRACK, 16));
        inventory(grinder).setStackInSlot(GrinderBlockEntity.SLOT_OUTPUT_1 + 3,
                new ItemStack(Ic2Items.EMPTY_CELL.get(), 64));
        helper.runAfterDelay(30, () -> {
            helper.assertTrue(grinder.isMachine(), "formed");
            helper.assertValueEqual(grinder.getProgressTime(), 0, "not started");
            helper.assertValueEqual(grinder.getWaterAmount(), 1000, "water kept");
            inventory(grinder).setStackInSlot(GrinderBlockEntity.SLOT_OUTPUT_1 + 3, ItemStack.EMPTY);
        });
        helper.runAfterDelay(35, () -> {
            helper.assertTrue(grinder.getProgressTime() > 0, "running");
            helper.assertTrue(inventory(grinder).getStackInSlot(GrinderBlockEntity.SLOT_INPUT).isEmpty(),
                    "16 netherrack used");
            helper.succeed();
        });
    }
}
