package dev.craft.gregtech.world;

import java.util.List;

import dev.craft.Craft;
import dev.craft.data.CraftWorldGen;
import dev.craft.gregtech.GtBlocks;
import dev.craft.gregtech.GtItems;
import dev.craft.gregtech.recipe.GtRecipe;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.item.tool.OreValues;
import dev.craft.ic2.machine.MassFabricatorBlockEntity;
import dev.craft.ic2.recipe.MachineRecipe;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** GregTech ores (phase 8c): blocks, drops, world generation registration and the processing they unlock. */
@GameTestHolder(Craft.MODID)
@PrefixGameTestTemplate(false)
public final class GtOreGameTests {
    private static final String EMPTY = "empty_8x4x8";
    private static final BlockPos POS = new BlockPos(3, 1, 3);

    private GtOreGameTests() {
    }

    /** Harvest level, hardness and ore value as in {@code GT_BlockMetaID_Ore}; deepslate variants are 1.5 harder. */
    @GameTest(template = EMPTY)
    public static void oreBlocks(GameTestHelper helper) {
        BlockState iridium = GtBlocks.ore(GtOre.IRIDIUM).get().defaultBlockState();
        helper.assertFalse(new ItemStack(Items.IRON_PICKAXE).isCorrectToolForDrops(iridium), "iron pickaxe on iridium");
        helper.assertTrue(new ItemStack(Items.DIAMOND_PICKAXE).isCorrectToolForDrops(iridium), "diamond on iridium");
        BlockState galena = GtBlocks.ore(GtOre.GALENA).get().defaultBlockState();
        helper.assertTrue(new ItemStack(Items.STONE_PICKAXE).isCorrectToolForDrops(galena), "stone on galena");
        helper.assertValueEqual(galena.getBlock().defaultDestroyTime(), 3.0F, "galena hardness");
        helper.assertValueEqual(GtBlocks.deepslateOre(GtOre.GALENA).get().defaultDestroyTime(), 4.5F,
                "deepslate galena hardness");
        helper.assertTrue(GtBlocks.deepslateOre(GtOre.BAUXITE) == null, "no deepslate bauxite");
        helper.assertValueEqual(OreValues.valueOf(iridium), 10, "iridium ore value");
        helper.assertValueEqual(OreValues.valueOf(GtBlocks.ore(GtOre.PYRITE).get().defaultBlockState()), 1,
                "pyrite ore value");
        helper.succeed();
    }

    /** Drops without fortune: iridium 1 IC2 iridium ore, sodalite 6 dust, galena itself; silk touch the ore. */
    @GameTest(template = EMPTY)
    public static void oreDrops(GameTestHelper helper) {
        ItemStack pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        List<ItemStack> iridium = drops(helper, GtBlocks.ore(GtOre.IRIDIUM).get(), pickaxe);
        helper.assertTrue(iridium.size() == 1 && iridium.get(0).is(Ic2Items.IRIDIUM_ORE.get())
                && iridium.get(0).getCount() == 1, "iridium drop " + iridium);
        List<ItemStack> sodalite = drops(helper, GtBlocks.ore(GtOre.SODALITE).get(), pickaxe);
        helper.assertTrue(sodalite.stream().anyMatch(stack -> stack.is(GtItems.SODALITE_DUST.get())
                && stack.getCount() == 6), "sodalite drop " + sodalite);
        List<ItemStack> galena = drops(helper, GtBlocks.ore(GtOre.GALENA).get(), pickaxe);
        helper.assertTrue(galena.size() == 1 && galena.get(0).is(GtItems.ore(GtOre.GALENA).get()), "galena drop");
        ItemStack silk = pickaxe.copy();
        silk.enchant(helper.getLevel().registryAccess().lookupOrThrow(Registries.ENCHANTMENT)
                .getOrThrow(Enchantments.SILK_TOUCH), 1);
        List<ItemStack> ruby = drops(helper, GtBlocks.deepslateOre(GtOre.RUBY).get(), silk);
        helper.assertTrue(ruby.size() == 1 && ruby.get(0).is(GtItems.deepslateOre(GtOre.RUBY).get()), "silk touch");
        helper.succeed();
    }

    /** The overworld, nether and end features are in the registries. */
    @GameTest(template = EMPTY)
    public static void worldGenRegistered(GameTestHelper helper) {
        var placed = helper.getLevel().registryAccess().registryOrThrow(Registries.PLACED_FEATURE);
        for (var key : List.of(CraftWorldGen.GT_GALENA, CraftWorldGen.GT_IRIDIUM, CraftWorldGen.GT_RUBY,
                CraftWorldGen.GT_SAPPHIRE, CraftWorldGen.GT_BAUXITE, CraftWorldGen.GT_NETHER_ORES,
                CraftWorldGen.GT_END_ORES)) {
            helper.assertTrue(placed.containsKey(key), "placed feature " + key.location());
        }
        helper.succeed();
    }

    /** Macerator and furnace (spec 07 §6.4). */
    @GameTest(template = EMPTY)
    public static void maceratorAndFurnace(GameTestHelper helper) {
        assertMacerator(helper, GtItems.ore(GtOre.GALENA).get().getDefaultInstance(), GtItems.GALENA_DUST.get()
                .getDefaultInstance().copyWithCount(2));
        assertMacerator(helper, GtItems.deepslateOre(GtOre.IRIDIUM).get().getDefaultInstance(),
                new ItemStack(Ic2Items.IRIDIUM_ORE.get(), 2));
        assertMacerator(helper, GtItems.ore(GtOre.SODALITE).get().getDefaultInstance(),
                new ItemStack(GtItems.SODALITE_DUST.get(), 12));
        assertMacerator(helper, new ItemStack(GtItems.RUBY.get()), new ItemStack(GtItems.RUBY_DUST.get()));
        assertSmelting(helper, GtItems.ore(GtOre.PYRITE).get().getDefaultInstance(), Items.IRON_INGOT);
        assertSmelting(helper, GtItems.ore(GtOre.SHELDONITE).get().getDefaultInstance(), GtItems.PLATINUM_INGOT.get());
        assertSmelting(helper, GtItems.ore(GtOre.TUNGSTATE).get().getDefaultInstance(), GtItems.TUNGSTEN_DUST.get());
        helper.succeed();
    }

    /** Blast furnace with calcium carbonate cell, nether dust electrolysis, gem implosion, amplifiers. */
    @GameTest(template = EMPTY)
    public static void unlockedProcessing(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        GtRecipe pyrite = GtRecipe.find(level, GtRecipe.Kind.BLAST_FURNACE,
                GtItems.ore(GtOre.PYRITE).get().getDefaultInstance(), new ItemStack(GtItems.CALCIUM_CARBONATE_CELL.get()),
                false);
        helper.assertTrue(pyrite != null && pyrite.getOutput(0).is(Ic2Items.REFINED_IRON_INGOT.get())
                && pyrite.getOutput(0).getCount() == 2 && pyrite.getSpecial() == 1500, "pyrite blast");
        GtRecipe iron = GtRecipe.find(level, GtRecipe.Kind.BLAST_FURNACE, new ItemStack(Items.IRON_ORE),
                new ItemStack(GtItems.CALCIUM_CARBONATE_CELL.get()), false);
        helper.assertTrue(iron != null && iron.getOutput(0).getCount() == 3 && iron.getSpecial() == 1000, "iron blast");
        GtRecipe cell = GtRecipe.find(level, GtRecipe.Kind.CANNER, new ItemStack(GtItems.CALCITE_DUST.get()),
                new ItemStack(Ic2Items.EMPTY_CELL.get()), false);
        helper.assertTrue(cell != null && cell.getOutput(0).is(GtItems.CALCIUM_CARBONATE_CELL.get()), "calcite canning");
        GtRecipe sphalerite = GtRecipe.find(level, GtRecipe.Kind.ELECTROLYZER,
                new ItemStack(GtItems.SPHALERITE_DUST.get(), 2), ItemStack.EMPTY, false);
        helper.assertTrue(sphalerite != null && sphalerite.getOutput(0).is(GtItems.ZINC_DUST.get())
                && sphalerite.getOutput(1).is(GtItems.SULFUR_DUST.get()) && sphalerite.getDuration() == 150,
                "sphalerite electrolysis");
        GtRecipe cinnabar = GtRecipe.find(level, GtRecipe.Kind.ELECTROLYZER,
                new ItemStack(GtItems.CINNABAR_DUST.get(), 2), new ItemStack(Ic2Items.EMPTY_CELL.get()), false);
        helper.assertTrue(cinnabar != null && cinnabar.getOutput(0).is(GtItems.MERCURY_CELL.get()), "cinnabar");
        GtRecipe ruby = GtRecipe.find(level, GtRecipe.Kind.IMPLOSION, new ItemStack(GtItems.RUBY_DUST.get(), 4),
                new ItemStack(Ic2Items.INDUSTRIAL_TNT.get(), 24), false);
        helper.assertTrue(ruby != null && ruby.getOutput(0).is(GtItems.RUBY.get()) && ruby.getOutput(0).getCount() == 3,
                "ruby implosion");
        helper.assertValueEqual(MassFabricatorBlockEntity.getAmplifierValue(new ItemStack(GtItems.PLATINUM_DUST.get())),
                100000, "platinum amplifier");
        helper.assertValueEqual(MassFabricatorBlockEntity.getAmplifierValue(new ItemStack(GtItems.OLIVINE_DUST.get())),
                50000, "olivine amplifier");
        helper.succeed();
    }

    private static List<ItemStack> drops(GameTestHelper helper, Block block, ItemStack tool) {
        return Block.getDrops(block.defaultBlockState(), helper.getLevel(), helper.absolutePos(POS), null, null, tool);
    }

    private static void assertMacerator(GameTestHelper helper, ItemStack input, ItemStack expected) {
        ItemStack result = MachineRecipe.getOutputFor(helper.getLevel(), MachineRecipe.Kind.MACERATOR, input, false);
        helper.assertTrue(ItemStack.isSameItem(result, expected) && result.getCount() == expected.getCount(),
                "macerator " + input + " → " + result);
    }

    private static void assertSmelting(GameTestHelper helper, ItemStack input, Item expected) {
        var recipe = helper.getLevel().getRecipeManager().getRecipeFor(RecipeType.SMELTING,
                new SingleRecipeInput(input), helper.getLevel());
        helper.assertTrue(recipe.isPresent() && recipe.get().value().getResultItem(helper.getLevel().registryAccess())
                .is(expected), "smelting " + input);
    }
}
