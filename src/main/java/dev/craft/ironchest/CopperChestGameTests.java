package dev.craft.ironchest;

import dev.craft.Craft;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.DataMapHooks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Copper chest oxidation (spec 35 §1). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class CopperChestGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos POS = new BlockPos(3, 1, 3);

    private CopperChestGameTests() {
    }

    private static Block chest(WeatherState stage, boolean waxed) {
        return IronChestBlocks.copperChest(stage, waxed).get();
    }

    private static IronChestBlockEntity place(GameTestHelper helper, Block block) {
        helper.setBlock(POS, block.defaultBlockState().setValue(IronChestBlock.FACING, Direction.EAST));
        IronChestBlockEntity chest = (IronChestBlockEntity) helper.getBlockEntity(POS);
        chest.setItem(30, new ItemStack(Items.REDSTONE, 40));
        return chest;
    }

    /** The chest still holds its redstone and nothing dropped. */
    private static void assertContentsKept(GameTestHelper helper) {
        IronChestBlockEntity chest = (IronChestBlockEntity) helper.getBlockEntity(POS);
        helper.assertTrue(chest.getItem(30).is(Items.REDSTONE) && chest.getItem(30).getCount() == 40,
                "contents kept");
        helper.assertTrue(helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                new AABB(helper.absolutePos(POS)).inflate(2)).isEmpty(), "nothing dropped");
    }

    private static void use(GameTestHelper helper, ItemStack stack) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setShiftKeyDown(true);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos absolute = helper.absolutePos(POS);
        stack.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false)));
    }

    @GameTest(template = EMPTY)
    public static void stagesFollowTheCopperChain(GameTestHelper helper) {
        helper.assertTrue(IronChestBlocks.chest(IronChestType.COPPER).get() == chest(WeatherState.UNAFFECTED, false),
                "craft:copper_chest is the unaffected stage");
        WeatherState[] stages = WeatherState.values();
        for (WeatherState stage : stages) {
            Block block = chest(stage, false);
            Block waxed = chest(stage, true);
            Block next = stage.ordinal() + 1 < stages.length ? chest(stages[stage.ordinal() + 1], false) : null;
            helper.assertTrue(WeatheringCopper.getNext(block).orElse(null) == next, "next of " + stage);
            helper.assertTrue(DataMapHooks.getBlockWaxed(block) == waxed, "waxed " + stage);
            helper.assertTrue(DataMapHooks.getBlockUnwaxed(waxed) == block, "unwaxed " + stage);
            helper.assertTrue(!waxed.defaultBlockState().isRandomlyTicking(), "waxed " + stage + " does not tick");
            helper.assertTrue(block.defaultBlockState().isRandomlyTicking() == (next != null), "ticks " + stage);
            helper.assertTrue(((IronChestBlock) waxed).getType() == IronChestType.COPPER, "still a copper chest");
        }
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void oxidizingKeepsContents(GameTestHelper helper) {
        IronChestBlockEntity before = place(helper, chest(WeatherState.UNAFFECTED, false));
        BlockPos absolute = helper.absolutePos(POS);
        BlockState state = helper.getBlockState(POS);
        helper.getLevel().setBlockAndUpdate(absolute,
                ((WeatheringCopper) state.getBlock()).getNext(state).orElseThrow());
        helper.assertBlockPresent(chest(WeatherState.EXPOSED, false), POS);
        helper.assertTrue(helper.getBlockEntity(POS) == before, "same block entity");
        helper.assertTrue(helper.getBlockState(POS).getValue(IronChestBlock.FACING) == Direction.EAST, "facing kept");
        assertContentsKept(helper);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void honeycombWaxesAndAxeScrapes(GameTestHelper helper) {
        place(helper, chest(WeatherState.WEATHERED, false));
        ItemStack honeycomb = new ItemStack(Items.HONEYCOMB, 2);
        use(helper, honeycomb);
        helper.assertBlockPresent(chest(WeatherState.WEATHERED, true), POS);
        helper.assertTrue(honeycomb.getCount() == 1, "honeycomb used");
        use(helper, new ItemStack(Items.IRON_AXE));
        helper.assertBlockPresent(chest(WeatherState.WEATHERED, false), POS);
        use(helper, new ItemStack(Items.IRON_AXE));
        helper.assertBlockPresent(chest(WeatherState.EXPOSED, false), POS);
        assertContentsKept(helper);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void lightningCleansCopperChest(GameTestHelper helper) {
        place(helper, chest(WeatherState.OXIDIZED, false));
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(helper.getLevel());
        bolt.moveTo(Vec3.atBottomCenterOf(helper.absolutePos(POS.above())));
        helper.getLevel().addFreshEntity(bolt);
        helper.succeedWhen(() -> {
            helper.assertBlockPresent(chest(WeatherState.UNAFFECTED, false), POS);
            assertContentsKept(helper);
        });
    }

    @GameTest(template = EMPTY)
    public static void breakingDropsFreshCopperChest(GameTestHelper helper) {
        place(helper, chest(WeatherState.OXIDIZED, true));
        helper.getLevel().destroyBlock(helper.absolutePos(POS), true);
        List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                new AABB(helper.absolutePos(POS)).inflate(2));
        helper.assertTrue(drops.stream().anyMatch(e -> e.getItem().is(IronChestItems.chest(IronChestType.COPPER))),
                "fresh copper chest dropped");
        helper.assertTrue(drops.stream().filter(e -> e.getItem().is(Items.REDSTONE))
                .mapToInt(e -> e.getItem().getCount()).sum() == 40, "contents dropped");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void upgradingOxidizedChestKeepsContents(GameTestHelper helper) {
        place(helper, chest(WeatherState.OXIDIZED, true));
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack upgrade = IronChestItems.upgrade(ChestUpgradeType.COPPER_IRON).toStack();
        player.setItemInHand(InteractionHand.MAIN_HAND, upgrade);
        BlockPos absolute = helper.absolutePos(POS);
        upgrade.getItem().onItemUseFirst(upgrade, new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absolute), Direction.UP, absolute, false)));
        helper.assertBlockPresent(IronChestBlocks.chest(IronChestType.IRON).get(), POS);
        IronChestBlockEntity chest = (IronChestBlockEntity) helper.getBlockEntity(POS);
        helper.assertTrue(chest.getItem(30).is(Items.REDSTONE), "contents moved into the iron chest");
        helper.succeed();
    }
}
