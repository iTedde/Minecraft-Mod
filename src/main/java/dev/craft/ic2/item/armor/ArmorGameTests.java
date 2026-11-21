package dev.craft.ic2.item.armor;

import java.util.List;
import java.util.Optional;

import dev.craft.Craft;
import dev.craft.core.MovementCap;
import dev.craft.core.CraftDamageTypes;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2MobEffects;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.keyboard.Ic2Keyboard;
import dev.craft.ic2.item.tool.NanoSaberItem;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for the IC2 armor (phase 6.3). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class ArmorGameTests {
    private static final String EMPTY = "empty_8x4x8";

    private ArmorGameTests() {
    }

    private static ItemStack charged(ItemStack stack) {
        return ElectricItems.charged(stack);
    }

    private static void wear(Player player, ItemStack helmet, ItemStack chest, ItemStack legs, ItemStack boots) {
        player.setItemSlot(EquipmentSlot.HEAD, helmet);
        player.setItemSlot(EquipmentSlot.CHEST, chest);
        player.setItemSlot(EquipmentSlot.LEGS, legs);
        player.setItemSlot(EquipmentSlot.FEET, boots);
    }

    private static void tickArmor(Player player, EquipmentSlot slot) {
        ItemStack stack = player.getItemBySlot(slot);
        stack.inventoryTick(player.level(), player, 0, false);
    }

    /** 10 damage = 250 units; nano absorbs 90 %, each piece pays (int)(absorbed / 25) · 800 EU. */
    @GameTest(template = EMPTY)
    public static void nanoSuitAbsorbsForEnergy(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        wear(player, charged(Ic2Items.NANO_HELMET.toStack()), charged(Ic2Items.NANO_CHESTPLATE.toStack()),
                charged(Ic2Items.NANO_LEGGINGS.toStack()), charged(Ic2Items.NANO_BOOTS.toStack()));
        player.hurt(player.damageSources().cactus(), 10.0F);
        helper.assertValueEqual(Math.round(player.getHealth() * 1000), 19000, "health");
        helper.assertValueEqual(ElectricItems.getCharge(player.getItemBySlot(EquipmentSlot.HEAD)), 99200, "helmet");
        helper.assertValueEqual(ElectricItems.getCharge(player.getItemBySlot(EquipmentSlot.CHEST)), 97600, "chest");
        helper.assertValueEqual(ElectricItems.getCharge(player.getItemBySlot(EquipmentSlot.LEGS)), 98400, "legs");
        helper.assertValueEqual(ElectricItems.getCharge(player.getItemBySlot(EquipmentSlot.FEET)), 99200, "boots");
        helper.succeed();
    }

    /** Empty electric armor absorbs nothing; mixed with iron the Forge ratios apply (iron chest 6/25). */
    @GameTest(template = EMPTY)
    public static void emptyNanoAbsorbsNothing(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        wear(player, Ic2Items.NANO_HELMET.toStack(), new ItemStack(Items.IRON_CHESTPLATE),
                ItemStack.EMPTY, ItemStack.EMPTY);
        player.hurt(player.damageSources().cactus(), 10.0F);
        // 250 · (1 − 6/25) = 190 units = 7.6 damage
        helper.assertValueEqual(Math.round(player.getHealth() * 1000), 12400, "health");
        helper.assertValueEqual(player.getItemBySlot(EquipmentSlot.CHEST).getDamageValue(), 2, "iron chest damage");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void nanoBootsCancelShortFalls(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemSlot(EquipmentSlot.FEET, charged(Ic2Items.NANO_BOOTS.toStack()));
        player.causeFallDamage(10.0F, 1.0F, player.damageSources().fall());
        helper.assertValueEqual(player.getHealth(), 20.0F, "health after 10 blocks");
        helper.assertValueEqual(ElectricItems.getCharge(player.getItemBySlot(EquipmentSlot.FEET)), 100000 - 7 * 800,
                "boots charge");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void fullHazmatBlocksLava(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        wear(player, Ic2Items.HAZMAT_HELMET.toStack(), Ic2Items.HAZMAT_CHESTPLATE.toStack(),
                Ic2Items.HAZMAT_LEGGINGS.toStack(), Ic2Items.RUBBER_BOOTS.toStack());
        player.hurt(player.damageSources().source(CraftDamageTypes.ELECTRICITY), 6.0F);
        helper.assertValueEqual(player.getHealth(), 20.0F, "health after electricity");
        helper.assertValueEqual(player.getItemBySlot(EquipmentSlot.CHEST).getDamageValue(), 0, "suit durability");
        player.invulnerableTime = 0;
        player.hurt(player.damageSources().lava(), 4.0F);
        helper.assertValueEqual(player.getHealth(), 20.0F, "health after lava");
        helper.assertTrue(player.hasEffect(MobEffects.FIRE_RESISTANCE), "fire resistance");
        helper.succeed();
    }

    /** Without full set every hazmat piece takes ratio 1.0 up to (rest+1)/2·25 and loses 2× durability. */
    @GameTest(template = EMPTY)
    public static void hazmatPieceWearsDown(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemSlot(EquipmentSlot.CHEST, Ic2Items.HAZMAT_CHESTPLATE.toStack());
        player.hurt(player.damageSources().cactus(), 4.0F);
        helper.assertValueEqual(player.getHealth(), 20.0F, "health");
        helper.assertValueEqual(player.getItemBySlot(EquipmentSlot.CHEST).getDamageValue(), 8, "durability");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void quantumHelmetCuresEffects(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemSlot(EquipmentSlot.HEAD, charged(Ic2Items.QUANTUM_HELMET.toStack()));
        player.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1));
        player.addEffect(new MobEffectInstance(Ic2MobEffects.RADIATION, 200, 0));
        player.setAirSupply(50);
        tickArmor(player, EquipmentSlot.HEAD);
        helper.assertFalse(player.hasEffect(MobEffects.POISON), "poison removed");
        helper.assertFalse(player.hasEffect(Ic2MobEffects.RADIATION), "radiation removed");
        helper.assertValueEqual(player.getAirSupply(), 250, "air");
        helper.assertValueEqual(ElectricItems.getCharge(player.getItemBySlot(EquipmentSlot.HEAD)),
                1000000 - 1000 - 20000 - 10000, "helmet charge");
        helper.succeed();
    }

    /** IC2 quirk: each fuel jetpack use costs 4 more than asked. */
    @GameTest(template = EMPTY)
    public static void jetpackFuelQuirk(GameTestHelper helper) {
        JetpackItem jetpack = Ic2Items.JETPACK.get();
        ItemStack stack = jetpack.createFull();
        helper.assertValueEqual(jetpack.getCharge(stack), 17998, "full charge");
        jetpack.use(stack, 9);
        helper.assertValueEqual(jetpack.getCharge(stack), 17985, "after one use");
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack electric = charged(Ic2Items.ELECTRIC_JETPACK.toStack());
        player.setItemSlot(EquipmentSlot.CHEST, electric);
        helper.assertTrue(Ic2Items.ELECTRIC_JETPACK.get().useJetpack(player, electric, false), "electric used");
        helper.assertValueEqual(ElectricItems.getCharge(electric), 30000 - 7, "electric charge");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void staticBootsChargeChest(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemSlot(EquipmentSlot.FEET, Ic2Items.STATIC_BOOTS.toStack());
        player.setItemSlot(EquipmentSlot.CHEST, Ic2Items.BATPACK.toStack());
        BlockPos start = helper.absolutePos(new BlockPos(0, 1, 0));
        player.setPos(start.getX() + 0.5, start.getY(), start.getZ() + 0.5);
        tickArmor(player, EquipmentSlot.FEET);
        player.setPos(start.getX() + 10.5, start.getY(), start.getZ() + 0.5);
        tickArmor(player, EquipmentSlot.FEET);
        helper.assertValueEqual(ElectricItems.getCharge(player.getItemBySlot(EquipmentSlot.CHEST)), 2, "batpack");
        helper.succeed();
    }

    /** Only nano armor is drained (IC2 checks the nano class); an emptied piece is deleted. */
    @GameTest(template = EMPTY)
    public static void nanoSaberDrainsNanoArmor(GameTestHelper helper) {
        Player target = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack nanoChest = Ic2Items.NANO_CHESTPLATE.toStack();
        ElectricItems.charge(nanoChest, 4800, Integer.MAX_VALUE, true, false);
        target.setItemSlot(EquipmentSlot.CHEST, nanoChest);
        target.setItemSlot(EquipmentSlot.FEET, charged(Ic2Items.QUANTUM_BOOTS.toStack()));
        ItemStack saber = charged(Ic2Items.NANO_SABER.toStack());
        Player wielder = helper.makeMockPlayer(GameType.SURVIVAL);
        wielder.setItemInHand(InteractionHand.MAIN_HAND, saber);
        saber.use(helper.getLevel(), wielder, InteractionHand.MAIN_HAND);
        helper.assertTrue(NanoSaberItem.isActive(saber), "saber on");
        Zombie attacker = helper.spawnWithNoFreeWill(EntityType.ZOMBIE, new BlockPos(1, 1, 1));
        Ic2Items.NANO_SABER.get().hurtEnemy(saber, target, attacker);
        helper.assertTrue(target.getItemBySlot(EquipmentSlot.CHEST).isEmpty(), "nano chest deleted");
        helper.assertValueEqual(ElectricItems.getCharge(target.getItemBySlot(EquipmentSlot.FEET)), 1000000,
                "quantum boots untouched");
        helper.assertValueEqual(ElectricItems.getCharge(saber), 40000 - 7 * 8, "saber charge");
        helper.succeed();
    }

    /** Composite vest: IC2_ALLOY chest 9 armor, 800 durability, enchantable 12, repaired with advanced alloy. */
    @GameTest(template = EMPTY)
    public static void compositeVest(GameTestHelper helper) {
        ItemStack vest = Ic2Items.COMPOSITE_VEST.toStack();
        helper.assertValueEqual(Ic2Items.COMPOSITE_VEST.get().getDefense(), 9, "armor");
        helper.assertValueEqual(vest.getMaxDamage(), 800, "durability");
        helper.assertValueEqual(vest.getItem().getEnchantmentValue(), 12, "enchantability");
        helper.assertTrue(vest.getItem().isValidRepairItem(vest, Ic2Items.ADVANCED_ALLOY.toStack()), "repair alloy");
        ItemStack alloy = Ic2Items.ADVANCED_ALLOY.toStack();
        ItemStack leather = new ItemStack(Items.LEATHER_CHESTPLATE);
        ItemStack iron = new ItemStack(Items.IRON_CHESTPLATE);
        List<ItemStack> grid = List.of(alloy, ItemStack.EMPTY, alloy, alloy, leather, alloy, alloy, iron, alloy);
        helper.assertTrue(craft(helper, grid).map(out -> out.is(Ic2Items.COMPOSITE_VEST.get())).orElse(false),
                "recipe leather/iron");
        List<ItemStack> swapped = List.of(alloy, ItemStack.EMPTY, alloy, alloy, iron, alloy, alloy, leather, alloy);
        helper.assertTrue(craft(helper, swapped).isPresent(), "recipe iron/leather");
        ItemStack worn = iron.copy();
        worn.setDamageValue(1);
        helper.assertTrue(craft(helper, List.of(alloy, ItemStack.EMPTY, alloy, alloy, leather, alloy, alloy, worn,
                alloy)).isEmpty(), "damaged chestplate rejected");
        helper.succeed();
    }

    private static Optional<ItemStack> craft(GameTestHelper helper, List<ItemStack> items) {
        CraftingInput input = CraftingInput.of(3, 3, items);
        return helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel())
                .map(holder -> holder.value().assemble(input, helper.getLevel().registryAccess()));
    }

    /** Spec 37 §3.1: sprinting with quantum leggings adds no speed but still costs 1 000 EU per 10 ticks. */
    @GameTest(template = EMPTY)
    public static void quantumLeggingsGiveNoSpeed(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemSlot(EquipmentSlot.LEGS, charged(Ic2Items.QUANTUM_LEGGINGS.toStack()));
        player.setOnGround(true);
        player.setSprinting(true);
        for (int i = 0; i < 10; i++) {
            player.setDeltaMovement(0.2, 0.0, 0.0);
            tickArmor(player, EquipmentSlot.LEGS);
            helper.assertValueEqual(player.getDeltaMovement(), new Vec3(0.2, 0.0, 0.0), "no sprint boost");
        }
        helper.assertValueEqual(ElectricItems.getCharge(player.getItemBySlot(EquipmentSlot.LEGS)), 1000000 - 1000,
                "sprint cost");
        helper.succeed();
    }

    /** Spec 37 §3.1: charged quantum leggings ignore knockback. */
    @GameTest(template = EMPTY)
    public static void quantumLeggingsIgnoreKnockback(GameTestHelper helper) {
        Player wearer = helper.makeMockPlayer(GameType.SURVIVAL);
        wearer.setItemSlot(EquipmentSlot.LEGS, charged(Ic2Items.QUANTUM_LEGGINGS.toStack()));
        wearer.knockback(1.0, 1.0, 0.0);
        helper.assertValueEqual(wearer.getDeltaMovement(), Vec3.ZERO, "no knockback");
        Player other = helper.makeMockPlayer(GameType.SURVIVAL);
        other.setItemSlot(EquipmentSlot.LEGS, Ic2Items.QUANTUM_LEGGINGS.toStack());
        other.knockback(1.0, 1.0, 0.0);
        helper.assertTrue(other.getDeltaMovement().x < 0.0, "empty leggings: knockback");
        helper.succeed();
    }

    /** Spec 37 §3.1: sprinting and sprint jumps cost no hunger with charged quantum leggings. */
    @GameTest(template = EMPTY)
    public static void quantumSprintCostsNoHunger(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.getAbilities().invulnerable = false;
        player.setItemSlot(EquipmentSlot.LEGS, charged(Ic2Items.QUANTUM_LEGGINGS.toStack()));
        player.setOnGround(true);
        player.setSprinting(true);
        float before = player.getFoodData().getExhaustionLevel();
        player.checkMovementStatistics(1.0, 0.0, 0.0);
        player.jumpFromGround();
        helper.assertValueEqual(player.getFoodData().getExhaustionLevel(), before, "no exhaustion");
        player.setItemSlot(EquipmentSlot.LEGS, ItemStack.EMPTY);
        player.setOnGround(true);
        player.checkMovementStatistics(1.0, 0.0, 0.0);
        helper.assertTrue(player.getFoodData().getExhaustionLevel() > before, "exhaustion without leggings");
        player.discard();
        helper.succeed();
    }

    /** Spec 37 §3.2: the boost jump of the quantum boots keeps its height but no longer multiplies x/z by 3.5. */
    @GameTest(template = EMPTY)
    public static void quantumBootsNoLongJump(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemSlot(EquipmentSlot.FEET, charged(Ic2Items.QUANTUM_BOOTS.toStack()));
        Ic2Keyboard.processKeyUpdate(player, Ic2Keyboard.JUMP | Ic2Keyboard.BOOST);
        player.setDeltaMovement(0.2, 0.0, 0.0);
        tickArmor(player, EquipmentSlot.FEET);
        Vec3 motion = player.getDeltaMovement();
        helper.assertTrue(Math.abs(motion.x - 0.2) < 1.0E-6 && motion.y > 0.29, "high jump without long jump");
        helper.succeed();
    }

    /** Spec 37 §1: a jetpack never pushes the player horizontally faster than sprinting. */
    @GameTest(template = EMPTY)
    public static void jetpackCappedToSprintSpeed(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack electric = charged(Ic2Items.ELECTRIC_JETPACK.toStack());
        player.setItemSlot(EquipmentSlot.CHEST, electric);
        Ic2Keyboard.processKeyUpdate(player, Ic2Keyboard.FORWARD | Ic2Keyboard.JUMP);
        player.setDeltaMovement(0.5, 0.0, 0.0);
        Ic2Items.ELECTRIC_JETPACK.get().useJetpack(player, electric, false);
        Vec3 motion = player.getDeltaMovement();
        helper.assertTrue(motion.horizontalDistance() <= MovementCap.MAX_HORIZONTAL + 1.0E-6, "capped");
        helper.assertTrue(motion.y > 0.0, "still lifts");
        helper.succeed();
    }
}
