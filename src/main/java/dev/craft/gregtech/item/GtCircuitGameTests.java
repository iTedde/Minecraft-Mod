package dev.craft.gregtech.item;

import dev.craft.Craft;
import dev.craft.gregtech.GtBlocks;
import dev.craft.gregtech.GtItems;
import dev.craft.gregtech.block.GtCasingBlock;
import dev.craft.gregtech.recipe.GtRecipe;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.item.ElectricItems;
import java.util.List;
import java.util.Optional;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** GT circuits, hulls, the highly advanced machine block and what they unlock (phase 8e.3, spec 12 §5.2/5.3). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class GtCircuitGameTests {
    private static final String EMPTY = "empty_8x4x8";

    private GtCircuitGameTests() {
    }

    /** Data storage from olivine/emerald, elite board from platinum, data control and energy flow circuit. */
    @GameTest(template = EMPTY)
    public static void assemblerCircuits(GameTestHelper helper) {
        ItemStack circuit = new ItemStack(Ic2Items.ADVANCED_CIRCUIT.get());
        assertAssemble(helper, new ItemStack(GtItems.OLIVINE.get(), 8), circuit, GtItems.DATA_STORAGE_CIRCUIT.get(), 4,
                6400, 8, "storage from olivine");
        assertAssemble(helper, new ItemStack(Items.EMERALD, 8), circuit, GtItems.DATA_STORAGE_CIRCUIT.get(), 4, 3200,
                4, "storage from emerald");
        assertAssemble(helper, new ItemStack(GtItems.EMERALD_DUST.get(), 8), circuit,
                GtItems.DATA_STORAGE_CIRCUIT.get(), 4, 6400, 8, "storage from emerald dust");
        assertAssemble(helper, new ItemStack(GtItems.PLATINUM_PLATE.get()), circuit, GtItems.ELITE_CIRCUIT_BOARD.get(),
                1, 3200, 4, "elite board");
        ItemStack board = new ItemStack(GtItems.ELITE_CIRCUIT_BOARD.get());
        assertAssemble(helper, board, new ItemStack(GtItems.DATA_STORAGE_CIRCUIT.get()),
                GtItems.DATA_CONTROL_CIRCUIT.get(), 1, 3200, 4, "data control");
        assertAssemble(helper, board, ElectricItems.charged(new ItemStack(Ic2Items.LAPOTRON_CRYSTAL.get())),
                GtItems.ENERGY_FLOW_CIRCUIT.get(), 1, 3200, 4, "energy flow from charged lapotron");
        assertAssemble(helper, new ItemStack(GtItems.TITANIUM_PLATE.get(), 8), new ItemStack(GtItems.MACHINE_PARTS.get()),
                GtItems.TITANIUM_MACHINE_HULL.get(), 1, 400, 8, "titanium hull");
        helper.succeed();
    }

    /** 4 data control circuits at the crafting table; highly advanced machine block → 4 advanced casings. */
    @GameTest(template = EMPTY)
    public static void craftedCircuitsAndCasings(GameTestHelper helper) {
        ItemStack a = new ItemStack(Ic2Items.ADVANCED_CIRCUIT.get());
        ItemStack d = new ItemStack(GtItems.DATA_STORAGE_CIRCUIT.get());
        assertCraft(helper, List.of(a, d, a, d, new ItemStack(Ic2Items.IRIDIUM_PLATE.get()), d, a, d, a),
                GtItems.DATA_CONTROL_CIRCUIT.get(), 4, "data control at the crafting table");
        ItemStack chrome = new ItemStack(GtItems.CHROME_PLATE.get());
        ItemStack titanium = new ItemStack(GtItems.TITANIUM_PLATE.get());
        assertCraft(helper, List.of(chrome, titanium, chrome, titanium, new ItemStack(GtItems.STEEL_MACHINE_HULL.get()),
                titanium, chrome, titanium, chrome), GtItems.HIGHLY_ADVANCED_MACHINE_BLOCK.get(), 1,
                "highly advanced machine block");
        ItemStack control = new ItemStack(GtItems.DATA_CONTROL_CIRCUIT.get());
        assertCraft(helper, List.of(chrome, chrome, chrome, control,
                new ItemStack(GtItems.HIGHLY_ADVANCED_MACHINE_BLOCK.get()), control, chrome, chrome, chrome),
                GtItems.casing(GtCasingBlock.Type.ADVANCED).get(), 4, "advanced machine casing");
        helper.succeed();
    }

    /** Hardness 10, resistance 200 and no casing (does not count for the blast furnace). */
    @GameTest(template = EMPTY)
    public static void highlyAdvancedMachineBlock(GameTestHelper helper) {
        Block block = GtBlocks.HIGHLY_ADVANCED_MACHINE_BLOCK.get();
        helper.assertTrue(block.defaultDestroyTime() == 10.0F && block.getExplosionResistance() == 200.0F,
                "hardness/resistance");
        helper.assertFalse(block instanceof GtCasingBlock, "no machine casing");
        helper.succeed();
    }

    /** Nano saber with the energy flow circuit; the lapotron crystal's charge goes into the saber. */
    @GameTest(template = EMPTY)
    public static void nanoSaber(GameTestHelper helper) {
        ItemStack e = ItemStack.EMPTY;
        ItemStack platinum = new ItemStack(GtItems.PLATINUM_PLATE.get());
        ItemStack iridium = new ItemStack(Ic2Items.IRIDIUM_PLATE.get());
        ItemStack flow = new ItemStack(GtItems.ENERGY_FLOW_CIRCUIT.get());
        ItemStack lapotron = new ItemStack(Ic2Items.LAPOTRON_CRYSTAL.get());
        ElectricItems.charge(lapotron, 30000, Integer.MAX_VALUE, true, false);
        Optional<ItemStack> saber = craft(helper, List.of(platinum, iridium, e, platinum, iridium, e, flow, lapotron,
                flow));
        helper.assertTrue(saber.isPresent() && saber.get().is(Ic2Items.NANO_SABER.get()), "nano saber");
        helper.assertTrue(ElectricItems.getCharge(saber.get()) == 30000,
                "charge " + ElectricItems.getCharge(saber.get()));
        helper.succeed();
    }

    private static void assertAssemble(GameTestHelper helper, ItemStack input1, ItemStack input2, Item expected,
                                       int count, int duration, int euPerTick, String what) {
        GtRecipe recipe = GtRecipe.find(helper.getLevel(), GtRecipe.Kind.ASSEMBLER, input1, input2, true);
        helper.assertTrue(recipe != null && recipe.getOutput(0).is(expected) && recipe.getOutput(0).getCount() == count
                && recipe.getDuration() == duration && recipe.getEuPerTick() == euPerTick, what);
    }

    private static void assertCraft(GameTestHelper helper, List<ItemStack> grid, Item expected, int count, String what) {
        Optional<ItemStack> result = craft(helper, grid);
        helper.assertTrue(result.isPresent() && result.get().is(expected) && result.get().getCount() == count, what);
    }

    private static Optional<ItemStack> craft(GameTestHelper helper, List<ItemStack> grid) {
        CraftingInput input = CraftingInput.of(3, 3, grid);
        Optional<RecipeHolder<CraftingRecipe>> recipe = helper.getLevel().getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
        return recipe.map(holder -> holder.value().assemble(input, helper.getLevel().registryAccess()));
    }
}
