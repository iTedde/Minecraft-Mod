package ivorius.psychedelicraft.recipe.ingredient;

import net.neoforged.neoforge.common.crafting.IngredientType;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import net.minecraft.core.Registry;

import com.mojang.serialization.MapCodec;

import ivorius.psychedelicraft.Psychedelicraft;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

public interface PSIngredients {
    IngredientType<FluidIngredient> FLUID = register("fluid", FluidIngredient.MAP_CODEC, FluidIngredient.PACKET_CODEC);
    IngredientType<OptionalFluidIngredient> OPTIONAL_FLUID = register("optional_fluid", OptionalFluidIngredient.CODEC, OptionalFluidIngredient.PACKET_CODEC);

    private static <T extends net.neoforged.neoforge.common.crafting.ICustomIngredient> IngredientType<T> register(String name, MapCodec<T> codec, StreamCodec<RegistryFriendlyByteBuf, T> packetCodec) {
        return Registry.register(NeoForgeRegistries.INGREDIENT_TYPES, Psychedelicraft.id(name), new IngredientType<>(codec, packetCodec));
    }

    static void bootstrap() {}
}
