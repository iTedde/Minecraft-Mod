package dev.craft.gravisuite;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.cable.CableType;
import dev.craft.ic2.item.ElectricItem;
import dev.craft.ic2.item.ElectricItems;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Phase 27.1: GraviSuite parts, lappacks and their recipes (spec 28 §1, §3, §10). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class GraviSuiteGameTests {
    private GraviSuiteGameTests() {
    }

    @GameTest(template = "empty_8x4x8")
    public static void lappackValues(GameTestHelper helper) {
        ElectricItem advanced = GsItems.ADVANCED_LAPPACK.get();
        ElectricItem ultimate = GsItems.ULTIMATE_LAPPACK.get();
        helper.assertTrue(advanced.getMaxCharge() == 1000000 && advanced.getTransferLimit() == 1000
                && advanced.getTier() == 2 && advanced.canProvideEnergy(), "advanced lappack values");
        helper.assertTrue(ultimate.getMaxCharge() == 10000000 && ultimate.getTransferLimit() == 20000
                && ultimate.getTier() == 2 && ultimate.canProvideEnergy(), "ultimate lappack values");
        helper.assertValueEqual(new ItemStack(GsItems.SUPERCONDUCTOR.get()).getMaxStackSize(), 64, "parts stack");
        helper.succeed();
    }

    /** LapPack (300 000) + lapotron crystal (1 000 000) → advanced lappack, charge capped at 1 000 000. */
    @GameTest(template = "empty_8x4x8")
    public static void advancedLappackRecipeTransfersCharge(GameTestHelper helper) {
        ItemStack result = craft(helper.getLevel(), 1, List.of(charged(Ic2Items.LAPPACK.toStack()),
                Ic2Items.ADVANCED_CIRCUIT.toStack(), charged(Ic2Items.LAPOTRON_CRYSTAL.toStack())));
        helper.assertTrue(result.is(GsItems.ADVANCED_LAPPACK.get()), "advanced lappack crafted");
        helper.assertValueEqual(ElectricItems.getCharge(result), 1000000, "charge capped");
        helper.succeed();
    }

    /** Six charged lapotron crystals + charged LapPack → 6 300 000 EU; the advanced lappack fits as well. */
    @GameTest(template = "empty_8x4x8")
    public static void ultimateLappackRecipeTransfersCharge(GameTestHelper helper) {
        ItemStack crystal = charged(Ic2Items.LAPOTRON_CRYSTAL.toStack());
        List<ItemStack> grid = new ArrayList<>(List.of(crystal.copy(), Ic2Items.IRIDIUM_PLATE.toStack(),
                crystal.copy(), crystal.copy(), charged(Ic2Items.LAPPACK.toStack()), crystal.copy(), crystal.copy(),
                GsItems.SUPERCONDUCTOR.toStack(), crystal.copy()));
        ItemStack result = craft(helper.getLevel(), 3, grid);
        helper.assertTrue(result.is(GsItems.ULTIMATE_LAPPACK.get()), "ultimate lappack crafted");
        helper.assertValueEqual(ElectricItems.getCharge(result), 6300000, "summed charge");
        grid.set(4, GsItems.ADVANCED_LAPPACK.toStack());
        helper.assertTrue(craft(helper.getLevel(), 3, grid).is(GsItems.ULTIMATE_LAPPACK.get()),
                "from advanced lappack");
        helper.succeed();
    }

    /** IC2 parts only fit undamaged: a used 60k coolant cell does not make a cooling core. */
    @GameTest(template = "empty_8x4x8")
    public static void partRecipes(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack cell = Ic2Items.COOLANT_CELL_60K.toStack();
        List<ItemStack> core = new ArrayList<>(List.of(cell, Ic2Items.ADVANCED_HEAT_EXCHANGER.toStack(), cell,
                Ic2Items.HEAT_CAPACITY_REACTOR_PLATING.toStack(), Ic2Items.IRIDIUM_PLATE.toStack(),
                Ic2Items.HEAT_CAPACITY_REACTOR_PLATING.toStack(), cell, Ic2Items.ADVANCED_HEAT_EXCHANGER.toStack(),
                cell));
        helper.assertTrue(craft(level, 3, core).is(GsItems.COOLING_CORE.get()), "cooling core");
        ItemStack used = cell.copy();
        used.setDamageValue(5);
        core.set(0, used);
        helper.assertTrue(craft(level, 3, core).isEmpty(), "used coolant cell rejected");
        ItemStack superconductor = craft(level, 3, List.of(GsItems.SUPERCONDUCTOR_COVER.toStack(),
                GsItems.SUPERCONDUCTOR_COVER.toStack(), GsItems.SUPERCONDUCTOR_COVER.toStack(),
                Ic2Items.cable(CableType.GLASS_FIBRE).toStack(),
                new ItemStack(Items.GOLD_INGOT),
                Ic2Items.cable(CableType.GLASS_FIBRE).toStack(),
                GsItems.SUPERCONDUCTOR_COVER.toStack(), GsItems.SUPERCONDUCTOR_COVER.toStack(),
                GsItems.SUPERCONDUCTOR_COVER.toStack()));
        helper.assertTrue(superconductor.is(GsItems.SUPERCONDUCTOR.get()) && superconductor.getCount() == 3,
                "3 superconductors");
        ItemStack tesla = Ic2Items.machine(Ic2Machine.TESLA_COIL).toStack();
        ItemStack engine = craft(level, 3, List.of(tesla, GsItems.SUPERCONDUCTOR.toStack(), tesla,
                GsItems.COOLING_CORE.toStack(), Ic2Items.machine(Ic2Machine.HV_TRANSFORMER).toStack(),
                GsItems.COOLING_CORE.toStack(), tesla, GsItems.SUPERCONDUCTOR.toStack(), tesla));
        helper.assertTrue(engine.is(GsItems.GRAVITATION_ENGINE.get()), "gravitation engine");
        helper.succeed();
    }

    /** A worn advanced/ultimate lappack recharges a held tool (IC2 chargeFromArmor, tier 2 ≥ tool tier). */
    @GameTest(template = "empty_8x4x8")
    public static void lappacksChargeTools(GameTestHelper helper) {
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        for (ItemStack pack : List.of(charged(GsItems.ADVANCED_LAPPACK.toStack()),
                charged(GsItems.ULTIMATE_LAPPACK.toStack()))) {
            player.setItemSlot(EquipmentSlot.CHEST, pack);
            ItemStack drill = Ic2Items.DIAMOND_DRILL.toStack();
            int before = ElectricItems.getCharge(pack);
            ElectricItems.chargeFromArmor(drill, player);
            int moved = ElectricItems.getCharge(drill);
            helper.assertTrue(moved > 0, "drill charged from " + pack.getItem());
            helper.assertValueEqual(ElectricItems.getCharge(player.getItemBySlot(EquipmentSlot.CHEST)),
                    before - moved, "pack discharged");
        }
        helper.succeed();
    }

    private static ItemStack charged(ItemStack stack) {
        return ElectricItems.charged(stack);
    }

    private static ItemStack craft(ServerLevel level, int width, List<ItemStack> items) {
        List<ItemStack> grid = new ArrayList<>(items);
        grid.addAll(Collections.nCopies((width - grid.size() % width) % width, ItemStack.EMPTY));
        CraftingInput input = CraftingInput.of(width, grid.size() / width, grid);
        return level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, level)
                .map(recipe -> recipe.value().assemble(input, level.registryAccess()))
                .orElse(ItemStack.EMPTY);
    }
}
