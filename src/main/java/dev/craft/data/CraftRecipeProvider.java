package dev.craft.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import dev.craft.Craft;
import dev.craft.buildcraft.BcItems;
import dev.craft.buildcraft.engine.EngineType;
import dev.craft.forestry.FrFluids;
import dev.craft.forestry.FrItems;
import dev.craft.forestry.circuit.CircuitBoardType;
import dev.craft.forestry.circuit.ElectronTube;
import dev.craft.forestry.recipe.CarpenterRecipe;
import dev.craft.forestry.recipe.FabricatorRecipe;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.cable.CableType;
import dev.craft.ic2.recipe.Ic2ShapedRecipe;
import dev.craft.ic2.recipe.Ic2ShapelessRecipe;
import dev.craft.ic2.recipe.MachineRecipe;
import dev.craft.powerconverters.ConverterType;
import dev.craft.powerconverters.PcItems;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
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
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.fluids.FluidStack;

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
        armor();
        misc();
        smelting();
        macerator();
        compressor();
        extractor();
        buildcraft();
        forestry();
        powerConverters();
    }

    // --- §14 Power Converters (vanilla recipes) ---

    private void powerConverters() {
        bcShaped(PcItems.ENERGY_BRIDGE).pattern("GRG").pattern("LDL").pattern("GRG").define('G', Items.GOLD_INGOT)
                .define('R', Items.REDSTONE).define('L', Items.GLASS).define('D', Items.DIAMOND)
                .unlockedBy("has_diamond", has(Items.DIAMOND)).save(output, id("energy_bridge"));
        bcShaped(PcItems.UNIVERSAL_CHARGER).pattern("GRG").pattern("ICI").pattern("GRG").define('G', Items.GOLD_INGOT)
                .define('R', Items.REDSTONE).define('I', Items.IRON_INGOT).define('C', Items.CHEST)
                .unlockedBy("has_gold", has(Items.GOLD_INGOT)).save(output, id("universal_charger"));
        bcShaped(PcItems.converter(ConverterType.BC_CONSUMER)).pattern("G G").pattern(" E ").pattern("G G")
                .define('G', Items.GOLD_INGOT).define('E', BcItems.engine(EngineType.STIRLING))
                .unlockedBy("has_engine", has(BcItems.engine(EngineType.STIRLING))).save(output, id("bc_consumer"));
        ic2Consumer(ConverterType.IC2_LV_CONSUMER, Ic2Machine.LV_TRANSFORMER);
        ic2Consumer(ConverterType.IC2_MV_CONSUMER, Ic2Machine.MV_TRANSFORMER);
        ic2Consumer(ConverterType.IC2_HV_CONSUMER, Ic2Machine.HV_TRANSFORMER);
        ic2Consumer(ConverterType.IC2_EV_CONSUMER, Ic2Machine.MFSU);
        for (ConverterType type : ConverterType.values()) {
            ItemLike from = PcItems.converter(type);
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, PcItems.converter(type.counterpart()))
                    .requires(from).unlockedBy("has_converter", has(from))
                    .save(output, id(type.counterpart().getName() + "_from_" + type.getName()));
        }
    }

    private void ic2Consumer(ConverterType type, Ic2Machine core) {
        bcShaped(PcItems.converter(type)).pattern("G G").pattern(" T ").pattern("G G").define('G', Items.GOLD_INGOT)
                .define('T', machine(core)).unlockedBy("has_core", has(machine(core)))
                .save(output, id(type.getName()));
    }

    // --- §12 BuildCraft (vanilla shaped recipes, mirroring allowed as in 1.4.7) ---

    private void buildcraft() {
        bcShaped(BcItems.WOODEN_GEAR).pattern(" S ").pattern("S S").pattern(" S ").define('S', Items.STICK)
                .unlockedBy("has_stick", has(Items.STICK)).save(output, id("wooden_gear"));
        gear(BcItems.STONE_GEAR, Ingredient.of(Items.COBBLESTONE), BcItems.WOODEN_GEAR);
        gear(BcItems.IRON_GEAR, Ingredient.of(Items.IRON_INGOT), BcItems.STONE_GEAR);
        gear(BcItems.GOLD_GEAR, Ingredient.of(Items.GOLD_INGOT), BcItems.IRON_GEAR);
        gear(BcItems.DIAMOND_GEAR, Ingredient.of(Items.DIAMOND), BcItems.GOLD_GEAR);
        bcShaped(BcItems.WRENCH).pattern("I I").pattern(" G ").pattern(" I ").define('I', Items.IRON_INGOT)
                .define('G', BcItems.STONE_GEAR).unlockedBy("has_gear", has(BcItems.STONE_GEAR))
                .save(output, id("bc_wrench"));
        engine(EngineType.REDSTONE, Ingredient.of(ItemTags.PLANKS), BcItems.WOODEN_GEAR);
        engine(EngineType.STIRLING, Ingredient.of(Items.COBBLESTONE), BcItems.STONE_GEAR);
        engine(EngineType.COMBUSTION, Ingredient.of(Items.IRON_INGOT), BcItems.IRON_GEAR);
        // "l /r " – the empty second column of the original makes no difference.
        bcShaped(BcItems.LANDMARK).pattern("l").pattern("r").define('l', Items.LAPIS_LAZULI)
                .define('r', Items.REDSTONE_TORCH).unlockedBy("has_lapis", has(Items.LAPIS_LAZULI))
                .save(output, id("landmark"));
        // GregTech ExpensiveBCQuarryRecipe; the diamond drill (any charge) itself needs titanium plates (phase 6b).
        bcShaped(BcItems.QUARRY).pattern("ICI").pattern("GIG").pattern("DPD").define('I', BcItems.IRON_GEAR)
                .define('C', Ic2Items.ADVANCED_CIRCUIT).define('G', BcItems.GOLD_GEAR).define('D', BcItems.DIAMOND_GEAR)
                .define('P', Ic2Items.DIAMOND_DRILL).unlockedBy("has_drill", has(Ic2Items.DIAMOND_DRILL))
                .save(output, id("quarry"));
        bcShaped(BcItems.TANK).pattern("ggg").pattern("g g").pattern("ggg").define('g', Items.GLASS)
                .unlockedBy("has_glass", has(Items.GLASS)).save(output, id("tank"));
        // Item.pickaxeSteel becomes an ItemStack with damage 0: only an undamaged pickaxe fits, as in 1.4.7.
        bcShaped(BcItems.MINING_WELL).pattern("ipi").pattern("igi").pattern("iPi").define('i', Items.IRON_INGOT)
                .define('p', Items.REDSTONE).define('g', BcItems.IRON_GEAR)
                .define('P', DataComponentIngredient.of(false, DataComponents.DAMAGE, 0, Items.IRON_PICKAXE))
                .unlockedBy("has_gear", has(BcItems.IRON_GEAR)).save(output, id("mining_well"));
        // "T /W " (mining.enabled = true) – the empty second column makes no difference.
        bcShaped(BcItems.PUMP).pattern("T").pattern("W").define('T', BcItems.TANK).define('W', BcItems.MINING_WELL)
                .unlockedBy("has_mining_well", has(BcItems.MINING_WELL)).save(output, id("pump"));
        // "   /RTR/TGT": 1.21 drops the empty top row, so the recipe also fits into the upper two rows.
        bcShaped(BcItems.REFINERY).pattern("RTR").pattern("TGT").define('R', Items.REDSTONE_TORCH)
                .define('T', BcItems.TANK).define('G', BcItems.DIAMOND_GEAR)
                .unlockedBy("has_tank", has(BcItems.TANK)).save(output, id("refinery"));
    }

    private static ShapedRecipeBuilder bcShaped(ItemLike result) {
        return ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result);
    }

    private void gear(ItemLike result, Ingredient material, ItemLike previous) {
        bcShaped(result).pattern(" I ").pattern("IGI").pattern(" I ").define('I', material).define('G', previous)
                .unlockedBy("has_gear", has(previous))
                .save(output, id(BuiltInRegistries.ITEM.getKey(result.asItem()).getPath()));
    }

    private void engine(EngineType type, Ingredient material, ItemLike gear) {
        bcShaped(BcItems.engine(type)).pattern("www").pattern(" g ").pattern("GpG").define('w', material)
                .define('g', Items.GLASS).define('G', gear).define('p', Items.PISTON)
                .unlockedBy("has_gear", has(gear)).save(output, id(type.getName()));
    }

    // --- §13 Forestry (vanilla shaped recipes with mirroring; machine recipes in the Fabricator and Carpenter) ---

    private void forestry() {
        bcShaped(FrItems.STURDY_CASING).pattern("###").pattern("# #").pattern("###")
                .define('#', CraftItemTagsProvider.INGOTS_BRONZE).unlockedBy("has_bronze",
                        has(CraftItemTagsProvider.INGOTS_BRONZE)).save(output, id("sturdy_casing"));
        bcShaped(FrItems.THERMIONIC_FABRICATOR).pattern("X#X").pattern("#Y#").pattern("XZX")
                .define('X', Items.GOLD_INGOT).define('#', Items.GLASS).define('Y', FrItems.STURDY_CASING)
                .define('Z', Items.CHEST).unlockedBy("has_casing", has(FrItems.STURDY_CASING))
                .save(output, id("thermionic_fabricator"));
        bcShaped(FrItems.CARPENTER).pattern("X#X").pattern("XYX").pattern("X#X")
                .define('X', CraftItemTagsProvider.INGOTS_BRONZE).define('#', Items.GLASS)
                .define('Y', FrItems.STURDY_CASING).unlockedBy("has_casing", has(FrItems.STURDY_CASING))
                .save(output, id("carpenter"));
        // gearTin: the BuildCraft variant (stone gear); the copper ingot variant only exists without BuildCraft.
        gear(FrItems.TIN_GEAR, Ingredient.of(CraftItemTagsProvider.INGOTS_TIN), BcItems.STONE_GEAR);
        bcShaped(FrItems.ELECTRICAL_ENGINE).pattern("###").pattern(" X ").pattern("YVY")
                .define('#', CraftItemTagsProvider.INGOTS_TIN).define('X', Items.GLASS).define('Y', FrItems.TIN_GEAR)
                .define('V', Items.PISTON).unlockedBy("has_gear", has(FrItems.TIN_GEAR))
                .save(output, id("electrical_engine"));
        tube(ElectronTube.COPPER, Ingredient.of(ingotsCopper()));
        tube(ElectronTube.TIN, Ingredient.of(CraftItemTagsProvider.INGOTS_TIN));
        tube(ElectronTube.BRONZE, Ingredient.of(CraftItemTagsProvider.INGOTS_BRONZE));
        tube(ElectronTube.IRON, Ingredient.of(Items.IRON_INGOT));
        carpenter("basic_circuit_board", 20, FrItems.circuitBoard(CircuitBoardType.BASIC),
                Map.of('#', Ingredient.of(CraftItemTagsProvider.INGOTS_TIN), 'R', Ingredient.of(Items.REDSTONE)),
                "R R", "R#R", "R R");
        carpenter("enhanced_circuit_board", 40, FrItems.circuitBoard(CircuitBoardType.ENHANCED),
                Map.of('#', Ingredient.of(CraftItemTagsProvider.INGOTS_BRONZE), 'R', Ingredient.of(Items.REDSTONE)),
                "R#R", "R#R", "R#R");
        carpenter("refined_circuit_board", 80, FrItems.circuitBoard(CircuitBoardType.REFINED),
                Map.of('#', Ingredient.of(Items.IRON_INGOT), 'R', Ingredient.of(Items.REDSTONE)),
                "R#R", "R#R", "R#R");
        carpenter("intricate_circuit_board", 80, FrItems.circuitBoard(CircuitBoardType.INTRICATE),
                Map.of('#', Ingredient.of(Items.GOLD_INGOT), 'R', Ingredient.of(Items.REDSTONE)),
                "R#R", "R#R", "R#R");
        carpenter("soldering_iron", 40, FrItems.SOLDERING_IRON,
                Map.of('#', Ingredient.of(Items.IRON_INGOT), 'B', Ingredient.of(CraftItemTagsProvider.INGOTS_BRONZE)),
                " # ", "# #", "  B");
    }

    /** {@code fabricatorManager.addRecipe(null, 500 mB liquid glass, 4 tubes, " X /#X#/XXX")}, # = redstone. */
    private void tube(ElectronTube tube, Ingredient metal) {
        output.accept(id("fabricator/" + tube.getName() + "_electron_tube"), new FabricatorRecipe(Optional.empty(),
                new FluidStack(FrFluids.LIQUID_GLASS.get(), 500),
                ShapedRecipePattern.of(Map.of('#', Ingredient.of(Items.REDSTONE), 'X', metal), " X ", "#X#", "XXX"),
                new ItemStack(FrItems.tube(tube).get(), 4)), null);
    }

    /** {@code carpenterManager.addRecipe(steps, 1000 mB water, no box, result, pattern)}. */
    private void carpenter(String name, int packagingTime, ItemLike result, Map<Character, Ingredient> key,
                           String... pattern) {
        output.accept(id("carpenter/" + name), new CarpenterRecipe(packagingTime,
                new FluidStack(Fluids.WATER, 1000), Optional.empty(), ShapedRecipePattern.of(key, pattern),
                new ItemStack(result)), null);
    }

    // --- §1 materials and components ---

    private void materials() {
        shaped(Ic2Items.REFINED_IRON_INGOT, 8, "refined_iron_from_machine_casing").pattern("M")
                .define('M', Ic2Items.MACHINE_CASING).save();
        shaped(Ic2Items.MACHINE_CASING, 1, "machine_casing").pattern("III").pattern("I I").pattern("III")
                .define('I', CraftItemTagsProvider.INGOTS_REFINED_IRON).save();
        shaped(Ic2Items.ELECTRONIC_CIRCUIT, 1, "electronic_circuit").pattern("CCC").pattern("RIR").pattern("CCC")
                .define('C', Ic2Items.cable(CableType.INSULATED_COPPER)).define('R', Items.REDSTONE)
                .define('I', CraftItemTagsProvider.INGOTS_REFINED_IRON).save();
        shaped(Ic2Items.ELECTRONIC_CIRCUIT, 1, "electronic_circuit_vertical").pattern("CRC").pattern("CIC").pattern("CRC")
                .define('C', Ic2Items.cable(CableType.INSULATED_COPPER)).define('R', Items.REDSTONE)
                .define('I', CraftItemTagsProvider.INGOTS_REFINED_IRON).save();
        shaped(Ic2Items.ADVANCED_CIRCUIT, 1, "advanced_circuit").pattern("RGR").pattern("LCL").pattern("RGR")
                .define('R', Items.REDSTONE).define('G', Items.GLOWSTONE_DUST).define('L', Items.LAPIS_LAZULI)
                .define('C', Ic2Items.ELECTRONIC_CIRCUIT).save();
        shaped(Ic2Items.ADVANCED_CIRCUIT, 1, "advanced_circuit_vertical").pattern("RLR").pattern("GCG").pattern("RLR")
                .define('R', Items.REDSTONE).define('G', Items.GLOWSTONE_DUST).define('L', Items.LAPIS_LAZULI)
                .define('C', Ic2Items.ELECTRONIC_CIRCUIT).save();
        shaped(Ic2Items.COAL_BALL, 1, "coal_ball").pattern("CCC").pattern("CFC").pattern("CCC")
                .define('C', CraftItemTagsProvider.DUSTS_COAL).define('F', Items.FLINT).save();
        shaped(Ic2Items.COAL_CHUNK, 1, "coal_chunk").pattern("###").pattern("#O#").pattern("###")
                .define('#', Ic2Items.COMPRESSED_COAL_BALL).define('O', Items.OBSIDIAN).save();
        shaped(Ic2Items.COAL_CHUNK, 1, "coal_chunk_from_iron_block").pattern("###").pattern("#O#").pattern("###")
                .define('#', Ic2Items.COMPRESSED_COAL_BALL).define('O', Items.IRON_BLOCK).save();
        shaped(Ic2Items.COAL_CHUNK, 1, "coal_chunk_from_bricks").pattern("###").pattern("#O#").pattern("###")
                .define('#', Ic2Items.COMPRESSED_COAL_BALL).define('O', Items.BRICKS).save();
        shaped(Ic2Items.SMALL_IRON_DUST, 1, "small_iron_dust").pattern("CTC").pattern("TCT").pattern("CTC")
                .define('C', CraftItemTagsProvider.DUSTS_COPPER).define('T', CraftItemTagsProvider.DUSTS_TIN).save();
        shaped(Ic2Items.SMALL_IRON_DUST, 1, "small_iron_dust_inverted").pattern("TCT").pattern("CTC").pattern("TCT")
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
        shaped(Ic2Items.HYDRATED_COAL_DUST, 8, "hydrated_coal_dust").pattern("CCC").pattern("CWC").pattern("CCC")
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
        shaped(Ic2Items.SCRAP_BOX, 1, "scrap_box").pattern("SSS").pattern("SSS").pattern("SSS").define('S', Ic2Items.SCRAP).save();
    }

    private void plantBall(String name, Ingredient ingredient, int count) {
        shaped(Ic2Items.PLANT_BALL, count, "plant_ball_from_" + name).pattern("PPP").pattern("P P").pattern("PPP")
                .define('P', ingredient).save();
    }

    // --- §2 cables ---

    private void cables() {
        shaped(Ic2Items.cable(CableType.INSULATED_COPPER), 6, "insulated_copper_cable").pattern("RRR").pattern("CCC").pattern("RRR")
                .define('R', Ic2Items.RUBBER).define('C', ingotsCopper()).save();
        shaped(Ic2Items.cable(CableType.INSULATED_COPPER), 6, "insulated_copper_cable_vertical")
                .pattern("RCR").pattern("RCR").pattern("RCR").define('R', Ic2Items.RUBBER).define('C', ingotsCopper()).save();
        shaped(Ic2Items.cable(CableType.COPPER), 6, "copper_cable").pattern("CCC").define('C', ingotsCopper()).save();
        shaped(Ic2Items.cable(CableType.GOLD), 12, "gold_cable").pattern("GGG").define('G', Items.GOLD_INGOT).save();
        shaped(Ic2Items.cable(CableType.INSULATED_GOLD), 4, "insulated_gold_cable").pattern(" R ").pattern("RGR").pattern(" R ")
                .define('R', Ic2Items.RUBBER).define('G', Items.GOLD_INGOT).save();
        shaped(Ic2Items.cable(CableType.DOUBLE_INSULATED_GOLD), 4, "double_insulated_gold_cable")
                .pattern("RRR").pattern("RGR").pattern("RRR").define('R', Ic2Items.RUBBER).define('G', Items.GOLD_INGOT).save();
        shaped(Ic2Items.cable(CableType.HV), 12, "hv_cable").pattern("III")
                .define('I', CraftItemTagsProvider.INGOTS_REFINED_IRON).save();
        shaped(Ic2Items.cable(CableType.INSULATED_HV), 4, "insulated_hv_cable").pattern(" R ").pattern("RIR").pattern(" R ")
                .define('R', Ic2Items.RUBBER).define('I', CraftItemTagsProvider.INGOTS_REFINED_IRON).save();
        shaped(Ic2Items.cable(CableType.TIN), 9, "tin_cable").pattern("TTT")
                .define('T', CraftItemTagsProvider.INGOTS_TIN).save();
        shaped(Ic2Items.cable(CableType.GLASS_FIBRE), 4, "glass_fibre_cable").pattern("GGG").pattern("RDR").pattern("GGG")
                .define('G', Items.GLASS).define('R', Items.REDSTONE).define('D', diamond()).save();
        shaped(Ic2Items.cable(CableType.DETECTOR), 1, "detector_cable").pattern(" C ").pattern("RIR").pattern(" R ")
                .define('C', Ic2Items.ELECTRONIC_CIRCUIT).define('R', Items.REDSTONE)
                .define('I', Ic2Items.cable(CableType.QUADRUPLE_INSULATED_HV)).save();
        shaped(Ic2Items.cable(CableType.SPLITTER), 1, "splitter_cable").pattern(" R ").pattern("ILI").pattern(" R ")
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
        shaped(machine(Ic2Machine.IRON_FURNACE), 1, "iron_furnace").pattern("III").pattern("I I").pattern("III")
                .define('I', Items.IRON_INGOT).save();
        shaped(machine(Ic2Machine.IRON_FURNACE), 1, "iron_furnace_from_furnace").pattern(" I ").pattern("I I").pattern("IFI")
                .define('I', Items.IRON_INGOT).define('F', Items.FURNACE).save();
        shaped(machine(Ic2Machine.ELECTRIC_FURNACE), 1, "electric_furnace").pattern(" C ", "RFR")
                .define('C', Ic2Items.ELECTRONIC_CIRCUIT).define('R', Items.REDSTONE)
                .define('F', machine(Ic2Machine.IRON_FURNACE)).save();
        // GregTech: diamonds and an advanced circuit instead of flint/cobblestone and an electronic circuit.
        shaped(machine(Ic2Machine.MACERATOR), 1, "macerator").pattern("FDF").pattern("DMD").pattern("FAF")
                .define('F', Items.FLINT).define('D', diamond()).define('M', Ic2Items.MACHINE_CASING)
                .define('A', Ic2Items.ADVANCED_CIRCUIT).save();
        shaped(machine(Ic2Machine.EXTRACTOR), 1, "extractor").pattern("TMT", "TCT")
                .define('T', Ic2Items.TREETAP).define('M', Ic2Items.MACHINE_CASING)
                .define('C', Ic2Items.ELECTRONIC_CIRCUIT).save();
        shaped(machine(Ic2Machine.COMPRESSOR), 1, "compressor").pattern("S S").pattern("SMS").pattern("SCS")
                .define('S', Items.STONE).define('M', Ic2Items.MACHINE_CASING)
                .define('C', Ic2Items.ELECTRONIC_CIRCUIT).save();
        shaped(machine(Ic2Machine.RECYCLER), 1, "recycler").pattern(" G ").pattern("DMD").pattern("IDI")
                .define('G', Items.GLOWSTONE_DUST).define('D', Items.DIRT).define('M', machine(Ic2Machine.COMPRESSOR))
                .define('I', CraftItemTagsProvider.INGOTS_REFINED_IRON).save();
        shaped(machine(Ic2Machine.GENERATOR), 1, "generator").pattern(" B ").pattern("III").pattern(" F ")
                .define('B', Ic2Items.RE_BATTERY).define('I', CraftItemTagsProvider.INGOTS_REFINED_IRON)
                .define('F', machine(Ic2Machine.IRON_FURNACE)).save();
        shaped(machine(Ic2Machine.GENERATOR), 1, "generator_from_machine_casing").pattern("B", "M", "F")
                .define('B', Ic2Items.RE_BATTERY).define('M', Ic2Items.MACHINE_CASING).define('F', Items.FURNACE)
                .save();
        shaped(machine(Ic2Machine.GEOTHERMAL_GENERATOR), 1, "geothermal_generator").pattern("gCg").pattern("gCg").pattern("IGI")
                .define('g', Items.GLASS).define('C', Ic2Items.EMPTY_CELL)
                .define('I', CraftItemTagsProvider.INGOTS_REFINED_IRON).define('G', machine(Ic2Machine.GENERATOR))
                .save();
        shaped(machine(Ic2Machine.BATBOX), 1, "batbox").pattern("PCP").pattern("BBB").pattern("PPP").define('P', ItemTags.PLANKS)
                .define('C', Ic2Items.cable(CableType.INSULATED_COPPER)).define('B', Ic2Items.RE_BATTERY).save();
        shaped(machine(Ic2Machine.MFE), 1, "mfe").pattern("cCc").pattern("CMC").pattern("cCc")
                .define('c', Ic2Items.cable(CableType.DOUBLE_INSULATED_GOLD)).define('C', Ic2Items.ENERGY_CRYSTAL)
                .define('M', Ic2Items.MACHINE_CASING).save();
        // MFSU: needs the advanced machine block (advanced alloy from GT plates) and follows in phase 6b.
        shaped(machine(Ic2Machine.LV_TRANSFORMER), 1, "lv_transformer").pattern("PCP").pattern("ccc").pattern("PCP")
                .define('P', ItemTags.PLANKS).define('C', Ic2Items.cable(CableType.INSULATED_COPPER))
                .define('c', ingotsCopper()).save();
        shaped(machine(Ic2Machine.MV_TRANSFORMER), 1, "mv_transformer").pattern(" C ").pattern(" M ").pattern(" C ")
                .define('C', Ic2Items.cable(CableType.DOUBLE_INSULATED_GOLD)).define('M', Ic2Items.MACHINE_CASING)
                .save();
        shaped(machine(Ic2Machine.HV_TRANSFORMER), 1, "hv_transformer").pattern(" c ").pattern("CED").pattern(" c ")
                .define('c', Ic2Items.cable(CableType.QUADRUPLE_INSULATED_HV)).define('C', Ic2Items.ELECTRONIC_CIRCUIT)
                .define('E', machine(Ic2Machine.MV_TRANSFORMER)).define('D', Ic2Items.ENERGY_CRYSTAL).save();
    }

    // --- §5 batteries ---

    private void batteries() {
        shaped(Ic2Items.RE_BATTERY, 1, "re_battery").pattern(" C ").pattern("TRT").pattern("TRT")
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
        shaped(Ic2Items.ENERGY_CRYSTAL, 1, "energy_crystal").pattern("RRR").pattern("RDR").pattern("RRR")
                .define('R', Items.REDSTONE).define('D', diamond()).save();
        shaped(Ic2Items.LAPOTRON_CRYSTAL, 1, "lapotron_crystal").pattern("LCL").pattern("LDL").pattern("LCL")
                .define('L', Items.LAPIS_LAZULI).define('C', Ic2Items.ELECTRONIC_CIRCUIT)
                .define('D', Ic2Items.ENERGY_CRYSTAL).save();
    }

    // --- §7 tools ---

    /**
     * §11 armor and its parts. Quantum suit (iridium plate, reinforced glass) and CF pack (foam sprayer) follow with
     * their ingredients.
     */
    private void armor() {
        shaped(Ic2Items.CARBON_FIBER, 1, "carbon_fiber").pattern("CC").pattern("CC")
                .define('C', CraftItemTagsProvider.DUSTS_COAL).save();
        shapeless(Ic2Items.CARBON_MESH, 1, "carbon_mesh", Ingredient.of(Ic2Items.CARBON_FIBER),
                Ingredient.of(Ic2Items.CARBON_FIBER));
        shaped(Ic2Items.EMPTY_FUEL_CAN, 1, "empty_fuel_can").pattern(" TT").pattern("T T").pattern("TTT")
                .define('T', CraftItemTagsProvider.INGOTS_TIN).save();

        shaped(Ic2Items.NANO_HELMET, 1, "nano_helmet").pattern("CcC").pattern("CGC")
                .define('C', Ic2Items.CARBON_PLATE).define('c', Ic2Items.ENERGY_CRYSTAL).define('G', Items.GLASS).save();
        shaped(Ic2Items.NANO_CHESTPLATE, 1, "nano_chestplate").pattern("C C").pattern("CcC").pattern("CCC")
                .define('C', Ic2Items.CARBON_PLATE).define('c', Ic2Items.ENERGY_CRYSTAL).save();
        shaped(Ic2Items.NANO_LEGGINGS, 1, "nano_leggings").pattern("CcC").pattern("C C").pattern("C C")
                .define('C', Ic2Items.CARBON_PLATE).define('c', Ic2Items.ENERGY_CRYSTAL).save();
        shaped(Ic2Items.NANO_BOOTS, 1, "nano_boots").pattern("C C").pattern("CcC")
                .define('C', Ic2Items.CARBON_PLATE).define('c', Ic2Items.ENERGY_CRYSTAL).save();

        shaped(Ic2Items.HAZMAT_HELMET, 1, "hazmat_helmet").pattern(" O ").pattern("RGR").pattern("R#R")
                .define('R', Ic2Items.RUBBER).define('G', Items.GLASS).define('#', Items.IRON_BARS)
                .define('O', Items.ORANGE_DYE).save();
        shaped(Ic2Items.HAZMAT_CHESTPLATE, 1, "hazmat_chestplate").pattern("R R").pattern("ROR").pattern("ROR")
                .define('R', Ic2Items.RUBBER).define('O', Items.ORANGE_DYE).save();
        shaped(Ic2Items.HAZMAT_LEGGINGS, 1, "hazmat_leggings").pattern("ROR").pattern("R R").pattern("R R")
                .define('R', Ic2Items.RUBBER).define('O', Items.ORANGE_DYE).save();
        shaped(Ic2Items.RUBBER_BOOTS, 1, "rubber_boots").pattern("R R").pattern("R R").pattern("RWR")
                .define('R', Ic2Items.RUBBER).define('W', ItemTags.WOOL).save();

        shaped(Ic2Items.JETPACK, 1, "jetpack").pattern("ICI").pattern("IFI").pattern("R R")
                .define('I', CraftItemTagsProvider.INGOTS_REFINED_IRON).define('C', Ic2Items.ELECTRONIC_CIRCUIT)
                .define('F', Ic2Items.EMPTY_FUEL_CAN).define('R', Items.REDSTONE).damage(18001).save();
        shaped(Ic2Items.ELECTRIC_JETPACK, 1, "electric_jetpack").pattern("ICI").pattern("IBI").pattern("G G")
                .define('I', CraftItemTagsProvider.INGOTS_REFINED_IRON).define('C', Ic2Items.ADVANCED_CIRCUIT)
                .define('B', Ic2Items.machine(Ic2Machine.BATBOX)).define('G', Items.GLOWSTONE_DUST).save();
        // IC2 has one recipe each for a charged and an empty RE battery; the ingredient accepts both.
        shaped(Ic2Items.BATPACK, 1, "batpack").pattern("BCB").pattern("BTB").pattern("B B")
                .define('T', CraftItemTagsProvider.INGOTS_TIN).define('C', Ic2Items.ELECTRONIC_CIRCUIT)
                .define('B', Ic2Items.RE_BATTERY).save();
        shaped(Ic2Items.LAPPACK, 1, "lappack").pattern("LAL").pattern("LBL").pattern("L L")
                .define('L', Items.LAPIS_BLOCK).define('A', Ic2Items.ADVANCED_CIRCUIT).define('B', Ic2Items.BATPACK)
                .save();
        shaped(Ic2Items.SOLAR_HELMET, 1, "solar_helmet").pattern("III").pattern("ISI").pattern("CCC")
                .define('I', Items.IRON_INGOT).define('S', Ic2Items.machine(Ic2Machine.SOLAR_PANEL))
                .define('C', Ic2Items.cable(CableType.INSULATED_COPPER)).save();
        shaped(Ic2Items.SOLAR_HELMET, 1, "solar_helmet_from_iron_helmet").pattern(" H ").pattern(" S ")
                .pattern("CCC").define('H', Items.IRON_HELMET).define('S', Ic2Items.machine(Ic2Machine.SOLAR_PANEL))
                .define('C', Ic2Items.cable(CableType.INSULATED_COPPER)).save();
        shaped(Ic2Items.STATIC_BOOTS, 1, "static_boots").pattern("I I").pattern("ISI").pattern("CCC")
                .define('I', Items.IRON_INGOT).define('S', ItemTags.WOOL)
                .define('C', Ic2Items.cable(CableType.INSULATED_COPPER)).save();
        shaped(Ic2Items.STATIC_BOOTS, 1, "static_boots_from_iron_boots").pattern(" H ").pattern(" S ")
                .pattern("CCC").define('H', Items.IRON_BOOTS).define('S', ItemTags.WOOL)
                .define('C', Ic2Items.cable(CableType.INSULATED_COPPER)).save();
    }

    private void tools() {
        shaped(Ic2Items.TREETAP, 1, "treetap").pattern(" P ").pattern("PPP").pattern("P  ").define('P', ItemTags.PLANKS).save();
        shaped(Ic2Items.WRENCH, 1, "wrench").pattern("B B").pattern("BBB").pattern(" B ")
                .define('B', CraftItemTagsProvider.INGOTS_BRONZE).save();
        // Electric tools: all but the scanners need GT plates in the pack (phase 6b).
        shaped(Ic2Items.OD_SCANNER, 1, "od_scanner").pattern(" G ").pattern("CBC").pattern("ccc")
                .define('G', Items.GLOWSTONE_DUST).define('C', Ic2Items.ELECTRONIC_CIRCUIT).define('B', Ic2Items.RE_BATTERY)
                .define('c', Ic2Items.cable(CableType.INSULATED_COPPER)).save();
        // IC2 bug kept: the second recipe takes a (charged) RE battery instead of the OD scanner.
        shaped(Ic2Items.OV_SCANNER, 1, "ov_scanner").pattern(" G ").pattern("GCG").pattern("cSc")
                .define('G', Items.GLOWSTONE_DUST).define('C', Ic2Items.ADVANCED_CIRCUIT)
                .define('S', Ingredient.of(Ic2Items.OD_SCANNER, Ic2Items.RE_BATTERY))
                .define('c', Ic2Items.cable(CableType.DOUBLE_INSULATED_GOLD)).save();
        shaped(Items.DIAMOND_PICKAXE, 1, "diamond_pickaxe_from_industrial_diamond").pattern("DDD").pattern(" S ").pattern(" S ")
                .define('D', Ic2Items.INDUSTRIAL_DIAMOND).define('S', Items.STICK).save();
        shaped(Items.DIAMOND_HOE, 1, "diamond_hoe_from_industrial_diamond").pattern("DD ").pattern(" S ").pattern(" S ")
                .define('D', Ic2Items.INDUSTRIAL_DIAMOND).define('S', Items.STICK).save();
        shaped(Items.DIAMOND_SHOVEL, 1, "diamond_shovel_from_industrial_diamond").pattern("D", "S", "S")
                .define('D', Ic2Items.INDUSTRIAL_DIAMOND).define('S', Items.STICK).save();
        shaped(Items.DIAMOND_AXE, 1, "diamond_axe_from_industrial_diamond").pattern("DD ").pattern("DS ").pattern(" S ")
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
        shaped(Ic2Items.EMPTY_CELL, 4, "empty_cell").pattern(" T ").pattern("T T").pattern(" T ")
                .define('T', CraftItemTagsProvider.INGOTS_TIN).save();
        shapeless(Ic2Items.EMPTY_CELL, 1, "empty_cell_from_air_cell", Ingredient.of(Ic2Items.AIR_CELL));
        shapeless(Items.OBSIDIAN, 1, "obsidian_from_cells", Ingredient.of(Ic2Items.WATER_CELL),
                Ingredient.of(Ic2Items.WATER_CELL), Ingredient.of(Ic2Items.LAVA_CELL), Ingredient.of(Ic2Items.LAVA_CELL));
        shaped(Ic2Items.COOLANT_CELL_10K, 1, "coolant_cell_10k").pattern(" T ").pattern("TWT").pattern(" T ")
                .define('T', CraftItemTagsProvider.INGOTS_TIN).define('W', water()).save();
        shaped(Ic2Items.OVERCLOCKER_UPGRADE, 1, "overclocker_upgrade").pattern("CCC", "WEW")
                .define('C', Ic2Items.COOLANT_CELL_10K).define('W', Ic2Items.cable(CableType.INSULATED_COPPER))
                .define('E', Ic2Items.ELECTRONIC_CIRCUIT).save();
        shaped(Ic2Items.TRANSFORMER_UPGRADE, 1, "transformer_upgrade").pattern("GGG").pattern("WTW").pattern("GEG")
                .define('G', Items.GLASS).define('W', Ic2Items.cable(CableType.DOUBLE_INSULATED_GOLD))
                .define('T', machine(Ic2Machine.MV_TRANSFORMER)).define('E', Ic2Items.ELECTRONIC_CIRCUIT).save();
        shaped(Ic2Items.ENERGY_STORAGE_UPGRADE, 1, "energy_storage_upgrade").pattern("www").pattern("WBW").pattern("wEw")
                .define('w', ItemTags.PLANKS).define('W', Ic2Items.cable(CableType.INSULATED_COPPER))
                .define('B', Ic2Items.RE_BATTERY).define('E', Ic2Items.ELECTRONIC_CIRCUIT).save();
        // Hidden IC2 recipes.
        shaped(Items.GLOWSTONE_DUST, 1, "glowstone_dust_from_gold_dust").pattern("RGR").pattern("GRG").pattern("RGR")
                .define('R', Items.REDSTONE).define('G', CraftItemTagsProvider.DUSTS_GOLD).save();
        shaped(Items.GUNPOWDER, 3, "gunpowder_from_coal_dust").pattern("RCR").pattern("CRC").pattern("RCR")
                .define('R', Items.REDSTONE).define('C', CraftItemTagsProvider.DUSTS_COAL).save();
        shaped(Items.BUCKET, 1, "bucket_from_tin").pattern("T T", " T ")
                .define('T', CraftItemTagsProvider.INGOTS_TIN).save();
        shaped(Items.RAIL, 8, "rail_from_bronze").pattern("B B").pattern("BsB").pattern("B B")
                .define('B', CraftItemTagsProvider.INGOTS_BRONZE).define('s', Items.STICK).save();
        shapeless(Items.STICKY_PISTON, 1, "sticky_piston_from_resin", Ingredient.of(Items.PISTON),
                Ingredient.of(Ic2Items.RESIN));
        // GregTech replaces IC2's bronze piston with the vanilla pattern using bronze or refined iron.
        shaped(Items.PISTON, 1, "piston_from_bronze").pattern("WWW").pattern("CBC").pattern("CRC").define('W', ItemTags.PLANKS)
                .define('C', Items.COBBLESTONE).define('B', CraftItemTagsProvider.INGOTS_BRONZE)
                .define('R', Items.REDSTONE).save();
        shaped(Items.PISTON, 1, "piston_from_refined_iron").pattern("WWW").pattern("CBC").pattern("CRC")
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
        smelt("tin_ingots_from_empty_fuel_can", Ingredient.of(Ic2Items.EMPTY_FUEL_CAN), Ic2Items.TIN_INGOT, 7, 0.0F);
        // GregTech recycling of vanilla items. The 1.21 iron door recipe makes 3 doors from 6 ingots, so a door gives 2
        // instead of GregTech's 6 (see docs/abweichungen.md).
        smelt("gold_ingots_from_clock", Ingredient.of(Items.CLOCK), Items.GOLD_INGOT, 4, 0.0F);
        smelt("iron_ingots_from_compass", Ingredient.of(Items.COMPASS), Items.IRON_INGOT, 4, 0.0F);
        smelt("iron_ingots_from_shears", Ingredient.of(Items.SHEARS), Items.IRON_INGOT, 2, 0.0F);
        smelt("iron_ingots_from_minecart", Ingredient.of(Items.MINECART), Items.IRON_INGOT, 5, 0.0F);
        smelt("iron_ingots_from_bucket", Ingredient.of(Items.BUCKET), Items.IRON_INGOT, 3, 0.0F);
        smelt("iron_ingots_from_iron_door", Ingredient.of(Items.IRON_DOOR), Items.IRON_INGOT, 2, 0.0F);
        smelt("iron_ingots_from_anvil", Ingredient.of(Items.ANVIL), Items.IRON_INGOT, 31, 0.0F);
        smelt("iron_ingots_from_chipped_anvil", Ingredient.of(Items.CHIPPED_ANVIL), Items.IRON_INGOT, 20, 0.0F);
        smelt("iron_ingots_from_damaged_anvil", Ingredient.of(Items.DAMAGED_ANVIL), Items.IRON_INGOT, 10, 0.0F);
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
        machine(MachineRecipe.Kind.MACERATOR, "empty_fuel_can", Ingredient.of(Ic2Items.EMPTY_FUEL_CAN), 1,
                Ic2Items.TIN_DUST, 7);
        // GregTech recycling of vanilla items (only the main output; door adjusted to the 1.21 recipe, see above).
        machine(MachineRecipe.Kind.MACERATOR, "clock", Ingredient.of(Items.CLOCK), 1, Ic2Items.GOLD_DUST, 4);
        machine(MachineRecipe.Kind.MACERATOR, "compass", Ingredient.of(Items.COMPASS), 1, Ic2Items.IRON_DUST, 4);
        machine(MachineRecipe.Kind.MACERATOR, "shears", Ingredient.of(Items.SHEARS), 1, Ic2Items.IRON_DUST, 2);
        machine(MachineRecipe.Kind.MACERATOR, "minecart", Ingredient.of(Items.MINECART), 1, Ic2Items.IRON_DUST, 5);
        machine(MachineRecipe.Kind.MACERATOR, "bucket", Ingredient.of(Items.BUCKET), 1, Ic2Items.IRON_DUST, 3);
        machine(MachineRecipe.Kind.MACERATOR, "iron_door", Ingredient.of(Items.IRON_DOOR), 1, Ic2Items.IRON_DUST, 2);
        machine(MachineRecipe.Kind.MACERATOR, "stone_button", Ingredient.of(Items.STONE_BUTTON), 1, Items.SAND, 1);
        machine(MachineRecipe.Kind.MACERATOR, "stone_pressure_plate", Ingredient.of(Items.STONE_PRESSURE_PLATE), 1,
                Items.SAND, 2);
        machine(MachineRecipe.Kind.MACERATOR, "lever", Ingredient.of(Items.LEVER), 1, Items.SAND, 1);
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
        machine(MachineRecipe.Kind.COMPRESSOR, "carbon_mesh", Ingredient.of(Ic2Items.CARBON_MESH), 1,
                Ic2Items.CARBON_PLATE, 1);
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
        private int damage;

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

        /** Item damage of the result (IC2 metadata). */
        ShapedBuilder damage(int damage) {
            this.damage = damage;
            return this;
        }

        void save() {
            ItemStack stack = new ItemStack(result, count);
            if (damage > 0) {
                stack.setDamageValue(damage);
            }
            output.accept(id(name), new Ic2ShapedRecipe(ShapedRecipePattern.of(key, rows), stack), null);
        }
    }
}
