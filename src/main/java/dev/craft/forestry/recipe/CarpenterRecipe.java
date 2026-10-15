package dev.craft.forestry.recipe;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import dev.craft.forestry.FrRecipes;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.ShapedRecipePattern;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Forestry {@code MachineCarpenter.Recipe}: number of work steps ({@code packagingTime}), an optional liquid (only
 * the kind is matched, the amount is checked as resource), an optional box and a shaped pattern (mirroring allowed).
 */
public class CarpenterRecipe implements Recipe<CarpenterRecipe.Input> {
    private final int packagingTime;
    private final FluidStack liquid;
    private final Optional<Ingredient> box;
    private final ShapedRecipePattern pattern;
    private final ItemStack result;

    public CarpenterRecipe(int packagingTime, FluidStack liquid, Optional<Ingredient> box,
                           ShapedRecipePattern pattern, ItemStack result) {
        this.packagingTime = packagingTime;
        this.liquid = liquid;
        this.box = box;
        this.pattern = pattern;
        this.result = result;
    }

    public int getPackagingTime() {
        return packagingTime;
    }

    /** The liquid used per product; empty if the recipe needs none. */
    public FluidStack getLiquid() {
        return liquid;
    }

    public Optional<Ingredient> getBox() {
        return box;
    }

    public ShapedRecipePattern getPattern() {
        return pattern;
    }

    public ItemStack getResult() {
        return result;
    }

    /** {@code hasLiquid}: the recipe uses this kind of liquid. */
    public boolean hasLiquid(FluidStack resource) {
        return !liquid.isEmpty() && !resource.isEmpty() && FluidStack.isSameFluidSameComponents(liquid, resource);
    }

    /** {@code hasBox}: a recipe without box accepts any item as box (original quirk). */
    public boolean hasBox(ItemStack stack) {
        return box.isEmpty() || box.get().test(stack);
    }

    /** {@code isIngredient}: the item is part of the pattern. */
    public boolean isIngredient(ItemStack stack) {
        for (Ingredient ingredient : pattern.ingredients()) {
            if (!ingredient.isEmpty() && ingredient.test(stack)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean matches(Input input, Level level) {
        if (!liquid.isEmpty() && !FluidStack.isSameFluidSameComponents(liquid, input.liquid())) {
            return false;
        }
        if (box.isPresent() && !box.get().test(input.box())) {
            return false;
        }
        return pattern.matches(input.grid());
    }

    @Override
    public ItemStack assemble(Input input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width >= pattern.width() && height >= pattern.height();
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return pattern.ingredients();
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return FrRecipes.CARPENTER_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return FrRecipes.CARPENTER.get();
    }

    /** Liquid tank, box slot and pattern grid of the carpenter. */
    public record Input(FluidStack liquid, ItemStack box, CraftingInput grid) implements RecipeInput {
        @Override
        public ItemStack getItem(int index) {
            return grid.getItem(index);
        }

        @Override
        public int size() {
            return grid.size();
        }
    }

    public static final class Serializer implements RecipeSerializer<CarpenterRecipe> {
        private static final MapCodec<CarpenterRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Codec.intRange(1, Integer.MAX_VALUE).fieldOf("packaging_time")
                        .forGetter(CarpenterRecipe::getPackagingTime),
                FluidStack.OPTIONAL_CODEC.optionalFieldOf("liquid", FluidStack.EMPTY)
                        .forGetter(CarpenterRecipe::getLiquid),
                Ingredient.CODEC_NONEMPTY.optionalFieldOf("box").forGetter(CarpenterRecipe::getBox),
                ShapedRecipePattern.MAP_CODEC.forGetter(CarpenterRecipe::getPattern),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(CarpenterRecipe::getResult)
        ).apply(instance, CarpenterRecipe::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, CarpenterRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.VAR_INT, CarpenterRecipe::getPackagingTime,
                        FluidStack.OPTIONAL_STREAM_CODEC, CarpenterRecipe::getLiquid,
                        ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC), CarpenterRecipe::getBox,
                        ShapedRecipePattern.STREAM_CODEC, CarpenterRecipe::getPattern,
                        ItemStack.STREAM_CODEC, CarpenterRecipe::getResult,
                        CarpenterRecipe::new);

        @Override
        public MapCodec<CarpenterRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, CarpenterRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
