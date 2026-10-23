package dev.craft.buildcraft;

import dev.craft.Craft;
import dev.craft.buildcraft.factory.MiningWellBlockEntity;
import dev.craft.buildcraft.factory.PumpBlockEntity;
import dev.craft.buildcraft.factory.RefineryBlockEntity;
import dev.craft.buildcraft.factory.TankBlock;
import dev.craft.buildcraft.factory.TankBlockEntity;
import dev.craft.buildcraft.fluid.BcFluids;
import dev.craft.buildcraft.transport.FluidPipeBlockEntity;
import dev.craft.buildcraft.transport.PipeType;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for tank, pump, refinery and mining well ({@code gradlew runGameTestServer}). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class FactoryGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final String LARGE = "empty_16x8x16";

    private FactoryGameTests() {
    }

    @GameTest(template = EMPTY)
    public static void tankStackFillsFromBottomAndPoursDown(GameTestHelper helper) {
        BlockPos bottomPos = new BlockPos(2, 1, 2);
        // Top first: placing the bottom tank updates the shape of the one above (JOINED).
        helper.setBlock(bottomPos.above(), BcBlocks.TANK.get());
        helper.setBlock(bottomPos, BcBlocks.TANK.get());
        TankBlockEntity bottom = helper.getBlockEntity(bottomPos);
        TankBlockEntity top = helper.getBlockEntity(bottomPos.above());
        int filled = top.fill(new FluidStack(Fluids.WATER, 20000), IFluidHandler.FluidAction.EXECUTE);
        helper.assertValueEqual(filled, 20000, "filled");
        helper.assertValueEqual(bottom.getTank().getFluidAmount(), 16000, "bottom full");
        helper.assertValueEqual(top.getTank().getFluidAmount(), 4000, "rest on top");
        helper.assertValueEqual(top.getCompositeFluid().getAmount(), 20000, "composite amount");
        helper.assertValueEqual(top.getCompositeCapacity(), 32000, "composite capacity");
        helper.assertValueEqual(top.fill(new FluidStack(Fluids.LAVA, 1000), IFluidHandler.FluidAction.EXECUTE), 0,
                "other liquid refused");
        FluidStack drained = top.drain(1000, IFluidHandler.FluidAction.EXECUTE);
        helper.assertValueEqual(drained.getAmount(), 1000, "drained");
        helper.assertValueEqual(bottom.getTank().getFluidAmount(), 15000, "drained from the bottom");
        helper.runAfterDelay(2, () -> {
            helper.assertValueEqual(bottom.getTank().getFluidAmount(), 16000, "top poured into the bottom");
            helper.assertValueEqual(top.getTank().getFluidAmount(), 3000, "top lost the poured amount");
            helper.assertBlockProperty(bottomPos.above(), TankBlock.JOINED, true);
            helper.assertBlockProperty(bottomPos, TankBlock.JOINED, false);
            helper.succeed();
        });
    }

    @GameTest(template = LARGE, timeoutTicks = 1200)
    public static void pumpLowersTubeAndPumpsLavaSources(GameTestHelper helper) {
        int poolY = 2;
        for (int x = 3; x <= 7; x++) {
            for (int z = 3; z <= 7; z++) {
                helper.setBlock(new BlockPos(x, poolY - 1, z), Blocks.STONE);
                boolean inner = x >= 4 && x <= 6 && z >= 4 && z <= 6;
                helper.setBlock(new BlockPos(x, poolY, z), inner ? Blocks.LAVA : Blocks.STONE);
            }
        }
        BlockPos pumpPos = new BlockPos(5, poolY + 2, 5);
        helper.setBlock(pumpPos, BcBlocks.PUMP.get());
        helper.setBlock(pumpPos.above(), BcBlocks.TANK.get());
        PumpBlockEntity pump = helper.getBlockEntity(pumpPos);
        TankBlockEntity tank = helper.getBlockEntity(pumpPos.above());
        helper.onEachTick(() -> pump.getPowerProvider().setEnergyStored(100.0F));
        helper.succeedWhen(() -> {
            helper.assertTrue(tank.getTank().getFluidAmount() >= 3000, "three buckets pumped");
            helper.assertTrue(tank.getTank().getFluid().is(Fluids.LAVA), "lava in the tank");
            int sources = 0;
            for (int x = 4; x <= 6; x++) {
                for (int z = 4; z <= 6; z++) {
                    if (helper.getLevel().getFluidState(helper.absolutePos(new BlockPos(x, poolY, z))).isSource()) {
                        sources++;
                    }
                }
            }
            helper.assertValueEqual(sources + tank.getTank().getFluidAmount() / 1000, 9, "sources removed");
        });
    }

    /** A 3×3 water pool at y 2 with the pump two blocks above its middle. */
    private static PumpBlockEntity pumpOverWater(GameTestHelper helper, BlockPos pumpPos) {
        for (int x = 3; x <= 7; x++) {
            for (int z = 3; z <= 7; z++) {
                helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
                boolean inner = x >= 4 && x <= 6 && z >= 4 && z <= 6;
                helper.setBlock(new BlockPos(x, 2, z), inner ? Blocks.WATER : Blocks.STONE);
            }
        }
        helper.setBlock(pumpPos, BcBlocks.PUMP.get());
        PumpBlockEntity pump = helper.getBlockEntity(pumpPos);
        helper.onEachTick(() -> pump.getPowerProvider().setEnergyStored(100.0F));
        return pump;
    }

    @GameTest(template = LARGE, timeoutTicks = 1200)
    public static void pumpPushesIntoFluidPipe(GameTestHelper helper) {
        BlockPos pumpPos = new BlockPos(5, 4, 5);
        pumpOverWater(helper, pumpPos);
        helper.setBlock(pumpPos.above(), BcBlocks.pipe(PipeType.COBBLESTONE_FLUID).get());
        helper.setBlock(pumpPos.above(2), BcBlocks.TANK.get());
        TankBlockEntity tank = helper.getBlockEntity(pumpPos.above(2));
        helper.succeedWhen(() -> helper.assertTrue(tank.getTank().getFluidAmount() >= 2000,
                "water through the pipe, got " + tank.getTank().getFluidAmount()));
    }

    @GameTest(template = LARGE, timeoutTicks = 1200)
    public static void woodenFluidPipeExtractsFromPump(GameTestHelper helper) {
        BlockPos pumpPos = new BlockPos(5, 4, 5);
        pumpOverWater(helper, pumpPos);
        BlockPos wood = pumpPos.east();
        helper.setBlock(wood, BcBlocks.pipe(PipeType.WOODEN_FLUID).get());
        helper.setBlock(wood.east(), BcBlocks.pipe(PipeType.COBBLESTONE_FLUID).get());
        helper.setBlock(wood.east(2), BcBlocks.TANK.get());
        TankBlockEntity tank = helper.getBlockEntity(wood.east(2));
        helper.onEachTick(() -> {
            if (helper.getBlockEntity(wood) instanceof FluidPipeBlockEntity pipe && pipe.getPowerProvider() != null) {
                pipe.getPowerProvider().setEnergyStored(1.0F);
            }
        });
        helper.succeedWhen(() -> helper.assertTrue(tank.getTank().getFluidAmount() >= 2000,
                "water through the wooden pipe, got " + tank.getTank().getFluidAmount()));
    }

    @GameTest(template = EMPTY)
    public static void refineryTurnsOilIntoFuel(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, BcBlocks.REFINERY.get());
        RefineryBlockEntity refinery = helper.getBlockEntity(pos);
        helper.assertValueEqual(refinery.fill(new FluidStack(BcFluids.OIL.get(), 1000),
                IFluidHandler.FluidAction.EXECUTE), 1000, "oil accepted");
        refinery.getPowerProvider().setEnergyStored(1000.0F);
        helper.runAfterDelay(10, () -> {
            int fuel = refinery.getResult().getFluidAmount();
            helper.assertTrue(fuel > 0 && fuel <= 11, "about one mB per tick, got " + fuel);
            helper.assertTrue(refinery.getResult().getFluid().is(BcFluids.FUEL.get()), "fuel produced");
            helper.assertValueEqual(refinery.getIngredient1().getFluidAmount(), 1000 - fuel, "oil used 1:1");
            float used = 1000.0F - refinery.getPowerProvider().getEnergyStored();
            helper.assertTrue(used - fuel * 12 >= 0.0F && used - fuel * 12 <= 1.0F,
                    "12 MJ per mB (plus at most 1 MJ loss), used " + used);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void refineryFiltersRouteLiquids(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos, BcBlocks.REFINERY.get());
        RefineryBlockEntity refinery = helper.getBlockEntity(pos);
        refinery.setFilter(1, BcFluids.OIL.get());
        helper.assertValueEqual(refinery.fill(new FluidStack(Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE),
                0, "unfiltered liquid refused");
        helper.assertValueEqual(refinery.fill(new FluidStack(BcFluids.OIL.get(), 5000),
                IFluidHandler.FluidAction.EXECUTE), 4000, "oil only into ingredient 2");
        helper.assertTrue(refinery.getIngredient1().isEmpty(), "ingredient 1 untouched");
        helper.assertValueEqual(refinery.getFluidHandler().drain(1000, IFluidHandler.FluidAction.EXECUTE).getAmount(),
                0, "only the result can be drained");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void miningWellDigsDownAndPipesVanish(GameTestHelper helper) {
        BlockPos wellPos = new BlockPos(2, 3, 2);
        helper.setBlock(new BlockPos(2, 0, 2), Blocks.BEDROCK);
        helper.setBlock(new BlockPos(2, 1, 2), Blocks.STONE);
        helper.setBlock(new BlockPos(2, 2, 2), Blocks.STONE);
        helper.setBlock(wellPos.east(), Blocks.CHEST);
        helper.setBlock(wellPos, BcBlocks.MINING_WELL.get());
        MiningWellBlockEntity well = helper.getBlockEntity(wellPos);
        well.getPowerProvider().setEnergyStored(1000.0F);
        helper.runAfterDelay(5, () -> {
            helper.assertBlockPresent(BcBlocks.MINING_PIPE.get(), new BlockPos(2, 2, 2));
            helper.assertBlockPresent(BcBlocks.MINING_PIPE.get(), new BlockPos(2, 1, 2));
            helper.assertBlockPresent(Blocks.BEDROCK, new BlockPos(2, 0, 2));
            helper.assertFalse(well.isDigging(), "stopped at bedrock");
            ChestBlockEntity chest = helper.getBlockEntity(wellPos.east());
            int cobble = 0;
            for (int i = 0; i < chest.getContainerSize(); i++) {
                if (chest.getItem(i).is(Items.COBBLESTONE)) {
                    cobble += chest.getItem(i).getCount();
                }
            }
            helper.assertValueEqual(cobble, 2, "drops in the chest");
            helper.destroyBlock(wellPos);
            helper.assertBlockPresent(Blocks.AIR, new BlockPos(2, 2, 2));
            helper.assertBlockPresent(Blocks.AIR, new BlockPos(2, 1, 2));
            helper.succeed();
        });
    }
}
