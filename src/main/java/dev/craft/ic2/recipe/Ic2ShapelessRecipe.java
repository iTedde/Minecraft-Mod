package dev.craft.ic2.recipe;

import com.mojang.serialization.DataResult;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.craft.ic2.Ic2Recipes;
import net.minecraft.core.HolderLookup;
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

/** Shapeless crafting recipe with IC2 {@code AdvShapelessRecipe} charge transfer. */
public class Ic2ShapelessRecipe extends ShapelessRecipe {
    private final ItemStack result;

    public Ic2ShapelessRecipe(ItemStack result, NonNullList<Ingredient> ingredients) {
        super("", CraftingBookCategory.MISC, result, ingredients);
        this.result = result;
    }

    public ItemStack getResult() {
        return result;
    }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        return ChargeTransfer.apply(result.copy(), input);
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Ic2Recipes.SHAPELESS.get();
    }

    public static final class Serializer implements RecipeSerializer<Ic2ShapelessRecipe> {
        private static final MapCodec<Ic2ShapelessRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(Ic2ShapelessRecipe::getResult),
                Ingredient.CODEC_NONEMPTY.listOf().fieldOf("ingredients").flatXmap(list -> {
                    if (list.isEmpty() || list.size() > 9) {
                        return DataResult.error(() -> "Invalid number of ingredients");
                    }
                    return DataResult.success(NonNullList.of(Ingredient.EMPTY, list.toArray(Ingredient[]::new)));
                }, DataResult::success).forGetter(Ic2ShapelessRecipe::getIngredients)
        ).apply(instance, Ic2ShapelessRecipe::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, Ic2ShapelessRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        ItemStack.STREAM_CODEC, Ic2ShapelessRecipe::getResult,
                        Ingredient.CONTENTS_STREAM_CODEC.apply(ByteBufCodecs.collection(NonNullList::createWithCapacity)),
                        Ic2ShapelessRecipe::getIngredients,
                        Ic2ShapelessRecipe::new);

        @Override
        public MapCodec<Ic2ShapelessRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, Ic2ShapelessRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
