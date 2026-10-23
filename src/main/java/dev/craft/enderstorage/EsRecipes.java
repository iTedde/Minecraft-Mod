package dev.craft.enderstorage;

import java.util.function.Supplier;

import dev.craft.Craft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class EsRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Craft.MODID);

    public static final Supplier<RecipeSerializer<EnderStorageRecolourRecipe>> RECOLOUR =
            SERIALIZERS.register("ender_storage_recolour",
                    () -> new SimpleCraftingRecipeSerializer<>(EnderStorageRecolourRecipe::new));

    private EsRecipes() {
    }
}
