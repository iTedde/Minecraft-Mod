package dev.craft.buildcraft;

import dev.craft.Craft;
import dev.craft.buildcraft.engine.CombustionEngineBlockEntity;
import dev.craft.buildcraft.engine.EngineBlock;
import dev.craft.buildcraft.engine.EngineBlockEntity;
import dev.craft.buildcraft.engine.EngineType;
import dev.craft.buildcraft.engine.RedstoneEngineBlockEntity;
import dev.craft.buildcraft.engine.StirlingEngineBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for the MJ system and the engines ({@code gradlew runGameTestServer}). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class BuildCraftGameTests {
    private static final String EMPTY = "empty_8x4x8";

    private BuildCraftGameTests() {
    }

    private static <T extends EngineBlockEntity> T place(GameTestHelper helper, BlockPos pos, EngineType type,
                                                         Direction facing) {
        helper.setBlock(pos, BcBlocks.engine(type).get().defaultBlockState().setValue(EngineBlock.FACING, facing));
        return helper.getBlockEntity(pos);
    }

    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void redstoneEngineGainsOneMjPerSecond(GameTestHelper helper) {
        RedstoneEngineBlockEntity engine = place(helper, new BlockPos(1, 1, 1), EngineType.REDSTONE, Direction.UP);
        helper.setBlock(new BlockPos(1, 1, 2), Blocks.REDSTONE_BLOCK);
        helper.runAfterDelay(45, () -> {
            // 45 ticks contain two or three multiples of 20 world ticks.
            helper.assertTrue(engine.getEnergyStored() >= 2 && engine.getEnergyStored() <= 3,
                    "1 MJ per 20 ticks, got " + engine.getEnergyStored());
            helper.assertTrue(engine.isBurning(), "burning while powered");
            helper.assertFalse(engine.isActive(), "no stroke without receiver");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void stirlingEngineBurnsCoalAndFeedsNextEngine(GameTestHelper helper) {
        StirlingEngineBlockEntity stirling = place(helper, new BlockPos(1, 1, 1), EngineType.STIRLING,
                Direction.EAST);
        CombustionEngineBlockEntity receiver = place(helper, new BlockPos(2, 1, 1), EngineType.COMBUSTION,
                Direction.UP);
        helper.setBlock(new BlockPos(1, 1, 2), Blocks.REDSTONE_BLOCK);
        helper.setBlock(new BlockPos(2, 1, 2), Blocks.REDSTONE_BLOCK);
        stirling.getInventory().setStackInSlot(0, new ItemStack(Items.COAL, 2));
        helper.succeedWhen(() -> {
            helper.assertValueEqual(stirling.getTotalBurnTime(), 1600, "coal burn time");
            helper.assertValueEqual(stirling.getInventory().getStackInSlot(0).getCount(), 1, "one coal taken");
            helper.assertTrue(stirling.isActive(), "piston running");
            // Received energy goes through the provider and arrives with 5 % loss.
            helper.assertTrue(receiver.getEnergyStored() >= 20.0F, "chained engine charged");
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void stirlingEngineNeedsRedstoneToBurn(GameTestHelper helper) {
        StirlingEngineBlockEntity stirling = place(helper, new BlockPos(1, 1, 1), EngineType.STIRLING,
                Direction.UP);
        stirling.getInventory().setStackInSlot(0, new ItemStack(Items.COAL));
        helper.runAfterDelay(20, () -> {
            helper.assertValueEqual(stirling.getInventory().getStackInSlot(0).getCount(), 1, "coal kept");
            helper.assertFalse(stirling.isBurning(), "not burning");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void combustionEngineBurnsLavaFromBucket(GameTestHelper helper) {
        CombustionEngineBlockEntity engine = place(helper, new BlockPos(1, 1, 1), EngineType.COMBUSTION,
                Direction.UP);
        helper.setBlock(new BlockPos(1, 1, 2), Blocks.REDSTONE_BLOCK);
        engine.getInventory().setStackInSlot(0, new ItemStack(Items.LAVA_BUCKET));
        helper.runAfterDelay(50, () -> {
            helper.assertTrue(engine.getInventory().getStackInSlot(0).is(Items.BUCKET), "bucket emptied");
            // Lava: 1 MJ/t, 20 ticks per mB.
            int fuel = engine.getFuelTank().getFluidAmount();
            helper.assertTrue(fuel >= 996 && fuel <= 998, "lava used, got " + fuel);
            helper.assertTrue(engine.getHeat() >= 45, "heat rises with the output, got " + engine.getHeat());
            helper.assertTrue(engine.getEnergyStored() >= 45.0F, "energy rises");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void combustionEngineSortsFluidsIntoTanks(GameTestHelper helper) {
        CombustionEngineBlockEntity engine = place(helper, new BlockPos(1, 1, 1), EngineType.COMBUSTION,
                Direction.UP);
        IFluidHandler tanks = engine.getFluidHandler();
        helper.assertValueEqual(tanks.fill(new FluidStack(Fluids.WATER, 3000), IFluidHandler.FluidAction.EXECUTE),
                3000, "water accepted");
        helper.assertValueEqual(tanks.fill(new FluidStack(Fluids.LAVA, 12000), IFluidHandler.FluidAction.EXECUTE),
                10000, "lava up to the tank size");
        helper.assertValueEqual(engine.getCoolantTank().getFluidAmount(), 3000, "coolant tank");
        helper.assertValueEqual(engine.getFuelTank().getFluidAmount(), 10000, "fuel tank");
        helper.assertTrue(tanks.drain(1000, IFluidHandler.FluidAction.EXECUTE).isEmpty(), "no draining");
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void combustionEngineCoolingPenaltyAfterSignalLoss(GameTestHelper helper) {
        CombustionEngineBlockEntity engine = place(helper, new BlockPos(1, 1, 1), EngineType.COMBUSTION,
                Direction.UP);
        BlockPos power = new BlockPos(1, 1, 2);
        helper.setBlock(power, Blocks.REDSTONE_BLOCK);
        engine.fill(new FluidStack(Fluids.LAVA, 1000), IFluidHandler.FluidAction.EXECUTE);
        helper.runAfterDelay(20, () -> helper.setBlock(power, Blocks.AIR));
        helper.runAfterDelay(25, () -> {
            // 600 ticks, counting down only once the remaining heat (about 20) is gone at 10 per tick.
            int penalty = engine.getPenaltyCooling();
            helper.assertTrue(penalty > 590 && penalty <= 600, "cooling penalty, got " + penalty);
            helper.assertFalse(engine.isBurning(), "stopped");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void wrenchTurnsEngineToNextReceiver(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        RedstoneEngineBlockEntity engine = place(helper, pos, EngineType.REDSTONE, Direction.UP);
        place(helper, pos.north(), EngineType.COMBUSTION, Direction.UP);
        place(helper, pos.east(), EngineType.COMBUSTION, Direction.UP);
        engine.switchOrientation();
        helper.assertValueEqual(engine.getOrientation(), Direction.NORTH, "first receiver after up");
        engine.switchOrientation();
        helper.assertValueEqual(engine.getOrientation(), Direction.EAST, "next receiver");
        engine.switchOrientation();
        helper.assertValueEqual(engine.getOrientation(), Direction.NORTH, "wraps around");
        helper.succeed();
    }
}
