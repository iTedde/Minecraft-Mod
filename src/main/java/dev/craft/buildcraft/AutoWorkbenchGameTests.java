package dev.craft.buildcraft;

import dev.craft.Craft;
import dev.craft.buildcraft.factory.AutoWorkbenchBlockEntity;
import dev.craft.buildcraft.transport.GenericPipeBlock;
import dev.craft.buildcraft.transport.ItemPipeBlockEntity;
import dev.craft.buildcraft.transport.PipeType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;

/** Automatic crafting table (spec 20 §2): pattern stays, neighbour supply, round-robin insertion, wooden pipe. */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class AutoWorkbenchGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos BENCH = new BlockPos(2, 1, 2);

    private AutoWorkbenchGameTests() {
    }

    private static AutoWorkbenchBlockEntity bench(GameTestHelper helper) {
        helper.setBlock(BENCH, BcBlocks.AUTO_WORKBENCH.get());
        return helper.getBlockEntity(BENCH);
    }

    /** Sticks: planks in grid slots 0 and 3. */
    private static void sticks(ItemStackHandler grid, int count) {
        grid.setStackInSlot(0, new ItemStack(Items.OAK_PLANKS, count));
        grid.setStackInSlot(3, new ItemStack(Items.OAK_PLANKS, count));
    }

    @GameTest(template = EMPTY)
    public static void pipeExtractionLeavesPattern(GameTestHelper helper) {
        AutoWorkbenchBlockEntity bench = bench(helper);
        sticks(bench.getGrid(), 3);
        ItemStack preview = bench.extractItem(false);
        helper.assertTrue(preview.is(Items.STICK) && bench.getGrid().getStackInSlot(0).getCount() == 3,
                "simulation takes nothing");
        ItemStack result = bench.extractItem(true);
        helper.assertTrue(result.is(Items.STICK) && result.getCount() == 4, "one craft: " + result);
        helper.assertTrue(bench.extractItem(true).getCount() == 4, "second craft");
        helper.assertTrue(bench.extractItem(true).isEmpty(), "the last plank of each slot stays as pattern");
        helper.assertTrue(bench.getGrid().getStackInSlot(0).getCount() == 1
                && bench.getGrid().getStackInSlot(3).getCount() == 1, "pattern kept");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void neighbourChestSuppliesPattern(GameTestHelper helper) {
        AutoWorkbenchBlockEntity bench = bench(helper);
        sticks(bench.getGrid(), 1);
        helper.setBlock(BENCH.west(), Blocks.CHEST);
        ChestBlockEntity chest = helper.getBlockEntity(BENCH.west());
        chest.setItem(5, new ItemStack(Items.BIRCH_PLANKS, 1));
        helper.assertTrue(bench.extractItem(true).isEmpty(), "one plank for two slots is not enough");
        helper.assertTrue(chest.getItem(5).getCount() == 1, "nothing taken");
        chest.setItem(5, new ItemStack(Items.OAK_PLANKS, 3));
        helper.assertTrue(bench.extractItem(true).is(Items.STICK), "sticks from the chest's planks");
        helper.assertTrue(chest.getItem(5).getCount() == 1, "two planks from the chest");
        helper.assertTrue(bench.getGrid().getStackInSlot(0).getCount() == 1, "pattern untouched");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void insertionIsRoundRobin(GameTestHelper helper) {
        AutoWorkbenchBlockEntity bench = bench(helper);
        bench.getGrid().setStackInSlot(0, new ItemStack(Items.OAK_PLANKS, 2));
        bench.getGrid().setStackInSlot(3, new ItemStack(Items.OAK_PLANKS, 5));
        IItemHandler handler = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK,
                helper.absolutePos(BENCH), Direction.UP);
        helper.assertTrue(handler != null, "item capability");
        ItemStack rest = handler.insertItem(0, new ItemStack(Items.OAK_PLANKS, 4), false);
        helper.assertTrue(rest.isEmpty(), "all inserted");
        helper.assertTrue(bench.getGrid().getStackInSlot(0).getCount() == 6
                && bench.getGrid().getStackInSlot(3).getCount() == 5, "fewest first, ties to the first slot");
        helper.assertTrue(handler.insertItem(0, new ItemStack(Items.COBBLESTONE), false).getCount() == 1,
                "no slot with cobblestone: empty slots stay empty");
        helper.assertTrue(handler.extractItem(0, 1, false).isEmpty(), "nothing can be taken");
        helper.succeed();
    }

    @GameTest(template = EMPTY, timeoutTicks = 400)
    public static void woodenPipePullsCrafts(GameTestHelper helper) {
        AutoWorkbenchBlockEntity bench = bench(helper);
        sticks(bench.getGrid(), 2);
        BlockPos wood = BENCH.east();
        BlockPos target = BENCH.east(3);
        helper.setBlock(wood, BcBlocks.pipe(PipeType.WOODEN_TRANSPORT).get());
        helper.setBlock(BENCH.east(2), BcBlocks.pipe(PipeType.COBBLESTONE_TRANSPORT).get());
        helper.setBlock(target, Blocks.CHEST);
        helper.runAfterDelay(3, () -> {
            helper.assertBlockProperty(wood, GenericPipeBlock.SOURCE, Direction.WEST.get3DDataValue());
            ((ItemPipeBlockEntity) helper.getBlockEntity(wood)).getPowerProvider().setEnergyStored(64.0F);
        });
        helper.succeedWhen(() -> {
            ChestBlockEntity chest = helper.getBlockEntity(target);
            helper.assertValueEqual(chest.getItem(0).getCount(), 4, "sticks in the chest");
            helper.assertValueEqual(bench.getGrid().getStackInSlot(0).getCount(), 1, "pattern left");
        });
    }
}
