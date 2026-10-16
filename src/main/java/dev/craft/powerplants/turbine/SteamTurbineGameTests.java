package dev.craft.powerplants.turbine;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.machine.MachineBlock;
import dev.craft.ic2.storage.ElectricStorageBlockEntity;
import dev.craft.powerplants.PpBlocks;
import dev.craft.powerplants.PpFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Tests for the steam turbine (phase 9.3b, spec 14 §4). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class SteamTurbineGameTests {
    private static final String EMPTY = "empty_8x4x8";
    /** Controller in the middle of the north end face, facing north; the box spans x 2–4, y 1–3, z 1–5. */
    private static final BlockPos CONTROLLER = new BlockPos(3, 2, 1);
    private static final BlockPos BACK_CASING = new BlockPos(3, 2, 5);
    private static final BlockPos CONSUMER = new BlockPos(3, 2, 0);

    private SteamTurbineGameTests() {
    }

    private static SteamTurbineBlockEntity build(GameTestHelper helper) {
        for (BlockPos pos : BlockPos.betweenClosed(new BlockPos(2, 1, 1), new BlockPos(4, 3, 5))) {
            boolean axis = pos.getX() == 3 && pos.getY() == 2;
            if (axis && pos.getZ() >= 2 && pos.getZ() <= 4) {
                helper.setBlock(pos, PpBlocks.TURBINE_ROTOR.get());
            } else if (!pos.equals(CONTROLLER)) {
                helper.setBlock(pos, PpBlocks.TURBINE_CASING.get());
            }
        }
        helper.setBlock(CONTROLLER, PpBlocks.STEAM_TURBINE.get().defaultBlockState()
                .setValue(MachineBlock.FACING, Direction.NORTH));
        return helper.getBlockEntity(CONTROLLER);
    }

    private static IFluidHandler backCasing(GameTestHelper helper) {
        return helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(BACK_CASING),
                Direction.SOUTH);
    }

    /** Without a consumer the 128 EU buffer fills up and no further steam is used. */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void fullBufferStopsSteamUse(GameTestHelper helper) {
        SteamTurbineBlockEntity turbine = build(helper);
        helper.runAtTickTime(5, () -> {
            IFluidHandler casing = backCasing(helper);
            helper.assertTrue(casing != null, "fluid access through the casing");
            helper.assertValueEqual(casing.fill(new FluidStack(Fluids.WATER, 1000),
                    IFluidHandler.FluidAction.EXECUTE), 0, "water refused");
            helper.assertValueEqual(casing.fill(new FluidStack(PpFluids.STEAM.get(), 1000),
                    IFluidHandler.FluidAction.EXECUTE), 1000, "steam accepted");
            helper.assertTrue(casing.drain(100, IFluidHandler.FluidAction.SIMULATE).isEmpty(), "never drained");
        });
        helper.runAtTickTime(20, () -> {
            helper.assertTrue(turbine.isFormed(), "structure formed");
            helper.assertValueEqual(turbine.getBuffer(), SteamTurbineBlockEntity.BUFFER_CAPACITY, "buffer full");
            helper.assertValueEqual(turbine.getSteam(), 1000 - SteamTurbineBlockEntity.BUFFER_CAPACITY, "steam");
            helper.succeed();
        });
    }

    /** With an MFE in front, 64 mB steam per tick become 64 EU per tick in the net. */
    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void turnsSixtyFourMillibucketsIntoSixtyFourEu(GameTestHelper helper) {
        SteamTurbineBlockEntity turbine = build(helper);
        helper.setBlock(CONSUMER, Ic2Blocks.machine(Ic2Machine.MFE).get());
        ElectricStorageBlockEntity mfe = helper.getBlockEntity(CONSUMER);
        turbine.getTank().fill(new FluidStack(PpFluids.STEAM.get(), SteamTurbineBlockEntity.TANK_CAPACITY),
                IFluidHandler.FluidAction.EXECUTE);
        int[] start = new int[2];
        helper.runAtTickTime(10, () -> {
            start[0] = turbine.getSteam();
            start[1] = mfe.getEnergy();
        });
        helper.runAtTickTime(40, () -> {
            helper.assertValueEqual(turbine.getProduction(), SteamTurbineBlockEntity.MAX_STEAM_PER_TICK, "EU/t");
            helper.assertValueEqual(start[0] - turbine.getSteam(), 30 * SteamTurbineBlockEntity.MAX_STEAM_PER_TICK,
                    "steam used");
            helper.assertValueEqual(mfe.getEnergy() - start[1], 30 * SteamTurbineBlockEntity.MAX_OUTPUT,
                    "energy delivered");
            helper.setBlock(CONSUMER, Blocks.AIR);
            helper.succeed();
        });
    }

    /** A missing rotor breaks the structure: no steam is used. */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void missingRotorStopsTheTurbine(GameTestHelper helper) {
        SteamTurbineBlockEntity turbine = build(helper);
        helper.setBlock(new BlockPos(3, 2, 3), PpBlocks.TURBINE_CASING.get());
        turbine.getTank().fill(new FluidStack(PpFluids.STEAM.get(), 1000), IFluidHandler.FluidAction.EXECUTE);
        helper.runAtTickTime(20, () -> {
            helper.assertFalse(turbine.isFormed(), "not formed");
            helper.assertValueEqual(turbine.getSteam(), 1000, "steam kept");
            helper.assertValueEqual(turbine.getBuffer(), 0, "no energy");
            helper.succeed();
        });
    }
}
