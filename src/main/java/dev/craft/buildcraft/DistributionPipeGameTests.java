package dev.craft.buildcraft;

import dev.craft.Craft;
import dev.craft.buildcraft.transport.ItemPipeBlockEntity;
import dev.craft.buildcraft.transport.PipeType;
import dev.craft.buildcraft.transport.TravelingItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Distribution transport pipe (spec 31 §6): items enter from the west, chests north, south and east. */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class DistributionPipeGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos PIPE = new BlockPos(2, 1, 2);

    private DistributionPipeGameTests() {
    }

    private static ItemPipeBlockEntity setUp(GameTestHelper helper, int north, int south, int east) {
        helper.setBlock(PIPE.north(), Blocks.CHEST);
        helper.setBlock(PIPE.south(), Blocks.CHEST);
        helper.setBlock(PIPE.east(), Blocks.CHEST);
        helper.setBlock(PIPE, BcBlocks.pipe(PipeType.DISTRIBUTION_TRANSPORT).get());
        ItemPipeBlockEntity pipe = helper.getBlockEntity(PIPE);
        pipe.getDistribution().setWeight(Direction.NORTH, north);
        pipe.getDistribution().setWeight(Direction.SOUTH, south);
        pipe.getDistribution().setWeight(Direction.EAST, east);
        return pipe;
    }

    /** Puts an item at the west edge of the pipe, moving east into it. */
    private static void inject(GameTestHelper helper, ItemStack stack) {
        BlockPos abs = helper.absolutePos(PIPE);
        TravelingItem item = new TravelingItem(stack, abs.getX(), abs.getY() + ItemPipeBlockEntity.PIPE_FLOOR,
                abs.getZ() + 0.5);
        ((ItemPipeBlockEntity) helper.getBlockEntity(PIPE)).entityEntering(item, Direction.EAST);
    }

    private static int count(GameTestHelper helper, BlockPos chest, Item item) {
        ChestBlockEntity container = helper.getBlockEntity(chest);
        int count = 0;
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            if (container.getItem(slot).is(item)) {
                count += container.getItem(slot).getCount();
            }
        }
        return count;
    }

    private static void assertCounts(GameTestHelper helper, int north, int south, int east) {
        helper.assertValueEqual(count(helper, PIPE.north(), Items.COBBLESTONE), north, "north chest");
        helper.assertValueEqual(count(helper, PIPE.south(), Items.COBBLESTONE), south, "south chest");
        helper.assertValueEqual(count(helper, PIPE.east(), Items.COBBLESTONE), east, "east chest");
    }

    @GameTest(template = EMPTY, timeoutTicks = 400)
    public static void singleItemsSplitExactly(GameTestHelper helper) {
        setUp(helper, 50, 30, 20);
        for (int tick = 0; tick < 20; tick++) {
            helper.runAfterDelay(2 + tick, () -> {
                for (int i = 0; i < 5; i++) {
                    inject(helper, new ItemStack(Items.COBBLESTONE));
                }
            });
        }
        helper.runAfterDelay(200, () -> {
            assertCounts(helper, 50, 30, 20);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 300)
    public static void stackIsSplit(GameTestHelper helper) {
        setUp(helper, 50, 30, 20);
        helper.runAfterDelay(2, () -> inject(helper, new ItemStack(Items.COBBLESTONE, 64)));
        helper.runAfterDelay(150, () -> {
            assertCounts(helper, 32, 19, 13);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 300)
    public static void zeroClosesConnectedSide(GameTestHelper helper) {
        setUp(helper, 100, 0, 0);
        helper.runAfterDelay(2, () -> inject(helper, new ItemStack(Items.COBBLESTONE, 10)));
        helper.runAfterDelay(150, () -> {
            assertCounts(helper, 10, 0, 0);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 300)
    public static void allZeroActsLikePlainPipe(GameTestHelper helper) {
        setUp(helper, 0, 0, 0);
        helper.runAfterDelay(2, () -> inject(helper, new ItemStack(Items.COBBLESTONE, 10)));
        helper.runAfterDelay(150, () -> {
            helper.assertValueEqual(count(helper, PIPE.north(), Items.COBBLESTONE)
                    + count(helper, PIPE.south(), Items.COBBLESTONE)
                    + count(helper, PIPE.east(), Items.COBBLESTONE), 10, "items delivered");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, timeoutTicks = 500)
    public static void fullSideSharesOutWithoutCatchingUp(GameTestHelper helper) {
        setUp(helper, 50, 50, 0);
        ChestBlockEntity south = helper.getBlockEntity(PIPE.south());
        for (int slot = 0; slot < south.getContainerSize(); slot++) {
            south.setItem(slot, new ItemStack(Items.STONE, 64));
        }
        helper.runAfterDelay(2, () -> {
            for (int i = 0; i < 10; i++) {
                inject(helper, new ItemStack(Items.COBBLESTONE));
            }
        });
        helper.runAfterDelay(150, () -> {
            assertCounts(helper, 10, 0, 0);
            south.clearContent();
            for (int i = 0; i < 10; i++) {
                inject(helper, new ItemStack(Items.COBBLESTONE));
            }
        });
        helper.runAfterDelay(300, () -> {
            assertCounts(helper, 15, 5, 0);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void recipeExists(GameTestHelper helper) {
        helper.assertTrue(helper.getLevel().getRecipeManager().byKey(ResourceLocation.fromNamespaceAndPath(Craft.MODID,
                "distribution_transport_pipe")).isPresent(), "recipe");
        helper.succeed();
    }
}
