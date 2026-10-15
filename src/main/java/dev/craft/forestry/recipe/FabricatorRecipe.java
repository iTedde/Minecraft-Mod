package dev.craft.forestry.recipe;

import java.util.Optional;

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
 * Forestry {@code MachineFabricator.Recipe}: an optional plan, molten liquid (same liquid, at least the amount) and a
 * shaped 3×3 pattern (mirroring allowed, like {@code ShapedRecipeCustom}).
 */
public class FabricatorRecipe implements Recipe<FabricatorRecipe.Input> {
    private final Optional<Ingredient> plan;
    private final FluidStack molten;
    private final ShapedRecipePattern pattern;
    private final ItemStack result;

    public FabricatorRecipe(Optional<Ingredient> plan, FluidStack molten, ShapedRecipePattern pattern,
                            ItemStack result) {
        this.plan = plan;
        this.molten = molten;
        this.pattern = pattern;
        this.result = result;
    }

    public Optional<Ingredient> getPlan() {
        return plan;
    }

    public FluidStack getMolten() {
        return molten;
    }

    public ShapedRecipePattern getPattern() {
        return pattern;
    }

    public ItemStack getResult() {
        return result;
    }

    /** {@code matches(plan)}: a recipe without plan matches any plan slot. */
    public boolean matchesPlan(ItemStack stack) {
        return plan.isEmpty() || plan.get().test(stack);
    }

    /** {@code hasLiquid}: the same liquid and at least the recipe amount. */
    public boolean hasLiquid(FluidStack liquid) {
        return FluidStack.isSameFluidSameComponents(liquid, molten) && molten.getAmount() <= liquid.getAmount();
    }

    @Override
    public boolean matches(Input input, Level level) {
        return matchesPlan(input.plan()) && pattern.matches(input.grid()) && hasLiquid(input.liquid());
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
        return FrRecipes.FABRICATOR_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return FrRecipes.FABRICATOR.get();
    }

    /** Plan slot, molten tank and crafting grid of the fabricator. */
    public record Input(ItemStack plan, FluidStack liquid, CraftingInput grid) implements RecipeInput {
        @Override
        public ItemStack getItem(int index) {
            return grid.getItem(index);
        }

        @Override
        public int size() {
            return grid.size();
        }
    }

    public static final class Serializer implements RecipeSerializer<FabricatorRecipe> {
        private static final MapCodec<FabricatorRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC_NONEMPTY.optionalFieldOf("plan").forGetter(FabricatorRecipe::getPlan),
                FluidStack.CODEC.fieldOf("molten").forGetter(FabricatorRecipe::getMolten),
                ShapedRecipePattern.MAP_CODEC.forGetter(FabricatorRecipe::getPattern),
                ItemStack.STRICT_CODEC.fieldOf("result").forGetter(FabricatorRecipe::getResult)
        ).apply(instance, FabricatorRecipe::new));
        private static final StreamCodec<RegistryFriendlyByteBuf, FabricatorRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        ByteBufCodecs.optional(Ingredient.CONTENTS_STREAM_CODEC), FabricatorRecipe::getPlan,
                        FluidStack.STREAM_CODEC, FabricatorRecipe::getMolten,
                        ShapedRecipePattern.STREAM_CODEC, FabricatorRecipe::getPattern,
                        ItemStack.STREAM_CODEC, FabricatorRecipe::getResult,
                        FabricatorRecipe::new);

        @Override
        public MapCodec<FabricatorRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, FabricatorRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
