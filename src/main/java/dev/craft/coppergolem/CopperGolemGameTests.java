package dev.craft.coppergolem;

import dev.craft.Craft;
import dev.craft.ironchest.IronChestBlock;
import dev.craft.ironchest.IronChestBlocks;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.WeatheringCopper.WeatherState;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Copper golem: building, oxidation, wax and axe (spec 35 §2.1, §2.4). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class CopperGolemGameTests {
    private static final String EMPTY = "empty_8x4x8";
    /** Own batch: golems search ±32 blocks and would walk into other tests. */
    static final String BATCH = "copper_golem";
    private static final BlockPos COPPER = new BlockPos(3, 1, 3);

    private CopperGolemGameTests() {
    }

    private static List<CopperGolem> golems(GameTestHelper helper) {
        return helper.getLevel().getEntitiesOfClass(CopperGolem.class, new AABB(helper.absolutePos(COPPER)).inflate(6));
    }

    /** A survival player puts a carved pumpkin on the given face of the copper block. */
    private static void placePumpkin(GameTestHelper helper, Direction face) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack pumpkin = new ItemStack(Items.CARVED_PUMPKIN);
        player.setItemInHand(InteractionHand.MAIN_HAND, pumpkin);
        BlockPos absolute = helper.absolutePos(COPPER);
        pumpkin.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(absolute).relative(face, 0.5), face, absolute, false)));
    }

    private static CopperGolem spawn(GameTestHelper helper, WeatherState stage) {
        CopperGolem golem = helper.spawn(CopperGolems.COPPER_GOLEM.get(), COPPER);
        golem.setWeatherState(stage);
        return golem;
    }

    private static void interact(GameTestHelper helper, CopperGolem golem, ItemStack stack) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        player.interactOn(golem, InteractionHand.MAIN_HAND);
    }

    @GameTest(template = EMPTY, batch = BATCH)
    public static void pumpkinOnCopperBlockBuildsGolem(GameTestHelper helper) {
        helper.setBlock(COPPER, Blocks.WAXED_WEATHERED_COPPER);
        placePumpkin(helper, Direction.UP);
        helper.succeedWhen(() -> {
            helper.assertBlockPresent(IronChestBlocks.copperChest(WeatherState.WEATHERED, false).get(), COPPER);
            helper.assertBlockPresent(Blocks.AIR, COPPER.above());
            List<CopperGolem> golems = golems(helper);
            helper.assertTrue(golems.size() == 1, "one golem");
            CopperGolem golem = golems.getFirst();
            helper.assertTrue(golem.getWeatherState() == WeatherState.WEATHERED, "stage of the block");
            helper.assertTrue(!golem.isWaxed(), "not waxed");
            helper.assertTrue(golem.blockPosition().equals(helper.absolutePos(COPPER.above())), "at the pumpkin");
        });
    }

    @GameTest(template = EMPTY, batch = BATCH)
    public static void sidewaysPatternBuildsGolem(GameTestHelper helper) {
        helper.setBlock(COPPER, Blocks.COPPER_BLOCK);
        placePumpkin(helper, Direction.EAST);
        helper.succeedWhen(() -> {
            helper.assertBlockPresent(IronChestBlocks.copperChest(WeatherState.UNAFFECTED, false).get(), COPPER);
            helper.assertTrue(golems(helper).size() == 1, "one golem");
        });
    }

    @GameTest(template = EMPTY, batch = BATCH)
    public static void pumpkinMustComeLast(GameTestHelper helper) {
        helper.setBlock(COPPER.above(), Blocks.CARVED_PUMPKIN);
        helper.setBlock(COPPER, Blocks.COPPER_BLOCK);
        helper.runAfterDelay(5, () -> {
            helper.assertBlockPresent(Blocks.COPPER_BLOCK, COPPER);
            helper.assertTrue(golems(helper).isEmpty(), "no golem");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, batch = BATCH)
    public static void dispenserBuildsGolem(GameTestHelper helper) {
        BlockPos dispenser = new BlockPos(1, 2, 3);
        helper.setBlock(new BlockPos(2, 1, 3), Blocks.EXPOSED_COPPER);
        helper.setBlock(dispenser, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.EAST));
        ((DispenserBlockEntity) helper.getBlockEntity(dispenser)).setItem(0, new ItemStack(Items.JACK_O_LANTERN));
        helper.setBlock(dispenser.west(), Blocks.REDSTONE_BLOCK);
        helper.succeedWhen(() -> {
            helper.assertBlockPresent(IronChestBlocks.copperChest(WeatherState.EXPOSED, false).get(),
                    new BlockPos(2, 1, 3));
            helper.assertBlockState(new BlockPos(2, 1, 3),
                    state -> state.getValue(IronChestBlock.FACING) == Direction.EAST, () -> "chest faces east");
            helper.assertTrue(golems(helper).size() == 1, "one golem");
            helper.assertTrue(((DispenserBlockEntity) helper.getBlockEntity(dispenser)).isEmpty(), "lantern used");
        });
    }

    /** Vanilla's dispenser golems still win, even with copper next to the head. */
    @GameTest(template = EMPTY, batch = BATCH)
    public static void dispenserStillBuildsSnowGolem(GameTestHelper helper) {
        BlockPos dispenser = new BlockPos(3, 3, 3);
        helper.setBlock(new BlockPos(3, 1, 3), Blocks.SNOW_BLOCK);
        helper.setBlock(new BlockPos(3, 0, 3), Blocks.SNOW_BLOCK);
        helper.setBlock(new BlockPos(4, 2, 3), Blocks.COPPER_BLOCK);
        helper.setBlock(dispenser, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING, Direction.DOWN));
        ((DispenserBlockEntity) helper.getBlockEntity(dispenser)).setItem(0, new ItemStack(Items.CARVED_PUMPKIN));
        helper.setBlock(dispenser.east(), Blocks.REDSTONE_BLOCK);
        helper.succeedWhen(() -> {
            helper.assertEntityPresent(EntityType.SNOW_GOLEM);
            helper.assertBlockPresent(Blocks.COPPER_BLOCK, new BlockPos(4, 2, 3));
            helper.assertTrue(golems(helper).isEmpty(), "no copper golem");
        });
    }

    @GameTest(template = EMPTY, batch = BATCH)
    public static void oxidationCatchesUpOnGameTime(GameTestHelper helper) {
        CopperGolem golem = spawn(helper, WeatherState.UNAFFECTED);
        CopperGolem waxed = spawn(helper, WeatherState.UNAFFECTED);
        waxed.setWaxed(true);
        long now = helper.getLevel().getGameTime();
        // Far enough back for every stage; stage 4 is the end, no statue.
        golem.setNextWeatheringTime(now - 10L * 552_000L);
        waxed.setNextWeatheringTime(now - 10L * 552_000L);
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(golem.getWeatherState() == WeatherState.OXIDIZED, "oxidized");
            helper.assertTrue(golem.isAlive(), "still a golem");
            helper.assertTrue(waxed.getWeatherState() == WeatherState.UNAFFECTED, "waxed golem does not oxidize");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, batch = BATCH)
    public static void oneStageAfterItsTime(GameTestHelper helper) {
        CopperGolem golem = spawn(helper, WeatherState.UNAFFECTED);
        golem.setNextWeatheringTime(helper.getLevel().getGameTime() + 3);
        helper.runAfterDelay(6, () -> {
            helper.assertTrue(golem.getWeatherState() == WeatherState.EXPOSED, "exposed");
            long left = golem.getNextWeatheringTime() - helper.getLevel().getGameTime();
            helper.assertTrue(left >= 420L * 1200L - 10 && left <= 460L * 1200L, "next stage in 420–460 min");
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY, batch = BATCH)
    public static void honeycombWaxesAndAxeScrapesGolem(GameTestHelper helper) {
        CopperGolem golem = spawn(helper, WeatherState.WEATHERED);
        ItemStack honeycomb = new ItemStack(Items.HONEYCOMB, 2);
        interact(helper, golem, honeycomb);
        helper.assertTrue(golem.isWaxed() && honeycomb.getCount() == 1, "waxed with one honeycomb");
        ItemStack axe = new ItemStack(Items.IRON_AXE);
        interact(helper, golem, axe);
        helper.assertTrue(!golem.isWaxed() && golem.getWeatherState() == WeatherState.WEATHERED, "wax off first");
        interact(helper, golem, axe);
        helper.assertTrue(golem.getWeatherState() == WeatherState.EXPOSED, "one stage scraped");
        helper.assertTrue(axe.getDamageValue() == 2, "axe used twice");
        helper.succeed();
    }

    @GameTest(template = EMPTY, batch = BATCH)
    public static void lightningTakesOneStage(GameTestHelper helper) {
        CopperGolem golem = spawn(helper, WeatherState.OXIDIZED);
        CopperGolem waxed = helper.spawn(CopperGolems.COPPER_GOLEM.get(), COPPER.east());
        waxed.setWeatherState(WeatherState.OXIDIZED);
        waxed.setWaxed(true);
        LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(helper.getLevel());
        bolt.moveTo(Vec3.atBottomCenterOf(helper.absolutePos(COPPER)).add(0.5, 0, 0));
        helper.getLevel().addFreshEntity(bolt);
        helper.runAfterDelay(20, () -> {
            helper.assertTrue(golem.getWeatherState() == WeatherState.WEATHERED, "exactly one stage off");
            helper.assertTrue(waxed.getWeatherState() == WeatherState.OXIDIZED, "waxed golem unchanged");
            helper.assertTrue(golem.isAlive(), "survives");
            helper.succeed();
        });
    }
}
