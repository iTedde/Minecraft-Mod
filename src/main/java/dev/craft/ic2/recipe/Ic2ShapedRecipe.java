package dev.craft.ic2.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.craft.ic2.Ic2Recipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.ShapedRecipe;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;

/**
 * Shaped crafting recipe with IC2 {@code AdvRecipe} semantics: patterns are never mirrored, and the charge of
 * electric ingredients is transferred to an electric result.
 */
public class Ic2ShapedRecipe extends ShapedRecipe {
    private final ShapedRecipePattern pattern;
    private final ItemStack result;

    public Ic2ShapedRecipe(ShapedRecipePattern pattern, ItemStack result) {
        super("", CraftingBookCategory.MISC, pattern, result);
        this.pattern = pattern;
        this.result = result;
    }

    public ShapedRecipePattern getPattern() {
        return pattern;
    }

    public ItemStack getResult() {
        return result;
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        if (input.width() != pattern.width() || input.height() != pattern.height()) {
            return false;
        }
        for (int y = 0; y < pattern.height(); y++) {
            for (int x = 0; x < pattern.width(); x++) {
                Ingredient ingredient = pattern.ingredients().get(x + y * pattern.width());
                if (!ingredient.test(input.getItem(x + y * input.width()))) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return ChargeTransfer.apply(result.copy(), input);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Ic2Recipes.SHAPED.get();
    }

    public static final class Serializer implements RecipeSerializer<Ic2ShapedRecipe> {
        private static final MapCodec<Ic2ShapedRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ShapedRecipePattern.MAP_CODEC.forGetter(Ic2ShapedRecipe::getPattern),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(Ic2ShapedRecipe::getResult)
        ).apply(instance, Ic2ShapedRecipe::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, Ic2ShapedRecipe> STREAM_CODEC = StreamCodec.composite(
                ShapedRecipePattern.STREAM_CODEC, Ic2ShapedRecipe::getPattern,
                ItemStack.STREAM_CODEC, Ic2ShapedRecipe::getResult,
                Ic2ShapedRecipe::new);

        @Override
        public MapCodec<Ic2ShapedRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, Ic2ShapedRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
