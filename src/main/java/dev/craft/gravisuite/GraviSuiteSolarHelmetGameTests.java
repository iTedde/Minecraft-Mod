package dev.craft.gravisuite;

import dev.craft.Craft;
import dev.craft.gravisuite.item.UltimateSolarHelmetItem;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.item.ElectricItems;
import dev.craft.ic2.item.armor.ArmorProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Phase 27.6: ultimate solar helmet (spec 28 §2, §8). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class GraviSuiteSolarHelmetGameTests {
    private static final String EMPTY = "empty_8x4x8";

    private GraviSuiteSolarHelmetGameTests() {
    }

    /** 512 EU/t in the sun, 64 under open sky, 0 without sky; a block above the head hides the sky. */
    @GameTest(template = EMPTY)
    public static void generation(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        UltimateSolarHelmetItem.setVisibility(player, true, true);
        helper.assertValueEqual(UltimateSolarHelmetItem.generation(helper.getLevel(), player), 512, "sun");
        UltimateSolarHelmetItem.setVisibility(player, false, true);
        helper.assertValueEqual(UltimateSolarHelmetItem.generation(helper.getLevel(), player), 64, "night");
        UltimateSolarHelmetItem.setVisibility(player, true, false);
        helper.assertValueEqual(UltimateSolarHelmetItem.generation(helper.getLevel(), player), 0, "no sky");

        Player covered = helper.makeMockPlayer(GameType.SURVIVAL);
        BlockPos feet = new BlockPos(3, 1, 3);
        // 3 × 3: the pack casts negative coordinates towards zero, one block beside the player.
        for (BlockPos roof : BlockPos.betweenClosed(feet.offset(-1, 2, -1), feet.offset(1, 2, 1))) {
            helper.setBlock(roof, Blocks.STONE);
        }
        covered.setPos(Vec3.atBottomCenterOf(helper.absolutePos(feet)));
        // canSeeSky follows the sky light, which is updated on the next ticks.
        helper.runAfterDelay(10, () -> {
            helper.assertValueEqual(UltimateSolarHelmetItem.generation(helper.getLevel(), covered), 0, "covered");
            helper.succeed();
        });
    }

    /** Armor first (feet → head, so the helmet itself), then the inventory with its transfer limit; rest is lost. */
    @GameTest(template = EMPTY)
    public static void distribution(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack helmet = GsItems.ULTIMATE_SOLAR_HELMET.toStack();
        player.setItemSlot(EquipmentSlot.HEAD, helmet);
        ItemStack battery = Ic2Items.RE_BATTERY.toStack();
        player.getInventory().items.set(0, battery);
        UltimateSolarHelmetItem.setVisibility(player, true, true);
        helmet.inventoryTick(helper.getLevel(), player, 39, false);
        helper.assertValueEqual(ElectricItems.getCharge(helmet), 512, "helmet charges itself first");
        helper.assertValueEqual(ElectricItems.getCharge(battery), 0, "nothing left");
        ElectricItems.charge(helmet, 1000000 - 512 - 100, Integer.MAX_VALUE, true, false);
        helmet.inventoryTick(helper.getLevel(), player, 39, false);
        helper.assertValueEqual(ElectricItems.getCharge(helmet), 1000000, "helmet full");
        int transfer = Ic2Items.RE_BATTERY.get().getTransferLimit();
        helper.assertValueEqual(ElectricItems.getCharge(battery), Math.min(412, transfer), "battery gets the rest");
        helper.succeed();
    }

    /** 0.15 with AbsorbMax charge / 900 (no ×25), display always 3, provides no energy, no recipe. */
    @GameTest(template = EMPTY)
    public static void protectionAndRecipe(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        var item = GsItems.ULTIMATE_SOLAR_HELMET.get();
        ItemStack full = ElectricItems.charged(GsItems.ULTIMATE_SOLAR_HELMET.toStack());
        ArmorProperties props = item.getProperties(player, full, helper.getLevel().damageSources().cactus(), 10, 3);
        helper.assertTrue(Math.abs(props.absorbRatio - 0.15) < 1.0E-9, "ratio 0.15");
        helper.assertValueEqual(props.absorbMax, 1000000 / 900, "1 000 000 / 900");
        helper.assertValueEqual(item.getArmorDisplay(GsItems.ULTIMATE_SOLAR_HELMET.toStack()), 3, "display 3");
        helper.assertTrue(!item.canProvideEnergy(), "provides no energy");
        ResourceLocation id = BuiltInRegistries.ITEM.getKey(item);
        helper.assertTrue(helper.getLevel().getRecipeManager().getRecipes().stream()
                .noneMatch(recipe -> recipe.value().getResultItem(helper.getLevel().registryAccess()).is(item)),
                "no recipe for " + id);
        helper.succeed();
    }
}
