package dev.craft.core.client;

import dev.craft.Craft;
import net.minecraft.resources.ResourceLocation;

/** Ids of the recipe viewer categories, shared by the GUIs ({@link RecipeArea}) and the EMI integration. */
public final class RecipeCategories {
    public static final ResourceLocation SMELTING = ResourceLocation.withDefaultNamespace("smelting");

    // IndustrialCraft 2
    public static final ResourceLocation MACERATOR = id("macerator");
    public static final ResourceLocation EXTRACTOR = id("extractor");
    public static final ResourceLocation COMPRESSOR = id("compressor");
    public static final ResourceLocation RECYCLER = id("recycler");
    public static final ResourceLocation SCRAP_BOX = id("scrap_box");
    public static final ResourceLocation MASS_FABRICATOR = id("mass_fabricator");
    public static final ResourceLocation CANNING_MACHINE = id("canning_machine");
    public static final ResourceLocation ELECTROLYZER = id("electrolyzer");
    public static final ResourceLocation GENERATOR = id("generator");
    public static final ResourceLocation GEOTHERMAL_GENERATOR = id("geothermal_generator");

    private RecipeCategories() {
    }

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(Craft.MODID, path);
    }
}
