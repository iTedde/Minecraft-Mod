package dev.craft.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import dev.craft.Craft;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.cable.CableType;
import dev.craft.ic2.recipe.Ic2ShapedRecipe;
import dev.craft.ic2.recipe.Ic2ShapelessRecipe;
import dev.craft.ic2.recipe.MachineRecipe;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.Tags;

/**
 * Phase 3 recipes, final pack state ({@code docs/spec/08-rezepte.md} §1–5, 7, 9, 10). Crafting recipes use the IC2
 * recipe types (no mirroring, charge transfer).
 */
public final class CraftRecipeProvider extends RecipeProvider {
    private RecipeOutput output;

    public CraftRecipeProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries) {
        super(packOutput, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        this.output = recipeOutput;
        materials();
        cables();
        machines();
        batteries();
        tools();
        misc();
        smelting();
        macerator();
        compressor();
        extractor();
    }

    // --- §1 materials and components ---

    private void materials() {
        shaped(Ic2Items.REFINED_IRON_INGOT, 8, "refined_iron_from_machine_casing").pattern("M")
                .define('M', Ic2Items.MACHINE_CASING).save();
        shaped(Ic2Items.MACHINE_CASING, 1, "machine_casing").pattern("III", "I I", "III")
                .define('I', CraftItemTagsProvider.INGOTS_REFINED_IRON).save();
        shaped(Ic2Items.ELECTRONIC_CIRCUIT, 1, "electronic_circuit").pattern("CCC", "RIR", "CCC")
                .define('C', Ic2Items.cable(CableType.INSULATED_COPPER)).define('R', Items.REDSTONE)
                .define('I', CraftItemTagsProvider.INGOTS_REFINED_IRON).save();
        shaped(Ic2Items.ELECTRONIC_CIRCUIT, 1, "electronic_circuit_vertical").pattern("CRC", "CIC", "CRC")
                .define('C', Ic2Items.cable(CableType.INSULATED_COPPER)).define('R', Items.REDSTONE)
                .define('I', CraftItemTagsProvider.INGOTS_REFINED_IRON).save();
        shaped(Ic2Items.ADVANCED_CIRCUIT, 1, "advanced_circuit").pattern("RGR", "LCL", "RGR")
                .define('R', Items.REDSTONE).define('G', Items.GLOWSTONE_DUST).define('L', Items.LAPIS_LAZULI)
                .define('C', Ic2Items.ELECTRONIC_CIRCUIT).save();
        shaped(Ic2Items.ADVANCED_CIRCUIT, 1, "advanced_circuit_vertical").pattern("RLR", "GCG", "RLR")
                .define('R', Items.REDSTONE).define('G', Items.GLOWSTONE_DUST).define('L', Items.LAPIS_LAZULI)
                .define('C', Ic2Items.ELECTRONIC_CIRCUIT).save();
        shaped(Ic2Items.COAL_BALL, 1, "coal_ball").pattern("CCC", "CFC", "CCC")
                .define('C', CraftItemTagsProvider.DUSTS_COAL).define('F', Items.FLINT).save();
        shaped(Ic2Items.COAL_CHUNK, 1, "coal_chunk").pattern("###", "#O#", "###")
                .define('#', Ic2Items.COMPRESSED_COAL_BALL).define('O', Items.OBSIDIAN).save();
        shaped(Ic2Items.COAL_CHUNK, 1, "coal_chunk_from_iron_block").pattern("###", "#O#", "###")
                .define('#', Ic2Items.COMPRESSED_COAL_BALL).define('O', Items.IRON_BLOCK).save();
        shaped(Ic2Items.COAL_CHUNK, 1, "coal_chunk_from_bricks").pattern("###", "#O#", "###")
                .define('#', Ic2Items.COMPRESSED_COAL_BALL).define('O', Items.BRICKS).save();
        shaped(Ic2Items.SMALL_IRON_DUST, 1, "small_iron_dust").pattern("CTC", "TCT", "CTC")
                .define('C', CraftItemTagsProvider.DUSTS_COPPER).define('T', CraftItemTagsProvider.DUSTS_TIN).save();
        shaped(Ic2Items.SMALL_IRON_DUST, 1, "small_iron_dust_inverted").pattern("TCT", "CTC", "TCT")
                .define('C', CraftItemTagsProvider.DUSTS_COPPER).define('T', CraftItemTagsProvider.DUSTS_TIN).save();
        shapeless(Ic2Items.IRON_DUST, 1, "iron_dust_from_small", Ingredient.of(Ic2Items.SMALL_IRON_DUST),
                Ingredient.of(Ic2Items.SMALL_IRON_DUST));
        shapeless(Ic2Items.BRONZE_DUST, 2, "bronze_dust", Ingredient.of(CraftItemTagsProvider.DUSTS_TIN),
                Ingredient.of(CraftItemTagsProvider.DUSTS_COPPER), Ingredient.of(CraftItemTagsProvider.DUSTS_COPPER),
                Ingredient.of(CraftItemTagsProvider.DUSTS_COPPER));
        // GregTech (ForestryBronzeNerf): 3 copper + 1 tin give a single bronze ingot.
        shapeless(Ic2Items.BRONZE_INGOT, 1, "bronze_ingot", Ingredient.of(ingotsCopper()),
                Ingredient.of(ingotsCopper()), Ingredient.of(ingotsCopper()),
                Ingredient.of(CraftItemTagsProvider.INGOTS_TIN));
        shaped(Ic2Items.HYDRATED_COAL_DUST, 8, "hydrated_coal_dust").pattern("CCC", "CWC", "CCC")
                .define('C', CraftItemTagsProvider.DUSTS_COAL).define('W', water()).save();
        shapeless(Ic2Items.HYDRATED_COAL_DUST, 1, "hydrated_coal_dust_single",
                Ingredient.of(CraftItemTagsProvider.DUSTS_COAL), water());
        shapeless(Items.DIAMOND, 1, "diamond_from_industrial_diamond", Ingredient.of(Ic2Items.INDUSTRIAL_DIAMOND));
        plantBall("wheat", Ingredient.of(Items.WHEAT), 1);
        plantBall("sugar_cane", Ingredient.of(Items.SUGAR_CANE), 1);
        plantBall("seeds", Ingredient.of(Items.WHEAT_SEEDS), 1);
        plantBall("leaves", Ingredient.of(ItemTags.LEAVES), 1);
        plantBall("saplings", Ingredient.of(ItemTags.SAPLINGS), 2);
        // IC2 registers the rubber sapling twice (1 and 2 plant balls); the first registration wins.
        plantBall("rubber_sapling", Ingredient.of(Ic2Items.RUBBER_SAPLING), 1);
        plantBall("short_grass", Ingredient.of(Items.SHORT_GRASS), 1);
        shaped(Ic2Items.SCRAP_BOX, 1, "scrap_box").pattern("SSS", "SSS", "SSS").define('S', Ic2Items.SCRAP).save();
    }

    private void plantBall(String name, Ingredient ingredient, int count) {
        shaped(Ic2Items.PLANT_BALL, count, "plant_ball_from_" + name).pattern("PPP", "P P", "PPP")
                .define('P', ingredient).save();
    }

    // --- §2 cables ---

    private void cables() {
        shaped(Ic2Items.cable(CableType.INSULATED_COPPER), 6, "insulated_copper_cable").pattern("RRR", "CCC", "RRR")
                .define('R', Ic2Items.RUBBER).define('C', ingotsCopper()).save();
        shaped(Ic2Items.cable(CableType.INSULATED_COPPER), 6, "insulated_copper_cable_vertical")
                .pattern("RCR", "RCR", "RCR").define('R', Ic2Items.RUBBER).define('C', ingotsCopper()).save();
        shaped(Ic2Items.cable(CableType.COPPER), 6, "copper_cable").pattern("CCC").define('C', ingotsCopper()).save();
        shaped(Ic2Items.cable(CableType.GOLD), 12, "gold_cable").pattern("GGG").define('G', Items.GOLD_INGOT).save();
        shaped(Ic2Items.cable(CableType.INSULATED_GOLD), 4, "insulated_gold_cable").pattern(" R ", "RGR", " R ")
                .define('R', Ic2Items.RUBBER).define('G', Items.GOLD_INGOT).save();
        shaped(Ic2Items.cable(CableType.DOUBLE_INSULATED_GOLD), 4, "double_insulated_gold_cable")
                .pattern("RRR", "RGR", "RRR").define('R', Ic2Items.RUBBER).define('G', Items.GOLD_INGOT).save();
        shaped(Ic2Items.cable(CableType.HV), 12, "hv_cable").pattern("III")
                .define('I', CraftItemTagsProvider.INGOTS_REFINED_IRON).save();
        shaped(Ic2Items.cable(CableType.INSULATED_HV), 4, "insulated_hv_cable").pattern(" R ", "RIR", " R ")
                .define('R', Ic2Items.RUBBER).define('I', CraftItemTagsProvider.INGOTS_REFINED_IRON).save();
        shaped(Ic2Items.cable(CableType.TIN), 9, "tin_cable").pattern("TTT")
                .define('T', CraftItemTagsProvider.INGOTS_TIN).save();
        shaped(Ic2Items.cable(CableType.GLASS_FIBRE), 4, "glass_fibre_cable").pattern("GGG", "RDR", "GGG")
                .define('G', Items.GLASS).define('R', Items.REDSTONE).define('D', diamond()).save();
        shaped(Ic2Items.cable(CableType.DETECTOR), 1, "detector_cable").pattern(" C ", "RIR", " R ")
                .define('C', Ic2Items.ELECTRONIC_CIRCUIT).define('R', Items.REDSTONE)
                .define('I', Ic2Items.cable(CableType.QUADRUPLE_INSULATED_HV)).save();
        shaped(Ic2Items.cable(CableType.SPLITTER), 1, "splitter_cable").pattern(" R ", "ILI", " R ")
                .define('R', Items.REDSTONE).define('L', Items.LEVER)
                .define('I', Ic2Items.cable(CableType.QUADRUPLE_INSULATED_HV)).save();
        insulation(CableType.INSULATED_COPPER, 1, CableType.COPPER);
        insulation(CableType.INSULATED_GOLD, 1, CableType.GOLD);
        insulation(CableType.DOUBLE_INSULATED_GOLD, 1, CableType.INSULATED_GOLD);
        insulation(CableType.DOUBLE_INSULATED_GOLD, 2, CableType.GOLD);
        insulation(CableType.INSULATED_HV, 1, CableType.HV);
        insulation(CableType.DOUBLE_INSULATED_HV, 1, CableType.INSULATED_HV);
        insulation(CableType.QUADRUPLE_INSULATED_HV, 1, CableType.DOUBLE_INSULATED_HV);
        insulation(CableType.QUADRUPLE_INSULATED_HV, 2, CableType.INSULATED_HV);
        insulation(CableType.DOUBLE_INSULATED_HV, 2, CableType.HV);
        insulation(CableType.QUADRUPLE_INSULATED_HV, 3, CableType.HV);
    }

    /** Shapeless insulation: {@code rubber} × rubber + one cable → one insulated cable. */
    private void insulation(CableType result, int rubber, CableType cable) {
        List<Ingredient> ingredients = new ArrayList<>();
        for (int i = 0; i < rubber; i++) {
            ingredients.add(Ingredient.of(Ic2Items.RUBBER));
        }
        ingredients.add(Ingredient.of(Ic2Items.cable(cable)));
        shapeless(Ic2Items.cable(result), 1, result.getName() + "_from_" + cable.getName(),
                ingredients.toArray(Ingredient[]::new));
    }

    // --- §3/§4 machines, generators, storage ---

    private void machines() {
        shaped(machine(Ic2Machine.IRON_FURNACE), 1, "iron_furnace").pattern("III", "I I", "III")
                .define('I', Items.IRON_INGOT).save();
        shaped(machine(Ic2Machine.IRON_FURNACE), 1, "iron_furnace_from_furnace").pattern(" I ", "I I", "IFI")
                .define('I', Items.IRON_INGOT).define('F', Items.FURNACE).save();
        shaped(machine(Ic2Machine.ELECTRIC_FURNACE), 1, "electric_furnace").pattern(" C ", "RFR")
                .define('C', Ic2Items.ELECTRONIC_CIRCUIT).define('R', Items.REDSTONE)
                .define('F', machine(Ic2Machine.IRON_FURNACE)).save();
        // GregTech: diamonds and an advanced circuit instead of flint/cobblestone and an electronic circuit.
        shaped(machine(Ic2Machine.MACERATOR), 1, "macerator").pattern("FDF", "DMD", "FAF")
                .define('F', Items.FLINT).define('D', diamond()).define('M', Ic2Items.MACHINE_CASING)
                .define('A', Ic2Items.ADVANCED_CIRCUIT).save();
        shaped(machine(Ic2Machine.EXTRACTOR), 1, "extractor").pattern("TMT", "TCT")
                .define('T', Ic2Items.TREETAP).define('M', Ic2Items.MACHINE_CASING)
                .define('C', Ic2Items.ELECTRONIC_CIRCUIT).save();
        shaped(machine(Ic2Machine.COMPRESSOR), 1, "compressor").pattern("S S", "SMS", "SCS")
                .define('S', Items.STONE).define('M', Ic2Items.MACHINE_CASING)
                .define('C', Ic2Items.ELECTRONIC_CIRCUIT).save();
        shaped(machine(Ic2Machine.RECYCLER), 1, "recycler").pattern(" G ", "DMD", "IDI")
                .define('G', Items.GLOWSTONE_DUST).define('D', Items.DIRT).define('M', machine(Ic2Machine.COMPRESSOR))
                .define('I', CraftItemTagsProvider.INGOTS_REFINED_IRON).save();
        shaped(machine(Ic2Machine.GENERATOR), 1, "generator").pattern(" B ", "III", " F ")
                .define('B', Ic2Items.RE_BATTERY).define('I', CraftItemTagsProvider.INGOTS_REFINED_IRON)
                .define('F', machine(Ic2Machine.IRON_FURNACE)).save();
        shaped(machine(Ic2Machine.GENERATOR), 1, "generator_from_machine_casing").pattern("B", "M", "F")
                .define('B', Ic2Items.RE_BATTERY).define('M', Ic2Items.MACHINE_CASING).define('F', Items.FURNACE)
                .save();
        shaped(machine(Ic2Machine.GEOTHERMAL_GENERATOR), 1, "geothermal_generator").pattern("gCg", "gCg", "IGI")
                .define('g', Items.GLASS).define('C', Ic2Items.EMPTY_CELL)
                .define('I', CraftItemTagsProvider.INGOTS_REFINED_IRON).define('G', machine(Ic2Machine.GENERATOR))
                .save();
        shaped(machine(Ic2Machine.BATBOX), 1, "batbox").pattern("PCP", "BBB", "PPP").define('P', ItemTags.PLANKS)
                .define('C', Ic2Items.cable(CableType.INSULATED_COPPER)).define('B', Ic2Items.RE_BATTERY).save();
    }

    // --- §5 batteries ---

    private void batteries() {
        shaped(Ic2Items.RE_BATTERY, 1, "re_battery").pattern(" C ", "TRT", "TRT")
                .define('C', Ic2Items.cable(CableType.INSULATED_COPPER)).define('T', CraftItemTagsProvider.INGOTS_TIN)
                .define('R', Items.REDSTONE).save();
        shaped(Ic2Items.SU_BATTERY, 5, "su_battery").pattern("C", "R", "D")
                .define('C', Ic2Items.cable(CableType.INSULATED_COPPER)).define('R', Items.REDSTONE)
                .define('D', CraftItemTagsProvider.DUSTS_COAL).save();
        shaped(Ic2Items.SU_BATTERY, 5, "su_battery_inverted").pattern("C", "D", "R")
                .define('C', Ic2Items.cable(CableType.INSULATED_COPPER)).define('R', Items.REDSTONE)
                .define('D', CraftItemTagsProvider.DUSTS_COAL).save();
        shaped(Ic2Items.SU_BATTERY, 8, "su_battery_hydrated").pattern("c", "C", "R")
                .define('c', Ic2Items.cable(CableType.INSULATED_COPPER)).define('C', Ic2Items.HYDRATED_COAL_DUST)
                .define('R', Items.REDSTONE).save();
        shaped(Ic2Items.SU_BATTERY, 8, "su_battery_hydrated_inverted").pattern("c", "R", "C")
                .define('c', Ic2Items.cable(CableType.INSULATED_COPPER)).define('C', Ic2Items.HYDRATED_COAL_DUST)
                .define('R', Items.REDSTONE).save();
        shaped(Ic2Items.ENERGY_CRYSTAL, 1, "energy_crystal").pattern("RRR", "RDR", "RRR")
                .define('R', Items.REDSTONE).define('D', diamond()).save();
        shaped(Ic2Items.LAPOTRON_CRYSTAL, 1, "lapotron_crystal").pattern("LCL", "LDL", "LCL")
                .define('L', Items.LAPIS_LAZULI).define('C', Ic2Items.ELECTRONIC_CIRCUIT)
                .define('D', Ic2Items.ENERGY_CRYSTAL).save();
    }

    // --- §7 tools ---

    private void tools() {
        shaped(Ic2Items.TREETAP, 1, "treetap").pattern(" P ", "PPP", "P  ").define('P', ItemTags.PLANKS).save();
        shaped(Ic2Items.WRENCH, 1, "wrench").pattern("B B", "BBB", " B ")
                .define('B', CraftItemTagsProvider.INGOTS_BRONZE).save();
        shaped(Items.DIAMOND_PICKAXE, 1, "diamond_pickaxe_from_industrial_diamond").pattern("DDD", " S ", " S ")
                .define('D', Ic2Items.INDUSTRIAL_DIAMOND).define('S', Items.STICK).save();
        shaped(Items.DIAMOND_HOE, 1, "diamond_hoe_from_industrial_diamond").pattern("DD ", " S ", " S ")
                .define('D', Ic2Items.INDUSTRIAL_DIAMOND).define('S', Items.STICK).save();
        shaped(Items.DIAMOND_SHOVEL, 1, "diamond_shovel_from_industrial_diamond").pattern("D", "S", "S")
                .define('D', Ic2Items.INDUSTRIAL_DIAMOND).define('S', Items.STICK).save();
        shaped(Items.DIAMOND_AXE, 1, "diamond_axe_from_industrial_diamond").pattern("DD ", "DS ", " S ")
                .define('D', Ic2Items.INDUSTRIAL_DIAMOND).define('S', Items.STICK).save();
        shaped(Items.DIAMOND_SWORD, 1, "diamond_sword_from_industrial_diamond").pattern("D", "D", "S")
                .define('D', Ic2Items.INDUSTRIAL_DIAMOND).define('S', Items.STICK).save();
    }

    // --- §9 misc ---

    private void misc() {
        shaped(Items.TORCH, 4, "torch_from_resin").pattern("R", "I").define('R', Ic2Items.RESIN)
                .define('I', Items.STICK).save();
        shaped(Items.JUNGLE_PLANKS, 3, "jungle_planks_from_rubber_wood").pattern("W")
                .define('W', Ic2Items.RUBBER_WOOD).save();
        // GregTech (TincellsPer4Tin): 4 instead of 16 cells.
        shaped(Ic2Items.EMPTY_CELL, 4, "empty_cell").pattern(" T ", "T T", " T ")
                .define('T', CraftItemTagsProvider.INGOTS_TIN).save();
        shapeless(Ic2Items.EMPTY_CELL, 1, "empty_cell_from_air_cell", Ingredient.of(Ic2Items.AIR_CELL));
        shapeless(Items.OBSIDIAN, 1, "obsidian_from_cells", Ingredient.of(Ic2Items.WATER_CELL),
                Ingredient.of(Ic2Items.WATER_CELL), Ingredient.of(Ic2Items.LAVA_CELL), Ingredient.of(Ic2Items.LAVA_CELL));
        shaped(Ic2Items.COOLANT_CELL_10K, 1, "coolant_cell_10k").pattern(" T ", "TWT", " T ")
                .define('T', CraftItemTagsProvider.INGOTS_TIN).define('W', water()).save();
        shaped(Ic2Items.OVERCLOCKER_UPGRADE, 1, "overclocker_upgrade").pattern("CCC", "WEW")
                .define('C', Ic2Items.COOLANT_CELL_10K).define('W', Ic2Items.cable(CableType.INSULATED_COPPER))
                .define('E', Ic2Items.ELECTRONIC_CIRCUIT).save();
        shaped(Ic2Items.ENERGY_STORAGE_UPGRADE, 1, "energy_storage_upgrade").pattern("www", "WBW", "wEw")
                .define('w', ItemTags.PLANKS).define('W', Ic2Items.cable(CableType.INSULATED_COPPER))
                .define('B', Ic2Items.RE_BATTERY).define('E', Ic2Items.ELECTRONIC_CIRCUIT).save();
        // Hidden IC2 recipes.
        shaped(Items.GLOWSTONE_DUST, 1, "glowstone_dust_from_gold_dust").pattern("RGR", "GRG", "RGR")
                .define('R', Items.REDSTONE).define('G', CraftItemTagsProvider.DUSTS_GOLD).save();
        shaped(Items.GUNPOWDER, 3, "gunpowder_from_coal_dust").pattern("RCR", "CRC", "RCR")
                .define('R', Items.REDSTONE).define('C', CraftItemTagsProvider.DUSTS_COAL).save();
        shaped(Items.BUCKET, 1, "bucket_from_tin").pattern("T T", " T ")
                .define('T', CraftItemTagsProvider.INGOTS_TIN).save();
        shaped(Items.RAIL, 8, "rail_from_bronze").pattern("B B", "BsB", "B B")
                .define('B', CraftItemTagsProvider.INGOTS_BRONZE).define('s', Items.STICK).save();
        shapeless(Items.STICKY_PISTON, 1, "sticky_piston_from_resin", Ingredient.of(Items.PISTON),
                Ingredient.of(Ic2Items.RESIN));
        // GregTech replaces IC2's bronze piston with the vanilla pattern using bronze or refined iron.
        shaped(Items.PISTON, 1, "piston_from_bronze").pattern("WWW", "CBC", "CRC").define('W', ItemTags.PLANKS)
                .define('C', Items.COBBLESTONE).define('B', CraftItemTagsProvider.INGOTS_BRONZE)
                .define('R', Items.REDSTONE).save();
        shaped(Items.PISTON, 1, "piston_from_refined_iron").pattern("WWW", "CBC", "CRC")
                .define('W', ItemTags.PLANKS).define('C', Items.COBBLESTONE)
                .define('B', CraftItemTagsProvider.INGOTS_REFINED_IRON).define('R', Items.REDSTONE).save();
    }

    // --- §10.1 furnace ---

    private void smelting() {
        smelt("tin_ingot_from_ore", Ingredient.of(Ic2Items.TIN_ORE, Ic2Items.DEEPSLATE_TIN_ORE), Ic2Items.TIN_INGOT, 1,
                0.5F);
        smelt("jungle_log_from_rubber_wood", Ingredient.of(Ic2Items.RUBBER_WOOD), Items.JUNGLE_LOG, 1, 0.1F);
        smelt("refined_iron_ingot", Ingredient.of(Items.IRON_INGOT), Ic2Items.REFINED_IRON_INGOT, 1, 0.2F);
        smelt("iron_ingot_from_dust", Ingredient.of(Ic2Items.IRON_DUST), Items.IRON_INGOT, 1, 0.0F);
        smelt("gold_ingot_from_dust", Ingredient.of(Ic2Items.GOLD_DUST), Items.GOLD_INGOT, 1, 0.0F);
        smelt("tin_ingot_from_dust", Ingredient.of(Ic2Items.TIN_DUST), Ic2Items.TIN_INGOT, 1, 0.0F);
        smelt("copper_ingot_from_dust", Ingredient.of(Ic2Items.COPPER_DUST), Items.COPPER_INGOT, 1, 0.0F);
        smelt("bronze_ingot_from_dust", Ingredient.of(Ic2Items.BRONZE_DUST), Ic2Items.BRONZE_INGOT, 1, 0.0F);
        smelt("coal_dust_from_hydrated", Ingredient.of(Ic2Items.HYDRATED_COAL_DUST), Ic2Items.COAL_DUST, 1, 0.0F);
        smelt("rubber_from_resin", Ingredient.of(Ic2Items.RESIN), Ic2Items.RUBBER, 1, 0.3F);
        smelt("refined_iron_from_machine_casing", Ingredient.of(Ic2Items.MACHINE_CASING), Ic2Items.REFINED_IRON_INGOT, 8,
                0.0F);
        smelt("tin_ingots_from_block", Ingredient.of(Ic2Items.TIN_BLOCK), Ic2Items.TIN_INGOT, 9, 0.0F);
        smelt("bronze_ingots_from_block", Ingredient.of(Ic2Items.BRONZE_BLOCK), Ic2Items.BRONZE_INGOT, 9, 0.0F);
        smelt("resin_from_slime_ball", Ingredient.of(Items.SLIME_BALL), Ic2Items.RESIN, 1, 0.0F);
        smelt("bronze_ingots_from_wrench", Ingredient.of(Ic2Items.WRENCH), Ic2Items.BRONZE_INGOT, 6, 0.0F);
    }

    private void smelt(String name, Ingredient input, ItemLike result, int count, float experience) {
        output.accept(id("smelting/" + name), new SmeltingRecipe("", CookingBookCategory.MISC, input,
                new ItemStack(result, count), experience, 200), null);
    }

    // --- §10.2 macerator ---

    private void macerator() {
        machine(MachineRecipe.Kind.MACERATOR, "iron_ore", Ingredient.of(Items.IRON_ORE, Items.DEEPSLATE_IRON_ORE), 1,
                Ic2Items.IRON_DUST, 2);
        machine(MachineRecipe.Kind.MACERATOR, "raw_iron", Ingredient.of(Items.RAW_IRON), 1, Ic2Items.IRON_DUST, 2);
        machine(MachineRecipe.Kind.MACERATOR, "gold_ore", Ingredient.of(Items.GOLD_ORE, Items.DEEPSLATE_GOLD_ORE), 1,
                Ic2Items.GOLD_DUST, 2);
        machine(MachineRecipe.Kind.MACERATOR, "raw_gold", Ingredient.of(Items.RAW_GOLD), 1, Ic2Items.GOLD_DUST, 2);
        machine(MachineRecipe.Kind.MACERATOR, "copper_ore",
                Ingredient.of(Items.COPPER_ORE, Items.DEEPSLATE_COPPER_ORE), 1, Ic2Items.COPPER_DUST, 2);
        machine(MachineRecipe.Kind.MACERATOR, "raw_copper", Ingredient.of(Items.RAW_COPPER), 1, Ic2Items.COPPER_DUST, 2);
        machine(MachineRecipe.Kind.MACERATOR, "tin_ore", Ingredient.of(Ic2Items.TIN_ORE, Ic2Items.DEEPSLATE_TIN_ORE), 1,
                Ic2Items.TIN_DUST, 2);
        machine(MachineRecipe.Kind.MACERATOR, "coal", Ingredient.of(Items.COAL), 1, Ic2Items.COAL_DUST, 1);
        machine(MachineRecipe.Kind.MACERATOR, "coal_ore", Ingredient.of(Items.COAL_ORE, Items.DEEPSLATE_COAL_ORE), 1,
                Ic2Items.COAL_DUST, 2);
        machine(MachineRecipe.Kind.MACERATOR, "lapis_ore", Ingredient.of(Items.LAPIS_ORE, Items.DEEPSLATE_LAPIS_ORE), 1,
                Items.LAPIS_LAZULI, 12);
        machine(MachineRecipe.Kind.MACERATOR, "redstone_ore",
                Ingredient.of(Items.REDSTONE_ORE, Items.DEEPSLATE_REDSTONE_ORE), 1, Items.REDSTONE, 10);
        machine(MachineRecipe.Kind.MACERATOR, "iron_ingot", Ingredient.of(Items.IRON_INGOT), 1, Ic2Items.IRON_DUST, 1);
        machine(MachineRecipe.Kind.MACERATOR, "gold_ingot", Ingredient.of(Items.GOLD_INGOT), 1, Ic2Items.GOLD_DUST, 1);
        machine(MachineRecipe.Kind.MACERATOR, "copper_ingot", Ingredient.of(Items.COPPER_INGOT), 1,
                Ic2Items.COPPER_DUST, 1);
        machine(MachineRecipe.Kind.MACERATOR, "tin_ingot", Ingredient.of(Ic2Items.TIN_INGOT), 1, Ic2Items.TIN_DUST, 1);
        machine(MachineRecipe.Kind.MACERATOR, "refined_iron_ingot", Ingredient.of(Ic2Items.REFINED_IRON_INGOT), 1,
                Ic2Items.IRON_DUST, 1);
        machine(MachineRecipe.Kind.MACERATOR, "bronze_ingot", Ingredient.of(Ic2Items.BRONZE_INGOT), 1,
                Ic2Items.BRONZE_DUST, 1);
        machine(MachineRecipe.Kind.MACERATOR, "copper_block", Ingredient.of(Items.COPPER_BLOCK), 1,
                Ic2Items.COPPER_DUST, 9);
        machine(MachineRecipe.Kind.MACERATOR, "tin_block", Ingredient.of(Ic2Items.TIN_BLOCK), 1, Ic2Items.TIN_DUST, 9);
        machine(MachineRecipe.Kind.MACERATOR, "bronze_block", Ingredient.of(Ic2Items.BRONZE_BLOCK), 1,
                Ic2Items.BRONZE_DUST, 9);
        machine(MachineRecipe.Kind.MACERATOR, "wool", Ingredient.of(ItemTags.WOOL), 1, Items.STRING, 2);
        machine(MachineRecipe.Kind.MACERATOR, "gravel", Ingredient.of(Items.GRAVEL), 1, Items.FLINT, 1);
        machine(MachineRecipe.Kind.MACERATOR, "stone", Ingredient.of(Items.STONE), 1, Items.COBBLESTONE, 1);
        machine(MachineRecipe.Kind.MACERATOR, "cobblestone", Ingredient.of(Items.COBBLESTONE), 1, Items.SAND, 1);
        machine(MachineRecipe.Kind.MACERATOR, "sandstone", Ingredient.of(Items.SANDSTONE), 1, Items.SAND, 1);
        machine(MachineRecipe.Kind.MACERATOR, "ice", Ingredient.of(Items.ICE), 1, Items.SNOWBALL, 1);
        machine(MachineRecipe.Kind.MACERATOR, "snow_block", Ingredient.of(Items.SNOW_BLOCK), 1, Items.SNOWBALL, 4);
        machine(MachineRecipe.Kind.MACERATOR, "clay", Ingredient.of(Items.CLAY), 1, Ic2Items.CLAY_DUST, 2);
        machine(MachineRecipe.Kind.MACERATOR, "glowstone", Ingredient.of(Items.GLOWSTONE), 1,
                Items.GLOWSTONE_DUST, 4);
        machine(MachineRecipe.Kind.MACERATOR, "bone", Ingredient.of(Items.BONE), 1, Items.BONE_MEAL, 5);
        machine(MachineRecipe.Kind.MACERATOR, "plant_ball", Ingredient.of(Ic2Items.PLANT_BALL), 1, Items.DIRT, 1);
        machine(MachineRecipe.Kind.MACERATOR, "blaze_rod", Ingredient.of(Items.BLAZE_ROD), 1,
                Items.BLAZE_POWDER, 5);
        machine(MachineRecipe.Kind.MACERATOR, "machine_casing", Ingredient.of(Ic2Items.MACHINE_CASING), 1,
                Ic2Items.IRON_DUST, 8);
        machine(MachineRecipe.Kind.MACERATOR, "melon", Ingredient.of(Items.MELON), 1, Items.MELON_SLICE, 8);
        machine(MachineRecipe.Kind.MACERATOR, "pumpkin", Ingredient.of(Items.PUMPKIN), 1, Items.PUMPKIN_SEEDS, 4);
        machine(MachineRecipe.Kind.MACERATOR, "melon_slice", Ingredient.of(Items.MELON_SLICE), 1,
                Items.MELON_SEEDS, 1);
        machine(MachineRecipe.Kind.MACERATOR, "wrench", Ingredient.of(Ic2Items.WRENCH), 1, Ic2Items.BRONZE_DUST, 6);
    }

    // --- §10.3 compressor ---

    private void compressor() {
        machine(MachineRecipe.Kind.COMPRESSOR, "plant_ball", Ingredient.of(Ic2Items.PLANT_BALL), 1,
                Ic2Items.COMPRESSED_PLANT_BALL, 1);
        for (Item plant : new Item[]{Items.WHEAT, Items.SUGAR_CANE, Items.CACTUS, Items.POPPY, Items.DANDELION}) {
            machine(MachineRecipe.Kind.COMPRESSOR, BuiltInRegistries.ITEM.getKey(plant)
                    .getPath(), Ingredient.of(plant), 8, Ic2Items.COMPRESSED_PLANT_BALL, 1);
        }
        machine(MachineRecipe.Kind.COMPRESSOR, "hydrated_coal_dust", Ingredient.of(Ic2Items.HYDRATED_COAL_DUST), 1,
                Ic2Items.HYDRATED_COAL_CLUMP, 1);
        machine(MachineRecipe.Kind.COMPRESSOR, "netherrack", Ingredient.of(Items.NETHERRACK), 3,
                Items.NETHER_BRICK, 1);
        machine(MachineRecipe.Kind.COMPRESSOR, "sand", Ingredient.of(Items.SAND), 4, Items.SANDSTONE, 1);
        machine(MachineRecipe.Kind.COMPRESSOR, "snowball", Ingredient.of(Items.SNOWBALL), 1, Items.ICE, 1);
        machine(MachineRecipe.Kind.COMPRESSOR, "water_cell", Ingredient.of(Ic2Items.WATER_CELL), 1,
                Items.SNOWBALL, 1);
        machine(MachineRecipe.Kind.COMPRESSOR, "coal_ball", Ingredient.of(Ic2Items.COAL_BALL), 1,
                Ic2Items.COMPRESSED_COAL_BALL, 1);
        // GregTech: an industrial diamond instead of a diamond.
        machine(MachineRecipe.Kind.COMPRESSOR, "coal_chunk", Ingredient.of(Ic2Items.COAL_CHUNK), 1,
                Ic2Items.INDUSTRIAL_DIAMOND, 1);
        machine(MachineRecipe.Kind.COMPRESSOR, "empty_cell", Ingredient.of(Ic2Items.EMPTY_CELL), 1, Ic2Items.AIR_CELL, 1);
        machine(MachineRecipe.Kind.COMPRESSOR, "clay_ball", Ingredient.of(Items.CLAY_BALL), 4, Items.CLAY, 1);
        machine(MachineRecipe.Kind.COMPRESSOR, "glowstone_dust", Ingredient.of(Items.GLOWSTONE_DUST), 4,
                Items.GLOWSTONE, 1);
        machine(MachineRecipe.Kind.COMPRESSOR, "tin_ingot", Ingredient.of(Ic2Items.TIN_INGOT), 9, Ic2Items.TIN_BLOCK, 1);
        machine(MachineRecipe.Kind.COMPRESSOR, "bronze_ingot", Ingredient.of(Ic2Items.BRONZE_INGOT), 9,
                Ic2Items.BRONZE_BLOCK, 1);
    }

    // --- §10.4 extractor ---

    private void extractor() {
        machine(MachineRecipe.Kind.EXTRACTOR, "rubber_sapling", Ingredient.of(Ic2Items.RUBBER_SAPLING), 1,
                Ic2Items.RUBBER, 1);
        machine(MachineRecipe.Kind.EXTRACTOR, "resin", Ingredient.of(Ic2Items.RESIN), 1, Ic2Items.RUBBER, 3);
        machine(MachineRecipe.Kind.EXTRACTOR, "rubber_wood", Ingredient.of(Ic2Items.RUBBER_WOOD), 1, Ic2Items.RUBBER, 1);
        machine(MachineRecipe.Kind.EXTRACTOR, "slime_ball", Ingredient.of(Items.SLIME_BALL), 1, Ic2Items.RUBBER, 2);
        machine(MachineRecipe.Kind.EXTRACTOR, "air_cell", Ingredient.of(Ic2Items.AIR_CELL), 1, Ic2Items.EMPTY_CELL, 1);
        machine(MachineRecipe.Kind.EXTRACTOR, "wool", Ingredient.of(ItemTags.WOOL), 1, Items.WHITE_WOOL, 1);
        machine(MachineRecipe.Kind.EXTRACTOR, "poppy", Ingredient.of(Items.POPPY), 1, Items.RED_DYE, 3);
        machine(MachineRecipe.Kind.EXTRACTOR, "dandelion", Ingredient.of(Items.DANDELION), 1, Items.YELLOW_DYE, 3);
    }

    private void machine(MachineRecipe.Kind kind, String name, Ingredient input, int inputCount,
                         ItemLike result, int count) {
        output.accept(id(kind.getName() + "/" + name),
                new MachineRecipe(kind, input, inputCount, new ItemStack(result, count)), null);
    }

    // --- helpers ---

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Craft.MODID, path);
    }

    private static TagKey<Item> ingotsCopper() {
        return Tags.Items.INGOTS_COPPER;
    }

    /** IC2 {@code liquid$water}: water bucket (the bucket stays) or water cell (consumed). */
    private static Ingredient water() {
        return Ingredient.of(Items.WATER_BUCKET, Ic2Items.WATER_CELL.get());
    }

    private static Ingredient diamond() {
        return Ingredient.of(Items.DIAMOND, Ic2Items.INDUSTRIAL_DIAMOND.get());
    }

    private static ItemLike machine(Ic2Machine machine) {
        return Ic2Items.machine(machine);
    }

    private ShapedBuilder shaped(ItemLike result, int count, String name) {
        return new ShapedBuilder(result, count, name);
    }

    private void shapeless(ItemLike result, int count, String name, Ingredient... ingredients) {
        output.accept(id(name), new Ic2ShapelessRecipe(new ItemStack(result, count),
                NonNullList.of(Ingredient.EMPTY, ingredients)), null);
    }

    private final class ShapedBuilder {
        private final ItemLike result;
        private final int count;
        private final String name;
        private final List<String> rows = new ArrayList<>();
        private final Map<Character, Ingredient> key = new LinkedHashMap<>();

        ShapedBuilder(ItemLike result, int count, String name) {
            this.result = result;
            this.count = count;
            this.name = name;
        }

        ShapedBuilder pattern(String... rows) {
            this.rows.addAll(List.of(rows));
            return this;
        }

        ShapedBuilder define(char symbol, Ingredient ingredient) {
            key.put(symbol, ingredient);
            return this;
        }

        ShapedBuilder define(char symbol, TagKey<Item> tag) {
            return define(symbol, Ingredient.of(tag));
        }

        ShapedBuilder define(char symbol, ItemLike item) {
            return define(symbol, Ingredient.of(item));
        }

        void save() {
            output.accept(id(name), new Ic2ShapedRecipe(ShapedRecipePattern.of(key, rows),
                    new ItemStack(result, count)), null);
        }
    }
}
