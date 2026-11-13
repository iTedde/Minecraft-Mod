package dev.craft.lightshield;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    /** Spec 51 §1: range stage × width stage EU every 2 ticks; default 2 × 2, largest 4 × 4. */
    @GameTest(template = EMPTY)
    public static void lightCostsRangeTimesWidth(GameTestHelper helper) {
        floor(helper);
        Player player = holder(helper, shield(10000));
        lookDown(player);
        Player big = holder(helper, shield(10000));
        lookDown(big);
        big.getOffhandItem().set(LsDataComponents.FLASHLIGHT.get(), Flashlight.of(4, 4));
        helper.onEachTick(() -> {
            ShieldLights.tick(player);
            ShieldLights.tick(big);
        });
        helper.runAfterDelay(40, () -> {
            int used = 10000 - ElectricItems.getCharge(player.getOffhandItem());
            int usedBig = 10000 - ElectricItems.getCharge(big.getOffhandItem());
            ShieldLights.remove(helper.getLevel().getServer(), player.getUUID());
            ShieldLights.remove(helper.getLevel().getServer(), big.getUUID());
            helper.assertTrue(used >= 76 && used <= 84, "default 2 EU/t: 80 EU in 40 ticks, got " + used);
            helper.assertTrue(usedBig >= 304 && usedBig <= 336, "largest 8 EU/t: 320 EU in 40 ticks, got " + usedBig);
            helper.succeed();
        });
    }

    /** Payload: clamped to stages 1–4, only on a held shield. */
    @GameTest(template = EMPTY)
    public static void flashlightPayloadClamps(GameTestHelper helper) {
        Player player = holder(helper, shield(1000));
        FlashlightPayload.handle(player, new Flashlight(9, 0));
        helper.assertTrue(ElectricLightShieldItem.flashlight(player.getOffhandItem()).equals(new Flashlight(4, 1)),
                "clamped to 4/1");
        Player empty = helper.makeMockPlayer(GameType.SURVIVAL);
        empty.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.SHIELD));
        FlashlightPayload.handle(empty, Flashlight.of(4, 4));
        helper.assertFalse(empty.getMainHandItem().has(LsDataComponents.FLASHLIGHT.get()), "not on other items");
        helper.assertTrue(Flashlight.of(1, 1).euPerTickText().equals("0.5") && Flashlight.of(4, 4).euPerTickText()
                .equals("8"), "EU/t text");
        helper.succeed();
    }

    /** Spec 51 §3: a wall 4 blocks ahead gets light 15 in the block in front of it, plus the glow at the eyes. */
    @GameTest(template = EMPTY)
    public static void coneLightsWallAhead(GameTestHelper helper) {
        for (int y = 1; y <= 3; y++) {
            for (int z = 0; z < 8; z++) {
                helper.setBlock(new BlockPos(7, y, z), Blocks.STONE);
            }
        }
        Vec3 eye = helper.absoluteVec(new Vec3(2.5, 2.62, 2.5));
        Map<BlockPos, Integer> lights = ShieldLights.compute(helper.getLevel(), eye, new Vec3(1, 0, 0),
                Flashlight.of(1, 1));
        Integer front = lights.get(helper.absolutePos(new BlockPos(6, 2, 2)));
        helper.assertTrue(front != null && front == 15, "light 15 before the wall, got " + front);
        helper.assertTrue(lights.get(helper.absolutePos(EYE)) == ShieldLights.GLOW_LEVEL, "glow at the eyes");
        helper.assertTrue(lights.size() <= ShieldLights.MAX_LIGHTS, "capped");
        helper.assertTrue(ShieldLights.lightAt(8, 16) == 15 && ShieldLights.lightAt(16, 16) == 11
                && ShieldLights.lightAt(12, 16) == 13, "falloff 15 → 11");
        helper.succeed();
    }

    /** Placed by the server, follow the holder and vanish when switched off. */
    @GameTest(template = EMPTY)
    public static void lightBlocksPlacedAndRemoved(GameTestHelper helper) {
        floor(helper);
        Player player = holder(helper, shield(10000));
        lookDown(player);
        helper.onEachTick(() -> ShieldLights.tick(player));
        helper.runAfterDelay(2, () -> {
            helper.assertBlockPresent(LsBlocks.SHIELD_LIGHT.get(), EYE);
            helper.assertBlockPresent(LsBlocks.SHIELD_LIGHT.get(), new BlockPos(2, 1, 2));
            helper.assertTrue(helper.getBlockState(new BlockPos(2, 1, 2)).getValue(ShieldLightBlock.LEVEL) == 15,
                    "cone light 15 on the floor");
            helper.assertTrue(helper.getBlockState(EYE).getValue(ShieldLightBlock.LEVEL) == ShieldLights.GLOW_LEVEL,
                    "glow 14 at the eyes");
            player.getOffhandItem().set(LsDataComponents.LIGHT_ON.get(), false);
        });
        helper.runAfterDelay(4, () -> {
            helper.assertBlockNotPresent(LsBlocks.SHIELD_LIGHT.get(), EYE);
            helper.assertBlockNotPresent(LsBlocks.SHIELD_LIGHT.get(), new BlockPos(2, 1, 2));
            helper.succeed();
        });
    }

    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 8; x++) {
            for (int z = 0; z < 8; z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
            }
        }
    }

    private static void lookDown(Player player) {
        player.setXRot(90.0F);
        player.setYRot(0.0F);
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
        ElectricItems.charge(stack, 97900, Integer.MAX_VALUE, true, false);
        helper.assertTrue(ElectricLightShieldItem.repairWithXp(player, 10) == 9, "100 EU missing takes 1 XP");
        helper.assertTrue(ElectricItems.getCharge(stack) == 100000, "full");
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
