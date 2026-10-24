package dev.craft.lightshield;

import java.util.ArrayList;
import java.util.List;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.keyboard.Ic2Keyboard;
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
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Electric light shield (spec 30 §7). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class LightShieldGameTests {
    private static final String EMPTY = "empty_8x4x8";
    /** Feet and eye block of a player standing at (2.5, 1, 2.5). */
    private static final BlockPos FEET = new BlockPos(2, 1, 2);
    private static final BlockPos EYE = FEET.above();

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

    private static boolean isLight(GameTestHelper helper, BlockPos pos) {
        return helper.getBlockState(pos).is(LsBlocks.SHIELD_LIGHT);
    }

    @GameTest(template = EMPTY)
    public static void lightFollowsHolder(GameTestHelper helper) {
        Player player = holder(helper, shield(1000));
        ShieldLights.tick(player);
        helper.assertTrue(isLight(helper, EYE), "light at eye level");
        helper.assertTrue(helper.getBlockState(EYE).getLightEmission(helper.getLevel(), helper.absolutePos(EYE))
                == 14, "torch light level");
        player.moveTo(helper.absoluteVec(new Vec3(4.5, 1.0, 2.5)));
        ShieldLights.tick(player);
        helper.assertBlockPresent(Blocks.AIR, EYE);
        helper.assertTrue(isLight(helper, new BlockPos(4, 2, 2)), "light moved along");
        ShieldLights.remove(player);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void lightFallsBackToFeet(GameTestHelper helper) {
        helper.setBlock(EYE, Blocks.STONE);
        Player player = holder(helper, shield(1000));
        ShieldLights.tick(player);
        helper.assertTrue(isLight(helper, FEET), "light at the feet");
        helper.setBlock(FEET, Blocks.SNOW);
        ShieldLights.tick(player);
        helper.assertTrue(ShieldLights.lightOf(player) == null, "no room, no light");
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void lightWorksUnderWater(GameTestHelper helper) {
        helper.setBlock(EYE, Blocks.WATER);
        Player player = holder(helper, shield(1000));
        ShieldLights.tick(player);
        BlockState state = helper.getBlockState(EYE);
        helper.assertTrue(state.is(LsBlocks.SHIELD_LIGHT) && state.getValue(ShieldLightBlock.WATERLOGGED),
                "waterlogged light");
        ShieldLights.remove(player);
        helper.assertBlockPresent(Blocks.WATER, EYE);
        helper.succeed();
    }

    @GameTest(template = EMPTY)
    public static void lightGoesWhenShieldIsPutAway(GameTestHelper helper) {
        Player player = holder(helper, shield(1000));
        ShieldLights.tick(player);
        ItemStack stack = player.getOffhandItem();
        player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        ShieldLights.tick(player);
        helper.assertBlockPresent(Blocks.AIR, EYE);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        ShieldLights.tick(player);
        helper.assertTrue(isLight(helper, EYE), "main hand works too");
        ShieldLights.remove(player);
        helper.assertBlockPresent(Blocks.AIR, EYE);
        helper.succeed();
    }

    /** A light nobody claims (e.g. after a crash) removes itself on its next check. */
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
            ShieldLights.remove(player);
            helper.succeed();
        });
    }

    @GameTest(template = EMPTY)
    public static void emptyShieldNeitherLightsNorBlocks(GameTestHelper helper) {
        Player player = holder(helper, shield(0));
        ShieldLights.tick(player);
        helper.assertBlockPresent(Blocks.AIR, EYE);
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
        ShieldLights.tick(player);
        helper.assertBlockPresent(Blocks.AIR, EYE);
        int charge = ElectricItems.getCharge(stack);
        for (int i = 0; i < 8; i++) {
            ShieldLights.tick(player);
        }
        helper.assertTrue(ElectricItems.getCharge(stack) == charge, "no cost while off");
        stack.use(helper.getLevel(), player, InteractionHand.OFF_HAND);
        Ic2Keyboard.processKeyUpdate(player, 0);
        ShieldLights.tick(player);
        helper.assertTrue(isLight(helper, EYE), "switched on again");
        ShieldLights.remove(player);
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
