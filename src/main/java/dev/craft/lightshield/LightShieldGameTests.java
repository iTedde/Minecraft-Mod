package dev.craft.lightshield;

import java.util.ArrayList;
import java.util.List;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.keyboard.Ic2Keyboard;
import it.unimi.dsi.fastutil.longs.Long2FloatMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Electric light shield (spec 30 §7). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class LightShieldGameTests {
    private static final String EMPTY = "empty_8x4x8";
    /** Eye block of a player standing at (2.5, 1, 2.5). */
    private static final BlockPos EYE = new BlockPos(2, 2, 2);

    private LightShieldGameTests() {
    }

    private static ItemStack shield(int charge) {
        ItemStack stack = new ItemStack(LsItems.ELECTRIC_LIGHT_SHIELD.get());
        ElectricItems.charge(stack, charge, Integer.MAX_VALUE, true, false);
        return stack;
    }

    private static Player holder(GameTestHelper helper, ItemStack shield) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.moveTo(helper.absoluteVec(new Vec3(2.5, 1.0, 2.5)));
        player.setItemInHand(InteractionHand.OFF_HAND, shield);
        return player;
    }

    /** Eye of a holder looking along +X at (1.5, 2.5, 3.5) of the test structure. */
    private static final Vec3 CONE_EYE = new Vec3(1.5, 2.5, 3.5);

    private static float coneAt(GameTestHelper helper, Long2FloatMap lights, int x, int y, int z) {
        return lights.get(helper.absolutePos(new BlockPos(x, y, z)).asLong());
    }

    private static Long2FloatMap cone(GameTestHelper helper) {
        return LightCone.compute(helper.getLevel(), helper.absoluteVec(CONE_EYE), new Vec3(1.0, 0.0, 0.0));
    }

    @GameTest(template = EMPTY)
    public static void coneValues(GameTestHelper helper) {
        helper.assertTrue(LightCone.coneLight(2.0, 0.0) == 14.0F, "full up to 4 blocks");
        helper.assertTrue(LightCone.coneLight(10.0, 0.0) == 7.0F, "half way between 4 and 16");
        helper.assertTrue(LightCone.coneLight(16.0, 0.0) == 0.0F, "ends at 16");
        helper.assertTrue(LightCone.coneLight(2.0, 10.0) == 14.0F, "full inside 10°");
        helper.assertTrue(LightCone.coneLight(2.0, 12.5) == 7.0F, "fades in the outer 5°");
        helper.assertTrue(LightCone.coneLight(2.0, 15.0) == 0.0F, "30° opening");
        helper.assertTrue(LightCone.glowLight(0.0) == 7.0F, "glow 7 at the eye");
        helper.assertTrue(LightCone.glowLight(3.0) == 4.0F, "glow -1 per block");
        helper.assertTrue(LightCone.glowLight(7.0) == 0.0F, "glow radius 7");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void coneLightsAhead(GameTestHelper helper) {
        Long2FloatMap lights = cone(helper);
        helper.assertTrue(coneAt(helper, lights, 4, 2, 3) == 14.0F, "cone ahead");
        float behind = coneAt(helper, lights, 0, 2, 3);
        helper.assertTrue(behind > 0.0F && behind < 7.0F, "only the glow behind, got " + behind);
        float side = coneAt(helper, lights, 1, 2, 7);
        helper.assertTrue(side > 0.0F && side < 4.0F, "only the glow to the side, got " + side);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void wallsBlockTheCone(GameTestHelper helper) {
        for (int y = 1; y <= 3; y++) {
            for (int z = 0; z < 8; z++) {
                helper.setBlock(new BlockPos(4, y, z), Blocks.STONE);
            }
        }
        Long2FloatMap lights = cone(helper);
        helper.assertTrue(coneAt(helper, lights, 4, 2, 3) == 14.0F, "the wall itself is lit");
        float behind = coneAt(helper, lights, 6, 2, 3);
        helper.assertTrue(behind < 3.0F, "dark behind the wall, got " + behind);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void coneShinesThroughWater(GameTestHelper helper) {
        for (int x = 2; x < 8; x++) {
            helper.setBlock(new BlockPos(x, 2, 3), Blocks.WATER);
        }
        helper.assertTrue(coneAt(helper, cone(helper), 6, 2, 3) > 10.0F, "water lets the light through");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void litOnlyInHandWithCharge(GameTestHelper helper) {
        Player player = holder(helper, shield(1000));
        helper.assertFalse(ShieldLights.litShield(player).isEmpty(), "off hand");
        ItemStack stack = player.getOffhandItem();
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        helper.assertTrue(ShieldLights.litShield(player).isEmpty(), "put away");
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        helper.assertFalse(ShieldLights.litShield(player).isEmpty(), "main hand works too");
        Player spectator = helper.makeMockPlayer(GameType.SPECTATOR);
        spectator.setItemInHand(InteractionHand.OFF_HAND, shield(1000));
        helper.assertTrue(ShieldLights.litShield(spectator).isEmpty(), "not for spectators");
        helper.succeed();
    }

    /** A light block left over from Craft 1.0.8 removes itself on its next check. */
    @GameTest(template = EMPTY)
    public static void orphanedLightRemovesItself(GameTestHelper helper) {
        helper.setBlock(EYE, LsBlocks.SHIELD_LIGHT.get());
        helper.runAfterDelay(ShieldLightBlock.CHECK_INTERVAL + 2, () -> {
            helper.assertBlockPresent(Blocks.AIR, EYE);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void lightCostsOneEuEveryFourTicks(GameTestHelper helper) {
        Player player = holder(helper, shield(1000));
        helper.onEachTick(() -> ShieldLights.tick(player));
        helper.runAfterDelay(40, () -> {
            int used = 1000 - ElectricItems.getCharge(player.getOffhandItem());
            helper.assertTrue(used >= 9 && used <= 11, "10 EU in 40 ticks, got " + used);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void emptyShieldNeitherLightsNorBlocks(GameTestHelper helper) {
        Player player = holder(helper, shield(0));
        helper.assertTrue(ShieldLights.litShield(player).isEmpty(), "empty shield gives no light");
        ItemStack stack = player.getOffhandItem();
        helper.assertFalse(stack.canPerformAction(ItemAbilities.SHIELD_BLOCK), "empty shield cannot block");
        helper.assertTrue(stack.use(helper.getLevel(), player, InteractionHand.OFF_HAND).getResult()
                == InteractionResult.FAIL, "cannot be raised");
        helper.assertFalse(player.isUsingItem(), "not raised");
        ElectricItems.charge(stack, 1, Integer.MAX_VALUE, true, false);
        helper.assertTrue(stack.canPerformAction(ItemAbilities.SHIELD_BLOCK), "charged shield blocks");
        stack.use(helper.getLevel(), player, InteractionHand.OFF_HAND);
        helper.assertTrue(player.isUsingItem(), "raised");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void blockedHitsCostEu(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack stack = shield(1000);
        helper.assertTrue(ElectricLightShieldItem.chargeForBlock(stack, 2.5F, level, player) == 0,
                "below 3 damage free");
        helper.assertTrue(ElectricLightShieldItem.chargeForBlock(stack, 5.0F, level, player) == 600,
                "1 + 5 points × 100 EU");
        helper.assertTrue(ElectricLightShieldItem.chargeForBlock(stack, 5.0F, level, player) == 400,
                "the rest when the charge runs short");
        helper.assertTrue(ElectricItems.getCharge(stack) == 0, "empty");
        Player creative = helper.makeMockPlayer(GameType.CREATIVE);
        creative.getAbilities().instabuild = true;
        ItemStack free = shield(1000);
        ElectricLightShieldItem.chargeForBlock(free, 10.0F, level, creative);
        helper.assertTrue(ElectricItems.getCharge(free) == 1000, "creative pays nothing");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void unbreakingSavesEu(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack stack = shield(10000);
        stack.enchant(enchantment(level, Enchantments.UNBREAKING), 3);
        int used = 0;
        for (int i = 0; i < 100; i++) {
            used += ElectricLightShieldItem.chargeForBlock(stack, 3.0F, level, player);
            ElectricItems.charge(stack, Integer.MAX_VALUE, Integer.MAX_VALUE, true, false);
        }
        // Without Unbreaking 100 × 400 EU; with III about a quarter.
        helper.assertTrue(used > 0 && used < 20000, "Unbreaking III used " + used + " EU");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void mendingChargesShield(GameTestHelper helper) {
        ItemStack stack = shield(0);
        stack.enchant(enchantment(helper.getLevel(), Enchantments.MENDING), 1);
        Player player = holder(helper, stack);
        helper.assertTrue(ElectricLightShieldItem.repairWithXp(player, 10) == 0, "all XP used");
        helper.assertTrue(ElectricItems.getCharge(stack) == 2000, "10 XP = 2000 EU");
        ElectricItems.charge(stack, 7900, Integer.MAX_VALUE, true, false);
        helper.assertTrue(ElectricLightShieldItem.repairWithXp(player, 10) == 9, "100 EU missing takes 1 XP");
        helper.assertTrue(ElectricItems.getCharge(stack) == 10000, "full");
        ItemStack plain = shield(0);
        player.setItemInHand(InteractionHand.OFF_HAND, plain);
        helper.assertTrue(ElectricLightShieldItem.repairWithXp(player, 10) == 10, "no Mending, no charge");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void modeKeyTogglesLight(GameTestHelper helper) {
        Player player = holder(helper, shield(1000));
        ItemStack stack = player.getOffhandItem();
        Ic2Keyboard.processKeyUpdate(player, Ic2Keyboard.MODE_SWITCH);
        stack.use(helper.getLevel(), player, InteractionHand.OFF_HAND);
        helper.assertFalse(ElectricLightShieldItem.isLightOn(stack), "switched off");
        helper.assertFalse(player.isUsingItem(), "mode key does not raise the shield");
        helper.assertTrue(ShieldLights.litShield(player).isEmpty(), "no light while off");
        int charge = ElectricItems.getCharge(stack);
        for (int i = 0; i < 8; i++) {
            ShieldLights.tick(player);
        }
        helper.assertTrue(ElectricItems.getCharge(stack) == charge, "no cost while off");
        stack.use(helper.getLevel(), player, InteractionHand.OFF_HAND);
        Ic2Keyboard.processKeyUpdate(player, 0);
        helper.assertFalse(ShieldLights.litShield(player).isEmpty(), "switched on again");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void recipeGivesEmptyShield(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack alloy = new ItemStack(Ic2Items.ADVANCED_ALLOY.get());
        ItemStack battery = ElectricItems.charged(new ItemStack(Ic2Items.RE_BATTERY.get()));
        List<ItemStack> grid = new ArrayList<>(List.of(alloy, new ItemStack(Ic2Items.LUMINATOR.get()), alloy,
                alloy, new ItemStack(Items.SHIELD), alloy, ItemStack.EMPTY, battery, ItemStack.EMPTY));
        CraftingInput input = CraftingInput.of(3, 3, grid);
        ItemStack result = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level)
                .map(recipe -> recipe.value().assemble(input, level.registryAccess())).orElse(ItemStack.EMPTY);
        helper.assertTrue(result.is(LsItems.ELECTRIC_LIGHT_SHIELD.get()), "light shield recipe");
        helper.assertTrue(ElectricItems.getCharge(result) == 0, "comes out empty");
        helper.succeed();
    }

    private static Holder<Enchantment> enchantment(ServerLevel level, ResourceKey<Enchantment> key) {
        return level.registryAccess().registryOrThrow(Registries.ENCHANTMENT).getHolderOrThrow(key);
    }
}
