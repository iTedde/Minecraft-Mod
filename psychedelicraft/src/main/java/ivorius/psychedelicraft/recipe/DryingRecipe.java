package ivorius.psychedelicraft.recipe;

import java.util.List;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import ivorius.psychedelicraft.item.PSItems;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public record DryingRecipe(
        String dryingGroup,
        Ingredient input,
        ItemStack output,
        float experience,
        float cookTime
    ) implements Recipe<DryingRecipe.Input> {
    public static final MapCodec<DryingRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            Codec.STRING.optionalFieldOf("group", "").forGetter(DryingRecipe::dryingGroup),
            Ingredient.CODEC.fieldOf("ingredient").forGetter(DryingRecipe::input),
            ItemStack.STRICT_CODEC.fieldOf("result").forGetter(DryingRecipe::output),
            Codec.FLOAT.optionalFieldOf("experience", 0F).forGetter(DryingRecipe::experience),
            Codec.FLOAT.optionalFieldOf("cookingTime", 1F).forGetter(DryingRecipe::cookTime)
        ).apply(instance, DryingRecipe::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, DryingRecipe> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, DryingRecipe::dryingGroup,
            Ingredient.CONTENTS_STREAM_CODEC, DryingRecipe::input,
            ItemStack.OPTIONAL_STREAM_CODEC, DryingRecipe::output,
            ByteBufCodecs.FLOAT, DryingRecipe::experience,
            ByteBufCodecs.FLOAT, DryingRecipe::cookTime,
            DryingRecipe::new
    );

    @Override
    public RecipeSerializer<?> getSerializer() {
        return PSRecipes.DRYING;
    }

    @Override
    public RecipeType<?> getType() {
        return PSRecipes.DRYING_TYPE;
    }

    @Override
    public String getGroup() {
        return dryingGroup;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, input);
    }

    @Override
    public ItemStack getToastSymbol() {
        return PSItems.DRYING_TABLE.getDefaultInstance();
    }

    @Override
    public boolean matches(Input input, Level world) {
        return (input.result.isEmpty() || ItemStack.isSameItemSameComponents(output, input.result())) && input.ingredients().stream()
                .filter(this.input)
                .mapToInt(ItemStack::getCount)
                .sum() >= 9;
    }

    // Suppress warnings being logged when Minecraft realises it doesn't know what category to put these recipes into
    // The mashing tub doesn't have a recipe book anyway
    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public ItemStack assemble(Input input, Provider lookup) {
        return getResultItem(lookup);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(Provider registriesLookup) {
        return output.copy();
    }

    @Override
    public NonNullList<ItemStack> getRemainingItems(DryingRecipe.Input input) {
        NonNullList<ItemStack> defaultedList = NonNullList.withSize(input.size(), ItemStack.EMPTY);
        int toConsume = 9;

        for (int i = 0; i < defaultedList.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (toConsume > 0 && input().test(stack)) {
                stack = stack.copy();
                toConsume -= stack.split(toConsume).getCount();
            }

            if (!stack.isEmpty()) {
                defaultedList.set(i, stack);
            }
        }

        return defaultedList;
    }

    public record Input(ItemStack result, List<ItemStack> ingredients) implements RecipeInput {
        @Override
        public ItemStack getItem(int slot) {
            return ingredients.get(slot);
        }

        @Override
        public int size() {
            return ingredients.size();
        }
    }
}
