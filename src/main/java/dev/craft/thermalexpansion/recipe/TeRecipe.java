package dev.craft.thermalexpansion.recipe;

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
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;

/**
 * A Thermal Expansion machine recipe ({@code FurnaceManager}, {@code PulverizerManager}, {@code SawmillManager},
 * later {@code SmelterManager}): one or two sized inputs, a primary and an optional secondary output with a chance in percent,
 * and the energy in MJ per operation.
 */
public class TeRecipe implements Recipe<TeRecipe.Input> {
    private final Kind kind;
    private final List<SizedIngredient> inputs;
    private final ItemStack primary;
    private final ItemStack secondary;
    private final int chance;
    private final int energy;

    public TeRecipe(Kind kind, List<SizedIngredient> inputs, ItemStack primary, ItemStack secondary, int chance,
                    int energy) {
        this.kind = kind;
        this.inputs = List.copyOf(inputs);
        this.primary = primary;
        this.secondary = secondary;
        this.chance = chance;
        this.energy = energy;
    }

    public Kind getKind() {
        return kind;
    }

    public List<SizedIngredient> getInputs() {
        return inputs;
    }

    /** {@code getPrimaryOutput}: a copy. */
    public ItemStack getPrimary() {
        return primary.copy();
    }

    /** {@code getSecondaryOutput}: a copy, or empty. */
    public ItemStack getSecondary() {
        return secondary.copy();
    }

    public int getChance() {
        return chance;
    }

    public int getEnergy() {
        return energy;
    }

    /** TE looks recipes up by item and metadata only; the amount is checked by the machine. */
    public boolean matchesItem(ItemStack stack) {
        return !stack.isEmpty() && inputs.get(0).ingredient().test(stack);
    }

    @Override
    public boolean matches(Input input, Level level) {
        return matchesItem(input.input1());
    }

    @Override
    public ItemStack assemble(Input input, HolderLookup.Provider registries) {
        return getPrimary();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return primary;
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

    /** The recipe lists ({@code CraftingManagers}). */
    public enum Kind {
        FURNACE("furnace"),
        PULVERIZER("pulverizer"),
        SAWMILL("sawmill");

        private final String name;
        private Supplier<RecipeType<TeRecipe>> type;
        private Supplier<RecipeSerializer<TeRecipe>> serializer;

        Kind(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public RecipeType<TeRecipe> type() {
            return type.get();
        }

        void bind(Supplier<RecipeType<TeRecipe>> type, Supplier<RecipeSerializer<TeRecipe>> serializer) {
            this.type = type;
            this.serializer = serializer;
        }
    }

    public static final class Serializer implements RecipeSerializer<TeRecipe> {
        private final MapCodec<TeRecipe> codec;
        private final StreamCodec<RegistryFriendlyByteBuf, TeRecipe> streamCodec;

        public Serializer(Kind kind) {
            this.codec = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    SizedIngredient.FLAT_CODEC.listOf(1, 2).fieldOf("inputs").forGetter(TeRecipe::getInputs),
                    ItemStack.CODEC.fieldOf("primary").forGetter(recipe -> recipe.primary),
                    ItemStack.OPTIONAL_CODEC.optionalFieldOf("secondary", ItemStack.EMPTY)
                            .forGetter(recipe -> recipe.secondary),
                    Codec.intRange(0, 100).optionalFieldOf("chance", 100).forGetter(TeRecipe::getChance),
                    Codec.intRange(1, Integer.MAX_VALUE).fieldOf("energy").forGetter(TeRecipe::getEnergy)
            ).apply(instance, (inputs, primary, secondary, chance, energy) ->
                    new TeRecipe(kind, inputs, primary, secondary, chance, energy)));
            this.streamCodec = StreamCodec.composite(
                    SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()), TeRecipe::getInputs,
                    ItemStack.STREAM_CODEC, recipe -> recipe.primary,
                    ItemStack.OPTIONAL_STREAM_CODEC, recipe -> recipe.secondary,
                    ByteBufCodecs.VAR_INT, TeRecipe::getChance,
                    ByteBufCodecs.VAR_INT, TeRecipe::getEnergy,
                    (inputs, primary, secondary, chance, energy) ->
                            new TeRecipe(kind, inputs, primary, secondary, chance, energy));
        }

        @Override
        public MapCodec<TeRecipe> codec() {
            return codec;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, TeRecipe> streamCodec() {
            return streamCodec;
        }
    }
}
