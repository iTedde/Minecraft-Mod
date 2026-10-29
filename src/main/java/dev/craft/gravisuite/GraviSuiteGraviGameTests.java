package dev.craft.gravisuite;

import java.util.List;

import dev.craft.Craft;
import dev.craft.gravisuite.item.GraviChestplateItem;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
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
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Phase 27.3: gravitation chestplate (spec 28 §2, §5). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class GraviSuiteGraviGameTests {
    private GraviSuiteGraviGameTests() {
    }

    private static Player wearing(GameTestHelper helper, GameType gameType, int charge) {
        Player player = helper.makeMockPlayer(gameType);
        ItemStack chest = GsItems.GRAVI_CHESTPLATE.toStack();
        ElectricItems.charge(chest, charge, Integer.MAX_VALUE, true, false);
        player.setItemSlot(EquipmentSlot.CHEST, chest);
        return player;
    }

    private static ItemStack chest(Player player) {
        return player.getItemBySlot(EquipmentSlot.CHEST);
    }

    private static void flyKey(Player player) {
        GsKeyPayload.handle(player, GsKeyPayload.FLY);
    }

    /** Switching on needs 80 000 EU; on grants mayfly and flying, off takes both away. */
    @GameTest(template = "empty_8x4x8")
    public static void minimumCharge(GameTestHelper helper) {
        Player low = wearing(helper, GameType.SURVIVAL, GraviChestplateItem.MIN_CHARGE - 1);
        flyKey(low);
        helper.assertTrue(!GraviChestplateItem.isFlyActive(chest(low)) && !low.getAbilities().mayfly, "too little");
        Player player = wearing(helper, GameType.SURVIVAL, GraviChestplateItem.MIN_CHARGE);
        flyKey(player);
        helper.assertTrue(GraviChestplateItem.isFlyActive(chest(player)), "engine on");
        helper.assertTrue(player.getAbilities().mayfly && player.getAbilities().flying, "flying");
        flyKey(player);
        helper.assertTrue(!GraviChestplateItem.isFlyActive(chest(player)), "engine off");
        helper.assertTrue(!player.getAbilities().mayfly && !player.getAbilities().flying, "no flight");
        helper.succeed();
    }

    /** 416 EU per tick in the air, nothing on the ground (deviation); below 416 EU the engine shuts down. */
    @GameTest(template = "empty_8x4x8")
    public static void consumption(GameTestHelper helper) {
        Player player = wearing(helper, GameType.SURVIVAL, 100000);
        flyKey(player);
        player.setOnGround(true);
        player.fallDistance = 5.0F;
        GraviChestplateItem.serverTick(player);
        helper.assertValueEqual(ElectricItems.getCharge(chest(player)), 100000, "free on the ground");
        helper.assertValueEqual(player.fallDistance, 0.0F, "no fall distance");
        player.setOnGround(false);
        GraviChestplateItem.serverTick(player);
        helper.assertValueEqual(ElectricItems.getCharge(chest(player)), 100000 - 416, "416 per tick");
        ElectricItems.discharge(chest(player), Integer.MAX_VALUE, Integer.MAX_VALUE, true, false);
        ElectricItems.charge(chest(player), 415, Integer.MAX_VALUE, true, false);
        GraviChestplateItem.serverTick(player);
        helper.assertTrue(!GraviChestplateItem.isFlyActive(chest(player)), "shut down");
        helper.assertTrue(!player.getAbilities().mayfly, "flight taken");
        helper.assertValueEqual(ElectricItems.getCharge(chest(player)), 415, "nothing used");
        helper.succeed();
    }

    /** Boost in the air: 416 + 2 × 1 248 = 2 912 EU per tick (original error); ≤ 1 248 EU no boost cost. */
    @GameTest(template = "empty_8x4x8")
    public static void boostCost(GameTestHelper helper) {
        Player player = wearing(helper, GameType.SURVIVAL, 100000);
        flyKey(player);
        player.setOnGround(false);
        Ic2Keyboard.processKeyUpdate(player, Ic2Keyboard.BOOST);
        GraviChestplateItem.serverTick(player);
        helper.assertValueEqual(ElectricItems.getCharge(chest(player)), 100000 - 2912, "boost paid twice");
        ElectricItems.discharge(chest(player), Integer.MAX_VALUE, Integer.MAX_VALUE, true, false);
        ElectricItems.charge(chest(player), 1248, Integer.MAX_VALUE, true, false);
        GraviChestplateItem.serverTick(player);
        helper.assertValueEqual(ElectricItems.getCharge(chest(player)), 1248 - 416, "only base consumption");
        helper.succeed();
    }

    /** Taking the plate off ends the flight; worn again the engine is off. Creative needs no charge. */
    @GameTest(template = "empty_8x4x8")
    public static void undressAndCreative(GameTestHelper helper) {
        Player player = wearing(helper, GameType.SURVIVAL, 100000);
        flyKey(player);
        ItemStack plate = chest(player);
        player.setItemSlot(EquipmentSlot.CHEST, ItemStack.EMPTY);
        GraviChestplateItem.serverTick(player);
        helper.assertTrue(!player.getAbilities().mayfly && !player.getAbilities().flying, "flight taken");
        player.setItemSlot(EquipmentSlot.CHEST, plate);
        GraviChestplateItem.serverTick(player);
        helper.assertTrue(!GraviChestplateItem.isFlyActive(plate), "engine off after undressing");
        Player creative = wearing(helper, GameType.CREATIVE, 0);
        creative.getAbilities().instabuild = true; // the mock player does not set the abilities itself
        flyKey(creative);
        GraviChestplateItem.serverTick(creative);
        helper.assertTrue(GraviChestplateItem.isFlyActive(chest(creative)), "creative on without charge");
        helper.assertValueEqual(ElectricItems.getCharge(chest(creative)), 0, "creative uses nothing");
        helper.succeed();
    }

    /** Above build height + 6 the player is put back; world load re-checks the minimum charge. */
    @GameTest(template = "empty_8x4x8")
    public static void heightLimitAndWorldLoad(GameTestHelper helper) {
        Player player = wearing(helper, GameType.SURVIVAL, 100000);
        flyKey(player);
        int maxY = player.level().getMaxBuildHeight() + GraviChestplateItem.HEIGHT_ABOVE_BUILD_LIMIT;
        player.setPos(player.getX(), maxY + 10, player.getZ());
        GraviChestplateItem.serverTick(player);
        helper.assertValueEqual(player.getY(), (double) maxY, "height limit");
        GraviChestplateItem.firstLoad(player, chest(player));
        helper.assertTrue(GraviChestplateItem.isFlyActive(chest(player)), "on again after load");
        ElectricItems.discharge(chest(player), Integer.MAX_VALUE, Integer.MAX_VALUE, true, false);
        ElectricItems.charge(chest(player), 50000, Integer.MAX_VALUE, true, false);
        GraviChestplateItem.firstLoad(player, chest(player));
        helper.assertTrue(!GraviChestplateItem.isFlyActive(chest(player)), "off: below 80 000");
        helper.succeed();
    }

    /** 0.4 · 1.1, AbsorbMax 25 · charge / 900, display always 9; provides energy to tools. */
    @GameTest(template = "empty_8x4x8")
    public static void protectionAndCharging(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        var source = helper.getLevel().damageSources().cactus();
        GraviChestplateItem item = GsItems.GRAVI_CHESTPLATE.get();
        ItemStack plate = GsItems.GRAVI_CHESTPLATE.toStack();
        ElectricItems.charge(plate, 900000, Integer.MAX_VALUE, true, false);
        ArmorProperties props = item.getProperties(player, plate, source, 10, 2);
        helper.assertTrue(Math.abs(props.absorbRatio - 0.44) < 1.0E-9, "ratio 0.44");
        helper.assertValueEqual(props.absorbMax, 25000, "25 · 900 000 / 900");
        helper.assertValueEqual(item.getArmorDisplay(GsItems.GRAVI_CHESTPLATE.toStack()), 9, "display 9 when empty");
        player.setItemSlot(EquipmentSlot.CHEST, plate);
        ItemStack drill = Ic2Items.DIAMOND_DRILL.toStack();
        ElectricItems.chargeFromArmor(drill, player);
        helper.assertTrue(ElectricItems.getCharge(drill) > 0, "drill charged from plate");
        helper.succeed();
    }

    /** Recipe RAR/DBD/RCR: the charges of quantum chestplate and ultimate lappack add up. */
    @GameTest(template = "empty_8x4x8")
    public static void recipe(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack sc = GsItems.SUPERCONDUCTOR.toStack();
        ItemStack engine = GsItems.GRAVITATION_ENGINE.toStack();
        ItemStack lappack = GsItems.ULTIMATE_LAPPACK.toStack();
        ElectricItems.charge(lappack, 2000000, Integer.MAX_VALUE, true, false);
        List<ItemStack> grid = List.of(sc, ElectricItems.charged(Ic2Items.QUANTUM_CHESTPLATE.toStack()), sc,
                engine, Ic2Items.machine(Ic2Machine.HV_TRANSFORMER).toStack(), engine, sc, lappack, sc);
        CraftingInput input = CraftingInput.of(3, 3, grid);
        ItemStack result = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level)
                .map(recipe -> recipe.value().assemble(input, level.registryAccess())).orElse(ItemStack.EMPTY);
        helper.assertTrue(result.is(GsItems.GRAVI_CHESTPLATE.get()), "gravi chestplate");
        int quantum = ElectricItems.getCharge(ElectricItems.charged(Ic2Items.QUANTUM_CHESTPLATE.toStack()));
        helper.assertValueEqual(ElectricItems.getCharge(result), quantum + 2000000, "summed charge");
        helper.succeed();
    }
}
