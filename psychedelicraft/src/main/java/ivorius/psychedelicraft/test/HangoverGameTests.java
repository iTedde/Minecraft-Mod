package ivorius.psychedelicraft.test;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.entity.drug.DrugProperties;
import ivorius.psychedelicraft.entity.drug.DrugType;
import ivorius.psychedelicraft.entity.drug.Hangover;
import ivorius.psychedelicraft.entity.drug.type.SimpleDrug;
import ivorius.psychedelicraft.entity.effect.PSEffects;
import ivorius.psychedelicraft.fluid.FluidVolumes;
import ivorius.psychedelicraft.fluid.PSFluids;
import ivorius.psychedelicraft.fluid.SimpleFluid;
import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.ItemFluids;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.EffectCures;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/**
 * Hangover (Phase 52.4, spec 52 §6).
 */
@GameTestHolder(Psychedelicraft.DEFAULT_NAMESPACE)
@PrefixGameTestTemplate(false)
public final class HangoverGameTests {
    private static final String EMPTY = "empty_16x8x16";

    private HangoverGameTests() {
    }

    private static Player survivalPlayer(GameTestHelper helper) {
        net.minecraft.server.level.ServerPlayer player = net.neoforged.neoforge.common.util.FakePlayerFactory.get(helper.getLevel(),
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "psychedelicraft-hangover-test"));
        player.setPos(helper.absoluteVec(new Vec3(1, 1, 1)));
        player.setGameMode(GameType.SURVIVAL);
        player.getFoodData().setFoodLevel(20);
        return player;
    }

    private static void setAlcohol(Player player, double value) {
        SimpleDrug alcohol = (SimpleDrug)DrugProperties.of(player).getDrug(DrugType.ALCOHOL);
        alcohol.setDesiredValue(value);
        alcohol.setActiveValue(value);
    }

    private static int tick = 0;

    private static void tickDrugs(Player player, int ticks) {
        DrugProperties properties = DrugProperties.of(player);
        for (int i = 0; i < ticks; i++) {
            player.tickCount = ++tick;
            properties.onTick();
        }
    }

    /** Ticks until the alcohol value is below the sober threshold and the hangover has started. */
    private static void soberUp(GameTestHelper helper, Player player) {
        DrugProperties properties = DrugProperties.of(player);
        for (int i = 0; i < 10000 && properties.getDrugValue(DrugType.ALCOHOL) >= Hangover.SOBER; i++) {
            tickDrugs(player, 1);
        }
        tickDrugs(player, 1);
        helper.assertTrue(properties.getDrugValue(DrugType.ALCOHOL) < Hangover.SOBER, "never sobered up");
    }

    private static Hangover hangover(Player player) {
        return DrugProperties.of(player).getHangover();
    }

    @GameTest(template = EMPTY)
    public static void levelAndDurationFollowPeak(GameTestHelper helper) {
        helper.assertTrue(Hangover.levelFor(0.19F) == 0, "below 0.2 no hangover");
        helper.assertTrue(Hangover.levelFor(0.2F) == 1, "0.2 is light");
        helper.assertTrue(Hangover.levelFor(0.5F) == 2, "0.5 is medium");
        helper.assertTrue(Hangover.levelFor(0.8F) == 3, "0.8 is heavy");
        helper.assertTrue(Hangover.durationFor(0.5F) == 12000, "duration 0.5 x day");
        helper.assertTrue(Hangover.durationFor(1F) == 24000, "at most one day");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void heavyDrinkingCausesHeavyHangover(GameTestHelper helper) {
        Player player = survivalPlayer(helper);
        setAlcohol(player, 0.85);
        tickDrugs(player, 20);
        helper.assertFalse(player.hasEffect(PSEffects.HANGOVER), "hangover while still drunk");
        soberUp(helper, player);
        tickDrugs(player, 20);

        Hangover hangover = hangover(player);
        helper.assertTrue(hangover.isActive() && hangover.getLevel() == 3, "level " + hangover.getLevel() + " peak " + hangover.getPeak());
        MobEffectInstance effect = player.getEffect(PSEffects.HANGOVER);
        helper.assertTrue(effect != null && effect.getAmplifier() == 2, "effect " + effect);
        helper.assertTrue(Math.abs(effect.getDuration() - hangover.getTicks()) <= 40, "duration " + effect.getDuration() + " vs " + hangover.getTicks());
        helper.assertTrue(hangover.getTicks() > 19000 && hangover.getTicks() <= 20400, "ticks " + hangover.getTicks());
        helper.assertTrue(player.hasEffect(MobEffects.DIG_SLOWDOWN), "no mining fatigue");
        helper.assertTrue(player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "no slowness");
        helper.assertTrue(player.hasEffect(MobEffects.HUNGER), "no hunger");
        helper.assertTrue(player.hasEffect(MobEffects.WEAKNESS), "no weakness");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void lightHangoverOnlyFatigueAndEnds(GameTestHelper helper) {
        Player player = survivalPlayer(helper);
        setAlcohol(player, 0.3);
        soberUp(helper, player);
        tickDrugs(player, 20);
        Hangover hangover = hangover(player);
        helper.assertTrue(hangover.getLevel() == 1, "level " + hangover.getLevel());
        helper.assertTrue(player.hasEffect(MobEffects.DIG_SLOWDOWN), "no mining fatigue");
        helper.assertFalse(player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "slowness at level I");
        helper.assertFalse(player.hasEffect(MobEffects.WEAKNESS), "weakness at level I");

        tickDrugs(player, Hangover.MAX_DURATION);
        helper.assertFalse(hangover.isActive(), "hangover did not end");
        helper.assertFalse(player.hasEffect(PSEffects.HANGOVER), "effect still there");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void littleAlcoholNoHangover(GameTestHelper helper) {
        Player player = survivalPlayer(helper);
        setAlcohol(player, 0.15);
        soberUp(helper, player);
        tickDrugs(player, 20);
        helper.assertFalse(player.hasEffect(PSEffects.HANGOVER), "hangover below 0.2");
        helper.assertTrue(hangover(player).getPeak() == 0, "peak not reset when sober");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void milkDoesNotCure(GameTestHelper helper) {
        Player player = survivalPlayer(helper);
        setAlcohol(player, 0.6);
        soberUp(helper, player);
        tickDrugs(player, 20);
        player.removeEffectsCuredBy(EffectCures.MILK);
        helper.assertTrue(player.hasEffect(PSEffects.HANGOVER), "milk cured the hangover");
        tickDrugs(player, 20);
        helper.assertTrue(player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN), "side effects not back after milk");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void drinkingAgainPausesAndRaisesLevel(GameTestHelper helper) {
        Player player = survivalPlayer(helper);
        setAlcohol(player, 0.3);
        soberUp(helper, player);
        tickDrugs(player, 20);
        Hangover hangover = hangover(player);
        helper.assertTrue(hangover.getLevel() == 1, "level " + hangover.getLevel());

        setAlcohol(player, 0.6);
        tickDrugs(player, 1);
        helper.assertFalse(hangover.isActive(), "hangover not paused while drunk");
        helper.assertFalse(player.hasEffect(PSEffects.HANGOVER), "effect shown while paused");
        int paused = hangover.getTicks();
        tickDrugs(player, 100);
        helper.assertTrue(hangover.getTicks() == paused, "time ran while paused");

        soberUp(helper, player);
        tickDrugs(player, 20);
        helper.assertTrue(hangover.isActive() && hangover.getLevel() == 2, "level after second round " + hangover.getLevel());
        helper.assertTrue(hangover.getTicks() > 11000, "duration not raised: " + hangover.getTicks());
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void sleepingWithAlcoholStartsHangover(GameTestHelper helper) {
        Player player = survivalPlayer(helper);
        setAlcohol(player, 0.55);
        tickDrugs(player, 5);
        DrugProperties.of(player).onAwoken();
        // sleeping resets the desired value; the active value follows within a few seconds
        tickDrugs(player, 200);
        Hangover hangover = hangover(player);
        helper.assertTrue(DrugProperties.of(player).getDrugValue(DrugType.ALCOHOL) < Hangover.SOBER, "alcohol not reset by sleeping");
        helper.assertTrue(hangover.isActive() && hangover.getLevel() == 2, "level after sleeping " + hangover.getLevel());
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void waterAndCoffeeRelieve(GameTestHelper helper) {
        Player player = survivalPlayer(helper);
        setAlcohol(player, 0.85);
        soberUp(helper, player);
        tickDrugs(player, 20);
        Hangover hangover = hangover(player);

        // water bottle (vanilla item, through the use-finish event)
        int before = hangover.getTicks();
        ItemStack bottle = new ItemStack(Items.POTION);
        bottle.set(DataComponents.POTION_CONTENTS, new PotionContents(Potions.WATER));
        NeoForge.EVENT_BUS.post(new LivingEntityUseItemEvent.Finish(player, bottle, 0, new ItemStack(Items.GLASS_BOTTLE)));
        helper.assertTrue(before - hangover.getTicks() == Hangover.WATER_RELIEF, "water: " + before + " -> " + hangover.getTicks());
        helper.assertTrue(hangover.getLevel() == 3, "water changed the level");

        // water from a Psychedelicraft vessel
        before = hangover.getTicks();
        ItemStack waterMug = ItemFluids.set(PSItems.WOODEN_MUG.getDefaultInstance(), SimpleFluid.of(Fluids.WATER).getDefaultStack(FluidVolumes.MUG));
        ItemStack rest = waterMug.getItem().finishUsingItem(waterMug, helper.getLevel(), player);
        helper.assertTrue(ItemFluids.of(rest).amount() < FluidVolumes.MUG, "water not drunk from mug: " + ItemFluids.of(rest));
        helper.assertTrue(before - hangover.getTicks() == Hangover.WATER_RELIEF, "mug water: " + before + " -> " + hangover.getTicks());

        // Psychedelicraft coffee
        before = hangover.getTicks();
        ItemStack coffee = ItemFluids.set(PSItems.WOODEN_MUG.getDefaultInstance(), PSFluids.COFFEE.getDefaultStack(FluidVolumes.MUG));
        rest = coffee.getItem().finishUsingItem(coffee, helper.getLevel(), player);
        helper.assertTrue(ItemFluids.of(rest).amount() < FluidVolumes.MUG, "coffee not drunk: " + ItemFluids.of(rest));
        helper.assertTrue(before - hangover.getTicks() == Hangover.COFFEE_RELIEF, "coffee: " + before + " -> " + hangover.getTicks());
        helper.assertTrue(hangover.getLevel() == 2, "coffee did not lower the level: " + hangover.getLevel());
        MobEffectInstance effect = player.getEffect(PSEffects.HANGOVER);
        helper.assertTrue(effect != null && effect.getAmplifier() == 1, "display not updated: " + effect);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void waterNotDrinkableWithoutHangover(GameTestHelper helper) {
        Player player = survivalPlayer(helper);
        ItemStack waterMug = ItemFluids.set(PSItems.WOODEN_MUG.getDefaultInstance(), SimpleFluid.of(Fluids.WATER).getDefaultStack(FluidVolumes.MUG));
        ItemStack rest = waterMug.getItem().finishUsingItem(waterMug, helper.getLevel(), player);
        helper.assertTrue(ItemFluids.of(rest).amount() == FluidVolumes.MUG, "water drunk without hangover (original behaviour changed)");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void heavyHangoverVomits(GameTestHelper helper) {
        Player player = survivalPlayer(helper);
        setAlcohol(player, 0.9);
        soberUp(helper, player);
        tickDrugs(player, 20);
        helper.assertTrue(hangover(player).getLevel() == 3, "level " + hangover(player).getLevel());
        int food = player.getFoodData().getFoodLevel();
        tickDrugs(player, 5 * 60 * 20 + 20);
        helper.assertTrue(player.getFoodData().getFoodLevel() <= food - 4, "food " + food + " -> " + player.getFoodData().getFoodLevel());
        var vomit = helper.getLevel().getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(4),
                item -> item.getItem().is(PSItems.VOMIT));
        helper.assertFalse(vomit.isEmpty(), "no vomit dropped");
        vomit.forEach(ItemEntity::discard);
        helper.succeed();
    }
}
