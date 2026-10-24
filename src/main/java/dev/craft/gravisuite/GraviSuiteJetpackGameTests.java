package dev.craft.gravisuite;

import java.util.List;

import dev.craft.Craft;
import dev.craft.gravisuite.item.AdvancedJetpackItem;
import dev.craft.ic2.Ic2DataComponents;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.cable.CableType;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.item.armor.ArmorProperties;
import dev.craft.ic2.keyboard.Ic2Keyboard;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Phase 27.2: advanced jetpack and advanced nano chestplate (spec 28 §2, §4, §6). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class GraviSuiteJetpackGameTests {
    private GraviSuiteJetpackGameTests() {
    }

    private static Player wearing(GameTestHelper helper, ItemStack chest, int keys) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.setItemSlot(EquipmentSlot.CHEST, chest);
        Ic2Keyboard.processKeyUpdate(player, keys);
        player.setDeltaMovement(Vec3.ZERO);
        return player;
    }

    private static ItemStack jetpack(boolean engine, int charge) {
        ItemStack stack = GsItems.ADVANCED_JETPACK.toStack();
        ElectricItems.charge(stack, charge, Integer.MAX_VALUE, true, false);
        stack.set(GsDataComponents.FLY_ACTIVE.get(), engine);
        return stack;
    }

    private static void tick(Player player) {
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        chest.inventoryTick(player.level(), player, 38, false);
    }

    @GameTest(template = "empty_8x4x8")
    public static void engineOffDoesNothing(GameTestHelper helper) {
        Player player = wearing(helper, jetpack(false, 500000), Ic2Keyboard.JUMP);
        tick(player);
        helper.assertValueEqual(ElectricItems.getCharge(player.getItemBySlot(EquipmentSlot.CHEST)), 500000, "no use");
        helper.assertValueEqual(player.getDeltaMovement().y, 0.0, "no thrust");
        helper.succeed();
    }

    @GameTest(template = "empty_8x4x8")
    public static void thrustAndBoost(GameTestHelper helper) {
        Player player = wearing(helper, jetpack(true, 500000), Ic2Keyboard.JUMP);
        player.fallDistance = 5.0F;
        tick(player);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        helper.assertValueEqual(ElectricItems.getCharge(chest), 500000 - 12, "12 EU per tick");
        helper.assertTrue(Math.abs(player.getDeltaMovement().y - 0.2) < 1.0E-6, "motionY +0.2");
        helper.assertValueEqual(player.fallDistance, 0.0F, "fall distance reset");
        Ic2Keyboard.processKeyUpdate(player, Ic2Keyboard.JUMP | Ic2Keyboard.FORWARD | Ic2Keyboard.BOOST);
        tick(player);
        helper.assertValueEqual(ElectricItems.getCharge(chest), 500000 - 12 - 12 - 60, "boost costs 60 more");
        helper.assertTrue(player.getDeltaMovement().horizontalDistance() > 0.0, "pushed forward");
        helper.succeed();
    }

    /** Hover mode: lift capped at 0.1 while jumping; falling faster than 0.35 fires the jetpack without jump. */
    @GameTest(template = "empty_8x4x8")
    public static void hoverMode(GameTestHelper helper) {
        ItemStack stack = jetpack(true, 500000);
        stack.set(Ic2DataComponents.HOVER_MODE.get(), true);
        Player player = wearing(helper, stack, Ic2Keyboard.JUMP);
        tick(player);
        helper.assertTrue(Math.abs(player.getDeltaMovement().y - 0.1) < 1.0E-6, "capped at 0.1");
        Ic2Keyboard.processKeyUpdate(player, 0);
        player.setDeltaMovement(0.0, -0.5, 0.0);
        tick(player);
        helper.assertTrue(Math.abs(player.getDeltaMovement().y + 0.3) < 1.0E-6, "slowed fall");
        player.setDeltaMovement(0.0, -0.2, 0.0);
        int charge = ElectricItems.getCharge(player.getItemBySlot(EquipmentSlot.CHEST));
        tick(player);
        helper.assertValueEqual(ElectricItems.getCharge(player.getItemBySlot(EquipmentSlot.CHEST)), charge,
                "slow fall without jump: off");
        helper.succeed();
    }

    /** Original error: at ≤ 1 000 EU the power drops to charge / 50 000. */
    @GameTest(template = "empty_8x4x8")
    public static void lowChargePowerDrop(GameTestHelper helper) {
        Player player = wearing(helper, jetpack(true, 1000), Ic2Keyboard.JUMP);
        tick(player);
        helper.assertTrue(Math.abs(player.getDeltaMovement().y - 0.004) < 1.0E-6, "power 0.02");
        Player empty = wearing(helper, jetpack(true, 11), Ic2Keyboard.JUMP);
        tick(empty);
        helper.assertValueEqual(empty.getDeltaMovement().y, 0.0, "below 12 EU nothing");
        helper.succeed();
    }

    @GameTest(template = "empty_8x4x8")
    public static void keys(GameTestHelper helper) {
        Player player = wearing(helper, jetpack(false, 0), 0);
        ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
        GsKeyPayload.handle(player, GsKeyPayload.FLY);
        helper.assertTrue(AdvancedJetpackItem.isEngineOn(chest), "engine on");
        GsKeyPayload.handle(player, GsKeyPayload.MODE);
        helper.assertTrue(!AdvancedJetpackItem.isHoverMode(chest), "mode without jump: nothing");
        Ic2Keyboard.processKeyUpdate(player, Ic2Keyboard.JUMP);
        GsKeyPayload.handle(player, GsKeyPayload.MODE);
        helper.assertTrue(AdvancedJetpackItem.isHoverMode(chest), "hover on");
        GsKeyPayload.handle(player, GsKeyPayload.FLY);
        helper.assertTrue(!AdvancedJetpackItem.isEngineOn(chest), "engine off");
        helper.succeed();
    }

    /** Jetpack protects nothing; nano chestplate 0.36 with AbsorbMax charge / 800 (no ×25), display 7 from 800 EU. */
    @GameTest(template = "empty_8x4x8")
    public static void protection(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        var source = helper.getLevel().damageSources().cactus();
        ItemStack jet = jetpack(false, 1000000);
        ArmorProperties jetProps = GsItems.ADVANCED_JETPACK.get().getProperties(player, jet, source, 10, 2);
        helper.assertTrue(jetProps.absorbRatio == 0.0 && jetProps.absorbMax == 0, "jetpack no protection");
        helper.assertValueEqual(GsItems.ADVANCED_JETPACK.get().getArmorDisplay(jet), 0, "jetpack display");
        ItemStack nano = ElectricItems.charged(GsItems.ADVANCED_NANO_CHESTPLATE.toStack());
        ArmorProperties props = GsItems.ADVANCED_NANO_CHESTPLATE.get().getProperties(player, nano, source, 10, 2);
        helper.assertTrue(Math.abs(props.absorbRatio - 0.36) < 1.0E-9, "ratio 0.36");
        helper.assertValueEqual(props.absorbMax, 1250, "1 000 000 / 800");
        helper.assertValueEqual(GsItems.ADVANCED_NANO_CHESTPLATE.get().getArmorDisplay(nano), 7, "display 7");
        ItemStack low = GsItems.ADVANCED_NANO_CHESTPLATE.toStack();
        ElectricItems.charge(low, 799, Integer.MAX_VALUE, true, false);
        helper.assertValueEqual(GsItems.ADVANCED_NANO_CHESTPLATE.get().getArmorDisplay(low), 0, "display 0 < 800");
        helper.assertTrue(GsItems.ADVANCED_NANO_CHESTPLATE.get().canProvideEnergy(), "provides energy");
        helper.succeed();
    }

    /** Advanced jetpack from a charged advanced lappack keeps the charge; nano chestplate from the jetpack. */
    @GameTest(template = "empty_8x4x8")
    public static void recipes(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack carbon = Ic2Items.CARBON_PLATE.toStack();
        ItemStack fibre = Ic2Items.cable(CableType.GLASS_FIBRE).toStack();
        ItemStack circuit = Ic2Items.ADVANCED_CIRCUIT.toStack();
        ItemStack booster = GsItems.ENGINE_BOOSTER.toStack();
        ItemStack jet = craft(level, List.of(carbon, Ic2Items.ELECTRIC_JETPACK.toStack(), carbon, booster,
                ElectricItems.charged(GsItems.ADVANCED_LAPPACK.toStack()), booster, fibre, circuit, fibre));
        helper.assertTrue(jet.is(GsItems.ADVANCED_JETPACK.get()), "advanced jetpack");
        helper.assertValueEqual(ElectricItems.getCharge(jet), 1000000, "charge from lappack");
        ItemStack nano = craft(level, List.of(carbon, jet, carbon, carbon, Ic2Items.NANO_CHESTPLATE.toStack(),
                carbon, fibre, circuit, fibre));
        helper.assertTrue(nano.is(GsItems.ADVANCED_NANO_CHESTPLATE.get()), "advanced nano chestplate");
        helper.assertValueEqual(ElectricItems.getCharge(nano), 1000000, "charge from jetpack");
        helper.succeed();
    }

    private static ItemStack craft(ServerLevel level, List<ItemStack> grid) {
        CraftingInput input = CraftingInput.of(3, 3, grid);
        return level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level)
                .map(recipe -> recipe.value().assemble(input, level.registryAccess()))
                .orElse(ItemStack.EMPTY);
    }
}
