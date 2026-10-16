package dev.craft.ic2.machine;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.generator.GeothermalGeneratorBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for the IC2 pump and the compressor snowball bonus (phase 8d.1). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class PumpGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos FLUID = new BlockPos(2, 1, 2);
    private static final BlockPos PUMP = FLUID.above();

    private PumpGameTests() {
    }

    private static PumpBlockEntity place(GameTestHelper helper) {
        helper.setBlock(PUMP, Ic2Blocks.machine(Ic2Machine.PUMP).get());
        return helper.getBlockEntity(PUMP);
    }

    /** 200 EU charge the pump in 200 ticks; then the water source becomes a water cell in the chest beside it. */
    @GameTest(template = EMPTY, timeoutTicks = 260)
    public static void chargedPumpFillsCellIntoChest(GameTestHelper helper) {
        helper.setBlock(FLUID, Blocks.WATER);
        PumpBlockEntity pump = place(helper);
        helper.setBlock(PUMP.east(), Blocks.CHEST);
        pump.setEnergyForTest(200);
        pump.getInventory().set(PumpBlockEntity.SLOT_CONTAINER, new ItemStack(Ic2Items.EMPTY_CELL.get(), 2));
        helper.runAfterDelay(150, () -> helper.assertBlockPresent(Blocks.WATER, FLUID));
        helper.succeedWhen(() -> {
            ChestBlockEntity chest = helper.getBlockEntity(PUMP.east());
            helper.assertTrue(chest.getItem(0).is(Ic2Items.WATER_CELL.get()), "water cell in chest");
            helper.assertBlockPresent(Blocks.AIR, FLUID);
            helper.assertValueEqual(pump.getInventory().get(PumpBlockEntity.SLOT_CONTAINER).getCount(), 1,
                    "cells left");
            helper.assertValueEqual(pump.getPumpCharge(), 0, "charge reset");
        });
    }

    /** Original quirk: a whole stack of empty buckets is filled by one source block. */
    @GameTest(template = EMPTY, timeoutTicks = 10)
    public static void bucketStackIsFilledAtOnce(GameTestHelper helper) {
        helper.setBlock(FLUID, Blocks.WATER);
        PumpBlockEntity pump = place(helper);
        helper.setBlock(PUMP.east(), Blocks.CHEST);
        pump.setPumpChargeForTest(200);
        pump.getInventory().set(PumpBlockEntity.SLOT_CONTAINER, new ItemStack(Items.BUCKET, 3));
        helper.succeedWhen(() -> {
            ChestBlockEntity chest = helper.getBlockEntity(PUMP.east());
            int buckets = 0;
            for (int i = 0; i < chest.getContainerSize(); i++) {
                if (chest.getItem(i).is(Items.WATER_BUCKET)) {
                    buckets += chest.getItem(i).getCount();
                }
            }
            helper.assertValueEqual(buckets, 3, "water buckets");
            helper.assertTrue(pump.getInventory().get(PumpBlockEntity.SLOT_CONTAINER).isEmpty(), "slot emptied");
        });
    }

    /**
     * Lava goes to an adjacent geothermal generator first, which credits half of the 3000 mB. The pump still needs a
     * container to harvest at all, but keeps it.
     */
    @GameTest(template = EMPTY, timeoutTicks = 10)
    public static void lavaFeedsGeothermalGenerator(GameTestHelper helper) {
        helper.setBlock(FLUID, Blocks.LAVA);
        PumpBlockEntity pump = place(helper);
        helper.setBlock(PUMP.east(), Ic2Blocks.machine(Ic2Machine.GEOTHERMAL_GENERATOR).get());
        pump.getInventory().set(PumpBlockEntity.SLOT_CONTAINER, new ItemStack(Ic2Items.EMPTY_CELL.get()));
        pump.setPumpChargeForTest(200);
        helper.succeedWhen(() -> {
            GeothermalGeneratorBlockEntity geo = helper.getBlockEntity(PUMP.east());
            helper.assertTrue(geo.getFuel() > 1000, "lava credited");
            helper.assertTrue(pump.getInventory().get(PumpBlockEntity.SLOT_CONTAINER).is(Ic2Items.EMPTY_CELL.get()),
                    "cell kept");
            helper.assertBlockPresent(Blocks.AIR, FLUID);
        });
    }

    /** Without a container nothing is pumped and the charge stays full. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void noContainerKeepsSource(GameTestHelper helper) {
        helper.setBlock(FLUID, Blocks.WATER);
        PumpBlockEntity pump = place(helper);
        pump.setPumpChargeForTest(200);
        helper.runAfterDelay(10, () -> {
            helper.assertBlockPresent(Blocks.WATER, FLUID);
            helper.assertTrue(pump.isPumpReady(), "still ready");
            helper.succeed();
        });
    }

    /** A compressor next to a ready pump above water makes a snowball from nothing. */
    @GameTest(template = EMPTY, timeoutTicks = 10)
    public static void compressorMakesSnowballFromPump(GameTestHelper helper) {
        helper.setBlock(FLUID, Blocks.WATER);
        PumpBlockEntity pump = place(helper);
        pump.setPumpChargeForTest(200);
        helper.setBlock(PUMP.east(), Ic2Blocks.machine(Ic2Machine.COMPRESSOR).get());
        RecipeMachineBlockEntity compressor = helper.getBlockEntity(PUMP.east());
        helper.assertTrue(compressor.canOperate(), "can operate without input");
        compressor.operate();
        helper.assertTrue(compressor.getInventory().get(ElectricMachineBlockEntity.SLOT_OUTPUT).is(Items.SNOWBALL),
                "snowball");
        helper.assertBlockPresent(Blocks.AIR, FLUID);
        helper.assertValueEqual(pump.getPumpCharge(), 0, "charge reset");
        helper.succeed();
    }
}
