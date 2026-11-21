/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.recipe;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.item.crafting.SmeltingRecipe;
import net.minecraft.world.level.Level;
import java.lang.ref.WeakReference;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.recipe.ingredient.FluidIngredient;
import ivorius.psychedelicraft.recipe.ingredient.OptionalFluidIngredient;

/**
 * Created by Sollace on 5 Jan 2023
 *
 * Recipe that alters a container's fluid when cooked in a furnace.
 *
 *  {
 *    "type": "psychedelicraft:smelting_fluid",
 *    "cookingtime": 200,
 *    "experience": 0.2,
 *    "input": {
 *      "fluid": "psychedelicraft:coffee"
 *    },
 *    "result": {
 *      "item": "minecraft:empty", <empty to keep as the same>
 *      "attributes": {
 *        "temperature": {
 *          "type": "add",
 *          "value": 1
 *        }
 *      }
 *    }
 *  }
 *
 */
public class SmeltingFluidRecipe extends SmeltingRecipe {
    public static final MapCodec<SmeltingFluidRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(SmeltingFluidRecipe::getGroup),
            CookingBookCategory.CODEC.fieldOf("category").orElse(CookingBookCategory.MISC).forGetter(SmeltingFluidRecipe::category),
            OptionalFluidIngredient.CODEC.fieldOf("input").forGetter(recipe -> recipe.input),
            FluidModifyingResult.CODEC.fieldOf("result").forGetter(recipe -> recipe.result),
            Codec.FLOAT.fieldOf("experience").forGetter(SmeltingFluidRecipe::getExperience),
            Codec.INT.optionalFieldOf("cookingTIme", 200).forGetter(SmeltingFluidRecipe::getCookingTime)
        ).apply(instance, SmeltingFluidRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, SmeltingFluidRecipe> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, SmeltingFluidRecipe::getGroup,
            RecipeUtils.COOKING_RECIPE_CATEGORY_PACKET_CODEC, SmeltingFluidRecipe::category,
            OptionalFluidIngredient.PACKET_CODEC, recipe -> recipe.input,
            FluidModifyingResult.PACKET_CODEC, recipe -> recipe.result,
            ByteBufCodecs.FLOAT, SmeltingFluidRecipe::getExperience,
            ByteBufCodecs.INT, SmeltingFluidRecipe::getCookingTime,
            SmeltingFluidRecipe::new
    );

    private final OptionalFluidIngredient input;
    private final FluidModifyingResult result;

    private WeakReference<SingleRecipeInput> lastQueriedInventory = new WeakReference<>(null);

    public SmeltingFluidRecipe(
            String group, CookingBookCategory category,
            OptionalFluidIngredient input,
            FluidModifyingResult result,
            float experience, int cookingTime) {
        super(group, category, input.toVanilla(), result.result(), experience, cookingTime);
        this.input = input;
        this.result = result;
    }

    public FluidIngredient getFluid() {
        return input.fluid().orElseThrow();
    }

    public FluidModifyingResult getResult() {
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.SMELTING_RECEPTICAL;
    }

    @Override
    public boolean matches(SingleRecipeInput inventory, Level world) {
        lastQueriedInventory = new WeakReference<>(inventory);
        return input.test(inventory.item());
    }

    @Override
    public ItemStack getResultItem(Provider registries) {
        SingleRecipeInput inventory = lastQueriedInventory.get();
        if (inventory == null) {
            return super.getResultItem(registries);
        }
        return assemble(inventory, registries);
    }

    @Override
    public ItemStack assemble(SingleRecipeInput inventory, Provider registries) {
        lastQueriedInventory = new WeakReference<>(inventory);
        return result.applyTo(inventory.item());
    }
}
