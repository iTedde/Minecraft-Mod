package dev.craft.ic2.recipe;

import java.util.Optional;
import java.util.function.Supplier;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;

/**
 * Recipe of the macerator, extractor and compressor (IC2 {@code Ic2Recipes.add*Recipe}): an input with a minimum
 * amount and one result. The input stack must hold at least {@code count} items, and exactly {@code count} are used.
 */
public class MachineRecipe implements Recipe<SingleRecipeInput> {
    private final Kind kind;
    private final Ingredient ingredient;
    private final int count;
    private final ItemStack result;

    public MachineRecipe(Kind kind, Ingredient ingredient, int count, ItemStack result) {
        this.kind = kind;
        this.ingredient = ingredient;
        this.count = count;
        this.result = result;
    }

    public Ingredient getIngredient() {
        return ingredient;
    }

    public int getCount() {
        return count;
    }

    public ItemStack getResult() {
        return result;
    }

    @Override
    public boolean matches(SingleRecipeInput input, Level level) {
        ItemStack stack = input.item();
        return ingredient.test(stack) && stack.getCount() >= count;
    }

    @Override
    public ItemStack assemble(SingleRecipeInput input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, ingredient);
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return kind.serializer.get();
    }

    @Override
    public RecipeType<?> getType() {
        return kind.type.get();
    }

    /**
     * IC2 {@code get*OutputFor}: the result for {@code stack}, or empty. With {@code adjustInput} the recipe amount
     * is removed from {@code stack}.
     */
    public static ItemStack getOutputFor(Level level, Kind kind, ItemStack stack, boolean adjustInput) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        Optional<RecipeHolder<MachineRecipe>> recipe = level.getRecipeManager()
                .getRecipeFor(kind.type.get(), new SingleRecipeInput(stack), level);
        if (recipe.isEmpty()) {
            return ItemStack.EMPTY;
        }
        if (adjustInput) {
            stack.shrink(recipe.get().value().count);
        }
        return recipe.get().value().result;
    }

    /** The three IC2 recipe lists sharing this recipe class. */
    public enum Kind {
        MACERATOR("macerator"),
        EXTRACTOR("extractor"),
        COMPRESSOR("compressor");

        private final String name;
        private Supplier<RecipeType<MachineRecipe>> type;
        private Supplier<RecipeSerializer<MachineRecipe>> serializer;

        Kind(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public RecipeType<MachineRecipe> type() {
            return type.get();
        }

        public void bind(Supplier<RecipeType<MachineRecipe>> type, Supplier<RecipeSerializer<MachineRecipe>> serializer) {
            this.type = type;
            this.serializer = serializer;
        }
    }

    public static final class Serializer implements RecipeSerializer<MachineRecipe> {
        private final MapCodec<MachineRecipe> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, MachineRecipe> streamCodec;

        public Serializer(Kind kind) {
            this.codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Ingredient.CODEC_NONEMPTY.fieldOf("ingredient").forGetter(MachineRecipe::getIngredient),
                    Codec.intRange(1, 64).optionalFieldOf("count", 1).forGetter(MachineRecipe::getCount),
                    ItemStack.STRICT_CODEC.fieldOf("result").forGetter(MachineRecipe::getResult)
            ).apply(instance, (ingredient, count, result) -> new MachineRecipe(kind, ingredient, count, result)));
            this.streamCodec = StreamCodec.composite(
                    Ingredient.CONTENTS_STREAM_CODEC, MachineRecipe::getIngredient,
                    ByteBufCodecs.VAR_INT, MachineRecipe::getCount,
                    ItemStack.STREAM_CODEC, MachineRecipe::getResult,
                    (ingredient, count, result) -> new MachineRecipe(kind, ingredient, count, result));
        }

        @Override
        public MapCodec<MachineRecipe> codec() {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, MachineRecipe> streamCodec() {
            return streamCodec;
        }
    }
}
