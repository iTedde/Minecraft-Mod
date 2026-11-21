package dev.craft.mps;

import dev.craft.Craft;
import dev.craft.mps.MpsDataComponents.FoodBuffer;
import dev.craft.mps.item.PowerArmorItem;
import dev.craft.mps.module.MpsModules;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Phase 28.3: environment and special modules, keybinding toggle and cosmetics (spec 29 §5, §7, §9). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class MpsEnvironmentGameTests {
    private static final double EPSILON = 1.0E-6;

    private MpsEnvironmentGameTests() {
    }

    private static void assertNear(GameTestHelper helper, double actual, double expected, String message) {
        helper.assertTrue(Math.abs(actual - expected) < EPSILON, message + ": expected " + expected + ", got "
                + actual);
    }

    /** A worn piece with a basic battery holding {@code joules} and the given modules, sliders at {@code tweak}. */
    private static ItemStack wear(Player player, EquipmentSlot slot, ItemStack stack, double joules, double tweak,
                                  String... modules) {
        ModularItems.addModule(stack, MpsModules.BASIC_BATTERY);
        for (String module : modules) {
            ModularItems.addModule(stack, module);
            for (String name : MpsModules.get(module).getTweaks()) {
                ModularItems.setTweak(stack, module, name, tweak);
            }
        }
        MpsEnergy.setJoules(stack, joules);
        player.setItemSlot(slot, stack);
        return player.getItemBySlot(slot);
    }

    /**
     * Night vision 5 J and camouflage 50 J, renewed below 210 ticks; with both effects the camouflage is renewed every
     * tick and stays when switched off (original loop), foreign effects are left alone.
     */
    @GameTest(template = "empty_8x4x8")
    public static void nightVisionAndCamouflage(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack helmet = wear(player, EquipmentSlot.HEAD, MpsItems.POWER_ARMOR_HEAD.toStack(), 20000.0, 0.0,
                MpsModules.NIGHT_VISION);
        ItemStack torso = wear(player, EquipmentSlot.CHEST, MpsItems.POWER_ARMOR_TORSO.toStack(), 20000.0, 0.0,
                MpsModules.ACTIVE_CAMOUFLAGE);
        MpsMovement.tick(player, MpsInput.NONE);
        MobEffectInstance nightVision = MpsEnvironment.ownEffect(player, MobEffects.NIGHT_VISION);
        helper.assertTrue(nightVision != null && nightVision.getDuration() == 500, "own night vision 500 ticks");
        helper.assertTrue(MpsEnvironment.ownEffect(player, MobEffects.INVISIBILITY) != null, "own invisibility");
        assertNear(helper, MpsEnergy.getJoules(torso), 20000.0 - 55.0, "5 + 50 J, torso pays before the helmet");
        MpsMovement.tick(player, MpsInput.NONE);
        assertNear(helper, MpsEnergy.getJoules(torso), 20000.0 - 105.0, "camouflage renewed, night vision not");
        ModularItems.setActive(helmet, MpsModules.NIGHT_VISION, false);
        MpsMovement.tick(player, MpsInput.NONE);
        helper.assertTrue(player.getEffect(MobEffects.NIGHT_VISION) == null, "night vision removed when off");
        ModularItems.setActive(torso, MpsModules.ACTIVE_CAMOUFLAGE, false);
        MpsMovement.tick(player, MpsInput.NONE);
        helper.assertTrue(player.getEffect(MobEffects.INVISIBILITY) == null, "invisibility removed when off");
        player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 400));
        MpsMovement.tick(player, MpsInput.NONE);
        helper.assertTrue(player.getEffect(MobEffects.NIGHT_VISION) != null, "potion effect kept");
        assertNear(helper, MpsEnergy.getJoules(torso), 20000.0 - 155.0, "nothing more paid");
        helper.succeed();
    }

    /**
     * Efficiency 50 %: four bread (5 × 0.5 each) fill the buffer for 10 missing points, the player eats 10 with
     * saturation modifier 1 and pays 10 × 100 J × 10.
     */
    @GameTest(template = "empty_8x4x8")
    public static void autoFeeder(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack helmet = wear(player, EquipmentSlot.HEAD, MpsItems.POWER_ARMOR_HEAD.toStack(), 20000.0, 0.0,
                MpsModules.AUTO_FEEDER);
        player.getInventory().items.set(0, new ItemStack(Items.BREAD, 5));
        player.getFoodData().setFoodLevel(10);
        double drain = MpsEnvironment.autoFeeder(player, helmet, MpsEnergy.getPlayerEnergy(player), 0.0);
        helper.assertTrue(player.getInventory().items.get(0).getCount() == 1, "four bread eaten");
        helper.assertTrue(player.getFoodData().getFoodLevel() == 20, "food level full");
        assertNear(helper, drain, 10000.0, "10 needed × 100 J × 10 eaten");
        FoodBuffer buffer = helmet.get(MpsDataComponents.FOOD.get());
        helper.assertTrue(buffer != null, "buffer stored");
        assertNear(helper, buffer.food(), 0.0, "food buffer used up");
        helper.assertTrue(Math.abs(buffer.saturation() - 0.2) < 1.0E-4, "saturation 4 × 0.3 − 1, got "
                + buffer.saturation());
        drain = MpsEnvironment.autoFeeder(player, helmet, MpsEnergy.getPlayerEnergy(player), 0.0);
        assertNear(helper, drain, 0.0, "nothing needed");
        helper.assertTrue(player.getInventory().items.get(0).getCount() == 1, "no more bread eaten");
        helper.succeed();
    }

    /** Below 10 air the helmet pays 1 000 J and refills to 300. */
    @GameTest(template = "empty_8x4x8")
    public static void waterElectrolyzer(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack helmet = wear(player, EquipmentSlot.HEAD, MpsItems.POWER_ARMOR_HEAD.toStack(), 20000.0, 0.0,
                MpsModules.WATER_ELECTROLYZER);
        player.setAirSupply(20);
        MpsEnvironment.waterElectrolyzer(player);
        helper.assertTrue(player.getAirSupply() == 20, "air 20 untouched");
        player.setAirSupply(5);
        MpsEnvironment.waterElectrolyzer(player);
        helper.assertTrue(player.getAirSupply() == 300, "air refilled");
        assertNear(helper, MpsEnergy.getJoules(helmet), 19000.0, "1 000 J");
        helper.succeed();
    }

    /** Sky above the head: 100 J by day, 10 J by night; a roof stops it. */
    @GameTest(template = "empty_8x4x8")
    public static void solarGenerator(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack helmet = wear(player, EquipmentSlot.HEAD, MpsItems.POWER_ARMOR_HEAD.toStack(), 0.0, 0.0,
                MpsModules.SOLAR_GENERATOR);
        player.setPos(helper.absoluteVec(new Vec3(2.5, 1.0, 2.5)));
        if (!helper.getLevel().isRaining() && helper.getLevel().canSeeSky(player.blockPosition().above())) {
            double expected = helper.getLevel().isDay() ? 100.0 : 10.0;
            assertNear(helper, MpsEnvironment.solarPayout(player, helmet), expected, "solar payout");
            assertNear(helper, MpsEnergy.getJoules(helmet), expected, "stored in the helmet");
        }
        helper.setBlock(new BlockPos(2, 3, 2), Blocks.STONE);
        helper.runAfterDelay(10, () -> {
            assertNear(helper, MpsEnvironment.solarPayout(player, helmet), 0.0, "roofed (sky light updated)");
            helper.succeed();
        });
    }

    /** 250 J every 5 blocks from the stored position; nothing while {@code hasImpulse} is set. */
    @GameTest(template = "empty_8x4x8")
    public static void kineticGenerator(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack pants = wear(player, EquipmentSlot.LEGS, MpsItems.POWER_ARMOR_LEGS.toStack(), 0.0, 0.0,
                MpsModules.KINETIC_GENERATOR);
        Vec3 start = helper.absoluteVec(new Vec3(0.5, 1.0, 0.5));
        // truncated coordinates: stay on one side of x = 0
        double direction = start.x >= 0.0 ? 1.0 : -1.0;
        player.setPos(start);
        MpsEnvironment.generators(player, ItemStack.EMPTY, pants, 0.0, 0.0);
        player.setPos(start.add(4.0 * direction, 0.0, 0.0));
        MpsEnvironment.generators(player, ItemStack.EMPTY, pants, 0.0, 0.0);
        assertNear(helper, MpsEnergy.getJoules(pants), 0.0, "4 blocks");
        player.setPos(start.add(5.0 * direction, 0.0, 0.0));
        player.hasImpulse = true;
        MpsEnvironment.generators(player, ItemStack.EMPTY, pants, 0.0, 0.0);
        assertNear(helper, MpsEnergy.getJoules(pants), 0.0, "airborne");
        player.hasImpulse = false;
        MpsEnvironment.generators(player, ItemStack.EMPTY, pants, 0.0, 0.0);
        assertNear(helper, MpsEnergy.getJoules(pants), 250.0, "5 blocks");
        player.setPos(start.add(8.0 * direction, 0.0, 0.0));
        MpsEnvironment.generators(player, ItemStack.EMPTY, pants, 0.0, 0.0);
        assertNear(helper, MpsEnergy.getJoules(pants), 250.0, "3 more blocks");
        helper.succeed();
    }

    /** A keybinding switches toggleable modules on all equipped items; passive modules stay on. */
    @GameTest(template = "empty_8x4x8")
    public static void toggleModules(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack helmet = wear(player, EquipmentSlot.HEAD, MpsItems.POWER_ARMOR_HEAD.toStack(), 0.0, 0.0,
                MpsModules.NIGHT_VISION);
        ItemStack torso = wear(player, EquipmentSlot.CHEST, MpsItems.POWER_ARMOR_TORSO.toStack(), 0.0, 0.0,
                MpsModules.NIGHT_VISION, MpsModules.JETPACK);
        MpsEnvironment.toggleForPlayer(player, MpsModules.NIGHT_VISION, false);
        MpsEnvironment.toggleForPlayer(player, MpsModules.BASIC_BATTERY, false);
        helper.assertFalse(ModularItems.hasActiveModule(helmet, MpsModules.NIGHT_VISION), "helmet off");
        helper.assertFalse(ModularItems.hasActiveModule(torso, MpsModules.NIGHT_VISION), "torso off");
        helper.assertTrue(ModularItems.hasActiveModule(torso, MpsModules.JETPACK), "jetpack untouched");
        helper.assertTrue(ModularItems.hasActiveModule(helmet, MpsModules.BASIC_BATTERY), "battery always on");
        MpsEnvironment.toggleForPlayer(player, MpsModules.NIGHT_VISION, true);
        helper.assertTrue(ModularItems.hasActiveModule(helmet, MpsModules.NIGHT_VISION), "on again");
        helper.succeed();
    }

    /** Tint from the colour module, texture by Transparent Armor and Citizen Joe Style. */
    @GameTest(template = "empty_8x4x8")
    public static void cosmetics(GameTestHelper helper) {
        ItemStack legs = MpsItems.POWER_ARMOR_LEGS.toStack();
        helper.assertTrue(ModularItems.getTintColour(legs) == 0xFFFFFFFF, "white without module");
        ModularItems.addModule(legs, MpsModules.CUSTOM_COLOUR);
        ModularItems.setTweak(legs, MpsModules.CUSTOM_COLOUR, "Red Intensity", 1.0);
        helper.assertTrue(ModularItems.getTintColour(legs) == 0xFFFF0000, "red");
        ModularItems.setTweak(legs, MpsModules.CUSTOM_COLOUR, "Red Intensity", 0.5);
        helper.assertTrue(ModularItems.getTintColour(legs) == 0xFFFF7F7F, "light red");
        ModularItems.setActive(legs, MpsModules.CUSTOM_COLOUR, false);
        helper.assertTrue(ModularItems.getTintColour(legs) == 0xFFFFFFFF, "white when off");
        PowerArmorItem item = (PowerArmorItem) legs.getItem();
        ArmorMaterial.Layer layer = MpsItems.POWER_ARMOR_MATERIAL.value().layers().get(0);
        helper.assertTrue(texture(item, legs, layer).equals("power_armor_layer_2"), "default pants");
        ModularItems.addModule(legs, MpsModules.CITIZEN_JOE_STYLE);
        helper.assertTrue(texture(item, legs, layer).equals("power_armor_joe_layer_2"), "joe pants");
        ModularItems.addModule(legs, MpsModules.TRANSPARENT_ARMOR);
        helper.assertTrue(texture(item, legs, layer).equals("power_armor_blank"), "transparent");
        ModularItems.setActive(legs, MpsModules.TRANSPARENT_ARMOR, false);
        helper.assertTrue(texture(item, legs, layer).equals("power_armor_joe_layer_2"), "transparent off");
        ItemStack head = MpsItems.POWER_ARMOR_HEAD.toStack();
        helper.assertTrue(texture((PowerArmorItem) head.getItem(), head, layer).equals("power_armor_layer_1"),
                "default helmet");
        helper.succeed();
    }

    private static String texture(PowerArmorItem item, ItemStack stack, ArmorMaterial.Layer layer) {
        String path = item.getArmorTexture(stack, null, item.getEquipmentSlot(), layer, false).getPath();
        return path.substring(path.lastIndexOf('/') + 1, path.length() - ".png".length());
    }
}
