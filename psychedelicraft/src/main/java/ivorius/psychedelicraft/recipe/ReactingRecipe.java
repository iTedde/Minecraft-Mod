/*
 *  Copyright (c) 2014, Lukas Tenbrink.
 *  * http://lukas.axxim.net
 */

package ivorius.psychedelicraft.recipe;

import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.Impurities;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.recipe.ingredient.FluidIngredient;
import ivorius.psychedelicraft.util.CodecUtils;
import ivorius.psychedelicraft.util.PacketCodecUtils;

/**
 * Created by Sollace on 19 Jul 2024
 *
 * Used by the bunsen burner to produce the correct fluid type for ingredients dropped into it
 */
public record ReactingRecipe (
        ReactionType reactionType,
        String reducingGroup,
        CraftingBookCategory category,
        Result result,
        Ingredients ingredients,
        int stewTime) implements BunsenBurnerRecipe {
    public static final MapCodec<ReactingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            ReactionType.CODEC.optionalFieldOf("reaction_type", ReactionType.INGREDIENTS).forGetter(ReactingRecipe::reactionType),
            Codec.STRING.optionalFieldOf("group", "").forGetter(ReactingRecipe::reducingGroup),
            CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC).forGetter(ReactingRecipe::category),
            Result.CODEC.fieldOf("result").forGetter(ReactingRecipe::result),
            Ingredients.CODEC.fieldOf("ingredients").forGetter(ReactingRecipe::ingredients),
            Codec.INT.optionalFieldOf("stew_time", 0).forGetter(ReactingRecipe::stewTime)
    ).apply(instance, ReactingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, ReactingRecipe> STREAM_CODEC = StreamCodec.composite(
            ReactionType.PACKET_CODEC, ReactingRecipe::reactionType,
            ByteBufCodecs.STRING_UTF8, ReactingRecipe::reducingGroup,
            RecipeUtils.CRAFTING_RECIPE_CATEGORY_PACKET_CODEC, ReactingRecipe::category,
            Result.PACKET_CODEC, ReactingRecipe::result,
            Ingredients.PACKET_CODEC, ReactingRecipe::ingredients,
            ByteBufCodecs.INT, ReactingRecipe::stewTime,
            ReactingRecipe::new
    );

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.REACTING;
    }

    @Override
    public String getGroup() {
        return reducingGroup;
    }

    @Override
    public ItemStack getToastSymbol() {
        return PSItems.BUNSEN_BURNER.getDefaultInstance();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return ingredients.solids();
    }

    @Override
    public boolean matches(Input input, Level world) {
        return reactionType == input.type()
                && ingredients.matchSolids(new ItemMound(input.input()))
                && ingredients.matchFluids(FluidMound.of(input.fluids()));
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ItemStack assemble(Input input, Provider lookup) {
        if (ingredients.matchSolids(input.input())) {
            if (reactionType == ReactionType.ADDITIONS) {
                result.impurity.ifPresent(input.consumer()::accept);
            } else {
                int level = ingredients.consumeMatchingFluids(input.fluids());
                if (!result.fluid().isEmpty()) {
                    input.consumer().accept(level == 0 ? result.fluid() : result.fluid().ofAmount(result.fluid().amount() * level));
                    result.impurity.ifPresent(input.consumer()::accept);
                }
            }
        }
        return result.byProduct();
    }

    @Override
    public ItemStack getResultItem(Provider lookup) {
        return result.byProduct();
    }

    public record Result (
            ItemFluids fluid,
            ItemStack byProduct,
            Optional<Impurities.Impurity> impurity
    ) {
        public static final MapCodec<Result> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ItemFluids.CODEC.fieldOf("fluid").forGetter(Result::fluid),
                ItemStack.OPTIONAL_CODEC.optionalFieldOf("by_product", ItemStack.EMPTY).forGetter(Result::byProduct),
                Impurities.Impurity.CODEC.optionalFieldOf("impurity").forGetter(Result::impurity)
        ).apply(instance, Result::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Result> PACKET_CODEC = StreamCodec.composite(
                ItemFluids.PACKET_CODEC, Result::fluid,
                ItemStack.OPTIONAL_STREAM_CODEC, Result::byProduct,
                ByteBufCodecs.optional(PacketCodecUtils.ofEnum(Impurities.Impurity.class)), Result::impurity,
                Result::new
        );
    }

    public record Ingredients(
            /**
             * Required input fluids (optional)
             */
            NonNullList<FluidIngredient> fluids,
            /**
             * Required input item (optional)
             */
            NonNullList<Ingredient> solids
    ) {
        public static final MapCodec<Ingredients> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                CodecUtils.toDefaultedList(FluidIngredient.CODEC, FluidIngredient.EMPTY).optionalFieldOf("fluids", NonNullList.create()).forGetter(Ingredients::fluids),
                CodecUtils.toDefaultedList(Ingredient.CODEC_NONEMPTY, Ingredient.EMPTY).optionalFieldOf("solids", NonNullList.create()).forGetter(Ingredients::solids)
        ).apply(instance, Ingredients::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Ingredients> PACKET_CODEC = StreamCodec.composite(
                FluidIngredient.PACKET_CODEC.apply(PacketCodecUtils.toDefaultedList()), Ingredients::fluids,
                Ingredient.CONTENTS_STREAM_CODEC.apply(PacketCodecUtils.toDefaultedList()), Ingredients::solids,
                Ingredients::new
        );

        public boolean matchSolids(ItemMound items) {
            if (solids().isEmpty()) {
                return items.isEmpty();
            }

            return solids().stream().allMatch(solid -> items.removeWhere(solid, 1));
        }

        public boolean matchFluids(FluidMound fluids) {
            return fluids().isEmpty() || fluids().stream().allMatch(ingredient -> fluids.removeMatch(ingredient, -1) > 0);
        }

        public int consumeMatchingFluids(FluidMound fluids) {
            return fluids().isEmpty() ? 0 : fluids().stream().mapToInt(ingredient -> fluids.removeMatch(ingredient, -1)).sum();
        }
    }
}
