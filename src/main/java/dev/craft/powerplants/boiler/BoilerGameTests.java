package dev.craft.powerplants.boiler;

import dev.craft.Craft;
import dev.craft.buildcraft.BcBlocks;
import dev.craft.gregtech.GtItems;
import dev.craft.ic2.machine.MachineBlock;
import dev.craft.powerplants.PpBlocks;
import dev.craft.powerplants.PpFluids;
import dev.craft.powerplants.PpItems;
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

/** Tests for the solid fuel boiler (phase 9.3a, spec 14 §3). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class BoilerGameTests {
    private static final String EMPTY = "empty_8x4x8";
    /** Firebox in the middle of the north side, facing north; the cube spans x 2–4, y 1–3, z 1–3. */
    private static final BlockPos FIREBOX = new BlockPos(3, 2, 1);
    private static final BlockPos CORE = new BlockPos(3, 2, 2);
    private static final BlockPos EAST_CASING = new BlockPos(4, 2, 2);

    private BoilerGameTests() {
    }

    private static BoilerBlockEntity build(GameTestHelper helper) {
        for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(2, 1, 1), new BlockPos(4, 3, 3))) {
            if (!pos.equals(CORE) && !pos.equals(FIREBOX)) {
                helper.setBlock(pos, PpBlocks.BOILER_CASING.get());
            }
        }
        helper.setBlock(FIREBOX, PpBlocks.BOILER_FIREBOX.get().defaultBlockState()
                .setValue(MachineBlock.FACING, Direction.NORTH));
        return helper.getBlockEntity(FIREBOX);
    }

    /** Burning coke heats the boiler by 1 °C per tick; the burn time is 1600 ticks. */
    @GameTest(template = EMPTY, timeoutTicks = 80)
    public static void heatsUpByOneDegreePerTick(GameTestHelper helper) {
        BoilerBlockEntity boiler = build(helper);
        boiler.setWater(BoilerBlockEntity.TANK_CAPACITY);
        boiler.getInventory().set(BoilerBlockEntity.SLOT_FUEL, new ItemStack(PpItems.COKE.get()));
        helper.runAtTickTime(60, () -> {
            helper.assertTrue(boiler.isFormed(), "structure formed");
            helper.assertTrue(boiler.getInventory().get(BoilerBlockEntity.SLOT_FUEL).isEmpty(), "coke burnt");
            helper.assertValueEqual(boiler.getMaxBurnTime(), BoilerBlockEntity.COKE_BURN_TIME, "coke burn time");
            int heated = boiler.getTemperature() - BoilerBlockEntity.MIN_TEMPERATURE;
            int burnt = BoilerBlockEntity.COKE_BURN_TIME - boiler.getBurnTime();
            helper.assertValueEqual(heated, burnt, "1 °C per burning tick");
            helper.assertTrue(heated >= 55, "heated for most of the 60 ticks");
            helper.assertValueEqual(boiler.getSteam(), 0, "no steam below 100 °C");
            helper.succeed();
        });
    }

    /** At 1000 °C the boiler turns 20 mB water into 20 mB steam per tick. */
    @GameTest(template = EMPTY, timeoutTicks = 80)
    public static void boilsTwentyMillibucketsPerTickAtFullHeat(GameTestHelper helper) {
        BoilerBlockEntity boiler = build(helper);
        boiler.setWater(BoilerBlockEntity.TANK_CAPACITY);
        boiler.setTemperature(BoilerBlockEntity.MAX_TEMPERATURE);
        boiler.getInventory().set(BoilerBlockEntity.SLOT_FUEL, new ItemStack(Items.COAL));
        int[] start = new int[2];
        helper.runAtTickTime(10, () -> {
            start[0] = boiler.getSteam();
            start[1] = boiler.getWater();
        });
        helper.runAtTickTime(60, () -> {
            helper.assertValueEqual(boiler.getTemperature(), BoilerBlockEntity.MAX_TEMPERATURE, "temperature");
            helper.assertValueEqual(boiler.getSteam() - start[0], 50 * BoilerBlockEntity.MAX_STEAM_RATE, "steam");
            helper.assertValueEqual(start[1] - boiler.getWater(), 50 * BoilerBlockEntity.MAX_STEAM_RATE, "water");
            helper.succeed();
        });
    }

    /** Every fourth coal (or second coke) leaves one ash; charcoal is no fuel. */
    @GameTest(template = EMPTY, timeoutTicks = 20)
    public static void coalLeavesAshAndCharcoalIsRefused(GameTestHelper helper) {
        BoilerBlockEntity boiler = build(helper);
        boiler.setWater(BoilerBlockEntity.TANK_CAPACITY);
        boiler.setAshCounter(BoilerBlockEntity.ASH_PER_ITEM - 1);
        boiler.getInventory().set(BoilerBlockEntity.SLOT_FUEL, new ItemStack(Items.COAL));
        helper.assertFalse(BoilerBlockEntity.isFuel(new ItemStack(Items.CHARCOAL)), "charcoal");
        helper.assertFalse(BoilerBlockEntity.isFuel(new ItemStack(Items.COAL_BLOCK)), "coal block");
        helper.runAtTickTime(10, () -> {
            ItemStack ash = boiler.getInventory().get(BoilerBlockEntity.SLOT_ASH);
            helper.assertTrue(ash.is(GtItems.ASH.get()) && ash.getCount() == 1, "one ash");
            helper.assertValueEqual(boiler.getMaxBurnTime(), BoilerBlockEntity.COAL_BURN_TIME, "coal burn time");
            helper.succeed();
        });
    }

    /** Water poured into a dry boiler above 100 °C destroys the whole structure (own batch: power 6 blast). */
    @GameTest(template = EMPTY, batch = "boiler_explosion", timeoutTicks = 40)
    public static void waterOnDryHotBoilerExplodes(GameTestHelper helper) {
        BoilerBlockEntity boiler = build(helper);
        boiler.setTemperature(500);
        helper.runAtTickTime(10, () -> {
            helper.assertTrue(boiler.isDry(), "dry");
            IFluidHandler casing = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
                    helper.absolutePos(EAST_CASING), Direction.EAST);
            helper.assertTrue(casing != null, "fluid access through the casing");
            helper.assertValueEqual(casing.fill(new FluidStack(Fluids.WATER, 1000),
                    IFluidHandler.FluidAction.EXECUTE), 1000, "water accepted");
        });
        helper.runAtTickTime(15, () -> {
            helper.assertBlockNotPresent(PpBlocks.BOILER_FIREBOX.get(), FIREBOX);
            helper.assertBlockNotPresent(PpBlocks.BOILER_CASING.get(), EAST_CASING);
            helper.succeed();
        });
    }

    /** A hot boiler without water cools down below 100 °C and is no longer dry. */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void dryBoilerRecoversWhenCooledDown(GameTestHelper helper) {
        BoilerBlockEntity boiler = build(helper);
        boiler.setTemperature(BoilerBlockEntity.BOILING_POINT + 10);
        helper.runAtTickTime(5, () -> helper.assertTrue(boiler.isDry(), "dry"));
        helper.runAtTickTime(25, () -> {
            helper.assertFalse(boiler.isDry(), "cooled down");
            boiler.getTanks().fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
        });
        helper.runAtTickTime(30, () -> {
            helper.assertBlockPresent(PpBlocks.BOILER_FIREBOX.get(), FIREBOX);
            helper.assertValueEqual(boiler.getWater(), 1000, "water");
            helper.succeed();
        });
    }

    /** Steam goes into an adjacent tank; the casing accepts only water and gives out only steam. */
    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void pushesSteamIntoNeighbours(GameTestHelper helper) {
        BoilerBlockEntity boiler = build(helper);
        helper.setBlock(new BlockPos(5, 2, 2), BcBlocks.TANK.get());
        boiler.setWater(BoilerBlockEntity.TANK_CAPACITY);
        boiler.setTemperature(BoilerBlockEntity.MAX_TEMPERATURE);
        boiler.getInventory().set(BoilerBlockEntity.SLOT_FUEL, new ItemStack(Items.COAL));
        helper.runAtTickTime(40, () -> {
            IFluidHandler tank = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
                    helper.absolutePos(new BlockPos(5, 2, 2)), Direction.WEST);
            helper.assertTrue(tank != null && tank.getFluidInTank(0).is(PpFluids.STEAM.get())
                    && tank.getFluidInTank(0).getAmount() > 0, "steam in the tank");
            helper.assertValueEqual(boiler.getSteam(), 0, "boiler pushed everything");
            IFluidHandler casing = boiler.getTanks();
            helper.assertValueEqual(casing.fill(new FluidStack(PpFluids.STEAM.get(), 100),
                    IFluidHandler.FluidAction.SIMULATE), 0, "no steam in");
            helper.assertTrue(casing.drain(100, IFluidHandler.FluidAction.SIMULATE).isEmpty(), "nothing to drain");
            helper.setBlock(new BlockPos(5, 2, 2), Blocks.AIR);
            helper.succeed();
        });
    }
}
