package dev.craft.thermalexpansion.recipe;

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
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * A Thermal Expansion recipe with a liquid ({@code CrucibleManager}, {@code TransposerManager}): an item input, an
 * optional item output with a chance in percent, the liquid (product of the crucible and of an extraction, ingredient of
 * a fill) and the energy in MJ.
 */
public class TeFluidRecipe implements Recipe<SingleRecipeInput> {
    private final Kind kind;
    private final SizedIngredient input;
    private final ItemStack output;
    private final FluidStack fluid;
    private final int chance;
    private final int energy;

    public TeFluidRecipe(Kind kind, SizedIngredient input, ItemStack output, FluidStack fluid, int chance, int energy) {
        this.kind = kind;
        this.input = input;
        this.output = output;
        this.fluid = fluid;
        this.chance = chance;
        this.energy = energy;
    }

    public Kind getKind() {
        return kind;
    }

    public SizedIngredient getInput() {
        return input;
    }

    /** A copy of the item output, or empty. */
    public ItemStack getOutput() {
        return output.copy();
    }

    /** A copy of the liquid. */
    public FluidStack getFluid() {
        return fluid.copy();
    }

    public int getChance() {
        return chance;
    }

    public int getEnergy() {
        return energy;
    }

    /** TE looks recipes up by item (and for fills by liquid) only; the machines take one item. */
    public boolean matchesItem(ItemStack stack) {
        return !stack.isEmpty() && input.ingredient().test(stack);
    }

    @Override
    public boolean matches(SingleRecipeInput recipeInput, Level level) {
        return matchesItem(recipeInput.item());
    }

    @Override
    public ItemStack assemble(SingleRecipeInput recipeInput, HolderLookup.Provider registries) {
        return getOutput();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return output;
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        return NonNullList.of(Ingredient.EMPTY, input.ingredient());
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

    /** The recipe lists: crucible, transposer fill and transposer extraction. */
    public enum Kind {
        CRUCIBLE("crucible"),
        FILL("transposer_fill"),
        EXTRACT("transposer_extract");

        private final String name;
        private Supplier<RecipeType<TeFluidRecipe>> type;
        private Supplier<RecipeSerializer<TeFluidRecipe>> serializer;

        Kind(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public RecipeType<TeFluidRecipe> type() {
            return type.get();
        }

        void bind(Supplier<RecipeType<TeFluidRecipe>> type, Supplier<RecipeSerializer<TeFluidRecipe>> serializer) {
            this.type = type;
            this.serializer = serializer;
        }
    }

    public static final class Serializer implements RecipeSerializer<TeFluidRecipe> {
        private final MapCodec<TeFluidRecipe> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, TeFluidRecipe> streamCodec;

        public Serializer(Kind kind) {
            this.codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    SizedIngredient.FLAT_CODEC.fieldOf("input").forGetter(TeFluidRecipe::getInput),
                    ItemStack.OPTIONAL_CODEC.optionalFieldOf("output", ItemStack.EMPTY)
                            .forGetter(recipe -> recipe.output),
                    FluidStack.CODEC.fieldOf("fluid").forGetter(recipe -> recipe.fluid),
                    Codec.intRange(0, 100).optionalFieldOf("chance", 100).forGetter(TeFluidRecipe::getChance),
                    Codec.intRange(1, Integer.MAX_VALUE).fieldOf("energy").forGetter(TeFluidRecipe::getEnergy)
            ).apply(instance, (input, output, fluid, chance, energy) ->
                    new TeFluidRecipe(kind, input, output, fluid, chance, energy)));
            this.streamCodec = StreamCodec.composite(
                    SizedIngredient.STREAM_CODEC, TeFluidRecipe::getInput,
                    ItemStack.OPTIONAL_STREAM_CODEC, recipe -> recipe.output,
                    FluidStack.STREAM_CODEC, recipe -> recipe.fluid,
                    ByteBufCodecs.VAR_INT, TeFluidRecipe::getChance,
                    ByteBufCodecs.VAR_INT, TeFluidRecipe::getEnergy,
                    (input, output, fluid, chance, energy) -> new TeFluidRecipe(kind, input, output, fluid, chance,
                            energy));
        }

        @Override
        public MapCodec<TeFluidRecipe> codec() {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, TeFluidRecipe> streamCodec() {
            return streamCodec;
        }
    }
}
