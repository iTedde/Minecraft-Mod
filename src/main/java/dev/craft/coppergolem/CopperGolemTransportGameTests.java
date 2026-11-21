package dev.craft.coppergolem;

import dev.craft.Craft;
import dev.craft.ironchest.IronChestBlock;
import dev.craft.ironchest.IronChestBlockEntity;
import dev.craft.ironchest.IronChestBlocks;
import dev.craft.ironchest.IronChestType;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Copper golem item transport (spec 35 §2.3). Every walking test runs in a batch of its own, since the golem searches
 * ±32 blocks and would find the containers of tests running next to it.
 */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class CopperGolemTransportGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos CONTAINER = new BlockPos(1, 1, 1);
    private static final BlockPos GOLEM = new BlockPos(3, 1, 1);

    private CopperGolemTransportGameTests() {
    }

    private static CopperGolem golem(GameTestHelper helper, ItemStack held) {
        CopperGolem golem = helper.spawn(CopperGolems.COPPER_GOLEM.get(), GOLEM);
        golem.setItemInHand(InteractionHand.MAIN_HAND, held);
        return golem;
    }

    private static Container chest(GameTestHelper helper, BlockPos pos, Block block) {
        helper.setBlock(pos, block.defaultBlockState().setValue(IronChestBlock.FACING, Direction.EAST));
        return (Container) helper.getBlockEntity(pos);
    }

    private static int count(Container container, Item item) {
        int total = 0;
        for (int slot = 0; slot < container.getContainerSize(); slot++) {
            if (container.getItem(slot).is(item)) {
                total += container.getItem(slot).getCount();
            }
        }
        return total;
    }

    @GameTest(template = EMPTY, batch = "copper_golem_take", timeoutTicks = 400)
    public static void takesSixteenOfTheFirstItem(GameTestHelper helper) {
        Container chest = chest(helper, CONTAINER, IronChestBlocks.copperChest(WeatherState.WEATHERED, true).get());
        chest.setItem(3, new ItemStack(Items.IRON_INGOT, 40));
        chest.setItem(5, new ItemStack(Items.STONE, 10));
        CopperGolem golem = golem(helper, ItemStack.EMPTY);
        helper.succeedWhen(() -> {
            helper.assertTrue(golem.getMainHandItem().is(Items.IRON_INGOT)
                    && golem.getMainHandItem().getCount() == 16, "16 iron ingots in hand");
            helper.assertTrue(chest.getItem(3).getCount() == 24, "24 left in the chest");
            helper.assertTrue(chest.getItem(5).getCount() == 10, "stone untouched");
        });
    }

    /** It walks a few blocks, looks in for 3 seconds and fills the empty diamond chest. */
    @GameTest(template = EMPTY, batch = "copper_golem_diamond", timeoutTicks = 400)
    public static void depositsIntoEmptyDiamondChest(GameTestHelper helper) {
        Container chest = chest(helper, new BlockPos(6, 1, 6), IronChestBlocks.chest(IronChestType.DIAMOND).get());
        CopperGolem golem = golem(helper, new ItemStack(Items.REDSTONE, 10));
        long start = helper.getTick();
        helper.succeedWhen(() -> {
            helper.assertTrue(chest.getItem(0).is(Items.REDSTONE) && chest.getItem(0).getCount() == 10,
                    "redstone in the first slot");
            helper.assertTrue(golem.getMainHandItem().isEmpty(), "hand empty");
            helper.assertTrue(helper.getTick() - start >= CopperGolemTransportGoal.INSPECT_TICKS, "looked 3 s");
        });
    }

    @GameTest(template = EMPTY, batch = "copper_golem_barrel", timeoutTicks = 400)
    public static void depositsIntoBarrelWithSameItem(GameTestHelper helper) {
        helper.setBlock(CONTAINER, Blocks.BARREL);
        Container barrel = (Container) helper.getBlockEntity(CONTAINER);
        barrel.setItem(0, new ItemStack(Items.COBBLESTONE, 64));
        barrel.setItem(2, new ItemStack(Items.REDSTONE, 60));
        CopperGolem golem = golem(helper, new ItemStack(Items.REDSTONE, 10));
        helper.succeedWhen(() -> {
            helper.assertTrue(count(barrel, Items.REDSTONE) == 70, "all redstone in the barrel");
            helper.assertTrue(barrel.getItem(1).is(Items.REDSTONE) && barrel.getItem(1).getCount() == 10,
                    "frontmost fitting slot");
            helper.assertTrue(golem.getMainHandItem().isEmpty(), "hand empty");
        });
    }

    /** A furnace is no target, a barrel with other items does not take it, a copper chest is only a source. */
    @GameTest(template = EMPTY, batch = "copper_golem_refuse", timeoutTicks = 400)
    public static void refusesFurnaceForeignBarrelAndCopperChest(GameTestHelper helper) {
        helper.setBlock(CONTAINER, Blocks.FURNACE);
        Container furnace = (Container) helper.getBlockEntity(CONTAINER);
        BlockPos barrelPos = new BlockPos(3, 1, 3);
        helper.setBlock(barrelPos, Blocks.BARREL);
        Container barrel = (Container) helper.getBlockEntity(barrelPos);
        barrel.setItem(0, new ItemStack(Items.COBBLESTONE, 1));
        Container copper = chest(helper, new BlockPos(5, 1, 1), IronChestBlocks.copperChest(WeatherState.UNAFFECTED,
                false).get());
        golem(helper, new ItemStack(Items.REDSTONE, 10));
        helper.runAfterDelay(300, () -> {
            helper.assertTrue(furnace.isEmpty(), "furnace untouched");
            helper.assertTrue(count(barrel, Items.REDSTONE) == 0, "foreign barrel refused");
            helper.assertTrue(copper.isEmpty(), "copper chest is no target");
            helper.succeed();
        });
    }

    /** What does not fit stays in the hand; the golem moves on to the next container. */
    @GameTest(template = EMPTY, batch = "copper_golem_overflow", timeoutTicks = 600)
    public static void keepsWhatDoesNotFitAndMovesOn(GameTestHelper helper) {
        Container full = chest(helper, CONTAINER, IronChestBlocks.chest(IronChestType.IRON).get());
        for (int slot = 0; slot < full.getContainerSize(); slot++) {
            full.setItem(slot, new ItemStack(Items.REDSTONE, slot == 0 ? 60 : 64));
        }
        Container empty = chest(helper, new BlockPos(6, 1, 6), IronChestBlocks.chest(IronChestType.GOLD).get());
        CopperGolem golem = golem(helper, new ItemStack(Items.REDSTONE, 10));
        helper.succeedWhen(() -> {
            helper.assertTrue(full.getItem(0).getCount() == 64, "topped up the full chest");
            helper.assertTrue(count(empty, Items.REDSTONE) == 6, "rest into the next chest");
            helper.assertTrue(golem.getMainHandItem().isEmpty(), "hand empty");
        });
    }

    @GameTest(template = EMPTY, batch = "copper_golem_player")
    public static void playerTakesCarriedItem(GameTestHelper helper) {
        CopperGolem golem = golem(helper, new ItemStack(Items.REDSTONE, 10));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.interactOn(golem, InteractionHand.MAIN_HAND);
        helper.assertTrue(player.getMainHandItem().is(Items.REDSTONE) && player.getMainHandItem().getCount() == 10,
                "player got the redstone");
        helper.assertTrue(golem.getMainHandItem().isEmpty(), "golem hand empty");
        helper.succeed();
    }

    /** The search box is ±32 in every direction; the chests are removed again for later batches. */
    @GameTest(template = EMPTY, batch = "copper_golem_range")
    public static void searchReachesThirtyTwoBlocks(GameTestHelper helper) {
        BlockPos center = new BlockPos(0, 1, 0);
        BlockPos inside = center.offset(32, 0, 0);
        BlockPos above = center.offset(0, 32, -32);
        BlockPos outside = center.offset(0, 0, 33);
        Block diamond = IronChestBlocks.chest(IronChestType.DIAMOND).get();
        for (BlockPos pos : List.of(inside, above, outside)) {
            helper.setBlock(pos, diamond);
        }
        List<BlockPos> found = CopperGolemTransportGoal.findCandidates(helper.getLevel(), helper.absolutePos(center),
                true);
        boolean ok = found.contains(helper.absolutePos(inside)) && found.contains(helper.absolutePos(above))
                && !found.contains(helper.absolutePos(outside));
        List<BlockPos> sources = CopperGolemTransportGoal.findCandidates(helper.getLevel(),
                helper.absolutePos(center), false);
        boolean noSource = !sources.contains(helper.absolutePos(inside));
        for (BlockPos pos : List.of(inside, above, outside)) {
            helper.setBlock(pos, Blocks.AIR);
        }
        helper.assertTrue(ok, "±32 found, 33 not");
        helper.assertTrue(noSource, "a diamond chest is no source");
        helper.succeed();
    }

    @GameTest(template = EMPTY, batch = "copper_golem_range")
    public static void memoryForgetsAfterTenFailures(GameTestHelper helper) {
        ContainerMemory memory = new ContainerMemory();
        for (int i = 0; i < 10; i++) {
            helper.assertTrue(!memory.isExhausted(), "not yet exhausted after " + i);
            memory.fail(new BlockPos(i, 0, 0));
        }
        helper.assertTrue(memory.isExhausted(), "exhausted after 10");
        helper.assertTrue(memory.size() == ContainerMemory.CAPACITY, "remembers 9");
        helper.assertTrue(!memory.isVisited(BlockPos.ZERO) && memory.isVisited(new BlockPos(9, 0, 0)),
                "oldest forgotten");
        memory.succeed();
        helper.assertTrue(memory.isEmpty() && !memory.isExhausted(), "success forgets everything");
        helper.succeed();
    }

    /** The lid opens while the golem looks in, without counting it as a player. */
    @GameTest(template = EMPTY, batch = "copper_golem_lid", timeoutTicks = 200)
    public static void ironChestLidOpensForGolem(GameTestHelper helper) {
        chest(helper, CONTAINER, IronChestBlocks.chest(IronChestType.SILVER).get());
        CopperGolem golem = golem(helper, new ItemStack(Items.REDSTONE, 10));
        helper.succeedWhen(() -> {
            helper.assertTrue(golem.isInteracting(), "looking into the chest");
            IronChestBlockEntity chest = (IronChestBlockEntity) helper.getBlockEntity(CONTAINER);
            helper.assertTrue(chest.getNumUsingPlayers() == 0, "players' count untouched");
        });
    }
}
