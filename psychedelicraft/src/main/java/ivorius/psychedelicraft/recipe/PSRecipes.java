package ivorius.psychedelicraft.recipe;

import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.Psychedelicraft;
import ivorius.psychedelicraft.recipe.ingredient.PSIngredients;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;

/**
 * @author Sollace
 * @since 5 Jan 2023
 */
public interface PSRecipes {
    RecipeSerializer<MixingRecipe> FILL_RECEPTICAL = serializer("fill_receptical", new Serializer<>(MixingRecipe.CODEC, MixingRecipe.STREAM_CODEC));
    RecipeSerializer<ChangeRecepticalRecipe> CHANGE_RECEPTICAL = serializer("change_receptical", new Serializer<>(ChangeRecepticalRecipe.CODEC, ChangeRecepticalRecipe.PACKET_CODEC));
    RecipeSerializer<PouringRecipe> CRAFTING_POURING = serializer("crafting_pouring", new SimpleCraftingRecipeSerializer<>(PouringRecipe::new));
    RecipeSerializer<SmeltingFluidRecipe> SMELTING_RECEPTICAL = serializer("smelting_receptical", new Serializer<>(SmeltingFluidRecipe.CODEC, SmeltingFluidRecipe.PACKET_CODEC));
    RecipeSerializer<BottleRecipe> CRAFTING_SHAPED = serializer("crafting_shaped", new Serializer<>(BottleRecipe.CODEC, BottleRecipe.PACKET_CODEC));
    RecipeSerializer<FluidAwareShapelessRecipe> CRAFTING_SHAPELESS_FLUID = serializer("crafting_shapeless_fluid", new Serializer<>(FluidAwareShapelessRecipe.CODEC, FluidAwareShapelessRecipe.PACKET_CODEC));

    RecipeType<MashingRecipe> MASHING_TYPE = type("mashing");
    RecipeSerializer<MashingRecipe> MASHING = serializer("mashing", new Serializer<>(MashingRecipe.CODEC, MashingRecipe.STREAM_CODEC));

    RecipeType<BunsenBurnerRecipe> CHEMISTRY = type("chemistry");
    RecipeSerializer<ReactingRecipe> REACTING = serializer("reacting", new Serializer<>(ReactingRecipe.CODEC, ReactingRecipe.STREAM_CODEC));

    RecipeType<DryingRecipe> DRYING_TYPE = type("drying");
    RecipeSerializer<DryingRecipe> DRYING = serializer("drying", new Serializer<>(DryingRecipe.CODEC, DryingRecipe.STREAM_CODEC));

    RecipeType<HardeningRecipe> TRAY = type("tray");
    RecipeSerializer<HardeningRecipe> HARDENING = serializer("hardening", new Serializer<>(HardeningRecipe.CODEC, HardeningRecipe.STREAM_CODEC));

    static <T extends Recipe<?>> RecipeType<T> type(String name) {
        ResourceLocation id = Psychedelicraft.id(name);
        return Registry.register(BuiltInRegistries.RECIPE_TYPE, id, new RecipeType<T>() {
            @Override
            public String toString() {
                return id.toString();
            }
        });
    }

    static <S extends RecipeSerializer<T>, T extends Recipe<?>> S serializer(String name, S serializer) {
        return Registry.register(BuiltInRegistries.RECIPE_SERIALIZER, Psychedelicraft.id(name), serializer);
    }

    static void bootstrap() {
        PSIngredients.bootstrap();
    }

    record Serializer<T extends Recipe<?>> (
            MapCodec<T> codec,
            StreamCodec<RegistryFriendlyByteBuf, T> streamCodec
        ) implements RecipeSerializer<T> {
    }
}
