package dev.craft.gregtech.machine;

import java.util.List;
import java.util.Optional;

import dev.craft.Craft;
import dev.craft.gregtech.GtBlocks;
import dev.craft.gregtech.GtItems;
import dev.craft.gregtech.GtMachine;
import dev.craft.gregtech.block.GtCasingBlock;
import dev.craft.gregtech.recipe.GtRecipe;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.item.reactor.HeatStorageItem;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** In-world tests for the vacuum freezer and the tungsten chain (phase 8e.4, spec 12 §5.6, spec 13 §11). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class VacuumFreezerGameTests {
    private static final String EMPTY = "empty_16x8x16";
    /** The casing cube lies directly below the controller. */
    private static final BlockPos CONTROLLER = new BlockPos(5, 4, 5);

    private VacuumFreezerGameTests() {
    }

    /** Builds the 3 × 3 × 3 cube: advanced casings on the face centres, reinforced casings elsewhere, air inside. */
    private static VacuumFreezerBlockEntity build(GameTestHelper helper) {
        for (int dy = -3; dy <= -1; dy++) {
            for (int dz = -1; dz <= 1; dz++) {
                for (int dx = -1; dx <= 1; dx++) {
                    int offCentre = (dx != 0 ? 1 : 0) + (dy != -2 ? 1 : 0) + (dz != 0 ? 1 : 0);
                    if (offCentre == 0) {
                        continue;
                    }
                    helper.setBlock(CONTROLLER.offset(dx, dy, dz), GtBlocks.casing(offCentre == 1
                            ? GtCasingBlock.Type.ADVANCED : GtCasingBlock.Type.REINFORCED).get());
                }
            }
        }
        helper.setBlock(CONTROLLER, GtBlocks.machine(GtMachine.VACUUM_FREEZER).get());
        VacuumFreezerBlockEntity freezer = helper.getBlockEntity(CONTROLLER);
        freezer.skipStartupForTest();
        return freezer;
    }

    @GameTest(template = EMPTY, timeoutTicks = 30)
    public static void freezerFormsOnCasingCube(GameTestHelper helper) {
        VacuumFreezerBlockEntity freezer = build(helper);
        helper.succeedWhen(() -> {
            helper.assertTrue(freezer.isMachine(), "structure formed");
            helper.assertBlockProperty(CONTROLLER, GtMachineBlock.ACTIVE, true);
        });
    }

    /** The block below the controller is a face centre: a reinforced casing there leaves the machine incomplete. */
    @GameTest(template = EMPTY, timeoutTicks = 40)
    public static void freezerFaceCentreNeedsAdvancedCasing(GameTestHelper helper) {
        VacuumFreezerBlockEntity freezer = build(helper);
        helper.setBlock(CONTROLLER.below(), GtBlocks.casing(GtCasingBlock.Type.REINFORCED).get());
        helper.runAfterDelay(30, () -> {
            helper.assertFalse(freezer.isMachine(), "incomplete");
            helper.assertBlockProperty(CONTROLLER, GtMachineBlock.ACTIVE, false);
            helper.succeed();
        });
    }

    /** Water cell → ice cell: 50 ticks at 128 EU/t. */
    @GameTest(template = EMPTY, timeoutTicks = 100)
    public static void freezerMakesIceCell(GameTestHelper helper) {
        VacuumFreezerBlockEntity freezer = build(helper);
        freezer.setStoredEnergyForTest(10000);
        freezer.getInventory().set(VacuumFreezerBlockEntity.SLOT_INPUT, new ItemStack(Ic2Items.WATER_CELL.get()));
        helper.succeedWhen(() -> {
            helper.assertTrue(freezer.getInventory().get(VacuumFreezerBlockEntity.SLOT_OUTPUT)
                    .is(GtItems.ICE_CELL.get()), "ice cell");
            helper.assertTrue(freezer.getInventory().get(VacuumFreezerBlockEntity.SLOT_INPUT).isEmpty(), "input used");
            helper.assertValueEqual(freezer.getStoredEU(), 10000 - 50 * 128, "energy used");
        });
    }

    /** Coolant cells with any heat come out cold; durations from the pack config. */
    @GameTest(template = EMPTY)
    public static void freezerCoolsCoolantCells(GameTestHelper helper) {
        assertCooled(helper, Ic2Items.COOLANT_CELL_10K.get(), 6000, 1000);
        assertCooled(helper, Ic2Items.COOLANT_CELL_60K.get(), 0, 6000);
        assertCooled(helper, GtItems.HELIUM_COOLANT_CELL_360K.get(), 300000, 39000);
        assertCooled(helper, GtItems.NAK_COOLANT_CELL_180K.get(), 1, 15000);
        GtRecipe hot = GtRecipe.find(helper.getLevel(), GtRecipe.Kind.VACUUM_FREEZER,
                new ItemStack(GtItems.HOT_TUNGSTENSTEEL_INGOT.get()), ItemStack.EMPTY, false);
        helper.assertTrue(hot != null && hot.getOutput(0).is(GtItems.TUNGSTENSTEEL_INGOT.get())
                && hot.getDuration() == 450 && hot.getEuPerTick() == 128, "tungstensteel");
        helper.succeed();
    }

    /** Tungsten dust at 2500 K; tungsten + steel (either slot order) at 3000 K → 2 hot ingots + 4 dark ash. */
    @GameTest(template = EMPTY)
    public static void tungstenBlastRecipes(GameTestHelper helper) {
        GtRecipe ingot = GtRecipe.find(helper.getLevel(), GtRecipe.Kind.BLAST_FURNACE,
                new ItemStack(GtItems.TUNGSTEN_DUST.get()), ItemStack.EMPTY, true);
        helper.assertTrue(ingot != null && ingot.getOutput(0).is(GtItems.TUNGSTEN_INGOT.get())
                && ingot.getDuration() == 2000 && ingot.getSpecial() == 2500, "tungsten ingot");
        GtRecipe steel = GtRecipe.find(helper.getLevel(), GtRecipe.Kind.BLAST_FURNACE,
                new ItemStack(GtItems.STEEL_INGOT.get()), new ItemStack(GtItems.TUNGSTEN_INGOT.get()), true);
        helper.assertTrue(steel != null && steel.getOutput(0).is(GtItems.HOT_TUNGSTENSTEEL_INGOT.get())
                && steel.getOutput(0).getCount() == 2 && steel.getOutput(1).is(GtItems.DARK_ASH.get())
                && steel.getOutput(1).getCount() == 4 && steel.getSpecial() == 3000, "hot tungstensteel");
        helper.succeed();
    }

    /** Wolframium grinder, energy flow circuit, tungstensteel mixed metal and the grinder macerator. */
    @GameTest(template = EMPTY)
    public static void tungstenCraftingRecipes(GameTestHelper helper) {
        ItemStack e = ItemStack.EMPTY;
        ItemStack tungsten = new ItemStack(GtItems.TUNGSTEN_PLATE.get());
        ItemStack steel = new ItemStack(GtItems.STEEL_PLATE.get());
        assertCraft(helper, 3, List.of(tungsten, steel, tungsten, steel, new ItemStack(GtItems.STEEL_BLOCK.get()), steel,
                tungsten, steel, tungsten), GtItems.WOLFRAMIUM_GRINDER.get(), 2, "wolframium grinder");
        ItemStack circuit = new ItemStack(Ic2Items.ADVANCED_CIRCUIT.get());
        ItemStack lapotron = new ItemStack(Ic2Items.LAPOTRON_CRYSTAL.get());
        assertCraft(helper, 3, List.of(circuit, tungsten, circuit, lapotron, new ItemStack(Ic2Items.IRIDIUM_PLATE.get()),
                lapotron, circuit, tungsten, circuit), GtItems.ENERGY_FLOW_CIRCUIT.get(), 4, "energy flow circuit");
        assertCraft(helper, 1, List.of(new ItemStack(GtItems.TUNGSTENSTEEL_PLATE.get()),
                new ItemStack(GtItems.BRONZE_PLATE.get()), new ItemStack(GtItems.ALUMINIUM_PLATE.get())),
                Ic2Items.MIXED_METAL_INGOT.get(), 6, "tungstensteel mixed metal");
        ItemStack flint = new ItemStack(Items.FLINT);
        ItemStack electronic = new ItemStack(Ic2Items.ELECTRONIC_CIRCUIT.get());
        assertCraft(helper, 3, List.of(flint, new ItemStack(GtItems.WOLFRAMIUM_GRINDER.get()), flint, electronic,
                new ItemStack(Ic2Items.MACHINE_CASING.get()), electronic, flint, electronic, flint),
                Ic2Items.machine(Ic2Machine.MACERATOR).get(), 1, "grinder macerator");
        assertCraft(helper, 3, List.of(e, e, e, e, e, e, e, e, new ItemStack(GtItems.STEEL_BLOCK.get())), null, 0,
                "no steel block decrafting");
        helper.succeed();
    }

    private static void assertCooled(GameTestHelper helper, Item cell, int heat, int duration) {
        ItemStack hot = new ItemStack(cell);
        if (heat > 0) {
            ((HeatStorageItem) cell).setHeatForStack(hot, heat);
        }
        GtRecipe recipe = GtRecipe.find(helper.getLevel(), GtRecipe.Kind.VACUUM_FREEZER, hot, ItemStack.EMPTY, false);
        String what = cell.getDescriptionId();
        helper.assertTrue(recipe != null && recipe.getOutput(0).is(cell)
                && HeatStorageItem.getHeatOfStack(recipe.getOutput(0)) == 0, what + " cooled");
        helper.assertValueEqual(recipe.getDuration(), duration, what + " duration");
        helper.assertTrue(recipe.isInputEqual(false, false, hot, ItemStack.EMPTY), what + " any heat");
    }

    /** {@code expected == null}: no crafting recipe matches. */
    private static void assertCraft(GameTestHelper helper, int width, List<ItemStack> grid, Item expected, int count,
                                    String what) {
        CraftingInput input = CraftingInput.of(width, grid.size() / width, grid);
        Optional<RecipeHolder<CraftingRecipe>> recipe = helper.getLevel().getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
        if (expected == null) {
            helper.assertTrue(recipe.isEmpty(), what);
            return;
        }
        Optional<ItemStack> result = recipe.map(holder -> holder.value().assemble(input, helper.getLevel().registryAccess()));
        helper.assertTrue(result.isPresent() && result.get().is(expected) && result.get().getCount() == count, what);
    }
}
