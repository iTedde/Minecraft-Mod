package dev.craft.ic2.brewing;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Blocks;
import dev.craft.ic2.Ic2DataComponents;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.block.ScaffoldBlock;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for coffee, the booze barrel and its drinks (phase 12.8). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class BrewingGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos POS = new BlockPos(4, 2, 4);

    private BrewingGameTests() {
    }

    private static UseOnContext click(GameTestHelper helper, Player player, Direction side) {
        BlockPos abs = helper.absolutePos(POS);
        return new UseOnContext(player, InteractionHand.MAIN_HAND,
                new BlockHitResult(Vec3.atCenterOf(abs), side, abs, false));
    }

    private static BarrelBlockEntity placeBarrel(GameTestHelper helper, Player player, int value) {
        helper.setBlock(POS, Ic2Blocks.WOODEN_SCAFFOLD.get().defaultBlockState().setValue(ScaffoldBlock.STRENGTH, 2));
        player.setItemInHand(InteractionHand.MAIN_HAND, BarrelBlock.barrelItem(value));
        Ic2Items.BOOZE_BARREL.get().useOn(click(helper, player, Direction.UP));
        helper.assertBlockPresent(Ic2Blocks.BARREL.get(), POS);
        return helper.getBlockEntity(POS);
    }

    private static void fill(BarrelBlockEntity barrel, Player player, ItemStack stack) {
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        barrel.rightClick(player);
    }

    @GameTest(template = EMPTY)
    public static void beerCompositionFromIngredients(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BarrelBlockEntity barrel = placeBarrel(helper, player, 0);
        helper.assertTrue(player.getMainHandItem().isEmpty(), "barrel item used up");
        ItemStack cells = new ItemStack(Ic2Items.WATER_CELL.get(), 8);
        fill(barrel, player, cells);
        helper.assertTrue(cells.isEmpty(), "all water cells consumed, no empty cells back");
        fill(barrel, player, new ItemStack(Items.WATER_BUCKET));
        helper.assertTrue(player.getMainHandItem().is(Items.BUCKET), "bucket emptied");
        fill(barrel, player, new ItemStack(Items.WHEAT, 9));
        fill(barrel, player, new ItemStack(Ic2Items.HOPS.get(), 9));
        helper.assertValueEqual(barrel.getBoozeAmount(), 9, "9 litres");
        // hops/wheat = 1 → hops ratio 3; solids/water = 2 → solid ratio 5.
        helper.assertValueEqual(barrel.calculateValue(), Booze.beer(9, 5, 3, 0), "packed beer value");
        fill(barrel, player, new ItemStack(Items.WHEAT));
        helper.assertValueEqual(player.getMainHandItem().getCount(), 1, "fixed beer takes no more solids");
        fill(barrel, player, new ItemStack(Items.SUGAR_CANE));
        helper.assertValueEqual(barrel.getBoozeType(), Booze.TYPE_BEER, "beer barrel takes no sugar cane");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void hopsWithoutWheatSpoil(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BarrelBlockEntity barrel = placeBarrel(helper, player, 0);
        fill(barrel, player, new ItemStack(Ic2Items.WATER_CELL.get(), 4));
        fill(barrel, player, new ItemStack(Ic2Items.HOPS.get(), 2));
        helper.assertValueEqual(Booze.getTimeRatio(barrel.calculateValue()), Booze.BLACK_STUFF,
                "hops without wheat count as ratio 10");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void beerAgesUnlessTapped(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BarrelBlockEntity barrel = placeBarrel(helper, player, Booze.beer(4, 3, 3, 0));
        barrel.setAge(23998);
        helper.runAfterDelay(5, () -> {
            helper.assertValueEqual(barrel.getTimeRatio(), 1, "Brew → Youngster after 24000 ticks");
            helper.assertTrue(barrel.getAge() < 10, "age restarts");
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Ic2Items.TREETAP.get()));
            Ic2Items.TREETAP.get().useOn(click(helper, player, Direction.EAST));
            int age = barrel.getAge();
            helper.runAfterDelay(5, () -> {
                helper.assertValueEqual(barrel.getAge(), age, "no ageing with treetap");
                helper.succeed();
            });
        });
    }

    @GameTest(template = EMPTY)
    public static void treetapMugAndPunching(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BarrelBlockEntity barrel = placeBarrel(helper, player, Booze.beer(5, 3, 3, 2));
        ItemStack damaged = new ItemStack(Ic2Items.TREETAP.get());
        damaged.setDamageValue(1);
        player.setItemInHand(InteractionHand.MAIN_HAND, damaged);
        Ic2Items.TREETAP.get().useOn(click(helper, player, Direction.NORTH));
        helper.assertFalse(BarrelBlock.isTapped(helper.getBlockState(POS)), "used treetap is refused");
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Ic2Items.TREETAP.get()));
        Ic2Items.TREETAP.get().useOn(click(helper, player, Direction.NORTH));
        helper.assertTrue(BarrelBlock.isTappedOn(helper.getBlockState(POS), Direction.NORTH), "treetap mounted");
        helper.assertTrue(player.getMainHandItem().isEmpty(), "treetap consumed");

        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Ic2Items.STONE_MUG.get()));
        Ic2Items.STONE_MUG.get().useOn(click(helper, player, Direction.SOUTH));
        helper.assertTrue(player.getMainHandItem().is(Ic2Items.STONE_MUG.get()), "wrong side does nothing");
        Ic2Items.STONE_MUG.get().useOn(click(helper, player, Direction.NORTH));
        ItemStack mug = player.getMainHandItem();
        helper.assertTrue(mug.is(Ic2Items.BOOZE_MUG.get()), "mug filled");
        helper.assertValueEqual(Booze.getValue(mug), Booze.beer(5, 3, 3, 2), "mug carries the barrel value");
        helper.assertValueEqual(barrel.getBoozeAmount(), 4, "one litre drawn");

        BlockPos abs = helper.absolutePos(POS);
        BlockState state = helper.getBlockState(POS);
        state.attack(helper.getLevel(), abs, player);
        helper.assertFalse(BarrelBlock.isTapped(helper.getBlockState(POS)), "punching removes the treetap");
        helper.assertValueEqual(barrel.getBoozeAmount(), 3, "and loses one litre");
        helper.assertItemEntityPresent(Ic2Items.TREETAP.get(), POS, 2.0);

        helper.getBlockState(POS).attack(helper.getLevel(), abs, player);
        helper.assertBlockPresent(Ic2Blocks.WOODEN_SCAFFOLD.get(), POS);
        List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class, new AABB(abs).inflate(2.0),
                entity -> entity.getItem().is(Ic2Items.BOOZE_BARREL.get()));
        helper.assertValueEqual(drops.size(), 1, "barrel item dropped");
        helper.assertValueEqual(Booze.getAmount(Booze.getValue(drops.get(0).getItem())), 3, "with its contents");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void rumDrainResetsUnfinishedProgress(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        BarrelBlockEntity barrel = placeBarrel(helper, player, 0);
        fill(barrel, player, new ItemStack(Items.SUGAR_CANE, 10));
        helper.assertValueEqual(barrel.getBoozeType(), Booze.TYPE_RUM, "rum barrel");
        barrel.setAge(BarrelBlockEntity.timeNeededForRum(10) / 2);
        helper.assertValueEqual(Booze.getRumProgress(barrel.calculateValue()), 50, "50 %");
        barrel.drainLiquid(1);
        helper.assertValueEqual(barrel.getAge(), 0, "integer percent rescaling resets the age");
        barrel.setAge(BarrelBlockEntity.timeNeededForRum(9) * 3);
        helper.assertValueEqual(Booze.getRumProgress(barrel.calculateValue()), 100, "progress capped at 100");
        barrel.setAge(601);
        fill(barrel, player, new ItemStack(Items.SUGAR_CANE));
        helper.assertValueEqual(barrel.getBoozeAmount(), 9, "old rum barrel takes no sugar cane");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void beerEffectsStack(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getFoodData().setFoodLevel(0);
        ItemStack beer = new ItemStack(Ic2Items.BOOZE_MUG.get());
        beer.set(Ic2DataComponents.BOOZE.get(), Booze.beer(1, 3, 3, 2));
        ItemStack result = beer.finishUsingItem(helper.getLevel(), player);
        helper.assertTrue(result.is(Ic2Items.STONE_MUG.get()), "empty mug back");
        helper.assertValueEqual(player.getFoodData().getFoodLevel(), 3, "6 - hops ratio food");
        MobEffectInstance fatigue = player.getEffect(MobEffects.DIG_SLOWDOWN);
        helper.assertTrue(fatigue != null && fatigue.getDuration() == 1200 && fatigue.getAmplifier() == 0,
                "mining fatigue for the solid ratio duration");
        beer.finishUsingItem(helper.getLevel(), player);
        fatigue = player.getEffect(MobEffects.DIG_SLOWDOWN);
        // max = (int) (1.0 · 3 · 0.5) = 1; extension min(1200, (int) (1200 · 3 − 1200) / 2) = 1200.
        helper.assertTrue(fatigue.getAmplifier() == 1 && fatigue.getDuration() == 2400, "fatigue stacks");
        helper.assertTrue(player.hasEffect(MobEffects.DAMAGE_BOOST), "strength from the second mug");
        helper.assertFalse(player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "slowness only from fatigue II");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void coffeeRaisesSpeedAndHaste(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack coffee = new ItemStack(Ic2Items.DARK_COFFEE.get());
        ItemStack result = coffee.finishUsingItem(helper.getLevel(), player);
        helper.assertTrue(result.is(Ic2Items.STONE_MUG.get()), "empty mug back");
        MobEffectInstance speed = player.getEffect(MobEffects.MOVEMENT_SPEED);
        helper.assertTrue(speed != null && speed.getDuration() == 300 && speed.getAmplifier() == 0, "fresh speed");
        coffee.finishUsingItem(helper.getLevel(), player);
        coffee.finishUsingItem(helper.getLevel(), player);
        speed = player.getEffect(MobEffects.MOVEMENT_SPEED);
        helper.assertTrue(speed.getAmplifier() == 2 && speed.getDuration() == 2700, "dark coffee +1 level, +1200");
        helper.assertFalse(player.hasEffect(MobEffects.CONFUSION), "no nausea below level 3");
        coffee.finishUsingItem(helper.getLevel(), player);
        helper.assertTrue(player.hasEffect(MobEffects.CONFUSION), "nausea at amplifier 3");
        ItemStack cold = new ItemStack(Ic2Items.COLD_COFFEE.get());
        Player other = helper.makeMockPlayer(GameType.SURVIVAL);
        for (int i = 0; i < 4; i++) {
            cold.finishUsingItem(helper.getLevel(), other);
        }
        helper.assertValueEqual(other.getEffect(MobEffects.DIG_SPEED).getAmplifier(), 1, "cold coffee caps at 1");
        helper.succeed();
    }
}
