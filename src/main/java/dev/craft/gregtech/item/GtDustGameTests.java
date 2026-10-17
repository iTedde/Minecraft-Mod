package dev.craft.gregtech.item;

import java.util.List;
import java.util.Optional;

import dev.craft.Craft;
import dev.craft.gregtech.GtItems;
import dev.craft.gregtech.recipe.GtRecipe;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.item.ScrapBoxDrops;
import dev.craft.ic2.recipe.MachineRecipe;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Reachable GT dusts of phase 13.3: flint, wood pulp, charcoal, obsidian, electrum. */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class GtDustGameTests {
    private static final String EMPTY = "empty_8x4x8";

    private GtDustGameTests() {
    }

    /** Macerator sources: flint, planks (incl. the wood plate), stick, door (1.21 amount), charcoal, obsidian. */
    @GameTest(template = EMPTY)
    public static void maceratorSources(GameTestHelper helper) {
        assertMacerates(helper, Items.FLINT, GtItems.SMALL_FLINT_DUST.get(), 2);
        assertMacerates(helper, Items.SPRUCE_PLANKS, GtItems.WOOD_PULP.get(), 1);
        assertMacerates(helper, GtItems.WOOD_PLATE.get(), GtItems.WOOD_PULP.get(), 1);
        assertMacerates(helper, Items.STICK, GtItems.SMALL_WOOD_PULP.get(), 2);
        assertMacerates(helper, Items.OAK_DOOR, GtItems.WOOD_PULP.get(), 2);
        assertMacerates(helper, Items.CHEST, GtItems.WOOD_PULP.get(), 8);
        assertMacerates(helper, Items.CHARCOAL, GtItems.CHARCOAL_DUST.get(), 1);
        assertMacerates(helper, Items.COAL, Ic2Items.COAL_DUST.get(), 1);
        assertMacerates(helper, Items.OBSIDIAN, GtItems.OBSIDIAN_DUST.get(), 1);
        assertMacerates(helper, Items.ENCHANTING_TABLE, GtItems.DIAMOND_DUST.get(), 2);
        assertMacerates(helper, GtItems.ELECTRUM_INGOT.get(), GtItems.ELECTRUM_DUST.get(), 1);
        ItemStack plate = MachineRecipe.getOutputFor(helper.getLevel(), MachineRecipe.Kind.COMPRESSOR,
                new ItemStack(GtItems.WOOD_PULP.get(), 8), false);
        helper.assertTrue(plate.is(GtItems.WOOD_PLATE.get()), "8 wood pulp → wood plate");
        helper.succeed();
    }

    /** Electrolyzer: 8 flint dust → silicon + air cell, 1 charcoal dust → carbon cell, 4 obsidian dust. */
    @GameTest(template = EMPTY)
    public static void electrolysis(GameTestHelper helper) {
        GtRecipe flint = GtRecipe.find(helper.getLevel(), GtRecipe.Kind.ELECTROLYZER,
                new ItemStack(GtItems.FLINT_DUST.get(), 8), new ItemStack(Ic2Items.EMPTY_CELL.get(), 2), false);
        helper.assertTrue(flint != null && flint.getOutput(0).is(GtItems.SILICON_CELL.get())
                && flint.getOutput(1).is(Ic2Items.AIR_CELL.get()) && flint.getDuration() == 1000, "flint dust");
        GtRecipe charcoal = GtRecipe.find(helper.getLevel(), GtRecipe.Kind.ELECTROLYZER,
                new ItemStack(GtItems.CHARCOAL_DUST.get()), new ItemStack(Ic2Items.EMPTY_CELL.get()), false);
        helper.assertTrue(charcoal != null && charcoal.getOutput(0).is(GtItems.CARBON_CELL.get())
                && charcoal.getOutput(0).getCount() == 1, "charcoal dust");
        GtRecipe obsidian = GtRecipe.find(helper.getLevel(), GtRecipe.Kind.ELECTROLYZER,
                new ItemStack(GtItems.OBSIDIAN_DUST.get(), 4), new ItemStack(Ic2Items.EMPTY_CELL.get(), 3), false);
        helper.assertTrue(obsidian != null && obsidian.getOutput(0).is(GtItems.SMALL_MAGNESIUM_DUST.get())
                && obsidian.getOutput(3).getCount() == 2, "obsidian dust");
        GtRecipe electrum = GtRecipe.find(helper.getLevel(), GtRecipe.Kind.CENTRIFUGE,
                new ItemStack(GtItems.ELECTRUM_DUST.get()), ItemStack.EMPTY, false);
        helper.assertTrue(electrum != null && electrum.getOutput(0).is(GtItems.SMALL_GOLD_DUST.get())
                && electrum.getDuration() == 975, "electrum centrifuge");
        GtRecipe saltpeter = GtRecipe.find(helper.getLevel(), GtRecipe.Kind.ELECTROLYZER,
                new ItemStack(GtItems.SALTPETER_DUST.get(), 10), new ItemStack(Ic2Items.EMPTY_CELL.get(), 7), false);
        helper.assertTrue(saltpeter != null && saltpeter.getOutput(2).isEmpty()
                && saltpeter.getOutput(3).is(Ic2Items.AIR_CELL.get()), "saltpeter dust (third slot empty)");
        GtRecipe netherrack = GtRecipe.find(helper.getLevel(), GtRecipe.Kind.CENTRIFUGE,
                new ItemStack(GtItems.NETHERRACK_DUST.get(), 16), ItemStack.EMPTY, false);
        helper.assertTrue(netherrack != null && netherrack.getOutput(3).is(Items.GOLD_NUGGET), "netherrack dust");
        ItemStack chunk = MachineRecipe.getOutputFor(helper.getLevel(), MachineRecipe.Kind.COMPRESSOR,
                new ItemStack(GtItems.LAZURITE_DUST.get(), 8), false);
        helper.assertTrue(chunk.is(GtItems.LAZURITE_CHUNK.get()), "chunk of lazurite");
        helper.succeed();
    }

    /** Crafting: electrum dust, gunpowder from charcoal dust, 5 ITNT from flint dust; smelting electrum dust. */
    @GameTest(template = EMPTY)
    public static void crafting(GameTestHelper helper) {
        ItemStack electrum = craft(helper, 2, List.of(new ItemStack(Ic2Items.SILVER_DUST.get()),
                new ItemStack(Ic2Items.GOLD_DUST.get()))).orElse(ItemStack.EMPTY);
        helper.assertTrue(electrum.is(GtItems.ELECTRUM_DUST.get()) && electrum.getCount() == 2, "electrum dust");
        ItemStack saltpeter = new ItemStack(GtItems.SALTPETER_DUST.get());
        ItemStack gunpowder = craft(helper, 2, List.of(new ItemStack(GtItems.CHARCOAL_DUST.get()),
                new ItemStack(GtItems.SULFUR_DUST.get()), saltpeter, saltpeter)).orElse(ItemStack.EMPTY);
        helper.assertTrue(gunpowder.is(Items.GUNPOWDER) && gunpowder.getCount() == 2, "gunpowder");
        ItemStack f = new ItemStack(GtItems.FLINT_DUST.get());
        ItemStack t = new ItemStack(Items.TNT);
        ItemStack itnt = craft(helper, 3, List.of(f, f, f, t, t, t, f, f, f)).orElse(ItemStack.EMPTY);
        helper.assertTrue(itnt.is(Ic2Items.INDUSTRIAL_TNT.get()) && itnt.getCount() == 5, "5 ITNT");
        var smelted = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMELTING,
                new SingleRecipeInput(new ItemStack(GtItems.ELECTRUM_DUST.get())), helper.getLevel());
        helper.assertTrue(smelted.isPresent() && smelted.get().value()
                .getResultItem(helper.getLevel().registryAccess()).is(GtItems.ELECTRUM_INGOT.get()), "smelting");
        helper.succeed();
    }

    /** Scrap box weights of the new dusts (GT chance × 100). */
    @GameTest(template = EMPTY)
    public static void scrapBoxDrops(GameTestHelper helper) {
        assertWeight(helper, GtItems.FLINT_DUST.get(), 400);
        assertWeight(helper, GtItems.WOOD_PULP.get(), 380);
        assertWeight(helper, GtItems.CHARCOAL_DUST.get(), 250);
        assertWeight(helper, GtItems.OBSIDIAN_DUST.get(), 150);
        assertWeight(helper, GtItems.ELECTRUM_DUST.get(), 50);
        helper.succeed();
    }

    /** Phase 13.4: GT reverse recipes for vanilla armor/tools (undamaged only); the tin bucket is gone. */
    @GameTest(template = EMPTY)
    public static void recycling(GameTestHelper helper) {
        assertMacerates(helper, Items.IRON_CHESTPLATE, Ic2Items.IRON_DUST.get(), 8);
        assertMacerates(helper, Items.GOLDEN_SWORD, Ic2Items.GOLD_DUST.get(), 2);
        assertMacerates(helper, Items.DIAMOND_PICKAXE, GtItems.DIAMOND_DUST.get(), 3);
        assertMacerates(helper, Items.WOODEN_AXE, GtItems.SMALL_WOOD_PULP.get(), 16);
        assertMacerates(helper, Items.WOODEN_SWORD, GtItems.SMALL_WOOD_PULP.get(), 10);
        ItemStack worn = new ItemStack(Items.IRON_HELMET);
        worn.setDamageValue(3);
        helper.assertTrue(MachineRecipe.getOutputFor(helper.getLevel(), MachineRecipe.Kind.MACERATOR, worn, false)
                .isEmpty(), "damaged helmet not macerated");
        assertSmelts(helper, new ItemStack(Items.IRON_LEGGINGS), Items.IRON_INGOT, 7);
        assertSmelts(helper, new ItemStack(Items.GOLDEN_HELMET), Items.GOLD_INGOT, 5);
        assertSmelts(helper, new ItemStack(Items.CHAINMAIL_BOOTS), Items.IRON_NUGGET, 1);
        helper.assertTrue(helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMELTING,
                new SingleRecipeInput(worn), helper.getLevel()).isEmpty(), "damaged helmet not smelted");
        helper.assertTrue(MachineRecipe.getOutputFor(helper.getLevel(), MachineRecipe.Kind.MACERATOR,
                new ItemStack(Items.DIAMOND_HELMET), false).is(GtItems.DIAMOND_DUST.get()), "diamond helmet");
        helper.assertTrue(helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMELTING,
                new SingleRecipeInput(new ItemStack(Items.DIAMOND_HELMET)), helper.getLevel()).isEmpty(),
                "no diamond smelting");
        ItemStack tin = new ItemStack(Ic2Items.TIN_INGOT.get());
        helper.assertTrue(craft(helper, 3, List.of(tin, ItemStack.EMPTY, tin, ItemStack.EMPTY, tin, ItemStack.EMPTY))
                .isEmpty(), "tin bucket removed by GregTech");
        helper.succeed();
    }

    private static void assertSmelts(GameTestHelper helper, ItemStack input, Item output, int count) {
        var recipe = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMELTING,
                new SingleRecipeInput(input), helper.getLevel());
        ItemStack result = recipe.map(holder -> holder.value().getResultItem(helper.getLevel().registryAccess()))
                .orElse(ItemStack.EMPTY);
        helper.assertTrue(result.is(output) && result.getCount() == count,
                "smelting " + input + " → " + count + " " + output + ", got " + result);
    }

    private static void assertMacerates(GameTestHelper helper, Item input, Item output, int count) {
        ItemStack result = MachineRecipe.getOutputFor(helper.getLevel(), MachineRecipe.Kind.MACERATOR,
                new ItemStack(input), false);
        helper.assertTrue(result.is(output) && result.getCount() == count,
                "macerator " + input + " → " + count + " " + output + ", got " + result);
    }

    private static void assertWeight(GameTestHelper helper, Item item, int weight) {
        int found = ScrapBoxDrops.all().stream().filter(drop -> drop.stack().is(item))
                .mapToInt(ScrapBoxDrops.Drop::weight).sum();
        helper.assertValueEqual(found, weight, "scrap box weight of " + item);
    }

    private static Optional<ItemStack> craft(GameTestHelper helper, int width, List<ItemStack> items) {
        CraftingInput input = CraftingInput.of(width, items.size() / width, items);
        return helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel())
                .map(holder -> holder.value().assemble(input, helper.getLevel().registryAccess()));
    }
}
