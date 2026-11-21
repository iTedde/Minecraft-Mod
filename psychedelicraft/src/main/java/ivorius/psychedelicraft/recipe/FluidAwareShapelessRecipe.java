/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.recipe;

import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapelessRecipe;
import net.minecraft.world.level.Level;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.recipe.ingredient.OptionalFluidIngredient;
import ivorius.psychedelicraft.util.PacketCodecUtils;

public class FluidAwareShapelessRecipe extends ShapelessRecipe {
    public static final MapCodec<FluidAwareShapelessRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(FluidAwareShapelessRecipe::getGroup),
            CraftingBookCategory.CODEC.fieldOf("category").orElse(CraftingBookCategory.MISC).forGetter(FluidAwareShapelessRecipe::category),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(recipe -> recipe.output),
            OptionalFluidIngredient.LIST_CODEC.fieldOf("ingredients").forGetter(recipe -> recipe.ingredients),
            Ingredient.CODEC.optionalFieldOf("destroy", Ingredient.of()).forGetter(recipe -> recipe.destructedIngredient)
    ).apply(instance, FluidAwareShapelessRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, FluidAwareShapelessRecipe> PACKET_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, FluidAwareShapelessRecipe::getGroup,
            RecipeUtils.CRAFTING_RECIPE_CATEGORY_PACKET_CODEC, FluidAwareShapelessRecipe::category,
            ItemStack.OPTIONAL_STREAM_CODEC, recipe -> recipe.output,
            OptionalFluidIngredient.PACKET_CODEC.apply(PacketCodecUtils.toDefaultedList()), recipe -> recipe.ingredients,
            Ingredient.CONTENTS_STREAM_CODEC, recipe -> recipe.destructedIngredient,
            FluidAwareShapelessRecipe::new
    );

    private final ItemStack output;
    private final NonNullList<OptionalFluidIngredient> ingredients;
    private final List<OptionalFluidIngredient> consumedFluids;
    private final Ingredient destructedIngredient;

    public FluidAwareShapelessRecipe(String group, CraftingBookCategory category, ItemStack output, NonNullList<OptionalFluidIngredient> input, Ingredient destructedIngredient) {
        super(group, category, output,
                // parent expects regular ingredients but we don't actually use them
                input.stream()
                .map(OptionalFluidIngredient::toVanilla)
                .collect(Collectors.toCollection(NonNullList::create))
        );
        this.output = output;
        this.ingredients = input;
        this.consumedFluids = ingredients.stream().filter(i -> i.fluid().isPresent()).toList();
        this.destructedIngredient = destructedIngredient;
    }

    public NonNullList<OptionalFluidIngredient> getFluidAwareIngredients() {
        return ingredients;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.CRAFTING_SHAPELESS_FLUID;
    }

    @Override
    public boolean matches(CraftingInput inventory, Level world) {
        List<OptionalFluidIngredient> unmatchedInputs = new ArrayList<>(ingredients);
        long matches = inventory.items().stream()
                    .filter(stack -> unmatchedInputs.stream()
                        .filter(ingredient -> ingredient.test(stack))
                        .findFirst()
                        .map(unmatchedInputs::remove)
                        .orElse(false)).count();
        return matches == ingredients.size()
                //&& matches == inventory.getStacks().size()
                && unmatchedInputs.isEmpty();
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(CraftingInput inventory) {
        NonNullList<ItemStack> defaultedList = NonNullList.withSize(inventory.size(), ItemStack.EMPTY);

        boolean destroyed = false;

        for (int i = 0; i < defaultedList.size(); ++i) {
            ItemStack stack = inventory.getItem(i);
            ItemFluids.Transaction t = ItemFluids.Transaction.begin(stack);

            if (!t.fluids().isEmpty() && consumedFluids.stream()
                .filter(ingredient -> ingredient.test(stack))
                .map(OptionalFluidIngredient::fluid)
                .flatMap(Optional::stream)
                .anyMatch(fluid -> {
                    t.withdraw(fluid.level().orElse(t.fluids().amount()));
                    return true;
                })) {
                defaultedList.set(i, t.toItemStack());
            } else {
                ItemStack remainder = stack.getCraftingRemainingItem();
                if (!remainder.isEmpty()) {
                    if (!destroyed && destructedIngredient.test(remainder)) {
                        destroyed = true;
                    } else {
                        defaultedList.set(i, remainder);
                    }
                }
            }
        }
        return defaultedList;
    }

}
