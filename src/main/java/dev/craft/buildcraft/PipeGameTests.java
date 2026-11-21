package dev.craft.buildcraft;

import dev.craft.Craft;
import dev.craft.buildcraft.factory.TankBlockEntity;
import dev.craft.buildcraft.transport.FluidPipeBlockEntity;
import dev.craft.buildcraft.transport.GenericPipeBlock;
import dev.craft.buildcraft.transport.ItemPipeBlockEntity;
import dev.craft.buildcraft.transport.PipeConnections;
import dev.craft.buildcraft.transport.PipeType;
import dev.craft.buildcraft.transport.TravelingItem;
import dev.craft.buildcraft.util.ItemOutput;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.machine.InductionFurnaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PipeBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for the item pipes ({@code gradlew runGameTestServer}). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class PipeGameTests {
    private static final String EMPTY = "empty_8x4x8";

    private PipeGameTests() {
    }

    private static void pipe(GameTestHelper helper, BlockPos pos, PipeType type) {
        helper.setBlock(pos, BcBlocks.pipe(type).get());
    }

    private static void assertStack(GameTestHelper helper, ItemStack actual, Item item, int count, String name) {
        helper.assertTrue(actual.is(item) && actual.getCount() == count,
                name + ": expected " + count + " " + item + ", got " + actual);
    }

    /** Puts an item at the pipe's edge facing {@code from}, moving into the pipe. */
    private static TravelingItem inject(GameTestHelper helper, BlockPos pos, Direction from, ItemStack stack) {
        BlockPos abs = helper.absolutePos(pos);
        TravelingItem item = new TravelingItem(stack, abs.getX() + 0.5 + from.getStepX() * 0.5,
                abs.getY() + ItemPipeBlockEntity.PIPE_FLOOR + from.getStepY() * 0.5,
                abs.getZ() + 0.5 + from.getStepZ() * 0.5);
        ((ItemPipeBlockEntity) helper.getBlockEntity(pos)).entityEntering(item, from.getOpposite());
        return item;
    }

    @GameTest(template = EMPTY)
    public static void connectionRulesFollowMaterials(GameTestHelper helper) {
        pipe(helper, new BlockPos(1, 1, 1), PipeType.WOODEN_TRANSPORT);
        pipe(helper, new BlockPos(2, 1, 1), PipeType.WOODEN_TRANSPORT);
        pipe(helper, new BlockPos(1, 1, 3), PipeType.COBBLESTONE_TRANSPORT);
        pipe(helper, new BlockPos(2, 1, 3), PipeType.STONE_TRANSPORT);
        pipe(helper, new BlockPos(3, 1, 3), PipeType.GOLDEN_TRANSPORT);
        helper.setBlock(new BlockPos(1, 1, 5), Blocks.CHEST);
        pipe(helper, new BlockPos(2, 1, 5), PipeType.SANDSTONE_TRANSPORT);
        pipe(helper, new BlockPos(3, 1, 5), PipeType.IRON_TRANSPORT);
        helper.setBlock(new BlockPos(4, 1, 5), Blocks.CHEST);
        helper.setBlock(new BlockPos(1, 1, 6), Blocks.STONE);
        helper.runAfterDelay(3, () -> {
            helper.assertBlockProperty(new BlockPos(1, 1, 1), PipeBlock.EAST, false);
            helper.assertBlockProperty(new BlockPos(1, 1, 3), PipeBlock.EAST, false);
            helper.assertBlockProperty(new BlockPos(2, 1, 3), PipeBlock.EAST, true);
            helper.assertBlockProperty(new BlockPos(2, 1, 5), PipeBlock.WEST, false);
            helper.assertBlockProperty(new BlockPos(2, 1, 5), PipeBlock.EAST, true);
            helper.assertBlockProperty(new BlockPos(3, 1, 5), PipeBlock.EAST, true);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 800)
    public static void woodenPipeExtractsIntoChest(GameTestHelper helper) {
        BlockPos source = new BlockPos(1, 1, 2);
        BlockPos target = new BlockPos(5, 1, 2);
        helper.setBlock(source, Blocks.CHEST);
        helper.setBlock(target, Blocks.CHEST);
        ((ChestBlockEntity) helper.getBlockEntity(source)).setItem(0, new ItemStack(Items.COBBLESTONE, 10));
        pipe(helper, new BlockPos(2, 1, 2), PipeType.WOODEN_TRANSPORT);
        pipe(helper, new BlockPos(3, 1, 2), PipeType.COBBLESTONE_TRANSPORT);
        pipe(helper, new BlockPos(4, 1, 2), PipeType.COBBLESTONE_TRANSPORT);
        helper.runAfterDelay(3, () -> {
            helper.assertBlockProperty(new BlockPos(2, 1, 2), GenericPipeBlock.SOURCE,
                    Direction.WEST.get3DDataValue());
            ItemPipeBlockEntity wood = helper.getBlockEntity(new BlockPos(2, 1, 2));
            wood.getPowerProvider().setEnergyStored(64.0F);
        });
        helper.succeedWhen(() -> {
            ChestBlockEntity chest = helper.getBlockEntity(target);
            helper.assertValueEqual(chest.getItem(0).getCount(), 10, "items in target chest");
            helper.assertTrue(((ChestBlockEntity) helper.getBlockEntity(source)).isEmpty(), "source emptied");
        });
    }

    /** The wooden pipe empties the second (right) output of the induction furnace from a side. */
    @GameTest(template = EMPTY, timeoutTicks = 800)
    public static void woodenPipeExtractsSecondFurnaceOutput(GameTestHelper helper) {
        BlockPos source = new BlockPos(1, 1, 2);
        BlockPos target = new BlockPos(5, 1, 2);
        helper.setBlock(source, Ic2Blocks.machine(Ic2Machine.INDUCTION_FURNACE).get());
        helper.setBlock(target, Blocks.CHEST);
        InductionFurnaceBlockEntity furnace = helper.getBlockEntity(source);
        furnace.getInventory().set(InductionFurnaceBlockEntity.SLOT_OUTPUT_2, new ItemStack(Items.GLASS, 10));
        pipe(helper, new BlockPos(2, 1, 2), PipeType.WOODEN_TRANSPORT);
        pipe(helper, new BlockPos(3, 1, 2), PipeType.COBBLESTONE_TRANSPORT);
        pipe(helper, new BlockPos(4, 1, 2), PipeType.COBBLESTONE_TRANSPORT);
        helper.runAfterDelay(3, () -> {
            helper.assertBlockProperty(new BlockPos(2, 1, 2), GenericPipeBlock.SOURCE,
                    Direction.WEST.get3DDataValue());
            ItemPipeBlockEntity wood = helper.getBlockEntity(new BlockPos(2, 1, 2));
            wood.getPowerProvider().setEnergyStored(64.0F);
        });
        helper.succeedWhen(() -> {
            ChestBlockEntity chest = helper.getBlockEntity(target);
            assertStack(helper, chest.getItem(0), Items.GLASS, 10, "glass in target chest");
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void voidPipeDestroysItems(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        pipe(helper, pos, PipeType.VOID_TRANSPORT);
        helper.runAfterDelay(2, () -> inject(helper, pos, Direction.NORTH, new ItemStack(Items.DIRT, 5)));
        helper.runAfterDelay(80, () -> {
            helper.assertTrue(((ItemPipeBlockEntity) helper.getBlockEntity(pos)).getItems().isEmpty(), "pipe empty");
            helper.assertEntityNotPresent(EntityType.ITEM);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 200)
    public static void ironPipeSendsOnlyToOutput(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        BlockPos west = pos.west();
        BlockPos east = pos.east();
        helper.setBlock(west, Blocks.CHEST);
        helper.setBlock(east, Blocks.CHEST);
        pipe(helper, pos, PipeType.IRON_TRANSPORT);
        helper.runAfterDelay(2, () -> {
            // From DOWN round the sides: up, north, south have nothing, west has the chest.
            PipeConnections.switchIronOutput(helper.getLevel(), helper.absolutePos(pos));
            helper.assertBlockProperty(pos, GenericPipeBlock.OUTPUT, Direction.WEST.get3DDataValue());
            for (int i = 0; i < 4; i++) {
                inject(helper, pos, Direction.NORTH, new ItemStack(Items.DIRT));
            }
        });
        helper.runAfterDelay(120, () -> {
            helper.assertValueEqual(((ChestBlockEntity) helper.getBlockEntity(west)).getItem(0).getCount(), 4,
                    "west chest");
            helper.assertTrue(((ChestBlockEntity) helper.getBlockEntity(east)).isEmpty(), "east chest empty");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void goldenPipeQuadruplesSpeed(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        pipe(helper, pos, PipeType.GOLDEN_TRANSPORT);
        helper.runAfterDelay(2, () -> {
            TravelingItem item = inject(helper, pos, Direction.NORTH, new ItemStack(Items.DIRT));
            BlockPos abs = helper.absolutePos(pos);
            double start = item.getZ(1.0F);
            ((ItemPipeBlockEntity) helper.getBlockEntity(pos)).tick();
            helper.assertTrue(Math.abs(item.getZ(1.0F) - start - 0.04) < 1.0E-6, "moved 0.04 per tick");
            helper.assertTrue(abs.getZ() < item.getZ(1.0F), "inside the pipe");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 300)
    public static void miningWellOutputsIntoPipe(GameTestHelper helper) {
        BlockPos well = new BlockPos(2, 1, 2);
        BlockPos chest = new BlockPos(4, 1, 2);
        helper.setBlock(well, BcBlocks.MINING_WELL.get());
        pipe(helper, new BlockPos(3, 1, 2), PipeType.COBBLESTONE_TRANSPORT);
        helper.setBlock(chest, Blocks.CHEST);
        helper.runAfterDelay(2, () -> ItemOutput.output(helper.getLevel(), helper.absolutePos(well),
                new ItemStack(Items.COAL, 3)));
        helper.succeedWhen(() -> helper.assertValueEqual(
                ((ChestBlockEntity) helper.getBlockEntity(chest)).getItem(0).getCount(), 3, "coal in chest"));
    }

    @GameTest(template = EMPTY, timeoutTicks = 300)
    public static void diamondPipeSortsByFilter(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos.west(), Blocks.CHEST);
        helper.setBlock(pos.east(), Blocks.CHEST);
        helper.setBlock(pos.south(), Blocks.CHEST);
        pipe(helper, pos, PipeType.DIAMOND_TRANSPORT);
        ItemPipeBlockEntity diamond = helper.getBlockEntity(pos);
        diamond.getFilters().setStackInSlot(Direction.WEST.get3DDataValue() * 9 + 4, new ItemStack(Items.DIRT));
        diamond.getFilters().setStackInSlot(Direction.EAST.get3DDataValue() * 9, new ItemStack(Items.STONE));
        helper.runAfterDelay(2, () -> {
            for (int i = 0; i < 3; i++) {
                inject(helper, pos, Direction.NORTH, new ItemStack(Items.DIRT));
                inject(helper, pos, Direction.NORTH, new ItemStack(Items.STONE));
                inject(helper, pos, Direction.NORTH, new ItemStack(Items.COBBLESTONE));
            }
        });
        helper.runAfterDelay(150, () -> {
            assertStack(helper, ((ChestBlockEntity) helper.getBlockEntity(pos.west())).getItem(0),
                    Items.DIRT, 3, "west chest");
            assertStack(helper, ((ChestBlockEntity) helper.getBlockEntity(pos.east())).getItem(0),
                    Items.STONE, 3, "east chest");
            assertStack(helper, ((ChestBlockEntity) helper.getBlockEntity(pos.south())).getItem(0),
                    Items.COBBLESTONE, 3, "south chest");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 800)
    public static void emeraldPipeExtractsOnlyFilteredItems(GameTestHelper helper) {
        BlockPos source = new BlockPos(1, 1, 2);
        BlockPos target = new BlockPos(4, 1, 2);
        helper.setBlock(source, Blocks.CHEST);
        helper.setBlock(target, Blocks.CHEST);
        ChestBlockEntity sourceChest = helper.getBlockEntity(source);
        sourceChest.setItem(0, new ItemStack(Items.DIRT, 5));
        sourceChest.setItem(1, new ItemStack(Items.COBBLESTONE, 5));
        pipe(helper, new BlockPos(2, 1, 2), PipeType.EMERALD_TRANSPORT);
        pipe(helper, new BlockPos(3, 1, 2), PipeType.COBBLESTONE_TRANSPORT);
        ItemPipeBlockEntity emerald = helper.getBlockEntity(new BlockPos(2, 1, 2));
        emerald.getFilters().setStackInSlot(3, new ItemStack(Items.COBBLESTONE));
        helper.onEachTick(() -> emerald.getPowerProvider().setEnergyStored(64.0F));
        helper.runAfterDelay(200, () -> {
            assertStack(helper, ((ChestBlockEntity) helper.getBlockEntity(target)).getItem(0),
                    Items.COBBLESTONE, 5, "target chest");
            assertStack(helper, sourceChest.getItem(0), Items.DIRT, 5, "dirt stays");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 300)
    public static void obsidianPipeSucksUpItems(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos.west(), Blocks.CHEST);
        pipe(helper, pos, PipeType.OBSIDIAN_TRANSPORT);
        ItemPipeBlockEntity obsidian = helper.getBlockEntity(pos);
        helper.onEachTick(() -> obsidian.getPowerProvider().setEnergyStored(256.0F));
        helper.runAfterDelay(3, () -> {
            helper.assertValueEqual(obsidian.getOpenOrientation(), Direction.EAST, "open side");
            helper.spawnItem(Items.DIRT, 4.5F, 1.2F, 2.5F);
        });
        helper.succeedWhen(() -> {
            assertStack(helper, ((ChestBlockEntity) helper.getBlockEntity(pos.west())).getItem(0),
                    Items.DIRT, 1, "chest");
            helper.assertEntityNotPresent(EntityType.ITEM);
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 300)
    public static void obsidianPipeTakesTouchingItemsForFree(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        helper.setBlock(pos.west(), Blocks.CHEST);
        pipe(helper, pos, PipeType.OBSIDIAN_TRANSPORT);
        helper.runAfterDelay(3, () -> helper.spawnItem(Items.DIRT, 2.5F, 2.0F, 2.5F));
        helper.succeedWhen(() -> assertStack(helper, ((ChestBlockEntity) helper.getBlockEntity(pos.west())).getItem(0),
                Items.DIRT, 1, "chest"));
    }

    // --- fluid pipes ---

    private static FluidStack water(int amount) {
        return new FluidStack(Fluids.WATER, amount);
    }

    @GameTest(template = EMPTY, timeoutTicks = 1200)
    public static void woodenFluidPipeMovesWaterIntoTank(GameTestHelper helper) {
        BlockPos source = new BlockPos(1, 1, 2);
        BlockPos target = new BlockPos(5, 1, 2);
        helper.setBlock(source, BcBlocks.TANK.get());
        helper.setBlock(target, BcBlocks.TANK.get());
        ((TankBlockEntity) helper.getBlockEntity(source)).fill(water(1000), IFluidHandler.FluidAction.EXECUTE);
        pipe(helper, new BlockPos(2, 1, 2), PipeType.WOODEN_FLUID);
        pipe(helper, new BlockPos(3, 1, 2), PipeType.COBBLESTONE_FLUID);
        pipe(helper, new BlockPos(4, 1, 2), PipeType.COBBLESTONE_FLUID);
        helper.runAfterDelay(3, () -> {
            helper.assertBlockProperty(new BlockPos(2, 1, 2), GenericPipeBlock.SOURCE,
                    Direction.WEST.get3DDataValue());
            FluidPipeBlockEntity wood = helper.getBlockEntity(new BlockPos(2, 1, 2));
            // 1 MJ = 1000 mB of extraction budget.
            wood.getPowerProvider().setEnergyStored(1.0F);
        });
        helper.succeedWhen(() -> {
            helper.assertValueEqual(((TankBlockEntity) helper.getBlockEntity(target)).getTank().getFluidAmount(),
                    1000, "water in target tank");
            helper.assertTrue(((TankBlockEntity) helper.getBlockEntity(source)).getTank().isEmpty(),
                    "source emptied");
        });
    }

    @GameTest(template = EMPTY)
    public static void fluidPipesOnlyConnectToFluidPipesAndTanks(GameTestHelper helper) {
        pipe(helper, new BlockPos(1, 1, 1), PipeType.COBBLESTONE_FLUID);
        pipe(helper, new BlockPos(2, 1, 1), PipeType.COBBLESTONE_TRANSPORT);
        helper.setBlock(new BlockPos(1, 1, 0), Blocks.CHEST);
        helper.setBlock(new BlockPos(1, 1, 3), BcBlocks.TANK.get());
        pipe(helper, new BlockPos(2, 1, 3), PipeType.SANDSTONE_FLUID);
        pipe(helper, new BlockPos(3, 1, 3), PipeType.GOLDEN_FLUID);
        helper.setBlock(new BlockPos(4, 1, 3), BcBlocks.TANK.get());
        helper.runAfterDelay(3, () -> {
            helper.assertBlockProperty(new BlockPos(1, 1, 1), PipeBlock.EAST, false);
            helper.assertBlockProperty(new BlockPos(1, 1, 1), PipeBlock.NORTH, false);
            helper.assertBlockProperty(new BlockPos(2, 1, 3), PipeBlock.WEST, false);
            helper.assertBlockProperty(new BlockPos(2, 1, 3), PipeBlock.EAST, true);
            helper.assertBlockProperty(new BlockPos(3, 1, 3), PipeBlock.EAST, true);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void fluidPipeSectionsLimitFlowPerTick(GameTestHelper helper) {
        BlockPos cobble = new BlockPos(1, 1, 1);
        BlockPos gold = new BlockPos(3, 1, 1);
        BlockPos sandstone = new BlockPos(5, 1, 1);
        BlockPos voidPipe = new BlockPos(1, 1, 3);
        pipe(helper, cobble, PipeType.COBBLESTONE_FLUID);
        pipe(helper, gold, PipeType.GOLDEN_FLUID);
        pipe(helper, sandstone, PipeType.SANDSTONE_FLUID);
        pipe(helper, voidPipe, PipeType.VOID_FLUID);
        helper.runAfterDelay(3, () -> {
            FluidPipeBlockEntity cobblePipe = helper.getBlockEntity(cobble);
            helper.assertValueEqual(cobblePipe.getFluidHandler(Direction.UP)
                    .fill(water(1000), IFluidHandler.FluidAction.EXECUTE), 10, "cobblestone per tick");
            helper.assertValueEqual(cobblePipe.getTransferState(Direction.UP),
                    FluidPipeBlockEntity.TransferState.INPUT, "filled side is an input");
            FluidPipeBlockEntity goldPipe = helper.getBlockEntity(gold);
            helper.assertValueEqual(goldPipe.getFluidHandler(Direction.UP)
                    .fill(water(1000), IFluidHandler.FluidAction.EXECUTE), 40, "gold per tick");
            helper.assertValueEqual(goldPipe.getFluidHandler(Direction.UP)
                    .fill(water(1000), IFluidHandler.FluidAction.EXECUTE), 0, "gold limit reached");
            FluidPipeBlockEntity sandstonePipe = helper.getBlockEntity(sandstone);
            helper.assertValueEqual(sandstonePipe.getFluidHandler(Direction.UP)
                    .fill(water(1000), IFluidHandler.FluidAction.EXECUTE), 0, "sandstone only from pipes");
            FluidPipeBlockEntity voidPipeEntity = helper.getBlockEntity(voidPipe);
            helper.assertValueEqual(voidPipeEntity.getFluidHandler(Direction.UP)
                    .fill(water(1000), IFluidHandler.FluidAction.EXECUTE), 1000, "void takes everything");
            helper.assertTrue(voidPipeEntity.getFluid(Direction.UP.get3DDataValue()).isEmpty(), "void keeps nothing");
            helper.assertTrue(cobblePipe.getFluidHandler(Direction.UP).drain(10, IFluidHandler.FluidAction.EXECUTE)
                    .isEmpty(), "no draining");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 400)
    public static void ironFluidPipeOnlyOutputsToItsOutputSide(GameTestHelper helper) {
        BlockPos iron = new BlockPos(2, 1, 2);
        helper.setBlock(iron.west(), BcBlocks.TANK.get());
        helper.setBlock(iron.east(), BcBlocks.TANK.get());
        pipe(helper, iron, PipeType.IRON_FLUID);
        FluidPipeBlockEntity pipe = helper.getBlockEntity(iron);
        helper.onEachTick(() -> pipe.getFluidHandler(Direction.UP).fill(water(10), IFluidHandler.FluidAction.EXECUTE));
        // From output 0 (down) the switch skips up, north and south and stops at the west tank.
        helper.runAfterDelay(3, () -> PipeConnections.switchIronOutput(helper.getLevel(), helper.absolutePos(iron)));
        helper.runAfterDelay(200, () -> {
            helper.assertBlockProperty(iron, GenericPipeBlock.OUTPUT, Direction.WEST.get3DDataValue());
            helper.assertTrue(((TankBlockEntity) helper.getBlockEntity(iron.west())).getTank().getFluidAmount() > 0,
                    "output tank filled");
            helper.assertTrue(((TankBlockEntity) helper.getBlockEntity(iron.east())).getTank().isEmpty(),
                    "other tank empty");
            helper.succeed();
        });
    }
}
