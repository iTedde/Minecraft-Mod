package dev.craft.core.client;

import java.util.Map;

import dev.craft.Craft;
import net.minecraft.resources.ResourceLocation;

/** Ids of the recipe viewer categories, shared by the GUIs ({@link RecipeArea}) and the EMI integration. */
public final class RecipeCategories {
    public static final ResourceLocation SMELTING = ResourceLocation.withDefaultNamespace("smelting");

    // IndustrialCraft 2
    public static final ResourceLocation MACERATOR = id("macerator");
    public static final ResourceLocation EXTRACTOR = id("extractor");
    public static final ResourceLocation COMPRESSOR = id("compressor");
    /** Advanced Machines: the rotary macerator's supplement slot (spec 25 §4). */
    public static final ResourceLocation ROTARY_MACERATOR_SUPPLEMENT = id("rotary_macerator_supplement");
    public static final ResourceLocation RECYCLER = id("recycler");
    public static final ResourceLocation SCRAP_BOX = id("scrap_box");
    public static final ResourceLocation MASS_FABRICATOR = id("mass_fabricator");
    public static final ResourceLocation CANNING_MACHINE = id("canning_machine");
    public static final ResourceLocation ELECTROLYZER = id("electrolyzer");
    public static final ResourceLocation GENERATOR = id("generator");
    public static final ResourceLocation GEOTHERMAL_GENERATOR = id("geothermal_generator");
    public static final ResourceLocation CROPS = id("crops");

    // GregTech (ids of the machine blocks)
    public static final ResourceLocation GT_BENDER = id("plate_bending_machine");
    public static final ResourceLocation GT_ELECTROLYZER = id("industrial_electrolyzer");
    public static final ResourceLocation GT_BLAST_FURNACE = id("industrial_blast_furnace");
    public static final ResourceLocation GT_ASSEMBLER = id("assembling_machine");
    public static final ResourceLocation GT_CANNER = id("automatic_canning_machine");
    public static final ResourceLocation GT_IMPLOSION = id("implosion_compressor");
    public static final ResourceLocation GT_CENTRIFUGE = id("industrial_centrifuge");
    public static final ResourceLocation GT_GRINDER = id("industrial_grinder");
    public static final ResourceLocation GT_VACUUM_FREEZER = id("vacuum_freezer");

    // BuildCraft, Forestry and the power plants
    public static final ResourceLocation REFINERY = id("refinery");
    public static final ResourceLocation COMBUSTION_ENGINE = id("combustion_engine");
    public static final ResourceLocation FUEL = ResourceLocation.withDefaultNamespace("fuel");
    public static final ResourceLocation FABRICATOR = id("fabricator");
    public static final ResourceLocation FABRICATOR_SMELTING = id("fabricator_smelting");
    public static final ResourceLocation CARPENTER = id("carpenter");
    public static final ResourceLocation COKE_OVEN = id("coke_oven");
    public static final ResourceLocation BOILER = id("boiler");
    public static final ResourceLocation STEAM_TURBINE = id("steam_turbine");

    /**
     * Category of every recipe type the mod registers (path of the type id). A GameTest checks that no recipe type
     * is missing here, so new machines also get a recipe viewer category.
     */
    public static final Map<String, ResourceLocation> BY_RECIPE_TYPE = Map.ofEntries(
            Map.entry("macerator", MACERATOR),
            Map.entry("extractor", EXTRACTOR),
            Map.entry("compressor", COMPRESSOR),
            Map.entry("gt_bender", GT_BENDER),
            Map.entry("gt_electrolyzer", GT_ELECTROLYZER),
            Map.entry("gt_blast_furnace", GT_BLAST_FURNACE),
            Map.entry("gt_assembler", GT_ASSEMBLER),
            Map.entry("gt_canner", GT_CANNER),
            Map.entry("gt_implosion", GT_IMPLOSION),
            Map.entry("gt_centrifuge", GT_CENTRIFUGE),
            Map.entry("gt_grinder", GT_GRINDER),
            Map.entry("gt_vacuum_freezer", GT_VACUUM_FREEZER),
            Map.entry("fabricator", FABRICATOR),
            Map.entry("carpenter", CARPENTER));

    private RecipeCategories() {
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Craft.MODID, path);
    }
}
