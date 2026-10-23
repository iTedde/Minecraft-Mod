package dev.craft.data;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import dev.craft.Craft;
import dev.craft.thermalexpansion.TeBlocks;
import dev.craft.thermalexpansion.TeItems;
import dev.craft.thermalexpansion.TeFluids;
import dev.craft.thermalexpansion.recipe.TeFluidRecipe;
import dev.craft.thermalexpansion.recipe.TeRecipe;
import dev.craft.thermalexpansion.recipe.TeRecipes;
import dev.craft.buildcraft.fluid.LiquidContainers;
import dev.craft.advancedmachines.AdvancedMachinesItems;
import dev.craft.buildcraft.BcItems;
import dev.craft.buildcraft.transport.PipeType;
import dev.craft.buildcraft.engine.EngineType;
import dev.craft.compactsolars.CsItems;
import dev.craft.compactsolars.SolarArrayType;
import dev.craft.forestry.FrFluids;
import dev.craft.forestry.FrItems;
import dev.craft.forestry.circuit.CircuitBoardType;
import dev.craft.forestry.circuit.ElectronTube;
import dev.craft.forestry.recipe.CarpenterRecipe;
import dev.craft.forestry.recipe.FabricatorRecipe;
import dev.craft.gregtech.GtItems;
import dev.craft.gregtech.GtMachine;
import dev.craft.gregtech.block.GtCasingBlock;
import dev.craft.gregtech.recipe.GtRecipe;
import dev.craft.gregtech.world.GtOre;
import dev.craft.ic2.Ic2DataComponents;
import dev.craft.ic2.Ic2Items;
import dev.craft.ic2.item.tool.PainterItem;
import dev.craft.ic2.Ic2Machine;
import dev.craft.ic2.cable.CableType;
import dev.craft.ic2.item.FilledFuelCanItem;
import dev.craft.ic2.item.FilledTinCanItem;
import dev.craft.ic2.machine.CanningMachineBlockEntity;
import dev.craft.ic2.recipe.Ic2ShapedRecipe;
import dev.craft.ic2.recipe.GradualRepairRecipe;
import dev.craft.ic2.item.armor.CfPackItem;
import dev.craft.ic2.item.tool.FoamSprayerItem;
import dev.craft.ic2.recipe.Ic2ShapelessRecipe;
import dev.craft.ic2.recipe.MachineRecipe;
import dev.craft.powerconverters.ConverterType;
import dev.craft.powerconverters.PcItems;
import dev.craft.powerplants.PpItems;
import dev.craft.enderstorage.EnderStorageRecolourRecipe;
import dev.craft.enderstorage.EsDataComponents;
import dev.craft.enderstorage.EsItems;
import dev.craft.enderstorage.Frequency;
import dev.craft.ironchest.ChestUpgradeType;
import dev.craft.ironchest.IronChestItems;
import dev.craft.ironchest.IronChestType;
import dev.craft.portalgun.PgItems;
import dev.craft.redpower.RpGem;
import dev.craft.redpower.RpItems;
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
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.crafting.CompoundIngredient;
import net.neoforged.neoforge.common.crafting.DifferenceIngredient;
import net.neoforged.neoforge.common.crafting.DataComponentIngredient;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

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
        reactor();
        reactorGregTech();
        gregTechFollowUps();
        gregTechOres();
        grinder();
        gregTechUnlocks();
        gregTechUnlockedRecipes();
        gregTechCircuits();
        gregTechTungsten();
        gregTechDusts();
        gregTechRecycling();
        redPowerTools();
        redPowerTable();
        longFallBoots();
        ironChests();
        enderStorage();
        advancedMachines();
        thermalExpansion();
        gregTechCircuitBoards();
        gregTechBrass();
        gregTechMortars();
        uuMatter();
        smelting();
        macerator();
        compressor();
        extractor();
        buildcraft();
        forestry();
        powerConverters();
        compactSolars();
        gregTech();
        powerPlants();
    }

    // --- GregTech (spec 13): plate bender, plates and what they unlock ---

    private void gregTech() {
        shaped(GtItems.machine(GtMachine.PLATE_BENDING_MACHINE), 1, "plate_bending_machine").pattern("PCP", "RQR", "PCP")
                .define('P', Items.PISTON).define('C', Ic2Items.ELECTRONIC_CIRCUIT)
                .define('R', machine(Ic2Machine.COMPRESSOR)).define('Q', GtItems.CONVEYOR_MODULE).save();
        // Refined iron ingots or plates (aluminium ingots/plates follow with aluminium).
        conveyorModule("conveyor_module", CraftItemTagsProvider.INGOTS_REFINED_IRON);
        conveyorModule("conveyor_module_from_plates", CraftItemTagsProvider.PLATES_REFINED_IRON);
        plate("iron", Tags.Items.INGOTS_IRON, GtItems.IRON_PLATE, Ic2Items.IRON_DUST, Items.IRON_INGOT);
        plate("gold", Tags.Items.INGOTS_GOLD, GtItems.GOLD_PLATE, Ic2Items.GOLD_DUST, Items.GOLD_INGOT);
        plate("refined_iron", CraftItemTagsProvider.INGOTS_REFINED_IRON, GtItems.REFINED_IRON_PLATE, null,
                Ic2Items.REFINED_IRON_INGOT);
        plate("tin", CraftItemTagsProvider.INGOTS_TIN, GtItems.TIN_PLATE, Ic2Items.TIN_DUST, Ic2Items.TIN_INGOT);
        plate("copper", ingotsCopper(), GtItems.COPPER_PLATE, Ic2Items.COPPER_DUST, Items.COPPER_INGOT);
        plate("bronze", CraftItemTagsProvider.INGOTS_BRONZE, GtItems.BRONZE_PLATE, Ic2Items.BRONZE_DUST,
                Ic2Items.BRONZE_INGOT);
        // GT replaces "8 copper ingots" with 8 copper plates; recycling of the dense plate is GT as well.
        machine(MachineRecipe.Kind.COMPRESSOR, "copper_plate", Ingredient.of(CraftItemTagsProvider.PLATES_COPPER), 8,
                Ic2Items.DENSE_COPPER_PLATE, 1);
        smelt("copper_ingot_from_dense_copper_plate", Ingredient.of(Ic2Items.DENSE_COPPER_PLATE), Items.COPPER_INGOT, 8,
                0.0F);
        machine(MachineRecipe.Kind.MACERATOR, "dense_copper_plate", Ingredient.of(Ic2Items.DENSE_COPPER_PLATE), 1,
                Ic2Items.COPPER_DUST, 8);
        // GT mixed metal ingot (IC2 recipe removed), vertical in any column; other metals follow with their plates.
        mixedMetal("refined_iron_bronze_tin", CraftItemTagsProvider.PLATES_REFINED_IRON, CraftItemTagsProvider.PLATES_TIN,
                1);
        machine(MachineRecipe.Kind.COMPRESSOR, "mixed_metal_ingot", Ingredient.of(Ic2Items.MIXED_METAL_INGOT), 1,
                Ic2Items.ADVANCED_ALLOY, 1);
        shaped(Ic2Items.ADVANCED_MACHINE_CASING, 1, "advanced_machine_casing").pattern(" A ", "CMC", " A ")
                .define('A', Ic2Items.ADVANCED_ALLOY).define('C', Ic2Items.CARBON_PLATE)
                .define('M', Ic2Items.MACHINE_CASING).save();
        shaped(Ic2Items.ADVANCED_MACHINE_CASING, 1, "advanced_machine_casing_rotated").pattern(" C ", "AMA", " C ")
                .define('A', Ic2Items.ADVANCED_ALLOY).define('C', Ic2Items.CARBON_PLATE)
                .define('M', Ic2Items.MACHINE_CASING).save();
        electrolyzer();
        blastFurnace();
        assembler();
        canner();
        implosionCompressor();
        centrifuge();
    }

    /**
     * Implosion compressor (spec 13 §8): the machine, the iridium chain up to the iridium plate and the gem dust
     * recipes whose inputs exist.
     */
    private void implosionCompressor() {
        shaped(GtItems.machine(GtMachine.IMPLOSION_COMPRESSOR), 1, "implosion_compressor")
                .pattern("ABA", "DCD", "ABA").define('A', Ic2Items.ADVANCED_ALLOY)
                .define('B', CraftItemTagsProvider.RAW_MACHINE_TIER_02).define('D', Ic2Items.ELECTRONIC_CIRCUIT)
                .define('C', machine(Ic2Machine.COMPRESSOR)).save();
        machine(MachineRecipe.Kind.COMPRESSOR, "iridium_ore", Ingredient.of(Ic2Items.IRIDIUM_ORE), 1,
                GtItems.IRIDIUM_INGOT, 1);
        // Rolling machine recipe in the pack (Railcraft is out of scope): crafting table, GT's own fallback.
        shaped(GtItems.IRIDIUM_ALLOY_INGOT, 1, "iridium_alloy_ingot").pattern("IAI", "ADA", "IAI")
                .define('I', GtItems.IRIDIUM_INGOT).define('A', Ic2Items.ADVANCED_ALLOY)
                .define('D', GtItems.DIAMOND_DUST).save();
        machine(MachineRecipe.Kind.MACERATOR, "diamond", diamond(), 1, GtItems.DIAMOND_DUST, 1);
        machine(MachineRecipe.Kind.MACERATOR, "emerald", Ingredient.of(Items.EMERALD), 1, GtItems.EMERALD_DUST, 1);
        // GT_Mod: 2 iron fences → 3 small iron dust (GT small dust 241 = the IC2 item, see centrifuge).
        machine(MachineRecipe.Kind.MACERATOR, "iron_fence", Ingredient.of(Ic2Items.IRON_FENCE), 2,
                Ic2Items.SMALL_IRON_DUST, 3);

        implosion("iridium_plate", GtItems.IRIDIUM_ALLOY_INGOT, 1, 8, new ItemStack(Ic2Items.IRIDIUM_PLATE.get()),
                new ItemStack(GtItems.DARK_ASH.get(), 4));
        implosion("industrial_diamond", GtItems.DIAMOND_DUST, 4, 32,
                new ItemStack(Ic2Items.INDUSTRIAL_DIAMOND.get(), 3), new ItemStack(GtItems.DARK_ASH.get(), 16));
        implosion("emerald", GtItems.EMERALD_DUST, 4, 24, new ItemStack(Items.EMERALD, 3),
                new ItemStack(GtItems.DARK_ASH.get(), 12));
    }

    /**
     * Industrial centrifuge (spec 13 §9): crafting recipes, the reachable centrifuge recipes (groups A and B1–B4 in
     * STATUS.md) and the small dust crafting ({@code dustSmall*} ↔ {@code dust*}) for the new small dusts.
     */
    private void centrifuge() {
        for (String variant : new String[]{"refined_iron", "aluminium"}) {
            shaped(GtItems.machine(GtMachine.INDUSTRIAL_CENTRIFUGE), 1, "industrial_centrifuge_from_" + variant)
                    .pattern("RCR", "AEA", "RCR").define('R', variant.equals("aluminium")
                            ? CraftItemTagsProvider.PLATES_ALUMINIUM : CraftItemTagsProvider.PLATES_REFINED_IRON)
                    .define('C', Ic2Items.ADVANCED_CIRCUIT).define('A', CraftItemTagsProvider.RAW_MACHINE_TIER_02)
                    .define('E', machine(Ic2Machine.EXTRACTOR)).save();
        }
        ItemStack none = ItemStack.EMPTY;
        // GT_Mod: the ice cell recipe is in gregTechTungsten(); uranium/plutonium/thorium recipes are in reactorGregTech().
        centrifuge("magma_cream", Ingredient.of(Items.MAGMA_CREAM), 1, 0, 500, stack(Items.BLAZE_POWDER, 1),
                stack(Items.SLIME_BALL, 1));
        centrifuge("dirt", Ingredient.of(Items.DIRT), 16, 0, 2500, stack(Ic2Items.COMPRESSED_PLANT_BALL, 1),
                stack(Ic2Items.PLANT_BALL, 1), stack(Items.CLAY_BALL, 1), stack(Items.SAND, 8));
        centrifuge("grass_block", Ingredient.of(Items.GRASS_BLOCK), 16, 0, 2500,
                stack(Ic2Items.COMPRESSED_PLANT_BALL, 2), stack(Ic2Items.PLANT_BALL, 2), stack(Items.CLAY_BALL, 1),
                stack(Items.SAND, 8));
        centrifuge("mycelium", Ingredient.of(Items.MYCELIUM), 8, 0, 1650, stack(Items.BROWN_MUSHROOM, 2),
                stack(Items.RED_MUSHROOM, 2), stack(Items.CLAY_BALL, 1), stack(Items.SAND, 4));
        centrifuge("enchanted_golden_apple", Ingredient.of(Items.ENCHANTED_GOLDEN_APPLE), 1, 2, 10000,
                stack(GtItems.METHANE_CELL, 2), stack(Items.GOLD_INGOT, 64));
        centrifuge("golden_apple", Ingredient.of(Items.GOLDEN_APPLE), 1, 1, 10000, stack(GtItems.METHANE_CELL, 1),
                stack(Items.GOLD_NUGGET, 6));
        centrifuge("golden_carrot", Ingredient.of(Items.GOLDEN_CARROT), 1, 1, 10000, stack(GtItems.METHANE_CELL, 1),
                stack(Items.GOLD_NUGGET, 6));
        centrifuge("glistering_melon_slice", Ingredient.of(Items.GLISTERING_MELON_SLICE), 8, 1, 10000,
                stack(GtItems.METHANE_CELL, 1), stack(Items.GOLD_NUGGET, 6));
        // Methane from food: 1.4.7 had one fish (cod and salmon here) and huge mushroom blocks with the stem as meta.
        methane("apple", Ingredient.of(Items.APPLE), 32);
        methane("mushroom_stew", Ingredient.of(Items.MUSHROOM_STEW), 16);
        methane("bread", Ingredient.of(Items.BREAD), 64);
        methane("porkchop", Ingredient.of(Items.PORKCHOP), 12);
        methane("cooked_porkchop", Ingredient.of(Items.COOKED_PORKCHOP), 16);
        methane("beef", Ingredient.of(Items.BEEF), 12);
        methane("cooked_beef", Ingredient.of(Items.COOKED_BEEF), 16);
        methane("fish", Ingredient.of(Items.COD, Items.SALMON), 12);
        methane("cooked_fish", Ingredient.of(Items.COOKED_COD, Items.COOKED_SALMON), 16);
        methane("chicken", Ingredient.of(Items.CHICKEN), 12);
        methane("cooked_chicken", Ingredient.of(Items.COOKED_CHICKEN), 16);
        methane("melon_slice", Ingredient.of(Items.MELON_SLICE), 64);
        methane("pumpkin", Ingredient.of(Items.PUMPKIN, Items.CARVED_PUMPKIN), 16);
        methane("rotten_flesh", Ingredient.of(Items.ROTTEN_FLESH), 16);
        methane("spider_eye", Ingredient.of(Items.SPIDER_EYE), 32);
        methane("carrot", Ingredient.of(Items.CARROT), 16);
        methane("potato", Ingredient.of(Items.POTATO), 16);
        methane("poisonous_potato", Ingredient.of(Items.POISONOUS_POTATO), 12);
        methane("baked_potato", Ingredient.of(Items.BAKED_POTATO), 24);
        methane("cookie", Ingredient.of(Items.COOKIE), 64);
        methane("cake", Ingredient.of(Items.CAKE), 8);
        methane("brown_mushroom_block", Ingredient.of(Items.BROWN_MUSHROOM_BLOCK, Items.MUSHROOM_STEM), 12);
        methane("red_mushroom_block", Ingredient.of(Items.RED_MUSHROOM_BLOCK), 12);
        methane("brown_mushroom", Ingredient.of(Items.BROWN_MUSHROOM), 32);
        methane("red_mushroom", Ingredient.of(Items.RED_MUSHROOM), 32);
        methane("nether_wart", Ingredient.of(Items.NETHER_WART), 32);
        methane("terra_wart", Ingredient.of(Ic2Items.TERRA_WART), 16);
        centrifuge("resin", Ingredient.of(Ic2Items.RESIN), 4, 0, 1300, stack(Ic2Items.RUBBER, 14),
                stack(Ic2Items.COMPRESSED_PLANT_BALL, 1), stack(Ic2Items.PLANT_BALL, 1));
        centrifuge("soul_sand", Ingredient.of(Items.SOUL_SAND), 16, 1, 2500, stack(GtItems.OIL_CELL, 1),
                stack(GtItems.SALTPETER_DUST, 4), stack(Ic2Items.COAL_DUST, 1), stack(Items.SAND, 10));
        // Tin cells per ingot: 8 (GregTech.cfg) → 2 + 64 / 8 tin ingots.
        centrifuge("lava_cell", Ingredient.of(Ic2Items.LAVA_CELL), 16, 0, 15000, stack(GtItems.ELECTRUM_INGOT, 1),
                stack(Items.COPPER_INGOT, 4), stack(GtItems.SMALL_TUNGSTEN_DUST, 1), stack(Ic2Items.TIN_INGOT, 10));
        centrifuge("lava", Ingredient.of(GtItems.LAVA), 16, 0, 10000, stack(GtItems.ELECTRUM_INGOT, 1),
                stack(Items.COPPER_INGOT, 4), stack(GtItems.SMALL_TUNGSTEN_DUST, 1), stack(Ic2Items.TIN_INGOT, 2));
        // GT_OreDictHandler: dusts and molecule cells.
        centrifuge("lapis_lazuli", Ingredient.of(Items.LAPIS_LAZULI), 4, 0, 1500, stack(GtItems.LAZURITE_DUST, 3),
                stack(GtItems.SMALL_PYRITE_DUST, 1), stack(GtItems.SMALL_CALCITE_DUST, 1),
                stack(GtItems.SMALL_SODALITE_DUST, 2));
        centrifuge("redstone", Ingredient.of(Items.REDSTONE), 10, 4, 7000, stack(GtItems.SILICON_CELL, 1),
                stack(GtItems.PYRITE_DUST, 5), stack(GtItems.RUBY_DUST, 1), stack(GtItems.MERCURY_CELL, 3));
        centrifuge("glowstone_dust", Ingredient.of(Items.GLOWSTONE_DUST), 16, 1, 25000, stack(Items.REDSTONE, 8),
                stack(Ic2Items.GOLD_DUST, 8), stack(GtItems.HELIUM_CELL, 1));
        // ForestryBronzeNerf = true: a single bronze dust.
        centrifuge("bronze_dust", Ingredient.of(CraftItemTagsProvider.DUSTS_BRONZE), 1, 0, 1500,
                stack(GtItems.SMALL_COPPER_DUST, 6), stack(GtItems.SMALL_TIN_DUST, 2));
        centrifuge("copper_dust", Ingredient.of(CraftItemTagsProvider.DUSTS_COPPER), 3, 0, 2400,
                stack(GtItems.SMALL_GOLD_DUST, 1), stack(GtItems.SMALL_NICKEL_DUST, 1));
        centrifuge("gold_dust", Ingredient.of(CraftItemTagsProvider.DUSTS_GOLD), 3, 0, 2400,
                stack(GtItems.SMALL_COPPER_DUST, 1), stack(GtItems.SMALL_NICKEL_DUST, 1));
        centrifuge("nickel_dust", Ingredient.of(CraftItemTagsProvider.DUSTS_NICKEL), 3, 0, 3450,
                stack(Ic2Items.SMALL_IRON_DUST, 1), stack(GtItems.SMALL_GOLD_DUST, 1),
                stack(GtItems.SMALL_COPPER_DUST, 1));
        centrifuge("iron_dust", Ingredient.of(CraftItemTagsProvider.DUSTS_IRON), 2, 0, 1500,
                stack(GtItems.SMALL_TIN_DUST, 1), stack(GtItems.SMALL_NICKEL_DUST, 1));
        centrifuge("tin_dust", Ingredient.of(CraftItemTagsProvider.DUSTS_TIN), 2, 0, 2100,
                stack(GtItems.SMALL_ZINC_DUST, 1), stack(Ic2Items.SMALL_IRON_DUST, 1));
        centrifuge("silver_dust", Ingredient.of(CraftItemTagsProvider.DUSTS_SILVER), 2, 0, 2400,
                stack(GtItems.SMALL_LEAD_DUST, 1));
        // TE slag does not exist → the second output is ash as well.
        centrifuge("dark_ash", Ingredient.of(GtItems.DARK_ASH), 2, 0, 250, stack(GtItems.ASH, 1),
                stack(GtItems.ASH, 1));
        centrifuge("hydrogen_cell", Ingredient.of(GtItems.HYDROGEN_CELL), 4, 0, 3000,
                stack(GtItems.DEUTERIUM_CELL, 1), none, none, stack(Ic2Items.EMPTY_CELL, 3));
        centrifuge("deuterium_cell", Ingredient.of(GtItems.DEUTERIUM_CELL), 4, 0, 3000,
                stack(GtItems.TRITIUM_CELL, 1), none, none, stack(Ic2Items.EMPTY_CELL, 3));
        centrifuge("helium_cell", Ingredient.of(GtItems.HELIUM_CELL), 16, 0, 10000,
                stack(GtItems.HELIUM_3_CELL, 1), none, none, stack(Ic2Items.EMPTY_CELL, 15));
        centrifuge("rubber_wood", Ingredient.of(Ic2Items.RUBBER_WOOD), 16, 5, 5000, stack(Ic2Items.RESIN, 8),
                stack(Ic2Items.PLANT_BALL, 6), stack(GtItems.METHANE_CELL, 1), stack(GtItems.CARBON_CELL, 4));

        smallDust("pyrite", GtItems.SMALL_PYRITE_DUST, Ingredient.of(GtItems.PYRITE_DUST), GtItems.PYRITE_DUST);
        smallDust("nickel", GtItems.SMALL_NICKEL_DUST, Ingredient.of(CraftItemTagsProvider.DUSTS_NICKEL),
                GtItems.NICKEL_DUST);
        smallDust("gold", GtItems.SMALL_GOLD_DUST, Ingredient.of(CraftItemTagsProvider.DUSTS_GOLD), Ic2Items.GOLD_DUST);
        smallDust("copper", GtItems.SMALL_COPPER_DUST, Ingredient.of(CraftItemTagsProvider.DUSTS_COPPER),
                Ic2Items.COPPER_DUST);
        smallDust("tin", GtItems.SMALL_TIN_DUST, Ingredient.of(CraftItemTagsProvider.DUSTS_TIN), Ic2Items.TIN_DUST);
    }

    /** Food etc. + 1 empty cell → 1 methane cell, 5000 ticks. */
    private void methane(String name, Ingredient input, int amount) {
        centrifuge(name, input, amount, 1, 5000, stack(GtItems.METHANE_CELL, 1));
    }

    /** {@code GT_Mod.addCentrifugeRecipe}: always 5 EU/t; input 2 is {@code cells} empty cells (none if 0). */
    private void centrifuge(String name, Ingredient input, int amount, int cells, int duration, ItemStack... outputs) {
        List<SizedIngredient> inputs = cells > 0
                ? List.of(new SizedIngredient(input, amount), new SizedIngredient(Ingredient.of(Ic2Items.EMPTY_CELL), cells))
                : List.of(new SizedIngredient(input, amount));
        output.accept(id("gt_centrifuge/" + name), new GtRecipe(GtRecipe.Kind.CENTRIFUGE, inputs, List.of(outputs),
                duration, 5, 0), null);
    }

    /** GT {@code dustSmall*}: four small dusts → one dust, one dust → four small dusts. */
    private void smallDust(String name, ItemLike small, Ingredient dusts, ItemLike dust) {
        shapeless(dust, 1, name + "_dust_from_small", Ingredient.of(small), Ingredient.of(small),
                Ingredient.of(small), Ingredient.of(small));
        shapeless(small, 4, "small_" + name + "_dust", dusts);
    }

    private static ItemStack stack(ItemLike item, int count) {
        return new ItemStack(item, count);
    }

    /** {@code GT_Mod.addImplosionRecipe}: input 2 is always industrial TNT; 20 ticks at 32 EU/t. */
    private void implosion(String name, ItemLike input, int count, int tnt, ItemStack... outputs) {
        output.accept(id("gt_implosion/" + name), new GtRecipe(GtRecipe.Kind.IMPLOSION,
                List.of(SizedIngredient.of(input, count), SizedIngredient.of(Ic2Items.INDUSTRIAL_TNT.get(), tnt)),
                List.of(outputs), 20, 32, 0), null);
    }

    /** Automatic canning machine (spec 13 §7): crafting recipe and the canner recipes whose items exist. */
    private void canner() {
        shapeless(GtItems.machine(GtMachine.AUTOMATIC_CANNING_MACHINE), 1, "automatic_canning_machine",
                Ingredient.of(GtItems.CONVEYOR_MODULE), Ingredient.of(machine(Ic2Machine.CANNING_MACHINE)));
        // GT_Mod: the specific food recipes come first, so the generic loop below skips their inputs.
        can("rotten_flesh", SizedIngredient.of(Items.ROTTEN_FLESH, 1), SizedIngredient.of(Ic2Items.TIN_CAN, 2), 200,
                FilledTinCanItem.create(1, 2));
        can("spider_eye", SizedIngredient.of(Items.SPIDER_EYE, 1), SizedIngredient.of(Ic2Items.TIN_CAN, 1), 100,
                FilledTinCanItem.create(1, 1));
        can("poisonous_potato", SizedIngredient.of(Items.POISONOUS_POTATO, 1), SizedIngredient.of(Ic2Items.TIN_CAN, 1),
                100, FilledTinCanItem.create(1, 1));
        can("cake", SizedIngredient.of(Items.CAKE, 1), SizedIngredient.of(Ic2Items.TIN_CAN, 6), 600,
                FilledTinCanItem.create(0, 6));
        can("mushroom_stew", SizedIngredient.of(Items.MUSHROOM_STEW, 1), SizedIngredient.of(Ic2Items.TIN_CAN, 3), 300,
                FilledTinCanItem.create(0, 3), new ItemStack(Items.BOWL));
        Set<Item> specific = Set.of(Items.ROTTEN_FLESH, Items.SPIDER_EYE, Items.POISONOUS_POTATO, Items.MUSHROOM_STEW,
                Ic2Items.FILLED_TIN_CAN.get());
        BuiltInRegistries.ITEM.entrySet().stream().sorted(Comparator.comparing(entry -> entry.getKey().location()))
                .forEach(entry -> {
                    Item item = entry.getValue();
                    FoodProperties food = item.components().get(DataComponents.FOOD);
                    int cans = food == null ? 0 : (int) Math.ceil(food.nutrition() / 2.0);
                    if (cans <= 0 || specific.contains(item)) {
                        return;
                    }
                    ResourceLocation id = entry.getKey().location();
                    String name = (id.getNamespace().equals("minecraft") ? "" : id.getNamespace() + "_") + id.getPath();
                    ItemStack remainder = CanningMachineBlockEntity.getFoodRemainder(new ItemStack(item));
                    can(name, SizedIngredient.of(item, 1), SizedIngredient.of(Ic2Items.TIN_CAN, cans), 100 * cans,
                            FilledTinCanItem.create(0, cans), remainder);
                });
        can("bio_cell", SizedIngredient.of(Ic2Items.COMPRESSED_PLANT_BALL, 1), SizedIngredient.of(Ic2Items.EMPTY_CELL, 1),
                100, new ItemStack(Ic2Items.BIO_CELL.get()));
        can("hydrated_coal_cell", SizedIngredient.of(Ic2Items.HYDRATED_COAL_CLUMP, 1),
                SizedIngredient.of(Ic2Items.EMPTY_CELL, 1), 100, new ItemStack(Ic2Items.HYDRATED_COAL_CELL.get()));
        can("lava_cell", SizedIngredient.of(Items.LAVA_BUCKET, 1), SizedIngredient.of(Ic2Items.EMPTY_CELL, 1), 100,
                new ItemStack(Ic2Items.LAVA_CELL.get()), new ItemStack(Items.BUCKET));
        can("water_cell", SizedIngredient.of(Items.WATER_BUCKET, 1), SizedIngredient.of(Ic2Items.EMPTY_CELL, 1), 100,
                new ItemStack(Ic2Items.WATER_CELL.get()), new ItemStack(Items.BUCKET));
        can("biofuel_can", SizedIngredient.of(Ic2Items.BIOFUEL_CELL, 6), SizedIngredient.of(Ic2Items.EMPTY_FUEL_CAN, 1),
                600, FilledFuelCanItem.create(6 * CanningMachineBlockEntity.BIOFUEL_VALUE),
                new ItemStack(Ic2Items.EMPTY_CELL.get(), 6));
        can("coalfuel_can", SizedIngredient.of(Ic2Items.COALFUEL_CELL, 6), SizedIngredient.of(Ic2Items.EMPTY_FUEL_CAN, 1),
                600, FilledFuelCanItem.create(6 * CanningMachineBlockEntity.COALFUEL_VALUE),
                new ItemStack(Ic2Items.EMPTY_CELL.get(), 6));
    }

    /** {@code GT_Mod.addCannerRecipe}: two inputs, one or two outputs, 1 EU/t unless given. */
    private void can(String name, SizedIngredient input1, SizedIngredient input2, int duration, ItemStack... outputs) {
        can(name, input1, input2, duration, 1, outputs);
    }

    private void can(String name, SizedIngredient input1, SizedIngredient input2, int duration, int euPerTick,
                     ItemStack... outputs) {
        List<ItemStack> results = new ArrayList<>();
        for (ItemStack stack : outputs) {
            if (!stack.isEmpty()) {
                results.add(stack);
            }
        }
        output.accept(id("gt_canner/" + name), new GtRecipe(GtRecipe.Kind.CANNER, List.of(input1, input2), results,
                duration, euPerTick, 0), null);
    }

    /**
     * Industrial blast furnace (spec 13 §5): casings, coils, the furnace itself, its recipes and the materials it
     * unlocks (nickel, aluminium, steel, silicon plate).
     */
    private void blastFurnace() {
        shaped(GtItems.machine(GtMachine.INDUSTRIAL_BLAST_FURNACE), 1, "industrial_blast_furnace")
                .pattern("ACA", "CMC", "FCF").define('M', Ic2Items.ADVANCED_MACHINE_CASING)
                .define('A', Ic2Items.ELECTRONIC_CIRCUIT).define('C', GtItems.CUPRONICKEL_HEATING_COIL)
                .define('F', machine(Ic2Machine.INDUCTION_FURNACE)).save();
        standardCasing("standard_machine_casing", CraftItemTagsProvider.PLATES_REFINED_IRON);
        standardCasing("standard_machine_casing_from_aluminium", CraftItemTagsProvider.PLATES_ALUMINIUM);
        shaped(GtItems.casing(GtCasingBlock.Type.REINFORCED), 4, "reinforced_machine_casing")
                .pattern("SSS", "CMC", "SSS").define('S', CraftItemTagsProvider.PLATES_STEEL)
                .define('C', Ic2Items.ADVANCED_CIRCUIT).define('M', CraftItemTagsProvider.RAW_MACHINE_TIER_02).save();
        // Rolling machine recipes in the pack (Railcraft is out of scope): crafting table, GT's own fallback.
        shaped(GtItems.CUPRONICKEL_HEATING_COIL, 3, "cupronickel_heating_coil").pattern("BAB", "A A", "BAB")
                .define('A', ingotsCopper()).define('B', CraftItemTagsProvider.INGOTS_NICKEL).save();

        blast("steel_ingot", new SizedIngredient(Ingredient.of(CraftItemTagsProvider.INGOTS_REFINED_IRON), 1),
                new SizedIngredient(Ingredient.of(CraftItemTagsProvider.DUSTS_COAL), 2), 500, 1000,
                new ItemStack(GtItems.STEEL_INGOT.get()), new ItemStack(GtItems.DARK_ASH.get(), 2));
        blast("steel_ingot_from_dust", new SizedIngredient(Ingredient.of(GtItems.STEEL_DUST), 1), null, 100, 1000,
                new ItemStack(GtItems.STEEL_INGOT.get()));
        blast("aluminium_ingot", new SizedIngredient(Ingredient.of(GtItems.ALUMINIUM_DUST), 1), null, 200, 1700,
                new ItemStack(GtItems.ALUMINIUM_INGOT.get()));
        blast("silicon_plate", new SizedIngredient(Ingredient.of(GtItems.SILICON_CELL), 2), null, 1000, 1500,
                new ItemStack(GtItems.SILICON_PLATE.get()), new ItemStack(Ic2Items.EMPTY_CELL.get(), 2));

        plate("nickel", CraftItemTagsProvider.INGOTS_NICKEL, GtItems.NICKEL_PLATE, GtItems.NICKEL_DUST,
                GtItems.NICKEL_INGOT);
        plate("aluminium", CraftItemTagsProvider.INGOTS_ALUMINIUM, GtItems.ALUMINIUM_PLATE, GtItems.ALUMINIUM_DUST,
                GtItems.ALUMINIUM_INGOT);
        plate("steel", CraftItemTagsProvider.INGOTS_STEEL, GtItems.STEEL_PLATE, GtItems.STEEL_DUST,
                GtItems.STEEL_INGOT);
        machine(MachineRecipe.Kind.MACERATOR, "nickel_ingot", Ingredient.of(CraftItemTagsProvider.INGOTS_NICKEL), 1,
                GtItems.NICKEL_DUST, 1);
        machine(MachineRecipe.Kind.MACERATOR, "aluminium_ingot",
                Ingredient.of(CraftItemTagsProvider.INGOTS_ALUMINIUM), 1, GtItems.ALUMINIUM_DUST, 1);
        machine(MachineRecipe.Kind.MACERATOR, "steel_ingot", Ingredient.of(CraftItemTagsProvider.INGOTS_STEEL), 1,
                GtItems.STEEL_DUST, 1);
        // Only nickel dust smelts; aluminium and steel need the blast furnace (basicmetalblastfurnacerequirement).
        smelt("nickel_ingot_from_dust", Ingredient.of(CraftItemTagsProvider.DUSTS_NICKEL), GtItems.NICKEL_INGOT, 1,
                0.0F);

        conveyorModule("conveyor_module_from_aluminium", CraftItemTagsProvider.INGOTS_ALUMINIUM);
        conveyorModule("conveyor_module_from_aluminium_plates", CraftItemTagsProvider.PLATES_ALUMINIUM);
        mixedMetal("refined_iron_bronze_aluminium", CraftItemTagsProvider.PLATES_REFINED_IRON,
                CraftItemTagsProvider.PLATES_ALUMINIUM, 1);
        mixedMetal("nickel_bronze_tin", CraftItemTagsProvider.PLATES_NICKEL, CraftItemTagsProvider.PLATES_TIN, 1);
        mixedMetal("nickel_bronze_aluminium", CraftItemTagsProvider.PLATES_NICKEL,
                CraftItemTagsProvider.PLATES_ALUMINIUM, 1);
        mixedMetal("steel_bronze_tin", CraftItemTagsProvider.PLATES_STEEL, CraftItemTagsProvider.PLATES_TIN, 2);
        mixedMetal("steel_bronze_aluminium", CraftItemTagsProvider.PLATES_STEEL,
                CraftItemTagsProvider.PLATES_ALUMINIUM, 3);
        shaped(GtItems.machine(GtMachine.INDUSTRIAL_ELECTROLYZER), 1, "industrial_electrolyzer_from_aluminium")
                .pattern("RDR", "CEC", "RMR").define('R', CraftItemTagsProvider.PLATES_ALUMINIUM)
                .define('D', machine(Ic2Machine.EXTRACTOR)).define('C', Ic2Items.ADVANCED_CIRCUIT)
                .define('E', machine(Ic2Machine.ELECTROLYZER)).define('M', machine(Ic2Machine.MAGNETIZER)).save();
        for (String variant : new String[]{"aluminium_ingot", "refined_iron_plate", "aluminium_plate"}) {
            TagKey<Item> metal = switch (variant) {
                case "aluminium_ingot" -> CraftItemTagsProvider.INGOTS_ALUMINIUM;
                case "refined_iron_plate" -> CraftItemTagsProvider.PLATES_REFINED_IRON;
                default -> CraftItemTagsProvider.PLATES_ALUMINIUM;
            };
            shaped(machine(Ic2Machine.GENERATOR), 1, "generator_from_" + variant).pattern(" B ", "RRR", " F ")
                    .define('B', Ic2Items.RE_BATTERY).define('R', metal)
                    .define('F', machine(Ic2Machine.IRON_FURNACE)).save();
        }

        // GT replacements of IC2 recipes that need the new materials (08-rezepte.md).
        // User request (2026-10-23): only IC2's own solar panel recipe, not GregTech's silicon one, so the
        // basic panel needs no blast furnace.
        shaped(machine(Ic2Machine.SOLAR_PANEL), 1, "solar_panel").pattern("CgC", "gCg", "cGc")
                .define('C', CraftItemTagsProvider.DUSTS_COAL).define('g', Items.GLASS)
                .define('c', Ic2Items.ELECTRONIC_CIRCUIT).define('G', machine(Ic2Machine.GENERATOR)).save();
        steelTool(Ic2Items.MINING_DRILL, "mining_drill", " S ", "SCS", "SBS");
        steelTool(Ic2Items.CHAINSAW, "chainsaw", " SS", "SCS", "BS ");
        steelTool(Ic2Items.ELECTRIC_HOE, "electric_hoe", "SS ", " C ", " B ");
        steelTool(Ic2Items.ELECTRIC_TREETAP, "electric_treetap", " B ", "SCS", "S  ");
        steelTool(Ic2Items.ELECTRIC_WRENCH, "electric_wrench", "S S", "SCS", " B ");
    }

    private void standardCasing(String name, TagKey<Item> plates) {
        shaped(GtItems.casing(GtCasingBlock.Type.STANDARD), 4, name).pattern("AAA", "CMC", "AAA")
                .define('A', plates).define('C', Ic2Items.ELECTRONIC_CIRCUIT)
                .define('M', CraftItemTagsProvider.RAW_MACHINE_TIER_01)
                .save();
    }

    /** GT {@code ElectricSteelTools=true}: steel plates, electronic circuit, RE battery. */
    private void steelTool(ItemLike result, String name, String... rows) {
        shaped(result, 1, name).pattern(rows).define('S', CraftItemTagsProvider.PLATES_STEEL)
                .define('C', Ic2Items.ELECTRONIC_CIRCUIT).define('B', Ic2Items.RE_BATTERY).save();
    }

    /** Mixed metal ingot {@code X/Y/Z} with a bronze plate in the middle, and the same with brass (spec 13 §3). */
    private void mixedMetal(String name, TagKey<Item> top, TagKey<Item> bottom, int count) {
        for (TagKey<Item> middle : List.of(CraftItemTagsProvider.PLATES_BRONZE, CraftItemTagsProvider.PLATES_BRASS)) {
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Ic2Items.MIXED_METAL_INGOT, count).pattern("X")
                    .pattern("Y").pattern("Z").define('X', top).define('Y', middle).define('Z', bottom)
                    .unlockedBy("has_plate", has(bottom)).save(output, id("mixed_metal_ingot_"
                            + (middle == CraftItemTagsProvider.PLATES_BRASS ? name.replace("_bronze_", "_brass_") : name)));
        }
    }

    /** Assembling machine (spec 13 §6): crafting recipe, lithium items and the assembler recipes of §6.2. */
    private void assembler() {
        for (var metal : List.of(CraftItemTagsProvider.PLATES_REFINED_IRON, CraftItemTagsProvider.PLATES_ALUMINIUM)) {
            shaped(GtItems.machine(GtMachine.ASSEMBLING_MACHINE), 1,
                    metal == CraftItemTagsProvider.PLATES_ALUMINIUM ? "assembling_machine_from_aluminium"
                            : "assembling_machine")
                    .pattern("CPC", "RQR", "CRC").define('C', Ic2Items.ELECTRONIC_CIRCUIT).define('P', Items.PISTON)
                    .define('R', metal).define('Q', GtItems.CONVEYOR_MODULE).save();
        }
        shaped(GtItems.LITHIUM_BATTERY, 1, "lithium_battery").pattern(" C ", "ALA", "ALA")
                .define('C', Ic2Items.cable(CableType.DOUBLE_INSULATED_GOLD)).define('L', GtItems.LITHIUM_CELL)
                .define('A', CraftItemTagsProvider.PLATES_ALUMINIUM).save();
        shaped(GtItems.LITHIUM_BATPACK, 1, "lithium_batpack").pattern("LCL", "LAL", "L L")
                .define('L', GtItems.LITHIUM_BATTERY).define('C', Ic2Items.ADVANCED_CIRCUIT)
                .define('A', CraftItemTagsProvider.PLATES_ALUMINIUM).save();

        ItemLike generator = machine(Ic2Machine.GENERATOR);
        ItemLike waterMill = machine(Ic2Machine.WATER_MILL);
        assemble("water_mill", SizedIngredient.of(generator, 1),
                SizedIngredient.of(CraftItemTagsProvider.PLATES_ALUMINIUM, 4), new ItemStack(waterMill, 2), 6400, 8);
        // User request (2026-10-18): IC2's own water mill recipe back, which GregTech removed
        // (ExpensiveWatermillRecipe), so the mills no longer need aluminium plates.
        shaped(waterMill, 2, "water_mill_from_generator").pattern("SPS", "PGP", "SPS")
                .define('S', Items.STICK).define('P', ItemTags.PLANKS).define('G', generator).save();
        assemble("wind_mill", SizedIngredient.of(generator, 1), SizedIngredient.of(Ic2Items.CARBON_PLATE, 4),
                new ItemStack(machine(Ic2Machine.WIND_MILL)), 6400, 8);
        assemble("generator_from_water_mills", SizedIngredient.of(waterMill, 2), null, new ItemStack(generator),
                6400, 8);
        assemble("pneumatic_generator_upgrade", SizedIngredient.of(BcItems.IRON_GEAR, 1),
                SizedIngredient.of(generator, 1), new ItemStack(GtItems.PNEUMATIC_GENERATOR_UPGRADE.get()), 3200, 4);
        assemble("hv_transformer_upgrade", SizedIngredient.of(machine(Ic2Machine.HV_TRANSFORMER), 1),
                SizedIngredient.of(Ic2Items.TRANSFORMER_UPGRADE, 1),
                new ItemStack(GtItems.HV_TRANSFORMER_UPGRADE.get()), 3200, 4);
        assemble("lithium_battery_upgrade", SizedIngredient.of(GtItems.LITHIUM_BATTERY, 1),
                SizedIngredient.of(CraftItemTagsProvider.PLATES_ALUMINIUM, 4),
                new ItemStack(GtItems.LITHIUM_BATTERY_UPGRADE.get()), 3200, 4);
        assemble("machine_casing", SizedIngredient.of(CraftItemTagsProvider.INGOTS_REFINED_IRON, 8), null,
                new ItemStack(Ic2Items.MACHINE_CASING.get()), 400, 8);
        assemble("machine_parts", SizedIngredient.of(CraftItemTagsProvider.PLATES_REFINED_IRON, 2),
                SizedIngredient.of(Ic2Items.ELECTRONIC_CIRCUIT, 1), new ItemStack(GtItems.MACHINE_PARTS.get(), 4),
                800, 16);
        assemble("machine_parts_from_aluminium", SizedIngredient.of(CraftItemTagsProvider.PLATES_ALUMINIUM, 2),
                SizedIngredient.of(Ic2Items.ELECTRONIC_CIRCUIT, 1), new ItemStack(GtItems.MACHINE_PARTS.get(), 3),
                800, 16);
        machineHull("aluminium_machine_hull", CraftItemTagsProvider.PLATES_ALUMINIUM, GtItems.ALUMINIUM_MACHINE_HULL);
        machineHull("bronze_machine_hull", CraftItemTagsProvider.PLATES_BRONZE, GtItems.BRONZE_MACHINE_HULL);
        machineHull("steel_machine_hull", CraftItemTagsProvider.PLATES_STEEL, GtItems.STEEL_MACHINE_HULL);
        assemble("re_batteries_from_batpack", SizedIngredient.of(Ic2Items.BATPACK, 1), null,
                new ItemStack(Ic2Items.RE_BATTERY.get(), 6), 1600, 2);
        assemble("lithium_batteries_from_lithium_batpack", SizedIngredient.of(GtItems.LITHIUM_BATPACK, 1), null,
                new ItemStack(GtItems.LITHIUM_BATTERY.get(), 6), 3200, 4);
        // itemRubber; the duration is configured per output, so all recipes of one cable share it.
        insulate("insulated_copper_cable", CableType.COPPER, 1, CableType.INSULATED_COPPER, 100);
        insulate("double_insulated_gold_cable", CableType.GOLD, 2, CableType.DOUBLE_INSULATED_GOLD, 200);
        insulate("double_insulated_gold_cable_from_insulated", CableType.INSULATED_GOLD, 1,
                CableType.DOUBLE_INSULATED_GOLD, 200);
        insulate("quadruple_insulated_hv_cable", CableType.HV, 3, CableType.QUADRUPLE_INSULATED_HV, 300);
        insulate("quadruple_insulated_hv_cable_from_insulated", CableType.INSULATED_HV, 2,
                CableType.QUADRUPLE_INSULATED_HV, 300);
        insulate("quadruple_insulated_hv_cable_from_double_insulated", CableType.DOUBLE_INSULATED_HV, 1,
                CableType.QUADRUPLE_INSULATED_HV, 300);
        // Gears: GT adds an assembler recipe for every ingot that crafts into a gear around a stone gear.
        assemble("iron_gear", SizedIngredient.of(BcItems.STONE_GEAR, 1), SizedIngredient.of(Tags.Items.INGOTS_IRON, 4),
                new ItemStack(BcItems.IRON_GEAR.get()), 1600, 2);
        assemble("tin_gear", SizedIngredient.of(BcItems.STONE_GEAR, 1),
                SizedIngredient.of(CraftItemTagsProvider.INGOTS_TIN, 4), new ItemStack(FrItems.TIN_GEAR.get()), 1600, 2);
        assemble("gold_gear", SizedIngredient.of(BcItems.IRON_GEAR, 1), SizedIngredient.of(Tags.Items.INGOTS_GOLD, 4),
                new ItemStack(BcItems.GOLD_GEAR.get()), 800, 1);
        assemble("diamond_gear", SizedIngredient.of(BcItems.GOLD_GEAR, 1), new SizedIngredient(diamond(), 4),
                new ItemStack(BcItems.DIAMOND_GEAR.get()), 1600, 2);
        // stickWood, ingotIron, ingotGold
        assemble("lever", SizedIngredient.of(Tags.Items.RODS_WOODEN, 1), SizedIngredient.of(Items.COBBLESTONE, 1),
                new ItemStack(Items.LEVER), 400, 1);
        assemble("torch_from_coal", SizedIngredient.of(Tags.Items.RODS_WOODEN, 1),
                new SizedIngredient(Ingredient.of(Items.COAL, Items.CHARCOAL), 1), new ItemStack(Items.TORCH, 4), 400,
                1);
        assemble("torch_from_resin", SizedIngredient.of(Tags.Items.RODS_WOODEN, 1), SizedIngredient.of(Ic2Items.RESIN, 1),
                new ItemStack(Items.TORCH, 4), 400, 1);
        assemble("redstone_torch", SizedIngredient.of(Tags.Items.RODS_WOODEN, 1),
                SizedIngredient.of(Tags.Items.DUSTS_REDSTONE, 1), new ItemStack(Items.REDSTONE_TORCH), 400, 1);
        assemble("compass", SizedIngredient.of(Tags.Items.INGOTS_IRON, 4),
                SizedIngredient.of(Tags.Items.DUSTS_REDSTONE, 1), new ItemStack(Items.COMPASS), 400, 4);
        assemble("clock", SizedIngredient.of(Tags.Items.INGOTS_GOLD, 4), SizedIngredient.of(Tags.Items.DUSTS_REDSTONE, 1),
                new ItemStack(Items.CLOCK), 400, 4);
    }

    private void machineHull(String name, TagKey<Item> plates, ItemLike hull) {
        assemble(name, SizedIngredient.of(plates, 8), SizedIngredient.of(GtItems.MACHINE_PARTS, 1), new ItemStack(hull),
                400, 8);
    }

    private void insulate(String name, CableType cable, int rubber, CableType result, int duration) {
        assemble(name, SizedIngredient.of(Ic2Items.cable(cable), 1), SizedIngredient.of(Ic2Items.RUBBER, rubber),
                new ItemStack(Ic2Items.cable(result).get()), duration, 2);
    }

    /** {@code GT_Mod.addAssemblerRecipe}: one or two inputs, one output. */
    private void assemble(String name, SizedIngredient input1, @Nullable SizedIngredient input2, ItemStack result,
                          int duration, int euPerTick) {
        List<SizedIngredient> inputs = input2 == null ? List.of(input1) : List.of(input1, input2);
        output.accept(id("gt_assembler/" + name), new GtRecipe(GtRecipe.Kind.ASSEMBLER, inputs, List.of(result),
                duration, euPerTick, 0), null);
    }

    /** {@code GT_Mod.addBlastRecipe}: 128 EU/t, {@code heat} = minimum heat capacity in K. */
    private void blast(String name, SizedIngredient input1, @Nullable SizedIngredient input2, int duration, int heat,
                       ItemStack... outputs) {
        List<SizedIngredient> inputs = input2 == null ? List.of(input1) : List.of(input1, input2);
        output.accept(id("gt_blast_furnace/" + name), new GtRecipe(GtRecipe.Kind.BLAST_FURNACE, inputs,
                List.of(outputs), duration, 128, heat), null);
    }

    /** Industrial electrolyzer (spec 13 §4): crafting recipe (aluminium plate variant follows with 6b.3) and recipes. */
    private void electrolyzer() {
        shaped(GtItems.machine(GtMachine.INDUSTRIAL_ELECTROLYZER), 1, "industrial_electrolyzer")
                .pattern("RDR", "CEC", "RMR").define('R', CraftItemTagsProvider.PLATES_REFINED_IRON)
                .define('D', machine(Ic2Machine.EXTRACTOR)).define('C', Ic2Items.ADVANCED_CIRCUIT)
                .define('E', machine(Ic2Machine.ELECTROLYZER)).define('M', machine(Ic2Machine.MAGNETIZER)).save();
        electrolysis("water", Ingredient.of(GtItems.WATER), 6, 5, 775, 120, new ItemStack(GtItems.HYDROGEN_CELL.get(), 4),
                new ItemStack(Ic2Items.AIR_CELL.get()));
        electrolysis("electrolyzed_water_cell", Ingredient.of(Ic2Items.ELECTROLYZED_WATER_CELL), 6, 0, 100, 30,
                new ItemStack(GtItems.HYDROGEN_CELL.get(), 4), new ItemStack(Ic2Items.AIR_CELL.get()), ItemStack.EMPTY,
                new ItemStack(Ic2Items.EMPTY_CELL.get()));
        electrolysis("water_cell", Ingredient.of(Ic2Items.WATER_CELL), 1, 0, 128, 128,
                new ItemStack(Ic2Items.ELECTROLYZED_WATER_CELL.get()));
        electrolysis("water_bucket", Ingredient.of(Items.WATER_BUCKET), 1, 1, 128, 128,
                new ItemStack(Ic2Items.ELECTROLYZED_WATER_CELL.get()), new ItemStack(Items.BUCKET));
        electrolysis("bone_meal", Ingredient.of(Items.BONE_MEAL), 3, 1, 24, 106,
                new ItemStack(GtItems.CALCIUM_CELL.get()));
        electrolysis("sugar", Ingredient.of(Items.SUGAR), 32, 7, 210, 32, new ItemStack(GtItems.CARBON_CELL.get(), 2),
                new ItemStack(Ic2Items.WATER_CELL.get(), 5));
        electrolysis("blaze_powder", Ingredient.of(Items.BLAZE_POWDER), 4, 0, 300, 25,
                new ItemStack(GtItems.DARK_ASH.get()), new ItemStack(GtItems.SULFUR_DUST.get()));
        electrolysis("sand", Ingredient.of(Items.SAND), 16, 2, 1000, 25, new ItemStack(GtItems.SILICON_CELL.get()),
                new ItemStack(Ic2Items.AIR_CELL.get()));
        electrolysis("clay_dust", Ingredient.of(CraftItemTagsProvider.DUSTS_CLAY), 8, 5, 200, 50,
                new ItemStack(GtItems.LITHIUM_CELL.get()), new ItemStack(GtItems.SILICON_CELL.get(), 2),
                new ItemStack(GtItems.ALUMINIUM_DUST.get(), 2), new ItemStack(GtItems.SODIUM_CELL.get(), 2));
        electrolysis("coal_dust", Ingredient.of(CraftItemTagsProvider.DUSTS_COAL), 1, 2, 40, 50,
                new ItemStack(GtItems.CARBON_CELL.get(), 2));
    }

    /** {@code GT_Mod.addElectrolyzerRecipe}: input 2 is {@code cells} empty cells (none if 0). */
    private void electrolysis(String name, Ingredient input, int amount, int cells, int duration, int euPerTick,
                              ItemStack... outputs) {
        List<SizedIngredient> inputs = cells > 0
                ? List.of(new SizedIngredient(input, amount), new SizedIngredient(Ingredient.of(Ic2Items.EMPTY_CELL), cells))
                : List.of(new SizedIngredient(input, amount));
        output.accept(id("gt_electrolyzer/" + name), new GtRecipe(GtRecipe.Kind.ELECTROLYZER, inputs,
                List.of(outputs), duration, euPerTick, 0), null);
    }

    private void conveyorModule(String name, TagKey<Item> metal) {
        shaped(GtItems.CONVEYOR_MODULE, 1, name).pattern("GGG", "RRR", "CBC").define('G', Items.GLASS)
                .define('R', metal).define('C', Ic2Items.ELECTRONIC_CIRCUIT).define('B', Ic2Items.RE_BATTERY)
                .save();
    }

    /**
     * GT {@code ingot*} and {@code plate*} handling: bender ingot → plate (50 ticks, 20 EU/t), furnace plate → ingot,
     * macerator plate → dust (if the dust exists).
     */
    private void plate(String name, TagKey<Item> ingots, ItemLike plate, @Nullable ItemLike dust, ItemLike ingot) {
        output.accept(id("gt_bender/" + name), new GtRecipe(GtRecipe.Kind.BENDER,
                List.of(new SizedIngredient(Ingredient.of(ingots), 1)), List.of(new ItemStack(plate)), 50, 20, 0), null);
        smelt(name + "_ingot_from_plate", Ingredient.of(plate), ingot, 1, 0.0F);
        if (dust != null) {
            machine(MachineRecipe.Kind.MACERATOR, name + "_plate", Ingredient.of(plate), 1, dust, 1);
        }
    }

    // --- New power plants (spec 14) ---

    private void powerPlants() {
        shaped(PpItems.COKE_OVEN_BRICKS, 2, "coke_oven_bricks").pattern("BSB", "SCS", "BSB")
                .define('B', Items.BRICK).define('S', Items.SAND).define('C', Items.CLAY_BALL).save();
        shaped(PpItems.COKE_OVEN, 1, "coke_oven").pattern("BBB", "BFB", "BBB")
                .define('B', PpItems.COKE_OVEN_BRICKS).define('F', Items.FURNACE).save();
        shaped(PpItems.COKE_BLOCK, 1, "coke_block").pattern("CCC", "CCC", "CCC")
                .define('C', PpItems.COKE).save();
        shapeless(PpItems.COKE, 9, "coke_from_block", Ingredient.of(PpItems.COKE_BLOCK));
        shaped(PpItems.BOILER_CASING, 4, "boiler_casing").pattern("PBP", "BFB", "PBP")
                .define('P', CraftItemTagsProvider.PLATES_IRON).define('B', Items.BRICKS).define('F', Items.FURNACE)
                .save();
        shaped(PpItems.BOILER_FIREBOX, 1, "boiler_firebox").pattern("CCC", "CIC", "CCC")
                .define('C', PpItems.BOILER_CASING).define('I', machine(Ic2Machine.IRON_FURNACE)).save();
        shaped(PpItems.TURBINE_CASING, 6, "turbine_casing").pattern("PPP", "PMP", "PPP")
                .define('P', CraftItemTagsProvider.PLATES_STEEL).define('M', Ic2Items.MACHINE_CASING).save();
        shaped(PpItems.TURBINE_ROTOR, 1, "turbine_rotor").pattern("PSP", "SIS", "PSP")
                .define('P', CraftItemTagsProvider.PLATES_STEEL).define('S', CraftItemTagsProvider.INGOTS_STEEL)
                .define('I', Items.IRON_BLOCK).save();
        shaped(PpItems.STEAM_TURBINE, 1, "steam_turbine").pattern("CRC", "GMG", "CRC")
                .define('C', PpItems.TURBINE_CASING).define('R', PpItems.TURBINE_ROTOR)
                .define('G', machine(Ic2Machine.GENERATOR)).define('M', machine(Ic2Machine.MV_TRANSFORMER)).save();
        shaped(PpItems.WATER_TURBINE_CASING, 2, "water_turbine_casing").pattern("RRR", "RMR", "RRR")
                .define('R', CraftItemTagsProvider.PLATES_REFINED_IRON).define('M', Ic2Items.MACHINE_CASING).save();
        shaped(PpItems.WATER_TURBINE, 1, "water_turbine").pattern("WCW", "CGC", "WCW")
                .define('W', machine(Ic2Machine.WATER_MILL)).define('C', PpItems.WATER_TURBINE_CASING)
                .define('G', machine(Ic2Machine.GENERATOR)).save();
        // large stages (spec 23)
        shaped(PpItems.HIGH_PRESSURE_BOILER_CASING, 8, "high_pressure_boiler_casing").pattern("SBS", "BKB", "SBS")
                .define('S', CraftItemTagsProvider.PLATES_STEEL).define('B', PpItems.BOILER_CASING)
                .define('K', Items.BRICKS).save();
        shaped(PpItems.HIGH_PRESSURE_BOILER_FIREBOX, 1, "high_pressure_boiler_firebox").pattern("CCC", "CFC", "CCC")
                .define('C', PpItems.HIGH_PRESSURE_BOILER_CASING).define('F', PpItems.BOILER_FIREBOX).save();
        shaped(PpItems.LARGE_TURBINE_CASING, 8, "large_turbine_casing").pattern("PPP", "PAP", "PPP")
                .define('P', CraftItemTagsProvider.PLATES_STEEL).define('A', Ic2Items.ADVANCED_MACHINE_CASING).save();
        shaped(PpItems.LARGE_TURBINE_ROTOR, 1, "large_turbine_rotor").pattern("TWT", "WRW", "TWT")
                .define('T', CraftItemTagsProvider.PLATES_TITANIUM)
                .define('W', CraftItemTagsProvider.PLATES_TUNGSTENSTEEL).define('R', PpItems.TURBINE_ROTOR).save();
        shaped(PpItems.LARGE_STEAM_TURBINE, 1, "large_steam_turbine").pattern("CRC", "GHG", "CRC")
                .define('C', PpItems.LARGE_TURBINE_CASING).define('R', PpItems.LARGE_TURBINE_ROTOR)
                .define('G', PpItems.STEAM_TURBINE).define('H', machine(Ic2Machine.HV_TRANSFORMER)).save();
        shaped(PpItems.LARGE_WATER_TURBINE_CASING, 8, "large_water_turbine_casing").pattern("SRS", "RMR", "SRS")
                .define('S', CraftItemTagsProvider.PLATES_STEEL)
                .define('R', CraftItemTagsProvider.PLATES_REFINED_IRON).define('M', Ic2Items.MACHINE_CASING).save();
        shaped(PpItems.LARGE_WATER_TURBINE, 1, "large_water_turbine").pattern("WCW", "CHC", "WCW")
                .define('W', PpItems.WATER_TURBINE).define('C', PpItems.LARGE_WATER_TURBINE_CASING)
                .define('H', machine(Ic2Machine.HV_TRANSFORMER)).save();
        shaped(PpItems.WIND_TURBINE_MAST, 8, "wind_turbine_mast").pattern("R R", "RRR", "R R")
                .define('R', CraftItemTagsProvider.INGOTS_REFINED_IRON).save();
        shaped(PpItems.LARGE_WIND_TURBINE, 1, "large_wind_turbine").pattern("WAW", "GHG", "WAW")
                .define('W', machine(Ic2Machine.WIND_MILL)).define('A', Ic2Items.ADVANCED_MACHINE_CASING)
                .define('G', machine(Ic2Machine.GENERATOR)).define('H', machine(Ic2Machine.HV_TRANSFORMER)).save();
        shaped(PpItems.IRON_WIND_ROTOR, 1, "iron_wind_rotor").pattern("PIP", "IBI", "PIP")
                .define('P', CraftItemTagsProvider.PLATES_IRON).define('I', Items.IRON_INGOT)
                .define('B', Items.IRON_BLOCK).save();
        shaped(PpItems.STEEL_WIND_ROTOR, 1, "steel_wind_rotor").pattern("PSP", "SRS", "PSP")
                .define('P', CraftItemTagsProvider.PLATES_STEEL).define('S', CraftItemTagsProvider.INGOTS_STEEL)
                .define('R', PpItems.IRON_WIND_ROTOR).save();
        shaped(PpItems.CARBON_WIND_ROTOR, 1, "carbon_wind_rotor").pattern("CAC", "ARA", "CAC")
                .define('C', Ic2Items.CARBON_PLATE).define('A', Ic2Items.ADVANCED_ALLOY)
                .define('R', PpItems.STEEL_WIND_ROTOR).save();
    }

    // --- Compact Solars (multiblock variant, spec 11 §4): shapeless transformer → controller ---

    private void compactSolars() {
        // Original Compact Solars recipes: 8 of the tier below around its transformer (user, 2026-10-20)
        for (SolarArrayType type : SolarArrayType.values()) {
            ItemLike lower = type.ordinal() == 0 ? machine(Ic2Machine.SOLAR_PANEL)
                    : CsItems.array(SolarArrayType.values()[type.ordinal() - 1]);
            shaped(CsItems.array(type), 1, type.getName()).pattern("SSS", "SXS", "SSS").define('S', lower)
                    .define('X', machine(type.getTransformer())).save();
        }
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
        bcShaped(BcItems.AUTO_WORKBENCH).pattern(" g ").pattern("gwg").pattern(" g ").define('w', Items.CRAFTING_TABLE)
                .define('g', BcItems.WOODEN_GEAR).unlockedBy("has_gear", has(BcItems.WOODEN_GEAR))
                .save(output, id("auto_workbench"));
        pipe(PipeType.WOODEN_TRANSPORT, Ingredient.of(ItemTags.PLANKS), Ingredient.of(ItemTags.PLANKS));
        pipe(PipeType.COBBLESTONE_TRANSPORT, Ingredient.of(Items.COBBLESTONE), Ingredient.of(Items.COBBLESTONE));
        pipe(PipeType.STONE_TRANSPORT, Ingredient.of(Items.STONE), Ingredient.of(Items.STONE));
        pipe(PipeType.SANDSTONE_TRANSPORT, Ingredient.of(Items.SANDSTONE), Ingredient.of(Items.SANDSTONE));
        pipe(PipeType.IRON_TRANSPORT, Ingredient.of(Items.IRON_INGOT), Ingredient.of(Items.IRON_INGOT));
        pipe(PipeType.GOLDEN_TRANSPORT, Ingredient.of(Items.GOLD_INGOT), Ingredient.of(Items.GOLD_INGOT));
        pipe(PipeType.DIAMOND_TRANSPORT, Ingredient.of(Items.DIAMOND), Ingredient.of(Items.DIAMOND));
        pipe(PipeType.OBSIDIAN_TRANSPORT, Ingredient.of(Items.OBSIDIAN), Ingredient.of(Items.OBSIDIAN));
        pipe(PipeType.EMERALD_TRANSPORT, Ingredient.of(Items.EMERALD), Ingredient.of(Items.EMERALD));
        pipe(PipeType.VOID_TRANSPORT, Ingredient.of(Items.INK_SAC), Ingredient.of(Items.REDSTONE));
        // pipeWaterproof: cactus green (dye meta 2).
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, BcItems.PIPE_WATERPROOF).requires(Items.GREEN_DYE)
                .unlockedBy("has_green_dye", has(Items.GREEN_DYE)).save(output, id("pipe_waterproof"));
        pipe(PipeType.WOODEN_FLUID, BcItems.PIPE_WATERPROOF, PipeType.WOODEN_TRANSPORT);
        pipe(PipeType.COBBLESTONE_FLUID, BcItems.PIPE_WATERPROOF, PipeType.COBBLESTONE_TRANSPORT);
        pipe(PipeType.STONE_FLUID, BcItems.PIPE_WATERPROOF, PipeType.STONE_TRANSPORT);
        pipe(PipeType.SANDSTONE_FLUID, BcItems.PIPE_WATERPROOF, PipeType.SANDSTONE_TRANSPORT);
        pipe(PipeType.IRON_FLUID, BcItems.PIPE_WATERPROOF, PipeType.IRON_TRANSPORT);
        pipe(PipeType.GOLDEN_FLUID, BcItems.PIPE_WATERPROOF, PipeType.GOLDEN_TRANSPORT);
        pipe(PipeType.EMERALD_FLUID, BcItems.PIPE_WATERPROOF, PipeType.EMERALD_TRANSPORT);
        pipe(PipeType.VOID_FLUID, BcItems.PIPE_WATERPROOF, PipeType.VOID_TRANSPORT);
        pipe(PipeType.WOODEN_POWER, Items.REDSTONE, PipeType.WOODEN_TRANSPORT);
        pipe(PipeType.STONE_POWER, Items.REDSTONE, PipeType.STONE_TRANSPORT);
        pipe(PipeType.GOLDEN_POWER, Items.REDSTONE, PipeType.GOLDEN_TRANSPORT);
        pipe(PipeType.COBBLESTONE_STRUCTURE, Items.GRAVEL, PipeType.COBBLESTONE_TRANSPORT);
    }

    /** BuildCraft {@code createPipe} with two ingredients: shapeless, one pipe. */
    private void pipe(PipeType type, ItemLike first, PipeType base) {
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, BcItems.pipe(type)).requires(first)
                .requires(BcItems.pipe(base)).unlockedBy("has_pipe", has(BcItems.pipe(base)))
                .save(output, id(type.getName()));
    }

    /** BuildCraft {@code createPipe} with three ingredients: one row, eight pipes. */
    private void pipe(PipeType type, Ingredient left, Ingredient right) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, BcItems.pipe(type), 8).pattern("LGR").define('L', left)
                .define('G', Items.GLASS).define('R', right).unlockedBy("has_glass", has(Items.GLASS))
                .save(output, id(type.getName()));
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
                .define('R', Items.REDSTONE).define('G', Items.GLOWSTONE_DUST).define('L', CraftItemTagsProvider.ITEM_LAZURITE)
                .define('C', Ic2Items.ELECTRONIC_CIRCUIT).save();
        shaped(Ic2Items.ADVANCED_CIRCUIT, 1, "advanced_circuit_vertical").pattern("RLR").pattern("GCG").pattern("RLR")
                .define('R', Items.REDSTONE).define('G', Items.GLOWSTONE_DUST).define('L', CraftItemTagsProvider.ITEM_LAZURITE)
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
                .define('F', Items.FLINT).define('D', diamond()).define('M', CraftItemTagsProvider.RAW_MACHINE_TIER_01)
                .define('A', Ic2Items.ADVANCED_CIRCUIT).save();
        // GT: second variant with a grinding head (craftingGrinder) and electronic circuits.
        shaped(machine(Ic2Machine.MACERATOR), 1, "macerator_from_grinder").pattern("FDF", "AMA", "FAF")
                .define('F', Items.FLINT).define('D', CraftItemTagsProvider.CRAFTING_GRINDER)
                .define('M', CraftItemTagsProvider.RAW_MACHINE_TIER_01).define('A', Ic2Items.ELECTRONIC_CIRCUIT).save();
        shaped(machine(Ic2Machine.PUMP), 1, "ic2_pump").pattern("cCc", "cMc", "PTP")
                .define('c', Ic2Items.EMPTY_CELL).define('C', Ic2Items.ELECTRONIC_CIRCUIT)
                .define('M', Ic2Items.MACHINE_CASING).define('P', Ic2Items.MINING_PIPE)
                .define('T', Ic2Items.TREETAP).save();
        shaped(machine(Ic2Machine.TELEPORTER), 1, "teleporter").pattern("GFG", "CMC", "GDG")
                .define('G', Ic2Items.ADVANCED_CIRCUIT).define('F', Ic2Items.FREQUENCY_TRANSMITTER)
                .define('C', Ic2Items.cable(CableType.GLASS_FIBRE)).define('M', Ic2Items.ADVANCED_MACHINE_CASING)
                .define('D', Ic2Items.INDUSTRIAL_DIAMOND).save();
        shaped(machine(Ic2Machine.TELEPORTER), 1, "teleporter_from_diamond").pattern("GFG", "CMC", "GDG")
                .define('G', Ic2Items.ADVANCED_CIRCUIT).define('F', Ic2Items.FREQUENCY_TRANSMITTER)
                .define('C', Ic2Items.cable(CableType.GLASS_FIBRE)).define('M', Ic2Items.ADVANCED_MACHINE_CASING)
                .define('D', Items.DIAMOND).save();
        shaped(machine(Ic2Machine.TESLA_COIL), 1, "tesla_coil").pattern("RRR", "RMR", "ICI")
                .define('R', Items.REDSTONE).define('M', machine(Ic2Machine.MV_TRANSFORMER))
                .define('C', Ic2Items.ELECTRONIC_CIRCUIT).define('I', CraftItemTagsProvider.INGOTS_REFINED_IRON).save();
        shaped(machine(Ic2Machine.CROP_MATRON), 1, "crop_matron").pattern("cBc", "CMC", "CCC")
                .define('c', Ic2Items.ELECTRONIC_CIRCUIT).define('B', Items.CHEST).define('C', Ic2Items.CROP)
                .define('M', Ic2Items.MACHINE_CASING).save();
        shapeless(Ic2Items.FREQUENCY_TRANSMITTER, 1, "frequency_transmitter", Ingredient.of(Ic2Items.ELECTRONIC_CIRCUIT),
                Ingredient.of(Ic2Items.cable(CableType.INSULATED_COPPER)));
        shaped(machine(Ic2Machine.TERRAFORMER), 1, "terraformer").pattern("GTG", "DMD", "GDG")
                .define('G', Items.GLOWSTONE_DUST).define('T', Ic2Items.TERRAFORMER_BLUEPRINT)
                .define('D', Items.DIRT).define('M', Ic2Items.ADVANCED_MACHINE_CASING).save();
        shaped(Ic2Items.TERRAFORMER_BLUEPRINT, 1, "terraformer_blueprint").pattern(" C ", " A ", "R R")
                .define('C', Ic2Items.ELECTRONIC_CIRCUIT).define('A', Ic2Items.ADVANCED_CIRCUIT)
                .define('R', Items.REDSTONE).save();
        blueprint(Ic2Items.CULTIVATION_BLUEPRINT, Items.WHEAT_SEEDS, true);
        blueprint(Ic2Items.DESERTIFICATION_BLUEPRINT, Items.SAND, true);
        blueprint(Ic2Items.IRRIGATION_BLUEPRINT, Items.WATER_BUCKET, true);
        blueprint(Ic2Items.CHILLING_BLUEPRINT, Items.SNOWBALL, true);
        blueprint(Ic2Items.FLATIFICATION_BLUEPRINT, Items.DIRT, true);
        shaped(Ic2Items.MUSHROOM_BLUEPRINT, 1, "mushroom_blueprint").pattern("mMm", "M#M", "mMm")
                .define('#', Ic2Items.TERRAFORMER_BLUEPRINT).define('M', Items.BROWN_MUSHROOM)
                .define('m', Items.MYCELIUM).save();
        shaped(machine(Ic2Machine.PERSONAL_SAFE), 1, "personal_safe").pattern("c", "M", "C")
                .define('c', Ic2Items.ELECTRONIC_CIRCUIT).define('M', Ic2Items.MACHINE_CASING).define('C', Items.CHEST)
                .save();
        shaped(machine(Ic2Machine.TRADE_O_MAT), 1, "trade_o_mat").pattern("RRR", "CMC")
                .define('R', Items.REDSTONE).define('C', Items.CHEST).define('M', Ic2Items.MACHINE_CASING).save();
        shaped(machine(Ic2Machine.ENERGY_O_MAT), 1, "energy_o_mat").pattern("RBR", "CMC")
                .define('R', Items.REDSTONE).define('B', Ic2Items.RE_BATTERY)
                .define('C', Ic2Items.cable(CableType.INSULATED_COPPER)).define('M', Ic2Items.MACHINE_CASING).save();
        shaped(machine(Ic2Machine.MINER), 1, "miner").pattern("CMC", " P ", " P ")
                .define('C', Ic2Items.ELECTRONIC_CIRCUIT).define('M', Ic2Items.MACHINE_CASING)
                .define('P', Ic2Items.MINING_PIPE).save();
        shaped(Ic2Items.MINING_PIPE, 8, "ic2_mining_pipe").pattern("I I", "I I", "ITI")
                .define('I', CraftItemTagsProvider.INGOTS_REFINED_IRON).define('T', Ic2Items.TREETAP).save();
        shaped(machine(Ic2Machine.EXTRACTOR), 1, "extractor").pattern("TMT", "TCT")
                .define('T', Ic2Items.TREETAP).define('M', Ic2Items.MACHINE_CASING)
                .define('C', Ic2Items.ELECTRONIC_CIRCUIT).save();
        shaped(machine(Ic2Machine.COMPRESSOR), 1, "compressor").pattern("S S").pattern("SMS").pattern("SCS")
                .define('S', Items.STONE).define('M', Ic2Items.MACHINE_CASING)
                .define('C', Ic2Items.ELECTRONIC_CIRCUIT).save();
        shaped(machine(Ic2Machine.RECYCLER), 1, "recycler").pattern(" G ").pattern("DMD").pattern("IDI")
                .define('G', Items.GLOWSTONE_DUST).define('D', Items.DIRT).define('M', machine(Ic2Machine.COMPRESSOR))
                .define('I', CraftItemTagsProvider.INGOTS_REFINED_IRON).save();
        shaped(machine(Ic2Machine.MAGNETIZER), 1, "magnetizer").pattern("RFR").pattern("RMR").pattern("RFR")
                .define('R', Items.REDSTONE).define('F', Ic2Items.IRON_FENCE).define('M', Ic2Items.MACHINE_CASING)
                .save();
        shaped(machine(Ic2Machine.ELECTROLYZER), 1, "electrolyzer").pattern("c c").pattern("cCc").pattern("EME")
                .define('c', Ic2Items.cable(CableType.INSULATED_COPPER)).define('C', Ic2Items.ELECTRONIC_CIRCUIT)
                .define('E', Ic2Items.EMPTY_CELL).define('M', Ic2Items.MACHINE_CASING).save();
        shaped(machine(Ic2Machine.CANNING_MACHINE), 1, "canning_machine").pattern("TCT").pattern("TMT").pattern("TTT")
                .define('T', CraftItemTagsProvider.INGOTS_TIN).define('C', Ic2Items.ELECTRONIC_CIRCUIT)
                .define('M', Ic2Items.MACHINE_CASING).save();
        shaped(machine(Ic2Machine.INDUCTION_FURNACE), 1, "induction_furnace").pattern("CCC").pattern("CFC")
                .pattern("CMC").define('C', ingotsCopper()).define('F', machine(Ic2Machine.ELECTRIC_FURNACE))
                .define('M', Ic2Items.ADVANCED_MACHINE_CASING).save();
        shaped(Ic2Items.IRON_FENCE, 12, "iron_fence").pattern("III").pattern("III")
                .define('I', CraftItemTagsProvider.INGOTS_REFINED_IRON).save();
        shaped(machine(Ic2Machine.GENERATOR), 1, "generator").pattern(" B ").pattern("III").pattern(" F ")
                .define('B', Ic2Items.RE_BATTERY).define('I', CraftItemTagsProvider.INGOTS_REFINED_IRON)
                .define('F', machine(Ic2Machine.IRON_FURNACE)).save();
        shaped(machine(Ic2Machine.GENERATOR), 1, "generator_from_machine_casing").pattern("B", "M", "F")
                .define('B', Ic2Items.RE_BATTERY).define('M', CraftItemTagsProvider.RAW_MACHINE_TIER_01)
                .define('F', Items.FURNACE)
                .save();
        shaped(machine(Ic2Machine.GEOTHERMAL_GENERATOR), 1, "geothermal_generator").pattern("gCg").pattern("gCg").pattern("IGI")
                .define('g', Items.GLASS).define('C', Ic2Items.EMPTY_CELL)
                .define('I', CraftItemTagsProvider.INGOTS_REFINED_IRON).define('G', machine(Ic2Machine.GENERATOR))
                .save();
        shaped(machine(Ic2Machine.BATBOX), 1, "batbox").pattern("PCP").pattern("BBB").pattern("PPP").define('P', ItemTags.PLANKS)
                .define('C', Ic2Items.cable(CableType.INSULATED_COPPER)).define('B', Ic2Items.RE_BATTERY).save();
        shaped(machine(Ic2Machine.MFE), 1, "mfe").pattern("cCc").pattern("CMC").pattern("cCc")
                .define('c', Ic2Items.cable(CableType.DOUBLE_INSULATED_GOLD))
                .define('C', CraftItemTagsProvider.CRAFTING_100K_EU_STORE)
                .define('M', CraftItemTagsProvider.RAW_MACHINE_TIER_01).save();
        shaped(machine(Ic2Machine.MFSU), 1, "mfsu").pattern("LCL").pattern("LML").pattern("LAL")
                .define('L', Ic2Items.LAPOTRON_CRYSTAL).define('C', Ic2Items.ADVANCED_CIRCUIT)
                .define('M', machine(Ic2Machine.MFE)).define('A', Ic2Items.ADVANCED_MACHINE_CASING).save();
        // Removed in the pack (ic2recipes.Massfabricator=false); kept on purpose (user decision).
        shaped(machine(Ic2Machine.MASS_FABRICATOR), 1, "mass_fabricator").pattern("GCG").pattern("ALA").pattern("GCG")
                .define('G', Items.GLOWSTONE_DUST).define('C', Ic2Items.ADVANCED_CIRCUIT)
                .define('A', Ic2Items.ADVANCED_MACHINE_CASING).define('L', Ic2Items.LAPOTRON_CRYSTAL).save();
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

    // --- §4 UU matter (hidden IC2 recipes, fixed 3×3 layout) ---

    private void uuMatter() {
        uu(Items.STONE, 16, "stone", "   ", " M ", "   ");
        uu(Items.GLASS, 32, "glass", " M ", "M M", " M ");
        uu(Items.GRASS_BLOCK, 16, "grass_block", "   ", "M  ", "M  ");
        uu(Items.MOSSY_COBBLESTONE, 16, "mossy_cobblestone", "   ", " M ", "M M");
        uu(Items.SANDSTONE, 16, "sandstone", "   ", "  M", " M ");
        // GregTech: 16 instead of 4 snow blocks.
        uu(Items.SNOW_BLOCK, 16, "snow_block", "M M", "   ", "   ");
        // Water and lava (1 block each) have no item form in 1.21 – see abweichungen.md.
        // GregTech: dusts instead of iron/gold ore.
        uu(Ic2Items.IRON_DUST, 4, "iron_dust", "M M", " M ", "M M");
        uu(Ic2Items.GOLD_DUST, 2, "gold_dust", " M ", "MMM", " M ");
        uu(Items.OBSIDIAN, 12, "obsidian", "M M", "M M", "   ");
        uu(Items.NETHERRACK, 16, "netherrack", "  M", " M ", "M  ");
        // GregTech: 32 glowstone dust instead of 8 glowstone blocks.
        uu(Items.GLOWSTONE_DUST, 32, "glowstone_dust", " M ", "M M", "MMM");
        uu(Items.OAK_LOG, 8, "oak_log", " M ", "   ", "   ");
        uu(Items.SPRUCE_LOG, 8, "spruce_log", "M  ", "   ", "   ");
        uu(Items.BIRCH_LOG, 8, "birch_log", "  M", "   ", "   ");
        uu(Items.JUNGLE_LOG, 8, "jungle_log", "   ", "M  ", "   ");
        uu(Items.CACTUS, 48, "cactus", " M ", "MMM", "M M");
        uu(Items.VINE, 24, "vine", "M  ", "M  ", "M  ");
        // GregTech: 32 string instead of 12 wool, 8 instead of 20 coal; the snowball recipe is removed.
        uu(Items.STRING, 32, "string", "M M", "   ", " M ");
        uu(Items.COAL, 8, "coal", "  M", "M  ", "  M");
        uu(Items.DIAMOND, 1, "diamond", "MMM", "MMM", "MMM");
        uu(Items.REDSTONE, 24, "redstone", "   ", " M ", "MMM");
        uu(Items.LAPIS_LAZULI, 9, "lapis_lazuli", " M ", " M ", " MM");
        uu(Items.FEATHER, 32, "feather", " M ", " M ", "M M");
        uu(Items.GUNPOWDER, 15, "gunpowder", "MMM", "M  ", "MMM");
        uu(Items.CLAY_BALL, 48, "clay_ball", "MM ", "M  ", "MM ");
        uu(Items.COCOA_BEANS, 32, "cocoa_beans", "MM ", "  M", "MM ");
        uu(Items.INK_SAC, 48, "ink_sac", " MM", " MM", " M ");
        uu(Items.SUGAR_CANE, 48, "sugar_cane", "M M", "M M", "M M");
        uu(Items.FLINT, 32, "flint", " M ", "MM ", "MM ");
        uu(Items.BONE, 32, "bone", "M  ", "MM ", "M  ");
        uu(Ic2Items.RESIN, 21, "resin", "M M", "   ", "M M");
        uu(Ic2Items.IRIDIUM_ORE, 1, "iridium_ore", "MMM", " M ", "MMM");
        uu(Items.MYCELIUM, 24, "mycelium", "   ", "M M", "MMM");
        uu(Items.CHISELED_STONE_BRICKS, 48, "chiseled_stone_bricks", "MM ", "MM ", "M  ");
        uu(Ic2Items.COPPER_DUST, 10, "copper_dust", "  M", "M M", "   ");
        uu(Ic2Items.TIN_DUST, 10, "tin_dust", "   ", "M M", "  M");
        // GregTech additions.
        uu(Items.EMERALD, 2, "emerald", "MMM", "MMM", " M ");
        uu(Items.ENDER_PEARL, 1, "ender_pearl", "MMM", "M M", "MMM");
        uu(Items.BLAZE_ROD, 4, "blaze_rod", "M M", "MM ", "M M");
        uu(Items.LILY_PAD, 32, "lily_pad", "M M", "MMM", " M ");
        uu(Items.LEATHER, 32, "leather", "M M", " M ", "MMM");
        uu(GtItems.LEAD_DUST, 14, "lead_dust", "MMM", "MMM", "M  ");
        uu(Ic2Items.SILVER_DUST, 14, "silver_dust", " M ", "MMM", "MMM");
        uu(GtItems.NICKEL_DUST, 10, "nickel_dust", "M  ", "M M", "   ");
        uu(GtItems.ALUMINIUM_DUST, 16, "aluminium_dust", " M ", " M ", "MMM");
        uu(GtItems.ZINC_DUST, 10, "zinc_dust", "   ", "M M", "M  ");
        uu(GtItems.TITANIUM_DUST, 2, "titanium_dust", "MMM", " M ", " M ");
        uu(GtItems.PLATINUM_DUST, 1, "platinum_dust", "  M", "MMM", "MMM");
        uu(GtItems.TUNGSTEN_DUST, 6, "tungsten_dust", "M  ", "MMM", "MMM");
        uu(GtItems.RUBY, 2, "ruby", " MM", "MMM", "MM ");
        uu(GtItems.SAPPHIRE, 2, "sapphire", "MM ", "MMM", " MM");
        uu(GtItems.GREEN_SAPPHIRE, 2, "green_sapphire", " MM", "MMM", " MM");
        uu(GtItems.OLIVINE, 2, "olivine", "MM ", "MMM", "MM ");
    }

    private void uu(ItemLike result, int count, String name, String... rows) {
        shaped(result, count, "uu_matter/" + name).pattern(rows).define('M', Ic2Items.UU_MATTER).fixed().save();
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
                .define('L', CraftItemTagsProvider.ITEM_LAZURITE).define('C', Ic2Items.ELECTRONIC_CIRCUIT)
                .define('D', Ic2Items.ENERGY_CRYSTAL).save();
    }

    // --- §7 tools ---

    /** §11 armor and its parts. The CF pack follows with the foam sprayer. */
    private void armor() {
        shaped(Ic2Items.CARBON_FIBER, 1, "carbon_fiber").pattern("CC").pattern("CC")
                .define('C', CraftItemTagsProvider.DUSTS_COAL).save();
        shapeless(Ic2Items.CARBON_MESH, 1, "carbon_mesh", Ingredient.of(Ic2Items.CARBON_FIBER),
                Ingredient.of(Ic2Items.CARBON_FIBER));
        shaped(Ic2Items.EMPTY_FUEL_CAN, 1, "empty_fuel_can").pattern(" TT").pattern("T T").pattern("TTT")
                .define('T', CraftItemTagsProvider.INGOTS_TIN).save();
        shaped(Ic2Items.TIN_CAN, 4, "tin_can").pattern("T T").pattern("TTT")
                .define('T', CraftItemTagsProvider.INGOTS_TIN).save();

        // Composite vest, both row orders; Item.plateLeather/plateSteel are damage-0 stacks: only undamaged armor fits.
        Ingredient leather = DataComponentIngredient.of(false, DataComponents.DAMAGE, 0, Items.LEATHER_CHESTPLATE);
        Ingredient iron = DataComponentIngredient.of(false, DataComponents.DAMAGE, 0, Items.IRON_CHESTPLATE);
        shaped(Ic2Items.COMPOSITE_VEST, 1, "composite_vest").pattern("A A", "ALA", "AIA")
                .define('A', Ic2Items.ADVANCED_ALLOY).define('L', leather).define('I', iron).save();
        shaped(Ic2Items.COMPOSITE_VEST, 1, "composite_vest_iron_top").pattern("A A", "AIA", "ALA")
                .define('A', Ic2Items.ADVANCED_ALLOY).define('L', leather).define('I', iron).save();
        shaped(Ic2Items.NANO_HELMET, 1, "nano_helmet").pattern("CcC").pattern("CGC")
                .define('C', Ic2Items.CARBON_PLATE).define('c', Ic2Items.ENERGY_CRYSTAL).define('G', Items.GLASS).save();
        shaped(Ic2Items.NANO_CHESTPLATE, 1, "nano_chestplate").pattern("C C").pattern("CcC").pattern("CCC")
                .define('C', Ic2Items.CARBON_PLATE).define('c', Ic2Items.ENERGY_CRYSTAL).save();
        shaped(Ic2Items.NANO_LEGGINGS, 1, "nano_leggings").pattern("CcC").pattern("C C").pattern("C C")
                .define('C', Ic2Items.CARBON_PLATE).define('c', Ic2Items.ENERGY_CRYSTAL).save();
        shaped(Ic2Items.NANO_BOOTS, 1, "nano_boots").pattern("C C").pattern("CcC")
                .define('C', Ic2Items.CARBON_PLATE).define('c', Ic2Items.ENERGY_CRYSTAL).save();

        shaped(Ic2Items.QUANTUM_HELMET, 1, "quantum_helmet").pattern(" n ", "ILI", "CGC")
                .define('n', Ic2Items.NANO_HELMET).define('I', Ic2Items.IRIDIUM_PLATE)
                .define('L', Ic2Items.LAPOTRON_CRYSTAL).define('G', Ic2Items.REINFORCED_GLASS)
                .define('C', Ic2Items.ADVANCED_CIRCUIT).save();
        shaped(Ic2Items.QUANTUM_CHESTPLATE, 1, "quantum_chestplate").pattern("AnA", "ILI", "IAI")
                .define('n', Ic2Items.NANO_CHESTPLATE).define('I', Ic2Items.IRIDIUM_PLATE)
                .define('L', Ic2Items.LAPOTRON_CRYSTAL).define('A', Ic2Items.ADVANCED_ALLOY).save();
        shaped(Ic2Items.QUANTUM_LEGGINGS, 1, "quantum_leggings").pattern("MLM", "InI", "G G")
                .define('n', Ic2Items.NANO_LEGGINGS).define('I', Ic2Items.IRIDIUM_PLATE)
                .define('L', Ic2Items.LAPOTRON_CRYSTAL).define('G', Items.GLOWSTONE_DUST)
                .define('M', Ic2Items.MACHINE_CASING).save();
        shaped(Ic2Items.QUANTUM_BOOTS, 1, "quantum_boots").pattern("InI", "RLR")
                .define('n', Ic2Items.NANO_BOOTS).define('I', Ic2Items.IRIDIUM_PLATE)
                .define('L', Ic2Items.LAPOTRON_CRYSTAL).define('R', Ic2Items.RUBBER_BOOTS).save();

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
        shaped(Ic2Items.CF_PACK, 1, "cf_pack").pattern("SCS", "FTF", "F F")
                .define('S', DataComponentIngredient.of(false, DataComponents.DAMAGE, FoamSprayerItem.EMPTY_DAMAGE,
                        Ic2Items.FOAM_SPRAYER))
                .define('C', Ic2Items.ELECTRONIC_CIRCUIT).define('F', Ic2Items.EMPTY_FUEL_CAN)
                .define('T', CraftItemTagsProvider.INGOTS_TIN).damage(CfPackItem.EMPTY_DAMAGE).save();
        shaped(Ic2Items.ELECTRIC_JETPACK, 1, "electric_jetpack").pattern("ICI").pattern("IBI").pattern("G G")
                .define('I', CraftItemTagsProvider.INGOTS_REFINED_IRON).define('C', Ic2Items.ADVANCED_CIRCUIT)
                .define('B', Ic2Items.machine(Ic2Machine.BATBOX)).define('G', Items.GLOWSTONE_DUST).save();
        // IC2 has one recipe each for a charged and an empty RE battery; the ingredient accepts both.
        shaped(Ic2Items.BATPACK, 1, "batpack").pattern("BCB").pattern("BTB").pattern("B B")
                .define('T', CraftItemTagsProvider.INGOTS_TIN).define('C', Ic2Items.ELECTRONIC_CIRCUIT)
                .define('B', Ic2Items.RE_BATTERY).save();
        shaped(Ic2Items.LAPPACK, 1, "lappack").pattern("LAL").pattern("LBL").pattern("L L")
                .define('L', CraftItemTagsProvider.CHUNK_LAZURITE).define('A', Ic2Items.ADVANCED_CIRCUIT)
                .define('B', Ic2Items.BATPACK)
                .save();
        shaped(Ic2Items.SOLAR_HELMET, 1, "solar_helmet").pattern("III").pattern("ISI").pattern("CCC")
                .define('I', Items.IRON_INGOT).define('S', Ic2Items.machine(Ic2Machine.SOLAR_PANEL))
                .define('C', Ic2Items.cable(CableType.INSULATED_COPPER)).save();
        // Item.helmetSteel/bootsSteel are damage-0 stacks in AdvRecipe: only undamaged iron armor fits.
        Ingredient ironHelmet = DataComponentIngredient.of(false, DataComponents.DAMAGE, 0, Items.IRON_HELMET);
        Ingredient ironBoots = DataComponentIngredient.of(false, DataComponents.DAMAGE, 0, Items.IRON_BOOTS);
        shaped(Ic2Items.SOLAR_HELMET, 1, "solar_helmet_from_iron_helmet").pattern(" H ").pattern(" S ")
                .pattern("CCC").define('H', ironHelmet).define('S', Ic2Items.machine(Ic2Machine.SOLAR_PANEL))
                .define('C', Ic2Items.cable(CableType.INSULATED_COPPER)).save();
        shaped(Ic2Items.STATIC_BOOTS, 1, "static_boots").pattern("I I").pattern("ISI").pattern("CCC")
                .define('I', Items.IRON_INGOT).define('S', ItemTags.WOOL)
                .define('C', Ic2Items.cable(CableType.INSULATED_COPPER)).save();
        shaped(Ic2Items.STATIC_BOOTS, 1, "static_boots_from_iron_boots").pattern(" H ").pattern(" S ")
                .pattern("CCC").define('H', ironBoots).define('S', ItemTags.WOOL)
                .define('C', Ic2Items.cable(CableType.INSULATED_COPPER)).save();
    }

    private void tools() {
        shaped(Ic2Items.TREETAP, 1, "treetap").pattern(" P ").pattern("PPP").pattern("P  ").define('P', ItemTags.PLANKS).save();
        shaped(Ic2Items.WRENCH, 1, "wrench").pattern("B B").pattern("BBB").pattern(" B ")
                .define('B', CraftItemTagsProvider.INGOTS_BRONZE).save();
        shaped(Ic2Items.INSULATION_CUTTER, 1, "insulation_cutter").pattern("A A", " A ", "I I")
                .define('A', CraftItemTagsProvider.INGOTS_REFINED_IRON).define('I', Items.IRON_INGOT).save();
        shaped(Ic2Items.TOOLBOX, 1, "toolbox").pattern("ICI", "III").define('C', Items.CHEST)
                .define('I', CraftItemTagsProvider.INGOTS_REFINED_IRON).save();
        shaped(Ic2Items.CROP, 2, "crop").pattern("S S", "S S").define('S', Tags.Items.RODS_WOODEN).save();
        shaped(Ic2Items.CROPNALYZER, 1, "cropnalyzer").pattern("cc ", "RGR", "RCR").define('G', Items.GLASS)
                .define('c', Ic2Items.cable(CableType.INSULATED_COPPER)).define('R', Items.REDSTONE)
                .define('C', Ic2Items.ELECTRONIC_CIRCUIT).save();
        shapeless(Ic2Items.FERTILIZER, 2, "fertilizer", Ingredient.of(Ic2Items.SCRAP), Ingredient.of(Items.BONE_MEAL));
        shapeless(Ic2Items.FERTILIZER, 2, "fertilizer_from_scrap", Ingredient.of(Ic2Items.SCRAP),
                Ingredient.of(Ic2Items.SCRAP), Ingredient.of(Ic2Items.FERTILIZER));
        // GregTech adds fertilizer + bone meal.
        shapeless(Ic2Items.FERTILIZER, 2, "fertilizer_from_bone_meal", Ingredient.of(Ic2Items.FERTILIZER),
                Ingredient.of(Items.BONE_MEAL));
        shaped(Ic2Items.WEED_EX, 1, "weed_ex").pattern("R", "G", "C").define('R', Items.REDSTONE)
                .define('G', Ic2Items.GRIN_POWDER).define('C', Ic2Items.EMPTY_CELL).save();
        shaped(Ic2Items.STONE_MUG, 1, "stone_mug").pattern("SS ", "SSS", "SS ").define('S', Items.STONE).save();
        shapeless(Ic2Items.COFFEE_POWDER, 1, "coffee_powder", Ingredient.of(Ic2Items.COFFEE_BEANS));
        shapeless(Ic2Items.COLD_COFFEE, 1, "cold_coffee", Ingredient.of(Ic2Items.STONE_MUG),
                Ingredient.of(Ic2Items.COFFEE_POWDER), water());
        shapeless(Ic2Items.COFFEE, 1, "coffee", Ingredient.of(Ic2Items.DARK_COFFEE), Ingredient.of(Items.SUGAR),
                Ingredient.of(Items.MILK_BUCKET));
        shaped(Ic2Items.BOOZE_BARREL, 1, "booze_barrel").pattern("P", "W", "P").define('P', ItemTags.PLANKS)
                .define('W', Ic2Items.RUBBER_WOOD).save();
        // Emptying: any booze mug back to a stone mug, any barrel to an empty barrel.
        shaped(Ic2Items.STONE_MUG, 1, "stone_mug_from_booze_mug").pattern("#").define('#', Ic2Items.BOOZE_MUG)
                .save();
        shaped(Ic2Items.BOOZE_BARREL, 1, "booze_barrel_emptying").pattern("#").define('#', Ic2Items.BOOZE_BARREL)
                .save();
        shaped(Ic2Items.EU_METER, 1, "eu_meter").pattern(" G ", "cCc", "c c").define('G', Items.GLOWSTONE_DUST)
                .define('c', Ic2Items.cable(CableType.INSULATED_COPPER)).define('C', Ic2Items.ELECTRONIC_CIRCUIT).save();
        shaped(Ic2Items.FOAM_SPRAYER, 1, "foam_sprayer").pattern("SS ", "Ss ", "  S").define('S', Items.COBBLESTONE)
                .define('s', Items.STICK).damage(FoamSprayerItem.EMPTY_DAMAGE).save();
        gradualRepair("foam_sprayer_fill", Ic2Items.FOAM_SPRAYER.get(), Ingredient.of(Ic2Items.CONSTRUCTION_FOAM_PELLET),
                FoamSprayerItem.DAMAGE_PER_SPRAY);
        // 1.4.7 block ingredient = metadata 0 only, i.e. white wool.
        shaped(Ic2Items.PAINTER, 1, "painter").pattern(" CC", " IC", "I  ").define('C', Items.WHITE_WOOL)
                .define('I', Items.IRON_INGOT).save();
        for (int color = 0; color < 16; color++) {
            String name = Ic2Items.painter(color).getId().getPath();
            shapeless(Ic2Items.painter(color), 1, name, Ingredient.of(Ic2Items.PAINTER),
                    Ingredient.of(PainterItem.dyeTag(color)));
            // IC2 and GregTech both register the way back; one recipe is enough.
            shapeless(Ic2Items.PAINTER, 1, "painter_from_" + name, Ingredient.of(Ic2Items.painter(color)));
        }
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

    // --- §6 reactor (spec 06, 08 §6) ---

    private void reactor() {
        shaped(Ic2Items.REACTOR_CHAMBER, 1, "reactor_chamber").pattern(" C ", "CMC", " C ")
                .define('C', Ic2Items.DENSE_COPPER_PLATE).define('M', Ic2Items.MACHINE_CASING).save();
        shaped(machine(Ic2Machine.NUCLEAR_REACTOR), 1, "nuclear_reactor").pattern(" c ", "CCC", " G ")
                .define('c', Ic2Items.ADVANCED_CIRCUIT).define('C', Ic2Items.REACTOR_CHAMBER)
                .define('G', machine(Ic2Machine.GENERATOR)).save();
        // Uranium: ore/drop -> compressor -> ingot; storage block via compressor/furnace (GregTech).
        machine(MachineRecipe.Kind.COMPRESSOR, "uranium_ingot_from_drop", Ingredient.of(Ic2Items.URANIUM_DROP), 1,
                Ic2Items.URANIUM_INGOT, 1);
        machine(MachineRecipe.Kind.COMPRESSOR, "uranium_ingot_from_ore", Ingredient.of(Ic2Items.URANIUM_ORE,
                Ic2Items.DEEPSLATE_URANIUM_ORE), 1, Ic2Items.URANIUM_INGOT, 1);
        machine(MachineRecipe.Kind.COMPRESSOR, "uranium_ingot", Ingredient.of(CraftItemTagsProvider.INGOTS_URANIUM), 9,
                Ic2Items.URANIUM_BLOCK, 1);
        smelt("uranium_ingots_from_block", Ingredient.of(Ic2Items.URANIUM_BLOCK), Ic2Items.URANIUM_INGOT, 9, 0.0F);
        // Uranium cell from an ingot only in the GT canning machine (100 ticks, 2 EU/t).
        can("uranium_cell", SizedIngredient.of(CraftItemTagsProvider.INGOTS_URANIUM, 1),
                SizedIngredient.of(Ic2Items.EMPTY_CELL, 1), 100, 2, new ItemStack(Ic2Items.URANIUM_CELL.get()));
        shapeless(Ic2Items.URANIUM_CELL, 1, "uranium_cell_from_re_enriched",
                Ingredient.of(Ic2Items.RE_ENRICHED_URANIUM_CELL), Ingredient.of(CraftItemTagsProvider.DUSTS_COAL));
        output.accept(id("isotope_cell"), new Ic2ShapelessRecipe(isotopeCell(),
                NonNullList.of(Ingredient.EMPTY, Ingredient.of(Ic2Items.NEAR_DEPLETED_URANIUM_CELL),
                        Ingredient.of(CraftItemTagsProvider.DUSTS_COAL))), null);
        shaped(Ic2Items.NEAR_DEPLETED_URANIUM_CELL, 8, "near_depleted_uranium_cell").pattern("CCC", "CUC", "CCC")
                .define('C', Ic2Items.EMPTY_CELL).define('U', CraftItemTagsProvider.INGOTS_URANIUM).save();
        shaped(Ic2Items.DUAL_URANIUM_CELL, 1, "dual_uranium_cell").pattern("UCU")
                .define('U', fresh(Ic2Items.URANIUM_CELL)).define('C', Ic2Items.DENSE_COPPER_PLATE).save();
        shaped(Ic2Items.QUAD_URANIUM_CELL, 1, "quad_uranium_cell").pattern(" U ", "CCC", " U ")
                .define('U', fresh(Ic2Items.DUAL_URANIUM_CELL)).define('C', Ic2Items.DENSE_COPPER_PLATE).save();
        shaped(Ic2Items.COOLANT_CELL_30K, 1, "coolant_cell_30k").pattern("TTT", "CCC", "TTT")
                .define('C', fresh(Ic2Items.COOLANT_CELL_10K)).define('T', CraftItemTagsProvider.INGOTS_TIN).save();
        shaped(Ic2Items.COOLANT_CELL_60K, 1, "coolant_cell_60k").pattern("TCT", "TcT", "TCT")
                .define('C', fresh(Ic2Items.COOLANT_CELL_30K)).define('T', CraftItemTagsProvider.INGOTS_TIN)
                .define('c', Ic2Items.DENSE_COPPER_PLATE).save();
        shapeless(Ic2Items.REACTOR_PLATING, 1, "reactor_plating", Ingredient.of(Ic2Items.DENSE_COPPER_PLATE),
                Ingredient.of(Ic2Items.ADVANCED_ALLOY));
        shapeless(Ic2Items.HEAT_CAPACITY_REACTOR_PLATING, 1, "heat_capacity_reactor_plating",
                Ingredient.of(Ic2Items.REACTOR_PLATING), Ingredient.of(Ic2Items.DENSE_COPPER_PLATE),
                Ingredient.of(Ic2Items.DENSE_COPPER_PLATE));
        shapeless(Ic2Items.CONTAINMENT_REACTOR_PLATING, 1, "containment_reactor_plating",
                Ingredient.of(Ic2Items.REACTOR_PLATING), Ingredient.of(Ic2Items.ADVANCED_ALLOY),
                Ingredient.of(Ic2Items.ADVANCED_ALLOY));
        shaped(Ic2Items.HEAT_EXCHANGER, 1, "heat_exchanger").pattern(" c ", "TCT", " T ")
                .define('c', Ic2Items.ELECTRONIC_CIRCUIT).define('T', CraftItemTagsProvider.INGOTS_TIN)
                .define('C', Ic2Items.DENSE_COPPER_PLATE).save();
        shaped(Ic2Items.REACTOR_HEAT_EXCHANGER, 1, "reactor_heat_exchanger").pattern("C", "S", "C")
                .define('S', fresh(Ic2Items.HEAT_EXCHANGER)).define('C', Ic2Items.DENSE_COPPER_PLATE).save();
        shaped(Ic2Items.COMPONENT_HEAT_EXCHANGER, 1, "component_heat_exchanger").pattern(" G ", "GSG", " G ")
                .define('S', fresh(Ic2Items.HEAT_EXCHANGER)).define('G', Items.GOLD_INGOT).save();
        shaped(Ic2Items.ADVANCED_HEAT_EXCHANGER, 1, "advanced_heat_exchanger").pattern("GcG", "SCS", "GcG")
                .define('S', fresh(Ic2Items.HEAT_EXCHANGER)).define('C', Ic2Items.DENSE_COPPER_PLATE)
                .define('G', Ic2Items.cable(CableType.GLASS_FIBRE)).define('c', Ic2Items.ELECTRONIC_CIRCUIT).save();
        shaped(Ic2Items.HEAT_VENT, 1, "heat_vent").pattern("I#I", "# #", "I#I")
                .define('I', Ic2Items.REFINED_IRON_INGOT).define('#', Items.IRON_BARS).save();
        shaped(Ic2Items.REACTOR_HEAT_VENT, 1, "reactor_heat_vent").pattern("C", "V", "C")
                .define('V', fresh(Ic2Items.HEAT_VENT)).define('C', Ic2Items.DENSE_COPPER_PLATE).save();
        shaped(Ic2Items.OVERCLOCKED_HEAT_VENT, 1, "overclocked_heat_vent").pattern("G", "V", "G")
                .define('V', fresh(Ic2Items.REACTOR_HEAT_VENT)).define('G', Items.GOLD_INGOT).save();
        shaped(Ic2Items.COMPONENT_HEAT_VENT, 1, "component_heat_vent").pattern("#T#", "TVT", "#T#")
                .define('V', fresh(Ic2Items.HEAT_VENT)).define('#', Items.IRON_BARS)
                .define('T', CraftItemTagsProvider.INGOTS_TIN).save();
        shaped(Ic2Items.ADVANCED_HEAT_VENT, 1, "advanced_heat_vent").pattern("#V#", "#D#", "#V#")
                .define('V', fresh(Ic2Items.HEAT_VENT)).define('#', Items.IRON_BARS).define('D', Items.DIAMOND).save();
        shaped(Ic2Items.HEATING_CELL, 1, "heating_cell").pattern("c", "L", "C")
                .define('c', Ic2Items.ELECTRONIC_CIRCUIT).define('C', Ic2Items.DENSE_COPPER_PLATE)
                .define('L', Ic2Items.LAVA_CELL).save();
        shaped(Ic2Items.NEUTRON_REFLECTOR, 1, "neutron_reflector").pattern("TcT", "cCc", "TcT")
                .define('c', CraftItemTagsProvider.DUSTS_COAL).define('C', Ic2Items.DENSE_COPPER_PLATE)
                .define('T', CraftItemTagsProvider.DUSTS_TIN).save();
        shaped(Ic2Items.RSH_CONDENSATOR, 1, "rsh_condensator").pattern("RRR", "RVR", "RSR")
                .define('R', Items.REDSTONE).define('V', fresh(Ic2Items.HEAT_VENT))
                .define('S', fresh(Ic2Items.HEAT_EXCHANGER)).save();
        shaped(Ic2Items.LZH_CONDENSATOR, 1, "lzh_condensator").pattern("RVR", "CLC", "RSR")
                .define('R', Items.REDSTONE).define('V', fresh(Ic2Items.REACTOR_HEAT_VENT))
                .define('S', fresh(Ic2Items.REACTOR_HEAT_EXCHANGER)).define('C', fresh(Ic2Items.RSH_CONDENSATOR))
                .define('L', Items.LAPIS_BLOCK).save();
        gradualRepair("rsh_condensator_repair", Ic2Items.RSH_CONDENSATOR.get(), Ingredient.of(Items.REDSTONE), 10000);
        gradualRepair("lzh_condensator_repair_redstone", Ic2Items.LZH_CONDENSATOR.get(), Ingredient.of(Items.REDSTONE),
                5000);
        gradualRepair("lzh_condensator_repair_lapis", Ic2Items.LZH_CONDENSATOR.get(), Ingredient.of(Items.LAPIS_LAZULI),
                40000);
    }

    /** GregTech reactor parts reachable without GT ores (phase 7b). */
    private void reactorGregTech() {
        ItemStack none = ItemStack.EMPTY;
        // Uranium, thorium and plutonium dusts and ingots.
        machine(MachineRecipe.Kind.MACERATOR, "uranium_ore", Ingredient.of(Ic2Items.URANIUM_ORE,
                Ic2Items.DEEPSLATE_URANIUM_ORE), 1, GtItems.URANIUM_DUST, 2);
        machine(MachineRecipe.Kind.MACERATOR, "uranium_block", Ingredient.of(Ic2Items.URANIUM_BLOCK), 1,
                GtItems.URANIUM_DUST, 9);
        machine(MachineRecipe.Kind.MACERATOR, "uranium_ingot", Ingredient.of(CraftItemTagsProvider.INGOTS_URANIUM), 1,
                GtItems.URANIUM_DUST, 1);
        machine(MachineRecipe.Kind.MACERATOR, "thorium_ingot", Ingredient.of(GtItems.THORIUM_INGOT), 1,
                GtItems.THORIUM_DUST, 1);
        machine(MachineRecipe.Kind.MACERATOR, "plutonium_ingot", Ingredient.of(GtItems.PLUTONIUM_INGOT), 1,
                GtItems.PLUTONIUM_DUST, 1);
        machine(MachineRecipe.Kind.COMPRESSOR, "uranium_dust", Ingredient.of(GtItems.URANIUM_DUST), 1,
                Ic2Items.URANIUM_INGOT, 1);
        machine(MachineRecipe.Kind.COMPRESSOR, "thorium_dust", Ingredient.of(GtItems.THORIUM_DUST), 1,
                GtItems.THORIUM_INGOT, 1);
        machine(MachineRecipe.Kind.COMPRESSOR, "plutonium_dust", Ingredient.of(GtItems.PLUTONIUM_DUST), 1,
                GtItems.PLUTONIUM_INGOT, 1);
        smallDust("plutonium", GtItems.SMALL_PLUTONIUM_DUST, Ingredient.of(GtItems.PLUTONIUM_DUST),
                GtItems.PLUTONIUM_DUST);
        smallDust("tungsten", GtItems.SMALL_TUNGSTEN_DUST, Ingredient.of(GtItems.TUNGSTEN_DUST), GtItems.TUNGSTEN_DUST);
        centrifuge("near_depleted_uranium_cell", Ingredient.of(Ic2Items.NEAR_DEPLETED_URANIUM_CELL), 1, 0, 500,
                stack(GtItems.THORIUM_DUST, 1), none, none, stack(Ic2Items.EMPTY_CELL, 1));
        centrifuge("re_enriched_uranium_cell", Ingredient.of(Ic2Items.RE_ENRICHED_URANIUM_CELL), 8, 0, 20000,
                stack(Ic2Items.NEAR_DEPLETED_URANIUM_CELL, 3), stack(GtItems.PLUTONIUM_DUST, 1),
                stack(GtItems.THORIUM_DUST, 4), stack(Ic2Items.EMPTY_CELL, 5));
        centrifuge("uranium_dust", Ingredient.of(GtItems.URANIUM_DUST), 4, 4, 10000,
                stack(Ic2Items.URANIUM_CELL, 4), stack(GtItems.SMALL_PLUTONIUM_DUST, 1),
                stack(GtItems.THORIUM_DUST, 2), stack(GtItems.TUNGSTEN_DUST, 1));
        // Thorium and plutonium cells: canned from ingots, combined like the uranium cells.
        can("thorium_cell", SizedIngredient.of(GtItems.THORIUM_INGOT, 1), SizedIngredient.of(Ic2Items.EMPTY_CELL, 1),
                100, 2, new ItemStack(GtItems.THORIUM_CELL.get()));
        can("plutonium_cell", SizedIngredient.of(GtItems.PLUTONIUM_INGOT, 1),
                SizedIngredient.of(Ic2Items.EMPTY_CELL, 1), 100, 2, new ItemStack(GtItems.PLUTONIUM_CELL.get()));
        shaped(GtItems.DUAL_THORIUM_CELL, 1, "dual_thorium_cell").pattern("CPC")
                .define('C', fresh(GtItems.THORIUM_CELL)).define('P', CraftItemTagsProvider.PLATES_COPPER).save();
        shaped(GtItems.QUAD_THORIUM_CELL, 1, "quad_thorium_cell").pattern(" C ", "PPP", " C ")
                .define('C', fresh(GtItems.DUAL_THORIUM_CELL)).define('P', CraftItemTagsProvider.PLATES_COPPER).save();
        shaped(GtItems.DUAL_PLUTONIUM_CELL, 1, "dual_plutonium_cell").pattern("CPC")
                .define('C', fresh(GtItems.PLUTONIUM_CELL)).define('P', Ic2Items.DENSE_COPPER_PLATE).save();
        shaped(GtItems.QUAD_PLUTONIUM_CELL, 1, "quad_plutonium_cell").pattern(" C ", "PPP", " C ")
                .define('C', fresh(GtItems.DUAL_PLUTONIUM_CELL)).define('P', Ic2Items.DENSE_COPPER_PLATE).save();
        // Helium coolant cells (Helium2Coolantcell) and the cheaper overclocker with them.
        shaped(GtItems.HELIUM_COOLANT_CELL_60K, 1, "helium_coolant_cell_60k").pattern(" T ", "THT", " T ")
                .define('H', GtItems.HELIUM_CELL).define('T', CraftItemTagsProvider.INGOTS_TIN).save();
        shaped(GtItems.HELIUM_COOLANT_CELL_180K, 1, "helium_coolant_cell_180k").pattern("TTT", "HHH", "TTT")
                .define('H', fresh(GtItems.HELIUM_COOLANT_CELL_60K)).define('T', CraftItemTagsProvider.INGOTS_TIN)
                .save();
        shaped(GtItems.HELIUM_COOLANT_CELL_360K, 1, "helium_coolant_cell_360k").pattern("THT", "TCT", "THT")
                .define('H', fresh(GtItems.HELIUM_COOLANT_CELL_180K)).define('T', CraftItemTagsProvider.INGOTS_TIN)
                .define('C', Ic2Items.DENSE_COPPER_PLATE).save();
        shaped(Ic2Items.OVERCLOCKER_UPGRADE, 2, "overclocker_upgrade_from_helium_coolant").pattern(" H ", "WCW")
                .define('H', fresh(GtItems.HELIUM_COOLANT_CELL_60K))
                .define('W', Ic2Items.cable(CableType.INSULATED_COPPER)).define('C', Ic2Items.ELECTRONIC_CIRCUIT)
                .save();
        // Reflectors: GT replaces the thick reflector recipe (BeryliumReflector), adds the iridium reflector.
        shaped(Ic2Items.THICK_NEUTRON_REFLECTOR, 1, "thick_neutron_reflector").pattern(" N ", "NBN", " N ")
                .define('N', fresh(Ic2Items.NEUTRON_REFLECTOR)).define('B', GtItems.BERYLLIUM_CELL).save();
        shaped(GtItems.IRIDIUM_NEUTRON_REFLECTOR, 1, "iridium_neutron_reflector").pattern("NNN", "NIN", "NNN")
                .define('N', fresh(Ic2Items.THICK_NEUTRON_REFLECTOR)).define('I', Ic2Items.IRIDIUM_PLATE).save();
        // Beryllium from emerald dust (GT electrolyzer, 600 ticks at 50 EU/t).
        electrolysis("emerald_dust", Ingredient.of(GtItems.EMERALD_DUST), 29, 18, 600, 50,
                stack(GtItems.ALUMINIUM_DUST, 2), stack(GtItems.BERYLLIUM_CELL, 3), stack(GtItems.SILICON_CELL, 6),
                stack(Ic2Items.AIR_CELL, 9));
        // GT heat vent variant with aluminium plates.
        shaped(Ic2Items.HEAT_VENT, 1, "heat_vent_from_aluminium").pattern("AIA", "I I", "AIA")
                .define('A', CraftItemTagsProvider.PLATES_ALUMINIUM).define('I', Items.IRON_BARS).save();
    }

    /**
     * Phase 8a: NaK coolant cells (sodium from sodalite, potassium from ender pearl dust) and the lead plate for the
     * GT containment reactor plating (lead from silver dust or UU matter).
     */
    private void gregTechFollowUps() {
        // Sodium: lapis → centrifuge → small sodalite dust → sodalite dust → electrolyzer.
        smallDust("sodalite", GtItems.SMALL_SODALITE_DUST, Ingredient.of(GtItems.SODALITE_DUST), GtItems.SODALITE_DUST);
        electrolysis("sodalite_dust", Ingredient.of(GtItems.SODALITE_DUST), 23, 8, 1350, 90,
                stack(GtItems.SODIUM_CELL, 4), stack(GtItems.ALUMINIUM_DUST, 3), stack(GtItems.SILICON_CELL, 3),
                stack(GtItems.CHLORITE_CELL, 1));
        // Potassium: ender pearl → macerator → ender pearl dust → electrolyzer.
        machine(MachineRecipe.Kind.MACERATOR, "ender_pearl", Ingredient.of(Items.ENDER_PEARL), 1,
                GtItems.ENDER_PEARL_DUST, 1);
        electrolysis("ender_pearl_dust", Ingredient.of(GtItems.ENDER_PEARL_DUST), 16, 16, 1300, 50,
                stack(GtItems.NITROGEN_CELL, 5), stack(GtItems.BERYLLIUM_CELL, 1), stack(GtItems.POTASSIUM_CELL, 4),
                stack(GtItems.CHLORITE_CELL, 6));
        // NaK coolant cells (NaKCoolantcell) and the cheaper overclocker with them.
        shaped(GtItems.NAK_COOLANT_CELL_60K, 1, "nak_coolant_cell_60k").pattern("TNT", "KCK", "TNT")
                .define('N', GtItems.SODIUM_CELL).define('K', GtItems.POTASSIUM_CELL)
                .define('T', CraftItemTagsProvider.INGOTS_TIN).define('C', fresh(Ic2Items.COOLANT_CELL_10K)).save();
        shaped(GtItems.NAK_COOLANT_CELL_60K, 1, "nak_coolant_cell_60k_rotated").pattern("TKT", "NCN", "TKT")
                .define('N', GtItems.SODIUM_CELL).define('K', GtItems.POTASSIUM_CELL)
                .define('T', CraftItemTagsProvider.INGOTS_TIN).define('C', fresh(Ic2Items.COOLANT_CELL_10K)).save();
        shaped(GtItems.NAK_COOLANT_CELL_180K, 1, "nak_coolant_cell_180k").pattern("TTT", "HHH", "TTT")
                .define('H', fresh(GtItems.NAK_COOLANT_CELL_60K)).define('T', CraftItemTagsProvider.INGOTS_TIN)
                .save();
        shaped(GtItems.NAK_COOLANT_CELL_360K, 1, "nak_coolant_cell_360k").pattern("THT", "TCT", "THT")
                .define('H', fresh(GtItems.NAK_COOLANT_CELL_180K)).define('T', CraftItemTagsProvider.INGOTS_TIN)
                .define('C', Ic2Items.DENSE_COPPER_PLATE).save();
        shaped(Ic2Items.OVERCLOCKER_UPGRADE, 2, "overclocker_upgrade_from_nak_coolant").pattern(" H ", "WCW")
                .define('H', fresh(GtItems.NAK_COOLANT_CELL_60K))
                .define('W', Ic2Items.cable(CableType.INSULATED_COPPER)).define('C', Ic2Items.ELECTRONIC_CIRCUIT)
                .save();
        // Lead: 2 silver dust → small lead dust (centrifuge, in centrifuge()) → lead dust → ingot → plate.
        smallDust("lead", GtItems.SMALL_LEAD_DUST, Ingredient.of(CraftItemTagsProvider.DUSTS_LEAD), GtItems.LEAD_DUST);
        smallDust("silver", GtItems.SMALL_SILVER_DUST, Ingredient.of(CraftItemTagsProvider.DUSTS_SILVER),
                Ic2Items.SILVER_DUST);
        centrifuge("lead_dust", Ingredient.of(CraftItemTagsProvider.DUSTS_LEAD), 2, 0, 2400,
                stack(GtItems.SMALL_SILVER_DUST, 1));
        smelt("lead_ingot_from_dust", Ingredient.of(CraftItemTagsProvider.DUSTS_LEAD), GtItems.LEAD_INGOT, 1, 0.0F);
        machine(MachineRecipe.Kind.MACERATOR, "lead_ingot", Ingredient.of(CraftItemTagsProvider.INGOTS_LEAD), 1,
                GtItems.LEAD_DUST, 1);
        plate("lead", CraftItemTagsProvider.INGOTS_LEAD, GtItems.LEAD_PLATE, GtItems.LEAD_DUST, GtItems.LEAD_INGOT);
        shapeless(Ic2Items.CONTAINMENT_REACTOR_PLATING, 1, "containment_reactor_plating_from_lead",
                Ingredient.of(Ic2Items.REACTOR_PLATING), Ingredient.of(CraftItemTagsProvider.PLATES_LEAD));
    }

    /**
     * Phase 8c: processing of the GT ores (spec 07 §6.4) and the {@code GT_OreDictHandler} recipes of their dusts and
     * gems whose outputs exist. Industrial grinder recipes follow with 8d.
     */
    private void gregTechOres() {
        gtOreMacerator(GtOre.GALENA, GtItems.GALENA_DUST, 2);
        gtOreMacerator(GtOre.IRIDIUM, Ic2Items.IRIDIUM_ORE, 2);
        gtOreMacerator(GtOre.RUBY, GtItems.RUBY_DUST, 2);
        gtOreMacerator(GtOre.SAPPHIRE, GtItems.SAPPHIRE_DUST, 2);
        gtOreMacerator(GtOre.BAUXITE, GtItems.BAUXITE_DUST, 4);
        gtOreMacerator(GtOre.PYRITE, GtItems.PYRITE_DUST, 5);
        gtOreMacerator(GtOre.CINNABAR, GtItems.CINNABAR_DUST, 3);
        gtOreMacerator(GtOre.SPHALERITE, GtItems.SPHALERITE_DUST, 4);
        gtOreMacerator(GtOre.TUNGSTATE, GtItems.TUNGSTEN_DUST, 2);
        gtOreMacerator(GtOre.SHELDONITE, GtItems.PLATINUM_DUST, 2);
        gtOreMacerator(GtOre.OLIVINE, GtItems.OLIVINE_DUST, 2);
        gtOreMacerator(GtOre.SODALITE, GtItems.SODALITE_DUST, 12);
        // Gems → 1 dust (addPulverisationRecipe), platinum ingot → dust.
        machine(MachineRecipe.Kind.MACERATOR, "ruby", Ingredient.of(GtItems.RUBY), 1, GtItems.RUBY_DUST, 1);
        machine(MachineRecipe.Kind.MACERATOR, "sapphire", Ingredient.of(GtItems.SAPPHIRE), 1, GtItems.SAPPHIRE_DUST, 1);
        machine(MachineRecipe.Kind.MACERATOR, "green_sapphire", Ingredient.of(GtItems.GREEN_SAPPHIRE), 1,
                GtItems.GREEN_SAPPHIRE_DUST, 1);
        machine(MachineRecipe.Kind.MACERATOR, "olivine", Ingredient.of(GtItems.OLIVINE), 1, GtItems.OLIVINE_DUST, 1);
        machine(MachineRecipe.Kind.MACERATOR, "red_garnet", Ingredient.of(GtItems.RED_GARNET), 1,
                GtItems.RED_GARNET_DUST, 1);
        machine(MachineRecipe.Kind.MACERATOR, "yellow_garnet", Ingredient.of(GtItems.YELLOW_GARNET), 1,
                GtItems.YELLOW_GARNET_DUST, 1);
        machine(MachineRecipe.Kind.MACERATOR, "platinum_ingot", Ingredient.of(CraftItemTagsProvider.INGOTS_PLATINUM), 1,
                GtItems.PLATINUM_DUST, 1);

        // Furnace: pyrite → iron, sheldonite → platinum, tungstate → tungsten dust (tungsten needs the blast furnace).
        smelt("iron_ingot_from_pyrite_ore", gtOre(GtOre.PYRITE), Items.IRON_INGOT, 1, 0.0F);
        smelt("platinum_ingot_from_sheldonite_ore", gtOre(GtOre.SHELDONITE), GtItems.PLATINUM_INGOT, 1, 0.0F);
        smelt("tungsten_dust_from_tungstate_ore", gtOre(GtOre.TUNGSTATE), GtItems.TUNGSTEN_DUST, 1, 0.0F);
        smelt("platinum_ingot_from_dust", Ingredient.of(CraftItemTagsProvider.DUSTS_PLATINUM), GtItems.PLATINUM_INGOT, 1,
                0.0F);

        // Blast furnace: ore + calcium carbonate cell → refined iron.
        blast("refined_iron_from_iron_ore", SizedIngredient.of(Items.IRON_ORE, 1), SizedIngredient.of(
                GtItems.CALCIUM_CARBONATE_CELL.get(), 1), 100, 1000, stack(Ic2Items.REFINED_IRON_INGOT, 3),
                stack(Ic2Items.EMPTY_CELL, 1));
        blast("refined_iron_from_deepslate_iron_ore", SizedIngredient.of(Items.DEEPSLATE_IRON_ORE, 1),
                SizedIngredient.of(GtItems.CALCIUM_CARBONATE_CELL.get(), 1), 100, 1000,
                stack(Ic2Items.REFINED_IRON_INGOT, 3), stack(Ic2Items.EMPTY_CELL, 1));
        blast("refined_iron_from_pyrite_ore", new SizedIngredient(gtOre(GtOre.PYRITE), 1), SizedIngredient.of(
                GtItems.CALCIUM_CARBONATE_CELL.get(), 1), 100, 1500, stack(Ic2Items.REFINED_IRON_INGOT, 2),
                stack(Ic2Items.EMPTY_CELL, 1));

        // Calcite: small dust (centrifuge byproduct) ↔ dust ↔ calcium carbonate cell, electrolysis.
        smallDust("calcite", GtItems.SMALL_CALCITE_DUST, Ingredient.of(CraftItemTagsProvider.DUSTS_CALCITE),
                GtItems.CALCITE_DUST);
        can("calcium_carbonate_cell", SizedIngredient.of(GtItems.CALCITE_DUST.get(), 1),
                SizedIngredient.of(Ic2Items.EMPTY_CELL.get(), 1), 100, stack(GtItems.CALCIUM_CARBONATE_CELL, 1));
        centrifuge("calcium_carbonate_cell", Ingredient.of(GtItems.CALCIUM_CARBONATE_CELL), 1, 0, 40,
                stack(GtItems.CALCITE_DUST, 1), ItemStack.EMPTY, ItemStack.EMPTY, stack(Ic2Items.EMPTY_CELL, 1));
        electrolysis("calcite_dust", Ingredient.of(CraftItemTagsProvider.DUSTS_CALCITE), 10, 7, 700, 80,
                stack(GtItems.CALCIUM_CELL, 2), stack(GtItems.CARBON_CELL, 2), stack(Ic2Items.AIR_CELL, 3));

        // Sulfur and tungsten cells (GT_OreDictHandler dustSulfur/dustTungsten, molecule_1s); further uses need the
        // chemical/fusion reactor (not in scope).
        can("sulfur_cell", SizedIngredient.of(GtItems.SULFUR_DUST.get(), 1),
                SizedIngredient.of(Ic2Items.EMPTY_CELL.get(), 1), 100, stack(GtItems.SULFUR_CELL, 1));
        centrifuge("sulfur_cell", Ingredient.of(GtItems.SULFUR_CELL), 1, 0, 40,
                stack(GtItems.SULFUR_DUST, 1), ItemStack.EMPTY, ItemStack.EMPTY, stack(Ic2Items.EMPTY_CELL, 1));
        can("tungsten_cell", SizedIngredient.of(GtItems.TUNGSTEN_DUST.get(), 1),
                SizedIngredient.of(Ic2Items.EMPTY_CELL.get(), 1), 100, stack(GtItems.TUNGSTEN_CELL, 1));

        // Nether ore dusts.
        electrolysis("sphalerite_dust", Ingredient.of(GtItems.SPHALERITE_DUST), 2, 0, 150, 100,
                stack(GtItems.ZINC_DUST, 1), stack(GtItems.SULFUR_DUST, 1));
        electrolysis("cinnabar_dust", Ingredient.of(GtItems.CINNABAR_DUST), 2, 1, 100, 128,
                stack(GtItems.MERCURY_CELL, 1), stack(GtItems.SULFUR_DUST, 1));
        electrolysis("galena_dust", Ingredient.of(GtItems.GALENA_DUST), 2, 0, 1000, 120,
                stack(GtItems.SMALL_SILVER_DUST, 3), stack(GtItems.SMALL_LEAD_DUST, 3), stack(GtItems.SMALL_SULFUR_DUST, 2));
        smallDust("zinc", GtItems.SMALL_ZINC_DUST, Ingredient.of(CraftItemTagsProvider.DUSTS_ZINC), GtItems.ZINC_DUST);
        centrifuge("zinc_dust", Ingredient.of(CraftItemTagsProvider.DUSTS_ZINC), 1, 0, 1050,
                stack(GtItems.SMALL_TIN_DUST, 1));
        smallDust("sulfur", GtItems.SMALL_SULFUR_DUST, Ingredient.of(GtItems.SULFUR_DUST), GtItems.SULFUR_DUST);

        // Gem dusts.
        electrolysis("sapphire_dust", Ingredient.of(GtItems.SAPPHIRE_DUST), 8, 3, 400, 50,
                stack(GtItems.ALUMINIUM_DUST, 2), stack(Ic2Items.AIR_CELL, 3));
        electrolysis("green_sapphire_dust", Ingredient.of(GtItems.GREEN_SAPPHIRE_DUST), 1, 0, 100, 50,
                stack(GtItems.SAPPHIRE_DUST, 1));
        implosion("ruby", GtItems.RUBY_DUST, 4, 24, stack(GtItems.RUBY, 3), stack(GtItems.DARK_ASH, 12));
        implosion("sapphire", GtItems.SAPPHIRE_DUST, 4, 24, stack(GtItems.SAPPHIRE, 3), stack(GtItems.DARK_ASH, 12));
        implosion("green_sapphire", GtItems.GREEN_SAPPHIRE_DUST, 4, 24, stack(GtItems.GREEN_SAPPHIRE, 3),
                stack(GtItems.DARK_ASH, 12));
        implosion("olivine", GtItems.OLIVINE_DUST, 4, 24, stack(GtItems.OLIVINE, 3), stack(GtItems.DARK_ASH, 12));
        implosion("red_garnet", GtItems.RED_GARNET_DUST, 4, 16, stack(GtItems.RED_GARNET, 3),
                stack(GtItems.DARK_ASH, 8));
        implosion("yellow_garnet", GtItems.YELLOW_GARNET_DUST, 4, 16, stack(GtItems.YELLOW_GARNET, 3),
                stack(GtItems.DARK_ASH, 8));
    }

    /** Materials unlocked by the GT ores (spec 12 §5.2, phase 8e.1). */
    private void gregTechUnlocks() {
        // Silver and zinc: blast furnace galena, furnace dust, macerator ingot, bender.
        blast("silver_and_lead_ingot_from_galena", SizedIngredient.of(GtItems.GALENA_DUST.get(), 2), null, 20, 1500,
                stack(GtItems.SILVER_INGOT, 1), stack(GtItems.LEAD_INGOT, 1));
        smelt("silver_ingot_from_dust", Ingredient.of(CraftItemTagsProvider.DUSTS_SILVER), GtItems.SILVER_INGOT, 1, 0.0F);
        machine(MachineRecipe.Kind.MACERATOR, "silver_ingot", Ingredient.of(CraftItemTagsProvider.INGOTS_SILVER), 1,
                Ic2Items.SILVER_DUST, 1);
        smelt("zinc_ingot_from_dust", Ingredient.of(CraftItemTagsProvider.DUSTS_ZINC), GtItems.ZINC_INGOT, 1, 0.0F);
        machine(MachineRecipe.Kind.MACERATOR, "zinc_ingot", Ingredient.of(CraftItemTagsProvider.INGOTS_ZINC), 1,
                GtItems.ZINC_DUST, 1);
        plate("zinc", CraftItemTagsProvider.INGOTS_ZINC, GtItems.ZINC_PLATE, GtItems.ZINC_DUST, GtItems.ZINC_INGOT);
        plate("silver", CraftItemTagsProvider.INGOTS_SILVER, GtItems.SILVER_PLATE, Ic2Items.SILVER_DUST,
                GtItems.SILVER_INGOT);

        // Chrome, magnesium, titanium from gem and bauxite dusts.
        electrolysis("ruby_dust", Ingredient.of(GtItems.RUBY_DUST), 9, 3, 500, 50,
                stack(GtItems.ALUMINIUM_DUST, 2), stack(GtItems.CHROME_DUST, 1), stack(Ic2Items.AIR_CELL, 3));
        electrolysis("olivine_dust", Ingredient.of(GtItems.OLIVINE_DUST), 9, 3, 600, 60,
                stack(GtItems.MAGNESIUM_DUST, 2), stack(Ic2Items.IRON_DUST, 2), stack(GtItems.SILICON_CELL, 1),
                stack(Ic2Items.AIR_CELL, 2));
        electrolysis("bauxite_dust", Ingredient.of(GtItems.BAUXITE_DUST), 12, 8, 2000, 128,
                stack(GtItems.ALUMINIUM_DUST, 8), stack(GtItems.SMALL_TITANIUM_DUST, 2),
                stack(GtItems.HYDROGEN_CELL, 5), stack(Ic2Items.AIR_CELL, 3));

        // Garnets → sub-dusts (centrifuge) → electrolysis.
        centrifuge("red_garnet_dust", Ingredient.of(GtItems.RED_GARNET_DUST), 16, 0, 3000,
                stack(GtItems.PYROPE_DUST, 3), stack(GtItems.ALMANDINE_DUST, 5), stack(GtItems.SPESSARTINE_DUST, 8));
        centrifuge("yellow_garnet_dust", Ingredient.of(GtItems.YELLOW_GARNET_DUST), 16, 0, 3500,
                stack(GtItems.ANDRADITE_DUST, 5), stack(GtItems.GROSSULAR_DUST, 8), stack(GtItems.UVAROVITE_DUST, 3));
        electrolysis("pyrope_dust", Ingredient.of(GtItems.PYROPE_DUST), 20, 9, 1790, 50,
                stack(GtItems.MAGNESIUM_DUST, 3), stack(GtItems.ALUMINIUM_DUST, 2), stack(GtItems.SILICON_CELL, 3),
                stack(Ic2Items.AIR_CELL, 6));
        electrolysis("almandine_dust", Ingredient.of(GtItems.ALMANDINE_DUST), 20, 9, 1640, 50,
                stack(Ic2Items.IRON_DUST, 3), stack(GtItems.ALUMINIUM_DUST, 2), stack(GtItems.SILICON_CELL, 3),
                stack(Ic2Items.AIR_CELL, 6));
        electrolysis("spessartine_dust", Ingredient.of(GtItems.SPESSARTINE_DUST), 20, 9, 1810, 50,
                stack(GtItems.ALUMINIUM_DUST, 2), stack(GtItems.MANGANESE_DUST, 3), stack(GtItems.SILICON_CELL, 3),
                stack(Ic2Items.AIR_CELL, 6));
        electrolysis("andradite_dust", Ingredient.of(GtItems.ANDRADITE_DUST), 20, 12, 1280, 50,
                stack(GtItems.CALCIUM_CELL, 3), stack(Ic2Items.IRON_DUST, 2), stack(GtItems.SILICON_CELL, 3),
                stack(Ic2Items.AIR_CELL, 6));
        electrolysis("grossular_dust", Ingredient.of(GtItems.GROSSULAR_DUST), 20, 12, 2050, 50,
                stack(GtItems.CALCIUM_CELL, 3), stack(GtItems.ALUMINIUM_DUST, 2), stack(GtItems.SILICON_CELL, 3),
                stack(Ic2Items.AIR_CELL, 6));
        electrolysis("uvarovite_dust", Ingredient.of(GtItems.UVAROVITE_DUST), 20, 12, 2200, 50,
                stack(GtItems.CALCIUM_CELL, 3), stack(GtItems.CHROME_DUST, 2), stack(GtItems.SILICON_CELL, 3),
                stack(Ic2Items.AIR_CELL, 6));

        // Titanium and chrome: blast furnace only (basicmetalblastfurnacerequirement), macerator back, bender.
        blast("titanium_ingot", SizedIngredient.of(GtItems.TITANIUM_DUST.get(), 1), null, 1000, 1500,
                stack(GtItems.TITANIUM_INGOT, 1));
        blast("chrome_ingot", SizedIngredient.of(GtItems.CHROME_DUST.get(), 1), null, 800, 1700,
                stack(GtItems.CHROME_INGOT, 1));
        machine(MachineRecipe.Kind.MACERATOR, "titanium_ingot", Ingredient.of(CraftItemTagsProvider.INGOTS_TITANIUM), 1,
                GtItems.TITANIUM_DUST, 1);
        machine(MachineRecipe.Kind.MACERATOR, "chrome_ingot", Ingredient.of(CraftItemTagsProvider.INGOTS_CHROME), 1,
                GtItems.CHROME_DUST, 1);
        plate("titanium", CraftItemTagsProvider.INGOTS_TITANIUM, GtItems.TITANIUM_PLATE, GtItems.TITANIUM_DUST,
                GtItems.TITANIUM_INGOT);
        plate("chrome", CraftItemTagsProvider.INGOTS_CHROME, GtItems.CHROME_PLATE, GtItems.CHROME_DUST,
                GtItems.CHROME_INGOT);
        plate("platinum", CraftItemTagsProvider.INGOTS_PLATINUM, GtItems.PLATINUM_PLATE, GtItems.PLATINUM_DUST,
                GtItems.PLATINUM_INGOT);

        // Small dusts 4 ↔ 1 (manganese dust exists now).
        smallDust("magnesium", GtItems.SMALL_MAGNESIUM_DUST, Ingredient.of(CraftItemTagsProvider.DUSTS_MAGNESIUM),
                GtItems.MAGNESIUM_DUST);
        smallDust("titanium", GtItems.SMALL_TITANIUM_DUST, Ingredient.of(CraftItemTagsProvider.DUSTS_TITANIUM),
                GtItems.TITANIUM_DUST);
        smallDust("chrome", GtItems.SMALL_CHROME_DUST, Ingredient.of(CraftItemTagsProvider.DUSTS_CHROME),
                GtItems.CHROME_DUST);
        smallDust("manganese", GtItems.SMALL_MANGANESE_DUST, Ingredient.of(CraftItemTagsProvider.DUSTS_MANGANESE),
                GtItems.MANGANESE_DUST);

        // Magnalium plate: rolling machine recipe, crafting table without Railcraft.
        shaped(GtItems.MAGNALIUM_PLATE, 3, "magnalium_plate").pattern("AAA", "MMM", "AAA")
                .define('A', CraftItemTagsProvider.PLATES_ALUMINIUM).define('M', CraftItemTagsProvider.DUSTS_MAGNESIUM)
                .save();
    }

    /** Recipes of existing items unlocked by the GT ore materials (spec 12 §5.3, phase 8e.2). */
    private void gregTechUnlockedRecipes() {
        // TitaniumDiamondDrill=true: the diamond drill (and with it the quarry) needs titanium plates.
        shaped(Ic2Items.DIAMOND_DRILL, 1, "diamond_drill").pattern(" D ", "DMD", "TAT").define('D', diamond())
                .define('M', Ic2Items.MINING_DRILL).define('T', CraftItemTagsProvider.PLATES_TITANIUM)
                .define('A', Ic2Items.ADVANCED_CIRCUIT).save();
        // ExpensiveLaser=true with Helium2Coolantcell=true.
        shaped(Ic2Items.MINING_LASER, 1, "mining_laser").pattern("RHE", "TTC", " AA")
                .define('R', CraftItemTagsProvider.GEMS_RUBY).define('H', GtItems.HELIUM_COOLANT_CELL_60K)
                .define('E', CraftItemTagsProvider.CRAFTING_100K_EU_STORE)
                .define('T', CraftItemTagsProvider.PLATES_TITANIUM).define('C', Ic2Items.ADVANCED_CIRCUIT)
                .define('A', Ic2Items.ADVANCED_ALLOY).save();

        // Heating coils: rolling machine recipes, crafting table without Railcraft.
        shaped(GtItems.KANTHAL_HEATING_COIL, 3, "kanthal_heating_coil").pattern("AAA", "BCC", "BBC")
                .define('A', CraftItemTagsProvider.INGOTS_REFINED_IRON).define('B', CraftItemTagsProvider.INGOTS_CHROME)
                .define('C', CraftItemTagsProvider.INGOTS_ALUMINIUM).save();
        shaped(GtItems.NICHROME_HEATING_COIL, 1, "nichrome_heating_coil").pattern(" B ", "BAB", " B ")
                .define('A', CraftItemTagsProvider.INGOTS_CHROME).define('B', CraftItemTagsProvider.INGOTS_NICKEL)
                .save();

        mixedMetal("titanium_bronze_tin", CraftItemTagsProvider.PLATES_TITANIUM, CraftItemTagsProvider.PLATES_TIN, 3);
        mixedMetal("titanium_bronze_zinc", CraftItemTagsProvider.PLATES_TITANIUM, CraftItemTagsProvider.PLATES_ZINC, 3);
        mixedMetal("titanium_bronze_aluminium", CraftItemTagsProvider.PLATES_TITANIUM,
                CraftItemTagsProvider.PLATES_ALUMINIUM, 4);
        mixedMetal("refined_iron_bronze_zinc", CraftItemTagsProvider.PLATES_REFINED_IRON,
                CraftItemTagsProvider.PLATES_ZINC, 1);
        mixedMetal("nickel_bronze_zinc", CraftItemTagsProvider.PLATES_NICKEL, CraftItemTagsProvider.PLATES_ZINC, 1);
        mixedMetal("steel_bronze_zinc", CraftItemTagsProvider.PLATES_STEEL, CraftItemTagsProvider.PLATES_ZINC, 2);

        for (String metal : new String[]{"aluminium", "steel", "titanium"}) {
            TagKey<Item> ingots = switch (metal) {
                case "aluminium" -> CraftItemTagsProvider.INGOTS_ALUMINIUM;
                case "steel" -> CraftItemTagsProvider.INGOTS_STEEL;
                default -> CraftItemTagsProvider.INGOTS_TITANIUM;
            };
            shaped(Items.PISTON, 1, "piston_from_" + metal).pattern("WWW", "CBC", "CRC").define('W', ItemTags.PLANKS)
                    .define('C', Items.COBBLESTONE).define('B', ingots).define('R', Items.REDSTONE).save();
        }

        // Ruby2Energycrystal, Sapphire2Lapotron.
        shaped(Ic2Items.ENERGY_CRYSTAL, 1, "energy_crystal_from_ruby").pattern("DDD", "DRD", "DDD")
                .define('D', Items.REDSTONE).define('R', CraftItemTagsProvider.GEMS_RUBY).save();
        shaped(Ic2Items.LAPOTRON_CRYSTAL, 1, "lapotron_crystal_from_sapphire").pattern("LCL", "LSL", "LCL")
                .define('L', CraftItemTagsProvider.ITEM_LAZURITE).define('C', Ic2Items.ELECTRONIC_CIRCUIT)
                .define('S', CraftItemTagsProvider.GEMS_SAPPHIRE).save();
        shaped(Ic2Items.cable(CableType.GLASS_FIBRE), 6, "glass_fibre_cable_from_silver").pattern("GGG", "XDX", "GGG")
                .define('G', Items.GLASS).define('X', CraftItemTagsProvider.INGOTS_SILVER).define('D', diamond()).save();
        assemble("wind_mill_from_magnalium", SizedIngredient.of(machine(Ic2Machine.GENERATOR), 1),
                SizedIngredient.of(CraftItemTagsProvider.PLATES_MAGNALIUM, 2),
                new ItemStack(machine(Ic2Machine.WIND_MILL)), 6400, 8);
    }

    /** GT circuits, hulls and the highly advanced machine block with what they unlock (spec 12 §5.2/5.3, phase 8e.3). */
    private void gregTechCircuits() {
        SizedIngredient advancedCircuit = SizedIngredient.of(Ic2Items.ADVANCED_CIRCUIT, 1);
        assemble("data_storage_circuit_from_olivine", SizedIngredient.of(CraftItemTagsProvider.GEMS_OLIVINE, 8),
                advancedCircuit, new ItemStack(GtItems.DATA_STORAGE_CIRCUIT.get(), 4), 6400, 8);
        assemble("data_storage_circuit_from_olivine_dust", SizedIngredient.of(GtItems.OLIVINE_DUST, 8),
                advancedCircuit, new ItemStack(GtItems.DATA_STORAGE_CIRCUIT.get(), 4), 6400, 8);
        assemble("data_storage_circuit_from_emerald", SizedIngredient.of(Items.EMERALD, 8), advancedCircuit,
                new ItemStack(GtItems.DATA_STORAGE_CIRCUIT.get(), 4), 3200, 4);
        assemble("data_storage_circuit_from_emerald_dust", SizedIngredient.of(GtItems.EMERALD_DUST, 8),
                advancedCircuit, new ItemStack(GtItems.DATA_STORAGE_CIRCUIT.get(), 4), 6400, 8);
        assemble("elite_circuit_board", SizedIngredient.of(CraftItemTagsProvider.PLATES_PLATINUM, 1), advancedCircuit,
                new ItemStack(GtItems.ELITE_CIRCUIT_BOARD.get()), 3200, 4);
        SizedIngredient eliteBoard = SizedIngredient.of(GtItems.ELITE_CIRCUIT_BOARD, 1);
        assemble("data_control_circuit", eliteBoard, SizedIngredient.of(GtItems.DATA_STORAGE_CIRCUIT, 1),
                new ItemStack(GtItems.DATA_CONTROL_CIRCUIT.get()), 3200, 4);
        // Any charge: the ingredient ignores the stored energy.
        assemble("energy_flow_circuit", eliteBoard, SizedIngredient.of(Ic2Items.LAPOTRON_CRYSTAL, 1),
                new ItemStack(GtItems.ENERGY_FLOW_CIRCUIT.get()), 3200, 4);
        shaped(GtItems.DATA_CONTROL_CIRCUIT, 4, "data_control_circuit").pattern("AEA", "EIE", "AEA")
                .define('A', Ic2Items.ADVANCED_CIRCUIT).define('E', GtItems.DATA_STORAGE_CIRCUIT)
                .define('I', Ic2Items.IRIDIUM_PLATE).save();

        machineHull("titanium_machine_hull", CraftItemTagsProvider.PLATES_TITANIUM, GtItems.TITANIUM_MACHINE_HULL);
        shaped(GtItems.HIGHLY_ADVANCED_MACHINE_BLOCK, 1, "highly_advanced_machine_block").pattern("CTC", "TMT", "CTC")
                .define('C', CraftItemTagsProvider.PLATES_CHROME).define('T', CraftItemTagsProvider.PLATES_TITANIUM)
                .define('M', CraftItemTagsProvider.RAW_MACHINE_TIER_02).save();
        shaped(GtItems.casing(GtCasingBlock.Type.ADVANCED), 4, "gt_advanced_machine_casing").pattern("TTT", "CMC", "TTT")
                .define('T', CraftItemTagsProvider.PLATES_CHROME).define('C', GtItems.DATA_CONTROL_CIRCUIT)
                .define('M', GtItems.HIGHLY_ADVANCED_MACHINE_BLOCK).save();

        // craftingCircuitTier07 = energy flow circuit; the lapotron crystal's charge goes into the saber.
        shaped(Ic2Items.NANO_SABER, 1, "nano_saber").pattern("PI ", "PI ", "CLC")
                .define('P', CraftItemTagsProvider.PLATES_PLATINUM).define('I', Ic2Items.IRIDIUM_PLATE)
                .define('L', Ic2Items.LAPOTRON_CRYSTAL).define('C', GtItems.ENERGY_FLOW_CIRCUIT).save();
    }

    /** Tungsten chain and vacuum freezer (spec 12 §5.6, spec 13 §11, phase 8e.4). */
    /**
     * Phase 13.3: GT dusts reachable without other mods (flint, wood pulp, charcoal, obsidian, electrum) with their
     * macerator sources ({@code GT_Mod} 1670–1684, 2019, 4738–4750, {@code GT_OreDictHandler} plank/slab/sapling),
     * uses and the GT gunpowder recipes. Only the main macerator output, as before.
     */
    private void gregTechDusts() {
        // Flint: 2 tiny piles from flint (10 % third pile = secondary output), electrolysis, 5 ITNT.
        machine(MachineRecipe.Kind.MACERATOR, "flint", Ingredient.of(Items.FLINT), 1, GtItems.SMALL_FLINT_DUST, 2);
        smallDust("flint", GtItems.SMALL_FLINT_DUST, Ingredient.of(GtItems.FLINT_DUST), GtItems.FLINT_DUST);
        electrolysis("flint_dust", Ingredient.of(GtItems.FLINT_DUST), 8, 2, 1000, 5,
                new ItemStack(GtItems.SILICON_CELL.get()), new ItemStack(Ic2Items.AIR_CELL.get()));
        shaped(Ic2Items.INDUSTRIAL_TNT, 5, "industrial_tnt_from_flint_dust").pattern("FFF", "TTT", "FFF")
                .define('F', GtItems.FLINT_DUST).define('T', Items.TNT).save();

        // Wood pulp: plankWood → 1, slabWood/treeSapling/stick → 2 tiny piles, wooden items (1.21 tags).
        machine(MachineRecipe.Kind.MACERATOR, "planks", Ingredient.of(ItemTags.PLANKS), 1, GtItems.WOOD_PULP, 1);
        machine(MachineRecipe.Kind.MACERATOR, "wooden_slab", Ingredient.of(ItemTags.WOODEN_SLABS), 1,
                GtItems.SMALL_WOOD_PULP, 2);
        machine(MachineRecipe.Kind.MACERATOR, "sapling", Ingredient.of(ItemTags.SAPLINGS), 1,
                GtItems.SMALL_WOOD_PULP, 2);
        machine(MachineRecipe.Kind.MACERATOR, "stick", Ingredient.of(Items.STICK), 1, GtItems.SMALL_WOOD_PULP, 2);
        machine(MachineRecipe.Kind.MACERATOR, "crop", Ingredient.of(Ic2Items.CROP), 1, GtItems.WOOD_PULP, 1);
        machine(MachineRecipe.Kind.MACERATOR, "sign", Ingredient.of(ItemTags.SIGNS), 1, GtItems.WOOD_PULP, 2);
        // 1.21 makes 3 wooden doors from 6 planks (1.4.7: 1 door, 6 pulp), so a door gives 2 like the iron door.
        machine(MachineRecipe.Kind.MACERATOR, "wooden_door", Ingredient.of(ItemTags.WOODEN_DOORS), 1,
                GtItems.WOOD_PULP, 2);
        machine(MachineRecipe.Kind.MACERATOR, "chest", Ingredient.of(Items.CHEST), 1, GtItems.WOOD_PULP, 8);
        machine(MachineRecipe.Kind.MACERATOR, "wooden_button", Ingredient.of(ItemTags.WOODEN_BUTTONS), 1,
                GtItems.WOOD_PULP, 1);
        machine(MachineRecipe.Kind.MACERATOR, "wooden_pressure_plate", Ingredient.of(ItemTags.WOODEN_PRESSURE_PLATES),
                1, GtItems.WOOD_PULP, 2);
        machine(MachineRecipe.Kind.MACERATOR, "redstone_torch", Ingredient.of(Items.REDSTONE_TORCH), 1,
                GtItems.SMALL_WOOD_PULP, 2);
        machine(MachineRecipe.Kind.MACERATOR, "ladder", Ingredient.of(Items.LADDER), 1, GtItems.WOOD_PULP, 1);
        smallDust("wood", GtItems.SMALL_WOOD_PULP, Ingredient.of(GtItems.WOOD_PULP), GtItems.WOOD_PULP);
        machine(MachineRecipe.Kind.COMPRESSOR, "wood_pulp", Ingredient.of(GtItems.WOOD_PULP), 8, GtItems.WOOD_PLATE,
                1);

        // Charcoal dust: macerator, electrolysis → carbon cell, gunpowder (and the coal dust variant).
        machine(MachineRecipe.Kind.MACERATOR, "charcoal", Ingredient.of(Items.CHARCOAL), 1, GtItems.CHARCOAL_DUST, 1);
        electrolysis("charcoal_dust", Ingredient.of(GtItems.CHARCOAL_DUST), 1, 1, 20, 50,
                new ItemStack(GtItems.CARBON_CELL.get()));
        Ingredient sulfur = Ingredient.of(GtItems.SULFUR_DUST);
        Ingredient saltpeter = Ingredient.of(GtItems.SALTPETER_DUST);
        shapeless(Items.GUNPOWDER, 3, "gunpowder_from_coal_dust_shapeless",
                Ingredient.of(CraftItemTagsProvider.DUSTS_COAL), sulfur, saltpeter, saltpeter);
        shapeless(Items.GUNPOWDER, 2, "gunpowder_from_charcoal_dust", Ingredient.of(GtItems.CHARCOAL_DUST), sulfur,
                saltpeter, saltpeter);

        // Obsidian dust: macerator (obsidian; enchanting table → diamond dust), electrolysis.
        machine(MachineRecipe.Kind.MACERATOR, "obsidian", Ingredient.of(Items.OBSIDIAN), 1, GtItems.OBSIDIAN_DUST, 1);
        machine(MachineRecipe.Kind.MACERATOR, "enchanting_table", Ingredient.of(Items.ENCHANTING_TABLE), 1,
                GtItems.DIAMOND_DUST, 2);
        electrolysis("obsidian_dust", Ingredient.of(GtItems.OBSIDIAN_DUST), 4, 3, 500, 5,
                stack(GtItems.SMALL_MAGNESIUM_DUST, 2), stack(Ic2Items.SMALL_IRON_DUST, 2),
                new ItemStack(GtItems.SILICON_CELL.get()), stack(Ic2Items.AIR_CELL, 2));

        // Electrum dust: from silver + gold dust, back to ingot and to small gold/silver; ingot → dust.
        shapeless(GtItems.ELECTRUM_DUST, 2, "electrum_dust", Ingredient.of(CraftItemTagsProvider.DUSTS_SILVER),
                Ingredient.of(CraftItemTagsProvider.DUSTS_GOLD));
        Ingredient smallSilver = Ingredient.of(GtItems.SMALL_SILVER_DUST);
        Ingredient smallGold = Ingredient.of(GtItems.SMALL_GOLD_DUST);
        shapeless(GtItems.ELECTRUM_DUST, 1, "electrum_dust_from_small", smallSilver, smallSilver, smallGold,
                smallGold);
        smelt("electrum_ingot_from_dust", Ingredient.of(GtItems.ELECTRUM_DUST), GtItems.ELECTRUM_INGOT, 1, 0.0F);
        centrifuge("electrum_dust", Ingredient.of(GtItems.ELECTRUM_DUST), 1, 0, 975, stack(GtItems.SMALL_GOLD_DUST, 2),
                stack(GtItems.SMALL_SILVER_DUST, 2));
        machine(MachineRecipe.Kind.MACERATOR, "electrum_ingot", Ingredient.of(GtItems.ELECTRUM_INGOT), 1,
                GtItems.ELECTRUM_DUST, 1);

        // Further GT_OreDictHandler dust recipes whose items exist (left as "later" since 6b.2).
        electrolysis("ash", Ingredient.of(GtItems.ASH), 2, 1, 25, 50, new ItemStack(GtItems.CARBON_CELL.get()));
        electrolysis("saltpeter_dust", Ingredient.of(GtItems.SALTPETER_DUST), 10, 7, 50, 110,
                stack(GtItems.POTASSIUM_CELL, 2), stack(GtItems.NITROGEN_CELL, 2), ItemStack.EMPTY,
                stack(Ic2Items.AIR_CELL, 3));
        electrolysis("pyrite_dust", Ingredient.of(GtItems.PYRITE_DUST), 3, 0, 120, 128, stack(Ic2Items.IRON_DUST, 1),
                stack(GtItems.SULFUR_DUST, 2));
        electrolysis("lazurite_dust", Ingredient.of(GtItems.LAZURITE_DUST), 29, 11, 1475, 100,
                stack(GtItems.ALUMINIUM_DUST, 3), stack(GtItems.SILICON_CELL, 3), stack(GtItems.CALCIUM_CELL, 4),
                stack(GtItems.SODIUM_CELL, 4));
        machine(MachineRecipe.Kind.COMPRESSOR, "lazurite_dust", Ingredient.of(GtItems.LAZURITE_DUST), 8,
                GtItems.LAZURITE_CHUNK, 1);
        centrifuge("netherrack_dust", Ingredient.of(GtItems.NETHERRACK_DUST), 16, 0, 2400, stack(Items.REDSTONE, 1),
                stack(GtItems.SULFUR_DUST, 4), stack(Ic2Items.COAL_DUST, 1), stack(Items.GOLD_NUGGET, 1));
    }

    /**
     * Phase 13.4: {@code GT_Utility.applyUsagesForMaterials} – every crafting recipe in one of GregTech's armor/tool
     * grids gets a macerator recipe (material dust × count) and, for metals, a furnace recipe (ingots × count). In
     * this mod the vanilla iron, gold, diamond and wooden armor/tools and the RedPower gem tools fit (bronze and steel
     * tools are not in scope). Machine and furnace recipes compared item and damage, so only undamaged pieces count.
     */
    /** Electrum plate and the circuit boards made from it (spec 12 §5.7, phase 14). */
    private void gregTechCircuitBoards() {
        plate("electrum", CraftItemTagsProvider.INGOTS_ELECTRUM, GtItems.ELECTRUM_PLATE, GtItems.ELECTRUM_DUST,
                GtItems.ELECTRUM_INGOT);
        SizedIngredient twoElectrum = SizedIngredient.of(CraftItemTagsProvider.PLATES_ELECTRUM, 2);
        assemble("gt_basic_circuit_board", SizedIngredient.of(CraftItemTagsProvider.PLATES_REFINED_IRON, 1), twoElectrum,
                stack(GtItems.BASIC_CIRCUIT_BOARD, 2), 800, 1);
        assemble("gt_basic_circuit_board_from_aluminium", SizedIngredient.of(CraftItemTagsProvider.PLATES_ALUMINIUM, 1),
                twoElectrum, stack(GtItems.BASIC_CIRCUIT_BOARD, 2), 800, 1);
        assemble("gt_advanced_circuit_board", SizedIngredient.of(Ic2Items.ELECTRONIC_CIRCUIT, 1), twoElectrum,
                stack(GtItems.ADVANCED_CIRCUIT_BOARD, 1), 1600, 2);
        assemble("gt_advanced_circuit_board_from_silicon", SizedIngredient.of(CraftItemTagsProvider.PLATES_SILICON, 1),
                SizedIngredient.of(CraftItemTagsProvider.PLATES_ELECTRUM, 4), stack(GtItems.ADVANCED_CIRCUIT_BOARD, 2),
                1600, 2);
        assemble("advanced_circuit_parts", SizedIngredient.of(CraftItemTagsProvider.ITEM_LAZURITE, 1),
                SizedIngredient.of(Tags.Items.DUSTS_GLOWSTONE, 1), stack(GtItems.ADVANCED_CIRCUIT_PARTS, 2), 800, 2);
        assemble("advanced_circuit", SizedIngredient.of(GtItems.ADVANCED_CIRCUIT_BOARD, 1),
                SizedIngredient.of(GtItems.ADVANCED_CIRCUIT_PARTS, 2), stack(Ic2Items.ADVANCED_CIRCUIT, 1), 1600, 2);
        // copperWire = insulated copper cable.
        ItemLike copperWire = Ic2Items.cable(CableType.INSULATED_COPPER);
        assemble("electronic_circuit", SizedIngredient.of(GtItems.BASIC_CIRCUIT_BOARD, 1),
                SizedIngredient.of(copperWire, 3), stack(Ic2Items.ELECTRONIC_CIRCUIT, 1), 800, 1);
        assemble("frequency_transmitter", SizedIngredient.of(Ic2Items.ELECTRONIC_CIRCUIT, 1),
                SizedIngredient.of(copperWire, 1), stack(Ic2Items.FREQUENCY_TRANSMITTER, 1), 800, 1);
    }

    /** Brass from copper and zinc dust (spec 12 §5.7, phase 14); mixed metal variants in {@link #mixedMetal}. */
    private void gregTechBrass() {
        Ingredient copper = Ingredient.of(CraftItemTagsProvider.DUSTS_COPPER);
        shapeless(GtItems.BRASS_DUST, 4, "brass_dust", copper, copper, copper,
                Ingredient.of(CraftItemTagsProvider.DUSTS_ZINC));
        Ingredient smallCopper = Ingredient.of(GtItems.SMALL_COPPER_DUST);
        shapeless(GtItems.BRASS_DUST, 1, "brass_dust_from_small", smallCopper, smallCopper, smallCopper,
                Ingredient.of(GtItems.SMALL_ZINC_DUST));
        smelt("brass_ingot_from_dust", Ingredient.of(GtItems.BRASS_DUST), GtItems.BRASS_INGOT, 1, 0.0F);
        machine(MachineRecipe.Kind.MACERATOR, "brass_ingot", Ingredient.of(CraftItemTagsProvider.INGOTS_BRASS), 1,
                GtItems.BRASS_DUST, 1);
        plate("brass", CraftItemTagsProvider.INGOTS_BRASS, GtItems.BRASS_PLATE, GtItems.BRASS_DUST, GtItems.BRASS_INGOT);
        // ForestryBronzeNerf = true: a single brass dust, as for bronze.
        centrifuge("brass_dust", Ingredient.of(CraftItemTagsProvider.DUSTS_BRASS), 1, 0, 1500,
                stack(GtItems.SMALL_COPPER_DUST, 3), stack(GtItems.SMALL_ZINC_DUST, 1));
    }

    /** Flint and iron mortar ({@code GT_Mod} 3923–3945, spec 12 §5.7) and flour (phase 14). */
    private void gregTechMortars() {
        machine(MachineRecipe.Kind.MACERATOR, "wheat", Ingredient.of(Items.WHEAT), 1, GtItems.FLOUR, 1);
        smelt("bread_from_flour", Ingredient.of(GtItems.FLOUR), Items.BREAD, 1, 0.0F);
        shapeless(GtItems.FLINT_MORTAR, 1, "flint_mortar", Ingredient.of(Items.BOWL), Ingredient.of(Items.FLINT));
        shaped(GtItems.IRON_MORTAR, 1, "iron_mortar").pattern(" R ", "SRS", "SSS")
                .define('R', CraftItemTagsProvider.INGOTS_REFINED_IRON).define('S', Items.STONE_BRICKS).save();
        for (ItemLike mortar : List.of(GtItems.FLINT_MORTAR, GtItems.IRON_MORTAR)) {
            boolean iron = mortar == GtItems.IRON_MORTAR;
            String prefix = iron ? "iron_mortar/" : "flint_mortar/";
            // GT gives hydrated coal dust here (getIC2Item("hydratedCoalDust")), not coal dust.
            mortar(mortar, prefix + "coal", Ingredient.of(Items.COAL), Ic2Items.HYDRATED_COAL_DUST);
            mortar(mortar, prefix + "gold", Ingredient.of(Tags.Items.INGOTS_GOLD), Ic2Items.GOLD_DUST);
            mortar(mortar, prefix + "clay", Ingredient.of(Items.CLAY), Ic2Items.CLAY_DUST);
            mortar(mortar, prefix + "copper", Ingredient.of(ingotsCopper()), Ic2Items.COPPER_DUST);
            mortar(mortar, prefix + "tin", Ingredient.of(CraftItemTagsProvider.INGOTS_TIN), Ic2Items.TIN_DUST);
            if (iron) {
                mortar(mortar, prefix + "bronze", Ingredient.of(CraftItemTagsProvider.INGOTS_BRONZE),
                        Ic2Items.BRONZE_DUST);
            }
            mortar(mortar, prefix + "silver", Ingredient.of(CraftItemTagsProvider.INGOTS_SILVER), Ic2Items.SILVER_DUST);
            mortar(mortar, prefix + "electrum", Ingredient.of(CraftItemTagsProvider.INGOTS_ELECTRUM),
                    GtItems.ELECTRUM_DUST);
            if (iron) {
                mortar(mortar, prefix + "brass", Ingredient.of(CraftItemTagsProvider.INGOTS_BRASS), GtItems.BRASS_DUST);
                mortar(mortar, prefix + "iron", Ingredient.of(Tags.Items.INGOTS_IRON), Ic2Items.IRON_DUST);
                mortar(mortar, prefix + "refined_iron", Ingredient.of(CraftItemTagsProvider.INGOTS_REFINED_IRON),
                        Ic2Items.IRON_DUST);
                mortar(mortar, prefix + "gravel", Ingredient.of(Items.GRAVEL), Items.FLINT);
            }
            mortar(mortar, prefix + "wheat", Ingredient.of(Items.WHEAT), GtItems.FLOUR);
        }
    }

    private void mortar(ItemLike mortar, String name, Ingredient input, ItemLike result) {
        shapeless(result, 1, name, Ingredient.of(mortar), input);
    }

    /** RedPower World gem tools (spec 19 §2): RedPower's gems are GregTech's here. */
    private void redPowerTools() {
        Map<RpItems.Tool, String[]> patterns = new EnumMap<>(RpItems.Tool.class);
        patterns.put(RpItems.Tool.PICKAXE, new String[]{"GGG", " S ", " S "});
        patterns.put(RpItems.Tool.SHOVEL, new String[]{"G", "S", "S"});
        patterns.put(RpItems.Tool.AXE, new String[]{"GG", "GS", " S"});
        patterns.put(RpItems.Tool.SWORD, new String[]{"G", "G", "S"});
        patterns.put(RpItems.Tool.HOE, new String[]{"GG", " S", " S"});
        patterns.put(RpItems.Tool.SICKLE, new String[]{" G ", "  G", "SG "});
        patterns.put(RpItems.Tool.HANDSAW, new String[]{"SSS", " II", " GG"});
        for (RpGem gem : RpGem.values()) {
            for (RpItems.Tool tool : RpItems.Tool.values()) {
                DeferredItem<Item> item = RpItems.tool(gem, tool);
                ShapedBuilder recipe = shaped(item, 1, item.getId().getPath()).pattern(patterns.get(tool))
                        .define('G', gem.getGemTag()).define('S', Items.STICK);
                if (tool == RpItems.Tool.HANDSAW) {
                    recipe.define('I', Items.IRON_INGOT);
                }
                recipe.save();
            }
        }
    }

    /**
     * RedPower project table ({@code RedPowerBase} 354, stone, planks, crafting table, chest) and blank plan: paper and
     * indigo dye, here any blue dye (RedPower registers indigo as {@code dyeBlue}, spec 20 §4 F4).
     */
    private void redPowerTable() {
        shaped(RpItems.PROJECT_TABLE, 1, "project_table").pattern("SSS", "WTW", "WCW")
                .define('S', Items.STONE).define('W', ItemTags.PLANKS).define('T', Items.CRAFTING_TABLE)
                .define('C', Items.CHEST).save();
        shapeless(RpItems.BLANK_PLAN, 1, "blank_plan", Ingredient.of(Items.PAPER), Ingredient.of(Tags.Items.DYES_BLUE));
    }

    /**
     * Portal Gun long fall boots (spec 21): one boot from iron, obsidian and diamonds (non-easy recipe), two boots make
     * the pair and the pair splits back into two boots.
     */
    private void longFallBoots() {
        shaped(PgItems.LONG_FALL_BOOT, 1, "long_fall_boot").pattern("#O#", "#D#", "#D#")
                .define('#', Items.IRON_INGOT).define('O', Items.OBSIDIAN).define('D', Items.DIAMOND).save();
        shapeless(PgItems.LONG_FALL_BOOTS, 1, "long_fall_boots", Ingredient.of(PgItems.LONG_FALL_BOOT),
                Ingredient.of(PgItems.LONG_FALL_BOOT));
        shapeless(PgItems.LONG_FALL_BOOT, 2, "long_fall_boot_from_long_fall_boots",
                Ingredient.of(PgItems.LONG_FALL_BOOTS));
    }

    /**
     * Iron Chests (spec 22 §4): chest recipes of {@code IronChestType} and upgrade recipes of {@code ChestChangerType}.
     * Iron is the iron or the refined iron ingot (the original adds every recipe once per material).
     */
    /** Thermal Expansion basics (spec 26 §5, GT_OreDictHandler rules for the new ores). */
    private void thermalExpansion() {
        Ingredient ironIngot = Ingredient.of(Items.IRON_INGOT);
        Ingredient redstone = Ingredient.of(Items.REDSTONE);
        // Vanilla shaped recipes (ShapedOreRecipe mirrors): BC/Forestry style builders.
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, TeItems.CRESCENT_HAMMER.get())
                .pattern("I I").pattern(" S ").pattern(" I ").define('I', ironIngot)
                .define('S', CraftItemTagsProvider.INGOTS_SILVER).unlockedBy("has_silver", has(CraftItemTagsProvider.INGOTS_SILVER))
                .save(output, id("crescent_hammer"));
        for (ItemLike coal : List.of(Items.COAL, Items.CHARCOAL)) {
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.GUNPOWDER)
                    .requires(GtItems.SALTPETER_DUST.get()).requires(GtItems.SULFUR_DUST.get())
                    .requires(coal).unlockedBy("has_sulfur", has(GtItems.SULFUR_DUST.get()))
                    .save(output, id("gunpowder_from_saltpeter_and_" + BuiltInRegistries.ITEM.getKey(coal.asItem()).getPath()));
        }
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.PAPER, 3).pattern("###")
                .define('#', TeItems.WOOD_CHIPS.get()).unlockedBy("has_chips", has(TeItems.WOOD_CHIPS.get()))
                .save(output, id("paper_from_wood_chips"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TeItems.COMPRESSED_SAWDUST.get()).pattern("###").pattern("# #")
                .pattern("###").define('#', TeItems.SAWDUST.get()).unlockedBy("has_sawdust", has(TeItems.SAWDUST.get()))
                .save(output, id("compressed_sawdust"));
        smelt("charcoal_from_compressed_sawdust", Ingredient.of(TeItems.COMPRESSED_SAWDUST), Items.CHARCOAL, 1, 0.0F);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TeItems.PNEUMATIC_SERVO.get()).pattern(" I ").pattern("GRG")
                .pattern(" I ").define('R', redstone).define('G', Items.GLASS).define('I', ironIngot)
                .unlockedBy("has_redstone", has(Items.REDSTONE)).save(output, id("pneumatic_servo"));
        coil(TeItems.REDSTONE_RECEPTION_COIL.get(), Ingredient.of(Items.GOLD_INGOT), "redstone_reception_coil");
        coil(TeItems.REDSTONE_TRANSMISSION_COIL.get(), Ingredient.of(CraftItemTagsProvider.INGOTS_SILVER),
                "redstone_transmission_coil");
        coil(TeItems.REDSTONE_CONDUCTANCE_COIL.get(), Ingredient.of(CraftItemTagsProvider.INGOTS_ELECTRUM),
                "redstone_conductance_coil");
        gear(TeItems.COPPER_GEAR.get(), Ingredient.of(ingotsCopper()), "copper_gear");
        gear(FrItems.TIN_GEAR.get(), Ingredient.of(CraftItemTagsProvider.INGOTS_TIN), "tin_gear_te");
        gear(TeItems.INVAR_GEAR.get(), Ingredient.of(CraftItemTagsProvider.INGOTS_INVAR), "invar_gear");
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, TeItems.INVAR_DUST.get(), 3)
                .requires(CraftItemTagsProvider.DUSTS_IRON).requires(CraftItemTagsProvider.DUSTS_IRON)
                .requires(CraftItemTagsProvider.DUSTS_NICKEL).unlockedBy("has_nickel", has(CraftItemTagsProvider.DUSTS_NICKEL))
                .save(output, id("invar_dust"));
        smelt("invar_ingot_from_dust", Ingredient.of(CraftItemTagsProvider.DUSTS_INVAR), TeItems.INVAR_INGOT, 1, 0.0F);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TeItems.MACHINE_FRAME.get()).pattern("IGI").pattern("GXG")
                .pattern("IGI").define('I', ironIngot).define('G', Items.GLASS).define('X', Items.GOLD_INGOT)
                .unlockedBy("has_gold", has(Items.GOLD_INGOT)).save(output, id("machine_frame"));
        // Ores: TE smelting XP; GT_OreDictHandler adds macerator and industrial grinder recipes.
        Ingredient silverOre = Ingredient.of(TeItems.SILVER_ORE, TeItems.DEEPSLATE_SILVER_ORE);
        Ingredient leadOre = Ingredient.of(TeItems.LEAD_ORE, TeItems.DEEPSLATE_LEAD_ORE);
        Ingredient nickelOre = Ingredient.of(TeItems.NICKEL_ORE, TeItems.DEEPSLATE_NICKEL_ORE);
        smelt("silver_ingot_from_ore", silverOre, GtItems.SILVER_INGOT, 1, 0.9F);
        smelt("lead_ingot_from_ore", leadOre, GtItems.LEAD_INGOT, 1, 0.8F);
        smelt("nickel_ingot_from_ore", nickelOre, GtItems.NICKEL_INGOT, 1, 1.0F);
        machine(MachineRecipe.Kind.MACERATOR, "silver_ore", silverOre, 1, Ic2Items.SILVER_DUST, 2);
        machine(MachineRecipe.Kind.MACERATOR, "lead_ore", leadOre, 1, GtItems.LEAD_DUST, 2);
        machine(MachineRecipe.Kind.MACERATOR, "nickel_ore", nickelOre, 1, GtItems.NICKEL_DUST, 2);
        grind("silver_ore", silverOre, 1, stack(Ic2Items.SILVER_DUST, 2), stack(GtItems.SMALL_LEAD_DUST, 2));
        grindMercury("silver_ore", silverOre, 1, stack(Ic2Items.SILVER_DUST, 3), stack(GtItems.SMALL_LEAD_DUST, 2));
        grind("lead_ore", leadOre, 1, stack(GtItems.LEAD_DUST, 2), stack(GtItems.SMALL_SILVER_DUST, 1));
        grindMercury("lead_ore", leadOre, 1, stack(GtItems.LEAD_DUST, 2), stack(Ic2Items.SILVER_DUST, 1));
        grind("nickel_ore", nickelOre, 1, stack(GtItems.NICKEL_DUST, 3), stack(GtItems.SMALL_PLATINUM_DUST, 1),
                stack(GtItems.SMALL_COPPER_DUST, 1));
        grindMercury("nickel_ore", nickelOre, 1, stack(GtItems.NICKEL_DUST, 3), stack(GtItems.PLATINUM_DUST, 1));
        // Rockwool: slag in the furnace, then dyed (8 around a dye).
        smelt("light_gray_rockwool_from_slag", Ingredient.of(TeItems.SLAG), TeBlocks.rockwool(DyeColor.LIGHT_GRAY), 1,
                0.0F);
        Ingredient anyRockwool = Ingredient.of(Arrays.stream(DyeColor.values())
                .map(color -> TeBlocks.rockwool(color).get()).toArray(ItemLike[]::new));
        for (DyeColor color : DyeColor.values()) {
            ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, TeBlocks.rockwool(color).get(), 8)
                    .pattern("###").pattern("#D#").pattern("###").define('#', anyRockwool)
                    .define('D', DyeItem.byColor(color)).unlockedBy("has_rockwool", has(TeBlocks.rockwool(DyeColor.LIGHT_GRAY).get()))
                    .save(output, id(color.getSerializedName() + "_rockwool"));
        }
        teMachines();
        gregTechPulverisationByProducts();
    }

    /** TE machines (spec 26 §5) and the recipes of {@code FurnaceManager}, {@code PulverizerManager}, {@code SawmillManager}. */
    /**
     * {@code GT_ModHandler.addPulverisationRecipe} with a by-product (spec 26 §4.1): GT gives the macerator only the
     * main output and puts the by-product into the pulverizer (400 MJ) or, for (tiny) wood pulp, the sawmill (80 MJ);
     * chance 0 means 10 %. With overwrite GT replaces TE's own recipe (coal, lapis and redstone ore). Tool recycling
     * adds wood pulp in {@link #gregTechRecycling()}.
     */
    private void gregTechPulverisationByProducts() {
        TeRecipe.Kind pulverizer = TeRecipe.Kind.PULVERIZER;
        // GT_OreDictHandler: vanilla ores (overwrite) and the GT ores that exist in Craft.
        te(pulverizer, "coal_ore", Ingredient.of(Items.COAL_ORE, Items.DEEPSLATE_COAL_ORE), stack(Ic2Items.COAL_DUST, 2),
                stack(GtItems.THORIUM_DUST, 1), 10, TeRecipes.GT_PULVERIZER_ENERGY);
        te(pulverizer, "lapis_ore", Ingredient.of(Items.LAPIS_ORE, Items.DEEPSLATE_LAPIS_ORE),
                stack(Items.LAPIS_LAZULI, 12), stack(GtItems.LAZURITE_DUST, 1), 10, TeRecipes.GT_PULVERIZER_ENERGY);
        te(pulverizer, "redstone_ore", Ingredient.of(Items.REDSTONE_ORE, Items.DEEPSLATE_REDSTONE_ORE),
                stack(Items.REDSTONE, 10), stack(Items.GLOWSTONE_DUST, 1), 10, TeRecipes.GT_PULVERIZER_ENERGY);
        gtOrePulverisation(GtOre.SODALITE, GtItems.SODALITE_DUST, 12, GtItems.ALUMINIUM_DUST, 10);
        gtOrePulverisation(GtOre.GALENA, GtItems.GALENA_DUST, 2, GtItems.SULFUR_DUST, 50);
        gtOrePulverisation(GtOre.RUBY, GtItems.RUBY_DUST, 2, GtItems.RED_GARNET_DUST, 10);
        gtOrePulverisation(GtOre.SAPPHIRE, GtItems.SAPPHIRE_DUST, 2, GtItems.GREEN_SAPPHIRE_DUST, 10);
        gtOrePulverisation(GtOre.OLIVINE, GtItems.OLIVINE_DUST, 2, GtItems.EMERALD_DUST, 10);
        gtOrePulverisation(GtOre.IRIDIUM, Ic2Items.IRIDIUM_ORE, 2, GtItems.PLATINUM_DUST, 10);
        gtOrePulverisation(GtOre.SHELDONITE, GtItems.PLATINUM_DUST, 2, Ic2Items.IRIDIUM_ORE, 10);
        gtOrePulverisation(GtOre.PYRITE, GtItems.PYRITE_DUST, 5, Ic2Items.IRON_DUST, 10);
        gtOrePulverisation(GtOre.CINNABAR, GtItems.CINNABAR_DUST, 3, Items.REDSTONE, 10);
        gtOrePulverisation(GtOre.SPHALERITE, GtItems.SPHALERITE_DUST, 4, GtItems.ZINC_DUST, 10);
        gtOrePulverisation(GtOre.TUNGSTATE, GtItems.TUNGSTEN_DUST, 2, GtItems.MANGANESE_DUST, 10);
        gtOrePulverisation(GtOre.BAUXITE, GtItems.BAUXITE_DUST, 4, GtItems.ALUMINIUM_DUST, 10);
        // GT_Mod: flint, melon, wool and the vanilla recycling.
        te(pulverizer, "gt_flint", Ingredient.of(Items.FLINT), stack(GtItems.SMALL_FLINT_DUST, 2),
                stack(GtItems.SMALL_FLINT_DUST, 1), 10, TeRecipes.GT_PULVERIZER_ENERGY);
        te(pulverizer, "gt_melon", Ingredient.of(Items.MELON), stack(Items.MELON_SLICE, 8), stack(Items.MELON_SEEDS, 1),
                80, TeRecipes.GT_PULVERIZER_ENERGY);
        te(pulverizer, "gt_wool", Ingredient.of(ItemTags.WOOL), stack(Items.STRING, 2), stack(Items.STRING, 1), 50,
                TeRecipes.GT_PULVERIZER_ENERGY);
        te(pulverizer, "gt_clock", Ingredient.of(Items.CLOCK), stack(Ic2Items.GOLD_DUST, 4), stack(Items.REDSTONE, 1),
                95, TeRecipes.GT_PULVERIZER_ENERGY);
        te(pulverizer, "gt_compass", Ingredient.of(Items.COMPASS), stack(Ic2Items.IRON_DUST, 4),
                stack(Items.REDSTONE, 1), 95, TeRecipes.GT_PULVERIZER_ENERGY);
        te(pulverizer, "gt_enchanting_table", Ingredient.of(Items.ENCHANTING_TABLE), stack(GtItems.DIAMOND_DUST, 2),
                stack(GtItems.OBSIDIAN_DUST, 4), 95, TeRecipes.GT_PULVERIZER_ENERGY);
        te(pulverizer, "gt_lever", Ingredient.of(Items.LEVER), stack(Items.SAND, 1), stack(GtItems.SMALL_WOOD_PULP, 2),
                95, TeRecipes.GT_PULVERIZER_ENERGY);
        te(TeRecipe.Kind.SAWMILL, "gt_redstone_torch", Ingredient.of(Items.REDSTONE_TORCH),
                stack(GtItems.SMALL_WOOD_PULP, 2), stack(Items.REDSTONE, 1), 95, TeRecipes.GT_SAWMILL_ENERGY);
    }

    /** {@code GT_Utility.applyUsagesForMaterials}: tools with sticks give wood pulp, 50 % for sword and sickle. */
    private void toolPulverisation(String name, Ingredient tool, ItemLike dust, int count, boolean half) {
        te(TeRecipe.Kind.PULVERIZER, "gt_" + name, tool, stack(dust, count), stack(GtItems.WOOD_PULP, 1),
                half ? 50 : 100, TeRecipes.GT_PULVERIZER_ENERGY);
    }

    private void gtOrePulverisation(GtOre ore, ItemLike result, int count, ItemLike byProduct, int chance) {
        te(TeRecipe.Kind.PULVERIZER, ore.getBlockName(), gtOre(ore), stack(result, count), stack(byProduct, 1), chance,
                TeRecipes.GT_PULVERIZER_ENERGY);
    }

    private void teMachines() {
        teMachine(TeItems.POWERED_FURNACE.get(), Ingredient.of(Items.REDSTONE), Ingredient.of(Items.BRICKS));
        teMachine(TeItems.PULVERIZER.get(), Ingredient.of(Items.PISTON), Ingredient.of(Items.FLINT));
        teMachine(TeItems.SAWMILL.get(), Ingredient.of(Items.IRON_AXE), Ingredient.of(ItemTags.PLANKS));
        teMachine(TeItems.INDUCTION_SMELTER.get(), Ingredient.of(Items.BUCKET),
                Ingredient.of(CraftItemTagsProvider.INGOTS_INVAR));
        teMachine(TeItems.MAGMA_CRUCIBLE.get(), Ingredient.of(Items.BUCKET), Ingredient.of(Items.NETHER_BRICKS));
        teMachine(TeItems.LIQUID_TRANSPOSER.get(), Ingredient.of(Items.BUCKET), Ingredient.of(Items.GLASS));
        teMachine(TeItems.GLACIAL_PRECIPITATOR.get(), Ingredient.of(Items.PISTON), Ingredient.of(Items.SNOW_BLOCK));
        Ingredient tin = Ingredient.of(CraftItemTagsProvider.INGOTS_TIN);
        Ingredient servo = Ingredient.of(TeItems.PNEUMATIC_SERVO.get());
        teMachine(TeItems.IGNEOUS_EXTRUDER.get(), Ingredient.of(Items.PISTON), Ingredient.of(Items.GLASS), tin, servo);
        teMachine(TeItems.AQUEOUS_ACCUMULATOR.get(), Ingredient.of(Items.BUCKET), Ingredient.of(Items.GLASS), tin,
                servo);
        // ThermalExpansionEnergy: engines III/ C /GPG, energy cell X/YCY/IPI from the filled frame.
        teEngine(TeItems.STEAM_ENGINE.get(), Ingredient.of(ingotsCopper()), CraftItemTagsProvider.GEARS_COPPER);
        teEngine(TeItems.MAGMATIC_ENGINE.get(), Ingredient.of(CraftItemTagsProvider.INGOTS_INVAR),
                CraftItemTagsProvider.GEARS_INVAR);
        // ThermalExpansionEnergy/-Transport: frame, conduits (4), multimeter.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TeItems.ENERGY_CELL_FRAME.get()).pattern("IGI").pattern("GXG")
                .pattern("IGI").define('I', CraftItemTagsProvider.INGOTS_ELECTRUM)
                .define('G', TeItems.HARDENED_GLASS.get()).define('X', Items.DIAMOND)
                .unlockedBy("has_hardened_glass", has(TeItems.HARDENED_GLASS.get()))
                .save(output, id("energy_cell_frame"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TeItems.EMPTY_ENERGY_CONDUIT.get(), 4).pattern("IGI")
                .define('I', CraftItemTagsProvider.INGOTS_ELECTRUM).define('G', TeItems.HARDENED_GLASS.get())
                .unlockedBy("has_hardened_glass", has(TeItems.HARDENED_GLASS.get()))
                .save(output, id("empty_energy_conduit"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TeItems.LIQUIDUCT.get(), 4).pattern("IGI")
                .define('I', ingotsCopper()).define('G', TeItems.HARDENED_GLASS.get())
                .unlockedBy("has_hardened_glass", has(TeItems.HARDENED_GLASS.get()))
                .save(output, id("liquiduct"));
        ShapedRecipeBuilder.shaped(RecipeCategory.TOOLS, TeItems.MULTIMETER.get()).pattern("C C").pattern("LPL")
                .pattern(" G ").define('C', ingotsCopper()).define('L', CraftItemTagsProvider.INGOTS_LEAD)
                .define('P', TeItems.REDSTONE_CONDUCTANCE_COIL.get()).define('G', CraftItemTagsProvider.GEARS_TIN)
                .unlockedBy("has_coil", has(TeItems.REDSTONE_CONDUCTANCE_COIL.get()))
                .save(output, id("multimeter"));
        teFluid(TeFluidRecipe.Kind.FILL, "redstone_energy_conduit", Ingredient.of(TeItems.EMPTY_ENERGY_CONDUIT), 1,
                stack(TeItems.ENERGY_CONDUIT, 1), new FluidStack(TeFluids.MOLTEN_REDSTONE.get(), 50), 80);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TeItems.ENERGY_CELL.get()).pattern(" X ").pattern("YCY")
                .pattern("IPI").define('X', CraftItemTagsProvider.INGOTS_ELECTRUM)
                .define('Y', CraftItemTagsProvider.INGOTS_ELECTRUM).define('C', TeItems.FULL_ENERGY_CELL_FRAME.get())
                .define('I', CraftItemTagsProvider.INGOTS_LEAD).define('P', TeItems.REDSTONE_CONDUCTANCE_COIL.get())
                .unlockedBy("has_frame", has(TeItems.FULL_ENERGY_CELL_FRAME.get()))
                .save(output, id("redstone_energy_cell"));
        // ThermalExpansionTransport: tesseract frame IGI/GXG/IGI, tesseracts X/YCY/IPI from the attuned frame.
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TeItems.TESSERACT_FRAME.get()).pattern("IGI").pattern("GXG")
                .pattern("IGI").define('I', CraftItemTagsProvider.INGOTS_TIN)
                .define('G', TeItems.HARDENED_GLASS.get()).define('X', Items.DIAMOND)
                .unlockedBy("has_hardened_glass", has(TeItems.HARDENED_GLASS.get()))
                .save(output, id("tesseract_frame"));
        tesseract(TeItems.ENERGY_TESSERACT.get(), CraftItemTagsProvider.INGOTS_ELECTRUM,
                CraftItemTagsProvider.INGOTS_LEAD, Ingredient.of(TeItems.REDSTONE_CONDUCTANCE_COIL.get()));
        tesseract(TeItems.LIQUID_TESSERACT.get(), ingotsCopper(), CraftItemTagsProvider.INGOTS_TIN, servo);
        tesseract(TeItems.ITEM_TESSERACT.get(), CraftItemTagsProvider.INGOTS_TIN, CraftItemTagsProvider.INGOTS_TIN,
                servo);
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, TeItems.PORTABLE_TANK.get()).pattern(" G ").pattern("GTG")
                .pattern(" G ").define('G', TeItems.HARDENED_GLASS.get()).define('T', tin)
                .unlockedBy("has_hardened_glass", has(TeItems.HARDENED_GLASS.get())).save(output, id("portable_tank"));

        // Powered Furnace: own recipes; everything else smelts at 160 MJ (TeRecipes.find).
        te(TeRecipe.Kind.FURNACE, "cactus", Ingredient.of(Items.CACTUS), stack(Items.GREEN_DYE, 1), 80);
        for (ItemLike[] food : new ItemLike[][]{{Items.PORKCHOP, Items.COOKED_PORKCHOP}, {Items.COD, Items.COOKED_COD},
                {Items.BEEF, Items.COOKED_BEEF}, {Items.CHICKEN, Items.COOKED_CHICKEN},
                {Items.POTATO, Items.BAKED_POTATO}}) {
            te(TeRecipe.Kind.FURNACE, path(food[0]), Ingredient.of(food[0]), stack(food[1], 1), 40);
        }

        TeRecipe.Kind pulverizer = TeRecipe.Kind.PULVERIZER;
        te(pulverizer, "stone", Ingredient.of(Items.STONE), stack(Items.COBBLESTONE, 1), 320);
        te(pulverizer, "cobblestone", Ingredient.of(Items.COBBLESTONE), stack(Items.SAND, 1), stack(Items.GRAVEL, 1),
                10, 320);
        te(pulverizer, "gravel", Ingredient.of(Items.GRAVEL), stack(Items.FLINT, 1), 320);
        te(pulverizer, "glass", Ingredient.of(Items.GLASS), stack(Items.SAND, 1), 320);
        te(pulverizer, "stone_bricks", Ingredient.of(Items.STONE_BRICKS), stack(Items.CRACKED_STONE_BRICKS, 1), 80);
        te(pulverizer, "sandstone", Ingredient.of(Items.SANDSTONE, Items.CHISELED_SANDSTONE, Items.CUT_SANDSTONE),
                stack(Items.SAND, 2), stack(GtItems.SALTPETER_DUST, 1), 10, 320);
        te(pulverizer, "obsidian", Ingredient.of(Items.OBSIDIAN), stack(GtItems.OBSIDIAN_DUST, 1), 400);
        te(pulverizer, "netherrack", Ingredient.of(Items.NETHERRACK), stack(Items.COBBLESTONE, 1),
                stack(GtItems.SULFUR_DUST, 1), 10, 320);
        te(pulverizer, "diamond_ore", Ingredient.of(Items.DIAMOND_ORE, Items.DEEPSLATE_DIAMOND_ORE),
                stack(Items.DIAMOND, 1), 80);
        te(pulverizer, "emerald_ore", Ingredient.of(Items.EMERALD_ORE, Items.DEEPSLATE_EMERALD_ORE),
                stack(Items.EMERALD, 1), 80);
        te(pulverizer, "glowstone", Ingredient.of(Items.GLOWSTONE), stack(Items.GLOWSTONE_DUST, 4), 80);
        te(pulverizer, "logs", Ingredient.of(Items.OAK_LOG, Items.SPRUCE_LOG, Items.BIRCH_LOG, Items.JUNGLE_LOG),
                stack(TeItems.WOOD_CHIPS, 1), 160);
        te(pulverizer, "sugar_cane", Ingredient.of(Items.SUGAR_CANE), stack(Items.SUGAR, 2), 80);
        te(pulverizer, "bone", Ingredient.of(Items.BONE), stack(Items.BONE_MEAL, 6), 160);
        te(pulverizer, "blaze_rod", Ingredient.of(Items.BLAZE_ROD), stack(Items.BLAZE_POWDER, 4),
                stack(GtItems.SULFUR_DUST, 1), 50, 160);
        // Ores (raw metal counts as ore, as in the macerator): 2 dust and a 10 % by-product.
        te(pulverizer, "iron_ore", Ingredient.of(Items.IRON_ORE, Items.DEEPSLATE_IRON_ORE, Items.RAW_IRON),
                stack(Ic2Items.IRON_DUST, 2), stack(GtItems.NICKEL_DUST, 1), 10, 400);
        te(pulverizer, "gold_ore", Ingredient.of(Items.GOLD_ORE, Items.DEEPSLATE_GOLD_ORE, Items.RAW_GOLD),
                stack(Ic2Items.GOLD_DUST, 2), 400);
        te(pulverizer, "copper_ore", Ingredient.of(Items.COPPER_ORE, Items.DEEPSLATE_COPPER_ORE, Items.RAW_COPPER),
                stack(Ic2Items.COPPER_DUST, 2), stack(Ic2Items.GOLD_DUST, 1), 10, 400);
        te(pulverizer, "tin_ore", Ingredient.of(Ic2Items.TIN_ORE, Ic2Items.DEEPSLATE_TIN_ORE),
                stack(Ic2Items.TIN_DUST, 2), stack(Ic2Items.IRON_DUST, 1), 10, 400);
        te(pulverizer, "silver_ore", Ingredient.of(TeItems.SILVER_ORE, TeItems.DEEPSLATE_SILVER_ORE),
                stack(Ic2Items.SILVER_DUST, 2), stack(GtItems.LEAD_DUST, 1), 10, 400);
        te(pulverizer, "lead_ore", Ingredient.of(TeItems.LEAD_ORE, TeItems.DEEPSLATE_LEAD_ORE),
                stack(GtItems.LEAD_DUST, 2), stack(Ic2Items.SILVER_DUST, 1), 10, 400);
        te(pulverizer, "nickel_ore", Ingredient.of(TeItems.NICKEL_ORE, TeItems.DEEPSLATE_NICKEL_ORE),
                stack(GtItems.NICKEL_DUST, 2), stack(GtItems.PLATINUM_DUST, 1), 10, 400);
        te(pulverizer, "iron_ingot", Ingredient.of(Items.IRON_INGOT), stack(Ic2Items.IRON_DUST, 1), 240);
        te(pulverizer, "gold_ingot", Ingredient.of(Items.GOLD_INGOT), stack(Ic2Items.GOLD_DUST, 1), 240);
        te(pulverizer, "copper_ingot", Ingredient.of(ingotsCopper()), stack(Ic2Items.COPPER_DUST, 1), 240);
        te(pulverizer, "tin_ingot", Ingredient.of(CraftItemTagsProvider.INGOTS_TIN), stack(Ic2Items.TIN_DUST, 1), 240);
        te(pulverizer, "silver_ingot", Ingredient.of(CraftItemTagsProvider.INGOTS_SILVER),
                stack(Ic2Items.SILVER_DUST, 1), 240);
        te(pulverizer, "lead_ingot", Ingredient.of(CraftItemTagsProvider.INGOTS_LEAD), stack(GtItems.LEAD_DUST, 1), 240);
        te(pulverizer, "nickel_ingot", Ingredient.of(CraftItemTagsProvider.INGOTS_NICKEL),
                stack(GtItems.NICKEL_DUST, 1), 240);
        te(pulverizer, "platinum_ingot", Ingredient.of(CraftItemTagsProvider.INGOTS_PLATINUM),
                stack(GtItems.PLATINUM_DUST, 1), 240);
        te(pulverizer, "electrum_ingot", Ingredient.of(CraftItemTagsProvider.INGOTS_ELECTRUM),
                stack(GtItems.ELECTRUM_DUST, 1), 240);
        te(pulverizer, "invar_ingot", Ingredient.of(CraftItemTagsProvider.INGOTS_INVAR), stack(TeItems.INVAR_DUST, 1),
                240);
        te(pulverizer, "bronze_ingot", Ingredient.of(CraftItemTagsProvider.INGOTS_BRONZE),
                stack(Ic2Items.BRONZE_DUST, 1), 240);
        te(pulverizer, "coal", Ingredient.of(Items.COAL), stack(Ic2Items.COAL_DUST, 1), 160);
        te(pulverizer, "charcoal", Ingredient.of(Items.CHARCOAL), stack(GtItems.CHARCOAL_DUST, 1), 160);

        TeRecipe.Kind sawmill = TeRecipe.Kind.SAWMILL;
        ItemStack sawdust = stack(TeItems.SAWDUST, 1);
        for (ItemLike[] log : new ItemLike[][]{{Items.OAK_LOG, Items.OAK_PLANKS}, {Items.SPRUCE_LOG, Items.SPRUCE_PLANKS},
                {Items.BIRCH_LOG, Items.BIRCH_PLANKS}, {Items.JUNGLE_LOG, Items.JUNGLE_PLANKS}}) {
            te(sawmill, path(log[0]), Ingredient.of(log[0]), stack(log[1], 6), sawdust, 100, 80);
        }
        te(sawmill, "note_block", Ingredient.of(Items.NOTE_BLOCK), stack(Items.OAK_PLANKS, 8), stack(Items.REDSTONE, 1),
                100, 240);
        te(sawmill, "bed", Ingredient.of(ItemTags.BEDS), stack(Items.OAK_PLANKS, 3), stack(Items.WHITE_WOOL, 3), 100,
                240);
        te(sawmill, "bookshelf", Ingredient.of(Items.BOOKSHELF), stack(Items.OAK_PLANKS, 6), stack(Items.BOOK, 3), 100,
                240);
        te(sawmill, "chest", Ingredient.of(Items.CHEST), stack(Items.OAK_PLANKS, 8), 240);
        te(sawmill, "crafting_table", Ingredient.of(Items.CRAFTING_TABLE), stack(Items.OAK_PLANKS, 4), 240);
        te(sawmill, "sign", Ingredient.of(Items.OAK_SIGN), stack(Items.OAK_PLANKS, 2), 240);
        // 1.4.7: one door from 6 planks gave 6; 1.21 crafts three doors from 6 planks (abweichungen.md).
        te(sawmill, "door", Ingredient.of(Items.OAK_DOOR), stack(Items.OAK_PLANKS, 2), 240);
        te(sawmill, "pressure_plate", Ingredient.of(Items.OAK_PRESSURE_PLATE), stack(Items.OAK_PLANKS, 2), 240);
        te(sawmill, "jukebox", Ingredient.of(Items.JUKEBOX), stack(Items.OAK_PLANKS, 8), stack(Items.DIAMOND, 1), 100,
                240);
        te(sawmill, "trapdoor", Ingredient.of(Items.OAK_TRAPDOOR), stack(Items.OAK_PLANKS, 3), 240);
        te(sawmill, "fence_gate", Ingredient.of(Items.OAK_FENCE_GATE), stack(Items.OAK_PLANKS, 2), sawdust, 100, 240);
        te(sawmill, "boat", Ingredient.of(Items.OAK_BOAT), stack(Items.OAK_PLANKS, 5), 240);
        te(sawmill, "wooden_sword", Ingredient.of(Items.WOODEN_SWORD), stack(Items.OAK_PLANKS, 2), sawdust, 100, 160);
        te(sawmill, "wooden_shovel", Ingredient.of(Items.WOODEN_SHOVEL), stack(Items.OAK_PLANKS, 1), sawdust, 100, 160);
        te(sawmill, "wooden_pickaxe", Ingredient.of(Items.WOODEN_PICKAXE), stack(Items.OAK_PLANKS, 3), sawdust, 100,
                160);
        te(sawmill, "wooden_axe", Ingredient.of(Items.WOODEN_AXE), stack(Items.OAK_PLANKS, 3), sawdust, 100, 160);
        te(sawmill, "wooden_hoe", Ingredient.of(Items.WOODEN_HOE), stack(Items.OAK_PLANKS, 2), sawdust, 100, 160);
        te(sawmill, "rubber_wood", Ingredient.of(Ic2Items.RUBBER_WOOD), stack(Items.JUNGLE_PLANKS, 4),
                stack(Ic2Items.RESIN, 1), 33, 120);
        teLiquidMachines();
    }

    /** {@code SmelterManager} with the GregTech additions, {@code CrucibleManager}, {@code TransposerManager}. */
    private void teLiquidMachines() {
        Ingredient sand = Ingredient.of(Items.SAND);
        Ingredient richSlag = Ingredient.of(TeItems.RICH_SLAG);
        ItemStack slag = stack(TeItems.SLAG, 1);
        // Ores (+ raw metal as in the pulverizer): with sand 2 ingots, with rich slag 3 ingots.
        Object[][] ores = {
                {"iron", Ingredient.of(Items.IRON_ORE, Items.DEEPSLATE_IRON_ORE, Items.RAW_IRON), Items.IRON_INGOT},
                {"gold", Ingredient.of(Items.GOLD_ORE, Items.DEEPSLATE_GOLD_ORE, Items.RAW_GOLD), Items.GOLD_INGOT},
                {"copper", Ingredient.of(Items.COPPER_ORE, Items.DEEPSLATE_COPPER_ORE, Items.RAW_COPPER),
                        Items.COPPER_INGOT},
                {"tin", Ingredient.of(Ic2Items.TIN_ORE, Ic2Items.DEEPSLATE_TIN_ORE), Ic2Items.TIN_INGOT.get()},
                {"silver", Ingredient.of(TeItems.SILVER_ORE, TeItems.DEEPSLATE_SILVER_ORE), GtItems.SILVER_INGOT.get()},
                {"lead", Ingredient.of(TeItems.LEAD_ORE, TeItems.DEEPSLATE_LEAD_ORE), GtItems.LEAD_INGOT.get()},
                {"nickel", Ingredient.of(TeItems.NICKEL_ORE, TeItems.DEEPSLATE_NICKEL_ORE), GtItems.NICKEL_INGOT.get()}};
        for (Object[] ore : ores) {
            Ingredient input = (Ingredient) ore[1];
            ItemLike ingot = (ItemLike) ore[2];
            smelter(ore[0] + "_ore_sand", input, 1, sand, 1, stack(ingot, 2), stack(TeItems.RICH_SLAG, 1), 5, 320);
            smelter(ore[0] + "_ore_rich_slag", input, 1, richSlag, 1, stack(ingot, 3), slag, 75, 400);
        }
        // Dusts: 2 dust + sand → 2 ingots.
        Object[][] dusts = {
                {"iron", Ingredient.of(CraftItemTagsProvider.DUSTS_IRON), Items.IRON_INGOT},
                {"gold", Ingredient.of(CraftItemTagsProvider.DUSTS_GOLD), Items.GOLD_INGOT},
                {"copper", Ingredient.of(CraftItemTagsProvider.DUSTS_COPPER), Items.COPPER_INGOT},
                {"tin", Ingredient.of(CraftItemTagsProvider.DUSTS_TIN), Ic2Items.TIN_INGOT.get()},
                {"silver", Ingredient.of(CraftItemTagsProvider.DUSTS_SILVER), GtItems.SILVER_INGOT.get()},
                {"lead", Ingredient.of(CraftItemTagsProvider.DUSTS_LEAD), GtItems.LEAD_INGOT.get()},
                {"nickel", Ingredient.of(CraftItemTagsProvider.DUSTS_NICKEL), GtItems.NICKEL_INGOT.get()},
                {"platinum", Ingredient.of(CraftItemTagsProvider.DUSTS_PLATINUM), GtItems.PLATINUM_INGOT.get()},
                {"electrum", Ingredient.of(GtItems.ELECTRUM_DUST), GtItems.ELECTRUM_INGOT.get()},
                {"invar", Ingredient.of(CraftItemTagsProvider.DUSTS_INVAR), TeItems.INVAR_INGOT.get()},
                {"bronze", Ingredient.of(CraftItemTagsProvider.DUSTS_BRONZE), Ic2Items.BRONZE_INGOT.get()},
                {"brass", Ingredient.of(CraftItemTagsProvider.DUSTS_BRASS), GtItems.BRASS_INGOT.get()},
                {"zinc", Ingredient.of(CraftItemTagsProvider.DUSTS_ZINC), GtItems.ZINC_INGOT.get()}};
        for (Object[] dust : dusts) {
            smelter(dust[0] + "_dust", (Ingredient) dust[1], 2, sand, 1, stack((ItemLike) dust[2], 2), slag, 25, 80);
        }
        // GregTech (GT_OreDictHandler): pyrite ore, refined iron, brass.
        smelter("pyrite_ore_sand", gtOre(GtOre.PYRITE), 1, sand, 1, stack(Items.IRON_INGOT, 1),
                stack(TeItems.RICH_SLAG, 1), 10, 300);
        smelter("pyrite_ore_rich_slag", gtOre(GtOre.PYRITE), 1, richSlag, 1, stack(Items.IRON_INGOT, 2), slag, 95, 300);
        smelter("refined_iron", Ingredient.of(Items.IRON_INGOT), 2, sand, 1, stack(Ic2Items.REFINED_IRON_INGOT, 2), slag,
                25, 400);
        smelter("brass", Ingredient.of(ingotsCopper()), 3, Ingredient.of(CraftItemTagsProvider.INGOTS_ZINC), 1,
                stack(GtItems.BRASS_INGOT, 4), ItemStack.EMPTY, 0, 400);
        // Alloys and the rest of SmelterManager.
        smelter("electrum", Ingredient.of(CraftItemTagsProvider.INGOTS_SILVER), 1, Ingredient.of(Items.GOLD_INGOT), 1,
                stack(GtItems.ELECTRUM_INGOT, 2), ItemStack.EMPTY, 0, 240);
        smelter("invar", Ingredient.of(CraftItemTagsProvider.INGOTS_NICKEL), 1, Ingredient.of(Items.IRON_INGOT), 2,
                stack(TeItems.INVAR_INGOT, 3), ItemStack.EMPTY, 0, 240);
        smelter("bronze", Ingredient.of(ingotsCopper()), 3, Ingredient.of(CraftItemTagsProvider.INGOTS_TIN), 1,
                stack(Ic2Items.BRONZE_INGOT, 2), ItemStack.EMPTY, 0, 240);
        Ingredient obsidianDust = Ingredient.of(GtItems.OBSIDIAN_DUST);
        smelter("hardened_glass_from_lead_ingot", Ingredient.of(CraftItemTagsProvider.INGOTS_LEAD), 1, obsidianDust, 2,
                stack(TeItems.HARDENED_GLASS, 2), ItemStack.EMPTY, 0, 400);
        smelter("hardened_glass_from_lead_dust", Ingredient.of(CraftItemTagsProvider.DUSTS_LEAD), 1, obsidianDust, 2,
                stack(TeItems.HARDENED_GLASS, 2), ItemStack.EMPTY, 0, 400);
        smelter("nether_bricks", Ingredient.of(Items.NETHERRACK), 4, Ingredient.of(Items.SOUL_SAND), 1,
                stack(Items.NETHER_BRICKS, 2), stack(GtItems.SULFUR_DUST, 1), 25, 400);

        // Magma Crucible: lava 20 000 MJ (core.lava.mj) × 6/5; netherrack from the pack config.
        TeFluidRecipe.Kind crucible = TeFluidRecipe.Kind.CRUCIBLE;
        for (ItemLike rock : List.of(Items.COBBLESTONE, Items.STONE, Items.OBSIDIAN)) {
            teFluid(crucible, path(rock), Ingredient.of(rock), 1, ItemStack.EMPTY, new FluidStack(Fluids.LAVA, 1000), 24000);
        }
        teFluid(crucible, "netherrack", Ingredient.of(Items.NETHERRACK), 1, ItemStack.EMPTY,
                new FluidStack(Fluids.LAVA, 1000), 12000);
        teFluid(crucible, "snowball", Ingredient.of(Items.SNOWBALL), 4, ItemStack.EMPTY,
                new FluidStack(Fluids.WATER, 500), 60);
        teFluid(crucible, "snow_block", Ingredient.of(Items.SNOW_BLOCK), 1, ItemStack.EMPTY,
                new FluidStack(Fluids.WATER, 500), 60);
        teFluid(crucible, "ice", Ingredient.of(Items.ICE), 1, ItemStack.EMPTY, new FluidStack(Fluids.WATER, 1000), 120);
        teFluid(crucible, "redstone", Ingredient.of(Items.REDSTONE), 1, ItemStack.EMPTY,
                new FluidStack(TeFluids.MOLTEN_REDSTONE.get(), 25), 1200);
        teFluid(crucible, "ender_pearl", Ingredient.of(Items.ENDER_PEARL), 1, ItemStack.EMPTY,
                new FluidStack(TeFluids.MOLTEN_ENDER.get(), 250), 2400);

        // Liquid Transposer: IC2 module (cells, 80 MJ), then every registered container (bucket 80, else 160 MJ);
        // a fill already present for the same container and liquid keeps the first one (no extraction added).
        Set<String> fills = new HashSet<>();
        transpose(fills, new LiquidContainers.Entry(Fluids.WATER, 1000, Ic2Items.WATER_CELL.get(),
                Ic2Items.EMPTY_CELL.get()), 80);
        transpose(fills, new LiquidContainers.Entry(Fluids.LAVA, 1000, Ic2Items.LAVA_CELL.get(),
                Ic2Items.EMPTY_CELL.get()), 80);
        for (LiquidContainers.Entry entry : LiquidContainers.entries()) {
            transpose(fills, entry, entry.empty() == Items.BUCKET ? 80 : 160);
        }
        TeFluidRecipe.Kind fill = TeFluidRecipe.Kind.FILL;
        teFluid(fill, "full_energy_cell_frame", Ingredient.of(TeItems.ENERGY_CELL_FRAME), 1,
                stack(TeItems.FULL_ENERGY_CELL_FRAME, 1), new FluidStack(TeFluids.MOLTEN_REDSTONE.get(), 1000), 1600);
        teFluid(fill, "unattuned_tesseract", Ingredient.of(TeItems.TESSERACT_FRAME), 1,
                stack(TeItems.UNATTUNED_TESSERACT, 1), new FluidStack(TeFluids.MOLTEN_ENDER.get(), 1000), 1600);
        // GregTech: hydrated coal dust.
        teFluid(fill, "hydrated_coal_dust", Ingredient.of(Ic2Items.COAL_DUST), 1, stack(Ic2Items.HYDRATED_COAL_DUST, 1),
                new FluidStack(Fluids.WATER, 125), 125);
        teFluid(TeFluidRecipe.Kind.EXTRACT, "hydrated_coal_dust", Ingredient.of(Ic2Items.HYDRATED_COAL_DUST), 1,
                stack(Ic2Items.COAL_DUST, 1), new FluidStack(Fluids.WATER, 100), 125);
    }

    /** {@code addFillRecipe(energy, data, reversible = true)}: fill plus the extraction back. */
    private void transpose(Set<String> fills, LiquidContainers.Entry entry, int energy) {
        String key = path(entry.empty()) + "|" + BuiltInRegistries.FLUID.getKey(entry.fluid());
        if (!fills.add(key)) {
            return;
        }
        FluidStack fluid = new FluidStack(entry.fluid(), entry.amount());
        teFluid(TeFluidRecipe.Kind.FILL, path(entry.filled()), Ingredient.of(entry.empty()), 1,
                stack(entry.filled(), 1), fluid, energy);
        teFluid(TeFluidRecipe.Kind.EXTRACT, path(entry.filled()), Ingredient.of(entry.filled()), 1,
                stack(entry.empty(), 1), fluid, energy);
    }

    private void smelter(String name, Ingredient first, int firstCount, Ingredient second, int secondCount,
                         ItemStack primary, ItemStack secondary, int chance, int energy) {
        output.accept(id("te_smelter/" + name), new TeRecipe(TeRecipe.Kind.SMELTER,
                List.of(new SizedIngredient(first, firstCount), new SizedIngredient(second, secondCount)), primary,
                secondary, chance, energy), null);
    }

    private void teFluid(TeFluidRecipe.Kind kind, String name, Ingredient input, int count, ItemStack itemOutput,
                         FluidStack fluid, int energy) {
        output.accept(id("te_" + kind.getName() + "/" + name), new TeFluidRecipe(kind,
                new SizedIngredient(input, count), itemOutput, fluid, 100, energy), null);
    }

    /** TE machines: {@code " X ", "YCY", "IPI"} with machine frame, copper and a redstone reception coil. */
    private void teEngine(ItemLike result, Ingredient ingot, TagKey<Item> gear) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result).pattern("III").pattern(" C ").pattern("GPG")
                .define('I', ingot).define('C', TeItems.REDSTONE_TRANSMISSION_COIL.get()).define('G', gear)
                .define('P', Items.PISTON).unlockedBy("has_coil", has(TeItems.REDSTONE_TRANSMISSION_COIL.get()))
                .save(output, id(path(result)));
    }

    /** {@code ThermalExpansionTransport}: " X "/"YCY"/"IPI" with Y silver and C the unattuned tesseract. */
    private void tesseract(ItemLike result, TagKey<Item> x, TagKey<Item> i, Ingredient p) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result).pattern(" X ").pattern("YCY").pattern("IPI")
                .define('X', x).define('Y', CraftItemTagsProvider.INGOTS_SILVER)
                .define('C', TeItems.UNATTUNED_TESSERACT.get()).define('I', i).define('P', p)
                .unlockedBy("has_tesseract", has(TeItems.UNATTUNED_TESSERACT.get()))
                .save(output, id(path(result)));
    }

    private void teMachine(ItemLike result, Ingredient x, Ingredient y) {
        teMachine(result, x, y, Ingredient.of(ingotsCopper()), Ingredient.of(TeItems.REDSTONE_RECEPTION_COIL.get()));
    }

    private void teMachine(ItemLike result, Ingredient x, Ingredient y, Ingredient i, Ingredient p) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result).pattern(" X ").pattern("YCY").pattern("IPI")
                .define('X', x).define('Y', y).define('C', TeItems.MACHINE_FRAME.get()).define('I', i)
                .define('P', p)
                .unlockedBy("has_frame", has(TeItems.MACHINE_FRAME.get()))
                .save(output, id(path(result)));
    }

    private void te(TeRecipe.Kind kind, String name, Ingredient input, ItemStack primary, int energy) {
        te(kind, name, input, primary, ItemStack.EMPTY, 0, energy);
    }

    private void te(TeRecipe.Kind kind, String name, Ingredient input, ItemStack primary, ItemStack secondary,
                    int chance, int energy) {
        output.accept(id("te_" + kind.getName() + "/" + name), new TeRecipe(kind,
                List.of(new SizedIngredient(input, 1)), primary, secondary, chance, energy), null);
    }

    private static String path(ItemLike item) {
        return BuiltInRegistries.ITEM.getKey(item.asItem()).getPath();
    }

    /** {@code powerCoil*}: {@code "  R", " G ", "R  "}. */
    private void coil(ItemLike result, Ingredient metal, String name) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result).pattern("  R").pattern(" G ").pattern("R  ")
                .define('R', Items.REDSTONE).define('G', metal).unlockedBy("has_redstone", has(Items.REDSTONE))
                .save(output, id(name));
    }

    /** TE gears: {@code " X ", "XIX", " X "} around an iron ingot. */
    private void gear(ItemLike result, Ingredient metal, String name) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, result).pattern(" X ").pattern("XIX").pattern(" X ")
                .define('X', metal).define('I', Items.IRON_INGOT).unlockedBy("has_iron", has(Items.IRON_INGOT))
                .save(output, id(name));
    }

    /** Advanced Machines (spec 25 §2). */
    private void advancedMachines() {
        shaped(machine(Ic2Machine.ROTARY_MACERATOR), 1, "rotary_macerator").pattern("RRR", "RMR", "RAR")
                .define('R', CraftItemTagsProvider.INGOTS_REFINED_IRON).define('M', machine(Ic2Machine.MACERATOR))
                .define('A', Ic2Items.ADVANCED_MACHINE_CASING).save();
        shaped(machine(Ic2Machine.SINGULARITY_COMPRESSOR), 1, "singularity_compressor").pattern("RRR", "RMR", "RAR")
                .define('R', Items.OBSIDIAN).define('M', machine(Ic2Machine.COMPRESSOR))
                .define('A', Ic2Items.ADVANCED_MACHINE_CASING).save();
        shaped(machine(Ic2Machine.CENTRIFUGE_EXTRACTOR), 1, "centrifuge_extractor").pattern("RRR", "RMR", "RAR")
                .define('R', Ic2Items.ELECTROLYZED_WATER_CELL).define('M', machine(Ic2Machine.EXTRACTOR))
                .define('A', Ic2Items.ADVANCED_MACHINE_CASING).save();
        smelt("refined_iron_ingot_from_dust", Ingredient.of(AdvancedMachinesItems.REFINED_IRON_DUST),
                Ic2Items.REFINED_IRON_INGOT, 1, 1.0F);
    }

    private void ironChests() {
        Ingredient iron = CompoundIngredient.of(Ingredient.of(Items.IRON_INGOT),
                Ingredient.of(CraftItemTagsProvider.INGOTS_REFINED_IRON));
        Ingredient gold = Ingredient.of(Items.GOLD_INGOT);
        Ingredient diamond = Ingredient.of(Items.DIAMOND);
        Ingredient copper = Ingredient.of(ingotsCopper());
        Ingredient silver = Ingredient.of(CraftItemTagsProvider.INGOTS_SILVER);
        Ingredient obsidian = Ingredient.of(Items.OBSIDIAN);
        Ingredient glass = Ingredient.of(Items.GLASS);
        Ingredient planks = Ingredient.of(ItemTags.PLANKS);
        Ingredient chest = Ingredient.of(Items.CHEST);
        chestRecipe(IronChestType.IRON, "", "mmm", "mPm", "mmm", iron, chest);
        chestRecipe(IronChestType.IRON, "_from_copper_chest", "mGm", "GPG", "mGm", iron, ironChest(IronChestType.COPPER));
        chestRecipe(IronChestType.GOLD, "", "mmm", "mPm", "mmm", gold, ironChest(IronChestType.IRON));
        chestRecipe(IronChestType.GOLD, "_from_silver_chest", "mGm", "GPG", "mGm", gold, ironChest(IronChestType.SILVER));
        chestRecipe(IronChestType.DIAMOND, "", "GGG", "mPm", "GGG", diamond, ironChest(IronChestType.GOLD));
        chestRecipe(IronChestType.DIAMOND, "_from_silver_chest", "GGG", "GPG", "mmm", diamond,
                ironChest(IronChestType.SILVER));
        chestRecipe(IronChestType.COPPER, "", "mmm", "mPm", "mmm", copper, chest);
        chestRecipe(IronChestType.SILVER, "", "mmm", "mPm", "mmm", silver, ironChest(IronChestType.COPPER));
        chestRecipe(IronChestType.SILVER, "_from_iron_chest", "mGm", "GPG", "mGm", silver, ironChest(IronChestType.IRON));
        chestRecipe(IronChestType.CRYSTAL, "", "GGG", "GPG", "GGG", glass, ironChest(IronChestType.DIAMOND));
        chestRecipe(IronChestType.OBSIDIAN, "", "mmm", "mPm", "mmm", obsidian, ironChest(IronChestType.DIAMOND));

        upgradeRecipe(ChestUpgradeType.IRON_GOLD, gold, iron, "mmm", "msm", "mmm");
        upgradeRecipe(ChestUpgradeType.GOLD_DIAMOND, diamond, gold, "GGG", "msm", "GGG");
        upgradeRecipe(ChestUpgradeType.COPPER_SILVER, silver, copper, "mmm", "msm", "mmm");
        upgradeRecipe(ChestUpgradeType.SILVER_GOLD, gold, silver, "mGm", "GsG", "mGm");
        upgradeRecipe(ChestUpgradeType.COPPER_IRON, iron, copper, "mGm", "GsG", "mGm");
        upgradeRecipe(ChestUpgradeType.DIAMOND_CRYSTAL, glass, diamond, "GGG", "GOG", "GGG");
        upgradeRecipe(ChestUpgradeType.WOOD_IRON, iron, planks, "mmm", "msm", "mmm");
        upgradeRecipe(ChestUpgradeType.WOOD_COPPER, copper, planks, "mmm", "msm", "mmm");
        upgradeRecipe(ChestUpgradeType.DIAMOND_OBSIDIAN, obsidian, diamond, "mmm", "mGm", "mmm");
    }

    private static Ingredient ironChest(IronChestType type) {
        return Ingredient.of(IronChestItems.chest(type));
    }

    /** {@code m} material, {@code P} previous chest, {@code G} glass. */
    /**
     * EnderStorage (spec 27 §6): per wool colour i the ender chest i/i/i (blaze rods, wool, obsidian, chest, ender
     * pearl), and the recolour recipe.
     */
    private void enderStorage() {
        for (DyeColor colour : DyeColor.values()) {
            int i = colour.getId();
            ItemStack chest = new ItemStack(EsItems.ENDER_CHEST.get());
            chest.set(EsDataComponents.FREQUENCY.get(), Frequency.of(i, i, i));
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, chest)
                    .pattern("bWb").pattern("OCO").pattern("bpb")
                    .define('b', Items.BLAZE_ROD).define('W', woolOf(colour)).define('O', Items.OBSIDIAN)
                    .define('C', Items.CHEST).define('p', Items.ENDER_PEARL)
                    .unlockedBy("has_ender_pearl", has(Items.ENDER_PEARL))
                    .save(output, id("ender_storage_chest_" + colour.getName()));
            ItemStack pouch = new ItemStack(EsItems.ENDER_POUCH.get());
            pouch.set(EsDataComponents.FREQUENCY.get(), Frequency.of(i, i, i));
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, pouch)
                    .pattern("blb").pattern("lpl").pattern("bWb")
                    .define('b', Items.BLAZE_POWDER).define('l', Items.LEATHER).define('p', Items.ENDER_PEARL)
                    .define('W', woolOf(colour))
                    .unlockedBy("has_ender_pearl", has(Items.ENDER_PEARL))
                    .save(output, id("ender_pouch_" + colour.getName()));
        }
        SpecialRecipeBuilder.special(EnderStorageRecolourRecipe::new).save(output, id("ender_storage_recolour"));
    }

    private static Item woolOf(DyeColor colour) {
        return BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace(colour.getName() + "_wool"));
    }

    private void chestRecipe(IronChestType type, String suffix, String top, String middle, String bottom,
                             Ingredient material, Ingredient previous) {
        String all = top + middle + bottom;
        ShapedBuilder builder = shaped(IronChestItems.chest(type), 1, type.getName() + "_chest" + suffix)
                .pattern(top, middle, bottom).define('P', previous);
        if (all.indexOf('m') >= 0) {
            builder.define('m', material);
        }
        if (all.indexOf('G') >= 0) {
            builder.define('G', Items.GLASS);
        }
        builder.save();
    }

    /** {@code m} target material, {@code s} source material, {@code G} glass, {@code O} obsidian. */
    private void upgradeRecipe(ChestUpgradeType type, Ingredient target, Ingredient source, String top, String middle,
                               String bottom) {
        String all = top + middle + bottom;
        ShapedBuilder builder = shaped(IronChestItems.upgrade(type), 1, type.getName()).pattern(top, middle, bottom);
        if (all.indexOf('m') >= 0) {
            builder.define('m', target);
        }
        if (all.indexOf('s') >= 0) {
            builder.define('s', source);
        }
        if (all.indexOf('G') >= 0) {
            builder.define('G', Items.GLASS);
        }
        if (all.indexOf('O') >= 0) {
            builder.define('O', Items.OBSIDIAN);
        }
        builder.save();
    }

    private void gregTechRecycling() {
        record Piece(String name, int count) {
        }
        List<Piece> armor = List.of(new Piece("helmet", 5), new Piece("chestplate", 8), new Piece("leggings", 7),
                new Piece("boots", 4));
        List<Piece> tools = List.of(new Piece("sword", 2), new Piece("pickaxe", 3), new Piece("shovel", 1),
                new Piece("axe", 3), new Piece("hoe", 2));
        record Material(String prefix, ItemLike dust, ItemLike ingot, boolean armor) {
        }
        List<Material> materials = List.of(new Material("iron", Ic2Items.IRON_DUST, Items.IRON_INGOT, true),
                new Material("golden", Ic2Items.GOLD_DUST, Items.GOLD_INGOT, true),
                new Material("diamond", GtItems.DIAMOND_DUST, null, true));
        for (Material material : materials) {
            for (Piece piece : material.armor() ? concat(armor, tools) : tools) {
                Item item = BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace(
                        material.prefix() + "_" + piece.name()));
                Ingredient undamaged = DataComponentIngredient.of(false, DataComponents.DAMAGE, 0, item);
                String name = material.prefix() + "_" + piece.name();
                machine(MachineRecipe.Kind.MACERATOR, name, undamaged, 1, material.dust(), piece.count());
                if (tools.contains(piece)) {
                    toolPulverisation(name, undamaged, material.dust(), piece.count(), piece.name().equals("sword"));
                }
                if (material.ingot() != null) {
                    smelt(material.ingot() == Items.IRON_INGOT ? "iron_ingots_from_" + name : "gold_ingots_from_" + name,
                            undamaged, material.ingot(), piece.count(), 0.0F);
                }
            }
        }
        // Planks give 4 tiny piles of wood pulp; tool grids with sticks add 2 (sword) or 4 tiny piles.
        for (Piece piece : tools) {
            Item item = BuiltInRegistries.ITEM.get(ResourceLocation.withDefaultNamespace("wooden_" + piece.name()));
            int extra = piece.name().equals("sword") ? 2 : 4;
            machine(MachineRecipe.Kind.MACERATOR, "wooden_" + piece.name(),
                    DataComponentIngredient.of(false, DataComponents.DAMAGE, 0, item), 1, GtItems.SMALL_WOOD_PULP,
                    piece.count() * 4 + extra);
        }
        // RedPower gem tools: macerator only, no furnace (spec 19 §5); the handsaw fits no grid.
        for (RpGem gem : RpGem.values()) {
            for (RpItems.Tool tool : RpItems.Tool.values()) {
                if (tool.getRecoveredDust() > 0) {
                    DeferredItem<Item> item = RpItems.tool(gem, tool);
                    Ingredient undamaged = DataComponentIngredient.of(false, DataComponents.DAMAGE, 0, item.get());
                    machine(MachineRecipe.Kind.MACERATOR, item.getId().getPath(), undamaged, 1, gem.getDust(),
                            tool.getRecoveredDust());
                    toolPulverisation(item.getId().getPath(), undamaged, gem.getDust(), tool.getRecoveredDust(),
                            tool == RpItems.Tool.SWORD || tool == RpItems.Tool.SICKLE);
                }
            }
        }
        // The vanilla nugget smelting would compete with the ingot recipes: it keeps only the items GT leaves alone.
        output.accept(ResourceLocation.withDefaultNamespace("iron_nugget_from_smelting"), new SmeltingRecipe("",
                CookingBookCategory.MISC, Ingredient.of(Items.CHAINMAIL_HELMET, Items.CHAINMAIL_CHESTPLATE,
                Items.CHAINMAIL_LEGGINGS, Items.CHAINMAIL_BOOTS, Items.IRON_HORSE_ARMOR),
                new ItemStack(Items.IRON_NUGGET), 0.1F, 200), null);
        output.accept(ResourceLocation.withDefaultNamespace("gold_nugget_from_smelting"), new SmeltingRecipe("",
                CookingBookCategory.MISC, Ingredient.of(Items.GOLDEN_HORSE_ARMOR), new ItemStack(Items.GOLD_NUGGET),
                0.1F, 200), null);
    }

    private static <T> List<T> concat(List<T> first, List<T> second) {
        List<T> all = new ArrayList<>(first);
        all.addAll(second);
        return all;
    }

    private void gregTechTungsten() {
        for (String variant : new String[]{"refined_iron", "aluminium"}) {
            shaped(GtItems.machine(GtMachine.VACUUM_FREEZER), 1, "vacuum_freezer_from_" + variant)
                    .pattern("RPR", "CGC", "RPR").define('R', variant.equals("aluminium")
                            ? CraftItemTagsProvider.PLATES_ALUMINIUM : CraftItemTagsProvider.PLATES_REFINED_IRON)
                    .define('P', machine(Ic2Machine.PUMP)).define('C', Ic2Items.ADVANCED_CIRCUIT)
                    .define('G', Ic2Items.REINFORCED_GLASS).save();
        }
        // Pack config "vacuumfreezer"; coolant cells with any heat come out cold.
        freeze("coolant_cell_10k", Ic2Items.COOLANT_CELL_10K, Ic2Items.COOLANT_CELL_10K, 1000);
        freeze("coolant_cell_30k", Ic2Items.COOLANT_CELL_30K, Ic2Items.COOLANT_CELL_30K, 3000);
        freeze("coolant_cell_60k", Ic2Items.COOLANT_CELL_60K, Ic2Items.COOLANT_CELL_60K, 6000);
        freeze("helium_coolant_cell_60k", GtItems.HELIUM_COOLANT_CELL_60K, GtItems.HELIUM_COOLANT_CELL_60K, 7000);
        freeze("helium_coolant_cell_180k", GtItems.HELIUM_COOLANT_CELL_180K, GtItems.HELIUM_COOLANT_CELL_180K, 20000);
        freeze("helium_coolant_cell_360k", GtItems.HELIUM_COOLANT_CELL_360K, GtItems.HELIUM_COOLANT_CELL_360K, 39000);
        freeze("nak_coolant_cell_60k", GtItems.NAK_COOLANT_CELL_60K, GtItems.NAK_COOLANT_CELL_60K, 5000);
        freeze("nak_coolant_cell_180k", GtItems.NAK_COOLANT_CELL_180K, GtItems.NAK_COOLANT_CELL_180K, 15000);
        freeze("nak_coolant_cell_360k", GtItems.NAK_COOLANT_CELL_360K, GtItems.NAK_COOLANT_CELL_360K, 30000);
        freeze("tungstensteel_ingot", GtItems.HOT_TUNGSTENSTEEL_INGOT, GtItems.TUNGSTENSTEEL_INGOT, 450);
        freeze("ice_cell", Ic2Items.WATER_CELL, GtItems.ICE_CELL, 50);
        centrifuge("ice_cell", Ingredient.of(GtItems.ICE_CELL), 1, 0, 40, stack(Items.ICE, 1), ItemStack.EMPTY,
                ItemStack.EMPTY, stack(Ic2Items.EMPTY_CELL, 1));

        // basicmetalblastfurnacerequirement Tungsten=true: no furnace recipe for the dust.
        blast("tungsten_ingot", SizedIngredient.of(GtItems.TUNGSTEN_DUST.get(), 1), null, 2000, 2500,
                stack(GtItems.TUNGSTEN_INGOT, 1));
        blast("hot_tungstensteel_ingot", SizedIngredient.of(CraftItemTagsProvider.INGOTS_TUNGSTEN, 1),
                SizedIngredient.of(CraftItemTagsProvider.INGOTS_STEEL, 1), 2000, 3000,
                stack(GtItems.HOT_TUNGSTENSTEEL_INGOT, 2), stack(GtItems.DARK_ASH, 4));
        machine(MachineRecipe.Kind.MACERATOR, "tungsten_ingot", Ingredient.of(CraftItemTagsProvider.INGOTS_TUNGSTEN), 1,
                GtItems.TUNGSTEN_DUST, 1);
        plate("tungsten", CraftItemTagsProvider.INGOTS_TUNGSTEN, GtItems.TUNGSTEN_PLATE, GtItems.TUNGSTEN_DUST,
                GtItems.TUNGSTEN_INGOT);
        plate("tungstensteel", CraftItemTagsProvider.INGOTS_TUNGSTENSTEEL, GtItems.TUNGSTENSTEEL_PLATE, null,
                GtItems.TUNGSTENSTEEL_INGOT);

        // Steel block: compressor only (storage block crafting/decrafting is off in the pack), furnace and macerator back.
        machine(MachineRecipe.Kind.COMPRESSOR, "steel_block", Ingredient.of(CraftItemTagsProvider.INGOTS_STEEL), 9,
                GtItems.STEEL_BLOCK, 1);
        smelt("steel_ingot_from_steel_block", Ingredient.of(CraftItemTagsProvider.STORAGE_BLOCKS_STEEL),
                GtItems.STEEL_INGOT, 9, 0.0F);
        machine(MachineRecipe.Kind.MACERATOR, "steel_block", Ingredient.of(CraftItemTagsProvider.STORAGE_BLOCKS_STEEL),
                1, GtItems.STEEL_DUST, 9);

        shaped(GtItems.WOLFRAMIUM_GRINDER, 2, "wolframium_grinder").pattern("TST", "SBS", "TST")
                .define('T', CraftItemTagsProvider.PLATES_TUNGSTEN).define('S', CraftItemTagsProvider.PLATES_STEEL)
                .define('B', CraftItemTagsProvider.STORAGE_BLOCKS_STEEL).save();
        // Any charge: the lapotron crystal's energy is lost.
        shaped(GtItems.ENERGY_FLOW_CIRCUIT, 4, "energy_flow_circuit").pattern("AWA", "LIL", "AWA")
                .define('A', Ic2Items.ADVANCED_CIRCUIT).define('W', CraftItemTagsProvider.PLATES_TUNGSTEN)
                .define('L', Ic2Items.LAPOTRON_CRYSTAL).define('I', Ic2Items.IRIDIUM_PLATE).save();
        // Brass variants need the alloy smelter (out of scope).
        mixedMetal("tungsten_bronze_tin", CraftItemTagsProvider.PLATES_TUNGSTEN, CraftItemTagsProvider.PLATES_TIN, 3);
        mixedMetal("tungsten_bronze_zinc", CraftItemTagsProvider.PLATES_TUNGSTEN, CraftItemTagsProvider.PLATES_ZINC, 3);
        mixedMetal("tungsten_bronze_aluminium", CraftItemTagsProvider.PLATES_TUNGSTEN,
                CraftItemTagsProvider.PLATES_ALUMINIUM, 4);
        mixedMetal("tungstensteel_bronze_tin", CraftItemTagsProvider.PLATES_TUNGSTENSTEEL,
                CraftItemTagsProvider.PLATES_TIN, 5);
        mixedMetal("tungstensteel_bronze_zinc", CraftItemTagsProvider.PLATES_TUNGSTENSTEEL,
                CraftItemTagsProvider.PLATES_ZINC, 5);
        mixedMetal("tungstensteel_bronze_aluminium", CraftItemTagsProvider.PLATES_TUNGSTENSTEEL,
                CraftItemTagsProvider.PLATES_ALUMINIUM, 6);
    }

    /** Vacuum freezer recipe: one input (any components), 128 EU/t. */
    private void freeze(String name, ItemLike input, ItemLike result, int duration) {
        output.accept(id("gt_vacuum_freezer/" + name), new GtRecipe(GtRecipe.Kind.VACUUM_FREEZER,
                List.of(SizedIngredient.of(input, 1)), List.of(new ItemStack(result)), duration, 128, 0), null);
    }

    /**
     * Industrial grinder (spec 13 §10): the machine, its grinding head, all reachable grinder recipes and what they
     * unlock (small dusts, iridium nuggets).
     */
    private void grinder() {
        shaped(GtItems.machine(GtMachine.INDUSTRIAL_GRINDER), 1, "industrial_grinder")
                .pattern("EAP", "GGG", "ABA").define('E', GtItems.machine(GtMachine.INDUSTRIAL_ELECTROLYZER))
                .define('A', Ic2Items.ADVANCED_CIRCUIT).define('P', machine(Ic2Machine.PUMP))
                .define('G', CraftItemTagsProvider.CRAFTING_GRINDER).define('B', CraftItemTagsProvider.RAW_MACHINE_TIER_02)
                .save();
        shaped(GtItems.DIAMOND_GRINDER, 2, "diamond_grinder").pattern("DSD", "SGS", "DSD")
                .define('D', GtItems.DIAMOND_DUST).define('S', CraftItemTagsProvider.PLATES_STEEL)
                .define('G', Ic2Items.INDUSTRIAL_DIAMOND).save();

        // GT_Mod: netherrack.
        grind("netherrack", Ingredient.of(Items.NETHERRACK), 16, stack(Items.GOLD_NUGGET, 1),
                stack(GtItems.NETHERRACK_DUST, 16));
        grindMercury("netherrack", Ingredient.of(Items.NETHERRACK), 8, stack(Items.GOLD_NUGGET, 1),
                stack(GtItems.NETHERRACK_DUST, 8));
        // GT_OreDictHandler: vanilla and IC2 ores.
        Ingredient lapis = Ingredient.of(Items.LAPIS_ORE, Items.DEEPSLATE_LAPIS_ORE);
        grind("lapis_ore", lapis, 1, stack(Items.LAPIS_LAZULI, 12), stack(GtItems.LAZURITE_DUST, 3));
        Ingredient redstone = Ingredient.of(Items.REDSTONE_ORE, Items.DEEPSLATE_REDSTONE_ORE);
        grind("redstone_ore", redstone, 1, stack(Items.REDSTONE, 10), stack(GtItems.SMALL_GLOWSTONE_DUST, 2));
        Ingredient iron = Ingredient.of(Items.IRON_ORE, Items.DEEPSLATE_IRON_ORE);
        grind("iron_ore", iron, 1, stack(Ic2Items.IRON_DUST, 2), stack(GtItems.SMALL_TIN_DUST, 1),
                stack(GtItems.SMALL_NICKEL_DUST, 1));
        Ingredient gold = Ingredient.of(Items.GOLD_ORE, Items.DEEPSLATE_GOLD_ORE);
        grind("gold_ore", gold, 1, stack(Ic2Items.GOLD_DUST, 2), stack(GtItems.SMALL_COPPER_DUST, 1),
                stack(GtItems.SMALL_NICKEL_DUST, 1));
        grindMercury("gold_ore", gold, 1, stack(Ic2Items.GOLD_DUST, 3), stack(GtItems.SMALL_COPPER_DUST, 1),
                stack(GtItems.SMALL_NICKEL_DUST, 1));
        Ingredient copper = Ingredient.of(Items.COPPER_ORE, Items.DEEPSLATE_COPPER_ORE);
        grind("copper_ore", copper, 1, stack(Ic2Items.COPPER_DUST, 2), stack(GtItems.SMALL_GOLD_DUST, 1),
                stack(GtItems.SMALL_NICKEL_DUST, 1));
        grindMercury("copper_ore", copper, 1, stack(Ic2Items.COPPER_DUST, 2), stack(Ic2Items.GOLD_DUST, 1));
        grind("tin_ore", Ingredient.of(Ic2Items.TIN_ORE, Ic2Items.DEEPSLATE_TIN_ORE), 1, stack(Ic2Items.TIN_DUST, 2),
                stack(Ic2Items.SMALL_IRON_DUST, 1), stack(GtItems.SMALL_ZINC_DUST, 1));
        grind("diamond_ore", Ingredient.of(Items.DIAMOND_ORE, Items.DEEPSLATE_DIAMOND_ORE), 1,
                stack(Items.DIAMOND, 1), stack(GtItems.SMALL_DIAMOND_DUST, 6), stack(Ic2Items.HYDRATED_COAL_DUST, 1));
        grind("emerald_ore", Ingredient.of(Items.EMERALD_ORE, Items.DEEPSLATE_EMERALD_ORE), 1,
                stack(Items.EMERALD, 1), stack(GtItems.SMALL_EMERALD_DUST, 6), stack(GtItems.SMALL_OLIVINE_DUST, 2));
        grind("coal_ore", Ingredient.of(Items.COAL_ORE, Items.DEEPSLATE_COAL_ORE), 1, stack(Items.COAL, 1),
                stack(Ic2Items.COAL_DUST, 1), stack(GtItems.SMALL_THORIUM_DUST, 1));
        grind("uranium_ore", Ingredient.of(Ic2Items.URANIUM_ORE, Ic2Items.DEEPSLATE_URANIUM_ORE), 1,
                stack(GtItems.URANIUM_DUST, 2), stack(GtItems.SMALL_PLUTONIUM_DUST, 2));
        // GT ores (07-weltgenerierung §6.4, details spec 13 §10).
        grind("galena_ore", gtOre(GtOre.GALENA), 1, stack(GtItems.GALENA_DUST, 2), stack(GtItems.SULFUR_DUST, 1));
        grindMercury("galena_ore", gtOre(GtOre.GALENA), 1, stack(GtItems.GALENA_DUST, 2),
                stack(GtItems.SULFUR_DUST, 1), stack(Ic2Items.SILVER_DUST, 1));
        grind("iridium_ore", gtOre(GtOre.IRIDIUM), 1, stack(Ic2Items.IRIDIUM_ORE, 2),
                stack(GtItems.SMALL_PLATINUM_DUST, 2));
        grindMercury("iridium_ore", gtOre(GtOre.IRIDIUM), 1, stack(Ic2Items.IRIDIUM_ORE, 2),
                stack(GtItems.PLATINUM_DUST, 1));
        grind("ruby_ore", gtOre(GtOre.RUBY), 1, stack(GtItems.RUBY, 1), stack(GtItems.SMALL_RUBY_DUST, 6),
                stack(GtItems.SMALL_RED_GARNET_DUST, 2));
        grind("sapphire_ore", gtOre(GtOre.SAPPHIRE), 1, stack(GtItems.SAPPHIRE, 1),
                stack(GtItems.SMALL_SAPPHIRE_DUST, 6), stack(GtItems.SMALL_GREEN_SAPPHIRE_DUST, 2));
        grind("olivine_ore", gtOre(GtOre.OLIVINE), 1, stack(GtItems.OLIVINE, 1), stack(GtItems.SMALL_OLIVINE_DUST, 6),
                stack(GtItems.SMALL_EMERALD_DUST, 2));
        grind("bauxite_ore", gtOre(GtOre.BAUXITE), 1, stack(GtItems.BAUXITE_DUST, 4),
                stack(GtItems.ALUMINIUM_DUST, 1));
        grind("pyrite_ore", gtOre(GtOre.PYRITE), 1, stack(GtItems.PYRITE_DUST, 5), stack(GtItems.SULFUR_DUST, 2));
        grind("cinnabar_ore", gtOre(GtOre.CINNABAR), 1, stack(GtItems.CINNABAR_DUST, 5),
                stack(GtItems.SMALL_REDSTONE_DUST, 2), stack(GtItems.SMALL_GLOWSTONE_DUST, 1));
        grind("sphalerite_ore", gtOre(GtOre.SPHALERITE), 1, stack(GtItems.SPHALERITE_DUST, 5),
                stack(GtItems.ZINC_DUST, 1), stack(GtItems.SMALL_YELLOW_GARNET_DUST, 1));
        grind("tungstate_ore", gtOre(GtOre.TUNGSTATE), 1, stack(GtItems.TUNGSTEN_DUST, 2),
                stack(Ic2Items.SMALL_IRON_DUST, 3), stack(GtItems.SMALL_MANGANESE_DUST, 3));
        grind("sheldonite_ore", gtOre(GtOre.SHELDONITE), 1, stack(GtItems.PLATINUM_DUST, 2),
                stack(GtItems.NICKEL_DUST, 1), stack(GtItems.IRIDIUM_NUGGET, 2));
        grindMercury("sheldonite_ore", gtOre(GtOre.SHELDONITE), 1, stack(GtItems.PLATINUM_DUST, 3),
                stack(GtItems.NICKEL_DUST, 1), stack(GtItems.IRIDIUM_NUGGET, 2));
        grind("sodalite_ore", gtOre(GtOre.SODALITE), 1, stack(GtItems.SODALITE_DUST, 12),
                stack(GtItems.ALUMINIUM_DUST, 3));

        // Small dusts of the outputs (where the dust exists), iridium nuggets, platinum dust centrifuge.
        smallDust("glowstone", GtItems.SMALL_GLOWSTONE_DUST, Ingredient.of(Items.GLOWSTONE_DUST),
                Items.GLOWSTONE_DUST);
        smallDust("redstone", GtItems.SMALL_REDSTONE_DUST, Ingredient.of(Items.REDSTONE), Items.REDSTONE);
        smallDust("diamond", GtItems.SMALL_DIAMOND_DUST, Ingredient.of(GtItems.DIAMOND_DUST), GtItems.DIAMOND_DUST);
        smallDust("emerald", GtItems.SMALL_EMERALD_DUST, Ingredient.of(GtItems.EMERALD_DUST), GtItems.EMERALD_DUST);
        smallDust("olivine", GtItems.SMALL_OLIVINE_DUST, Ingredient.of(GtItems.OLIVINE_DUST), GtItems.OLIVINE_DUST);
        smallDust("ruby", GtItems.SMALL_RUBY_DUST, Ingredient.of(GtItems.RUBY_DUST), GtItems.RUBY_DUST);
        smallDust("sapphire", GtItems.SMALL_SAPPHIRE_DUST, Ingredient.of(GtItems.SAPPHIRE_DUST),
                GtItems.SAPPHIRE_DUST);
        smallDust("green_sapphire", GtItems.SMALL_GREEN_SAPPHIRE_DUST, Ingredient.of(GtItems.GREEN_SAPPHIRE_DUST),
                GtItems.GREEN_SAPPHIRE_DUST);
        smallDust("red_garnet", GtItems.SMALL_RED_GARNET_DUST, Ingredient.of(GtItems.RED_GARNET_DUST),
                GtItems.RED_GARNET_DUST);
        smallDust("yellow_garnet", GtItems.SMALL_YELLOW_GARNET_DUST, Ingredient.of(GtItems.YELLOW_GARNET_DUST),
                GtItems.YELLOW_GARNET_DUST);
        smallDust("thorium", GtItems.SMALL_THORIUM_DUST, Ingredient.of(GtItems.THORIUM_DUST), GtItems.THORIUM_DUST);
        smallDust("platinum", GtItems.SMALL_PLATINUM_DUST, Ingredient.of(CraftItemTagsProvider.DUSTS_PLATINUM),
                GtItems.PLATINUM_DUST);
        Ingredient nugget = Ingredient.of(GtItems.IRIDIUM_NUGGET);
        shapeless(GtItems.IRIDIUM_INGOT, 1, "iridium_ingot_from_nuggets", nugget, nugget, nugget, nugget, nugget,
                nugget, nugget, nugget, nugget);
        shapeless(GtItems.IRIDIUM_NUGGET, 9, "iridium_nugget", Ingredient.of(GtItems.IRIDIUM_INGOT));
        machine(MachineRecipe.Kind.COMPRESSOR, "iridium_nugget", nugget, 9, GtItems.IRIDIUM_INGOT, 1);
        centrifuge("platinum_dust", Ingredient.of(CraftItemTagsProvider.DUSTS_PLATINUM), 1, 0, 3000,
                stack(GtItems.IRIDIUM_NUGGET, 1), stack(GtItems.SMALL_NICKEL_DUST, 1));
    }

    /**
     * {@code GT_Mod.addGrinderRecipe(ore, -1, …)}: ore + 1 water cell, and the automatic water bucket variant whose
     * empty cell output becomes an empty bucket. 100 ticks per ore at 128 EU/t.
     */
    private void grind(String name, Ingredient ore, int count, ItemStack... outputs) {
        grinderRecipe(name, ore, count, SizedIngredient.of(Ic2Items.WATER_CELL.get(), 1),
                grinderOutputs(outputs, Ic2Items.EMPTY_CELL));
        grinderRecipe(name + "_with_bucket", ore, count, SizedIngredient.of(Items.WATER_BUCKET, 1),
                grinderOutputs(outputs, Items.BUCKET));
    }

    /** Grinder recipe with a mercury cell instead of water. */
    private void grindMercury(String name, Ingredient ore, int count, ItemStack... outputs) {
        grinderRecipe(name + "_with_mercury", ore, count, SizedIngredient.of(GtItems.MERCURY_CELL.get(), 1),
                grinderOutputs(outputs, Ic2Items.EMPTY_CELL));
    }

    private void grinderRecipe(String name, Ingredient ore, int count, SizedIngredient fluid, List<ItemStack> outputs) {
        output.accept(id("gt_grinder/" + name), new GtRecipe(GtRecipe.Kind.GRINDER,
                List.of(new SizedIngredient(ore, count), fluid), outputs, 100 * count, 128, 0), null);
    }

    /** Outputs 1–3 (missing ones empty) and the returned container as output 4. */
    private static List<ItemStack> grinderOutputs(ItemStack[] outputs, ItemLike container) {
        List<ItemStack> list = new ArrayList<>(List.of(outputs));
        while (list.size() < 3) {
            list.add(ItemStack.EMPTY);
        }
        list.add(stack(container, 1));
        return list;
    }

    /** A GT ore and its deepslate variant. */
    private static Ingredient gtOre(GtOre ore) {
        return ore.hasDeepslateVariant() ? Ingredient.of(GtItems.ore(ore), GtItems.deepslateOre(ore))
                : Ingredient.of(GtItems.ore(ore));
    }

    private void gtOreMacerator(GtOre ore, ItemLike result, int count) {
        machine(MachineRecipe.Kind.MACERATOR, ore.getBlockName(), gtOre(ore), 1, result, count);
    }

    /** Depleted isotope cell as crafted: damage 9999. */
    private static ItemStack isotopeCell() {
        ItemStack stack = new ItemStack(Ic2Items.ISOTOPE_CELL.get());
        stack.setDamageValue(9999);
        return stack;
    }

    private void gradualRepair(String name, Item item, Ingredient material, int amount) {
        output.accept(id(name), new GradualRepairRecipe(item, material, amount), null);
    }

    /** IC2 recipes name gradual items as stacks with damage 0: worn components do not match. */
    private static Ingredient fresh(ItemLike item) {
        return DataComponentIngredient.of(false, DataComponents.DAMAGE, 0, item);
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
                .define('C', fresh(Ic2Items.COOLANT_CELL_10K)).define('W', Ic2Items.cable(CableType.INSULATED_COPPER))
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
        // IC2's hidden tin bucket "T T/ T " is removed by GregTech (applyUsagesForMaterials, every metal but iron).
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
        shaped(Ic2Items.REINFORCED_STONE, 8, "reinforced_stone").pattern("SSS", "SAS", "SSS")
                .define('S', Items.STONE).define('A', Ic2Items.ADVANCED_ALLOY).save();
        shaped(Ic2Items.REINFORCED_DOOR, 1, "reinforced_door").pattern("SS", "SS", "SS")
                .define('S', Ic2Items.REINFORCED_STONE).save();
        shaped(Ic2Items.RUBBER_MAT, 3, "rubber_mat").pattern("RRR", "RRR").define('R', Ic2Items.RUBBER).save();
        shapeless(Ic2Items.CONSTRUCTION_FOAM, 3, "construction_foam", Ingredient.of(CraftItemTagsProvider.DUSTS_CLAY),
                water(), Ingredient.of(Items.REDSTONE), Ingredient.of(CraftItemTagsProvider.DUSTS_COAL));
        // GregTech: 4 wooden scaffolds instead of 16.
        shaped(Ic2Items.WOODEN_SCAFFOLD, 4, "wooden_scaffold").pattern("PPP", " s ", "s s").define('P', ItemTags.PLANKS)
                .define('s', Items.STICK).save();
        shaped(Ic2Items.IRON_SCAFFOLD, 16, "iron_scaffold").pattern("PPP", " s ", "s s")
                .define('P', CraftItemTagsProvider.INGOTS_REFINED_IRON).define('s', Ic2Items.IRON_FENCE).save();
        shaped(Ic2Items.LUMINATOR, 8, "luminator").pattern("ICI", "GTG", "GGG")
                .define('I', CraftItemTagsProvider.INGOTS_REFINED_IRON)
                .define('C', Ic2Items.cable(CableType.INSULATED_COPPER)).define('T', Ic2Items.cable(CableType.TIN))
                .define('G', Items.GLASS).save();
        // Config enableCraftingCoin = true.
        shaped(Ic2Items.COIN, 16, "coin").pattern("II", "II").define('I', CraftItemTagsProvider.INGOTS_REFINED_IRON)
                .save();
        shaped(Ic2Items.REFINED_IRON_INGOT, 1, "refined_iron_ingot_from_coins").pattern("III", "III", "III")
                .define('I', Ic2Items.COIN).save();
        shaped(Ic2Items.REINFORCED_GLASS, 7, "reinforced_glass").pattern("GAG", "GGG", "GAG")
                .define('G', Items.GLASS).define('A', Ic2Items.ADVANCED_ALLOY).save();
        shaped(Ic2Items.REINFORCED_GLASS, 7, "reinforced_glass_rotated").pattern("GGG", "AGA", "GGG")
                .define('G', Items.GLASS).define('A', Ic2Items.ADVANCED_ALLOY).save();
        shaped(Ic2Items.NUKE, 1, "nuke").pattern("RCR", "UMU", "RCR")
                .define('R', Ic2Items.RE_ENRICHED_URANIUM_CELL).define('C', Ic2Items.ADVANCED_CIRCUIT)
                .define('U', Ic2Items.URANIUM_BLOCK)
                .define('M', Ic2Items.ADVANCED_MACHINE_CASING).save();
        shaped(Ic2Items.INDUSTRIAL_TNT, 4, "industrial_tnt").pattern("FFF", "TTT", "FFF")
                .define('F', Items.FLINT).define('T', Items.TNT).save();
        shaped(Ic2Items.INDUSTRIAL_TNT, 4, "industrial_tnt_columns").pattern("FTF", "FTF", "FTF")
                .define('F', Items.FLINT).define('T', Items.TNT).save();
        shapeless(Ic2Items.DYNAMITE, 8, "dynamite", Ingredient.of(Ic2Items.INDUSTRIAL_TNT),
                Ingredient.of(Items.STRING));
        shaped(Ic2Items.STICKY_DYNAMITE, 8, "sticky_dynamite").pattern("DDD", "DRD", "DDD")
                .define('D', Ic2Items.DYNAMITE).define('R', Ic2Items.RESIN).save();
        shaped(Ic2Items.DYNAMITE_O_MOTE, 1, "dynamite_o_mote").pattern(" c ", "GCG", "TTT")
                .define('c', Ic2Items.cable(CableType.INSULATED_COPPER)).define('G', Items.GLOWSTONE_DUST)
                .define('C', Ic2Items.ELECTRONIC_CIRCUIT).define('T', Items.TNT).save();
        shaped(Ic2Items.DYNAMITE_O_MOTE, 1, "dynamite_o_mote_from_frequency_transmitter")
                .pattern(" C ", "TLT", " F ").define('C', Ic2Items.cable(CableType.INSULATED_COPPER))
                .define('F', Ic2Items.FREQUENCY_TRANSMITTER).define('L', Items.LAPIS_LAZULI)
                .define('T', CraftItemTagsProvider.INGOTS_TIN).save();
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
        smelt("dark_coffee", Ingredient.of(Ic2Items.COLD_COFFEE), Ic2Items.DARK_COFFEE, 1, 0.1F);
        smelt("refined_iron_from_machine_casing", Ingredient.of(Ic2Items.MACHINE_CASING), Ic2Items.REFINED_IRON_INGOT, 8,
                0.0F);
        smelt("tin_ingots_from_block", Ingredient.of(Ic2Items.TIN_BLOCK), Ic2Items.TIN_INGOT, 9, 0.0F);
        smelt("bronze_ingots_from_block", Ingredient.of(Ic2Items.BRONZE_BLOCK), Ic2Items.BRONZE_INGOT, 9, 0.0F);
        smelt("resin_from_slime_ball", Ingredient.of(Items.SLIME_BALL), Ic2Items.RESIN, 1, 0.0F);
        smelt("bronze_ingots_from_wrench", Ingredient.of(Ic2Items.WRENCH), Ic2Items.BRONZE_INGOT, 6, 0.0F);
        smelt("tin_ingots_from_empty_fuel_can", Ingredient.of(Ic2Items.EMPTY_FUEL_CAN), Ic2Items.TIN_INGOT, 7, 0.0F);
        smelt("tin_ingot_from_tin_can", Ingredient.of(Ic2Items.TIN_CAN), Ic2Items.TIN_INGOT, 1, 0.0F);
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
        machine(MachineRecipe.Kind.MACERATOR, "spider_eye", Ingredient.of(Items.SPIDER_EYE), 1,
                Ic2Items.GRIN_POWDER, 2);
        machine(MachineRecipe.Kind.MACERATOR, "coffee_beans", Ingredient.of(Ic2Items.COFFEE_BEANS), 1,
                Ic2Items.COFFEE_POWDER, 3);
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
        // GregTech treeSapling/treeLeaves handler: 4 saplings or 8 leaves; IC2 rubber leaves were not "treeLeaves".
        machine(MachineRecipe.Kind.COMPRESSOR, "saplings", Ingredient.of(ItemTags.SAPLINGS), 4,
                Ic2Items.COMPRESSED_PLANT_BALL, 1);
        machine(MachineRecipe.Kind.COMPRESSOR, "leaves", DifferenceIngredient.of(Ingredient.of(ItemTags.LEAVES),
                Ingredient.of(Ic2Items.RUBBER_LEAVES)), 8, Ic2Items.COMPRESSED_PLANT_BALL, 1);
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
        machine(MachineRecipe.Kind.COMPRESSOR, "construction_foam", Ingredient.of(Ic2Items.CONSTRUCTION_FOAM), 1,
                Ic2Items.CONSTRUCTION_FOAM_PELLET, 1);
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
        machine(MachineRecipe.Kind.EXTRACTOR, "bio_cell", Ingredient.of(Ic2Items.BIO_CELL), 1, Ic2Items.BIOFUEL_CELL, 1);
        machine(MachineRecipe.Kind.EXTRACTOR, "hydrated_coal_cell", Ingredient.of(Ic2Items.HYDRATED_COAL_CELL), 1,
                Ic2Items.COALFUEL_CELL, 1);
        machine(MachineRecipe.Kind.EXTRACTOR, "water_cell", Ingredient.of(Ic2Items.WATER_CELL), 1,
                Ic2Items.HYDRATING_CELL, 1);
        machine(MachineRecipe.Kind.EXTRACTOR, "air_cell", Ingredient.of(Ic2Items.AIR_CELL), 1, Ic2Items.EMPTY_CELL, 1);
        // GregTech: only the plain and the spoiled (meta 1) filled tin can.
        for (int meta = 0; meta <= 1; meta++) {
            machine(MachineRecipe.Kind.EXTRACTOR, "filled_tin_can_" + meta, DataComponentIngredient.of(false,
                    Ic2DataComponents.CAN_META.get(), meta, Ic2Items.FILLED_TIN_CAN), 1, Ic2Items.TIN_CAN, 1);
        }
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

    /** IC2 terraformer blueprint: the empty blueprint surrounded by four items, and back (shapeless). */
    private void blueprint(DeferredItem<? extends Item> result, ItemLike material, boolean reversible) {
        shaped(result, 1, result.getId().getPath()).pattern(" S ", "S#S", " S ")
                .define('#', Ic2Items.TERRAFORMER_BLUEPRINT).define('S', material).save();
        if (reversible) {
            shapeless(Ic2Items.TERRAFORMER_BLUEPRINT, 1, "terraformer_blueprint_from_" + result.getId().getPath(),
                    Ingredient.of(result));
        }
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
        private boolean fixed;

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

        /** The rows form a full 3×3 grid that only matches at exactly this place (IC2 rows with blanks). */
        ShapedBuilder fixed() {
            this.fixed = true;
            return this;
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
            Optional<Ic2ShapedRecipe.FixedPosition> position = Optional.empty();
            if (fixed) {
                int top = 0;
                while (rows.get(top).isBlank()) {
                    top++;
                }
                int left = 0;
                while (columnBlank(left)) {
                    left++;
                }
                position = Optional.of(new Ic2ShapedRecipe.FixedPosition(left, top));
            }
            output.accept(id(name), new Ic2ShapedRecipe(ShapedRecipePattern.of(key, rows), stack, position), null);
        }

        private boolean columnBlank(int column) {
            return rows.stream().allMatch(row -> row.charAt(column) == ' ');
        }
    }
}
