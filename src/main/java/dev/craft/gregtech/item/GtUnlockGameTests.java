package dev.craft.gregtech.item;

import dev.craft.Craft;
import dev.craft.gregtech.GtItems;
import dev.craft.gregtech.recipe.GtRecipe;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.machine.MassFabricatorBlockEntity;
import dev.craft.ic2.recipe.MachineRecipe;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Materials unlocked by the GT ores (phase 8e.1, spec 12 §5.2). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class GtUnlockGameTests {
    private static final String EMPTY = "empty_8x4x8";

    private GtUnlockGameTests() {
    }

    /** Bauxite → small titanium dust, titanium dust → ingot at 1500 K, ingot → plate. */
    @GameTest(template = EMPTY)
    public static void titaniumChain(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GtRecipe bauxite = GtRecipe.find(level, GtRecipe.Kind.ELECTROLYZER,
                new ItemStack(GtItems.BAUXITE_DUST.get(), 12), new ItemStack(Ic2Items.EMPTY_CELL.get(), 8), false);
        helper.assertTrue(bauxite != null && bauxite.getOutput(1).is(GtItems.SMALL_TITANIUM_DUST.get())
                && bauxite.getOutput(1).getCount() == 2 && bauxite.getDuration() == 2000, "bauxite electrolysis");
        GtRecipe ingot = GtRecipe.find(level, GtRecipe.Kind.BLAST_FURNACE, new ItemStack(GtItems.TITANIUM_DUST.get()),
                ItemStack.EMPTY, false);
        helper.assertTrue(ingot != null && ingot.getOutput(0).is(GtItems.TITANIUM_INGOT.get())
                && ingot.getSpecial() == 1500 && ingot.getDuration() == 1000, "titanium blast");
        GtRecipe plate = GtRecipe.find(level, GtRecipe.Kind.BENDER, new ItemStack(GtItems.TITANIUM_INGOT.get()),
                ItemStack.EMPTY, false);
        helper.assertTrue(plate != null && plate.getOutput(0).is(GtItems.TITANIUM_PLATE.get()), "titanium plate");
        // basicmetalblastfurnacerequirement: no furnace shortcut.
        helper.assertFalse(smelts(helper, new ItemStack(GtItems.TITANIUM_DUST.get())), "titanium dust smelts");
        helper.succeed();
    }

    /** Ruby → chrome dust → ingot at 1700 K; olivine → magnesium. */
    @GameTest(template = EMPTY)
    public static void chromeAndMagnesium(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GtRecipe ruby = GtRecipe.find(level, GtRecipe.Kind.ELECTROLYZER, new ItemStack(GtItems.RUBY_DUST.get(), 9),
                new ItemStack(Ic2Items.EMPTY_CELL.get(), 3), false);
        helper.assertTrue(ruby != null && ruby.getOutput(1).is(GtItems.CHROME_DUST.get()), "ruby electrolysis");
        GtRecipe chrome = GtRecipe.find(level, GtRecipe.Kind.BLAST_FURNACE, new ItemStack(GtItems.CHROME_DUST.get()),
                ItemStack.EMPTY, false);
        helper.assertTrue(chrome != null && chrome.getOutput(0).is(GtItems.CHROME_INGOT.get())
                && chrome.getSpecial() == 1700 && chrome.getDuration() == 800, "chrome blast");
        GtRecipe olivine = GtRecipe.find(level, GtRecipe.Kind.ELECTROLYZER,
                new ItemStack(GtItems.OLIVINE_DUST.get(), 9), new ItemStack(Ic2Items.EMPTY_CELL.get(), 3), false);
        helper.assertTrue(olivine != null && olivine.getOutput(0).is(GtItems.MAGNESIUM_DUST.get())
                && olivine.getOutput(2).is(GtItems.SILICON_CELL.get()), "olivine electrolysis");
        helper.succeed();
    }

    /** Garnet centrifuge → sub-dusts → electrolysis (manganese, chrome, calcium). */
    @GameTest(template = EMPTY)
    public static void garnets(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GtRecipe red = GtRecipe.find(level, GtRecipe.Kind.CENTRIFUGE, new ItemStack(GtItems.RED_GARNET_DUST.get(), 16),
                ItemStack.EMPTY, false);
        helper.assertTrue(red != null && red.getOutput(2).is(GtItems.SPESSARTINE_DUST.get())
                && red.getOutput(2).getCount() == 8 && red.getDuration() == 3000, "red garnet centrifuge");
        GtRecipe yellow = GtRecipe.find(level, GtRecipe.Kind.CENTRIFUGE,
                new ItemStack(GtItems.YELLOW_GARNET_DUST.get(), 16), ItemStack.EMPTY, false);
        helper.assertTrue(yellow != null && yellow.getOutput(2).is(GtItems.UVAROVITE_DUST.get())
                && yellow.getDuration() == 3500, "yellow garnet centrifuge");
        GtRecipe spessartine = GtRecipe.find(level, GtRecipe.Kind.ELECTROLYZER,
                new ItemStack(GtItems.SPESSARTINE_DUST.get(), 20), new ItemStack(Ic2Items.EMPTY_CELL.get(), 9), false);
        helper.assertTrue(spessartine != null && spessartine.getOutput(1).is(GtItems.MANGANESE_DUST.get())
                && spessartine.getOutput(1).getCount() == 3, "spessartine electrolysis");
        GtRecipe uvarovite = GtRecipe.find(level, GtRecipe.Kind.ELECTROLYZER,
                new ItemStack(GtItems.UVAROVITE_DUST.get(), 20), new ItemStack(Ic2Items.EMPTY_CELL.get(), 12), false);
        helper.assertTrue(uvarovite != null && uvarovite.getOutput(0).is(GtItems.CALCIUM_CELL.get())
                && uvarovite.getOutput(1).is(GtItems.CHROME_DUST.get()) && uvarovite.getDuration() == 2200,
                "uvarovite electrolysis");
        helper.succeed();
    }

    /** Galena blast → silver + lead, silver/zinc dust smelt, ingots macerate back; amplifiers. */
    @GameTest(template = EMPTY)
    public static void silverAndZinc(GameTestHelper helper) {
        GtRecipe galena = GtRecipe.find(helper.getLevel(), GtRecipe.Kind.BLAST_FURNACE,
                new ItemStack(GtItems.GALENA_DUST.get(), 2), ItemStack.EMPTY, false);
        helper.assertTrue(galena != null && galena.getOutput(0).is(GtItems.SILVER_INGOT.get())
                && galena.getOutput(1).is(GtItems.LEAD_INGOT.get()) && galena.getSpecial() == 1500
                && galena.getDuration() == 20, "galena blast");
        assertSmelting(helper, new ItemStack(Ic2Items.SILVER_DUST.get()), GtItems.SILVER_INGOT.get());
        assertSmelting(helper, new ItemStack(GtItems.ZINC_DUST.get()), GtItems.ZINC_INGOT.get());
        ItemStack dust = MachineRecipe.getOutputFor(helper.getLevel(), MachineRecipe.Kind.MACERATOR,
                new ItemStack(GtItems.CHROME_INGOT.get()), false);
        helper.assertTrue(dust.is(GtItems.CHROME_DUST.get()), "chrome ingot macerator");
        helper.assertValueEqual(MassFabricatorBlockEntity.getAmplifierValue(new ItemStack(GtItems.MANGANESE_DUST.get())),
                5000, "manganese amplifier");
        helper.assertValueEqual(MassFabricatorBlockEntity.getAmplifierValue(new ItemStack(GtItems.TUNGSTEN_DUST.get())),
                50000, "tungsten amplifier");
        helper.succeed();
    }

    private static boolean smelts(GameTestHelper helper, ItemStack input) {
        return helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(input),
                helper.getLevel()).isPresent();
    }

    private static void assertSmelting(GameTestHelper helper, ItemStack input, Item expected) {
        var recipe = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMELTING,
                new SingleRecipeInput(input), helper.getLevel());
        helper.assertTrue(recipe.isPresent() && recipe.get().value().getResultItem(helper.getLevel().registryAccess())
                .is(expected), "smelting " + input);
    }
}
