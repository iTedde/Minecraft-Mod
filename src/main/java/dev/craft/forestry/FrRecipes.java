package dev.craft.forestry;

import java.util.function.Supplier;

import dev.craft.Craft;
import dev.craft.forestry.recipe.CarpenterRecipe;
import dev.craft.forestry.recipe.FabricatorRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class FrRecipes {
    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, Craft.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Craft.MODID);

    public static final Supplier<RecipeType<FabricatorRecipe>> FABRICATOR = TYPES.register("fabricator",
            () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "fabricator")));
    public static final Supplier<RecipeSerializer<FabricatorRecipe>> FABRICATOR_SERIALIZER =
            SERIALIZERS.register("fabricator", FabricatorRecipe.Serializer::new);
    public static final Supplier<RecipeType<CarpenterRecipe>> CARPENTER = TYPES.register("carpenter",
            () -> RecipeType.simple(ResourceLocation.fromNamespaceAndPath(Craft.MODID, "carpenter")));
    public static final Supplier<RecipeSerializer<CarpenterRecipe>> CARPENTER_SERIALIZER =
            SERIALIZERS.register("carpenter", CarpenterRecipe.Serializer::new);

    private FrRecipes() {
    }
}
