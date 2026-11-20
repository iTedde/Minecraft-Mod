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
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.item.PSItems;
import ivorius.psychedelicraft.item.component.ItemFluids;
import ivorius.psychedelicraft.util.CodecUtils;
import ivorius.psychedelicraft.util.PacketCodecUtils;

/**
 * Created by Sollace on 7 Feb 2023
 *
 * Used by the mash table to produce a particular fluid from items dropped in.
 */
public record MashingRecipe (
        String mashingGroup,
        CraftingBookCategory category,
        ItemFluids.Predicate baseFluid,
        ItemFluids result,
        Ingredients ingredients,
        int stewTime) implements Recipe<MashingRecipe.Input> {
    public static final MapCodec<MashingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(MashingRecipe::mashingGroup),
            CraftingBookCategory.CODEC.optionalFieldOf("category", CraftingBookCategory.MISC).forGetter(MashingRecipe::category),
            ItemFluids.Predicate.CODEC.fieldOf("base_fluid").forGetter(MashingRecipe::baseFluid),
            ItemFluids.CODEC.fieldOf("result").forGetter(MashingRecipe::result),
            Ingredients.CODEC.fieldOf("ingredients").forGetter(MashingRecipe::ingredients),
            Codec.INT.optionalFieldOf("stew_time", 0).forGetter(MashingRecipe::stewTime)
    ).apply(instance, MashingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, MashingRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, MashingRecipe::mashingGroup,
            RecipeUtils.CRAFTING_RECIPE_CATEGORY_PACKET_CODEC, MashingRecipe::category,
            ItemFluids.Predicate.PACKET_CODEC, MashingRecipe::baseFluid,
            ItemFluids.PACKET_CODEC, MashingRecipe::result,
            Ingredients.PACKET_CODEC, MashingRecipe::ingredients,
            ByteBufCodecs.INT, MashingRecipe::stewTime,
            MashingRecipe::new
    );

    @Override
    public RecipeType<?> getType() {
        return PSRecipes.MASHING_TYPE;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.MASHING;
    }

    @Override
    public String getGroup() {
        return mashingGroup;
    }

    @Override
    public ItemStack getToastSymbol() {
        return PSItems.MASH_TUB.getDefaultInstance();
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return ingredients.ingredients();
    }

    @Override
    public boolean matches(Input input, Level world) {
        return !input.tankFluid().isEmpty()
                && baseFluid.test(input.tankFluid())
                && ingredients.matches(input);
    }

    public boolean matchesPartially(Input input, Level world) {
        return !input.tankFluid().isEmpty()
                && baseFluid.test(input.tankFluid())
                && ingredients.includes(input);
    }

    @Override
    public ItemStack assemble(Input input, Provider lookup) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return (width * height) > 0;
    }

    @Override
    public ItemStack getResultItem(Provider lookup) {
        return ItemStack.EMPTY;
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(Input input) {
        ItemMound unmatchedInputs = new ItemMound(input.inputs());
        ingredients.removeMatches(unmatchedInputs);
        return unmatchedInputs.convertToItemStacks();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public record Input(ItemFluids tankFluid, ItemStack solids, ItemMound inputs) implements RecipeInput {
        @Override
        public ItemStack getItem(int slot) {
            return solids;
        }

        @Override
        public int size() {
            return 1;
        }

        @Override
        public boolean isEmpty() {
            return solids.isEmpty() && tankFluid.isEmpty() && inputs.isEmpty();
        }
    }

    public record Ingredients (NonNullList<Entry> counts, NonNullList<Ingredient> ingredients) {
        public static final Codec<Ingredients> CODEC = CodecUtils.toDefaultedList(Entry.CODEC, Entry.EMPTY).xmap(Ingredients::new, Ingredients::counts);
        public static final StreamCodec<RegistryFriendlyByteBuf, Ingredients> PACKET_CODEC = Entry.PACKET_CODEC.apply(PacketCodecUtils.toDefaultedList()).map(Ingredients::new, Ingredients::counts);

        public Ingredients(NonNullList<Entry> counts) {
            this(counts, NonNullList.of(Ingredient.EMPTY, counts.stream().map(Entry::ingredient).toArray(Ingredient[]::new)));
        }

        public boolean matches(Input input) {
            return removeMatches(new ItemMound(input.inputs()));
        }

        public boolean includes(Input input) {
            return !input.inputs().isEmpty()
                && ingredients.stream().anyMatch(i -> input.inputs().countMatches(i) > 0);
        }

        /**
         * @param inputs The mound of items to consume
         * @return True if all ingredients are satisfied
         */
        public boolean removeMatches(ItemMound inputs) {
            return counts().stream().filter(ingredient -> {
                return !inputs.removeWhere(ingredient.ingredient(), ingredient.minimum());
            }).count() == 0;
        }

        public record Entry(Ingredient ingredient, int minimum) {
            public static final Entry EMPTY = new Entry(Ingredient.EMPTY, 0);
            public static final Codec<Entry> CODEC = RecordCodecBuilder.create(i -> i.group(
                    Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(Entry::ingredient),
                    Codec.INT.fieldOf("count").forGetter(Entry::minimum)
            ).apply(i, Entry::new));
            public static final StreamCodec<RegistryFriendlyByteBuf, Entry> PACKET_CODEC = StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, Entry::ingredient,
                    ByteBufCodecs.INT, Entry::minimum,
                    Entry::new
            );
        }

    }

    public enum MatchResult {
        NONE,
        INPUTS_ONLY,
        INGREDIENTS_ONLY,
        BOTH;

        public boolean isMatch() {
            return this == INPUTS_ONLY || this == BOTH;
        }

        public boolean isCraftable() {
            return this == BOTH;
        }

        public static MatchResult of(boolean inputs, boolean ingredients) {
            return inputs && ingredients ? BOTH : inputs ? INPUTS_ONLY : ingredients ? INGREDIENTS_ONLY : NONE;
        }
    }
}
