package dev.craft.gregtech.recipe;

import java.util.List;
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
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import org.jetbrains.annotations.Nullable;

/**
 * {@code GT_Recipe}: one or two sized inputs, up to four outputs, duration, EU/t and a machine specific value
 * (blast furnace heat, start EU …). One recipe type per GT recipe list ({@link Kind}). Outputs may be empty
 * placeholders (an unused electrolyzer output slot).
 */
public class GtRecipe implements Recipe<GtRecipe.Input> {
    private final Kind kind;
    private final List<SizedIngredient> inputs;
    private final List<ItemStack> outputs;
    private final int duration;
    private final int euPerTick;
    private final int special;

    public GtRecipe(Kind kind, List<SizedIngredient> inputs, List<ItemStack> outputs, int duration, int euPerTick,
                    int special) {
        this.kind = kind;
        this.inputs = List.copyOf(inputs);
        this.outputs = List.copyOf(outputs);
        this.duration = duration;
        this.euPerTick = euPerTick;
        this.special = special;
    }

    public List<SizedIngredient> getInputs() {
        return inputs;
    }

    public List<ItemStack> getOutputs() {
        return outputs;
    }

    /** Output {@code index}, or empty. */
    public ItemStack getOutput(int index) {
        return index < outputs.size() ? outputs.get(index) : ItemStack.EMPTY;
    }

    public int getDuration() {
        return duration;
    }

    public int getEuPerTick() {
        return euPerTick;
    }

    public int getSpecial() {
        return special;
    }

    /**
     * {@code GT_Recipe.isRecipeInputEqual}: input 1 (and input 2, if the recipe has one) match item and amount; with
     * {@code shapeless} the swapped order is tried first. With {@code consume} the recipe amounts are removed from the
     * stacks.
     */
    public boolean isInputEqual(boolean shapeless, boolean consume, ItemStack input1, ItemStack input2) {
        if (shapeless && isInputEqual(false, consume, input2, input1)) {
            return true;
        }
        if (input1.isEmpty() || !inputs.get(0).test(input1)) {
            return false;
        }
        if (inputs.size() > 1 && (input2.isEmpty() || !inputs.get(1).test(input2))) {
            return false;
        }
        if (consume) {
            input1.shrink(inputs.get(0).count());
            if (inputs.size() > 1) {
                input2.shrink(inputs.get(1).count());
            }
        }
        return true;
    }

    /** {@code findEqualRecipeIndex}: the first recipe of {@code kind} whose inputs match, or {@code null}. */
    @Nullable
    public static GtRecipe find(Level level, Kind kind, ItemStack input1, ItemStack input2, boolean shapeless) {
        for (RecipeHolder<GtRecipe> holder : level.getRecipeManager().getAllRecipesFor(kind.type())) {
            if (holder.value().isInputEqual(shapeless, false, input1, input2)) {
                return holder.value();
            }
        }
        return null;
    }

    @Override
    public boolean matches(Input input, Level level) {
        return isInputEqual(kind.shapeless, false, input.input1(), input.input2());
    }

    @Override
    public ItemStack assemble(Input input, HolderLookup.Provider registries) {
        return getOutput(0).copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return getOutput(0);
    }

    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        inputs.forEach(input -> list.add(input.ingredient()));
        return list;
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

    /** The two input slots of a GT machine. */
    public record Input(ItemStack input1, ItemStack input2) implements RecipeInput {
        @Override
        public ItemStack getItem(int index) {
            return index == 0 ? input1 : input2;
        }

        @Override
        public int size() {
            return 2;
        }
    }

    /** GT recipe lists ({@code GT_Recipe.s*Recipes}). */
    public enum Kind {
        BENDER("bender", false),
        ELECTROLYZER("electrolyzer", false),
        BLAST_FURNACE("blast_furnace", true);

        private final String name;
        private final boolean shapeless;
        private Supplier<RecipeType<GtRecipe>> type;
        private Supplier<RecipeSerializer<GtRecipe>> serializer;

        Kind(String name, boolean shapeless) {
            this.name = name;
            this.shapeless = shapeless;
        }

        public String getName() {
            return name;
        }

        /** Whether the machine also accepts the inputs in swapped slots ({@code findEqual*RecipeIndex}). */
        public boolean isShapeless() {
            return shapeless;
        }

        public RecipeType<GtRecipe> type() {
            return type.get();
        }

        public void bind(Supplier<RecipeType<GtRecipe>> type, Supplier<RecipeSerializer<GtRecipe>> serializer) {
            this.type = type;
            this.serializer = serializer;
        }
    }

    public static final class Serializer implements RecipeSerializer<GtRecipe> {
        private final MapCodec<GtRecipe> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, GtRecipe> streamCodec;

        public Serializer(Kind kind) {
            this.codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    SizedIngredient.FLAT_CODEC.listOf(1, 2).fieldOf("inputs").forGetter(GtRecipe::getInputs),
                    ItemStack.OPTIONAL_CODEC.listOf(1, 4).fieldOf("outputs").forGetter(GtRecipe::getOutputs),
                    Codec.intRange(1, Integer.MAX_VALUE).fieldOf("duration").forGetter(GtRecipe::getDuration),
                    Codec.INT.fieldOf("eu_per_tick").forGetter(GtRecipe::getEuPerTick),
                    Codec.INT.optionalFieldOf("special", 0).forGetter(GtRecipe::getSpecial)
            ).apply(instance, (inputs, outputs, duration, eut, special) ->
                    new GtRecipe(kind, inputs, outputs, duration, eut, special)));
            this.streamCodec = StreamCodec.composite(
                    SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), GtRecipe::getInputs,
                    ItemStack.OPTIONAL_STREAM_CODEC.apply(ByteBufCodecs.list()), GtRecipe::getOutputs,
                    ByteBufCodecs.VAR_INT, GtRecipe::getDuration,
                    ByteBufCodecs.VAR_INT, GtRecipe::getEuPerTick,
                    ByteBufCodecs.VAR_INT, GtRecipe::getSpecial,
                    (inputs, outputs, duration, eut, special) ->
                            new GtRecipe(kind, inputs, outputs, duration, eut, special));
        }

        @Override
        public MapCodec<GtRecipe> codec() {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, GtRecipe> streamCodec() {
            return streamCodec;
        }
    }
}
