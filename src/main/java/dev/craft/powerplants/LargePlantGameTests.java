package dev.craft.powerplants;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.machine.MachineBlock;
import dev.craft.ic2.storage.ElectricStorageBlockEntity;
import dev.craft.powerplants.boiler.BoilerBlockEntity;
import dev.craft.powerplants.boiler.BoilerTier;
import dev.craft.powerplants.turbine.SteamTurbineBlockEntity;
import dev.craft.powerplants.turbine.SteamTurbineTier;
import dev.craft.powerplants.waterturbine.WaterTurbineBlockEntity;
import dev.craft.powerplants.waterturbine.WaterTurbineTier;
import dev.craft.powerplants.windturbine.LargeWindTurbineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Tests for the large power plants (phase 21, spec 23). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class LargePlantGameTests {
    private static final String EMPTY = "empty_16x8x16";
    /** Controller of every 5-wide plant: middle of the north face of a box starting at x 2, y 1, z 1. */
    private static final BlockPos CONTROLLER = new BlockPos(4, 3, 1);
    private static final BlockPos FRONT = new BlockPos(4, 3, 0);

    private LargePlantGameTests() {
    }

    private static BoilerBlockEntity buildBoiler(GameTestHelper helper) {
        for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(2, 1, 1), new BlockPos(6, 5, 5))) {
            boolean inside = pos.getX() > 2 && pos.getX() < 6 && pos.getY() > 1 && pos.getY() < 5 && pos.getZ() > 1
                    && pos.getZ() < 5;
            if (!inside && !pos.equals(CONTROLLER)) {
                helper.setBlock(pos, PpBlocks.HIGH_PRESSURE_BOILER_CASING.get());
            }
        }
        helper.setBlock(CONTROLLER, PpBlocks.HIGH_PRESSURE_BOILER_FIREBOX.get().defaultBlockState()
                .setValue(MachineBlock.FACING, Direction.NORTH));
        return helper.getBlockEntity(CONTROLLER);
    }

    private static SteamTurbineBlockEntity buildTurbine(GameTestHelper helper) {
        for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(2, 1, 1), new BlockPos(6, 5, 9))) {
            boolean inside = pos.getX() > 2 && pos.getX() < 6 && pos.getY() > 1 && pos.getY() < 5 && pos.getZ() > 1
                    && pos.getZ() < 9;
            if (inside) {
                if (pos.getX() == 4 && pos.getY() == 3) {
                    helper.setBlock(pos, PpBlocks.LARGE_TURBINE_ROTOR.get());
                }
            } else if (!pos.equals(CONTROLLER)) {
                helper.setBlock(pos, PpBlocks.LARGE_TURBINE_CASING.get());
            }
        }
        helper.setBlock(CONTROLLER, PpBlocks.LARGE_STEAM_TURBINE.get().defaultBlockState()
                .setValue(MachineBlock.FACING, Direction.NORTH));
        return helper.getBlockEntity(CONTROLLER);
    }

    private static WaterTurbineBlockEntity buildWaterTurbine(GameTestHelper helper) {
        for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(2, 1, 1), new BlockPos(6, 5, 5))) {
            if (!pos.equals(CONTROLLER)) {
                helper.setBlock(pos, PpBlocks.LARGE_WATER_TURBINE_CASING.get());
            }
        }
        helper.setBlock(CONTROLLER, PpBlocks.LARGE_WATER_TURBINE.get().defaultBlockState()
                .setValue(MachineBlock.FACING, Direction.NORTH));
        return helper.getBlockEntity(CONTROLLER);
    }

    private static ElectricStorageBlockEntity mfsu(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, Ic2Blocks.machine(Ic2Machine.MFSU).get());
        return helper.getBlockEntity(pos);
    }

    // --- high-pressure boiler ---

    /** At 1500 °C the high-pressure boiler turns 160 mB water into 160 mB steam per tick; coal burns 150 ticks. */
    @GameTest(template = EMPTY, timeoutTicks = 80)
    public static void highPressureBoilerBoils160PerTick(GameTestHelper helper) {
        BoilerBlockEntity boiler = buildBoiler(helper);
        BoilerTier tier = BoilerTier.HIGH_PRESSURE;
        boiler.setWater(tier.tankCapacity());
        boiler.setTemperature(tier.maxTemperature());
        boiler.getInventory().set(BoilerBlockEntity.SLOT_FUEL, new ItemStack(Items.COAL));
        int[] start = new int[2];
        helper.runAtTickTime(10, () -> {
            helper.assertTrue(boiler.isFormed(), "structure formed");
            helper.assertValueEqual(boiler.getMaxBurnTime(), tier.coalBurnTime(), "coal burn time");
            start[0] = boiler.getSteam();
            start[1] = boiler.getWater();
        });
        helper.runAtTickTime(60, () -> {
            helper.assertValueEqual(boiler.getTemperature(), 1500, "temperature");
            helper.assertValueEqual(boiler.getSteam() - start[0], 50 * 160, "steam");
            helper.assertValueEqual(start[1] - boiler.getWater(), 50 * 160, "water");
            helper.succeed();
        });
    }

    /** Coke burns 300 ticks (48 000 EU) and the boiler heats past the small boiler's 1000 °C up to 1500 °C. */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void highPressureBoilerHeatsTo1500(GameTestHelper helper) {
        BoilerBlockEntity boiler = buildBoiler(helper);
        boiler.setWater(BoilerTier.HIGH_PRESSURE.tankCapacity());
        boiler.setTemperature(1490);
        boiler.getInventory().set(BoilerBlockEntity.SLOT_FUEL, new ItemStack(PpItems.COKE.get()));
        helper.runAtTickTime(30, () -> {
            helper.assertValueEqual(boiler.getMaxBurnTime(), 300, "coke burn time");
            helper.assertValueEqual(300 * BoilerTier.HIGH_PRESSURE.maxSteamRate(), 48000, "EU per coke");
            helper.assertValueEqual(boiler.getTemperature(), 1500, "capped at 1500 °C");
            helper.succeed();
        });
    }

    /** A small-boiler casing in the shell does not form the high-pressure boiler. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void highPressureBoilerNeedsItsOwnCasing(GameTestHelper helper) {
        BoilerBlockEntity boiler = buildBoiler(helper);
        helper.setBlock(new BlockPos(6, 3, 3), PpBlocks.BOILER_CASING.get());
        helper.runAtTickTime(10, () -> {
            helper.assertFalse(boiler.isFormed(), "not formed");
            helper.succeed();
        });
    }

    /** Water on a dry, hot high-pressure boiler destroys all of it (own batch: power 10 blast). */
    @GameTest(template = EMPTY, batch = "high_pressure_boiler_explosion", timeoutTicks = 40)
    public static void waterOnDryHighPressureBoilerExplodes(GameTestHelper helper) {
        BoilerBlockEntity boiler = buildBoiler(helper);
        boiler.setTemperature(800);
        helper.runAtTickTime(10, () -> {
            helper.assertTrue(boiler.isDry(), "dry");
            IFluidHandler casing = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
                    helper.absolutePos(new BlockPos(6, 3, 3)), Direction.EAST);
            helper.assertTrue(casing != null, "fluid access through the casing");
            casing.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
        });
        helper.runAtTickTime(15, () -> {
            helper.assertBlockNotPresent(PpBlocks.HIGH_PRESSURE_BOILER_FIREBOX.get(), CONTROLLER);
            helper.assertBlockNotPresent(PpBlocks.HIGH_PRESSURE_BOILER_CASING.get(), new BlockPos(4, 5, 5));
            helper.succeed();
        });
    }

    // --- large steam turbine ---

    /** 512 mB steam per tick become 512 EU per tick (HV) in an MFSU in front of the controller. */
    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void largeTurbineTurns512IntoEu(GameTestHelper helper) {
        SteamTurbineBlockEntity turbine = buildTurbine(helper);
        ElectricStorageBlockEntity storage = mfsu(helper, FRONT);
        turbine.getTank().fill(new FluidStack(PpFluids.STEAM.get(), SteamTurbineTier.LARGE.tankCapacity()),
                IFluidHandler.FluidAction.EXECUTE);
        int[] start = new int[2];
        helper.runAtTickTime(10, () -> {
            helper.assertTrue(turbine.isFormed(), "structure formed");
            start[0] = turbine.getSteam();
            start[1] = storage.getEnergy();
        });
        helper.runAtTickTime(40, () -> {
            helper.assertValueEqual(turbine.getProduction(), 512, "EU/t");
            helper.assertValueEqual(start[0] - turbine.getSteam(), 30 * 512, "steam used");
            helper.assertValueEqual(storage.getEnergy() - start[1], 30 * 512, "energy delivered");
            helper.setBlock(FRONT, Blocks.AIR);
            helper.succeed();
        });
    }

    /** Every large casing conducts: an MFSU beside the back of the turbine gets the full output. */
    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void largeTurbineCasingDeliversEnergy(GameTestHelper helper) {
        BlockPos beside = new BlockPos(7, 3, 8);
        SteamTurbineBlockEntity turbine = buildTurbine(helper);
        ElectricStorageBlockEntity storage = mfsu(helper, beside);
        IFluidHandler[] back = new IFluidHandler[1];
        int[] start = new int[1];
        helper.runAtTickTime(5, () -> {
            back[0] = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
                    helper.absolutePos(new BlockPos(4, 3, 9)), Direction.SOUTH);
            helper.assertTrue(back[0] != null, "steam through the back casing");
            back[0].fill(new FluidStack(PpFluids.STEAM.get(), 64000), IFluidHandler.FluidAction.EXECUTE);
        });
        helper.runAtTickTime(10, () -> start[0] = storage.getEnergy());
        helper.runAtTickTime(40, () -> {
            helper.assertValueEqual(storage.getEnergy() - start[0], 30 * 512, "energy through the casing");
            helper.setBlock(beside, Blocks.AIR);
            helper.succeed();
        });
    }

    /** The space around the rotor axis must be air. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void largeTurbineNeedsAirAroundTheAxis(GameTestHelper helper) {
        SteamTurbineBlockEntity turbine = buildTurbine(helper);
        helper.setBlock(new BlockPos(3, 3, 5), PpBlocks.LARGE_TURBINE_CASING.get());
        helper.runAtTickTime(10, () -> {
            helper.assertFalse(turbine.isFormed(), "not formed");
            helper.succeed();
        });
    }

    // --- large water turbine ---

    /** 1 EU/t per flowing block from 64 blocks on, × 1.5 in rivers, at most 256 EU/t (384 in rivers). */
    @GameTest(template = EMPTY)
    public static void largeWaterTurbineOutput(GameTestHelper helper) {
        WaterTurbineTier tier = WaterTurbineTier.LARGE;
        helper.assertValueEqual(tier.output(63, true), 0.0, "below 64");
        helper.assertValueEqual(tier.output(64, false), 64.0, "64 flowing");
        helper.assertValueEqual(tier.output(100, true), 150.0, "river");
        helper.assertValueEqual(tier.output(256, false), 256.0, "cap reached");
        helper.assertValueEqual(tier.output(400, false), 256.0, "cap");
        helper.assertValueEqual(tier.output(300, true), 384.0, "river cap");
        helper.assertValueEqual(tier.packetSize(), 384, "HV packets");
        helper.succeed();
    }

    /** 64 flowing blocks on top of the cube feed 64 EU/t into an MFSU in front of the controller. */
    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void largeWaterTurbineFeedsTheNet(GameTestHelper helper) {
        WaterTurbineBlockEntity turbine = buildWaterTurbine(helper);
        ElectricStorageBlockEntity storage = mfsu(helper, FRONT);
        int[] start = new int[1];
        helper.runAtTickTime(5, () -> {
            helper.assertTrue(turbine.isFormed(), "structure formed");
            BlockState flowing = Blocks.WATER.defaultBlockState().setValue(LiquidBlock.LEVEL, 1);
            // 64 flowing blocks in an 8×8 patch on the cube's top layer and beside it
            for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(0, 7, 0), new BlockPos(7, 7, 7))) {
                helper.setBlock(pos, flowing);
            }
            turbine.updateWaterCount();
            helper.assertValueEqual(turbine.getFlowing(), 64, "flowing");
            helper.assertValueEqual(turbine.getFlow(), WaterTurbineTier.LARGE.output(64, turbine.isInRiver()),
                    "EU/t");
            start[0] = storage.getEnergy();
        });
        helper.runAtTickTime(35, () -> {
            helper.assertValueEqual(storage.getEnergy() - start[0], (int) (30 * turbine.getFlow()),
                    "energy delivered");
            helper.setBlock(FRONT, Blocks.AIR);
            helper.succeed();
        });
    }

    // --- large wind turbine ---

    /** 12 × the wind mill formula × rotor factor (spec 32 §2): y 128 → 38.4, y 200 → 81.6, y 319 → 153, cap 192. */
    @GameTest(template = EMPTY)
    public static void largeWindTurbineOutput(GameTestHelper helper) {
        helper.assertValueEqual(Math.round(LargeWindTurbineBlockEntity.output(15, 128, 0, false, false, 1.0) * 100),
                3840L, "y 128 carbon");
        helper.assertValueEqual(Math.round(LargeWindTurbineBlockEntity.output(15, 200, 0, false, false, 1.0) * 100),
                8160L, "y 200 carbon");
        helper.assertValueEqual(Math.round(LargeWindTurbineBlockEntity.output(15, 319, 0, false, false, 1.0) * 100),
                15300L, "y 319 carbon");
        helper.assertValueEqual(LargeWindTurbineBlockEntity.output(15, 200, 0, true, false, 1.0), 192.0,
                "thunder cap");
        helper.assertValueEqual(Math.round(LargeWindTurbineBlockEntity.output(15, 128, 0, false, false, 0.5) * 100),
                1920L, "iron rotor");
        helper.assertValueEqual(LargeWindTurbineBlockEntity.output(15, 128, 0, false, false, 0.0), 0.0, "no rotor");
        helper.assertValueEqual(LargeWindTurbineBlockEntity.output(15, 70, 10, false, false, 1.0), 0.0,
                "obstructed");
        helper.succeed();
    }

    /**
     * The nacelle on 24 masts forms, takes a rotor and delivers its output through the mast foot; a missing mast
     * stops it.
     */
    @GameTest(template = "empty_16x32x16", timeoutTicks = 80)
    public static void largeWindTurbineDeliversThroughTheMast(GameTestHelper helper) {
        BlockPos nacelle = new BlockPos(4, LargeWindTurbineBlockEntity.MAST_HEIGHT + 1, 4);
        for (int y = 1; y <= LargeWindTurbineBlockEntity.MAST_HEIGHT; y++) {
            helper.setBlock(new BlockPos(4, y, 4), PpBlocks.WIND_TURBINE_MAST.get());
        }
        helper.setBlock(nacelle, PpBlocks.LARGE_WIND_TURBINE.get().defaultBlockState()
                .setValue(MachineBlock.FACING, Direction.NORTH));
        LargeWindTurbineBlockEntity turbine = helper.getBlockEntity(nacelle);
        BlockPos foot = new BlockPos(5, 1, 4);
        ElectricStorageBlockEntity storage = mfsu(helper, foot);
        int[] start = new int[1];
        helper.runAtTickTime(5, () -> {
            helper.assertTrue(turbine.isFormed(), "structure formed");
            ItemStack previous = turbine.swapRotor(new ItemStack(PpItems.CARBON_WIND_ROTOR.get()));
            helper.assertTrue(previous.isEmpty(), "no rotor before");
            helper.assertValueEqual(turbine.getRotorFactor(), 1.0, "carbon factor");
            turbine.setPowerForTest(100.0);
            start[0] = storage.getEnergy();
        });
        helper.runAtTickTime(35, () -> {
            helper.assertValueEqual(storage.getEnergy() - start[0], 30 * 100, "energy through the mast");
            helper.setBlock(new BlockPos(4, 5, 4), Blocks.AIR);
        });
        helper.runAtTickTime(45, () -> start[0] = storage.getEnergy());
        helper.runAtTickTime(60, () -> {
            helper.assertFalse(turbine.isFormed(), "not formed without a mast");
            helper.assertValueEqual(turbine.getPower(), 0.0, "no output");
            helper.assertValueEqual(storage.getEnergy(), start[0], "no energy while broken");
            helper.setBlock(foot, Blocks.AIR);
            helper.succeed();
        });
    }
}
