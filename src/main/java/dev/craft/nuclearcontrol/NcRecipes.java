package dev.craft.nuclearcontrol;

import java.util.function.Supplier;

import dev.craft.Craft;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class NcRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, Craft.MODID);

    public static final Supplier<RecipeSerializer<EnergyArrayRecipe>> ENERGY_ARRAY =
            SERIALIZERS.register("energy_array_card", () -> new SimpleCraftingRecipeSerializer<>(EnergyArrayRecipe::new));

    private NcRecipes() {
    }
}
