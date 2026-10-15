package dev.craft.ic2.item.armor;

import dev.craft.Craft;
import dev.craft.core.CraftDamageTypes;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2MobEffects;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.item.tool.NanoSaberItem;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
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
}
