package dev.craft.gregtech.item;

import dev.craft.Craft;
import dev.craft.gregtech.GtItems;
import dev.craft.gregtech.recipe.GtRecipe;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.cable.CableType;
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
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Recipes of existing items unlocked by the GT ore materials (phase 8e.2, spec 12 §5.3). */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class GtUnlockedRecipeGameTests {
    private static final String EMPTY = "empty_8x4x8";

    private GtUnlockedRecipeGameTests() {
    }

    /** Diamond drill with titanium plates, mining laser with ruby, helium cell and energy crystal. */
    @GameTest(template = EMPTY)
    public static void drillAndLaser(GameTestHelper helper) {
        ItemStack e = ItemStack.EMPTY;
        ItemStack diamond = new ItemStack(Items.DIAMOND);
        ItemStack titanium = new ItemStack(GtItems.TITANIUM_PLATE.get());
        ItemStack circuit = new ItemStack(Ic2Items.ADVANCED_CIRCUIT.get());
        assertCraft(helper, List.of(e, diamond, e, diamond, new ItemStack(Ic2Items.MINING_DRILL.get()), diamond,
                titanium, circuit, titanium), Ic2Items.DIAMOND_DRILL.get(), 1, "diamond drill");
        ItemStack alloy = new ItemStack(Ic2Items.ADVANCED_ALLOY.get());
        assertCraft(helper, List.of(new ItemStack(GtItems.RUBY.get()), new ItemStack(GtItems.HELIUM_COOLANT_CELL_60K.get()),
                new ItemStack(Ic2Items.ENERGY_CRYSTAL.get()), titanium, titanium, circuit, e, alloy, alloy),
                Ic2Items.MINING_LASER.get(), 1, "mining laser");
        helper.succeed();
    }

    /** Kanthal (3) and nichrome (1) heating coils at the crafting table. */
    @GameTest(template = EMPTY)
    public static void heatingCoils(GameTestHelper helper) {
        ItemStack e = ItemStack.EMPTY;
        ItemStack iron = new ItemStack(Ic2Items.REFINED_IRON_INGOT.get());
        ItemStack chrome = new ItemStack(GtItems.CHROME_INGOT.get());
        ItemStack aluminium = new ItemStack(GtItems.ALUMINIUM_INGOT.get());
        assertCraft(helper, List.of(iron, iron, iron, chrome, aluminium, aluminium, chrome, chrome, aluminium),
                GtItems.KANTHAL_HEATING_COIL.get(), 3, "kanthal coil");
        ItemStack nickel = new ItemStack(GtItems.NICKEL_INGOT.get());
        assertCraft(helper, List.of(e, nickel, e, nickel, chrome, nickel, e, nickel, e),
                GtItems.NICHROME_HEATING_COIL.get(), 1, "nichrome coil");
        helper.succeed();
    }

    /** Mixed metal with titanium/zinc, titanium piston, ruby energy crystal, sapphire lapotron, silver glass fibre. */
    @GameTest(template = EMPTY)
    public static void materialVariants(GameTestHelper helper) {
        ItemStack bronze = new ItemStack(GtItems.BRONZE_PLATE.get());
        assertCraft(helper, 1, List.of(new ItemStack(GtItems.TITANIUM_PLATE.get()), bronze,
                new ItemStack(GtItems.ALUMINIUM_PLATE.get())), Ic2Items.MIXED_METAL_INGOT.get(), 4, "titanium mixed metal");
        assertCraft(helper, 1, List.of(new ItemStack(GtItems.STEEL_PLATE.get()), bronze,
                new ItemStack(GtItems.ZINC_PLATE.get())), Ic2Items.MIXED_METAL_INGOT.get(), 2, "zinc mixed metal");
        ItemStack plank = new ItemStack(Items.OAK_PLANKS);
        ItemStack cobble = new ItemStack(Items.COBBLESTONE);
        assertCraft(helper, List.of(plank, plank, plank, cobble, new ItemStack(GtItems.TITANIUM_INGOT.get()), cobble,
                cobble, new ItemStack(Items.REDSTONE), cobble), Items.PISTON, 1, "titanium piston");
        ItemStack redstone = new ItemStack(Items.REDSTONE);
        assertCraft(helper, List.of(redstone, redstone, redstone, redstone, new ItemStack(GtItems.RUBY.get()), redstone,
                redstone, redstone, redstone), Ic2Items.ENERGY_CRYSTAL.get(), 1, "ruby energy crystal");
        ItemStack lazurite = new ItemStack(GtItems.LAZURITE_DUST.get());
        ItemStack circuit = new ItemStack(Ic2Items.ELECTRONIC_CIRCUIT.get());
        assertCraft(helper, List.of(lazurite, circuit, lazurite, lazurite, new ItemStack(GtItems.SAPPHIRE.get()),
                lazurite, lazurite, circuit, lazurite), Ic2Items.LAPOTRON_CRYSTAL.get(), 1, "sapphire lapotron");
        ItemStack glass = new ItemStack(Items.GLASS);
        ItemStack silver = new ItemStack(GtItems.SILVER_INGOT.get());
        assertCraft(helper, List.of(glass, glass, glass, silver, new ItemStack(Items.DIAMOND), silver, glass, glass,
                glass), Ic2Items.cable(CableType.GLASS_FIBRE).get(), 6, "silver glass fibre");
        helper.succeed();
    }

    /** Generator + 2 magnalium plates → wind mill in the assembler. */
    @GameTest(template = EMPTY)
    public static void magnaliumWindMill(GameTestHelper helper) {
        GtRecipe recipe = GtRecipe.find(helper.getLevel(), GtRecipe.Kind.ASSEMBLER,
                new ItemStack(Ic2Items.machine(Ic2Machine.GENERATOR).get()),
                new ItemStack(GtItems.MAGNALIUM_PLATE.get(), 2), true);
        helper.assertTrue(recipe != null && recipe.getOutput(0).is(Ic2Items.machine(Ic2Machine.WIND_MILL).get())
                && recipe.getDuration() == 6400 && recipe.getEuPerTick() == 8, "magnalium wind mill");
        helper.succeed();
    }

    private static void assertCraft(GameTestHelper helper, List<ItemStack> grid, Item expected, int count, String what) {
        assertCraft(helper, 3, grid, expected, count, what);
    }

    private static void assertCraft(GameTestHelper helper, int width, List<ItemStack> grid, Item expected, int count,
                                    String what) {
        CraftingInput input = CraftingInput.of(width, grid.size() / width, grid);
        Optional<RecipeHolder<CraftingRecipe>> recipe = helper.getLevel().getRecipeManager()
                .getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
        Optional<ItemStack> result = recipe.map(holder -> holder.value().assemble(input, helper.getLevel().registryAccess()));
        helper.assertTrue(result.isPresent() && result.get().is(expected) && result.get().getCount() == count, what);
    }
}
