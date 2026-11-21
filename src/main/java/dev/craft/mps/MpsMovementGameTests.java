package dev.craft.mps;

import dev.craft.Craft;
import dev.craft.core.MovementCap;
import dev.craft.mps.module.MpsModules;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Phase 28.2: movement modules, weight, step height and fall height (spec 29 §6). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class MpsMovementGameTests {
    private static final double EPSILON = 1.0E-6;
    private static final MpsInput JUMP = new MpsInput(0.0F, 0.0F, true, false, false);
    private static final MpsInput SNEAK = new MpsInput(0.0F, 0.0F, false, true, false);

    private MpsMovementGameTests() {
    }

    private static void assertNear(GameTestHelper helper, double actual, double expected, String message) {
        helper.assertTrue(Math.abs(actual - expected) < EPSILON, message + ": expected " + expected + ", got "
                + actual);
    }

    /** A worn piece with a full basic battery and the given modules, sliders at 1. */
    private static ItemStack wear(Player player, EquipmentSlot slot, ItemStack stack, String... modules) {
        ModularItems.addModule(stack, MpsModules.BASIC_BATTERY);
        for (String module : modules) {
            ModularItems.addModule(stack, module);
            for (String tweak : MpsModules.get(module).getTweaks()) {
                ModularItems.setTweak(stack, module, tweak, 1.0);
            }
        }
        MpsEnergy.setJoules(stack, 20000.0);
        player.setItemSlot(slot, stack);
        return player.getItemBySlot(slot);
    }

    /** Without flight control: jump held and motionY below 0.5 adds the full thrust for the full consumption. */
    @GameTest(template = "empty_8x4x8")
    public static void jetpackThrust(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack torso = wear(player, EquipmentSlot.CHEST, MpsItems.POWER_ARMOR_TORSO.toStack(), MpsModules.JETPACK);
        ItemStack boots = wear(player, EquipmentSlot.FEET, MpsItems.POWER_ARMOR_FEET.toStack(), MpsModules.JET_BOOTS);
        player.setDeltaMovement(0.0, 0.0, 0.0);
        MpsMovement.tick(player, JUMP);
        assertNear(helper, player.getDeltaMovement().y, 0.16 + 0.08, "jetpack + jet boots thrust");
        assertNear(helper, MpsEnergy.getJoules(boots), 20000.0 - 225.0, "150 + 75 J from the boots first");
        assertNear(helper, MpsEnergy.getJoules(torso), 20000.0, "torso untouched");
        player.setDeltaMovement(0.0, 0.6, 0.0);
        MpsMovement.tick(player, JUMP);
        assertNear(helper, player.getDeltaMovement().y, 0.6, "no thrust from 0.5 up");
        MpsMovement.tick(player, MpsInput.NONE);
        assertNear(helper, MpsEnergy.getJoules(boots), 20000.0 - 225.0, "no jump, no cost");
        helper.succeed();
    }

    /** Flight control brakes the fall first and hovers without keys; cost proportional to the used thrust. */
    @GameTest(template = "empty_8x4x8")
    public static void flightControlHover(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        wear(player, EquipmentSlot.HEAD, MpsItems.POWER_ARMOR_HEAD.toStack(), MpsModules.FLIGHT_CONTROL);
        ItemStack torso = wear(player, EquipmentSlot.CHEST, MpsItems.POWER_ARMOR_TORSO.toStack(), MpsModules.JETPACK);
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        helper.assertTrue(boots.isEmpty(), "no boots");
        player.setDeltaMovement(0.05, -0.1, 0.0);
        MpsMovement.tick(player, MpsInput.NONE);
        assertNear(helper, player.getDeltaMovement().y, 0.0, "fall stopped");
        assertNear(helper, player.getDeltaMovement().x, 0.0, "drift removed");
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        assertNear(helper, MpsEnergy.getJoules(torso), 20000.0 - 150.0 * 0.1 - 150.0 * 0.05, "150 J × used speed");
        assertNear(helper, MpsEnergy.getJoules(head), 20000.0, "torso pays before the helmet");
        player.setDeltaMovement(0.0, 0.0, 0.0);
        MpsMovement.tick(player, JUMP);
        assertNear(helper, player.getDeltaMovement().y, 0.16, "jump = straight up");
        player.setDeltaMovement(0.0, -2.0, 0.0);
        MpsMovement.tick(player, new MpsInput(0.0F, 0.0F, false, false, true));
        // original: capping at -1 makes the remaining thrust negative (0.16 - 1), so "down" pushes up by 0.84
        assertNear(helper, player.getDeltaMovement().y, -0.16, "fall capped at -1");
        helper.succeed();
    }

    /** Glider: Δ = min(0.08, −0.1 − motionY) up and forward, air speed +0.03; parachute scales to 0.1. */
    @GameTest(template = "empty_8x4x8")
    public static void gliderAndParachute(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack torso = wear(player, EquipmentSlot.CHEST, MpsItems.POWER_ARMOR_TORSO.toStack(), MpsModules.GLIDER);
        player.setDeltaMovement(0.0, -0.5, 0.0);
        MpsMovement.tick(player, SNEAK);
        assertNear(helper, player.getDeltaMovement().y, -0.42, "glider brakes 0.08");
        assertNear(helper, player.getDeltaMovement().z, 0.08, "and pushes forward");
        assertNear(helper, MpsMovement.getAirSpeed(player), 0.05F, "air speed 0.02 + 0.03");
        assertNear(helper, MpsEnergy.getJoules(torso), 20000.0, "glider is free");
        ModularItems.removeModule(torso, MpsModules.GLIDER);
        ModularItems.addModule(torso, MpsModules.PARACHUTE);
        player.setDeltaMovement(0.3, -0.4, 0.0);
        MpsMovement.tick(player, SNEAK);
        assertNear(helper, player.getDeltaMovement().length(), 0.1, "parachute speed 0.1");
        assertNear(helper, player.getDeltaMovement().y, -0.08, "direction kept");
        player.setDeltaMovement(0.0, -0.4, 0.0);
        MpsMovement.tick(player, MpsInput.NONE);
        assertNear(helper, player.getDeltaMovement().y, -0.4, "only while sneaking");
        helper.succeed();
    }

    /** Jump Assist pays once per jump, then adds 0.15 per tick while jump is held for Jump Boost × 2 ticks. */
    @GameTest(template = "empty_8x4x8")
    public static void jumpAssist(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack legs = wear(player, EquipmentSlot.LEGS, MpsItems.POWER_ARMOR_LEGS.toStack(), MpsModules.JUMP_ASSIST);
        MpsMovement.onJump(new LivingEvent.LivingJumpEvent(player));
        assertNear(helper, MpsMovement.getJumpTicks(player), 10.0, "Jump Boost 5 × 2");
        assertNear(helper, MpsEnergy.getJoules(legs), 20000.0 - 30.0, "25 + 5 J per jump");
        player.setDeltaMovement(0.0, 0.42, 0.0);
        MpsMovement.tick(player, JUMP);
        assertNear(helper, player.getDeltaMovement().y, 0.57, "+0.15");
        assertNear(helper, MpsMovement.getJumpTicks(player), 9.0, "one tick used");
        helper.assertTrue(MpsMovement.getAirSpeed(player) == null, "no extra air control (spec 37)");
        MpsMovement.tick(player, MpsInput.NONE);
        assertNear(helper, MpsMovement.getJumpTicks(player), 0.0, "released: ticks gone");
        helper.succeed();
    }

    /**
     * Spec 37 §3.6: Sprint Assist changes no speed; sprinting still pays 12 J × v × 5, walking is free; Power raises the
     * weight capacity by 25 000 g.
     */
    @GameTest(template = "empty_8x4x8")
    public static void sprintAssist(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack legs = wear(player, EquipmentSlot.LEGS, MpsItems.POWER_ARMOR_LEGS.toStack(),
                MpsModules.SPRINT_ASSIST);
        player.setDeltaMovement(0.2, 0.0, 0.0);
        MpsMovement.tick(player, MpsInput.NONE);
        assertNear(helper, player.getAttributeValue(Attributes.MOVEMENT_SPEED), 0.1, "walking unchanged");
        assertNear(helper, MpsEnergy.getJoules(legs), 20000.0, "walking is free");
        player.setSprinting(true);
        MpsMovement.tick(player, MpsInput.NONE);
        assertNear(helper, player.getAttributeValue(Attributes.MOVEMENT_SPEED), 0.1 * 1.3, "sprinting unchanged");
        assertNear(helper, MpsEnergy.getJoules(legs), 20000.0 - 12.0 * 0.2 * 5.0, "sprint 12 J × v × 5");
        helper.assertTrue(MpsMovement.getAirSpeed(player) == null, "no extra air speed");
        for (EquipmentSlot slot : new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST}) {
            ItemStack stack = (slot == EquipmentSlot.HEAD ? MpsItems.POWER_ARMOR_HEAD : MpsItems.POWER_ARMOR_TORSO)
                    .toStack();
            ModularItems.addModule(stack, MpsModules.IRON_PLATING);
            ModularItems.setTweak(stack, MpsModules.IRON_PLATING, "Plating Thickness", 1.0);
            player.setItemSlot(slot, stack);
        }
        ModularItems.addModule(legs, MpsModules.IRON_PLATING);
        ModularItems.setTweak(legs, MpsModules.IRON_PLATING, "Plating Thickness", 1.0);
        player.setSprinting(false);
        player.setDeltaMovement(0.2, 0.0, 0.0);
        MpsMovement.tick(player, MpsInput.NONE);
        assertNear(helper, player.getDeltaMovement().x, 0.2, "32 kg under 50 kg capacity: not slowed");
        ModularItems.setActive(legs, MpsModules.SPRINT_ASSIST, false);
        MpsMovement.tick(player, MpsInput.NONE);
        assertNear(helper, player.getDeltaMovement().x, 0.2 * 25000.0 / 32000.0, "switched off: slowed");
        helper.succeed();
    }

    /** Spec 37 §1: jets with flight control never push faster than sprinting. */
    @GameTest(template = "empty_8x4x8")
    public static void jetsCappedToSprintSpeed(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        wear(player, EquipmentSlot.HEAD, MpsItems.POWER_ARMOR_HEAD.toStack(), MpsModules.FLIGHT_CONTROL);
        wear(player, EquipmentSlot.CHEST, MpsItems.POWER_ARMOR_TORSO.toStack(), MpsModules.JETPACK);
        wear(player, EquipmentSlot.FEET, MpsItems.POWER_ARMOR_FEET.toStack(), MpsModules.JET_BOOTS);
        player.setDeltaMovement(0.27, 0.0, 0.0);
        player.setYRot(-90.0F);
        player.setXRot(0.0F);
        MpsMovement.tick(player, new MpsInput(1.0F, 0.0F, false, false, false));
        Vec3 motion = player.getDeltaMovement();
        helper.assertTrue(motion.horizontalDistance() <= MovementCap.MAX_HORIZONTAL + EPSILON,
                "capped, got " + motion.horizontalDistance());
        helper.succeed();
    }

    /**
     * Spec 37 §3.6: the Swim Boost gives no thrust; under water it pays 100 J per tick and mining is as fast as on
     * land (without ground too).
     */
    @GameTest(template = "empty_8x4x8")
    public static void swimBoostMinesUnderwater(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        helper.setBlock(pos, Blocks.WATER);
        helper.setBlock(pos.above(), Blocks.WATER);
        helper.setBlock(pos.above(2), Blocks.WATER);
        Vec3 at = helper.absoluteVec(new Vec3(3.5, 1.5, 3.5));
        Player diver = helper.makeMockPlayer(GameType.SURVIVAL);
        Player other = helper.makeMockPlayer(GameType.SURVIVAL);
        for (Player player : new Player[] {diver, other}) {
            player.setPos(at.x, at.y, at.z);
            player.baseTick();
            player.setOnGround(false);
        }
        ItemStack legs = wear(diver, EquipmentSlot.LEGS, MpsItems.POWER_ARMOR_LEGS.toStack(), MpsModules.SWIM_BOOST);
        wear(other, EquipmentSlot.LEGS, MpsItems.POWER_ARMOR_LEGS.toStack());
        helper.assertTrue(diver.isInWater() && diver.isEyeInFluid(net.minecraft.tags.FluidTags.WATER), "under water");
        diver.setDeltaMovement(0.0, 0.0, 0.0);
        MpsMovement.tick(diver, new MpsInput(1.0F, 0.0F, false, false, false));
        MpsMovement.tick(other, MpsInput.NONE);
        assertNear(helper, diver.getDeltaMovement().horizontalDistance(), 0.0, "no swim thrust");
        assertNear(helper, MpsEnergy.getJoules(legs), 20000.0 - 100.0, "100 J per tick");
        BlockPos stone = helper.absolutePos(new BlockPos(3, 0, 3));
        float diverSpeed = diver.getDigSpeed(Blocks.STONE.defaultBlockState(), stone);
        float otherSpeed = other.getDigSpeed(Blocks.STONE.defaultBlockState(), stone);
        assertNear(helper, otherSpeed, 1.0 * 0.2 / 5.0, "vanilla: ×0.2 under water, ÷5 swimming");
        assertNear(helper, diverSpeed, 1.0, "swim boost: as on land");
        helper.succeed();
    }

    /** Step height 1.001 while active; switched off on worn leggings back to the default. */
    @GameTest(template = "empty_8x4x8")
    public static void stepAssist(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack legs = wear(player, EquipmentSlot.LEGS, MpsItems.POWER_ARMOR_LEGS.toStack(),
                MpsModules.UPHILL_STEP_ASSIST);
        MpsMovement.tick(player, MpsInput.NONE);
        assertNear(helper, player.getAttributeValue(Attributes.STEP_HEIGHT), 1.001, "step assist");
        ModularItems.setActive(legs, MpsModules.UPHILL_STEP_ASSIST, false);
        MpsMovement.tick(player, MpsInput.NONE);
        assertNear(helper, player.getAttributeValue(Attributes.STEP_HEIGHT), 0.6, "default");
        helper.succeed();
    }

    /** Shock Absorber above 3 m: Distance Reduction × height absorbed for Impact consumption J per metre. */
    @GameTest(template = "empty_8x4x8")
    public static void shockAbsorber(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack boots = wear(player, EquipmentSlot.FEET, MpsItems.POWER_ARMOR_FEET.toStack(),
                MpsModules.SHOCK_ABSORBER);
        LivingFallEvent fall = new LivingFallEvent(player, 10.0F, 1.0F);
        MpsMovement.onFall(fall);
        assertNear(helper, fall.getDistance(), 0.0, "fully absorbed");
        assertNear(helper, MpsEnergy.getJoules(boots), 20000.0 - 100.0, "10 m × 10 J");
        LivingFallEvent small = new LivingFallEvent(player, 3.0F, 1.0F);
        MpsMovement.onFall(small);
        assertNear(helper, small.getDistance(), 3.0, "3 m not absorbed");
        helper.succeed();
    }

    /** Fall height from the velocity only with a modular item; over 25 kg horizontal motion is slowed. */
    @GameTest(template = "empty_8x4x8")
    public static void fallHeightAndWeight(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.fallDistance = 7.0F;
        player.setDeltaMovement(0.0, MpsMovement.DEFAULT_GRAVITY * 10.0, 0.0);
        MpsMovement.tick(player, MpsInput.NONE);
        assertNear(helper, player.fallDistance, 7.0, "vanilla without powersuit");
        wear(player, EquipmentSlot.FEET, MpsItems.POWER_ARMOR_FEET.toStack());
        MpsMovement.tick(player, MpsInput.NONE);
        assertNear(helper, player.fallDistance, (float) (0.03920000076293945 * 100.0), "0.0392 × ticks²");
        player.setDeltaMovement(0.0, 0.3, 0.0);
        MpsMovement.tick(player, MpsInput.NONE);
        assertNear(helper, player.fallDistance, 0.0, "rising: 0");
        for (int i = 0; i < 3; i++) {
            EquipmentSlot slot = new EquipmentSlot[] {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS}[i];
            ItemStack stack = MpsItems.armor().get(i).toStack();
            ModularItems.addModule(stack, MpsModules.IRON_PLATING);
            ModularItems.setTweak(stack, MpsModules.IRON_PLATING, "Plating Thickness", 1.0);
            player.setItemSlot(slot, stack);
        }
        double weight = ModularItems.getPlayerWeight(player);
        assertNear(helper, weight, 30000.0 + 2000.0, "3 × 10 kg plating + battery");
        player.setDeltaMovement(1.0, 0.0, 0.0);
        MpsMovement.tick(player, MpsInput.NONE);
        assertNear(helper, player.getDeltaMovement().x, 25000.0 / weight, "slowed by capacity / weight");
        helper.succeed();
    }
}
