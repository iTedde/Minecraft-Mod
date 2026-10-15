package dev.craft.buildcraft;

import dev.craft.Craft;
import dev.craft.buildcraft.engine.CombustionEngineBlockEntity;
import dev.craft.buildcraft.engine.EngineBlock;
import dev.craft.buildcraft.engine.EngineType;
import dev.craft.buildcraft.fluid.BcFluids;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for oil and fuel ({@code gradlew runGameTestServer}). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class OilGameTests {
    private static final String EMPTY = "empty_8x4x8";

    private OilGameTests() {
    }

    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                helper.setBlock(x, 0, z, Blocks.STONE);
            }
        }
    }

    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void oilFlowsWithoutFormingNewSources(GameTestHelper helper) {
        floor(helper);
        // Closed channel x = 1..6 at z = 1.
        for (int x = 0; x < 8; x++) {
            helper.setBlock(x, 1, 0, Blocks.STONE);
            helper.setBlock(x, 1, 2, Blocks.STONE);
        }
        helper.setBlock(0, 1, 1, Blocks.STONE);
        helper.setBlock(7, 1, 1, Blocks.STONE);
        helper.setBlock(1, 1, 1, BcBlocks.OIL.get());
        helper.setBlock(3, 1, 1, BcBlocks.OIL.get());
        helper.runAfterDelay(60, () -> {
            // Between two sources water would form a new source; BuildCraft oil does not.
            FluidState between = helper.getLevel().getFluidState(helper.absolutePos(new BlockPos(2, 1, 1)));
            helper.assertTrue(between.getType() == BcFluids.FLOWING_OIL.get(), "flowing oil between the sources");
            helper.assertValueEqual(between.getAmount(), 7, "one level below a source");
            FluidState far = helper.getLevel().getFluidState(helper.absolutePos(new BlockPos(6, 1, 1)));
            helper.assertValueEqual(far.getAmount(), 5, "three blocks from the second source");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void emptyBucketPicksUpOilSource(GameTestHelper helper) {
        floor(helper);
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, BcBlocks.OIL.get());
        LiquidBlock block = BcBlocks.OIL.get();
        ItemStack bucket = block.pickupBlock(null, helper.getLevel(), helper.absolutePos(pos),
                helper.getBlockState(pos));
        helper.assertTrue(bucket.is(BcItems.OIL_BUCKET.get()), "oil bucket");
        helper.assertBlockPresent(Blocks.AIR, pos);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void bucketsAreFluidContainers(GameTestHelper helper) {
        helper.assertTrue(FluidStack.matches(FluidUtil.getFluidContained(new ItemStack(BcItems.OIL_BUCKET.get()))
                .orElseThrow(), new FluidStack(BcFluids.OIL.get(), 1000)), "oil bucket holds 1000 mB oil");
        helper.assertTrue(FluidStack.matches(FluidUtil.getFluidContained(new ItemStack(BcItems.FUEL_BUCKET.get()))
                .orElseThrow(), new FluidStack(BcFluids.FUEL.get(), 1000)), "fuel bucket holds 1000 mB fuel");
        helper.assertValueEqual(new ItemStack(BcItems.FUEL_BUCKET.get()).getMaxStackSize(), 1, "fuel bucket stack");
        helper.assertValueEqual(new ItemStack(BcItems.OIL_BUCKET.get()).getMaxStackSize(), 1, "oil bucket stack");
        helper.assertTrue(new ItemStack(BcFluids.FUEL.get().getBucket()).is(BcItems.FUEL_BUCKET.get()),
                "filled bucket of fuel");
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 60)
    public static void combustionEngineTakesOilAndFuelBuckets(GameTestHelper helper) {
        BlockPos oilPos = new BlockPos(1, 1, 1);
        BlockPos fuelPos = new BlockPos(3, 1, 1);
        helper.setBlock(oilPos, BcBlocks.engine(EngineType.COMBUSTION).get().defaultBlockState()
                .setValue(EngineBlock.FACING, Direction.UP));
        helper.setBlock(fuelPos, BcBlocks.engine(EngineType.COMBUSTION).get().defaultBlockState()
                .setValue(EngineBlock.FACING, Direction.UP));
        CombustionEngineBlockEntity oilEngine = helper.getBlockEntity(oilPos);
        CombustionEngineBlockEntity fuelEngine = helper.getBlockEntity(fuelPos);
        oilEngine.getInventory().setStackInSlot(0, new ItemStack(BcItems.OIL_BUCKET.get()));
        fuelEngine.getInventory().setStackInSlot(0, new ItemStack(BcItems.FUEL_BUCKET.get()));
        helper.succeedWhen(() -> {
            helper.assertTrue(oilEngine.getInventory().getStackInSlot(0).is(Items.BUCKET), "oil bucket emptied");
            helper.assertTrue(fuelEngine.getInventory().getStackInSlot(0).is(Items.BUCKET), "fuel bucket emptied");
            helper.assertValueEqual(oilEngine.getFuelTank().getFluidAmount(), 1000, "oil in tank");
            helper.assertValueEqual(fuelEngine.getFuelTank().getFluidAmount(), 1000, "fuel in tank");
        });
    }

    @GameTest(template = EMPTY)
    public static void waterIsRejectedAsFuel(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, BcBlocks.engine(EngineType.COMBUSTION).get().defaultBlockState()
                .setValue(EngineBlock.FACING, Direction.UP));
        CombustionEngineBlockEntity engine = helper.getBlockEntity(pos);
        IFluidHandler tanks = engine.getFluidHandler();
        helper.assertValueEqual(tanks.fill(new FluidStack(BcFluids.FUEL.get(), 500), IFluidHandler.FluidAction.EXECUTE),
                500, "fuel accepted");
        helper.assertValueEqual(tanks.fill(new FluidStack(BcFluids.OIL.get(), 500), IFluidHandler.FluidAction.EXECUTE),
                0, "only one fuel at a time");
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void lavaNextToOilHardens(GameTestHelper helper) {
        floor(helper);
        for (int x = 0; x < 5; x++) {
            helper.setBlock(x, 1, 0, Blocks.STONE);
            helper.setBlock(x, 1, 2, Blocks.STONE);
        }
        helper.setBlock(0, 1, 1, Blocks.STONE);
        helper.setBlock(4, 1, 1, Blocks.STONE);
        helper.setBlock(1, 1, 1, Blocks.LAVA);
        helper.setBlock(2, 1, 1, BcBlocks.OIL.get());
        helper.succeedWhen(() -> helper.assertBlockPresent(Blocks.OBSIDIAN, new BlockPos(1, 1, 1)));
    }
}
